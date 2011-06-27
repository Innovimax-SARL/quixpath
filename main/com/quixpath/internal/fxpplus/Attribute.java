package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;
import fr.inria.lille.fxp.querylanguage.api.IFXPTextTerm;

/*package*/class Attribute extends AbstractTerm implements ILocal {

	private final String A;
	private final IText F;

	public Attribute(IText f) {
		super();
		F = f;
		A = null;
	}

	public Attribute(String a, IText f) {
		super();
		F = f;
		A = a;
	}

	@Override
	public IFXPTerm expand() {
		if (A != null) {
			return FXPFactory.attributeLabel(A, (IFXPTextTerm) F.expand());
		} else {
			return FXPFactory.attributeStar((IFXPTextTerm) F.expand());
		}
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
