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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigController;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.panels.ConfigPanel;

/**
 * This class represents the main class of the graphical configuration tool
 * for the DiffTool.
 * <br/>
 * <p>
 * The GUI can be used to set all necessary configuration parameters for the
 * DiffTool. However, it currently does not verify the validity of the
 * combination of the settings.
 * It only checks whether the individual setting contain valid values.
 * Consequently, it is possible to produce configurations that won't
 * work.</p>
 * <br/>
 * Example:<br/>
 * If the output mode is set to <i>bzip2</i>, it is currently not possible
 * to split the output into several files. However, the ConfigGUI allows for
 * this setting.
 *
 *
 *
 *
 *
 */
public class ConfigGUI
	extends JFrame
{

	private static final long serialVersionUID = 1L;

	/** Reference to the ConfigController */
	private final ConfigController controller;

	/**
	 * (Constructor) Creates a new ConfigGUI object.
	 */
	public ConfigGUI()
	{

		this.controller = new ConfigController();

		this.setTitle("RevisionMachine DiffTool - Configuration");

		setSize(600, 400);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((d.width - getSize().width) / 2,
				(d.height - getSize().height) / 2);

		this.setJMenuBar(new ConfigMenuBar(controller));
		this.setContentPane(new ConfigPanel(controller));

		//load default parameters
		this.controller.defaultConfiguration();
	}

	/**
	 * ConfigurationTool - Main Method
	 *
	 * Starts the ConfigurationTool GUI
	 *
	 * @param args
	 *            program arguments (not used)
	 */
	public static void main(final String[] args)
	{
		new ConfigGUI().setVisible(true);
	}

}
