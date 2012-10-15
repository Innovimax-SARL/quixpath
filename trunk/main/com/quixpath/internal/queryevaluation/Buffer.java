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
package com.quixpath.internal.queryevaluation;

import innovimax.quixproc.datamodel.IStream;
import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import innovimax.quixproc.datamodel.QuixException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.quixpath.internal.mvc.listeners.IBufferListener;

/**
 * The buffer is a partial tree (DOM) on the document.
 * 
 */
public class Buffer implements IBuffer {

	private Map<Integer, Node> undetermine = new HashMap<Integer, Node>();
	private Stack<Node> stacks = new Stack<Node>();
	private LinkedList<Node> buffer = new LinkedList<Node>();

	@Override
	public IStream<MatchEvent> read() {
		return new IStream<MatchEvent>() {

			@Override
			public MatchEvent next() throws QuixException {
				MatchEvent res = buffer.removeFirst().getMatchEvent();
				fireRead(res);
				return res;

			}

			@Override
			public boolean hasNext() throws QuixException {
				if (buffer.isEmpty()) {
					return false;
				}
				return buffer.getFirst().canOutput();
			}

			@Override
			public void close() {
				// TODO Auto-generated method stub

			}
		};

	}

	public void select(Integer nodeId) {
		undetermine.remove(nodeId).select();
	}

	public void reject(Integer nodeId) {
		undetermine.remove(nodeId).reject();
	}

	@Override
	public void write(QuixEvent quixEvent, Integer nodeId, boolean reject) {
		fireWrite(quixEvent);
		Node node;
		switch (quixEvent.getType()) {
		case START_DOCUMENT:
		case START_ELEMENT:
		case START_SEQUENCE:
			node = new Node(quixEvent);
			undetermine.put(nodeId, node);
			stacks.push(node);
			buffer.addLast(node);
			if (reject) {
				reject(nodeId);
			}
			break;
		case END_DOCUMENT:
		case END_ELEMENT:
		case END_SEQUENCE:
			node = stacks.pop();
			node.setCloseEvent(quixEvent);
			buffer.addLast(node);
			break;
		case ATTRIBUTE:
		case TEXT:
		case PI:
		case COMMENT:
			node = new Node(quixEvent);
			undetermine.put(nodeId, node);
			buffer.addLast(node);
			if (reject) {
				reject(nodeId);
			}
			break;
		default:
			break;
		}

	}

	private List<IBufferListener> listeners = null;

	private void fireRead(MatchEvent event) {
		if (listeners != null) {
			for (IBufferListener listener : listeners) {
				listener.read(event.getEvent());
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
	public void addListerner(IBufferListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<IBufferListener>();
		}
		listeners.add(listener);
	}

	@Override
	public boolean isEmpty() {
		return undetermine.isEmpty() && stacks.isEmpty() && buffer.isEmpty();
	}

	@Override
	public String toString() {
		return buffer.toString();
	}

	public boolean isUndetermine(Integer xId) {
		return undetermine.get(xId) != null;
	}

}
