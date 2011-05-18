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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigController;

/**
 * MenuBar of the ConfigurationTool
 *
 *
 *
 */
@SuppressWarnings("serial")
public class ConfigMenuBar
	extends JMenuBar
{

	/** Reference to the controller */
	private final ConfigController controller;

	/**
	 * (Constructor) Create the ConfigMenuBar object.
	 *
	 * @param controller
	 *            reference to the controller
	 */
	public ConfigMenuBar(final ConfigController controller)
	{

		this.controller = controller;

		createSystemMenu();
	}

	/**
	 * Creates the System menu and its menu items.
	 */
	private void createSystemMenu()
	{

		JMenu system = new JMenu("System");

		JMenuItem importConfig = new JMenuItem("Import Configuration");
		importConfig.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				controller.loadConfiguration();
			}
		});

		system.add(importConfig);

		JMenuItem exportConfig = new JMenuItem("Export Configuration");
		exportConfig.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				controller.saveConfiguration();
			}
		});

		system.add(exportConfig);

		system.addSeparator();

		JMenuItem defaultConfig = new JMenuItem(
				"Reset to default parameters");
		defaultConfig.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				controller.defaultConfiguration();
			}
		});

		system.add(defaultConfig);

		system.addSeparator();

		JMenuItem systemClose = new JMenuItem("Close");
		systemClose.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				System.exit(-1);
			}
		});

		system.add(systemClose);

		this.add(system);
	}
}
