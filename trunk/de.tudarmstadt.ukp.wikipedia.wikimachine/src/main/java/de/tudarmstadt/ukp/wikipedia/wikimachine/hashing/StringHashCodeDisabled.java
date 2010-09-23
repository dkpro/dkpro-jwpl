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
package de.tudarmstadt.ukp.wikipedia.wikimachine.hashing;

public class StringHashCodeDisabled implements IStringHashCode {

	public StringHashCodeDisabled() {
		// use for instantiate as generic
	}

	@Override
	public String hashCode(String string) {
		return string;
	}

}
