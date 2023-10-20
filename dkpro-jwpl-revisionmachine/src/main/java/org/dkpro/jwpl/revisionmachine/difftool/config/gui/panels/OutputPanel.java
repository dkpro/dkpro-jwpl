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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.dkpro.jwpl.revisionmachine.difftool.config.ConfigurationKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigController;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigVerification;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.data.ConfigErrorKeys;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.data.ConfigItem;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.data.ConfigItemTypes;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.data.OutputCompressionEnum;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.data.PanelKeys;
import org.dkpro.jwpl.revisionmachine.difftool.data.OutputType;

/**
 * Panel class of the ConfigurationTool
 * <p>
 * This panel contains all components for setting configuration parameters
 * related to the file output.
 *
 *
 *
 */
@SuppressWarnings("serial")
public class OutputPanel
	extends AbstractPanel
{

	private JLabel outputLabel;
	private JTextField outputPathField;

	private JCheckBox enableZipEncodingCompression;
	private JCheckBox activateDataFileOutput;
	private JLabel outputCompression;
	private JRadioButton disableOutputCompression;
	private JRadioButton enable7ZipOutputCompression;
	private JRadioButton enableBZip2OutputCompression;

	private JCheckBox enableMultipleOutputFiles;
	private JLabel outputSizeLimitLabel;
	private JTextField outputSizeLimitField;

	/**
	 * (Constructor) Create the OutputPanel object.
	 *
	 * @param controller
	 *            Reference to the controller
	 */
	public OutputPanel(final ConfigController controller)
	{

		super(controller);
		controller.register(PanelKeys.PANEL_OUTPUT, this);

		createOutputPathSettings();
		createOutputSizeSettings();
		createOutputSettings();
	}

	// --------------------------------------------------------------------------//
	// CONSTRUCTION METHODS //
	// --------------------------------------------------------------------------//

	private void createOutputPathSettings()
	{

		outputLabel = new JLabel("Output Folder: ");
		outputLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		outputLabel.setBounds(10, 10, 150, 25);
		this.add(outputLabel);

		outputPathField = new JTextField();
		outputPathField.setBounds(170, 10, 200, 25);
		this.add(outputPathField);
	}

	private void createOutputSettings()
	{

		enableZipEncodingCompression = new JCheckBox("Activate Zip Encoding");
		enableZipEncodingCompression.setBounds(120, 50, 150, 25);

		enableZipEncodingCompression.addActionListener(e -> {

      boolean flag = !controller.isZipCompressionEnabled();
      controller.setEnableZipCompression(flag);

      validate();
    });

		this.add(enableZipEncodingCompression);

		outputCompression = new JLabel("Output Compression:");
		outputCompression.setBounds(120, 85, 250, 25);
		this.add(outputCompression);

		disableOutputCompression = new JRadioButton("None");
		disableOutputCompression.setBounds(120, 110, 250, 20);
		this.add(disableOutputCompression);

		disableOutputCompression.addActionListener(e -> {

      OutputCompressionEnum oce = controller.getOutputCompression();
      if (oce != OutputCompressionEnum.None) {
        controller.setOutputCompression(OutputCompressionEnum.None);
      }

      validate();
    });

		enable7ZipOutputCompression = new JRadioButton("7Zip Compression");
		enable7ZipOutputCompression.setBounds(120, 130, 250, 20);
		this.add(enable7ZipOutputCompression);

		enable7ZipOutputCompression.addActionListener(e -> {

      OutputCompressionEnum oce = controller.getOutputCompression();
      if (oce != OutputCompressionEnum.SevenZip) {
        controller
            .setOutputCompression(OutputCompressionEnum.SevenZip);
      }

      validate();
    });

		enableBZip2OutputCompression = new JRadioButton("BZip2 Compression");
		enableBZip2OutputCompression.setBounds(120, 150, 250, 20);
		this.add(enableBZip2OutputCompression);

		enableBZip2OutputCompression.addActionListener(e -> {

      OutputCompressionEnum oce = controller.getOutputCompression();
      if (oce != OutputCompressionEnum.BZip2) {
        controller
            .setOutputCompression(OutputCompressionEnum.BZip2);
      }

      validate();
    });


		activateDataFileOutput = new JCheckBox("DataFile Output");
		activateDataFileOutput.setBounds(120, 50, 170, 25);
		activateDataFileOutput.setVisible(true);
		activateDataFileOutput.addActionListener(e -> {

      boolean flag = !controller.isEnableDataFileOutput();
      controller.setEnableDataFileOutput(flag);

      validate();
    });
		this.add(activateDataFileOutput);

	}

	private void createOutputSizeSettings()
	{

		enableMultipleOutputFiles = new JCheckBox(
				"Allow multiple output files per consumer");
		enableMultipleOutputFiles.setBounds(10, 200, 250, 25);
		this.add(enableMultipleOutputFiles);

		outputSizeLimitLabel = new JLabel("File Size Limit (in byte): ");
		outputSizeLimitLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		outputSizeLimitLabel.setBounds(10, 230, 150, 25);
		this.add(outputSizeLimitLabel);

		outputSizeLimitField = new JTextField();
		outputSizeLimitField.setBounds(170, 230, 200, 25);
		this.add(outputSizeLimitField);

		enableMultipleOutputFiles.addActionListener(e -> {

      boolean flag = !controller.isMultipleOutputFiles();
      controller.setMultipleOutputFiles(flag);

      outputSizeLimitLabel.setEnabled(flag);
      outputSizeLimitField.setEnabled(flag);
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

		boolean flagA = !controller.isEnableSQLDatabaseOutput();
		boolean flagB = controller.isMultipleOutputFiles();

		OutputCompressionEnum oce = controller.getOutputCompression();

		enableZipEncodingCompression.setSelected(controller
				.isZipCompressionEnabled());

		disableOutputCompression.setSelected(oce == OutputCompressionEnum.None);

		enableBZip2OutputCompression
				.setSelected(oce == OutputCompressionEnum.BZip2);

		activateDataFileOutput.setSelected(controller.isEnableDataFileOutput());

		outputLabel.setEnabled(flagA);
		outputPathField.setEnabled(flagA);

		enableZipEncodingCompression.setEnabled(flagA);

		outputCompression.setEnabled(flagA);
		disableOutputCompression.setEnabled(flagA);

		enable7ZipOutputCompression.setEnabled(flagA
				&& controller.is7ZipEnabled());

		enable7ZipOutputCompression
			.setSelected(oce == OutputCompressionEnum.SevenZip);

		enableBZip2OutputCompression.setEnabled(flagA);

		//Enable multiple output files only for uncompressed output
		enableMultipleOutputFiles.setEnabled(flagA&&(oce == OutputCompressionEnum.None));
		enableMultipleOutputFiles.setSelected(flagB);

		outputSizeLimitLabel.setEnabled(flagA && flagB&&(oce == OutputCompressionEnum.None));
		outputSizeLimitField.setEnabled(flagA && flagB&&(oce == OutputCompressionEnum.None));


	}

	/**
	 * A call of this method should validate the positions of the panels
	 * components.
	 */
	@Override
	public void relocate()
	{

		int w = 360, h = 245;

		int x = (this.getWidth() - w) / 2;
		int y = (this.getHeight() - h) / 2;

		outputLabel.setLocation(x, y);
		outputPathField.setLocation(x + 160, y);

		enableZipEncodingCompression.setLocation(x + 110, y + 40);
		outputCompression.setLocation(x + 110, y + 75);
		disableOutputCompression.setLocation(x + 110, y + 100);
		enableBZip2OutputCompression.setLocation(x + 110, y + 120);
		enable7ZipOutputCompression.setLocation(x + 110, y + 140);
		activateDataFileOutput.setLocation(x + 110, y + 160);

		enableMultipleOutputFiles.setLocation(x, y + 190);
		outputSizeLimitLabel.setLocation(x, y + 220);
		outputSizeLimitField.setLocation(x + 160, y + 220);

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
				.getConfigParameter(ConfigurationKeys.PATH_OUTPUT_SQL_FILES);
		if (o != null) {
			this.outputPathField.setText((String) o);
		}
		else {
			this.outputPathField.setText("");
		}

		o = config
				.getConfigParameter(ConfigurationKeys.MODE_ZIP_COMPRESSION_ENABLED);
		if (o != null) {
			controller.setEnableZipCompression((Boolean) o);
		}
		else {
			controller.setEnableZipCompression(false);
		}

		o = config
				.getConfigParameter(ConfigurationKeys.MODE_DATAFILE_OUTPUT);
		if (o != null) {
			controller.setEnableDataFileOutput((Boolean) o);
		}
		else {
			controller.setEnableDataFileOutput(false);
		}

		o = config.getConfigParameter(ConfigurationKeys.MODE_OUTPUT);
		if (o != null) {
			switch ((OutputType) o) {
			case UNCOMPRESSED:
				controller.setEnableSQLDatabaseOutput(false);
				controller.setOutputCompression(OutputCompressionEnum.None);

				o = config
						.getConfigParameter(ConfigurationKeys.LIMIT_SQL_FILE_SIZE);
				break;
			case SEVENZIP:
				controller.setEnableSQLDatabaseOutput(false);
				controller.setOutputCompression(OutputCompressionEnum.SevenZip);

				o = config
						.getConfigParameter(ConfigurationKeys.LIMIT_SQL_ARCHIVE_SIZE);
				break;
			case BZIP2:
				controller.setEnableSQLDatabaseOutput(false);
				controller.setOutputCompression(OutputCompressionEnum.BZip2);

				o = config
						.getConfigParameter(ConfigurationKeys.LIMIT_SQL_ARCHIVE_SIZE);
				break;
			case DATABASE:
				controller.setEnableSQLDatabaseOutput(true);
				controller.setOutputCompression(OutputCompressionEnum.None);

				o = null;
				break;
			}
		}

		if (o != null) {
			controller.setMultipleOutputFiles(true);
			this.outputSizeLimitField.setText(Long.toString((Long) o));
		}
		else {
			controller.setMultipleOutputFiles(false);
			this.outputSizeLimitField.setText("");
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

		if (!controller.isEnableSQLDatabaseOutput()) {

			boolean zipComp = controller.isZipCompressionEnabled();
			boolean multiFile = controller.isMultipleOutputFiles();

			builder.append("\t<output>\r\n");
			builder.append("\t\t<OUTPUT_MODE>");

			OutputCompressionEnum comp = controller.getOutputCompression();
			switch (comp) {
			case None:
				builder.append(OutputType.UNCOMPRESSED);
				break;
			case BZip2:
				builder.append(OutputType.BZIP2);
				break;
			case SevenZip:
				builder.append(OutputType.SEVENZIP);
				break;
			default:
				throw new RuntimeException("Illegal Output Compression Mode");
			}

			builder.append("</OUTPUT_MODE>\r\n");

			String path = this.outputPathField.getText();

			if(path==null||path.equals("")){
				errors.add(new ConfigItem(ConfigItemTypes.WARNING,
						ConfigErrorKeys.MISSING_VALUE,
				"No output path has been set."));
			}

			if (!path.endsWith(File.separator) && path.contains(File.separator)) {
				path += File.separator;
			}

			builder.append("\t\t<PATH>\"" + path + "\"</PATH>\r\n");

			if (multiFile) {

				long sizeLimit = -1;

				String text = outputSizeLimitField.getText();
				if (text.length() == 0) {
					errors.add(new ConfigItem(ConfigItemTypes.ERROR,
							ConfigErrorKeys.MISSING_VALUE,
							"The output limit is missing."));
				}
				else {
					try {
						sizeLimit = Long.parseLong(text);
						if (sizeLimit < 100 * 1024 * 1024) {
							errors.add(new ConfigItem(ConfigItemTypes.ERROR,
									ConfigErrorKeys.VALUE_OUT_OF_RANGE,
									"The output limit has to be at"
											+ " least 100MB"));
						}
					}
					catch (NumberFormatException nfe) {
						errors.add(new ConfigItem(ConfigItemTypes.ERROR,
								ConfigErrorKeys.ILLEGAL_INPUT,
								"NumberFormatException for the"
										+ " output limit"));
					}
				}

				switch (comp) {
				case None:
					builder.append("\t\t<LIMIT_SQL_FILE_SIZE>" + sizeLimit
							+ "</LIMIT_SQL_FILE_SIZE>\r\n");
					break;
				default:
					builder.append("\t\t<LIMIT_SQL_ARCHIVE_SIZE>" + sizeLimit
							+ "</LIMIT_SQL_ARCHIVE_SIZE>\r\n");
					break;
				}
			}

			builder.append("\t\t<MODE_ZIP_COMPRESSION_ENABLED>" + zipComp
					+ "</MODE_ZIP_COMPRESSION_ENABLED>\r\n");

			if (controller.isEnableDataFileOutput()) {
				builder.append("\t\t<MODE_DATAFILE_OUTPUT>true</MODE_DATAFILE_OUTPUT>\r\n");
			}else{
				builder.append("\t\t<MODE_DATAFILE_OUTPUT>false</MODE_DATAFILE_OUTPUT>\r\n");
			}

			builder.append("\t</output>\r\n");
		}
	}
}
