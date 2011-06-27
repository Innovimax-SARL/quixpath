package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class ChLastElement extends AbstractTerm implements ITerm {

	private final ITerm F;

	public ChLastElement(ITerm f) {
		super();
		F = f;
	}

	@Override
	public IFXPTerm expand() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "ch-last-element(" + F + ")";
	}

}
