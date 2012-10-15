/*
QuiXPath: efficient evaluation of XPath queries on XML streams.
Copyright (C) 2009-2012 Innovimax and INRIA

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.quixpath.internal.xpath2fxp;

import java.util.Iterator;

import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.CardinalityChecker;
import net.sf.saxon.expr.CompareToIntegerConstant;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.GeneralComparison20;
import net.sf.saxon.expr.IsLastExpression;
import net.sf.saxon.expr.LastItemExpression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.RootExpression;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.UnaryExpression;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.VennExpression;
import net.sf.saxon.expr.instruct.ForEach;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.sort.DocumentSorter;
import net.sf.saxon.functions.BooleanFn;
import net.sf.saxon.functions.Contains;
import net.sf.saxon.functions.Count;
import net.sf.saxon.functions.Exists;
import net.sf.saxon.functions.IntegratedFunctionCall;
import net.sf.saxon.functions.NotFn;
import net.sf.saxon.functions.Position;
import net.sf.saxon.functions.StartsWith;
import net.sf.saxon.functions.StringFn;
import net.sf.saxon.functions.Subsequence;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.AnyChildNodeTest;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.LocalNameTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NamespaceTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.Value;

import com.quixpath.exceptions.UnsupportedQueryException;
import com.quixpath.interfaces.context.IStaticContext;
import com.quixpath.internal.fxpplus.IAccumulator;
import com.quixpath.internal.fxpplus.ILocal;
import com.quixpath.internal.fxpplus.ITerm;
import com.quixpath.internal.fxpplus.IText;
import com.quixpath.internal.interfaces.impl.count.Eq;
import com.quixpath.internal.interfaces.impl.count.Geq;
import com.quixpath.internal.interfaces.impl.count.Leq;
import com.quixpath.internal.interfaces.impl.count.Neq;

import fr.inria.lille.fxp.datamodel.api.IPools;
import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPFactory;
import fr.inria.lille.fxp.querylanguage.api.exception.DSTABuildingException;
import fr.inria.lille.fxp.querylanguage.internal.properties.StaticProperties;

/**
 * 
 * <p>
 * XPath2FXP is the implementation of a compiler that transform an XPath into an
 * FXP query.
 * </p>
 * 
 * <h1>Definitions</h1>
 * <p>
 * This class handles XPath 2.0. See ... to get more information.
 * </p>
 * 
 * <p>
 * FXP queries are defined in the PhD thesis of O. Gauwin: Streaming Tree
 * Automata and XPath (2009).
 * </p>
 * 
 * <h1>Algorithm to translate the query is described in Streamable Fragments of
 * Forward XPath - Olivier Gauwin and Jaochim Niehren (2010)</h1>
 * 
 * <h1>Example</h1>
 * 
 * <p>
 * Consider those three example:
 * <ul>
 * <li>
 * TODO / * in not valide in a Java doc <code>/ * / b</code> becomes
 * <code> ch(ch(b(x)) where x is a variable and b is label</code></li>
 * <li>
 * <code>//*[count(*)=2]</code> is not valid since FXP does not support counting
 * operator.</li>
 * <li>
 * TODO ... is not valid since it is not a valid XPath 2.0 query.</li>
 * </ul>
 * </p>
 */
public class XPath2FXPPlus extends XPath2Any {

	public static final String SELECTING_VARIABLE_NAME = "x";
	public static final String COUNTING_VARIABLE_NAME = "y";

	final IFXPFactory fxpTermFactory;

	protected IContext context;

	/**
	 * 
	 * 
	 * @param query
	 *            String version of the XPath query.
	 * @throws XPathExpressionException
	 *             when SAXON can not parse the query.
	 */
	public XPath2FXPPlus(final String query, final IPools pools,
			final IStaticContext staticContext) throws XPathExpressionException {
		super(query, staticContext);
		fxpTermFactory = FXPFactory.newInstance(pools);
	}

	public XPath2FXPPlus(final String query, final IPools pools)
	throws XPathExpressionException {
		this(query, pools, null);
	}

	// An FXPquery is always evaluated from the root of the document.
	protected ITerm rootQuery(final ITerm term)
	throws UnsupportedQueryException {
		if (context.isRooted()) {
			return term;
		} else {
			if (term.isLocal()) {
				return factory.chStarLocal((ILocal) term);
			}
			return factory.chStar(term);
		}
	}

