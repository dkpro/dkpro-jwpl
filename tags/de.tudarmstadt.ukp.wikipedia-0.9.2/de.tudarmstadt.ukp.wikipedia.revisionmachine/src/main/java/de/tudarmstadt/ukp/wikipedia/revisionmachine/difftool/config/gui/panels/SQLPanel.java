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
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.OutputType;

/**
 * Panel class of the ConfigurationTool
 *
 * This panel contains all components for setting configuration parameters
 * related to the database output.
 *
 *
 *
 */
@SuppressWarnings("serial")
public class SQLPanel
	extends AbstractPanel
{

	/**
	 * (Constructor) Create the SQLPanel object.
	 *
	 * @param controller
	 *            Reference to the controller
	 */
	public SQLPanel(final ConfigController controller)
	{

		super(controller);
		controller.register(PanelKeys.PANEL_SQL, this);

		createSQLFields();
		createOutputSettings();
	}

	// --------------------------------------------------------------------------//
	// CONSTRUCTION METHODS //
	// --------------------------------------------------------------------------//

	private JCheckBox enableSQLDatabaseConnection;
	private JLabel sqlHostLabel;
	private JTextField sqlHostField;
	private JLabel sqlDatabaseLabel;
	private JTextField sqlDatabaseField;
	private JLabel sqlUserLabel;
	private JTextField sqlUserField;
	private JLabel sqlPasswordLabel;
	private JTextField sqlPasswordField;

	private JCheckBox enableZipEncodingCheckBox;

	private void createSQLFields()
	{

		enableSQLDatabaseConnection = new JCheckBox(
				"Activate Database Output");
		enableSQLDatabaseConnection.setBounds(10, 10, 200, 25);

		enableSQLDatabaseConnection.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				boolean flag = !controller.isEnableSQLDatabaseOutput();
				controller.setEnableSQLDatabaseOutput(flag);

				validateSQLFields();
			}
		});

		this.add(enableSQLDatabaseConnection);

		sqlHostLabel = new JLabel("Host");
		sqlHostLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		sqlHostLabel.setBounds(10, 50, 100, 25);
		this.add(sqlHostLabel);

		sqlHostField = new JTextField();
		sqlHostField.setBounds(120, 50, 100, 25);
		this.add(sqlHostField);

		sqlDatabaseLabel = new JLabel("Database");
		sqlDatabaseLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		sqlDatabaseLabel.setBounds(10, 50, 100, 25);
		this.add(sqlDatabaseLabel);

		sqlDatabaseField = new JTextField();
		sqlDatabaseField.setBounds(120, 50, 100, 25);
		this.add(sqlDatabaseField);

		sqlUserLabel = new JLabel("User");
		sqlUserLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		sqlUserLabel.setBounds(10, 80, 100, 25);
		this.add(sqlUserLabel);

		sqlUserField = new JTextField();
		sqlUserField.setBounds(120, 80, 100, 25);
		this.add(sqlUserField);

		sqlPasswordLabel = new JLabel("Password");
		sqlPasswordLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		sqlPasswordLabel.setBounds(10, 110, 100, 25);
		this.add(sqlPasswordLabel);

		sqlPasswordField = new JTextField();
		sqlPasswordField.setBounds(120, 110, 100, 25);
		this.add(sqlPasswordField);
	}

	private void createOutputSettings()
	{

		enableZipEncodingCheckBox = new JCheckBox("Activate Zip Encoding");
		enableZipEncodingCheckBox.setBounds(10, 160, 200, 25);

		enableZipEncodingCheckBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{

				boolean flag = !controller.isZipCompressionEnabled();
				controller.setEnableZipCompression(flag);

				validateSettings();
			}
		});

		this.add(enableZipEncodingCheckBox);
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
		validateSQLFields();
		validateSettings();
	}

	/**
	 * Validates the Settings.
	 */
	private void validateSettings()
	{
		enableZipEncodingCheckBox.setSelected(controller
				.isZipCompressionEnabled());
	}

	/**
	 * Validates the UNCOMPRESSED Settings.
	 */
	private void validateSQLFields()
	{

		boolean flag = controller.isEnableSQLDatabaseOutput();

		enableSQLDatabaseConnection.setSelected(flag);

		sqlHostLabel.setEnabled(flag);
		sqlHostField.setEnabled(flag);
		sqlDatabaseLabel.setEnabled(flag);
		sqlDatabaseField.setEnabled(flag);
		sqlUserLabel.setEnabled(flag);
		sqlUserField.setEnabled(flag);
		sqlPasswordLabel.setEnabled(flag);
		sqlPasswordField.setEnabled(flag);

		enableZipEncodingCheckBox.setEnabled(flag);
	}

	/**
	 * A call of this method should validate the positions of the panels
	 * components.
	 */
	@Override
	public void relocate()
	{

		int w = 200, h = 235;

		int x = (this.getWidth() - w) / 2;
		int y = (this.getHeight() - h) / 2;

		enableSQLDatabaseConnection.setLocation(x, y);
		sqlHostLabel.setLocation(x, y + 40);
		sqlHostField.setLocation(x + 110, y + 40);
		sqlDatabaseLabel.setLocation(x, y + 70);
		sqlDatabaseField.setLocation(x + 110, y + 70);
		sqlUserLabel.setLocation(x, y + 100);
		sqlUserField.setLocation(x + 110, y + 100);
		sqlPasswordLabel.setLocation(x, y + 130);
		sqlPasswordField.setLocation(x + 110, y + 130);
		enableZipEncodingCheckBox.setLocation(x, y + 180);
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

		Object o = config.getConfigParameter(ConfigurationKeys.MODE_OUTPUT);
		if ((OutputType) o == OutputType.DATABASE) {
			controller.setEnableSQLDatabaseOutput(true);
		}

		o = config.getConfigParameter(ConfigurationKeys.SQL_HOST);
		if (o != null) {
			this.sqlHostField.setText((String) o);
		}
		else {
			this.sqlHostField.setText("");
		}

		o = config.getConfigParameter(ConfigurationKeys.SQL_DATABASE);
		if (o != null) {
			this.sqlDatabaseField.setText((String) o);
		}
		else {
			this.sqlDatabaseField.setText("");
		}

		o = config.getConfigParameter(ConfigurationKeys.SQL_USERNAME);
		if (o != null) {
			this.sqlUserField.setText((String) o);
		}
		else {
			this.sqlUserField.setText("");
		}

		o = config.getConfigParameter(ConfigurationKeys.SQL_PASSWORD);
		if (o != null) {
			this.sqlPasswordField.setText((String) o);
		}
		else {
			this.sqlPasswordField.setText("");
		}

		o = config
				.getConfigParameter(ConfigurationKeys.MODE_ZIP_COMPRESSION_ENABLED);
		if (o != null) {
			controller.setEnableZipCompression((Boolean) o);
		}
		else {
			controller.setEnableZipCompression(false);
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

		if (controller.isEnableSQLDatabaseOutput()) {

			String database = new String(), user = new String(), password = new String(), host = new String();

			host = sqlHostField.getText();
			if (host.length() == 0) {
				errors.add(new ConfigItem(ConfigItemTypes.ERROR,
						ConfigErrorKeys.MISSING_VALUE,
						"The name of the sqlproducer-host is missing."));
			}

			database = sqlDatabaseField.getText();
			if (database.length() == 0) {
				errors.add(new ConfigItem(ConfigItemTypes.ERROR,
						ConfigErrorKeys.MISSING_VALUE,
						"The name of the sqlproducer-database is missing."));
			}

			user = sqlUserField.getText();
			if (database.length() == 0) {
				errors.add(new ConfigItem(ConfigItemTypes.ERROR,
						ConfigErrorKeys.MISSING_VALUE,
						"The name of the sqlproducer-user is missing."));
			}

			password = sqlPasswordField.getText();
			if (password.length() == 0) {
				errors.add(new ConfigItem(ConfigItemTypes.ERROR,
						ConfigErrorKeys.MISSING_VALUE,
						"The password of the sqlproducer-user is missing."));
			}

			boolean zipComp = controller.isZipCompressionEnabled();

			builder.append("\t<output>\r\n");
			builder.append("\t\t<OUTPUT_MODE>" + OutputType.DATABASE
					+ "</OUTPUT_MODE>\r\n");
			builder.append("\t\t\t<sql>\r\n");
			builder.append("\t\t\t\t<host>" + host + "</host>\r\n");
			builder.append("\t\t\t\t<database>" + database + "</database>\r\n");
			builder.append("\t\t\t\t<user>" + user + "</user>\r\n");
			builder.append("\t\t\t\t<password>" + password + "</password>\r\n");
			builder.append("\t\t\t</sql>\r\n");
			builder.append("\t\t<MODE_ZIP_COMPRESSION_ENABLED>" + zipComp
					+ "</MODE_ZIP_COMPRESSION_ENABLED>\r\n");
			builder.append("\t</output>\r\n");
		}
	}
}
