package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class TrueQuery extends AbstractTerm implements IText {

	@Override
	public IFXPTerm expand() {
		return FXPFactory.trueQuery();
	}

	@Override
	public String toString() {
		return "true";
	}
}
