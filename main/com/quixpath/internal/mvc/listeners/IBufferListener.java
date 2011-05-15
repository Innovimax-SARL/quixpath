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
package com.quixpath.internal.mvc.listeners;

import com.quixpath.internal.events.IQuixPathEvent;

public interface IBufferListener {

	/**
	 * Invoke when an event is read from the buffer
	 * 
	 * @param event
	 */
	public void read(IQuixPathEvent event);

	/**
	 * Invoke when an event is written in the buffer
	 * 
	 * Event can be null if the algorithm does not build IQuixPathEvent.
	 * 
	 * @param event
	 */
	public void write(IQuixPathEvent event);

}
