package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class Equals extends AbstractTerm implements IAutomata {

	private final String d;

	public Equals(String d) {
		this.d = d;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.equals(d);
	}

	@Override
	public String toString() {
		return "equals(" + d + ")";
	}
}
