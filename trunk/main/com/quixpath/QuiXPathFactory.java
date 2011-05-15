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
package com.quixpath;

import com.quixpath.exceptions.QuiXPathException;
import com.quixpath.interfaces.IQuiXPath;
import com.quixpath.internal.interfaces.impl.QuiXPathImpl;

/**
 * QuiXPathFactory can be used to create QuiXPath objects.
 * 
 */
public class QuiXPathFactory {

	/**
	 * Get a new QuiXPathFactory instance.
	 * 
	 * @return Instance of a QuiXPathFactory.
	 * @throws QuiXPathException
	 *             When there is a failure in creating an QuiXPathFactory.
	 */
	public static QuiXPathFactory newInstance() throws QuiXPathException {
		return new QuiXPathFactory();

	}

	/**
	 * Get a new IQuiXPath instance.
	 * 
	 * @return Instance of an IQuiXPath.
	 */
	public IQuiXPath newQuiXPath() {
		return new QuiXPathImpl();
	}

	private QuiXPathFactory() {
		// empty
	}

}
