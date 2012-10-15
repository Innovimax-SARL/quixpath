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

import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.expr.Expression;

import com.quixpath.exceptions.UnsupportedQueryException;
import com.quixpath.interfaces.context.IStaticContext;
import com.quixpath.internal.fxpplus.ITerm;

import fr.inria.lille.fxp.datamodel.api.IPools;
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
public class XPath2FXP extends XPath2FXPPlus {

	/**
	 * 
	 * 
	 * @param query
	 *            String version of the XPath query.
	 * @throws XPathExpressionException
	 *             when SAXON can not parse the query.
	 */
	public XPath2FXP(final String query, final IPools pools)
			throws XPathExpressionException {
		this(query, pools, null);
	}

	public XPath2FXP(final String query, final IPools pools,
			final IStaticContext staticContext) throws XPathExpressionException {
		super(query, pools, staticContext);
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
			term = rootQuery(fxp).expand(fxpTermFactory);
		}
		return term;
	}

}