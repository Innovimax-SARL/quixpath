package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;
import fr.inria.lille.fxp.querylanguage.api.IFXPWordTerm;

/*package*/class LocalStringMatches extends AbstractTerm implements IText {

	private final IAutomata automata;

	public LocalStringMatches(IAutomata automata) {
		this.automata = automata;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.localStringMatches((IFXPWordTerm) automata.expand());
	}

	@Override
	public String toString() {
		return "local-string-mactches(" + automata + ")";
	}
}
