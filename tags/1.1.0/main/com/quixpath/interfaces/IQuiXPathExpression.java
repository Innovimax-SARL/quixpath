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

/**
 * IQuiXPathExpression provides access to compiled QuiXPath expressions.
 * 
 */
public interface IQuiXPathExpression {

	/**
	 * Evaluate the compiled QuiXPath expression.
	 * 
	 * @param document
	 *            The InputSource of the document to evaluate over.
	 * @param os
	 *            The result of evaluating the expression.
	 * @return The largest stream in which the matching of all elements is
	 *         known.
	 * @throws QuiXPathException
	 *             If the query can not be evaluated
	 */
	public IStream<MatchEvent> update(QuixEvent event) throws QuiXPathException;

	public QuixValue evaluate(IStream<QuixEvent> stream)
			throws QuiXPathException;

}
