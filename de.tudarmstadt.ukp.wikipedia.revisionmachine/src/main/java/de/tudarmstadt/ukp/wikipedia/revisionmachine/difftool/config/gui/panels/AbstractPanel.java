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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.panels;

import java.awt.Graphics;

import javax.swing.JPanel;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigController;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigSettings;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigVerification;

/**
 * AbstractPanel Super panel class of the KonfigurationTool
 *
 * All panels (which contain configuration parameters) will inherit from this
 * class.
 *
 *
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractPanel
	extends JPanel
{

	/** Reference to the controller */
	protected ConfigController controller;

	/**
	 * (Constructor) Creates an AbstractPanel object.
	 *
	 * @param controller
	 *            Reference to the controller
	 */
	public AbstractPanel(final ConfigController controller)
	{
		this.controller = controller;
		this.setLayout(null);
	}

	/**
	 * A call of this method should validate the status of the panels
	 * components.
	 */
	@Override
	public abstract void validate();

	/**
	 * A call of this method should validate the positions of the panels
	 * components.
	 */
	public abstract void relocate();

	/**
	 * The default paint method was expanded with calls of the validate() and
	 * relocate() methods.
	 *
	 * @param g
	 *            Graphics
	 */
	@Override
	public void paint(final Graphics g)
	{

		validate();
		relocate();

		super.paint(g);
	}

	/**
	 * Adds the xml description of the panels content to the StringBuilder.
	 * Errors which occur during the xml transformation will be added to the
	 * ConfigVerification.
	 *
	 * @param builder
	 *            Reference to a StringBuilder object
	 * @param errors
	 *            Reference to the ConfigVerification object
	 */
	public abstract void toXML(final StringBuilder builder,
			final ConfigVerification errors);

	/**
	 * Reads the configuration parameters described in the panel from the
	 * ConfigSettings and and sets the contained values.
	 *
	 * @param config
	 *            Reference to the ConfigSettings object
	 */
	public abstract void applyConfig(final ConfigSettings config);
}
