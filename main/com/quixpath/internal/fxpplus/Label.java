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

public class Label extends AbstractTerm implements ITerm {

	public final String uri;
	public final String A;
	public final ITerm F;

	public Label(String uri, String a, ITerm f) {
		super();
		A = a;
		F = f;
		this.uri = uri;
	}

	@Override
	public IFXPTerm expand(final IFXPFactory fxpTermFactory) {
		IFXPLocalTerm namespace = null;
		if (uri != null) {
			namespace = fxpTermFactory.namespaceH(uri);
		}
		final IFXPTerm label = fxpTermFactory
				.label(A, F.expand(fxpTermFactory));
		if (namespace != null) {
			return fxpTermFactory.and(namespace, label);
		}
		return label;
	}

	@Override
	public String toString() {
		return A + "(" + F + ")";
	}

	public boolean containsChildStar() {
		return F.containsChildStar();
	}

}
