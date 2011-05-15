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
package com.quixpath.internal.queryevaluation;

import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.IStream;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.quixpath.internal.events.IQuixPathEvent;
import com.quixpath.internal.mvc.listeners.IBufferListener;

// TODO make an immutable buffer?
// safe thread? NO
public class Buffer implements IBuffer {

	private final LinkedList<IQuixPathEvent> buffer;

	public Buffer() {
		super();
		buffer = new LinkedList<IQuixPathEvent>();
		listeners = new ArrayList<IBufferListener>();
	}

	@Override
	public void write(final IQuixPathEvent event) {
		buffer.addFirst(event);
		fireWrite(event);
	}

	@Override
	public IStream<MatchEvent> read() {
		return new IStream<MatchEvent>() {

			@Override
			public boolean hasNext() {
				if (buffer.isEmpty()) {
					return false;
				}
				IQuixPathEvent quixPathEvent = buffer.getLast();
				return quixPathEvent.canOutputed();

			}

			@Override
			public MatchEvent next() {
				final IQuixPathEvent quixPathEvent = buffer.removeLast();
				assert quixPathEvent.getQuixEvent() != null;
				while (!buffer.isEmpty()
						&& buffer.getLast().getQuixEvent() == null) {
					buffer.removeLast();
				}
				fireRead(quixPathEvent);
				return quixPathEvent.toMatchEvent();
			}

			@Override
			public void close() {

			}

		};
	}

	private void fireRead(IQuixPathEvent event) {
		for (IBufferListener listener : listeners) {
			listener.read(event);
		}
	}

	private void fireWrite(IQuixPathEvent event) {
		for (IBufferListener listener : listeners) {
			listener.write(event);
		}
	}

	private final List<IBufferListener> listeners;

	@Override
	public void addListerner(IBufferListener listener) {
		listeners.add(listener);
	}

}
