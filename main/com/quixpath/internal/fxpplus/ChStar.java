package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class ChStar extends AbstractTerm implements ITerm {

	private final ITerm F;

	public ChStar(ITerm f) {
		super();
		F = f;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.childStar(F.expand());
	}

	@Override
	public String toString() {
		return "ch*(" + F + ")";
	}

}
