package com.quixpath.internal.fxpplus;

public class FXPPlusFactory implements IFXPPlusFactory {

	public static IFXPPlusFactory newInstance() {
		return new FXPPlusFactory();
	}

	@Override
	public ITerm label(String A, ITerm F) {
		if (F.isLocal()) {
			return localAnd(new LocalLabel(A), (ILocal) F);
		}
		return new Label(A, F);
	}

	@Override
	public ITerm and(ITerm F1, ITerm F2) {
		if (F1 instanceof TrueQuery) {
			return F2;
		}
		if (F2 instanceof TrueQuery) {
			return F1;
		}
		if (F1.isText() && F2.isText()) {
			return textAnd((IText) F1, (IText) F2);
		}
		if (F1.isLocal() && F2.isLocal()) {
			return localAnd((ILocal) F1, (ILocal) F2);
		}
		return new And(F1, F2);
	}

	@Override
	public ITerm not(ITerm F) {
		return new Not(F);
	}

	@Override
	public ITerm ch(ITerm F) {
		return new Ch(F);
	}

	@Override
	public ITerm chithElement(int i, ITerm F) {
		return new ChithElement(i, F);
	}

	@Override
	public ITerm chLastElement(ITerm F) {
		return new ChLastElement(F);
	}

	@Override
	public ITerm innermostChStart(String a, ITerm F) {
		return new InnermostChStart(a, F);
	}

	@Override
	public ITerm outermostChStart(String a, ITerm F) {
		return new OuttermostChStart(a, F);
	}

	@Override
	public IAccumulator chStarLocal(int n, ILocal L) {
		return new ChStartLocalAcc(n, L);
	}

	@Override
	public IAccumulator ch(int n, ILocal L) {
		return new ChAcc(n, L);
	}

	@Override
	public ITerm chStar(ITerm F) {
		return new ChStar(F);
	}

	@Override
	public ITerm stringMatches(IAutomata A) {
		return new StringMatches(A);
	}

	@Override
	public ILocal label(String A) {
		return new LocalLabel(A);
	}

	@Override
	public ILocal localAnd(ILocal L1, ILocal L2) {
		if (L1 instanceof TrueQuery)
			return L2;
		if (L2 instanceof TrueQuery)
			return L1;
		if (L1.isText() && L2.isText()) {
			return textAnd((IText) L1, (IText) L2);
		}
		return new LocalAnd(L1, L2);
	}

	@Override
	public ILocal localNot(ILocal L) {
		return new LocalNot(L);
	}

	@Override
	public ILocal attribute(IText T) {
		return new Attribute(T);
	}

	@Override
	public ILocal attribute(String a, IText T) {
		return new Attribute(a, T);
	}

	@Override
	public ILocal text() {
		return new Text();
	}

	@Override
	public ILocal PI() {
		return new PI();
	}

	@Override
	public ILocal comment() {
		return new Comment();
	}

	@Override
	public ILocal element() {
		return new Element();
	}

	@Override
	public IText variable(String x) {
		return new Variable(x);
	}

	@Override
	public IText trueQuery() {
		return new TrueQuery();
	}

	@Override
	public IText localStringMatches(IAutomata A) {
		return new LocalStringMatches(A);
	}

	@Override
	public IText textAnd(IText T1, IText T2) {
		return new TextAnd(T1, T2);
	}

	@Override
	public IText textNot(IText T) {
		return new TextNot(T);
	}

	@Override
	public IAutomata equals(String d) {
		return new Equals(d);
	}

	@Override
	public IAutomata contains(String d) {
		return new Contains(d);
	}

	@Override
	public IAutomata startWith(String d) {
		return new StartWith(d);
	}

	@Override
	public IAutomata endWith(String d) {
		return new EndWith(d);
	}

	@Override
	public ITerm chStarLocal(ILocal L) {
		return new ChStartLocal(L);
	}

	@Override
	public ILocal namespace(String H) {
		return new NameSpace(H);
	}

}
