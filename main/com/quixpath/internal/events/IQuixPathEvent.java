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

import fr.inria.mostrare.evoxs.pub.IEvoxsEvent;
import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;

/**
 * IQuixPathEvent is the interface for QuiXPathEvent.
 * 
 * QuiXPathEvents are used to map QuixEvent to EvoxEvent.
 * 
 */
public interface IQuixPathEvent {

	/**
	 * 
	 * The returned value can be null. This indicate that a QuixEvent is
	 * associated to many EvoxsEvent. The mapping from QuixEvent and EvoxsEvent
	 * is store is another IQuixPathEvent.
	 * 
	 * TODO In this case all the EvoxsEvents have the same nodeId?
	 * 
	 * @return the QuixEvent associated to this QuixPathEvent
	 */
	public QuixEvent getQuixEvent();

	/**
	 * 
	 * The returned value can be null. This indicate that a QuixEvent is not
	 * managed by FXP.
	 * 
	 * @return the QuixEvent associated to this QuixPathEvent
	 */
	public IEvoxsEvent getEvoxsEvent();

	/**
	 * Convert the QuixEvent to the corresponding MatchEvent.
	 * 
	 * Matching value is set via select and reject methods.
	 * 
	 * @return
	 */
	public MatchEvent toMatchEvent();

	/**
	 * Set that the QuixEvent is selected.
	 */
	public void select();

	/**
	 * Set that the QuixEvent is rejected.
	 */
	public void reject();

	/**
	 * Set that the QuixEvent can be outputed.
	 */
	public boolean canOutputed();

}
