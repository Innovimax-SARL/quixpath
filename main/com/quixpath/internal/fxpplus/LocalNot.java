package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPLocalTerm;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class LocalNot extends AbstractTerm implements ILocal {

	private final ITerm F;

	public LocalNot(ILocal f) {
		super();
		F = f;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.localNot((IFXPLocalTerm) F.expand());
	}

	@Override
	public String toString() {
		return "local-not(" + F + ")";
	}

}
