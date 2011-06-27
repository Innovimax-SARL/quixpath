package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class StartWith extends AbstractTerm implements IAutomata {

	private final String d;

	public StartWith(String d) {
		this.d = d;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.startsWith(d);
	}

	@Override
	public String toString() {
		return "start-with(" + d + ")";
	}
}
