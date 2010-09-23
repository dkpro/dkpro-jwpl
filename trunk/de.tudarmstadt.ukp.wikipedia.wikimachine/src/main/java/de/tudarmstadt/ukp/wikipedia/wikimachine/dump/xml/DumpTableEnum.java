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

/**
 * Three possible tables, which can be parsed from the Wikimedia-Dump
 * <ul>
 * <li>PAGE</li>
 * <li>REVISION</li>
 * <li>TEXT</li>
 * </ul>
 * 
 * @author ivan.galkin
 * 
 */
public enum DumpTableEnum {
	PAGE, REVISION, TEXT
}
