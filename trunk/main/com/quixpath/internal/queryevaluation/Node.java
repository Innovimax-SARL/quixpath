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
import innovimax.quixproc.datamodel.QuixEvent;

/* package*/class Node {

	Node(QuixEvent current) {
		this.current = current;
	}

	private QuixEvent current;
	private QuixEvent next;

	void setCloseEvent(QuixEvent event) {
		if (current == null) {
			current = event;
		} else {
			next = event;
		}

	}

	boolean canOutput() {
		return (current != null) && (select || reject);
	}

	MatchEvent getMatchEvent() {
		MatchEvent res = new MatchEvent(current, select);
		// next can be null
		current = next;
		return res;
	}

	private boolean reject = false;

	void reject() {
		reject = true;
	}

	private boolean select = false;

	void select() {
		select = true;
	}

	@Override
	public String toString() {
		return current + (next != null ? "|" + next : "")
				+ (select ? " select" : "") + (reject ? " reject" : "");
	}

}
