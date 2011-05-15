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
package com.quixpath.internal.interfaces.impl;

import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import innovimax.quixproc.datamodel.IStream;
import net.sf.saxon.s9api.Processor;

import com.quixpath.exceptions.QuiXPathException;
import com.quixpath.interfaces.IQuiXPathExpression;

/**
 * AbstractQuiXPathExpression contains tools to compile and evaluate
 * QuiXPathExpression.
 * 
 */
public abstract class AbstractQuiXPathExpression implements IQuiXPathExpression {

	@Override
	public abstract IStream<MatchEvent> update(QuixEvent event)
			throws QuiXPathException;

	// As far as possible, an application should instantiate a single Processor.
	private static Processor processor;

	/**
	 * 
	 * @return the unique instance of the SAXON processor.
	 */
	public static Processor processor() {
		if (processor == null) {
			processor = new Processor(false);
		}
		return processor;
	}

}
