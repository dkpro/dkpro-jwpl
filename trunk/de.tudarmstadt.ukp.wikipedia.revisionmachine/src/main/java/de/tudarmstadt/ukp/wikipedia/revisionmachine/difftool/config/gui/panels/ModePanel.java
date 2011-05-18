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

import javax.swing.BorderFactory;
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
 * related to the diff calculation.
 *
 *
 *
 */
@SuppressWarnings("serial")
public class ModePanel
	extends AbstractPanel
{

	private JLabel fullRevisionLabel;
	private JTextField fullRevisionField;

	private JLabel minimumCommonSequenceLabel;
	private JTextField minimumCommonSequenceField;

	/**
	 * (Constructor) Creates a new ModePanel.
	 *
	 * @param controller
	 *            Reference to the controller
	 */
	public ModePanel(final ConfigController controller)
	{

		super(controller);
		controller.register(PanelKeys.PANEL_VALUES, this);

		createFullRevisionSettings();
		createMinimumCommonSequenceSettings();
	}

	// --------------------------------------------------------------------------//
	// CONSTRUCTION METHODS //
	// --------------------------------------------------------------------------//

	private void createFullRevisionSettings()
	{

		fullRevisionLabel = new JLabel(
				"Every n-th revision will be a full revision:");
		fullRevisionLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		fullRevisionLabel.setBounds(10, 10, 270, 25);
		this.add(fullRevisionLabel);

		fullRevisionField = new JTextField();
		fullRevisionField.setBounds(290, 10, 100, 25);
		this.add(fullRevisionField);
	}

	private void createMinimumCommonSequenceSettings()
	{

		minimumCommonSequenceLabel = new JLabel(
				"Min lenght of a common subsequence:");
		minimumCommonSequenceLabel.setBorder(BorderFactory
				.createRaisedBevelBorder());
		minimumCommonSequenceLabel.setBounds(10, 50, 270, 25);
		this.add(minimumCommonSequenceLabel);

		minimumCommonSequenceField = new JTextField();
		minimumCommonSequenceField.setBounds(290, 50, 100, 25);
		this.add(minimumCommonSequenceField);
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

		int w = 380, h = 65;
		int x = (this.getWidth() - w) / 2, y = (this.getHeight() - h) / 2;

		fullRevisionLabel.setLocation(x, y);
		fullRevisionField.setLocation(x + 280, y);

		minimumCommonSequenceLabel.setLocation(x, y + 40);
		minimumCommonSequenceField.setLocation(x + 280, y + 40);
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
				.getConfigParameter(ConfigurationKeys.COUNTER_FULL_REVISION);
		if (o != null) {
			this.fullRevisionField.setText(Integer.toString((Integer) o));
		}
		else {
			this.fullRevisionField.setText("");
		}

		o = config
				.getConfigParameter(ConfigurationKeys.VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING);
		if (o != null) {
			this.minimumCommonSequenceField.setText(Integer
					.toString((Integer) o));
		}
		else {
			this.minimumCommonSequenceField.setText("");
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
	public void toXML(StringBuilder builder, final ConfigVerification errors)
	{

		int minLCS = -1, fullRevCounter = -1;

		// Check the FullRevisionCounter input
		String text = this.minimumCommonSequenceField.getText();
		if (text.length() == 0) {
			errors.add(new ConfigItem(ConfigItemTypes.ERROR,
					ConfigErrorKeys.MISSING_VALUE, "The value for minimum "
							+ "LongestCommonSubsequence is missing."));
		}
		else {
			try {
				minLCS = Integer.parseInt(text);
				if (minLCS < 7) {
					errors.add(new ConfigItem(ConfigItemTypes.ERROR,
							ConfigErrorKeys.VALUE_OUT_OF_RANGE,
							"The value of the minimum "
									+ " LongestCommonSubsequence has to be"
									+ " at least 7."));
				}
				else if (minLCS < 12) {
					errors.add(new ConfigItem(ConfigItemTypes.WARNING,
							ConfigErrorKeys.VALUE_OUT_OF_RANGE,
							"A value smaller than 12 for the "
									+ "minimum LongestCommonSubsequence"
									+ " is not recommended."));
				}
			}
			catch (NumberFormatException nfe) {
				errors.add(new ConfigItem(ConfigItemTypes.ERROR,
						ConfigErrorKeys.ILLEGAL_INPUT,
						"NumberFormatException for "
								+ "ArticleProducer TaskLimit"));
			}
		}

		// Check the FullRevisionCounter input
		text = this.fullRevisionField.getText();
		if (text.length() == 0) {
			errors.add(new ConfigItem(ConfigItemTypes.ERROR,
					ConfigErrorKeys.MISSING_VALUE,
					"The value for FullRevision Counter" + " is missing."));
		}
		else {
			try {
				fullRevCounter = Integer.parseInt(text);
				if (fullRevCounter < 1) {
					errors.add(new ConfigItem(ConfigItemTypes.ERROR,
							ConfigErrorKeys.VALUE_OUT_OF_RANGE,
							"The FullRevision Counter has to "
									+ "be at least 1."));
				}
				else if (fullRevCounter < 100) {
					errors.add(new ConfigItem(ConfigItemTypes.WARNING,
							ConfigErrorKeys.VALUE_OUT_OF_RANGE,
							"A FullRevision Counter with a"
									+ " value smaller than 100 is not"
									+ " recommended."));
				}
			}
			catch (NumberFormatException nfe) {
				errors.add(new ConfigItem(ConfigItemTypes.ERROR,
						ConfigErrorKeys.ILLEGAL_INPUT,
						"NumberFormatException for "
								+ "ArticleProducer TaskLimit"));
			}
		}

		builder.append("\t<values>\r\n");
		builder.append("\t\t<VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING>" + minLCS
				+ "</VALUE_MINIMUM_LONGEST_COMMON_SUBSTRING>\r\n");
		builder.append("\t\t<COUNTER_FULL_REVISION>" + fullRevCounter
				+ "</COUNTER_FULL_REVISION>\r\n");
		builder.append("\t</values>\r\n");
	}
}
