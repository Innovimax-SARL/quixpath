package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class PI extends AbstractTerm implements ILocal {

	public PI() {
		super();
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.PI();
	}

	@Override
	public String toString() {
		return "PI()";
	}
}
