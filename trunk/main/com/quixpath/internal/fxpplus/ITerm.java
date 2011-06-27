package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

public interface ITerm {

	public IFXPTerm expand();

	public boolean isLocal();

	public boolean isText();

	public boolean isAutomata();

	public boolean isAcumulator();

	/**
	 * true -> the iterm is typed by element <br/>
	 * false -> we do not know but may be it is an element.
	 */
	public boolean isElement();

	public boolean isOuterMost();
}
