package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;
import fr.inria.lille.fxp.querylanguage.api.IFXPTextTerm;

/*package*/class TextNot extends AbstractTerm implements IText {

	private final IText T;

	public TextNot(IText t) {
		super();
		T = t;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.textNot((IFXPTextTerm) T.expand());
	}

	@Override
	public String toString() {
		return "text-not(" + T + ")";
	}

}