	/**
	 * This is a recursive and bottom-up algorithm.
	 * 
	 * 
	 * @param xpathExp
	 *            xpathExp is an XPath query. It matches all the steeps that
	 *            have NOT been translated.
	 * @param subQuery
	 *            subQuery is an FXP query. It matches all the steeps that have
	 *            been translated.
	 * @return
	 * @throws IllegalVariableDeclarationException
	 * @throws DSTABuildingException
	 */
	// According to the type of the xpathExp, call the correct rule to transform
	// the query.
	protected ITerm toFXP(final Expression xpathExp, final ITerm subQuery)
	throws UnsupportedQueryException {
		if (xpathExp instanceof SlashExpression) {
			final SlashExpression slashExpression = (SlashExpression) xpathExp;
			return slashExpression(slashExpression, subQuery);
		}
		if (xpathExp instanceof AxisExpression) {
			final AxisExpression axisPath = (AxisExpression) xpathExp;
			return axisExpressionToFXP(axisPath, subQuery);
		}
		// Since the algorithm is bottom-up, this is the stoping condition.
		if (xpathExp instanceof RootExpression) {
			context = context.setRoot(true);
			if (!context.isFilter()) {
				return subQuery;
			}
			throw new UnsupportedQueryException(xPathQuery);
		}
		if (xpathExp instanceof DocumentSorter) {
			final DocumentSorter sorter = (DocumentSorter) xpathExp;
			return toFXP(sorter.getBaseExpression(), subQuery);
		}
		if (xpathExp instanceof BooleanFn) {
			final BooleanFn booleanFunction = (BooleanFn) xpathExp;
			return booleanFnToFXP(booleanFunction, subQuery);
		}
		if (xpathExp instanceof BooleanExpression) {
			final BooleanExpression booleanExp = (BooleanExpression) xpathExp;
			return booleanExpressionToFXP(booleanExp, subQuery);
		}
		if (xpathExp instanceof FilterExpression) {
			FilterExpression filterExpression = (FilterExpression) xpathExp;
			return filterExpressionToFXP(filterExpression, subQuery);
		}
		if (xpathExp instanceof Exists) {
			Exists existsExpression = (Exists) xpathExp;
			return existsExpressionToFXP(existsExpression, subQuery);
		}
		// The class GeneralComparison20 specializes GeneralComparison for the
		// case where the comparison is done with 2.0 semantics
		if (xpathExp instanceof GeneralComparison20) {
			GeneralComparison20 generalComparison20 = (GeneralComparison20) xpathExp;
			return generalComparison20ToFXP(generalComparison20, subQuery);
		}
		// An Atomizer is an expression corresponding essentially to the
		// fn:data() function
		if (xpathExp instanceof Atomizer) {
			Atomizer atomizer = (Atomizer) xpathExp;
			return atomizerToFXP(atomizer, subQuery);

		}
		if (xpathExp instanceof LetExpression) {
			final LetExpression letExpression = (LetExpression) xpathExp;
			return letExpressionToFXP(letExpression, subQuery);
		}
		if (xpathExp instanceof UnaryExpression) {
			final UnaryExpression unaryExpression = (UnaryExpression) xpathExp;
			return unaryExpression(unaryExpression, subQuery);
		}

		if (xpathExp instanceof CompareToIntegerConstant) {
			final CompareToIntegerConstant compareToIntegerConstant = (CompareToIntegerConstant) xpathExp;
			return compareToIntegerConstant(compareToIntegerConstant, subQuery);
		}
		if (xpathExp instanceof Count) {
			final Count count = (Count) xpathExp;
			return count(count, subQuery);
		}
		if (xpathExp instanceof IntegratedFunctionCall) {
			final IntegratedFunctionCall integratedFunctionCall = (IntegratedFunctionCall) xpathExp;
//			return factory.and(
//					subQuery,
//					integratedFunctionCall(integratedFunctionCall,
//							factory.trueQuery()));
			return integratedFunctionCall(integratedFunctionCall, subQuery);
		}
		if (xpathExp instanceof ValueComparison) {
			ValueComparison valueComparison = (ValueComparison) xpathExp;
			Expression[] operands = valueComparison.getOperands();
			argumentsLength(operands, 2);
			if (operands[0] instanceof FunctionCall) {
				FunctionCall functionCall = (FunctionCall) operands[0];
				if ("local-name".equals(functionCall.getFunctionName())) {
					if (operands[1] instanceof StringLiteral) {
						StringLiteral stringLiteral = (StringLiteral) operands[1];
						return factory.label(null,
								stringLiteral.getStringValue());
					}
				}
				if ("name".equals(functionCall.getFunctionName())) {
					if (operands[1] instanceof StringLiteral) {
						StringLiteral stringLiteral = (StringLiteral) operands[1];
						// TODO uriResolver
						final String stringValue = stringLiteral
						.getStringValue();
						final int split = stringValue.indexOf(':');
						String uri = stringValue.substring(0, split);
						String localName = stringValue.substring(split + 1);
						return factory.label(uri, localName);
					}
				}

			}
			if (operands[1] instanceof Literal) {
				Literal literal = (Literal) operands[1];
				Value value = literal.getValue();
				if (value instanceof BooleanValue) {
					BooleanValue booleanValue = (BooleanValue) value;
					if (booleanValue.getBooleanValue()) {
						return toFXP(operands[0], subQuery);
					} else {
						return factory.not(toFXP(operands[0], subQuery));
					}
				}

			}

		}
		if (xpathExp instanceof IsLastExpression) {
			// lead to bug see Q30 of Xmark test suite.
			// return factory.chLastElement(subQuery);
		}
		if (xpathExp instanceof StartsWith) {
			StartsWith startsWith = (StartsWith) xpathExp;
			return startsWith(startsWith);
		}
		if (xpathExp instanceof Contains) {
			Contains contains = (Contains) xpathExp;
			return contains(contains);
		}
		if (xpathExp instanceof FirstItemExpression) {
			FirstItemExpression firstItemExpression = (FirstItemExpression) xpathExp;
			return firstItemExpression(firstItemExpression, subQuery);
		}
		if (xpathExp instanceof ContextItemExpression) {
			return subQuery;
		}
		if (xpathExp instanceof VennExpression) {
			VennExpression vennExpression = (VennExpression) xpathExp;
			return vennExpression(vennExpression, subQuery);
		}
		if (xpathExp instanceof BinaryExpression) {
			BinaryExpression binaryExpression = (BinaryExpression) xpathExp;
			return binaryExpression(binaryExpression, subQuery);
		}
		if (xpathExp instanceof NotFn) {
			NotFn notFn = (NotFn)xpathExp;
			return notFn(notFn, subQuery);
		}
		if (xpathExp instanceof Subsequence) {
			Subsequence subsequence = (Subsequence)xpathExp;
			return subsequence(subsequence, subQuery);
		}
		throw new UnsupportedQueryException(xPathQuery);
	}


