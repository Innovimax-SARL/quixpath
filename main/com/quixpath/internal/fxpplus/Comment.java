package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class Comment extends AbstractTerm implements ILocal {

	public Comment() {
		super();
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.comment();
	}

	@Override
	public String toString() {
		return "comment";
	}
}
