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

import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import innovimax.quixproc.datamodel.shared.ISimpleQueue;

import java.net.URI;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

/**
 * Convert a tiny tree (DOM) into a stream of MatchEvent. The value of the
 * matching is determined by the content of XdmSequenceIterator.
 */
public class MyEventConverter implements Runnable {

	private final ISimpleQueue<MatchEvent> doc;
	private XdmNode node = null;
	private boolean running = true;
	private final XdmSequenceIterator it;

	/**
	 * 
	 * @param doc
	 * @param node
	 * @param it
	 *            returns the node is the document order.
	 */
	public MyEventConverter(ISimpleQueue<MatchEvent> doc, XdmNode node,
			XdmSequenceIterator it) {
		// this.runtime = runtime;
		this.doc = doc;
		this.node = node;
		this.it = it;
	}

	public void run() {
		try {
			startProcess();
			process();
			doc.close();
			endProcess();
			running = false;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isRunning() {
		return running;
	}

	private XdmItem current = null;

	/**
	 * parse handler interface
	 */

	private void process() {
		if (it.hasNext()) {
			current = it.next();
		}
		final boolean match = computeMatch(node);
		final URI documentURI = node.getDocumentURI();
		final String uri = documentURI == null ? "" : node.getDocumentURI()
				.toString();
		doc.append(new MatchEvent(QuixEvent.getStartDocument(uri), match));
		processnode(node);
		doc.append(new MatchEvent(QuixEvent.getEndDocument(uri), match));
	}

	private boolean computeMatch(XdmNode localnode) {
		final boolean match;
		if (current != null && !(current instanceof XdmNode)) {
			throw new Error("Can not select: " + current);
		}
		// The equals() method on this class (XdmNode) can be used to test
		// for node identity.
		if (current != null && localnode.equals(current)) {
			match = true;
			if (it.hasNext()) {
				// assume that it returns the node in the document order.
				current = it.next();
			} else {
				current = null;
			}
		} else {
			match = false;
		}
		return match;
	}

	private void processnode(XdmNode localnode) {
		final boolean match = computeMatch(localnode);
		switch (localnode.getNodeKind()) {
		case DOCUMENT:
			// do nothing
			for (XdmSequenceIterator iter = localnode.axisIterator(Axis.CHILD); iter
					.hasNext();) {
				XdmNode item = (XdmNode) iter.next();
				processnode(item);
			}
			break;
		case ELEMENT:
			doc.append(new MatchEvent(QuixEvent.getStartElement(localnode
					.getNodeName().getLocalName(), localnode.getNodeName()
					.getNamespaceURI(), localnode.getNodeName().getPrefix()),
					match));
			for (XdmSequenceIterator iter = localnode
					.axisIterator(Axis.ATTRIBUTE); iter.hasNext();) {
				XdmNode item = (XdmNode) iter.next();
				processnode(item);
			}
			for (XdmSequenceIterator iter = localnode.axisIterator(Axis.CHILD); iter
					.hasNext();) {
				XdmNode item = (XdmNode) iter.next();
				processnode(item);
			}
			doc.append(new MatchEvent(QuixEvent.getEndElement(localnode
					.getNodeName().getLocalName(), localnode.getNodeName()
					.getNamespaceURI(), localnode.getNodeName().getPrefix()),
					match));
			break;
		case ATTRIBUTE:
			doc.append(new MatchEvent(QuixEvent.getAttribute(localnode
					.getNodeName().getLocalName(), localnode.getNodeName()
					.getNamespaceURI(), localnode.getNodeName().getPrefix(),
					localnode.getStringValue()), match));
			break;
		case TEXT:
			doc.append(new MatchEvent(QuixEvent.getText(localnode
					.getStringValue()), match));
			break;
		case COMMENT:
			doc.append(new MatchEvent(QuixEvent.getComment(localnode
					.getStringValue()), match));
			break;
		case PROCESSING_INSTRUCTION:
			doc.append(new MatchEvent(QuixEvent.getPI(localnode.getNodeName()
					.getLocalName(), localnode.getStringValue()), match));
			break;
		case NAMESPACE:
			doc.append(new MatchEvent(QuixEvent.getNamespace(localnode
					.getNodeName().getPrefix(), localnode.getNodeName()
					.getNamespaceURI()), match));
			break;
		}
	}

	public void startProcess() {
		// empty body
	}

	public void endProcess() {
		// empty body
	}

}