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
package com.quixpath.internal.interfaces.impl;

import innovimax.quixproc.datamodel.IStream;
import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import innovimax.quixproc.datamodel.QuixValue;

import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.s9api.XPathExecutable;

import com.quixpath.exceptions.QuiXPathException;
import com.quixpath.exceptions.UnsupportedQueryException;
import com.quixpath.interfaces.IQuiXPath;
import com.quixpath.interfaces.IQuiXPathExpression;
import com.quixpath.interfaces.context.IStaticContext;
import com.quixpath.internal.xpath2fxp.XPath2FXP;

import fr.inria.lille.fxp.datamodel.api.Pools;
import fr.inria.lille.fxp.queryengine.api.Algorithm;
import fr.inria.lille.fxp.queryengine.api.EnumerationHandlerFactory;
import fr.inria.lille.fxp.queryengine.api.IEnumerationHandler;
import fr.inria.lille.fxp.querylanguage.api.IFXPTerm;
import fr.inria.lille.fxp.querylanguage.api.exception.DSTABuildingException;

public class QuiXPathImpl implements IQuiXPath {

	@Override
	public IQuiXPathExpression compile(final String xpathQuery,
			final IStaticContext staticContext, boolean canUseTree)
			throws UnsupportedQueryException {
		XPath2FXP path2fxp = null;

		try {
			path2fxp = new XPath2FXP(xpathQuery, new Pools(), staticContext);
		} catch (XPathExpressionException e) {
			throw new UnsupportedQueryException(e);
		}

		try {
			IFXPTerm term = path2fxp.toFXP();
			final IEnumerationHandler handler = EnumerationHandlerFactory
					.newInstance().make(Algorithm.EARLY_ALGORITHM, term);

			if (path2fxp.isCountintAtTopLevel()) {
				return new FXPCountAtTopLevelExpression(term, handler);
			} else if (path2fxp.isCounting()) {
				return new FXPCountExpression(term, handler,
						path2fxp.getOperator());
			} else {

				if (term.getStaticProperties().is0Delay()
						&& term.getStaticProperties().isMonadic()) {
					return new FXPMonadic0DelayExpression(term, handler);
				} else {
					return new FXPExpression(term, handler);
				}
			}
		} catch (DSTABuildingException e) {
			throw new UnsupportedQueryException(e);
		} catch (Exception e) {
          if (canUseTree) {	
		  //	    System.out.println("xPathQuery "+xpathQuery+" : " + e);
			if (path2fxp != null) {
				final XPathExecutable exp = path2fxp.getXPathExecutable();
				if (exp != null) {
					return AbstractSAXONExpression.make(exp);
				}
			}
          }
			throw new UnsupportedQueryException(e); 
		}

	}

	@Override
	public IStream<MatchEvent> update(IQuiXPathExpression expression,
			QuixEvent event) throws QuiXPathException {
	  return expression.update(event);
	}

	@Override
	public QuixValue evaluate(IQuiXPathExpression expression,
			IStream<QuixEvent> stream) throws QuiXPathException {
		return expression.evaluate(stream);
	}

	@Override
	public IQuiXPathExpression compile(String xpathQuery,
			IStaticContext staticContext) throws UnsupportedQueryException {
		return compile(xpathQuery, staticContext, true);
	}

}
