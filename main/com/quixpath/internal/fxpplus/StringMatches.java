package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;
import fr.inria.lille.fxp.querylanguage.api.IFXPWordTerm;

/*package*/class StringMatches extends AbstractTerm implements ITerm {

	private final IAutomata A;

	public StringMatches(IAutomata a) {
		super();
		A = a;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.stringMatches((IFXPWordTerm) A.expand());
	}

	@Override
	public String toString() {
		return "string-matches(" + A + ")";
	}

}
