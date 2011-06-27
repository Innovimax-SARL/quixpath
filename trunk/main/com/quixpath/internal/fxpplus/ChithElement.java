package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class ChithElement extends AbstractTerm implements ITerm {

	private final int i;
	private final ITerm F;

	public ChithElement(int i, ITerm f) {
		super();
		this.i = i;
		F = f;
	}

	@Override
	public IFXPTerm expand() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "ch-" + i + "-th-element(" + F + ")";
	}

}
