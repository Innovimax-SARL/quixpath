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

import innovimax.quixproc.datamodel.ConvertException;
import innovimax.quixproc.datamodel.DOMConverter;
import innovimax.quixproc.datamodel.IStream;
import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XdmNode;

import com.quixpath.exceptions.QuiXPathException;
import com.quixpath.internal.mvc.listeners.IBufferListener;

/**
 * Compile representation of a query that is not compatible with FXP.
 * 
 * The evaluation algorithm is the following: buffer the whole document is a
 * tiny tree (DOM) and then evaluate the query on the tree.
 * 
 * Today, the query engine is SAXON.
 * 
 */
public abstract class AbstractSAXONExpression extends
		AbstractQuiXPathExpression {

	protected final XPathExecutable xPathExecutable;

	public AbstractSAXONExpression(XPathExecutable xPathExecutable) {
		super();
		this.xPathExecutable = xPathExecutable;
	}

	public static AbstractSAXONExpression make(
			final XPathExecutable xPathExecutable) {
		final ItemType itemType = xPathExecutable.getResultItemType();
		if (itemType.getUnderlyingItemType().equals(
				ItemType.INTEGER.getUnderlyingItemType())) {
			return new SAXONCountExpression(xPathExecutable);
		}
		return new SAXONExpression(xPathExecutable);
	}

	protected List<QuixEvent> buffer;

	protected final XdmNode getContextItem() throws ConvertException {
		final DocumentBuilder db = processor().newDocumentBuilder();
		final IStream<QuixEvent> reader = bufferReader();
		final DOMConverter converter = new DOMConverter(db, reader);
		return converter.exec();
	}

	private IStream<QuixEvent> bufferReader() {
		return new IStream<QuixEvent>() {
			Iterator<QuixEvent> it = buffer.iterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public QuixEvent next() {
				fireRead(null);
				return it.next();
			}

			@Override
			public void close() {
				// empty body
			}

		};
	}

	@Override
	public boolean isStreamingEvaluation() {
		return false;
	}

	@Override
	public abstract boolean isEmpty();

	@Override
	public abstract IStream<MatchEvent> update(QuixEvent event)
			throws QuiXPathException;

	// TODO Share the code about listeners.
	private List<IBufferListener> listeners;

	@Override
	public final void addBufferListener(final IBufferListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<IBufferListener>();
		}
		listeners.add(listener);
	}

	protected final void fireRead(QuixEvent event) {
		if (listeners != null) {
			for (IBufferListener listener : listeners) {
				listener.read(event);
			}
		}
	}

	protected final void fireWrite(QuixEvent event) {
		if (listeners != null) {
			for (IBufferListener listener : listeners) {
				listener.write(event);
			}
		}
	}

}
