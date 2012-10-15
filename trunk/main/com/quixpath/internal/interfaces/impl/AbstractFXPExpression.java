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
import innovimax.quixproc.datamodel.QuixEvent.Comment;
import innovimax.quixproc.datamodel.QuixEvent.EndDocument;
import innovimax.quixproc.datamodel.QuixEvent.EndElement;
import innovimax.quixproc.datamodel.QuixEvent.PI;
import innovimax.quixproc.datamodel.QuixEvent.StartDocument;
import innovimax.quixproc.datamodel.QuixEvent.StartElement;
import innovimax.quixproc.datamodel.QuixEvent.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.quixpath.exceptions.QuiXPathException;
import com.quixpath.internal.interfaces.IFXPCompatible;
import com.quixpath.internal.mvc.listeners.IBufferListener;
import com.quixpath.internal.queryevaluation.Buffer;

import fr.inria.lille.fxp.queryengine.api.IEnumerationHandler;
import fr.inria.lille.fxp.queryengine.api.IUpdates;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

public abstract class AbstractFXPExpression extends AbstractQuiXPathExpression
		implements IFXPCompatible {

	protected final IFXPTerm fxpTerm;
	protected final IEnumerationHandler handler;
	protected Buffer buffer;

	public AbstractFXPExpression(IFXPTerm fxpTerm, IEnumerationHandler handler) {
		super(fxpTerm.getStaticProperties().getProjectionProperties());
		this.fxpTerm = fxpTerm;
		this.handler = handler;
		this.listeners = new ArrayList<IBufferListener>();
	}

	protected int currentId;
	private int nextId;
	protected Stack<Integer> ids;

	final protected List<IBufferListener> listeners;

	public final IFXPTerm getFXPTerm() {
		return fxpTerm;
	}

	@Override
	public final void addBufferListener(IBufferListener listener) {
		listeners.add(listener);
		if (buffer != null) {
			buffer.addListerner(listener);
		}
	}

	// true iff FXP does not need new event to decide that all the future node
	// will be rejected
	protected boolean allReject = false;
	// true iff FXP will not create new candidate.
	protected boolean fxpLess = false;

	// TODO not clean...
	protected boolean isClosed = false;

	protected abstract void postprocess(final IUpdates updates);

	protected abstract void preprocess();

	private int depth;

	@Override
	public final IStream<MatchEvent> update(final QuixEvent quixEvent)
			throws QuiXPathException {
		if (quixEvent.isStartSequence()) {
			init();
		}

		switch (quixEvent.getType()) {
		case PI:
			nextId++;
			currentId = nextId;
			if (depth <= depthBound && keepPI) {
				buffer.write(quixEvent, currentId, allReject);
				final PI piel = quixEvent.asPI();
				{ // text event
					if (!fxpLess) {
						final IUpdates updates = handler.updatePI(currentId,
								piel.getTarget());
						postprocess(updates);
					}
				}
			} else {
				buffer.write(quixEvent, currentId, true);
			}
			break;
		case TEXT:
			nextId++;
			currentId = nextId;
			if (depth <= depthBound && keepText) {
				// preprocess();
				buffer.write(quixEvent, currentId, allReject);
				final Text el = quixEvent.asText();
				{ // text event
					if (!fxpLess) {
						final IUpdates updates = handler.updateText(currentId);
						postprocess(updates);
					}
				}
				{ // value
					if (!fxpLess) {
						final IUpdates updates = handler.updateData(el
								.getData());
						postprocess(updates);
					}
				}
			} else {
				buffer.write(quixEvent, currentId, true);
			}
			break;
		case COMMENT:
			nextId++;
			currentId = nextId;
			if (depth <= depthBound && keepComment) {
				// preprocess();
				buffer.write(quixEvent, currentId, allReject);
				final Comment comel = quixEvent.asComment();
				{ // text event
					if (!fxpLess) {
						final IUpdates updates = handler
								.updateComment(currentId);
						postprocess(updates);
					}
				}
				{ // value
					if (!fxpLess) {
						final IUpdates updates = handler.updateData(comel
								.getData());
						postprocess(updates);
					}
				}
			} else {
				buffer.write(quixEvent, currentId, true);
			}
			break;
		case START_ELEMENT:
			nextId++;
			currentId = nextId;
			depth++;
			ids.push(currentId);
			if (depth <= depthBound) {
				preprocess();
				final StartElement elsel = quixEvent.asStartElement();
				buffer.write(quixEvent, currentId, allReject);
				if (!fxpLess) {
					final IUpdates updates = handler.updateOpenElement(
							currentId, elsel.getLocalName(), elsel.getURI());
					postprocess(updates);
				}
			} else {
				buffer.write(quixEvent, currentId, true);
			}
			break;
		case END_ELEMENT:
			currentId = ids.pop();
			if (depth <= depthBound) {
				isClosed = true;
				final EndElement eleel = quixEvent.asEndElement();
				preprocess();
				buffer.write(quixEvent, currentId, allReject);
				if (!fxpLess) {
					final IUpdates updates = handler.updateCloseElement(
							currentId, eleel.getLocalName(), eleel.getURI());
					postprocess(updates);
				}
				isClosed = false;
			} else {
				buffer.write(quixEvent, currentId, true);
			}
			depth--;
			break;
		case ATTRIBUTE:
			nextId++;
			currentId = nextId;
			if (depth <= depthBound && keepAttribute) {
				preprocess();
				final Attribute at = quixEvent.asAttribute();
				{// attribute
					buffer.write(quixEvent, currentId, allReject);
					if (!fxpLess) {
						final IUpdates updates = handler.updateAttribute(
								currentId, at.getLocalName(), at.getURI());
						postprocess(updates);
					}
				}
				{ // value
					if (!fxpLess) {
						final IUpdates updates = handler.updateData(at
								.getValue());
						postprocess(updates);
					}
				}
			} else {
				buffer.write(quixEvent, currentId, true);
			}
			break;
		case START_DOCUMENT:
			ids.push(currentId);
			// preprocess();
			buffer.write(quixEvent, currentId, allReject);
			if (!fxpLess) {
				final StartDocument startDocument = quixEvent.asStartDocument();
				final IUpdates updates = handler.updateOpenElement(currentId,
						FAKE_ROOT, startDocument.getURI());
				postprocess(updates);
			}
			break;
		case END_DOCUMENT:
			isClosed = true;
			currentId = ids.pop();
			// preprocess()
			buffer.write(quixEvent, currentId, allReject);
			if (!fxpLess) {
				final EndDocument endDocument = quixEvent.asEndDocument();
				final IUpdates updates = handler.updateCloseElement(currentId,
						FAKE_ROOT, endDocument.getURI());
				postprocess(updates);
			}
			isClosed = false;
			break;
		default:
			notFXPCompatible(quixEvent);
		}

		return buffer.read();
	}

	private void notFXPCompatible(final QuixEvent quixEvent) {
		buffer.write(quixEvent, currentId, true);
	}

	/* package */static final String FAKE_ROOT = "";

	protected void init() throws QuiXPathException {
		handler.startStream(); // TODO where the best place to do this call
		buffer = new Buffer();
		for (IBufferListener listener : listeners) {
			buffer.addListerner(listener);
		}
		// allReject = false;
		// fxpLess = false;
		currentId = 0;
		nextId = 0;
		ids = new Stack<Integer>();
		// counts = new HashMap<Integer, Integer>();
	}

}
