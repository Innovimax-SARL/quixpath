package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class EndWith extends AbstractTerm implements IAutomata {

	private final String d;

	public EndWith(String d) {
		this.d = d;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.endsWith(d);
	}

	@Override
	public String toString() {
		return "end-with(" + d + ")";
	}
}
