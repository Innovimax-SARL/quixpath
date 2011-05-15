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

import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.PathExpression;
import net.sf.saxon.expr.RootExpression;
import net.sf.saxon.expr.Token;
import net.sf.saxon.expr.sort.DocumentSorter;
import net.sf.saxon.functions.BooleanFn;
import net.sf.saxon.functions.Exists;
import net.sf.saxon.om.Axis;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.s9api.XPathExecutable;

import com.quixpath.exceptions.UnsupportedQueryException;

import fr.inria.mostrare.xpath.exception.DSTABuildingException;
import fr.inria.mostrare.xpath.exception.IllegalSignatureException;
import fr.inria.mostrare.xpath.exception.IllegalVariableDeclarationException;
import fr.inria.mostrare.xpath.pub.FXPFactory;
import fr.inria.mostrare.xpath.pub.FXPType;
import fr.inria.mostrare.xpath.pub.IFXPLocalTerm;
import fr.inria.mostrare.xpath.pub.IFXPTerm;

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

	private IFXPContext context;

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
	 * @return FXP query equivalent to the XPath query.
	 * @throws UnsupportedQueryException
	 *             when the query can not be expressed with FXP.
	 */
	public IFXPTerm toFXP() throws UnsupportedQueryException {
		if (term == null) {
			context = new FXPContext();
			final Expression internalExp = xPathExecutable
					.getUnderlyingExpression().getInternalExpression();
			namePool = xPathExecutable.getUnderlyingStaticContext()
					.getNamePool();
			final IFXPTerm fxp = toFXP(internalExp,
					FXPFactory.variable(SELECTING_VARIABLE_NAME));
			term = rootQuery(fxp);
		}
		return term;
	}

	// An FXPquery is always evaluated from the root of the document.
	private IFXPTerm rootQuery(final IFXPTerm term)
			throws UnsupportedQueryException {
		if (context.isRooted()) {
			return term;
		} else {
			if (term instanceof IFXPLocalTerm) {
				return FXPFactory.childStar((IFXPLocalTerm) term);
			}
			throw new UnsupportedQueryException();
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
	 * @throws IllegalSignatureException
	 * @throws IllegalVariableDeclarationException
	 * @throws DSTABuildingException
	 */
	// According to the type of the xpathExp, call the correct rule to transform
	// the query.
	private IFXPTerm toFXP(final Expression xpathExp, final IFXPTerm subQuery)
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
			throw new UnsupportedQueryException();
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
		throw new UnsupportedQueryException();
	}

	private IFXPTerm existsExpressionToFXP(final Exists existsExpression,
			final IFXPTerm subQuery) throws UnsupportedQueryException {
		final Expression[] arguments = existsExpression.getArguments();
		argumentsLength(arguments, 1);
		return toFXP(arguments[0], subQuery);

	}

	private void argumentsLength(final Expression[] arguments, final int length)
			throws UnsupportedQueryException {
		// assert arguments.length == length;
		if (arguments.length != length) {
			throw new UnsupportedQueryException();
		}
	}

	private IFXPTerm booleanExpressionToFXP(final BooleanExpression booleanExp,
			final IFXPTerm subQuery) throws UnsupportedQueryException {
		if (booleanExp.getOperator() == Token.AND) {
			final Expression[] arguments = booleanExp.getOperands();
			argumentsLength(arguments, 2);
			final IFXPTerm left = toFXP(arguments[0], subQuery); // TODO
																	// subQuery
																	// is
			// always true?
			final IFXPTerm right = toFXP(arguments[1], FXPFactory.trueQuery());
			argumentsLength(arguments, 2);
			return and(left, right);
		}
		throw new UnsupportedQueryException();
	}

	private IFXPTerm booleanFnToFXP(final BooleanFn booleanFunction,
			final IFXPTerm subQuery) throws UnsupportedQueryException {
		// TODO How to get a function. By its name?
		if (booleanFunction.getDisplayName().equals("not")) {
			final Iterator<Expression> arguments = booleanFunction
					.iterateSubExpressions();
			final IFXPTerm argument = toFXP(arguments.next(), subQuery);
			assert !arguments.hasNext();
			if (argument instanceof IFXPLocalTerm) {
				return FXPFactory.localNot((IFXPLocalTerm) argument);
			} else {
				return FXPFactory.not(argument);
			}
		}

		throw new UnsupportedQueryException();
	}

	private IFXPTerm pathExpressionToFXP(final PathExpression pathExp,
			final IFXPTerm subQuery) throws UnsupportedQueryException {
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

		throw new UnsupportedQueryException();
	}

	private IFXPTerm filterExpressionToFXP(final FilterExpression filterExp,
			final IFXPTerm subQuery) throws UnsupportedQueryException {
		context = context.enterFilter();
		final Expression filter = filterExp.getFilter();
		final Expression controllingExpression = filterExp
				.getControllingExpression();

		final IFXPTerm left = subQuery;
		final IFXPTerm right = toFXP(filter, FXPFactory.trueQuery());
		final IFXPTerm and = and(left, right);
		context = context.exitFilter();
		return toFXP(controllingExpression, and);
	}

	private IFXPTerm and(final IFXPTerm left, final IFXPTerm right)
			throws UnsupportedQueryException {
		final IFXPTerm and;
		if ((right instanceof IFXPLocalTerm) && (left instanceof IFXPLocalTerm)) {
			and = FXPFactory.localAnd((IFXPLocalTerm) left,
					(IFXPLocalTerm) right);
		} else {
			and = FXPFactory.and(left, right);
		}
		return and;
	}

	private IFXPTerm axisExpressionToFXP(final AxisExpression axisExpression,
			final IFXPTerm subQuery) throws UnsupportedQueryException {
		final NodeTest test = axisExpression.getNodeTest();
		if (axisExpression.getAxis() == Axis.CHILD) {
			return FXPFactory.child(nodeTestToFXP(test, subQuery));
		}
		if (axisExpression.getAxis() == Axis.DESCENDANT_OR_SELF) {
			final IFXPTerm local = nodeTestToFXP(test, subQuery);
			if (local instanceof IFXPLocalTerm) {
				IFXPLocalTerm localOp = (IFXPLocalTerm) local;
				return FXPFactory.childStar(localOp);
			}
		}
		if (axisExpression.getAxis() == Axis.DESCENDANT) {
			final IFXPTerm local = nodeTestToFXP(test, subQuery);
			if (local instanceof IFXPLocalTerm) {
				final IFXPLocalTerm localOp = (IFXPLocalTerm) local;
				return FXPFactory.child(FXPFactory.childStar(localOp));
			}
		}
		if (axisExpression.getAxis() == Axis.ATTRIBUTE) {
			return nodeTestToAttribute(test, subQuery);
		}
		if (axisExpression.getAxis() == Axis.SELF) {
			return nodeTestToFXP(test, subQuery);
		}
		throw new UnsupportedQueryException();
	}

	private IFXPTerm nodeTestToAttribute(final NodeTest test,
			final IFXPTerm subQuery) throws UnsupportedQueryException {
		if (test instanceof NameTest) {
			// label
			final NameTest nameTest = (NameTest) test;
			final String label = namePool.getLocalName(nameTest
					.getFingerprint());
			if (namePool.getNamespaceCode(nameTest.getFingerprint()) != 0) {
				throw new UnsupportedQueryException();
			}
			switch (subQuery.getXPathEnum()) {
			case VARIABLE:
				return FXPFactory.attributeLabelX(label,
						SELECTING_VARIABLE_NAME);
			case TRUE:
				return FXPFactory.attributeLabel(label);
			default:
				throw new UnsupportedQueryException();
			}

		} else if (test instanceof NodeKindTest) {
			// *
			final NodeKindTest kindTest = (NodeKindTest) test;
			if (kindTest.equals(NodeKindTest.ATTRIBUTE)) {
				switch (subQuery.getXPathEnum()) {
				case VARIABLE:
					return FXPFactory.attributeStarX(SELECTING_VARIABLE_NAME);
				case TRUE:
					return FXPFactory.attributeStar();
				default:
					throw new UnsupportedQueryException();
				}
			}
		}
		throw new UnsupportedQueryException();
	}

	private IFXPTerm nodeTestToFXP(final NodeTest test, final IFXPTerm subQuery)
			throws UnsupportedQueryException {
		if (test instanceof NameTest) {
			// label
			final NameTest nameTest = (NameTest) test;
			final String label = namePool.getLocalName(nameTest
					.getFingerprint());
			if (namePool.getNamespaceCode(nameTest.getFingerprint()) != 0) {
				throw new UnsupportedQueryException();
			}
			if (subQuery instanceof IFXPLocalTerm) {
				final IFXPLocalTerm local = (IFXPLocalTerm) subQuery;
				final IFXPLocalTerm labelTerm = FXPFactory.localLabel(label);
				if (local.getXPathEnum().equals(FXPType.TRUE)) {
					return labelTerm;
				}
				return FXPFactory.localAnd(labelTerm, local);
			} else {
				return FXPFactory.label(label, subQuery);
			}
		} else if (test instanceof NodeKindTest) {
			// *
			final NodeKindTest kindTest = (NodeKindTest) test;
			if (kindTest.equals(NodeKindTest.ELEMENT)) {
				return subQuery;
			}
		}
		throw new UnsupportedQueryException();
	}

}
