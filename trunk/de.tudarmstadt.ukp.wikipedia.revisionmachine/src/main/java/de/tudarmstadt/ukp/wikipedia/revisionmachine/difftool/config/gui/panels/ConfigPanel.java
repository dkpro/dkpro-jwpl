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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTabbedPane;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigController;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigSettings;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigVerification;

/**
 * Panel of the ConfigGUI Contains a tabbed panel with reference to all the
 * other panels.
 *
 *
 *
 */
@SuppressWarnings("serial")
public class ConfigPanel
	extends AbstractPanel
{

	private JTabbedPane tabs;

	private JButton importButton;
	private JButton verifyButton;
	private JButton exportButton;

	/**
	 * (Constructor) Creates a new ConfigPanel.
	 *
	 * @param controller
	 *            Reference to the controller
	 */
	public ConfigPanel(final ConfigController controller)
	{

		super(controller);

		createTabbedPane();

		createImportButton();
		createVerifyButton();
		createExportButton();
	}

	// --------------------------------------------------------------------------//
	// CONSTRUCTION METHODS //
	// --------------------------------------------------------------------------//

	private void createTabbedPane()
	{

		tabs = new JTabbedPane();
		tabs.setBounds(5, 5, 580, 300);

		tabs.add("Mode", new ModePanel(controller));
		tabs.add("Externals", new ExternalProgramsPanel(controller));
		tabs.add("Input", new InputPanel(controller));
		tabs.add("Output", new OutputPanel(controller));
		tabs.add("Database", new SQLPanel(controller));
		tabs.add("Cache", new CachePanel(controller));
		tabs.add("Logging", new LoggingPanel(controller));
		tabs.add("Debug", new DebugPanel(controller));
		tabs.add("Filter", new FilterPanel(controller));

		this.add(tabs);

	}

	private void createImportButton()
	{

		importButton = new JButton("Import");
		importButton.setBounds(5, 310, 190, 25);

		importButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				controller.loadConfiguration();
				repaint();
			}
		});

		this.add(importButton);

	}

	private void createVerifyButton()
	{

		verifyButton = new JButton("Verify Settings");
		verifyButton.setBounds(200, 310, 190, 25);

		verifyButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				controller.createConfigurationXML();
				repaint();
			}
		});

		this.add(verifyButton);
	}

	private void createExportButton()
	{

		exportButton = new JButton("Export");
		exportButton.setBounds(395, 310, 190, 25);

		exportButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				controller.saveConfiguration();
				repaint();
			}
		});

		this.add(exportButton);
	}

	// --------------------------------------------------------------------------//
	// VALIDATION METHODS //
	// --------------------------------------------------------------------------//

	/**
	 * empty method
	 */
	@Override
	public void validate()
	{

	}

	/**
	 * A call of this method should validate the positions of the panels
	 * components.
	 */
	@Override
	public void relocate()
	{

		int w = 575, h = 330;

		int x = (this.getWidth() - w) / 2;
		int y = (this.getHeight() - h) / 2;

		tabs.setLocation(x, y);

		importButton.setLocation(x, y + 305);
		verifyButton.setLocation(x + 195, y + 305);
		exportButton.setLocation(x + 390, y + 305);

	}

	// --------------------------------------------------------------------------//
	// INPUT/OUTPUT METHODS //
	// --------------------------------------------------------------------------//

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public void applyConfig(final ConfigSettings config)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public void toXML(final StringBuilder builder,
			final ConfigVerification errors)
	{
		throw new UnsupportedOperationException();
	}
}
