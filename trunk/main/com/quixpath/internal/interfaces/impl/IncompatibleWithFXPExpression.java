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
import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import innovimax.quixproc.datamodel.Stream;
import innovimax.quixproc.datamodel.shared.SmartAppendQueue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;

import com.quixpath.exceptions.QuiXPathException;
import com.quixpath.internal.events.IQuixPathEvent;
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
public class IncompatibleWithFXPExpression extends AbstractQuiXPathExpression {

	private final XPathExecutable xPathExecutable;
	private List<QuixEvent> buffer;

	@Override
	public Stream<MatchEvent> update(QuixEvent event) throws QuiXPathException {
		fireWrite(null);
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
				final Stream<MatchEvent> stream = doc.registerReader();
				final MyEventConverter eventConverter = new MyEventConverter(
						doc, node, it);
				eventConverter.run();
				buffer = null;
				return asSequence(stream);
			} catch (SaxonApiException e) {
				throw new QuiXPathException(e);
			} catch (ConvertException e) {
				throw new QuiXPathException(e);
			}

		}

		return emptyStream();
	}

	private Stream<MatchEvent> asSequence(
			final Stream<MatchEvent> withoutSequence) {
		return new Stream<MatchEvent>() {

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

				if (withoutSequence.hasNext()) {
					final MatchEvent event = withoutSequence.next();
					if (event.getEvent().isEndDocument()) {
						sendEndSequence = true;
					}
					return event;
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

	private XdmNode getContextItem() throws ConvertException {
		final DocumentBuilder db = processor().newDocumentBuilder();
		final Stream<QuixEvent> reader = bufferReader();
		final DOMConverter converter = new DOMConverter(db, reader);
		return converter.exec();
	}

	private Stream<QuixEvent> bufferReader() {
		return new Stream<QuixEvent>() {
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

	private Stream<MatchEvent> emptyStream() {
		return new Stream<MatchEvent>() {

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

	@Override
	public boolean isStreamingEvaluation() {
		return false;
	}

	public IncompatibleWithFXPExpression(XPathExecutable xPathExecutable) {
		super();
		this.xPathExecutable = xPathExecutable;
	}

	// TODO Share the code about listeners.
	private List<IBufferListener> listeners;

	@Override
	public void addBufferListener(final IBufferListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<IBufferListener>();
		}
		listeners.add(listener);
	}

	private void fireRead(IQuixPathEvent event) {
		if (listeners != null) {
			for (IBufferListener listener : listeners) {
				listener.read(event);
			}
		}
	}

	private void fireWrite(IQuixPathEvent event) {
		if (listeners != null) {
			for (IBufferListener listener : listeners) {
				listener.write(event);
			}
		}
	}

}
