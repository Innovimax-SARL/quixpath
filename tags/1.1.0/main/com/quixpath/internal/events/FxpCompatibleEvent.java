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

import fr.inria.lille.fxp.datamodel.api.IFXPEvent;
import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;

/**
 * Use the IEventFactory to create new events.
 * 
 */
/* package */class FxpCompatibleEvent extends AbstractQuixPathEvent {

	/* package */FxpCompatibleEvent(QuixEvent quixEvent, IFXPEvent fxpEvent) {
		this(quixEvent, fxpEvent, false, false);
	}

	/* package */FxpCompatibleEvent(QuixEvent quixEvent, IFXPEvent fxpEvent,
			boolean output) {
		this(quixEvent, fxpEvent, output, false);
		assert fxpEvent != null;
	}

	private FxpCompatibleEvent(QuixEvent quixEvent, IFXPEvent fxpEvent,
			boolean output, boolean matched) {
		super(quixEvent);
		this.fxpEvent = fxpEvent;
		this.output = output;
		this.matched = matched;
	}

	private final IFXPEvent fxpEvent;
	private boolean matched;
	private boolean output;

	@Override
	public IFXPEvent getFxpEvent() {
		return fxpEvent;
	}

	@Override
	public MatchEvent toMatchEvent() {
		return new MatchEvent(getQuixEvent(), matched);
	}

	@Override
	public void select() {
		matched = true;
		output = true;
	}

	@Override
	public void reject() {
		output = true;
	}

	@Override
	public boolean canOutputed() {
		return output;
	}

}
