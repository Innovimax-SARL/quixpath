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
package com.quixpath.internal.interfaces.impl;

import com.quixpath.exceptions.QuiXPathException;
import com.quixpath.internal.xpath2fxp.XPath2FXP;

import fr.inria.lille.fxp.queryengine.api.IAssignment;
import fr.inria.lille.fxp.queryengine.api.IEnumerationHandler;
import fr.inria.lille.fxp.queryengine.api.IUpdates;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/**
 * Compile representation of a query that is compatible with FXP.
 * 
 * The evaluation algorithm is streaming: only a part of the document is
 * buffered.
 * 
 * Today, the query engine is the FXP query engine.
 * 
 */
/* package */class FXPExpression extends AbstractFXPExpression {

	/* package */FXPExpression(final IFXPTerm fxpTerm,
			final IEnumerationHandler handler) {
		super(fxpTerm, handler);
	}

	@Override
	public String toString() {
		return fxpTerm.toString();
	}

	protected void init() throws QuiXPathException {
		super.init();
		allReject = false;
		fxpLess = false;

	}

	@Override
	protected void preprocess() {
		// empty
	}

	@Override
	protected void postprocess(final IUpdates updates) {

		boolean isCurrentIdRejected = true;

		for (final IAssignment assignment : updates.rejectAssignments()) {
			final Integer nodeId = getNodeId(assignment);
			if (nodeId != null && currentId == nodeId) {
				isCurrentIdRejected = false;
			}
			if (nodeId == null) {
				allReject = true;
			} else {
				buffer.reject(nodeId);
			}
		}
		// currentId

		for (final IAssignment assignment : updates.selectAssignments()) {
			Integer nodeId = getNodeId(assignment);
			if (currentId == nodeId) {
				isCurrentIdRejected = false;
			}
			buffer.select(nodeId);
		}
		for (final IAssignment assignment : updates.aliveAssignments()) {
			Integer nodeId = getNodeId(assignment);
			if (nodeId != null && currentId == nodeId) {
				isCurrentIdRejected = false;
			}
		}
		if (isCurrentIdRejected & buffer.isUndetermine(currentId)) {
			buffer.reject(currentId);
		}
		fxpLess = allReject && updates.aliveAssignments().isEmpty();
	}

	private Integer getNodeId(final IAssignment assignment) {

		assert assignment.size() == 1; // monadic query
		return assignment.getNodeId(XPath2FXP.SELECTING_VARIABLE_NAME);
		// nodeId = null <-> empty candidate
	}

	@Override
	public boolean isStreamingEvaluation() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return buffer.isEmpty();
	}
}
