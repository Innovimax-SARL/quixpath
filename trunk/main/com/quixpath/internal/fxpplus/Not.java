package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class Not extends AbstractTerm implements ITerm {

	private final ITerm F;

	public Not(ITerm f) {
		super();
		F = f;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.not(F.expand());
	}

	@Override
	public String toString() {
		return "not(" + F + ")";
	}

}
