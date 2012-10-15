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
package com.quixpath.exceptions;

import net.sf.saxon.trans.XPathException;

import com.quixpath.internal.fxpplus.ITerm;

/**
 * Thrown when the quixpath compiler is not able to compile an XPath Query into
 * an FXPTerm.
 */
public class UnsupportedQueryException extends QuiXPathException {

	private static final long serialVersionUID = -7667359529361880303L;

	public UnsupportedQueryException() {
		super();
	}

	public UnsupportedQueryException(String message) {
		super(message);
	}

	public UnsupportedQueryException(Exception cause) {
		super(cause);
	}

	public UnsupportedQueryException(Error cause) {
		super(cause);
	}

	public UnsupportedQueryException(String xPathQuery, ITerm subQuery) {
		super(xPathQuery);
	}

	public UnsupportedQueryException(XPathException cause, String xPathQuery) {
		super(cause, xPathQuery);
	}

	public UnsupportedQueryException(Throwable cause, String xPathQuery) {
		super(cause, xPathQuery);
	}

}
