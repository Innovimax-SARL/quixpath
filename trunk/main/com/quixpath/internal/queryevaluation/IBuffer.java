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
import innovimax.quixproc.datamodel.Stream;

import com.quixpath.internal.events.IQuixPathEvent;
import com.quixpath.internal.mvc.listeners.IBufferListener;

/**
 * <p>
 * IBuffer helps to output the QuixEvent in the order of the document.
 * </p>
 * 
 * <p>
 * This buffer stores event until they can be outputed: an event can be outputed
 * <ul>
 * <li>if its type is not OPEN_ELEMENT</li>
 * <li>if its type is OPEN_ELEMENT and its is selected or rejected</li>
 * <li>if its predecessor can be outputed (by transition, all its predecessors
 * can be outputed)</li>
 * </ul>
 * </p>
 * 
 * 
 * <p>
 * This buffer ouputs MatchEvent. The match value is:
 * <ul>
 * <li>false if its type is not OPEN_ELEMENT</li>
 * <li>false if its type is OPEN_ELEMENT and its rejected</li>
 * <li>true if its type is OPEN_ELEMENT and its selected</li>
 * <ul/>
 * </p>
 * </p>
 * 
 */
public interface IBuffer {

	public Stream<MatchEvent> read();

	public void write(IQuixPathEvent event);

	public void addListerner(IBufferListener listener);

}
