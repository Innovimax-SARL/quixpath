package com.quixpath.internal.fxpplus;

/*package*/class AbstractTerm {

	public boolean isLocal() {
		return this instanceof ILocal;
	}

	public boolean isText() {
		return this instanceof IText;
	}

	public boolean isAutomata() {
		return this instanceof IAutomata;
	}

	public boolean isAcumulator() {
		return this instanceof IAccumulator;
	}

	public boolean isOuterMost() {
		return this instanceof OuttermostChStart;
	}

	public boolean isElement() {
		return false;
	}

}
