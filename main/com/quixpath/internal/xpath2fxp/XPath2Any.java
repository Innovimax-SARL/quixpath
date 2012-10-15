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

import net.sf.saxon.om.NamePool;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;

import com.quixpath.interfaces.context.IStaticContext;
import com.quixpath.internal.fxpplus.FXPPlusFactory;
import com.quixpath.internal.fxpplus.IFXPPlusFactory;
import com.quixpath.internal.interfaces.impl.AbstractQuiXPathExpression;
import com.quixpath.internal.interfaces.impl.count.IOperator;

public class XPath2Any {
	// SAXON representation of the query
	// TODO find a description of this representation.
	protected final XPathExecutable xPathExecutable;
	protected/* final */NamePool namePool;

	protected final IFXPPlusFactory factory;
	protected final String xPathQuery;

	/**
	 * 
	 * 
	 * @param query
	 *            String version of the XPath query.
	 * @throws XPathExpressionException
	 *             when SAXON can not parse the query.
	 */
	public XPath2Any(final String query, final IStaticContext staticContext)
			throws XPathExpressionException {
		final Processor processor = AbstractQuiXPathExpression.processor();

		final XPathCompiler compiler = processor.newXPathCompiler();
		try {
			if (staticContext != null) {
				for (String prefix : staticContext.getPrefixes()) {
					compiler.declareNamespace(prefix,
							staticContext.getNamespaceURI(prefix));
				}
			}
			xPathExecutable = compiler.compile(query);
		} catch (SaxonApiException e) {
			throw new XPathExpressionException(e);
		}
		factory = FXPPlusFactory.newInstance();
		xPathQuery = query;

	}

	protected boolean countAtTopLevel = false;

	public boolean isCountintAtTopLevel() {
		assert !countAtTopLevel || !isCounting();
		return countAtTopLevel;
	}

	// value = null <-> the query does not handle a count operator.
	protected IOperator operator = null;

	public IOperator getOperator() {
		return operator;
	}

	public boolean isCounting() {
		assert operator == null || !countAtTopLevel;
		return operator != null;
	}

	/**
	 * 
	 * Return null if Saxon fails to parse the query.
	 * 
	 * @return
	 */
	public XPathExecutable getXPathExecutable() {
		return xPathExecutable;
	}

}
