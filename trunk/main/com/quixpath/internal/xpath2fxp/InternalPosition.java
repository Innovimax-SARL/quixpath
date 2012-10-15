package com.quixpath.internal.xpath2fxp;

public class InternalPosition {

	boolean isChildStartPattern = false;
	int litteral = -1;

	boolean isPosition() {
		return isChildStartPattern && (litteral != -1);
	}

}
