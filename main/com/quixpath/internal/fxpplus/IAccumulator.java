package com.quixpath.internal.fxpplus;

public interface IAccumulator extends ITerm {

	public int getN();

	public ILocal getLocalOp();

	public boolean isChildAccumulator();

	public boolean isChildStartLocalAccumulator();

}
