package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPLocalTerm;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class ChStartLocal extends AbstractTerm implements ITerm {

	private final ILocal F;

	public ChStartLocal(ILocal f) {
		super();
		F = f;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.childStarLocal((IFXPLocalTerm) F.expand());
	}

	@Override
	public String toString() {
		return "ch*-local(" + F + ")";
	}

}
