package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPLocalTerm;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class LocalAnd extends AbstractTerm implements ILocal {

	private final ITerm F1;
	private final ITerm F2;

	public LocalAnd(ITerm f1, ITerm f2) {
		super();
		F1 = f1;
		F2 = f2;
	}

	@Override
	public IFXPTerm expand() {
		return FXPFactory.localAnd((IFXPLocalTerm) F1.expand(),
				(IFXPLocalTerm) F2.expand());
	}

	@Override
	public String toString() {
		return F1 + " local-and " + F2;
	}

	// TODO: and becommes || ?
	public boolean isElement() {
		return F1.isElement() || F2.isElement();
	}

}
