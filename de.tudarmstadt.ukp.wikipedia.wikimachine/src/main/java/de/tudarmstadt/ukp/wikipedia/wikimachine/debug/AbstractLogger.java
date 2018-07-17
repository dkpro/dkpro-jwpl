/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.wikipedia.wikimachine.debug;

/**
 * Logger, which does not implement some concrete output technique, but knows
 * how exceptions are represented like. AbstractLogger provides a template
 * method {@link #log(Object)} for its derivatives.
 *
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