	private ITerm subsequence(Subsequence subsequence, ITerm subQuery) throws UnsupportedQueryException {
		Expression[] exp = subsequence.getArguments();
		argumentsLength(exp, 3);
		if (!(exp[1] instanceof Literal) || !(exp[2] instanceof Literal)) {
//			Literal down = (Literal)exp[1];
//			Literal up = (Literal)exp[2];
//			int downValue = toInt(down);
//			int upValue = toInt(up);
//			if (downValue + 2 != upValue) {
//				//operator = new Eq(downValue+1);
//				throw new UnsupportedQueryException(xPathQuery);
//			}
			throw new UnsupportedQueryException(xPathQuery);
			
		}
		if (operator == null) {
			throw new UnsupportedQueryException(xPathQuery);
		}
		return count(subQuery, exp[0]);
		// throw new UnsupportedQueryException(xPathQuery);
	}

	private int toInt(Literal down) throws UnsupportedQueryException  {
		try{
		return Integer.parseInt(down.getValue().getStringValue());
		}catch(Throwable e) {
			throw new UnsupportedQueryException(e, xPathQuery);
		}
	}

	private ITerm notFn(NotFn notFn, ITerm subQuery) throws UnsupportedQueryException {
		final Iterator<Expression> arguments = notFn
		.iterateSubExpressions();
		final ITerm argument = toFXP(arguments.next(), subQuery);
		assert !arguments.hasNext();
		if (isCounting()) {
			throw new UnsupportedQueryException(xPathQuery);
		}
		if (argument.containsChildStar()) {
			throw new UnsupportedQueryException(xPathQuery);
		}
		if (argument.isLocal()) {
			return factory.localNot((ILocal) argument);
		} else {
			return factory.not(argument);
		}
	}

