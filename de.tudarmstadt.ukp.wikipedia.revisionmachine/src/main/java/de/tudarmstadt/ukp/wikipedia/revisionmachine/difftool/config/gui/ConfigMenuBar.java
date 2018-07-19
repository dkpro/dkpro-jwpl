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
