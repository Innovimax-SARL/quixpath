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
package com.quixpath.internal.interfaces.impl;

import java.util.HashMap;
import java.util.Map;

import com.quixpath.exceptions.QuiXPathException;
import com.quixpath.internal.interfaces.impl.count.IOperator;
import com.quixpath.internal.xpath2fxp.XPath2FXP;

import fr.inria.lille.fxp.queryengine.api.IAssignment;
import fr.inria.lille.fxp.queryengine.api.IEnumerationHandler;
import fr.inria.lille.fxp.queryengine.api.IUpdates;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

// TODO remove public when bugs 12 and 13 will be fixed.
public/* package */class FXPCountExpression extends AbstractFXPExpression {

	private Map<Integer, Integer> counts;
	private final IOperator operator;

	public FXPCountExpression(IFXPTerm binaryTerm, IEnumerationHandler handler,
			IOperator operator) {
		super(binaryTerm, handler);
		this.operator = operator;
	}

	@Override
	public boolean isEmpty() {
		return buffer.isEmpty() && counts.isEmpty();
	}

	protected void init() throws QuiXPathException {
		super.init();
		counts = new HashMap<Integer, Integer>();
	}

	private void notAlive(int nodeId) {
		final Integer count = counts.remove(nodeId);
		// count == null; the node has already been updated
		if (count != null) {
			if (operator.match(count)) {
				buffer.select(nodeId);
			} else {
				buffer.reject(nodeId);
			}
		}
	}

	@Override
	protected void postprocess(final IUpdates updates) {

		boolean isCurrentNodeRejected = true;
		for (final IAssignment assignment : updates.aliveAssignments()) {
			Integer xNodeId = getXNodeId(assignment);
			if (getYNodeId(assignment) == null
					&& (xNodeId != null && currentId == xNodeId)) {
				isCurrentNodeRejected = false;
			}
		}
		for (final IAssignment assignment : updates.selectAssignments()) {
			Integer xNodeId = getXNodeId(assignment);
			if (getYNodeId(assignment) == null
					&& (xNodeId != null && currentId == xNodeId)) {
				isCurrentNodeRejected = false;
			}
		}
		if (isCurrentNodeRejected && !isClosed) {
			if (buffer.isUndetermine(currentId)) {
				buffer.reject(currentId);
			}
		}

		for (final IAssignment assignment : updates.rejectAssignments()) {
			final Integer xId = getXNodeId(assignment);
			if (xId != null && buffer.isUndetermine(xId)) {
				final Integer yId = getYNodeId(assignment);
				if (yId == null) {
					// reject (xId, _)
					if (counts.get(xId) != null) {
						notAlive(xId);
					} else {
						// we have no information about xId
						// case 1: xId is selected => count = 0
						// case 2: xId is not selected => rejected
						if (isClosed && currentId <= xId) {
							counts.put(xId, 0);
							notAlive(xId);
						} else {
							buffer.reject(xId);
						}
					}
				}
			}

		}

		for (final IAssignment assignment : updates.selectAssignments()) {
			final Integer xId = getXNodeId(assignment);
			if (xId != null && buffer.isUndetermine(xId)) {
				final Integer yId = getYNodeId(assignment);
				if (yId != null) {
					Integer count = counts.get(xId);
					if (count == null) {
						count = 0;
					}
					Integer newCount = count + 1;
					if (operator.early(newCount)) {
						counts.put(xId, newCount); // assert xId will be
						// rejected
						notAlive(xId);
					} else {
						counts.put(xId, newCount);
					}
				}
			}
		}
	}

	// fxpLess = allReject && updates.aliveAssignments().isEmpty();

	private Integer getXNodeId(final IAssignment assignment) {
		return assignment.getNodeId(XPath2FXP.SELECTING_VARIABLE_NAME);
	}

	private Integer getYNodeId(final IAssignment assignment) {
		return assignment.getNodeId("y");
	}

	@Override
	public boolean isStreamingEvaluation() {
		return true;
	}

	@Override
	public String toString() {
		return "CountExpression [binaryTerm=" + fxpTerm + "]";
	}

	@Override
	protected void preprocess() {
		// empty
	}

}
