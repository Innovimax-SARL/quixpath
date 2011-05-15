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

import innovimax.quixproc.datamodel.MatchEvent;
import innovimax.quixproc.datamodel.QuixEvent;
import innovimax.quixproc.datamodel.Stream;

import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.s9api.XPathExecutable;

import com.quixpath.exceptions.QuiXPathException;
import com.quixpath.exceptions.UnsupportedQueryException;
import com.quixpath.interfaces.IQuiXPath;
import com.quixpath.interfaces.IQuiXPathExpression;
import com.quixpath.internal.xpath2fxp.XPath2FXP;

import fr.inria.mostrare.evoxs.processor.EnumerationHandler;
import fr.inria.mostrare.evoxs.pub.handlerFactory.Algorithm;
import fr.inria.mostrare.evoxs.pub.handlerFactory.EnumerationHandlerFactory;
import fr.inria.mostrare.xpath.exception.DSTABuildingException;
import fr.inria.mostrare.xpath.pub.IFXPTerm;

public class QuiXPathImpl implements IQuiXPath {

	@Override
	public IQuiXPathExpression compile(final String xpathQuery)
			throws UnsupportedQueryException {
		XPath2FXP path2fxp = null;

		try {
			path2fxp = new XPath2FXP(xpathQuery);
		} catch (XPathExpressionException e) {
			throw new UnsupportedQueryException(e);
		}

		try {
			IFXPTerm term = path2fxp.toFXP();
			final EnumerationHandler handler = EnumerationHandlerFactory
					.newInstance().make(Algorithm.EARLY_ALGORITHM, term);

			return new CompatibleWithFXPExpression(term, handler);
		} catch (DSTABuildingException e) {
			throw new UnsupportedQueryException(e);
		} catch (Exception e) {
			if (path2fxp != null) {
				final XPathExecutable exp = path2fxp.getXPathExecutable();
				if (exp != null) {
					return new IncompatibleWithFXPExpression(exp);
				}
			}
			throw new UnsupportedQueryException(e);
		}

	}

	@Override
	public Stream<MatchEvent> update(IQuiXPathExpression expression,
			QuixEvent event) throws QuiXPathException {
		return expression.update(event);
	}

}
