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

public class ChAcc extends AbstractTerm implements IAccumulator {

	public final int n;
	public final ILocal F;

	public ChAcc(int n, ILocal f) {
		super();
		this.n = n;
		F = f;
	}

	@Override
	public IFXPTerm expand(final IFXPFactory fxpTermFactory) {
		IFXPTerm res = F.expand(fxpTermFactory);
		for (int i = 0; i < n; i++) {
			res = fxpTermFactory.child(res); // TODO element.
		}
		return res;
	}

	@Override
	public int getN() {
		return n;
	}

	@Override
	public String toString() {
		return "ch^" + n + "(" + F + ")";
	}

	@Override
	public ILocal getLocalOp() {
		return F;
	}

	@Override
	public boolean isChildAccumulator() {
		return true;
	}

	@Override
	public boolean isChildStartLocalAccumulator() {
		return false;
	}

	public boolean containsChildStar() {
		return F.containsChildStar();
	}

}
