package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class Contains extends AbstractTerm implements IAutomata {

	private final String d;

	public Contains(String d) {
		this.d = d;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.contains(d);
	}

	@Override
	public String toString() {
		return "contains(" + d + ")";
	}
}
