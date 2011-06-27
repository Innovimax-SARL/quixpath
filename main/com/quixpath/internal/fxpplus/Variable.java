package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class Variable extends AbstractTerm implements IText {

	private final String name;

	public Variable(String variable) {
		this.name = variable;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.variable(name);
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	// TODO set to false when FXP will have the element operator.
	public boolean isElement() {
		return true;
	}
}
