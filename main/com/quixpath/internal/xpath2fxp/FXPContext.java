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
package com.quixpath.internal.xpath2fxp;

/**
 * Immutable implementation of the context.
 * 
 */
public class FXPContext implements IFXPContext {

	private final Node roots;
	private final Node filters;

	public FXPContext() {
		this(init(), init());
	}

	private static Node init() {
		return new Node(false, null);
	}

	private FXPContext(Node roots, Node filters) {
		super();
		this.roots = roots;
		this.filters = filters;
		assert invariant1();
		assert invariant2();
	}

	@Override
	public boolean isRooted() {
		assert invariant1();
		assert invariant2();
		return roots.getValue();
	}

	@Override
	public boolean isFilter() {
		return filters.getValue();
	}

	@Override
	public FXPContext enterFilter() {
		Node f = new Node(true, filters);
		Node r = new Node(false, roots);
		return new FXPContext(r, f);
	}

	@Override
	public FXPContext exitFilter() {
		Node f = filters.getNext();
		Node r = roots.getNext();
		return new FXPContext(r, f);
	}

	@Override
	public FXPContext setRoot(boolean root) {
		Node f = filters;
		Node r = new Node(root, roots.getNext());
		return new FXPContext(r, f);
	}

	// Invariants
	// 1. roots and filters always contain the same number of elements: trunk +
	// one by filters.
	// 2. roots and filters are never empty: information on the trunk of the
	// query.

	private boolean invariant1() {
		return roots.size() == filters.size();
	}

	private boolean invariant2() {
		return filters != null && roots != null;
	}

}

/**
 * Immutable implementation of an immutable Linked List of booleans.
 * 
 */
class Node {
	private final boolean value;
	private final Node next;

	Node(boolean value, Node next) {
		super();
		this.value = value;
		this.next = next;
	}

	public boolean getValue() {
		return value;
	}

	public Node getNext() {
		return next;
	}

	public int size() {
		int size = 0;
		Node it = this;
		while (it != null) {
			size++;
			it = it.next;
		}
		return size;
	}

}
