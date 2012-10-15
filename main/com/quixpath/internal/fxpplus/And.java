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
package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.IFXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

public class And extends AbstractTerm implements ITerm {

	public final ITerm F1;
	public final ITerm F2;

	public And(ITerm f1, ITerm f2) {
		super();
		F1 = f1;
		F2 = f2;
	}

	@Override
	public IFXPTerm expand(final IFXPFactory fxpTermFactory) {
		return fxpTermFactory.and(F1.expand(fxpTermFactory),
				F2.expand(fxpTermFactory));
	}

	public boolean isChildStarPattern() {
		if (F1.isElement()) {
			return F2.isChildStarPattern();
		}
		if (F2.isElement()) {
			return F1.isChildStarPattern();
		}
		return false;
	}

	public ITerm childStarPatternOperand() {
		assert isChildStarPattern();
		if (F1.isElement()) {
			return F2.childStarPatternOperand();
		}
		return F1.childStarPatternOperand();
	}

	@Override
	public String toString() {
		return F1 + " and " + F2;
	}

	public boolean containsChildStar() {
		return F1.containsChildStar() || F2.containsChildStar();
	}

}
