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
package com.quixpath.internal.interfaces.impl.count;

//count(...) <= value
public class Leq implements IOperator {

	private final long value;

	@Override
	public boolean match(long accumulator) {
		return accumulator <= value;
	}

	@Override
	public boolean early(long accumulator) {
		return accumulator > value;
	}

	public Leq(long value) {
		super();
		this.value = value;
	}

	@Override
	public String toString() {
		return "<=" + value;
	}

}
