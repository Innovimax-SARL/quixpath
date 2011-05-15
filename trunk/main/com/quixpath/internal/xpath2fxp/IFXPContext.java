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
 * IFXPContext helps to check weather a filter is or not evaluated from the root
 * of the document.
 * 
 * IFXPContext must be used in parallel with XPath2FXP. See this class to have
 * information on the notion of sub-queries.
 * 
 */
public interface IFXPContext {

	/**
	 * True iff the fxp sub-query is evaluated from the root of the document.
	 */
	public boolean isRooted();

	/**
	 * True iff the fxp sub-query is a term of a filter.
	 */
	public boolean isFilter();

	/**
	 * Start to translate a filter.
	 */
	public FXPContext enterFilter();

	/**
	 * End to translate a filter.
	 */
	public FXPContext exitFilter();

	/**
	 * Set information about a root.
	 */
	public FXPContext setRoot(boolean root);

}
