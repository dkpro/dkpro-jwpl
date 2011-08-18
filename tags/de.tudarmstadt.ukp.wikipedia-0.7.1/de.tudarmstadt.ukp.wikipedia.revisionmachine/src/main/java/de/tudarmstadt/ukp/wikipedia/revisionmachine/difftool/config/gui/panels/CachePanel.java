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
 * related to the cache.
 *
 *
 *
 */
@SuppressWarnings("serial")
public class CachePanel
	extends AbstractPanel
{

	private JLabel taskLimitationsLabel;

	private JLabel articleTaskLabel;
	private JTextField articleTaskLimitField;

	private JLabel diffTaskLabel;
	private JTextField diffTaskLimitField;

	private JLabel sqlProducerLimitationsLabel;

	private JLabel maxAllowedPacketLabel;
	private JTextField maxAllowedPacketField;

	/**
	 * (Constructor) Creates a new CachePanel.
	 *
	 * @param controller
	 *            Reference to the controller
	 */
	public CachePanel(final ConfigController controller)
	{

		super(controller);
		controller.register(PanelKeys.PANEL_CACHE, this);

		createTaskSettings();
		createSQLProducerSettings();
	}

	// --------------------------------------------------------------------------//
	// CONSTRUCTION METHODS //
	// --------------------------------------------------------------------------//

	private void createTaskSettings()
	{

		taskLimitationsLabel = new JLabel("Task Limitations (in byte)");
		taskLimitationsLabel.setBounds(10, 10, 250, 25);
		this.add(taskLimitationsLabel);

		articleTaskLabel = new JLabel("Article-Task: ");
		articleTaskLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		articleTaskLabel.setBounds(10, 40, 100, 25);
		this.add(articleTaskLabel);

		articleTaskLimitField = new JTextField();
		articleTaskLimitField.setBounds(120, 40, 200, 25);
		this.add(articleTaskLimitField);

		diffTaskLabel = new JLabel("Diff-Task: ");
		diffTaskLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		diffTaskLabel.setBounds(10, 70, 100, 25);
		this.add(diffTaskLabel);

		diffTaskLimitField = new JTextField();
		diffTaskLimitField.setBounds(120, 70, 200, 25);
		this.add(diffTaskLimitField);
	}


	private void createSQLProducerSettings()
	{

		sqlProducerLimitationsLabel = new JLabel(
				"SQLProducer Limitations (in byte)");
		sqlProducerLimitationsLabel.setBounds(10, 210, 250, 25);
		this.add(sqlProducerLimitationsLabel);

		maxAllowedPacketLabel = new JLabel("MAX_ALLOWED_PACKET");
		maxAllowedPacketLabel
				.setBorder(BorderFactory.createRaisedBevelBorder());
		maxAllowedPacketLabel.setBounds(10, 240, 160, 25);
		this.add(maxAllowedPacketLabel);

		maxAllowedPacketField = new JTextField();
		maxAllowedPacketField.setBounds(180, 240, 140, 25);
		this.add(maxAllowedPacketField);
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

	}

	/**
	 * A call of this method should validate the positions of the panels
	 * components.
	 */
	@Override
	public void relocate()
	{

		int w = 310, h = 255;
		int x = (this.getWidth() - w) / 2, y = (this.getHeight() - h) / 2;

		taskLimitationsLabel.setLocation(x, y);
		articleTaskLabel.setLocation(x, y + 30);
		articleTaskLimitField.setLocation(x + 110, y + 30);
		diffTaskLabel.setLocation(x, y + 60);
		diffTaskLimitField.setLocation(x + 110, y + 60);

		sqlProducerLimitationsLabel.setLocation(x, y + 100);
		maxAllowedPacketLabel.setLocation(x, y + 130);
		maxAllowedPacketField.setLocation(x + 170, y + 130);
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
				.getConfigParameter(ConfigurationKeys.LIMIT_TASK_SIZE_REVISIONS);
		if (o != null) {
			this.articleTaskLimitField.setText(Long.toString((Long) o));
		}
		else {
			this.articleTaskLimitField.setText("");
		}

		o = config.getConfigParameter(ConfigurationKeys.LIMIT_TASK_SIZE_DIFFS);
		if (o != null) {
			this.diffTaskLimitField.setText(Long.toString((Long) o));
		}
		else {
			this.diffTaskLimitField.setText("");
		}

		o = config
				.getConfigParameter(ConfigurationKeys.LIMIT_SQLSERVER_MAX_ALLOWED_PACKET);
		if (o != null) {
			this.maxAllowedPacketField.setText(Long.toString((Long) o));
		}
		else {
			this.maxAllowedPacketField.setText("");
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

		long tasksizeRevisions = -1, tasksizeDiffs = -1, maxAllowedPacket = -1;

		// Check the ArticleTask size input
		String text = this.articleTaskLimitField.getText();
		if (text.length() == 0) {
			errors.add(new ConfigItem(ConfigItemTypes.ERROR,
					ConfigErrorKeys.MISSING_VALUE,
					"The value for the size of ArticleTasks" + " is missing."));
		}
		else {
			try {
				tasksizeRevisions = Long.parseLong(text);
				if (tasksizeRevisions < 1000000) {
					errors.add(new ConfigItem(ConfigItemTypes.ERROR,
							ConfigErrorKeys.VALUE_OUT_OF_RANGE,
							"The value for the size of an "
									+ "ArticleTask has to be at least "
									+ "1000000 Byte."));
				}
			}
			catch (NumberFormatException nfe) {
				errors.add(new ConfigItem(ConfigItemTypes.ERROR,
						ConfigErrorKeys.ILLEGAL_INPUT,
						"NumberFormatException for the size of"
								+ " ArticleTasks"));
			}
		}

		// Check the DiffTask size input
		text = this.diffTaskLimitField.getText();
		if (text.length() == 0) {
			errors.add(new ConfigItem(ConfigItemTypes.ERROR,
					ConfigErrorKeys.MISSING_VALUE,
					"The value for the size of DiffTasks" + " is missing."));
		}
		else {
			try {
				tasksizeDiffs = Long.parseLong(text);
				if (tasksizeDiffs < 1000000) {
					errors.add(new ConfigItem(ConfigItemTypes.ERROR,
							ConfigErrorKeys.VALUE_OUT_OF_RANGE,
							"The value for the size of a DiffTask "
									+ "has to be at least 1000000 Byte."));
				}
			}
			catch (NumberFormatException nfe) {
				errors.add(new ConfigItem(ConfigItemTypes.ERROR,
						ConfigErrorKeys.ILLEGAL_INPUT,
						"NumberFormatException for the size of" + " DiffTasks"));
			}
		}

		// Check the SQLProducer MaxAllowedPacket input
		text = this.maxAllowedPacketField.getText();
		if (text.length() == 0) {
			errors.add(new ConfigItem(ConfigItemTypes.ERROR,
					ConfigErrorKeys.MISSING_VALUE,
					"The value for SQLProducer MaxAllowedPacket"
							+ " is missing."));
		}
		else {
			try {
				maxAllowedPacket = Long.parseLong(text);
				if (maxAllowedPacket < 1000000) {
					errors.add(new ConfigItem(ConfigItemTypes.WARNING,
							ConfigErrorKeys.VALUE_OUT_OF_RANGE,
							"The value for SQLProducer "
									+ "MaxAllowedPacket should be at least"
									+ " 1000000 Byte."));
				}
			}
			catch (NumberFormatException nfe) {
				errors.add(new ConfigItem(ConfigItemTypes.ERROR,
						ConfigErrorKeys.ILLEGAL_INPUT,
						"NumberFormatException for the size of"
								+ " SQLProducer MaxAllowedPacket"));
			}
		}

		builder.append("\t<cache>\r\n");
		builder.append("\t\t<LIMIT_TASK_SIZE_REVISIONS>" + tasksizeRevisions
				+ "</LIMIT_TASK_SIZE_REVISIONS>\r\n");
		builder.append("\t\t<LIMIT_TASK_SIZE_DIFFS>" + tasksizeDiffs
				+ "</LIMIT_TASK_SIZE_DIFFS>\r\n");
		builder.append("\t\t<LIMIT_SQLSERVER_MAX_ALLOWED_PACKET>"
				+ maxAllowedPacket
				+ "</LIMIT_SQLSERVER_MAX_ALLOWED_PACKET>\r\n");

		builder.append("\t</cache>\r\n");
	}
}
