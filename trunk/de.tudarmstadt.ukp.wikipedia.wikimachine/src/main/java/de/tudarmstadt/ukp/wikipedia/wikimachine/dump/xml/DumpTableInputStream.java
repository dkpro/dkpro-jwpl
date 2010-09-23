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
package de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml;

import java.io.IOException;
import java.io.InputStream;

public abstract class DumpTableInputStream extends InputStream {
	public abstract void initialize(InputStream inputStream, DumpTableEnum table)
			throws IOException;
}
