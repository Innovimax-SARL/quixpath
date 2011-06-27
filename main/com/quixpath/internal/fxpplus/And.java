package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class And extends AbstractTerm implements ITerm {

	private final ITerm F1;
	private final ITerm F2;

	public And(ITerm f1, ITerm f2) {
		super();
		F1 = f1;
		F2 = f2;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.and(F1.expand(), F2.expand());
	}

	@Override
	public String toString() {
		return F1 + " and " + F2;
	}

}
