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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigController;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigSettings;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigVerification;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.ConfigErrorKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.ConfigItem;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.ConfigItemTypes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.PanelKeys;

/**
 * Panel class of the ConfigurationTool
 *
 * This panel contains all components for setting configuration parameters
 * related to the use of external programs.
 *
 *
 *
 */
@SuppressWarnings("serial")
public class ExternalProgramsPanel
	extends AbstractPanel
{

	private JLabel executablePathLabel;

	private JCheckBox sevenZipEnableBox;
	private JLabel sevenZipLabel;
	private JTextField sevenZipPathField;
	private JButton sevenZipSearchButton;

	/**
	 * (Constructor) Creates a new ExternalProgramPanel.
	 *
	 * @param controller
	 *            Reference to the controller
	 */
	public ExternalProgramsPanel(final ConfigController controller)
	{

		super(controller);
		controller.register(PanelKeys.PANEL_EXTERNALS, this);

		createExecutableSettings();
	}

	// --------------------------------------------------------------------------//
	// CONSTRUCTION METHODS //
	// --------------------------------------------------------------------------//

	private void createExecutableSettings()
	{

		executablePathLabel = new JLabel("Path to executables: ");
		executablePathLabel.setBounds(10, 10, 250, 25);
		this.add(executablePathLabel);

		// ------------------------------------------------------------------//
		// 7ZIP / P7ZIP SETTINGS //
		// ------------------------------------------------------------------//

		sevenZipEnableBox = new JCheckBox();
		sevenZipEnableBox.setBounds(10, 45, 25, 25);

		this.add(sevenZipEnableBox);

		sevenZipLabel = new JLabel("7Zip Executable: ");
		sevenZipLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		sevenZipLabel.setBounds(40, 45, 120, 25);
		this.add(sevenZipLabel);

		sevenZipPathField = new JTextField();
		sevenZipPathField.setBounds(170, 45, 300, 25);
		this.add(sevenZipPathField);

		sevenZipSearchButton = new JButton("Search");
		sevenZipSearchButton.setBounds(480, 45, 80, 25);

		sevenZipSearchButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{

				JFileChooser fc = new JFileChooser();
				if (fc.showOpenDialog(new JPanel()) == JFileChooser.APPROVE_OPTION) {
					sevenZipPathField.setText(fc.getSelectedFile().getPath());
				}
			}
		});

		this.add(sevenZipSearchButton);

		sevenZipEnableBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				boolean flag = !controller.is7ZipEnabled();
				controller.setEnable7Zip(flag);

				sevenZipLabel.setEnabled(flag);
				sevenZipPathField.setEnabled(flag);
				sevenZipSearchButton.setEnabled(flag);
			}
		});

	}

	// --------------------------------------------------------------------------//
	// VALIDATION METHODS //
	// --------------------------------------------------------------------------//

	/**
	 * A call of this method should validate the status of the panels
	 * components.
	 */
	@Override
	public void validate()
	{
		validate7ZipSettings();
	}

	/**
	 * Validates the 7Zip settings
	 */
	private void validate7ZipSettings()
	{
		boolean flag = controller.is7ZipEnabled();

		sevenZipEnableBox.setSelected(flag);
		sevenZipLabel.setEnabled(flag);
		sevenZipPathField.setEnabled(flag);
		sevenZipSearchButton.setEnabled(flag);
	}


	/**
	 * A call of this method should validate the positions of the panels
	 * components.
	 */
	@Override
	public void relocate()
	{

		int w = 550, h = 210;

		int x = (this.getWidth() - w) / 2;
		int y = (this.getHeight() - h) / 2;

		// 10, 10 <-> 580, 185
		executablePathLabel.setLocation(x, y);

		sevenZipEnableBox.setLocation(x, y + 35);
		sevenZipLabel.setLocation(x + 30, y + 35);
		sevenZipPathField.setLocation(x + 160, y + 35);
		sevenZipSearchButton.setLocation(x + 470, y + 35);

	}

	// --------------------------------------------------------------------------//
	// INPUT/OUTPUT METHODS //
	// --------------------------------------------------------------------------//

	/**
	 * Reads the configuration parameters described in the panel from the
	 * ConfigSettings and and sets the contained values.
	 *
	 * @param config
	 *            Reference to the ConfigSettings object
	 */
	@Override
	public void applyConfig(final ConfigSettings config)
	{
		Object o = config
				.getConfigParameter(ConfigurationKeys.PATH_PROGRAM_7ZIP);
		if (o != null) {
			controller.setEnable7Zip(true);
			sevenZipPathField.setText((String) o);
		}
		else {
			controller.setEnable7Zip(false);
			sevenZipPathField.setText("");
		}

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
	@Override
	public void toXML(final StringBuilder builder,
			final ConfigVerification errors)
	{

		boolean sevenzip = controller.is7ZipEnabled();

		if (sevenzip) {

			String cmd;
			builder.append("\t<externals>\r\n");

			if (sevenzip) {
				cmd = sevenZipPathField.getText();
				if (cmd.length() == 0) {
					errors.add(new ConfigItem(ConfigItemTypes.ERROR,
							ConfigErrorKeys.PATH_NOT_SET,
							"The path to the 7Zip executable" + " is missing."));
				}

				builder.append("\t\t<sevenzip>\"" + cmd + "\"</sevenzip>\r\n");
			}

			builder.append("\t</externals>\r\n");
		}
	}
}
