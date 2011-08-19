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
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
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
 * related to the debug purposes.
 * 
 * 
 * 
 */
@SuppressWarnings("serial")
public class DebugPanel
	extends AbstractPanel
{

	private JCheckBox verifyDiffCheckBox;
	private JCheckBox verifyEncodingCheckBox;

	private JCheckBox debugOuputCheckBox;
	private JLabel debugOutputLabel;
	private JTextField debugOutputField;

	private JCheckBox statsOutputCheckBox;

	/**
	 * (Constructor) Creates a new DebugPanel.
	 * 
	 * @param controller
	 *            Reference to the controller
	 */
	public DebugPanel(final ConfigController controller)
	{

		super(controller);
		controller.register(PanelKeys.PANEL_DEBUG, this);

		createVerificationSettings();
		createStatsOutputSettings();
		createDebugSettings();
	}

	public void createVerificationSettings()
	{

		verifyDiffCheckBox = new JCheckBox("Activate Diff Verification");
		verifyDiffCheckBox.setBounds(10, 10, 200, 25);

		verifyDiffCheckBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{

				boolean flag = !controller.isDiffVerificationEnabled();
				controller.setEnableDiffVerification(flag);

				validateDebugSettings();
			}
		});

		this.add(verifyDiffCheckBox);

		verifyEncodingCheckBox = new JCheckBox("Activate Encoding Verification");
		verifyEncodingCheckBox.setBounds(10, 40, 200, 25);

		verifyEncodingCheckBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{

				boolean flag = !controller.isEncodingVerificationEnabled();
				controller.setEnableEncodingVerification(flag);

				validateDebugSettings();
			}
		});

		this.add(verifyEncodingCheckBox);
	}

	private void createStatsOutputSettings()
	{
		statsOutputCheckBox = new JCheckBox(
				"Activate Article Information Output");
		statsOutputCheckBox.setBounds(10, 80, 250, 25);

		statsOutputCheckBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{

				boolean flag = !controller.isStatsOutputEnabled();
				controller.setEnableStatsOutput(flag);
			}
		});

		this.add(statsOutputCheckBox);
	}

	private void createDebugSettings()
	{

		debugOuputCheckBox = new JCheckBox("Activate Debug Output");
		debugOuputCheckBox.setBounds(10, 120, 200, 25);
		this.add(debugOuputCheckBox);

		debugOuputCheckBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{

				boolean flag = !controller.isDebugOutputEnabled();
				controller.setEnableDebugOutput(flag);

				validateDebugSettings();
			}
		});

		debugOutputLabel = new JLabel("Debug Folder: ");
		debugOutputLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		debugOutputLabel.setBounds(10, 150, 100, 25);
		this.add(debugOutputLabel);

		debugOutputField = new JTextField();
		debugOutputField.setBounds(120, 150, 250, 25);
		this.add(debugOutputField);
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
		validateDebugSettings();
	}

	/**
	 * Validates the debug settings.
	 */
	private void validateDebugSettings()
	{

		verifyDiffCheckBox.setSelected(controller.isDiffVerificationEnabled());
		verifyEncodingCheckBox.setSelected(controller
				.isEncodingVerificationEnabled());
		statsOutputCheckBox.setSelected(controller.isStatsOutputEnabled());

		boolean flagA = controller.isDiffVerificationEnabled()
				|| controller.isEncodingVerificationEnabled();

		debugOuputCheckBox.setEnabled(flagA);
		debugOuputCheckBox.setSelected(controller.isDebugOutputEnabled());

		boolean flagB = controller.isDebugOutputEnabled();
		debugOutputLabel.setEnabled(flagA && flagB);
		debugOutputField.setEnabled(flagA && flagB);

	}

	/**
	 * A call of this method should validate the positions of the panels
	 * components.
	 */
	@Override
	public void relocate()
	{

		int w = 360, h = 165;
		int x = (this.getWidth() - w) / 2, y = (this.getHeight() - h) / 2;

		verifyDiffCheckBox.setLocation(x, y);
		verifyEncodingCheckBox.setLocation(x, y + 30);

		statsOutputCheckBox.setLocation(x, y + 70);

		debugOuputCheckBox.setLocation(x, y + 110);
		debugOutputLabel.setLocation(x, y + 140);
		debugOutputField.setLocation(x + 110, y + 140);
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
				.getConfigParameter(ConfigurationKeys.VERIFICATION_DIFF);
		if (o != null) {
			controller.setEnableDiffVerification((Boolean) o);
		}
		else {
			controller.setEnableDiffVerification(false);
		}

		o = config.getConfigParameter(ConfigurationKeys.VERIFICATION_ENCODING);
		if (o != null) {
			controller.setEnableEncodingVerification((Boolean) o);
		}
		else {
			controller.setEnableEncodingVerification(false);
		}

		o = config
				.getConfigParameter(ConfigurationKeys.MODE_STATISTICAL_OUTPUT);
		if (o != null) {
			controller.setEnableStatsOutput((Boolean) o);
		}
		else {
			controller.setEnableStatsOutput(false);
		}

		o = config.getConfigParameter(ConfigurationKeys.LOGGING_PATH_DEBUG);
		if (o != null) {
			controller.setEnableDebugOutput(true);
			this.debugOutputField.setText((String) o);
		}
		else {
			controller.setEnableDebugOutput(false);
			this.debugOutputField.setText("");
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

		boolean verifyDiff = controller.isDiffVerificationEnabled();
		boolean verifyEncoding = controller.isEncodingVerificationEnabled();
		boolean statsOutput = controller.isStatsOutputEnabled();
		boolean debugOutput = controller.isDebugOutputEnabled();

		if (verifyDiff || verifyEncoding || statsOutput || debugOutput) {

			builder.append("\t<debug>\r\n");

			if (verifyDiff) {
				builder.append("\t\t<verification_diff>" + verifyDiff
						+ "</verification_diff>\r\n");
			}

			if (verifyEncoding) {
				builder.append("\t\t<verification_encoding>" + verifyEncoding
						+ "</verification_encoding>\r\n");
			}

			if (statsOutput) {
				builder.append("\t\t<statistical_output>" + statsOutput
						+ "</statistical_output>\r\n");
			}

			builder.append("\t\t<debug_output>\r\n"); // \"" + path +
														// "\"</debug_output>\r\n");
			builder.append("\t\t\t<enabled>" + debugOutput + "</enabled>\r\n");

			if (debugOutput) {

				String path = debugOutputField.getText();
				if (path.length() == 0) {
					errors.add(new ConfigItem(ConfigItemTypes.WARNING,
							ConfigErrorKeys.PATH_NOT_SET,
							"The folder of the debug output is not specified."));
				}
				if (!path.endsWith(File.separator)
						&& path.contains(File.separator)) {
					path += File.separator;
				}

				builder.append("\t\t\t<path>\"" + path + "\"</path>\r\n");
			}

			builder.append("\t\t</debug_output>\r\n");
			builder.append("\t</debug>\r\n");
		}
	}
}
