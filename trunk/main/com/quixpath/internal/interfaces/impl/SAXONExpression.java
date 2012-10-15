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

import innovimax.quixproc.datamodel.ConvertException;
import innovimax.quixproc.datamodel.IStream;
import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import innovimax.quixproc.datamodel.QuixException;
import innovimax.quixproc.datamodel.shared.SmartAppendQueue;

import java.util.LinkedList;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
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
/* package */final class SAXONExpression extends AbstractSAXONExpression {

	@Override
	public IStream<MatchEvent> update(QuixEvent event) throws QuiXPathException {
		fireWrite(event);
		if (buffer != null) {
			buffer.add(event);
		}
		if (event.isStartDocument()) {
			buffer = new LinkedList<QuixEvent>();
		}
		if (event.isEndDocument()) {
			try {
				final XPathSelector evaluator = xPathExecutable.load();
				final XdmNode node = getContextItem();
				evaluator.setContextItem(node);
				final XdmValue values = evaluator.evaluate();
				final XdmSequenceIterator it = values.iterator();
				final SmartAppendQueue<MatchEvent> doc = new SmartAppendQueue<MatchEvent>();
				doc.setReaderCount(1);
				final IStream<MatchEvent> stream = doc.registerReader();
				final MyEventConverter eventConverter = new MyEventConverter(
						doc, node, it);
				buffer = null;
				eventConverter.run();

				return asSequence(stream);
			} catch (SaxonApiException e) {
				throw new QuiXPathException(e);
			} catch (ConvertException e) {
				throw new QuiXPathException(e);
			}

		}

		return emptyStream();
	}

	private IStream<MatchEvent> asSequence(
			final IStream<MatchEvent> withoutSequence) {
		return new IStream<MatchEvent>() {

			// true iff startSequence has been send on the stream
			private boolean startSequence = false;
			// true iff endSequence has been send on the stream
			private boolean endSequence = false;
			// true iff sendEndSequence is the next event
			private boolean sendEndSequence = false;

			@Override
			public MatchEvent next() {
				if (!startSequence) {
					startSequence = true;
					return new MatchEvent(QuixEvent.getStartSequence(), false);
				}

				try {
					if (withoutSequence.hasNext()) {
						final MatchEvent event = withoutSequence.next();
						if (event.getEvent().isEndDocument()) {
							sendEndSequence = true;
						}
						return event;
					}
				} catch (QuixException e) {
					new Error(e); // wrap the exception in an error.
				}
				if (sendEndSequence) {
					endSequence = true;
					return new MatchEvent(QuixEvent.getEndSequence(), false);
				}
				return null;
			}

			@Override
			public boolean hasNext() {
				return !endSequence;
			}

			@Override
			public void close() {
				// empty
			}
		};
	}

	private IStream<MatchEvent> emptyStream() {
		return new IStream<MatchEvent>() {

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public MatchEvent next() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void close() {
				// TODO Auto-generated method stub

			}

		};
	}

	/* package */SAXONExpression(XPathExecutable xPathExecutable) {
		super(xPathExecutable);
	}

	@Override
	public boolean isEmpty() {
		if (buffer == null) {
			return true; // TODO ???
		}
		return buffer.isEmpty();
	}

	@Override
	public String toString() {
		return "SAXONExpression [xPathExpression="
				+ xPathExecutable.getUnderlyingExpression()
						.getInternalExpression() + "]";
	}

}
