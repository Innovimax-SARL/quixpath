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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.quixpath.exceptions.QuiXPathException;
import com.quixpath.internal.events.IEventConverter;
import com.quixpath.internal.events.IQuixPathEvent;
import com.quixpath.internal.events.QuiXPathEventFactory;
import com.quixpath.internal.mvc.listeners.IBufferListener;
import com.quixpath.internal.queryevaluation.Buffer;
import com.quixpath.internal.queryevaluation.IBuffer;
import com.quixpath.internal.xpath2fxp.XPath2FXP;

import fr.inria.lille.fxp.datamodel.api.IFXPEvent;
import fr.inria.lille.fxp.datamodel.api.IUpdates;
import fr.inria.lille.fxp.datamodel.internal.event.Action;
import fr.inria.lille.fxp.datamodel.internal.event.IElementEvent;
import fr.inria.lille.fxp.datamodel.internal.event.ISelectableEvent;
import fr.inria.lille.fxp.queryengine.internal.sta.EnumerationHandler;
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
/* package */class CompatibleWithFXPExpression extends
		AbstractQuiXPathExpression {

	private final IFXPTerm fxpTerm;
	private final EnumerationHandler handler;
	private IBuffer buffer;

	/* package */CompatibleWithFXPExpression(final IFXPTerm fxpTerm,
			final EnumerationHandler handler) {
		this.fxpTerm = fxpTerm;
		this.handler = handler;
		this.listeners = new ArrayList<IBufferListener>();
	}

	public IFXPTerm getFXPTerm() {
		return fxpTerm;
	}

	@Override
	public String toString() {
		return fxpTerm.toString();
	}

	// NodeId -> IQuixPathEvent where the type is OPEN
	private Map<Integer, IQuixPathEvent> openMap;
	// NodeId -> IQuixPathEvent where the type is CLOSE
	private Map<Integer, IQuixPathEvent> closeMap;
	// NodeId -> IQuixPathEvent where the event has OPEN/CLOSE tags (attribute,
	// text...)
	private Map<Integer, IQuixPathEvent> otherMap;

	// nodeId is in reject iff the node is selected and the corresponding close
	// event in not in buffer
	// It will come in the future.
	private Set<Integer> rejects;
	// nodeId is in reject iff the node is rejected and the corresponding close
	// event in not in buffer
	// It will come in the future.
	private Set<Integer> selects;
	private IEventConverter eventFactory;

	final List<IBufferListener> listeners;

	@Override
	public void addBufferListener(IBufferListener listener) {
		listeners.add(listener);
		if (buffer != null) {
			buffer.addListerner(listener);
		}
	}

	private void init() throws QuiXPathException {
		handler.startStream(); // TODO where the best place to do this call
		buffer = new Buffer();
		for (IBufferListener listener : listeners) {
			buffer.addListerner(listener);
		}
		openMap = new HashMap<Integer, IQuixPathEvent>();
		closeMap = new HashMap<Integer, IQuixPathEvent>();
		otherMap = new HashMap<Integer, IQuixPathEvent>();
		rejects = new HashSet<Integer>();
		selects = new HashSet<Integer>();
		eventFactory = QuiXPathEventFactory.newInstance();
		allReject = false;
	}

	@Override
	public IStream<MatchEvent> update(final QuixEvent event)
			throws QuiXPathException {
		if (event.isStartSequence()) {
			init();
		}
		// buffer the events
		final Iterable<IQuixPathEvent> quixPathEvents = eventFactory
				.convert(event);
		for (final IQuixPathEvent quixPathEvent : quixPathEvents) {
			buffer.write(quixPathEvent);

			final IFXPEvent fxpEvent = quixPathEvent.getFxpEvent();
			if (fxpEvent instanceof ISelectableEvent) {
				final ISelectableEvent selectableFxpEvent = (ISelectableEvent) fxpEvent;
				if (selectableFxpEvent != null) {
					if (rejects.remove(selectableFxpEvent.getNodeId())) {
						quixPathEvent.reject();
					} else if (selects.remove(selectableFxpEvent.getNodeId())) {
						quixPathEvent.select();
					} else if (allReject) {
						quixPathEvent.reject();
						return buffer.read();
					}
				}
			}
			update(quixPathEvent);
		}

		return buffer.read();
	}

	private boolean reject(Map<Integer, IQuixPathEvent> map, Integer nodeId) {
		final IQuixPathEvent quixPathEvent = map.remove(nodeId);
		if (quixPathEvent != null) {
			quixPathEvent.reject();
			return true;
		}
		return false;
	}

	private boolean select(Map<Integer, IQuixPathEvent> map, Integer nodeId) {
		final IQuixPathEvent quixPathEvent = map.remove(nodeId);
		if (quixPathEvent != null) {
			quixPathEvent.select();
			return true;
		}
		return false;
	}

	// All node that will come in the future will be rejected.
	private boolean allReject = false;

	private void update(final IQuixPathEvent event) {
		IFXPEvent fxpEvent = event.getFxpEvent();
		if (fxpEvent != null) {
			if (fxpEvent instanceof IElementEvent) {
				final IElementEvent fxpElementEvent = (IElementEvent) fxpEvent;
				if (fxpElementEvent.getAction() == Action.OPEN) {
					openMap.put(fxpElementEvent.getNodeId(), event);
				}
				if (fxpElementEvent.getAction() == Action.CLOSE) {
					closeMap.put(fxpElementEvent.getNodeId(), event);
				}
			} else {
				if (fxpEvent instanceof ISelectableEvent) {
					otherMap.put(((ISelectableEvent) fxpEvent).getNodeId(),
							event);
				}
			}
			final IUpdates updates = handler.update(fxpEvent);
			for (final Map<String, Integer> assignment : updates
					.rejectAssignments()) {
				assert assignment.keySet().size() == 1; // monadic query
				final Integer nodeId = assignment
						.get(XPath2FXP.SELECTING_VARIABLE_NAME);
				if (nodeId == null) {
					allReject = true;
				} else {
					reject(openMap, nodeId);
					reject(otherMap, nodeId);
				}
				// It could be that the CLOSE event is not yet on map.
				if (!reject(closeMap, nodeId)) {
					rejects.add(nodeId);
				}
			}

			for (final Map<String, Integer> assignment : updates
					.selectAssignments()) {
				assert assignment.keySet().size() == 1;
				final Integer nodeId = assignment
						.get(XPath2FXP.SELECTING_VARIABLE_NAME);
				select(openMap, nodeId);
				select(otherMap, nodeId);
				if (!select(closeMap, nodeId)) {
					selects.add(nodeId);
				}
			}

		}
	}

	@Override
	public boolean isStreamingEvaluation() {
		return true;
	}
}
