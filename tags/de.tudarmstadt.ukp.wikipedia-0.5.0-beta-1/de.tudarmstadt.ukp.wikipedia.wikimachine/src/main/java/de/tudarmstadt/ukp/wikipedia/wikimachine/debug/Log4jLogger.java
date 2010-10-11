/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.wikimachine.debug;

import org.apache.log4j.Logger;

public class Log4jLogger extends AbstractLogger {

	private static final Logger log4j = Logger.getLogger(Log4jLogger.class);

	@Override
	public void logObject(Object message) {
		log4j.info(message);
	}
}
