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
package com.quixpath.internal.interfaces;

import com.quixpath.interfaces.IQuiXPathExpression;
import com.quixpath.internal.mvc.listeners.IBufferListener;

public interface IInternalQuiXPathExpression extends IQuiXPathExpression {

	/**
	 * Return true if the query is evaluated in a streaming mode.
	 * 
	 * Ih the return value is false, then the evaluation will build the DOM of
	 * the document.
	 * 
	 * @return true iff the query is evaluated with a streaming algorithm.
	 */
	public boolean isStreamingEvaluation();

	public void addBufferListener(IBufferListener listener);

	/**
	 * For test propose.
	 * 
	 * Return true iff all the intermediate data structure (like buffer) are
	 * empty.
	 * 
	 * @return
	 */
	public boolean isEmpty();

}
