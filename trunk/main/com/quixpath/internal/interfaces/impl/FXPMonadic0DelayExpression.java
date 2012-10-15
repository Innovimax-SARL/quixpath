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

import innovimax.quixproc.datamodel.IStream;
import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import innovimax.quixproc.datamodel.QuixEvent.Attribute;
import innovimax.quixproc.datamodel.QuixEvent.EndDocument;
import innovimax.quixproc.datamodel.QuixEvent.EndElement;
import innovimax.quixproc.datamodel.QuixEvent.PI;
import innovimax.quixproc.datamodel.QuixEvent.StartDocument;
import innovimax.quixproc.datamodel.QuixEvent.StartElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.quixpath.exceptions.QuiXPathException;
import com.quixpath.internal.interfaces.IFXPCompatible;
import com.quixpath.internal.mvc.listeners.IBufferListener;

import fr.inria.lille.fxp.queryengine.api.IEnumerationHandler;
import fr.inria.lille.fxp.queryengine.api.IUpdates;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/**
 * Compile representation of a query that is compatible with FXP.
 * 
 * The evaluation algorithm is streaming: only a part of the document is
 * buffered.
 * 
 * Today, the query engine is the FXP query engine.
 * 
 */
/* package */class FXPMonadic0DelayExpression extends
		AbstractQuiXPathExpression implements IFXPCompatible {

	/* package */FXPMonadic0DelayExpression(final IFXPTerm fxpTerm,
			final IEnumerationHandler handler) {
		// TODO Duplicate code.
		super(fxpTerm.getStaticProperties().getProjectionProperties());
		this.fxpTerm = fxpTerm;
		this.handler = handler;
	}

	private final IFXPTerm fxpTerm;
	private final IEnumerationHandler handler;
	private Stack<Boolean> matches;

	public IFXPTerm getFXPTerm() {
		return fxpTerm;
	}

	@Override
	public String toString() {
		return "[FXPMonadic0DelayExpression] " + fxpTerm.toString();
	}

	private void init() {
		matches = new Stack<Boolean>();
		handler.startStream();
	}

	private int depth;

	@Override
	public final IStream<MatchEvent> update(final QuixEvent quixEvent)
			throws QuiXPathException {
		final boolean matched;
		fireWrite(quixEvent);
		if (quixEvent.isStartSequence()) {
			init();
		}

		switch (quixEvent.getType()) {
		case START_SEQUENCE:
		case END_SEQUENCE:
			matched = false;
			depth = 0;
			break;
		case TEXT: {
			if (keepText && depth <= depthBound) {
				final IUpdates updates = handler.updateText(0);
				matched = matched(updates);
			} else {
				matched = false;
			}
		}
			break;
		case START_ELEMENT: {
			depth++;
			if (depth <= depthBound) {
				final StartElement elsel = quixEvent.asStartElement();
				final IUpdates updates = handler.updateOpenElement(0,
						elsel.getLocalName(), elsel.getURI());
				matched = matched(updates);
				matches.push(matched);
			} else {
				matched = false;
			}
		}
			break;
		case END_ELEMENT: {
			if (depth <= depthBound) {
				final EndElement eleel = quixEvent.asEndElement();
				handler.updateCloseElement(0, eleel.getLocalName(),
						eleel.getURI());
				matched = matches.pop();
			} else {
				matched = false;
			}

			depth--;
		}
			break;
		case ATTRIBUTE: {// attribute
			if (keepAttribute && depth <= depthBound) {
				final Attribute at = quixEvent.asAttribute();
				final IUpdates updates = handler.updateAttribute(0,
						at.getLocalName(), at.getURI());
				matched = matched(updates);
			} else {
				matched = false;
			}
		}
			break;
		case START_DOCUMENT: {
			final StartDocument startDocument = quixEvent.asStartDocument();
			final IUpdates updates = handler.updateOpenElement(0, FAKE_ROOT,
					startDocument.getURI());
			matched = matched(updates);
			matches.push(matched);
		}
			break;
		case END_DOCUMENT: {
			final EndDocument endDocument = quixEvent.asEndDocument();
			handler.updateCloseElement(0, FAKE_ROOT, endDocument.getURI());
			matched = matches.pop();
		}
			break;
		case PI: {
			if (keepPI && depth <= depthBound) {
				final PI pi = quixEvent.asPI();
				final IUpdates updates = handler.updatePI(0, pi.getTarget());
				matched = matched(updates);
			} else {
				matched = false;
			}
		}
			break;
		case COMMENT: {
			if (keepComment && depth <= depthBound) {
				final IUpdates updates = handler.updateComment(0);
				matched = matched(updates);
			} else {
				matched = false;
			}
		}
			break;
		default:
			return new EmptyStream(); // ???
		}
		fireRead(quixEvent);
		return new SingletonStream(new MatchEvent(quixEvent, matched));
	}

	private static String FAKE_ROOT = "";

	private boolean matched(IUpdates updates) {
		return !updates.selectAssignments().isEmpty();
	}

	public static class SingletonStream implements IStream<MatchEvent> {

		private final MatchEvent event;
		private boolean alreadyRead;

		public SingletonStream(MatchEvent event) {
			this.event = event;
			this.alreadyRead = false;
		}

		@Override
		public boolean hasNext() {
			return !this.alreadyRead;
		}

		@Override
		public MatchEvent next() {
			this.alreadyRead = true;
			return this.event;
		}

		@Override
		public void close() {
			//
		}

	}

	public static class EmptyStream implements IStream<MatchEvent> {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public MatchEvent next() {
			return null;
		}

		@Override
		public void close() {
			//
		}

	}

	@Override
	public boolean isStreamingEvaluation() {
		return true;
	}

	private List<IBufferListener> listeners;

	private void fireRead(QuixEvent event) {
		if (listeners != null) {
			for (IBufferListener listener : listeners) {
				listener.read(event);
			}
		}
	}

	private void fireWrite(QuixEvent event) {
		if (listeners != null) {
			for (IBufferListener listener : listeners) {
				listener.write(event);
			}
		}
	}

	@Override
	public void addBufferListener(IBufferListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<IBufferListener>();
		}
		listeners.add(listener);
	}

	@Override
	public boolean isEmpty() {
		return matches.isEmpty();
	}

}
