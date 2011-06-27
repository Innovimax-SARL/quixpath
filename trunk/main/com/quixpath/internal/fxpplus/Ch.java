package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class Ch extends AbstractTerm implements ITerm {

	private final ITerm F;

	public Ch(ITerm f) {
		super();
		F = f;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.child(F.expand());
	}

	@Override
	public String toString() {
		return "ch(" + F + ")";
	}

}
