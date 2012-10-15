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
package com.quixpath.interfaces;

import innovimax.quixproc.datamodel.IStream;
import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import innovimax.quixproc.datamodel.QuixValue;

import com.quixpath.exceptions.QuiXPathException;
import com.quixpath.exceptions.UnsupportedQueryException;
import com.quixpath.interfaces.context.IStaticContext;

/**
 * IQuiXPath interface provides access to the QuiXPath evaluation environment
 * and expressions.
 * 
 */
public interface IQuiXPath {

	/**
	 * 
	 * Compile an XPath expression.
	 * 
	 * @param xpathQuery
	 *            The XPath expression.
	 * @param staticContext
	 *            The context. Set it to null if there is no context.
	 * @return Compiled XPath expression.
	 * @throws UnsupportedQueryException
	 *             If the query can not be compiled.
	 */
	public IQuiXPathExpression compile(String xpathQuery,
			IStaticContext staticContext, boolean canUseTree) throws UnsupportedQueryException;

	
	/**
	 * 
	 * compile(xpathQuery, staticContext, true)
	 * 
	 * @param xpathQuery
	 * @param staticContext
	 * @return
	 * @throws UnsupportedQueryException
	 */
	public IQuiXPathExpression compile(String xpathQuery,
			IStaticContext staticContext) throws UnsupportedQueryException;
	
	/**
	 * Evaluate a query on a stream of event. Event are send one by one via this
	 * method.
	 * 
	 * @param expression
	 *            the compiled version of a query.
	 * @param event
	 *            the current event is the stream.
	 * @return The largest stream in which the matching of all elements is
	 *         known.
	 * @throws QuiXPathException
	 *             If the query can not be evaluated.
	 */
	public IStream<MatchEvent> update(IQuiXPathExpression expression,
			QuixEvent event) throws QuiXPathException;

	public QuixValue evaluate(IQuiXPathExpression expression,
			IStream<QuixEvent> stream) throws QuiXPathException;

}
