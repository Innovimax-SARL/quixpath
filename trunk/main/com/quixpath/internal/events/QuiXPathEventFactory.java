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
package com.quixpath.internal.events;

import fr.inria.lille.fxp.datamodel.api.EventFactory;
import fr.inria.lille.fxp.datamodel.api.IFXPEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import innovimax.quixproc.datamodel.QuixEvent.Attribute;
import innovimax.quixproc.datamodel.QuixEvent.EndElement;
import innovimax.quixproc.datamodel.QuixEvent.StartElement;
import innovimax.quixproc.datamodel.QuixEvent.Text;

import java.util.Iterator;
import java.util.Stack;

public class QuiXPathEventFactory implements IEventConverter {

	public static IEventConverter newInstance() {
		return new QuiXPathEventFactory();
	}

	private int nextId;
	private Stack<Integer> stack;

	private QuiXPathEventFactory() {
		nextId = 1;
		stack = new Stack<Integer>();
	}

	@Override
	public Iterable<IQuixPathEvent> convert(QuixEvent event) {
		if (event.isStartElement()) {
			StartElement sEvent = event.asStartElement();
			String label = sEvent.getLocalName();
			IFXPEvent fxpEvent = EventFactory.makeOpenElementEvent(label,
					nextId);
			stack.push(nextId++);
			return toIterable(new FxpCompatibleEvent(event, fxpEvent));
		}
		if (event.isEndElement()) {
			final EndElement sEvent = event.asEndElement();
			final String label = sEvent.getLocalName();
			final IFXPEvent fxpEvent = EventFactory.makeCloseElementEvent(
					label, stack.pop());
			return toIterable(new FxpCompatibleEvent(event, fxpEvent));
		}
		if (event.isText()) {
			final Text tEvent = event.asText();
			final IFXPEvent fxpEvent = EventFactory.makeTextEvent(nextId++);
			final FxpCompatibleEvent e1 = new FxpCompatibleEvent(tEvent,
					fxpEvent);
			final FxpCompatibleEvent e2 = new FxpCompatibleEvent(null,
					EventFactory.makeDataEvent(tEvent.getData()), true);
			return toIterable(e1, e2);
		}
		if (event.isAttribute()) {
			final Attribute attribute = event.asAttribute();
			final String label = attribute.getLocalName();
			final FxpCompatibleEvent e1 = new FxpCompatibleEvent(event,
					EventFactory.makeAttributeEvent(label, nextId++));
			final FxpCompatibleEvent e2 = new FxpCompatibleEvent(null,
					EventFactory.makeDataEvent(attribute.getValue()), true);
			final Iterable<IQuixPathEvent> res = toIterable(e1, e2);
			return res;
		}
		if (event.isStartDocument()) {
			return toIterable(new FxpCompatibleEvent(event,
					EventFactory.makeOpenElementEvent(FAKE_ROOT_LABEL,
							FAKE_ROOT_ID), true));
		}
		if (event.isEndDocument()) {
			return toIterable(new FxpCompatibleEvent(event,
					EventFactory.makeCloseElementEvent(FAKE_ROOT_LABEL,
							FAKE_ROOT_ID), true));
		}

		return toIterable(new FxpNotCompatibleEvent(event));
	}

	Iterable<IQuixPathEvent> toIterable(final IQuixPathEvent... events) {
		return new Iterable<IQuixPathEvent>() {

			@Override
			public Iterator<IQuixPathEvent> iterator() {
				return new Iterator<IQuixPathEvent>() {

					private int index = -1;

					@Override
					public boolean hasNext() {
						return index < events.length - 1;
					}

					@Override
					public IQuixPathEvent next() {
						index++;
						return events[index];
					}

					@Override
					public void remove() {
						// empty
					}
				};
			}
		};
	}

	private final static String FAKE_ROOT_LABEL = null;
	private final static int FAKE_ROOT_ID = 0;

}
