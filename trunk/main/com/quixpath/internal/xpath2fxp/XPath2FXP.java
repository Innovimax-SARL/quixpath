/*
QuiXPath: efficient evaluation of XPath queries on XML streams.
Copyright (C) 2011 Innovimax and INRIA

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
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.GeneralComparison20;
import net.sf.saxon.expr.PathExpression;
import net.sf.saxon.expr.RootExpression;
import net.sf.saxon.expr.SingletonComparison;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.Token;
import net.sf.saxon.expr.sort.DocumentSorter;
import net.sf.saxon.functions.BooleanFn;
import net.sf.saxon.functions.Exists;
import net.sf.saxon.om.Axis;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;

import com.quixpath.exceptions.UnsupportedQueryException;
import com.quixpath.internal.fxpplus.IAccumulator;
import com.quixpath.internal.fxpplus.ILocal;
import com.quixpath.internal.fxpplus.ITerm;
import com.quixpath.internal.fxpplus.IText;

import fr.inria.lille.fxp.querylanguage.api.DSTABuildingException;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

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
public class XPath2FXP extends XPath2Any {

	public static final String SELECTING_VARIABLE_NAME = "x";

	private IContext context;

	/**
	 * 
	 * 
	 * @param query
	 *            String version of the XPath query.
	 * @throws XPathExpressionException
	 *             when SAXON can not parse the query.
	 */
	public XPath2FXP(final String query) throws XPathExpressionException {
		super(query);
	}

	// as exp is immutable, we can cache the result of the toFXP method
	private IFXPTerm term = null;

	/**
	 * 
	 * Convert the XPath into an FXP query.
	 * 
	 * Variable of the query is always named 'x'.
	 * 
	 * @return FXP query equivalent to the XPath query. when the query can not
	 *         be expressed with FXP.
	 */
	public IFXPTerm toFXP() throws UnsupportedQueryException {
		if (term == null) {
			context = new FXPContext();
			final Expression internalExp = xPathExecutable
					.getUnderlyingExpression().getInternalExpression();
			namePool = xPathExecutable.getUnderlyingStaticContext()
					.getNamePool();
			newSteps();
			final ITerm fxp = toFXP(internalExp,
					factory.variable(SELECTING_VARIABLE_NAME));
			term = rootQuery(fxp).expand();
		}
		return term;
	}

	// An FXPquery is always evaluated from the root of the document.
	private ITerm rootQuery(final ITerm term) throws UnsupportedQueryException {
		if (context.isRooted()) {
			return term;
		} else {
			if (term.isLocal()) {
				return factory.chStarLocal((ILocal) term);
			}
			throw new UnsupportedQueryException(xPathQuery);
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
	private ITerm toFXP(final Expression xpathExp, final ITerm subQuery)
			throws UnsupportedQueryException {
		if (xpathExp instanceof PathExpression) {
			final PathExpression pathExp = (PathExpression) xpathExp;
			return pathExpressionToFXP(pathExp, subQuery);
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

		if (xpathExp instanceof SingletonComparison) {
			SingletonComparison singletonComparison = (SingletonComparison) xpathExp;
			return singletonComparisonToFXP(singletonComparison, subQuery);
		}
		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm singletonComparisonToFXP(
			SingletonComparison singletonComparison, ITerm subQuery)
			throws UnsupportedQueryException {
		if (singletonComparison.getSingletonOperator() == Token.FEQ) {
			final Expression[] operands = singletonComparison.getOperands();
			argumentsLength(operands, 2);
			return textEquality(subQuery, operands);
		}
		throw new UnsupportedQueryException();
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
			if (argument.isLocal()) {
				return factory.localNot((ILocal) argument);
			} else {
				return factory.not(argument);
			}
		}

		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm pathExpressionToFXP(final PathExpression pathExp,
			final ITerm subQuery) throws UnsupportedQueryException {
		final Expression step = pathExp.getLastStep();
		if (step instanceof AxisExpression) {
			final AxisExpression axis = (AxisExpression) step;
			final Expression start = pathExp.getLeadingSteps();
			return toFXP(start, axisExpressionToFXP(axis, subQuery));
		}
		if (step instanceof FilterExpression) {
			final FilterExpression filterExpression = (FilterExpression) step;
			final Expression leadingSteps = pathExp.getLeadingSteps();
			return toFXP(leadingSteps,
					filterExpressionToFXP(filterExpression, subQuery));
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

		final ITerm left = subQuery;
		final ITerm right = toFXP(filter, factory.trueQuery());
		final ITerm and = factory.and(left, right);
		context = context.exitFilter();
		return toFXP(controllingExpression, and);
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
			res = descedantAxis(test, subQuery);
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
				final String a = namePool.getLocalName(nameTest
						.getFingerprint());
				if (acc.getN() == 0) {
					if (acc.isChildStartLocalAccumulator()) {
						return factory.outermostChStart(a, subQuery);
					}
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
				final NameTest nameTest = (NameTest) test;
				final String a = namePool.getLocalName(nameTest
						.getFingerprint());
				return factory.outermostChStart(a, subQuery);
			}
		}
		return factory.chStar(nodeTestTerm);
	}

	private ITerm descedantAxis(NodeTest test, ITerm subQuery)
			throws UnsupportedQueryException {
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
				if (namePool.getNamespaceCode(nameTest.getFingerprint()) != 0) {
					throw new UnsupportedQueryException(xPathQuery);
				}
				if (subQuery.isText()) {
					return factory.attribute(label, (IText) subTerm);
				}

			} else if (test instanceof NodeKindTest) {
				// *
				if (subQuery.isText()) {
					return factory.attribute((IText) subTerm);
				}
			}
		}

		throw new UnsupportedQueryException(xPathQuery);
	}

	private ITerm nodeTestToFXP(final NodeTest test, final ITerm subQuery)
			throws UnsupportedQueryException {
		if (test instanceof NameTest) {
			// label
			final NameTest nameTest = (NameTest) test;
			final String label = namePool.getLocalName(nameTest
					.getFingerprint());
			if (namePool.getNamespaceCode(nameTest.getFingerprint()) != 0) {
				throw new UnsupportedQueryException(xPathQuery);
			}
			if (subQuery.isLocal()) {
				final ILocal local = (ILocal) subQuery;
				final ILocal labelTerm = factory.label(label);
				return factory.localAnd(labelTerm, local);
			} else {
				return factory.label(label, subQuery);
			}
		} else if (test instanceof NodeKindTest) {
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
				return factory.and(factory.comment(), subQuery);
			}
			if (kindTest.equals(NodeKindTest.DOCUMENT)) {
				throw new UnsupportedQueryException(xPathQuery);
			}
			if (kindTest.equals(NodeKindTest.NAMESPACE)) {
				final String ns = namePool.getLocalName(kindTest
						.getFingerprint());
				return factory.and(factory.namespace(ns), subQuery);
			}
		}
		if (test instanceof AnyNodeTest) {
			if (!(context.isInitialisation() && context
					.isStepsEqualsStringLitteral()))
				return subQuery;
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

	private void newSteps() {
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