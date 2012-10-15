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
package com.quixpath.internal.util;

import innovimax.quixproc.datamodel.IStream;
import innovimax.quixproc.datamodel.Load;
import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import innovimax.quixproc.datamodel.QuixException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

public class Util {

	public static String toString(final IStream<MatchEvent> stream) {
		final StringBuffer res = new StringBuffer();
		try {
			while (stream.hasNext()) {
				MatchEvent matchEvent = stream.next();
				res.append("<" + matchEvent + ">");
			}
		} catch (QuixException e) {
			res.append(e);
		}
		return res.toString();
	}

	public static IStream<QuixEvent> xml2Event(final String xml)
			throws XMLStreamException {
		return xml2Event(new ByteArrayInputStream(xml.getBytes()));
	}

	public static IStream<QuixEvent> xml2Event(final InputStream is)
			throws XMLStreamException {
		final String baseURI = "";
		return new Load(is, baseURI);
	}

	private Util() {
		// empty body
	}

}
