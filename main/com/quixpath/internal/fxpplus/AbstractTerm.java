/*
QuiXPath: efficient evaluation of XPath queries on XML streams.
Copyright (C) 2011 Innovimax and INRIA

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
		return this instanceof OutermostChStar;
	}
	
	// TODO remove all those instanceof and use OO Programming.
	public boolean isVariable(){
		return this instanceof Variable;
	}

	public boolean isElement() {
		return false;
	}

	public boolean isChildStarPattern() {
		if (this instanceof ChStarLocalAcc) {
			return ((ChStarLocalAcc) this).getN() == 0;
		}
		return this instanceof ChStar || this instanceof ChStarLocal;
	}

	public ITerm childStarPatternOperand() {
		assert !isChildStarPattern();
		throw new UnsupportedOperationException();
	}

	public boolean containsChildStar() {
		if (isLocal()) {
			return false;
		}
		// Subclass must override this method.
		return true;
	}

	public boolean isChildElementPattern() {
		return false;
	}

}
