/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.wikimachine.hashing;

public class StringHashCodeJBoss implements IStringHashCode {

	public StringHashCodeJBoss() {
		// use for instantiate as generic
	}

	@Override
	public Long hashCode(String string) {
		return HashStringUtil.hashName(string);
	}

}
