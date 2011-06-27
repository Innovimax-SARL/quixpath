package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class InnermostChStart extends AbstractTerm implements ITerm {

	private final String A;
	private final ITerm F;

	public InnermostChStart(String a, ITerm f) {
		super();
		A = a;
		F = f;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.outerMost(A, F.expand());
	}

	@Override
	public String toString() {
		return "innermost-ch*-" + A + "(" + F + ")";
	}

}
