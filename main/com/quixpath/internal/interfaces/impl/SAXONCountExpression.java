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
package com.quixpath.internal.interfaces.impl;

import innovimax.quixproc.datamodel.IStream;
import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import innovimax.quixproc.datamodel.QuixException;
import innovimax.quixproc.datamodel.QuixValue;

import java.util.LinkedList;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import com.quixpath.exceptions.QuiXPathException;

/**
 * Compile representation of a query that is not compatible with FXP.
 * 
 * The evaluation algorithm is the following: buffer the whole document is a
 * tiny tree (DOM) and then evaluate the query on the tree.
 * 
 * Today, the query engine is SAXON.
 * 
 */
/* package */final class SAXONCountExpression extends AbstractSAXONExpression {

	/* package */SAXONCountExpression(XPathExecutable xPathExecutable) {
		super(xPathExecutable);
	}

	@Override
	public boolean isEmpty() {
		return buffer.isEmpty();
	}

	@Override
	public IStream<MatchEvent> update(QuixEvent event) throws QuiXPathException {
		throw new UnsupportedOperationException();
	}

	@Override
	public final QuixValue evaluate(IStream<QuixEvent> stream)
			throws QuiXPathException {
		try {
			while (stream.hasNext()) {
				final QuixEvent event = stream.next();
				fireWrite(event);
				if (buffer != null) {
					buffer.add(event);
				}
				if (event.isStartDocument()) {
					buffer = new LinkedList<QuixEvent>();
				}
			}
		} catch (QuixException e) {
			throw new QuiXPathException(e);
		}
		try {
			return evaluate();
		} catch (SaxonApiException e) {
			throw new QuiXPathException(e);
		}
	}

	private QuixValue evaluate() throws SaxonApiException {
		final XPathSelector evaluator = xPathExecutable.load();
		final XdmNode node = getContextItem();
		evaluator.setContextItem(node);
		final XdmValue value = evaluator.evaluate();
		assert value instanceof XdmAtomicValue;
		final XdmAtomicValue atomicValue = (XdmAtomicValue) value;
		buffer = null;
		return new QuixValue(atomicValue.getStringValue());
	}

}
