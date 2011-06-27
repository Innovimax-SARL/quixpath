package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.FXPFactory;
import fr.inria.lille.fxp.querylanguage.api.IFXPLocalTerm;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/*package*/class ChStartLocalAcc extends AbstractTerm implements IAccumulator {

	private final int n;
	private final ILocal F;

	public ChStartLocalAcc(int n, ILocal f) {
		super();
		this.n = n;
		F = f;
	}

	@Override
	public IFXPTerm expand() {
		IFXPTerm res = FXPFactory.childStarLocal((IFXPLocalTerm) F.expand());
		for (int i = 0; i < n; i++) {
			res = FXPFactory.child(res); // TODO element.
		}
		return res;
	}

	@Override
	public int getN() {
		return n;
	}

	@Override
	public String toString() {
		return "ch*-local(" + F + ")";
	}

	@Override
	public ILocal getLocalOp() {
		return F;
	}

	@Override
	public boolean isChildAccumulator() {
		return false;
	}

	@Override
	public boolean isChildStartLocalAccumulator() {
		return true;
	}

}
