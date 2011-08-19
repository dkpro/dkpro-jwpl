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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.simpleconfig;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigController;

/**
 * This class is an alternative to the ConfigGUI and can be used to produce
 * configuration files for the DiffTool.
 *
 * @author Oliver Ferschke
 */
public class SimpleConfig
{
	/** Reference to the ConfigController */
	private final ConfigController controller;

	/**
	 * (Constructor) Creates a new ConfigGUI object.
	 */
	public SimpleConfig()
	{
		this.controller = new ConfigController();
		controller.defaultConfiguration();
		//TODO nothing here yet...
	}
}
