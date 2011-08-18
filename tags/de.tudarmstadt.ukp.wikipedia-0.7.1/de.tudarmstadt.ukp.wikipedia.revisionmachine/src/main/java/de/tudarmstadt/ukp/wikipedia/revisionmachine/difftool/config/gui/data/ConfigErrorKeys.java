/*******************************************************************************
 * Copyright (c) 2011 Ubiquitous Knowledge Processing Lab
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Project Website:
 * 	http://jwpl.googlecode.com
 * 
 * Contributors:
 * 	Torsten Zesch
 * 	Simon Kulessa
 * 	Oliver Ferschke
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data;

/**
 * Contains the keys for the configuration verification error types.
 * 
 * 
 * 
 */
public enum ConfigErrorKeys
{

	/** Mode was enabled, but no value was set */
	COMMAND_NOT_SET,

	/** Configuration value out of range */
	VALUE_OUT_OF_RANGE,

	/** Path was not set */
	PATH_NOT_SET,

	/** Illegal configuration value */
	ILLEGAL_INPUT,

	/** Illegal input file type */
	ILLEGAL_INPUT_FILE,

	/** Required value is missing */
	MISSING_VALUE
}
