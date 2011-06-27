package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class LocalLabel extends AbstractTerm implements ILocal {

	private final String A;

	public LocalLabel(String a) {
		super();
		A = a;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.localLabel(A);
	}

	@Override
	public String toString() {
		return A;
	}

	// TODO label can be use with PI?
	public boolean isElement() {
		return true;
	}
}
