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
package com.quixpath.internal.xpath2fxp;

/**
 * Immutable implementation of the context.
 * 
 */
public class FXPContext implements IContext {

	private final Node roots;
	private final Node filters;
	private final Node initialisation;
	// Null = no value.s
	private final String stringLitteralValue;

	public FXPContext() {
		this(init(), init(), init(), null);
	}

	private static Node init() {
		return new Node(false, null);
	}

	private FXPContext(Node roots, Node filters, Node initialisation,
			String stringLitteralValue) {
		super();
		this.roots = roots;
		this.filters = filters;
		this.initialisation = initialisation;
		this.stringLitteralValue = stringLitteralValue;
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
		return new FXPContext(r, f, initialisation, stringLitteralValue);
	}

	@Override
	public FXPContext exitFilter() {
		Node f = filters.getNext();
		Node r = roots.getNext();
		return new FXPContext(r, f, initialisation, stringLitteralValue);
	}

	@Override
	public FXPContext setRoot(boolean root) {
		Node f = filters;
		Node r = new Node(root, roots.getNext());
		return new FXPContext(r, f, initialisation, stringLitteralValue);
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

	@Override
	public boolean isTrunk() {
		return !isFilter();
	}

	@Override
	public boolean isInitialisation() {
		return initialisation.getValue();
	}

	@Override
	public IContext newSteps() {
		return new FXPContext(roots, filters, new Node(true, initialisation),
				stringLitteralValue);
	}

	@Override
	public IContext nextStep() {
		return new FXPContext(roots, filters, initialisation.getNext(),
				stringLitteralValue);
	}

	@Override
	public boolean isStepsEqualsStringLitteral() {
		return stringLitteralValue != null;
	}

	@Override
	public IContext setStepsEqualsStringLitteral(String stringLitteralValue) {
		return new FXPContext(roots, filters, initialisation,
				stringLitteralValue);

	}

	@Override
	public String getStepsEqualsStringLitteral() {
		return stringLitteralValue;
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
