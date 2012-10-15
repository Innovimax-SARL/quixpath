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

import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import fr.inria.lille.fxp.datamodel.internal.event.INamedEvent;

/*package*/class FxpNotCompatibleEvent extends AbstractQuixPathEvent {

	@Override
	public INamedEvent getFxpEvent() {
		return null;
	}

	@Override
	public MatchEvent toMatchEvent() {
		return new MatchEvent(getQuixEvent(), false);
	}

	@Override
	public void select() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reject() {
		throw new UnsupportedOperationException();

	}

	@Override
	public boolean canOutputed() {
		return true;
	}

	/* package */FxpNotCompatibleEvent(QuixEvent event) {
		super(event);
		assert event != null;
	}

}
