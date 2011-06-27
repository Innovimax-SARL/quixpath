package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class NameSpace extends AbstractTerm implements ILocal {

	private final String H;

	public NameSpace(String H) {
		super();
		this.H = H;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.namespaceH(H);
	}

	@Override
	public String toString() {
		return "namespace-" + H;
	}
}
