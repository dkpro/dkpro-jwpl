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

/**
 * Logger, which does not implement some concrete output technique, but knows
 * how exceptions are represented like. AbstractLogger provides a template
 * method {@link #logObject(Object)} for its derivatives.
 * 
 * @author ivan.galkin
 * 
 */
public abstract class AbstractLogger implements ILogger {

	protected boolean isThrowable(Class<?> c) {
		boolean throwable = false;
		if (c != null) {
			throwable = c.equals(Throwable.class);
			if (!throwable) {
				for (Class<?> i : c.getInterfaces()) {
					if (throwable |= isThrowable(i)) {
						break;
					}
				}
				if (!throwable) {
					throwable |= isThrowable(c.getSuperclass());
				}
			}
		}
		return throwable;
	}

	protected String createThrowableMessage(Throwable e) {
		StringBuffer message = new StringBuffer();
		message.append(e.getMessage());
		message.append('\n');
		for (StackTraceElement currentTrace : e.getStackTrace()) {
			message.append('\n');
			message.append(currentTrace);
		}
		return message.toString();
	}

	@Override
	public void log(Object message) {
		if (isThrowable(message.getClass())) {
			logObject(createThrowableMessage((Throwable) message));
		} else {
			logObject(message);
		}
	}

	protected abstract void logObject(Object message);

}
