package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class Text extends AbstractTerm implements ILocal {

	public Text() {
		super();
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.text();
	}

	@Override
	public String toString() {
		return "text";
	}
}
