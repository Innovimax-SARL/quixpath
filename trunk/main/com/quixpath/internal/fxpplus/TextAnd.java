package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;
import fr.inria.lille.fxp.querylanguage.api.IFXPTextTerm;

/*package*/class TextAnd extends AbstractTerm implements IText {

	private final IText T1;
	private final IText T2;

	public TextAnd(IText t1, IText t2) {
		super();
		T1 = t1;
		T2 = t2;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.textAnd((IFXPTextTerm) T1.expand(),
				(IFXPTextTerm) T2.expand());
	}

	@Override
	public String toString() {
		return T1 + " text-and " + T2;
	}

}
