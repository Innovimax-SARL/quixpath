package com.quixpath.internal.fxpplus;

import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;

/**
 * According to FXP Grammar, IAutomata is NOT an ITerm.
 * 
 */
public interface IAutomata {

	public IFXPTerm expand();

	public boolean isLocal();

	public boolean isText();

	public boolean isAutomata();

}
