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
package org.dkpro.jwpl.revisionmachine.difftool.config.gui.panels;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigController;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigVerification;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.data.ConfigErrorKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.data.ConfigItem;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.data.ConfigItemTypes;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.data.PanelKeys;
import org.slf4j.event.Level;

/**
 * Panel class of the ConfigurationTool
 *
 * This panel contains all components for setting configuration parameters
 * related to the logging.
 *
 */
@SuppressWarnings("serial")
public class LoggingPanel
	extends AbstractPanel
{

	private JLabel diffToolLabel;
	private JTextField diffToolField;
	private JComboBox<Level> diffToolLogLevelComboBox;

	/**
	 * (Constructor) Creates a new LoggingPanel.
	 *
	 * @param controller
	 *            Reference to the controller
	 */
	public LoggingPanel(final ConfigController controller)
	{

		super(controller);
		controller.register(PanelKeys.PANEL_LOGGING, this);

		createDiffToolLoggingSettings();
	}

	private void createDiffToolLoggingSettings()
	{
		diffToolLabel = new JLabel("Logging Root Folder: ");
		diffToolLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		diffToolLabel.setBounds(10, 10, 150, 25);
		this.add(diffToolLabel);

		diffToolField = new JTextField();
		diffToolField.setBounds(170, 10, 200, 25);
		this.add(diffToolField);

		diffToolLogLevelComboBox = new JComboBox<>();
		diffToolLogLevelComboBox.setBounds(390, 10, 100, 25);

		diffToolLogLevelComboBox.addItem(Level.ERROR);
		diffToolLogLevelComboBox.addItem(Level.WARN);
		diffToolLogLevelComboBox.addItem(Level.INFO);
		diffToolLogLevelComboBox.addItem(Level.DEBUG);
		diffToolLogLevelComboBox.addItem(Level.TRACE);

		this.add(diffToolLogLevelComboBox);
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

		int w = 480, h = 245;
		int x = (this.getWidth() - w) / 2, y = (this.getHeight() - h) / 2;

		diffToolLabel.setLocation(x, y);
		diffToolField.setLocation(x + 160, y);
		diffToolLogLevelComboBox.setLocation(x + 380, y);

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
				.getConfigParameter(ConfigurationKeys.LOGGING_PATH_DIFFTOOL);
		if (o != null) {
			this.diffToolField.setText((String) o);
		}
		else {
			this.diffToolField.setText("");
		}

		o = config
				.getConfigParameter(ConfigurationKeys.LOGGING_LOGLEVEL_DIFFTOOL);
		if (o != null) {
			this.diffToolLogLevelComboBox.setSelectedItem(o);
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

		builder.append("\t<logging>\r\n");

		// DIFFTOOL
		String pathDiffTool = diffToolField.getText();
		if (pathDiffTool.length() == 0) {
			errors.add(new ConfigItem(ConfigItemTypes.WARNING,
					ConfigErrorKeys.PATH_NOT_SET,
					"The root folder for all logs and debug"
							+ " information has not been set."));
		}
		if (!pathDiffTool.endsWith(File.separator)
				&& pathDiffTool.contains(File.separator)) {
			pathDiffTool += File.separator;
		}

		builder.append("\t\t<root_folder>\"").append(pathDiffTool).append( "\"</root_folder>\r\n");
		builder.append("\t\t<diff_tool>\r\n");
		builder.append("\t\t\t<level>").append(diffToolLogLevelComboBox.getSelectedItem() ).append("</level>\r\n");
		builder.append("\t\t</diff_tool>\r\n");
		builder.append("\t</logging>\r\n");
	}
}
