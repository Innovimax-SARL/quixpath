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
import fr.inria.lille.fxp.querylanguage.api.IFXPTextTerm;

public class Attribute extends AbstractTerm implements ILocal {

	public final String A;
	public final IText F;
	public final String uri;

	public Attribute(String uri, IText f) {
		this(null, uri, f);
	}

	public Attribute(String a, String uri, IText f) {
		super();
		F = f;
		A = a;
		this.uri = uri;
	}

	@Override
	public IFXPTerm expand(final IFXPFactory fxpTermFactory) {
		IFXPLocalTerm namespace = null;
		if (uri != null) {
			namespace = fxpTermFactory.attributeNamespaceH(uri);
		}
		IFXPLocalTerm att = null;
		if (A != null) {
			att = fxpTermFactory.attributeLabel(A,
					(IFXPTextTerm) F.expand(fxpTermFactory));
		} else {
			att = fxpTermFactory.attributeStar((IFXPTextTerm) F
					.expand(fxpTermFactory));
		}
		if (namespace != null) {
			return fxpTermFactory.localAnd(namespace, att);
		}
		return att;
	}

	@Override
	public String toString() {
		if (A == null) {
			return "@*(" + F + ")";
		} else {
			return "@" + A + "(" + F + ")";
		}
	}

	// only element nodes have attributes.
	public boolean isElement() {
		return true;
	}
}
