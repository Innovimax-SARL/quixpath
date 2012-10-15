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
import fr.inria.lille.fxp.querylanguage.api.IFXPLocalTerm;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/**
 * ch^n(local-ch*())
 * 
 */
/* package */class ChStarLocalAcc extends AbstractTerm implements IAccumulator {

	private final int n;
	private final ILocal F;

	public ChStarLocalAcc(int n, ILocal f) {
		super();
		this.n = n;
		F = f;
	}

	@Override
	public IFXPTerm expand(final IFXPFactory fxpTermFactory) {
		IFXPTerm res = fxpTermFactory.childStarLocal((IFXPLocalTerm) F
				.expand(fxpTermFactory));
		for (int i = 0; i < n; i++) {
			res = fxpTermFactory.child(res); // TODO element.
		}
		return res;
	}

	public ITerm childStarPatternOperand() {
		return F;
	}

	@Override
	public int getN() {
		return n;
	}

	@Override
	public String toString() {
		return "ch^" + n + "(ch*-local(" + F + "))";
	}

	@Override
	public ILocal getLocalOp() {
		return F;
	}

	@Override
	public boolean isChildAccumulator() {
		return false;
	}

	@Override
	public boolean isChildStartLocalAccumulator() {
		return true;
	}

	public boolean containsChildStar() {
		return n > 0;
	}

}
