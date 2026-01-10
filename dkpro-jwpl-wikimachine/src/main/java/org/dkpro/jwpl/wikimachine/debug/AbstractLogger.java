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
package org.dkpro.jwpl.wikimachine.debug;

/**
 * Logger, which does not implement some concrete output technique, but knows how exceptions are
 * handled. AbstractLogger provides a template method {@link #log(Object)} for its derivatives.
 *
 * @see ILogger
 */
public abstract class AbstractLogger
    implements ILogger
{

    /**
     * Checks whether a {@link Class} is throwable or not.
     * @param c The class to investigate.
     * @return {@code true} if {@code c} is throwable, {@code false} otherwise.
     */
    protected boolean isThrowable(Class<?> c)
    {
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

    /**
     * Instantiates a logger-friendly message from a {@link Throwable} object.
     *
     * @param e The throwable to create the log message from.
     *
     * @return The resulting, pretty-formatted message extracted from {@code e}.
     */
    protected String createThrowableMessage(Throwable e)
    {
        StringBuilder message = new StringBuilder();
        message.append(e.getMessage());
        message.append('\n');
        for (StackTraceElement currentTrace : e.getStackTrace()) {
            message.append('\n');
            message.append(currentTrace);
        }
        return message.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(Object message)
    {
        if (isThrowable(message.getClass())) {
            logObject(createThrowableMessage((Throwable) message));
        }
        else {
            logObject(message);
        }
    }

    /**
     * Logs the current object reference or state of a specified {@link Object object}.
     *
     * @param message The object to use and convert to a log message.
     */
    protected abstract void logObject(Object message);

}
