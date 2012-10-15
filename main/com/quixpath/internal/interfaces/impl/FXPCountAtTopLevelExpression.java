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

import innovimax.quixproc.datamodel.IStream;
import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import innovimax.quixproc.datamodel.QuixEvent.Attribute;
import innovimax.quixproc.datamodel.QuixEvent.EndDocument;
import innovimax.quixproc.datamodel.QuixEvent.EndElement;
import innovimax.quixproc.datamodel.QuixEvent.PI;
import innovimax.quixproc.datamodel.QuixEvent.StartDocument;
import innovimax.quixproc.datamodel.QuixEvent.StartElement;
import innovimax.quixproc.datamodel.QuixEvent.Text;
import innovimax.quixproc.datamodel.QuixException;
import innovimax.quixproc.datamodel.QuixValue;

import com.quixpath.exceptions.QuiXPathException;
import com.quixpath.internal.interfaces.IFXPCompatible;
import com.quixpath.internal.mvc.listeners.IBufferListener;

import fr.inria.lille.fxp.queryengine.api.IEnumerationHandler;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/* package */final class FXPCountAtTopLevelExpression extends
		AbstractQuiXPathExpression implements IFXPCompatible {

	private final IFXPTerm fxpTerm;
	private final IEnumerationHandler handler;

	public FXPCountAtTopLevelExpression(IFXPTerm fxpTerm,
			IEnumerationHandler handler) {
		super(fxpTerm.getStaticProperties().getProjectionProperties());
		this.fxpTerm = fxpTerm;
		this.handler = handler;
	}

	@Override
	public boolean isStreamingEvaluation() {
		return true;
	}

	@Override
	public void addBufferListener(IBufferListener listener) {
		// empty
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public IStream<MatchEvent> update(QuixEvent event) throws QuiXPathException {
		throw new UnsupportedOperationException();
	}

	private int depth;

	public QuixValue evaluate(IStream<QuixEvent> stream)
			throws QuiXPathException {
		long count = 0;

		try {
			depth = 0;
			handler.startStream();
			while (stream.hasNext()) {
				count += count(stream.next());
			}
			handler.endStream();
		} catch (QuixException e) {
			throw new QuiXPathException(e);
		}
		return new QuixValue("" + count);
	}

	private final long count(final QuixEvent quixEvent)
			throws QuiXPathException {

		switch (quixEvent.getType()) {
		case TEXT:
			if (keepText && depth <= depthBound) {
				final Text el = quixEvent.asText();
				{
					long count = 0;
					// text event
					count = handler.updateText(0).selectAssignments().size();

					// value
					count += handler.updateData(el.getData())
							.selectAssignments().size();
					return count;
				}
			}
			return 0;
		case START_ELEMENT:
			depth++;
			if (depth <= depthBound) {
				final StartElement elsel = quixEvent.asStartElement();
				return handler
						.updateOpenElement(0, elsel.getLocalName(),
								elsel.getURI()).selectAssignments().size();
			}
			return 0;
		case END_ELEMENT:
			depth--;
			if (depth < depthBound) {
				final EndElement eleel = quixEvent.asEndElement();
				return handler
						.updateCloseElement(0, eleel.getLocalName(),
								eleel.getURI()).selectAssignments().size();
			}
			return 0;
		case ATTRIBUTE:
			if (keepAttribute && depth < depthBound) {
				final Attribute at = quixEvent.asAttribute();
				long count;
				{// attribute
					count = handler
							.updateAttribute(0, at.getLocalName(), at.getURI())
							.selectAssignments().size();
				}
				{ // value
					count += handler.updateData(at.getValue())
							.selectAssignments().size();
				}
				return count;
			}
			return 0;
		case START_DOCUMENT:
			final StartDocument startDocument = quixEvent.asStartDocument();
			return handler
					.updateOpenElement(0, AbstractFXPExpression.FAKE_ROOT,
							startDocument.getURI()).selectAssignments().size();
		case END_DOCUMENT:
			final EndDocument endElement = quixEvent.asEndDocument();
			return handler
					.updateCloseElement(0, AbstractFXPExpression.FAKE_ROOT,
							endElement.getURI()).selectAssignments().size();
		case PI:
			if (keepPI && depth < depthBound) {
				final PI pi = quixEvent.asPI();
				long count;
				{
					count = handler.updatePI(0, pi.getTarget())
							.selectAssignments().size();
				}
				return count;
			}
			return 0;
		case COMMENT:
			if (keepComment && depth < depthBound) {
				return handler.updateComment(0).selectAssignments().size();
			}
			return 0;
		default:
			return 0;
		}

	}

	@Override
	public String toString() {
		return "count(" + fxpTerm + ")";
	}

	@Override
	public IFXPTerm getFXPTerm() {
		return fxpTerm;
	}
}
