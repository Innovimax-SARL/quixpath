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

public class LocalAnd extends AbstractTerm implements ILocal {

	public final ITerm F1;
	public final ITerm F2;

	public LocalAnd(ITerm f1, ITerm f2) {
		super();
		F1 = f1;
		F2 = f2;
	}

	@Override
	public IFXPTerm expand(final IFXPFactory fxpTermFactory) {
		return fxpTermFactory.localAnd(
				(IFXPLocalTerm) F1.expand(fxpTermFactory),
				(IFXPLocalTerm) F2.expand(fxpTermFactory));
	}

	@Override
	public String toString() {
		return F1 + " local-and " + F2;
	}

	// TODO: and becommes || ?
	public boolean isElement() {
		return F1.isElement() || F2.isElement();
	}

}