	private ITerm vennExpression(VennExpression vennExpression, ITerm subQuery)
	throws UnsupportedQueryException {
//		if (vennExpression.getOperator() == Token.UNION) {
//			Expression[] operands = vennExpression.getOperands();
//			argumentsLength(operands, 2);
//			return factory.or(toFXP(operands[0], subQuery),
//					toFXP(operands[1], subQuery));
//		}
		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm binaryExpression(BinaryExpression binaryExpression,
			ITerm subQuery) throws UnsupportedQueryException {
		if (binaryExpression.getOperator() == Token.FEQ) {
			Expression[] operands = binaryExpression.getOperands();
			argumentsLength(operands, 2);
			return textEquality(subQuery, operands);
		}
		throw new UnsupportedQueryException(xPathQuery);
	}


	private ITerm unaryExpression(UnaryExpression unaryExpression,
			ITerm subQuery) throws UnsupportedQueryException {
		if ("lazy".equals(unaryExpression.getExpressionName())) {
			return toFXP(unaryExpression.getBaseExpression(), subQuery);
		}
		if ("lastItem".equals(unaryExpression.getExpressionName())) {
			return lastItemExpression(unaryExpression, subQuery);
		}
		if ("firstItem".equals(unaryExpression.getExpressionName())) {
			return firstItemExpression(unaryExpression, subQuery);
		}
		throw new UnsupportedQueryException(xPathQuery);
		// return toFXP(lazyExpression.getBaseExpression(), subQuery);

	}

	private ITerm firstItemExpression(UnaryExpression unaryExpression,
			ITerm subQuery) throws UnsupportedQueryException {
		final Expression arg = unaryExpression.getBaseExpression();
		final ITerm term = toFXP(arg, factory.trueQuery());
		if (term.isChildElementPattern()) {
			return factory.chithElement(1, subQuery);
		}
		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm lastItemExpression(UnaryExpression unaryExpression,
			ITerm subQuery) throws UnsupportedQueryException {
		final Expression arg = unaryExpression.getBaseExpression();
		final ITerm term = toFXP(arg, factory.trueQuery());
		if (term.isChildElementPattern()) {
			return factory.chLastElement(subQuery);
		}
		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm slashExpression(SlashExpression slashExpression,
			ITerm subQuery) throws UnsupportedQueryException {
		return toFXP(slashExpression.getControllingExpression(),
				toFXP(slashExpression.getControlledExpression(), subQuery));

	}

	// TODO contains and startsWith method share a lot of code
	private ITerm contains(Contains contains) throws UnsupportedQueryException {
		Expression[] operands = contains.getArguments();
		argumentsLength(operands, 2);
		if (operands[0] instanceof CardinalityChecker) {
			CardinalityChecker cardinalityChecker = (CardinalityChecker) operands[0];
			Expression baseExpression = cardinalityChecker.getBaseExpression();
			if (baseExpression instanceof ForEach) {
				ForEach forEach = (ForEach) baseExpression;
				Expression selectExpression = forEach.getSelectExpression();
				if (operands[1] instanceof StringLiteral) {
					StringLiteral stringLiteral = (StringLiteral) operands[1];
					return toFXP(selectExpression,
							factory.stringMatches(factory
									.contains(stringLiteral.getStringValue())));
				}
			}
		} else {
			if (operands[0] instanceof Atomizer) {
				Atomizer atomizer = (Atomizer) operands[0];
				Expression exp = atomizer.getBaseExpression();
				if (operands[1] instanceof StringLiteral) {
					StringLiteral stringLiteral = (StringLiteral) operands[1];
					return toFXP(exp, factory.localStringMatches(factory
							.contains(stringLiteral.getStringValue())));
				}
			}
		}

		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm startsWith(StartsWith startsWith)
	throws UnsupportedQueryException {
		Expression[] operands = startsWith.getArguments();
		argumentsLength(operands, 2);
		if (operands[0] instanceof CardinalityChecker) {
			CardinalityChecker cardinalityChecker = (CardinalityChecker) operands[0];
			Expression baseExpression = cardinalityChecker.getBaseExpression();
			if (baseExpression instanceof ForEach) {
				ForEach forEach = (ForEach) baseExpression;
				Expression selectExpression = forEach.getSelectExpression();
				if (operands[1] instanceof StringLiteral) {
					StringLiteral stringLiteral = (StringLiteral) operands[1];
					return toFXP(selectExpression,
							factory.stringMatches(factory
									.startWith(stringLiteral.getStringValue())));
				}
			}
		}
		if (operands[0] instanceof StringFn) {
			StringFn stringFn = (StringFn) operands[0];
			Expression[] operand = stringFn.getArguments();
			argumentsLength(operand, 1);
			if (operand[0] instanceof ContextItemExpression) {
				if (operands[1] instanceof StringLiteral) {
					StringLiteral stringLiteral = (StringLiteral) operands[1];
					return factory.stringMatches(factory
							.startWith(stringLiteral.getStringValue()));
				}
			}
		}
		throw new UnsupportedQueryException(xPathQuery);
	}


	private ITerm integratedFunctionCall(
			IntegratedFunctionCall integratedFunctionCall, ITerm subQuery)
	throws UnsupportedQueryException {
		final String qName = integratedFunctionCall.getFunctionName()
		.toString();
		if (qName.startsWith("saxon")) {
			if (qName.endsWith("item-at")) {
				final Expression[] args = integratedFunctionCall.getArguments();
				argumentsLength(args, 2);
				final InternalPosition isPosition = isPosition(args[0], args[1]);
				if (isPosition.isPosition()) {
					return factory.chithElement(isPosition.litteral, subQuery);
				}
			}

		}
		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm count(final Count count, final ITerm subQuery)
	throws UnsupportedQueryException {
		final Expression[] args = count.getArguments();
		return count(subQuery, args);

	}

	private ITerm count(final ITerm subQuery, final Expression[] args)
	throws UnsupportedQueryException {
		argumentsLength(args, 1);
		countAtTopLevel = true;
		return toFXP(args[0], subQuery);
	}

	private ITerm compareToIntegerConstant(
			CompareToIntegerConstant compareToIntegerConstant, ITerm subQuery)
	throws UnsupportedQueryException {
		if (operator != null) {
			throw new UnsupportedQueryException(xPathQuery);
		}
		makeOperator(compareToIntegerConstant);
		if (operator != null) {
			final Expression operand = compareToIntegerConstant.getOperand();
			if (operand instanceof Count) {
				final Count count = (Count) operand;
				final Expression[] args = count.getArguments();
				argumentsLength(args, 1);
				Expression countExp = args[0];
				return count(subQuery, countExp);
			}
			if (operand instanceof Position) {
				return factory.chithElement(
						compareToIntegerConstant.getComparand(), subQuery);
			}

		}
		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm count(ITerm subQuery, Expression countExp)
	throws UnsupportedQueryException {
		if (subQuery.isVariable()) {
			return toFXP(countExp,
					subQuery);
		} else {
		final ITerm fxp = toFXP(countExp,
				factory.variable(COUNTING_VARIABLE_NAME));
		return factory.and(fxp, subQuery);
		}
	}

	private void makeOperator(CompareToIntegerConstant compareToIntegerConstant) {
		if (compareToIntegerConstant.getComparisonOperator() == Token.FEQ) {
			operator = new Eq(compareToIntegerConstant.getComparand());
		} else if (compareToIntegerConstant.getComparisonOperator() == Token.FGT) {
			// Operator ">"
			operator = new Geq(compareToIntegerConstant.getComparand() + 1);
		} else if (compareToIntegerConstant.getComparisonOperator() == Token.FLT) {
			// Operator "<"
			operator = new Leq(compareToIntegerConstant.getComparand() - 1);
		} else if (compareToIntegerConstant.getComparisonOperator() == Token.FGE) {
			// Operator ">="
			operator = new Geq(compareToIntegerConstant.getComparand());
		} else if (compareToIntegerConstant.getComparisonOperator() == Token.FLE) {
			// Operator "<="
			operator = new Leq(compareToIntegerConstant.getComparand());
		} else if (compareToIntegerConstant.getComparisonOperator() == Token.FNE) {
			// Operator "!="
			operator = new Neq(compareToIntegerConstant.getComparand());
		}

	}



	private ITerm letExpressionToFXP(LetExpression letExpression, ITerm subQuery)
	throws UnsupportedQueryException {
		throw new UnsupportedQueryException(xPathQuery);
		// I suspect that there is a bug in SAXON in the getSequence method.
		// return toFXP(letExpression.getSequence(), subQuery);

	}



	private ITerm atomizerToFXP(final Atomizer atomizer, final ITerm subQuery)
	throws UnsupportedQueryException {
		return toFXP(atomizer.getBaseExpression(), subQuery);
	}

	private ITerm generalComparison20ToFXP(
			final GeneralComparison20 generalComparison20, final ITerm subQuery)
	throws UnsupportedQueryException {
		if (generalComparison20.getOperator() == Token.EQUALS) {
			final Expression[] operands = generalComparison20.getOperands();
			argumentsLength(operands, 2);
			return textEquality(subQuery, operands);
		}
		throw new UnsupportedQueryException();

	}

	private ITerm textEquality(ITerm subQuery, Expression... operands)
	throws UnsupportedQueryException {
		if (operands[1] instanceof StringLiteral) {
			final StringLiteral stringLitteral = (StringLiteral) operands[1];
			final String stringLitteralValue = stringLitteral.getStringValue();
			context = context.setStepsEqualsStringLitteral(stringLitteralValue);
			return toFXP(operands[0], subQuery);
		}
		throw new UnsupportedQueryException();
	}

	private ITerm existsExpressionToFXP(final Exists existsExpression,
			final ITerm subQuery) throws UnsupportedQueryException {
		final Expression[] arguments = existsExpression.getArguments();
		argumentsLength(arguments, 1);
		return toFXP(arguments[0], subQuery);

	}

	private void argumentsLength(final Expression[] arguments, final int length)
	throws UnsupportedQueryException {
		// assert arguments.length == length;
		if (arguments.length != length) {
			throw new UnsupportedQueryException(xPathQuery);
		}
	}

	private ITerm booleanExpressionToFXP(final BooleanExpression booleanExp,
			final ITerm subQuery) throws UnsupportedQueryException {
		if (booleanExp.getOperator() == Token.AND) {
			final Expression[] arguments = booleanExp.getOperands();
			argumentsLength(arguments, 2);
			final ITerm left = toFXP(arguments[0], subQuery); // TODO
			// subQuery
			// is
			// always true?
			final ITerm right = toFXP(arguments[1], factory.trueQuery());
			argumentsLength(arguments, 2);
			return factory.and(left, right);
		}
		if (booleanExp.getOperator() == Token.OR) {
			final Expression[] arguments = booleanExp.getOperands();
			argumentsLength(arguments, 2);
			final ITerm left = toFXP(arguments[0], subQuery); // TODO
			// subQuery is always true
			final ITerm right = toFXP(arguments[1], factory.trueQuery());
			argumentsLength(arguments, 2);
			return factory.or(left, right);
		}
		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm booleanFnToFXP(final BooleanFn booleanFunction,
			final ITerm subQuery) throws UnsupportedQueryException {
		// TODO How to get a function. By its name?
		if (booleanFunction.getDisplayName().equals("not")) {
			final Iterator<Expression> arguments = booleanFunction
			.iterateSubExpressions();
			final ITerm argument = toFXP(arguments.next(), subQuery);
			assert !arguments.hasNext();
			if (isCounting()) {
				throw new UnsupportedQueryException(xPathQuery);
			}
			if (argument.containsChildStar()) {
				throw new UnsupportedQueryException(xPathQuery);
			}
			if (argument.isLocal()) {
				return factory.localNot((ILocal) argument);
			} else {
				return factory.not(argument);
			}
		}

		throw new UnsupportedQueryException(xPathQuery);
	}



	private ITerm firstItemExpression(FirstItemExpression firstItemExpression,
			ITerm subQuery) throws UnsupportedQueryException {
		final Expression arg = firstItemExpression.getBaseExpression();
		final ITerm term = toFXP(arg, factory.trueQuery());
		if (term.isChildElementPattern()) {
			return factory.chithElement(1, subQuery);
		}
		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm lastItemExpression(LastItemExpression lastItemExpression,
			ITerm subQuery) throws UnsupportedQueryException {
		final Expression arg = lastItemExpression.getBaseExpression();
		final ITerm term = toFXP(arg, factory.trueQuery());
		if (term.isChildElementPattern()) {
			return factory.chLastElement(subQuery);
		}
		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm filterExpressionToFXP(final FilterExpression filterExp,
			final ITerm subQuery) throws UnsupportedQueryException {
		context = context.enterFilter();
		newSteps();
		final Expression filter = filterExp.getFilter();
		final Expression controllingExpression = filterExp
		.getControllingExpression();

		final InternalPosition iposition = isPosition(controllingExpression,
				filter);
		if (iposition.isPosition()) {
			context = context.exitFilter();
			return factory.chithElement(iposition.litteral, subQuery);
		} else {
			if (iposition.litteral != -1) { // TODO magic number
				context = context.exitFilter();
				return count(subQuery, controllingExpression);
			}
		}

		if (controllingExpression instanceof IntegratedFunctionCall) {
			final IntegratedFunctionCall integratedFunctionCall = (IntegratedFunctionCall) controllingExpression;
			final ITerm res = integratedFunctionCall(integratedFunctionCall,
					factory.and(subQuery, toFXP(filter, factory.trueQuery())));
			context = context.exitFilter();
			return res;
		}

		final ITerm left = subQuery;
		final ITerm right = toFXP(filter, factory.trueQuery());
		final ITerm and = factory.and(left, right);
		context = context.exitFilter();
		return toFXP(controllingExpression, and);
	}

	private InternalPosition isPosition(Expression controllingExpression,
			Expression filter) {
		InternalPosition res = new InternalPosition();
		if (controllingExpression instanceof AxisExpression) {
			try {
				ITerm term = axisExpressionToFXP(
						(AxisExpression) controllingExpression,
						factory.trueQuery());
				if (term.isChildElementPattern()) {
					res.isChildStartPattern = true;
				}
			} catch (UnsupportedQueryException e) {
				return res;
			}
		} else {
			return res;
		}
		if (filter instanceof Literal) {
			final Literal literal = (Literal) filter;
			try {
				final String value = literal.getValue().getStringValue();
				try {
					final int intValue = Integer.parseInt(value);
					res.litteral = intValue;
					return res;
				} catch (NumberFormatException e) {
					return res;
				}
			} catch (XPathException e) {
				return res;
			}
		} else {
			return res;
		}
	}

	private ITerm axisExpressionToFXP(final AxisExpression axisExpression,
			final ITerm subQuery) throws UnsupportedQueryException {
		final NodeTest test = axisExpression.getNodeTest();
		final ITerm res;
		if (axisExpression.getAxis() == Axis.CHILD) {
			res = childAxis(test, subQuery);
		} else if (axisExpression.getAxis() == Axis.DESCENDANT_OR_SELF) {
			res = descedantOrSelfAxis(test, subQuery);
		} else if (axisExpression.getAxis() == Axis.DESCENDANT) {
			res = descendantAxis(test, subQuery);
		} else if (axisExpression.getAxis() == Axis.ATTRIBUTE) {
			res = nodeTestToAttribute(test, subQuery);
		} else if (axisExpression.getAxis() == Axis.SELF) {
			res = nodeTestToFXP(test, subQuery);
		} else {
			res = null;
		}
		nextStep();
		if (res != null) {
			return res;
		}
		throw new UnsupportedQueryException(xPathQuery);
	}

	// TODO many code is copy/paste between
	// descedantOrSelfAxis/descedantAxis/ChildAxis
	private ITerm descedantOrSelfAxis(NodeTest test, ITerm subQuery)
	throws UnsupportedQueryException {
		final ITerm nodeTestTerm = nodeTestToFXP(test, subQuery);
		if (context.isInitialisation()) {
			if (nodeTestTerm.isLocal()) {
				ILocal localOp = (ILocal) nodeTestTerm;
				if (subQuery.isElement()) {
					return factory.chStarLocal(0, localOp);
				}
				return factory.chStarLocal(localOp);
			}
			return factory.chStar(nodeTestTerm);
		} else { // recursion
			// Used to fix Bug 53. 
			if (nodeTestTerm.isAcumulator()) {
				IAccumulator acc = (IAccumulator) nodeTestTerm;				
				return factory.chStarLocal(acc.getN(), acc.getLocalOp());
			}
			return descedantOrSelfAxisRec(test, nodeTestTerm, subQuery);
		}
	}

	private ITerm descedantOrSelfAxisRec(NodeTest test, ITerm nodeTestTerm,
			ITerm subQuery) throws UnsupportedQueryException {
		if (subQuery.isAcumulator()) {
			IAccumulator acc = (IAccumulator) subQuery;
			if (isElementNodeKindTest(test)) {
				if (isElementNodeKindTest(test)) {
					return factory.chStarLocal(acc.getN(), acc.getLocalOp());
				}
			}
			if (test instanceof NameTest) {
				final NameTest nameTest = (NameTest) test;
				if (acc.getN() == 0) {
					if (acc.isChildStartLocalAccumulator()) {
						return nameTest2Outermost(subQuery, nameTest);
					}
				}
			}
			if (test instanceof LocalNameTest) {
				final ITerm outermost = localNameTest2Outermost(test, subQuery,
						acc);
				if (outermost != null) {
					return outermost;
				}
			}
		}
		if (nodeTestTerm.isLocal()) {
			ILocal localOp = (ILocal) nodeTestTerm;
			if (subQuery.isElement()) {
				return factory.chStarLocal(0, localOp);
			}
			return factory.chStarLocal(localOp);
		}
		if (subQuery.isOuterMost()) {
			if (test instanceof NameTest) {
				return nameTest2Outermost(subQuery, (NameTest) test);
			}
			if (test instanceof LocalNameTest) {
				final LocalNameTest nameTest = (LocalNameTest) test;
				final String a = nameTest.getLocalName();
				return factory.outermostChStart(a, subQuery);
			}
		}
		return factory.chStar(nodeTestTerm);
	}

	private ITerm localNameTest2Outermost(NodeTest test, ITerm subQuery,
			IAccumulator acc) {
		final LocalNameTest nameTest = (LocalNameTest) test;
		final String a = nameTest.getLocalName();
		if (acc.getN() == 0) {
			if (acc.isChildStartLocalAccumulator()) {
				final ITerm outermost = factory.outermostChStart(a, subQuery);
				return outermost;
			}
		}
		return null;
	}

	private ITerm nameTest2Outermost(ITerm subQuery, final NameTest nameTest) {
		final String a = namePool.getLocalName(nameTest.getFingerprint());
		final String uri = namePool.getURI(nameTest.getFingerprint());
		final ITerm _subQuery;
		if (uri == null) {
			_subQuery = subQuery;
		} else {
			_subQuery = factory.and(factory.namespace(uri), subQuery);
		}
		final ITerm outermost = factory.outermostChStart(a, _subQuery);
		return outermost;
	}

	private ITerm descendantAxis(NodeTest test, ITerm subQuery)
	throws UnsupportedQueryException {
		// TODO some accumulation is lost.
		return childAxis(NodeKindTest.ELEMENT/* AnyNodeTest.getInstance() */,
				// ELEMENT is OK since the fake root is an element
				descedantOrSelfAxis(test, subQuery));
	}

	private ITerm childAxis(NodeTest test, ITerm subQuery)
	throws UnsupportedQueryException {
		final ITerm nodeTestTerm = nodeTestToFXP(test, subQuery);
		if (context.isInitialisation()) {
			if (nodeTestTerm.isLocal()) {
				ILocal localOp = (ILocal) nodeTestTerm;
				if (subQuery.isElement()) {
					return factory.ch(1, localOp);
				}
				return factory.ch(localOp);
			}
			return factory.ch(nodeTestTerm);
		} else { // recursion
			if (nodeTestTerm.isAcumulator()) {
				IAccumulator acc = (IAccumulator) subQuery;
				if (isElementNodeKindTest(test)) {
					if (acc.isChildAccumulator()) {
						return factory.ch(acc.getN() + 1, acc.getLocalOp());
					}
					if (acc.isChildStartLocalAccumulator()) {
						return factory.chStarLocal(acc.getN() + 1,
								acc.getLocalOp());
					}
				}
			}
			if (nodeTestTerm.isLocal()) {
				ILocal localOp = (ILocal) nodeTestTerm;
				if (subQuery.isElement()) {
					return factory.ch(1, localOp);
				}
				return factory.ch(localOp);
			}
			return factory.ch(nodeTestTerm);
		}
	}

	private ITerm nodeTestToAttribute(final NodeTest test, final ITerm subQuery)
	throws UnsupportedQueryException {
		if (context.isInitialisation()) {
			final ITerm subTerm;
			if (context.isStepsEqualsStringLitteral()) {
				subTerm = factory.and(subQuery, factory
						.localStringMatches(factory.equals(context
								.getStepsEqualsStringLitteral())));
				context = context.setStepsEqualsStringLitteral(null);
			} else {
				subTerm = subQuery;
			}
			if (test instanceof NameTest) {
				// label
				final NameTest nameTest = (NameTest) test;
				final String label = namePool.getLocalName(nameTest
						.getFingerprint());
				final String uri = namePool.getURI(nameTest.getFingerprint());
				if (subQuery.isText()) {
					return factory.attribute(label, uri, (IText) subTerm);
				}

			} else if (test instanceof NodeKindTest) {
				// *
				if (subQuery.isText()) {
					return factory.attribute(null, (IText) subTerm);
				}
			} else if (test instanceof LocalNameTest) {
				// *:localname
				final LocalNameTest localNameTest = (LocalNameTest) test;
				final String label = localNameTest.getLocalName();
				if (subQuery.isText()) {
					return factory.attribute(label, null, (IText) subTerm);
				}
			} else if (test instanceof NamespaceTest) {
				final NamespaceTest namespaceTest = (NamespaceTest) test;
				if (subQuery.isText()) {
					return factory.attribute(null,
							namespaceTest.getNamespaceURI(), (IText) subTerm);
				}
			}
		}

		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm nodeTestToFXP(final NodeTest test, final ITerm subQuery)
	throws UnsupportedQueryException {
		if (test instanceof NameTest) {
			return nameTestToFXP(test, subQuery);
		} else if (test instanceof NodeKindTest) {
			return nodeKindTestToFXP(test, subQuery);
		}
		if (test instanceof AnyNodeTest) {
			if (!(context.isInitialisation() && context
					.isStepsEqualsStringLitteral()))
				return subQuery;
		}
		if (test instanceof LocalNameTest) {
			final LocalNameTest localNameTest = (LocalNameTest) test;
			final String label = localNameTest.getLocalName();
			final ILocal element = factory.element();
			if (subQuery.isLocal()) {
				final ILocal local = (ILocal) subQuery;
				return factory.localAnd(element,
						factory.localAnd(factory.label(null, label), local));
			} else {
				return factory.label(null, label, subQuery);
			}
		}
		if (test instanceof NamespaceTest) {
			final NamespaceTest namespaceTest = (NamespaceTest) test;
			return factory.and(
					factory.namespace(namespaceTest.getNamespaceURI()),
					subQuery);
		}
		if (test instanceof AnyChildNodeTest) {
			return factory.trueQuery();
		}
		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm nodeKindTestToFXP(final NodeTest test, final ITerm subQuery)
	throws UnsupportedQueryException {
		final NodeKindTest kindTest = (NodeKindTest) test;
		if (kindTest.equals(NodeKindTest.ELEMENT)) {
			if (subQuery.isAcumulator()) {
				return subQuery; // element are already accumulated.
			}
			return factory.and(factory.element(), subQuery);
		}
		if (kindTest.equals(NodeKindTest.TEXT)) {
			if (context.isStepsEqualsStringLitteral()) {
				final String d = context.getStepsEqualsStringLitteral();
				context = context.setStepsEqualsStringLitteral(null);
				return factory.and(subQuery,
						factory.localStringMatches(factory.equals(d)));
			}
			return factory.and(factory.text(), subQuery);
		}
		if (kindTest.equals(NodeKindTest.ATTRIBUTE)) {
			throw new UnsupportedQueryException(xPathQuery);
		}
		if (kindTest.equals(NodeKindTest.COMMENT)) {
			if (context.isStepsEqualsStringLitteral()) {
				final String d = context.getStepsEqualsStringLitteral();
				context = context.setStepsEqualsStringLitteral(null);
				return factory.and(factory.and(factory.comment(), subQuery),
						factory.localStringMatches(factory.equals(d)));
			}
			return factory.and(factory.comment(), subQuery);
		}
		if (kindTest.equals(NodeKindTest.DOCUMENT)) {
			throw new UnsupportedQueryException(xPathQuery);
		}
		if (kindTest.equals(NodeKindTest.NAMESPACE)) {
			final String ns = namePool.getLocalName(kindTest.getFingerprint());
			return factory.and(factory.namespace(ns), subQuery);
		}
		if (kindTest.equals(NodeKindTest.PROCESSING_INSTRUCTION)) {
			// is subquery always equals to true
			return factory.and(factory.PI(factory.trueQuery()), subQuery);
		}

		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm nameTestToFXP(final NodeTest test, final ITerm subQuery)
	throws UnsupportedQueryException {
		// label
		final NameTest nameTest = (NameTest) test;
		final String label = namePool.getLocalName(nameTest.getFingerprint());
		final String uri = namePool.getURI(nameTest.getFingerprint());
		final ILocal labelTerm = factory.label(uri, label);
		ITerm res = null;

		if (nameTest.getPrimitiveItemType().getPrimitiveType() == Type.ELEMENT) {
			final ILocal element = factory.element();
			if (subQuery.isLocal()) {
				final ILocal local = (ILocal) subQuery;
				res = factory.localAnd(element,
						factory.localAnd(labelTerm, local));
			} else {
				// TODO is element and a(non-local) equivalent to
				// a(non-local) ?
				// res = factory.and(subQuery,
				// factory.localAnd(element, labelTerm));
				res = factory.label(uri, label, subQuery);
			}
		}
		if (nameTest.getPrimitiveItemType().getPrimitiveType() == Type.PROCESSING_INSTRUCTION) {
			// is subquery always equals to true
			res = factory.and(subQuery, factory.PI(labelTerm));
		}
		if (res != null) {
			return res;
		}
		throw new UnsupportedQueryException(xPathQuery);
	}

	private boolean isElementNodeKindTest(final NodeTest test) {
		if (test instanceof NodeKindTest) {
			// *
			final NodeKindTest kindTest = (NodeKindTest) test;
			if (kindTest.equals(NodeKindTest.ELEMENT)) {
				return true;
			}
		}
		return false;
	}

	private boolean newStep = false;

	protected void newSteps() {
		context = context.newSteps();
		newStep = true;
	}

	private void nextStep() {
		if (newStep) {
			newStep = false;
			context = context.nextStep();
		}
	}
}