package com.quixpath.internal.fxpplus;

public interface IFXPPlusFactory {

	public ITerm label(String A, ITerm F);

	public ITerm and(ITerm F1, ITerm F2);

	public ITerm not(ITerm F);

	public ITerm ch(ITerm F);

	public ITerm chithElement(int i, ITerm F);

	public ITerm chLastElement(ITerm F);

	public ITerm innermostChStart(String a, ITerm F);

	public ITerm outermostChStart(String a, ITerm F);

	public IAccumulator chStarLocal(int n, ILocal L);

	public ITerm chStarLocal(ILocal L);

	public IAccumulator ch(int n, ILocal L);

	public ITerm chStar(ITerm F);

	// th-n-x(F, F')
	public ITerm stringMatches(IAutomata A);

	public ILocal label(String A);

	public ILocal namespace(String H);

	public ILocal localAnd(ILocal L1, ILocal L2);

	public ILocal localNot(ILocal L);

	public ILocal attribute(IText T);

	public ILocal attribute(String a, IText T);

	public ILocal text();

	public ILocal PI();

	public ILocal comment();

	public ILocal element();

	public IText variable(String x);

	public IText trueQuery();

	public IText localStringMatches(IAutomata A);

	public IText textAnd(IText T1, IText T2);

	public IText textNot(IText T);

	public IAutomata equals(String d); // TODO equals is a special method is
										// Java

	public IAutomata contains(String d);

	public IAutomata startWith(String d);

	public IAutomata endWith(String d);

	// dfa(...)
	// reg-ex(...)

}
