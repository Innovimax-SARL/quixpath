/*
QuiXPath: efficient evaluation of XPath queries on XML streams.
Copyright (C) 2009-2012 Innovimax and INRIA

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.quixpath.internal.fxpplus;

public interface IFXPPlusFactory {

	public ITerm label(String uri, String A, ITerm F);

	public ITerm and(ITerm F1, ITerm F2);

	public ITerm or(ITerm F1, ITerm F2);

	public ITerm not(ITerm F);

	public ITerm ch(ITerm F);

	public ITerm chithElement(long i, ITerm F);

	public ITerm chLastElement(ITerm F);

	public ITerm innermostChStart(String a, ITerm F);

	public ITerm outermostChStart(String a, ITerm F);

	public IAccumulator chStarLocal(int n, ILocal L);

	public ITerm chStarLocal(ILocal L);

	public IAccumulator ch(int n, ILocal L);

	public ITerm chStar(ITerm F);

	// th-n-x(F, F')
	public ITerm stringMatches(IAutomata A);

	public ILocal label(String uri, String A);

	public ILocal namespace(String H);

	public ILocal localAnd(ILocal L1, ILocal L2);

	public ILocal localNot(ILocal L);

	/**
	 * 
	 * @param uri
	 *            uri == null iff there is no test on the uri (!= default value)
	 * @param T
	 * @return
	 */
	public ILocal attribute(String uri, IText T);

	public ILocal attribute(String a, String uri, IText T);

	public ILocal text();

	public ILocal PI(ILocal label);

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
