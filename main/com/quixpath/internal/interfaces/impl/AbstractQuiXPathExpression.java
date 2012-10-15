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
package com.quixpath.internal.interfaces.impl;

import fr.inria.lille.fxp.querylanguage.api.IProjectionProperties;
import innovimax.quixproc.datamodel.IStream;
import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import innovimax.quixproc.datamodel.QuixValue;
import net.sf.saxon.s9api.Processor;

import com.quixpath.exceptions.QuiXPathException;
import com.quixpath.internal.interfaces.IInternalQuiXPathExpression;

/**
 * AbstractQuiXPathExpression contains tools to compile and evaluate
 * QuiXPathExpression.
 * 
 */
public abstract class AbstractQuiXPathExpression implements
		IInternalQuiXPathExpression {

	@Override
	public abstract IStream<MatchEvent> update(QuixEvent event)
			throws QuiXPathException;

    // As far as possible, an application should instantiate a single Processor.
    private static Processor processor;
    // Moz : Added to work with QuiXProc
    public static void setProcessor(Processor _processor) { 
    	processor = _processor; 
    	}

    /**
     * 
     * @return the unique instance of the SAXON processor.
     */
    public static Processor processor() {
        if (processor == null) {
            processor = new Processor(false);
        }
        return processor;
    }


	public QuixValue evaluate(IStream<QuixEvent> stream)
			throws QuiXPathException {
		throw new UnsupportedOperationException();
	}

	public AbstractQuiXPathExpression(IProjectionProperties projectionProperties) {
		this(projectionProperties.keepAttribute(), projectionProperties
				.keepText(), projectionProperties.keepPI(),
				projectionProperties.keepComment(), projectionProperties
						.isDepthBounded() ? projectionProperties
						.getDepthBound() : Integer.MAX_VALUE);
	}

	public AbstractQuiXPathExpression() {
		this(true, true, true, true, Integer.MAX_VALUE);
	}

	private AbstractQuiXPathExpression(boolean keepAttribute, boolean keepText,
			boolean keepPI, boolean keepComment, int depthBound) {
		super();
		this.keepAttribute = keepAttribute;
		this.keepText = keepText;
		this.keepPI = keepPI;
		this.keepComment = keepComment;
		this.depthBound = depthBound;
	}

	protected final boolean keepAttribute;
	protected final boolean keepText;
	protected final boolean keepPI;
	protected final boolean keepComment;
	protected final int depthBound;


}
