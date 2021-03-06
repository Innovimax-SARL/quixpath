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
package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.IFXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

public interface ITerm {

	/**
	 * Expand to FXP
	 */
	public IFXPTerm expand(IFXPFactory fxpTermFactory);

	public boolean isLocal();

	public boolean isText();

	public boolean isAutomata();

	public boolean isAcumulator();

	public boolean isChildStarPattern();

	public ITerm childStarPatternOperand();

	/**
	 * true -> the iterm is typed by element <br/>
	 * false -> we do not know but may be it is an element.
	 */
	public boolean isElement();

	public boolean isOuterMost();

	public boolean containsChildStar();

	public boolean isChildElementPattern();

	public boolean isVariable();
}
