package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class Element extends AbstractTerm implements ILocal {

	public Element() {
		super();
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.element();
	}

	@Override
	public String toString() {
		return "element";
	}

	public boolean isElement() {
		return true;
	}

}
