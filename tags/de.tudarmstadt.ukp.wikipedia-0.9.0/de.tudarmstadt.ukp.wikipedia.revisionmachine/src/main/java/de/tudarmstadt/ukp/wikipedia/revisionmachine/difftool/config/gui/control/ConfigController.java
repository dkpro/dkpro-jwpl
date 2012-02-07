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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control;

import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JPanel;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.OutputCompressionEnum;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.PanelKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.dialogs.ConfigDialog;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.dialogs.XMLFileChooser;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.panels.AbstractPanel;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.SurrogateModes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive.ArchiveDescription;

/**
 * Controller of the ConfigurationTool
 *
 *
 *
 */
public class ConfigController
{

	/** Reference to the ArchiveRegistry */
	private final ArchiveRegistry archives;

	/** Reference to the ComponentRegistry */
	private ComponentRegistry components;

	/** Reference to the configuration */
	private final ConfigSettings config;

	/**
	 * Configuration settings - Flag that indicates whether the 7Zip support is
	 * enabled or not
	 */
	private boolean enable7Zip;

	/**
	 * Configuration settings - Flag that indicates whether debug output is
	 * enabled
	 */
	private boolean enableDebugOutput;

	/**
	 * Configuration settings - Flag that indicates whether diff verification is
	 * enabled
	 */
	private boolean enableDiffVerification;

	/**
	 * Configuration settings - Flag that indicates whether encoding
	 * verification is enabled
	 */
	private boolean enableEncodingVerification;

	/**
	 * Configuration settings - Flag that indicates whether the database output
	 * mode is enabled
	 */
	private boolean enableSQLDatabaseOutput;

	/**
	 * Configuration settings - Flag that indicates whether output should
	 * be a datafile instead of an sql dump
	 */
	private boolean enableDataFileOutput;

	/**
	 * Configuration settings - Flag that indicates whether statistical output
	 * is enabled
	 */
	private boolean enableStatsOutput;

	/**
	 * Configuration settings - Flag that indicates whether statistical output
	 * is enabled
	 */
	private boolean enableZipCompression;

	/** Reference to the ConfigVerification */
	private ConfigVerification errors;

	/**
	 * Configuration settings - Flag that indicates whether multiple output
	 * files are allowed
	 */
	private boolean multipleOutputFiles;

	/** Configuration settings - Output compression mode */
	private OutputCompressionEnum outputCompression;

	/** Configuration settings - Output file limit */
	private long outputFileLimit;

	/** Configuration settings - Surrogate Mode */
	private SurrogateModes surrogates;

	/** XML Representation of the content */
	private StringBuilder xmlConfig;

	/**
	 * (Constructor) Creates a new ConfigController.
	 */
	public ConfigController()
	{

		this.components = new ComponentRegistry();
		this.archives = new ArchiveRegistry();

		this.config = new ConfigSettings();

		this.enable7Zip = false;

		this.outputFileLimit = -1;
		this.multipleOutputFiles = false;
		this.outputCompression = OutputCompressionEnum.None;

		this.enableZipCompression = true;
		this.enableDebugOutput = false;
		this.enableSQLDatabaseOutput = false;

		this.surrogates = SurrogateModes.DISCARD_REVISION;

	}

	/**
	 * Adds an archive to the archive registry.
	 *
	 * @param archive
	 *            reference to the archive
	 */
	public void addArchive(final ArchiveDescription archive)
	{
		this.archives.addArchive(archive);
	}

	/**
	 * Applies the configuration file.
	 *
	 * The input settings will be ignored if a default configuration was used.
	 */
	private void applyConfig()
	{
		this.components.applyConfig(config);

		switch (config.getConfigType()) {
		case DEFAULT:
			break;
		case IMPORT:
			this.archives.applyConfiguration(config);
		}

		repaint();
	}

	/**
	 * Creates the xml content representation of the currently used settings.
	 *
	 * @return TRUE if the ConfigVerfication contains no items, FALSE otherwise
	 */
	public boolean createConfigurationXML()
	{

		errors = new ConfigVerification();
		xmlConfig = new StringBuilder();

		xmlConfig.append("<config>\r\n");
		components.toXML(xmlConfig, errors);
		xmlConfig.append("</config>\r\n");

		if (errors.getRowCount() != 0) {

			// TODO: invoke the dialog at another place
			new ConfigDialog(this).setVisible(true);

			return false;
		}

		return true;
	}

	/**
	 * Applies the default parameter to the currently loaded config
	 */
	public void defaultConfiguration()
	{
		config.defaultConfiguration();
		applyConfig();
	}

	/**
	 * Returns the reference to the ArchiveRegistry.
	 *
	 * @return archive registry
	 */
	public ArchiveRegistry getArchives()
	{
		return archives;
	}

	/**
	 * Return the reference to the ConfigVerifactions.
	 *
	 * @return ConfigVerification
	 */
	public ConfigVerification getConfigErrors()
	{
		return errors;
	}

	/**
	 * Returns the output compression mode.
	 *
	 * @return output compression mode
	 */
	public OutputCompressionEnum getOutputCompression()
	{
		return outputCompression;
	}

	/**
	 * Returns the maximum size of an output file.
	 *
	 * @return maximum size of an output file.
	 */
	public long getOutputFileLimit()
	{
		return outputFileLimit;
	}

	/**
	 * Returns the reference to the component registry.
	 *
	 * @return component registry
	 */
	public ComponentRegistry getRegistry()
	{
		return components;
	}

	/**
	 * Returns the surrogate mode.
	 *
	 * @return surrogate mode
	 */
	public SurrogateModes getSurrogates()
	{
		return surrogates;
	}

	/**
	 * Returns whether the 7Zip support is enabled or not.
	 *
	 * @return TRUE | FALSE
	 */
	public boolean is7ZipEnabled()
	{
		return enable7Zip;
	}

	/**
	 * Returns whether the debug output is enabled.
	 *
	 * @return debug output flag
	 */
	public boolean isDebugOutputEnabled()
	{
		return enableDebugOutput;
	}

	/**
	 * Returns whether the diff verification mode is enabled.
	 *
	 * @return diff verification flag
	 */
	public boolean isDiffVerificationEnabled()
	{
		return enableDiffVerification;
	}

	/**
	 * Returns whether the database output mode is enabled.
	 *
	 * @return database output flag
	 */
	public boolean isEnableSQLDatabaseOutput()
	{
		return enableSQLDatabaseOutput;
	}

	/**
	 * Returns whether the encoding verification mode is enabled.
	 *
	 * @return encoding verification flag
	 */
	public boolean isEncodingVerificationEnabled()
	{
		return enableEncodingVerification;
	}

	/**
	 * Returns whether multiple output files should be used.
	 *
	 * @return multiple output files flag
	 */
	public boolean isMultipleOutputFiles()
	{
		return multipleOutputFiles;
	}

	/**
	 * Returns whether the statistical output mode is enabled.
	 *
	 * @return statistical output flag
	 */
	public boolean isStatsOutputEnabled()
	{
		return enableStatsOutput;
	}

	/**
	 * Returns whether the Zip-Compression is enabled or not.
	 *
	 * @return Zip-Compression flag
	 */
	public boolean isZipCompressionEnabled()
	{
		return enableZipCompression;
	}

	/**
	 * Loads the configuration from the specified file
	 *
	 * @param path
	 *            input file
	 */
	public void loadConfig(final String path)
	{
		config.loadConfig(path);
		applyConfig();
	}

	/**
	 * Loads the configuration file. The path of the file will be chosen by
	 * displaying a FileChooser Dialog.
	 */
	public void loadConfiguration()
	{

		XMLFileChooser fc = new XMLFileChooser();
		if (fc.showOpenDialog(new JPanel()) == XMLFileChooser.APPROVE_OPTION) {
			this.loadConfig(fc.getSelectedFile().getPath());
		}
	}

	/**
	 * Registers the panel with the given key.
	 *
	 * @param key
	 *            key
	 * @param panel
	 *            panel
	 */
	public void register(final PanelKeys key, final AbstractPanel panel)
	{
		this.components.register(key, panel);
	}

	/**
	 * Removes the specified archive from the archive registry.
	 *
	 * @param index
	 *            index of the archive
	 */
	public void removeArchive(final int index)
	{
		this.archives.removeArchive(index);
	}

	/**
	 * Repaints the GUI.
	 */
	public void repaint()
	{
		this.components.repaint();
	}

	/**
	 * Saves the configuration file. The path of the file will be chosen by
	 * displaying a FileChooser Dialog.
	 */
	public void saveConfiguration()
	{

		if (this.createConfigurationXML()) {

			XMLFileChooser fc = new XMLFileChooser();
			if (fc.showSaveDialog(new JPanel()) == XMLFileChooser.APPROVE_OPTION) {

				String path = fc.getSelectedFile().getPath();
				if (path.indexOf('.') == -1) {
					path += ".xml";
				}

				if (this.saveConfiguration(path)) {
					System.out.println("SAVE CONFIG SUCCESSFULL");
				}
				else {

					System.out.println("SAVE CONFIG FAILED");
				}
			}

		}
	}

	/**
	 * Save the configuration to a file.
	 *
	 * @param path
	 *            output path
	 * @return TRUE if the configuration was succesfully exported FALSE
	 *         otherwise
	 */
	public boolean saveConfiguration(final String path)
	{

		if (xmlConfig != null && !errors.hasFailed()) {

			boolean success = true;

			FileWriter writer = null;
			try {
				writer = new FileWriter(path);
				writer.write(xmlConfig.toString());
				writer.flush();

			}
			catch (IOException ioe) {
				ioe.printStackTrace();
				success = false;
			}
			finally {
				if (writer != null) {
					try {
						writer.close();
					}
					catch (IOException ioe) {
						success = false;
					}
				}
			}

			return success;
		}

		return false;
	}


	/**
	 * Enables or disables the 7Zip support.
	 *
	 * If the support is disabled the and the OutputCompression Mode was 7Zip
	 * the Mode will be reseted to None.
	 *
	 * @param enable7Zip
	 *            7Zip support flag
	 */
	public void setEnable7Zip(final boolean enable7Zip)
	{
		this.enable7Zip = enable7Zip;
		if (!this.enable7Zip) {
			if (outputCompression == OutputCompressionEnum.SevenZip) {
				outputCompression = OutputCompressionEnum.None;
			}
		}
	}

	/**
	 * Sets the debug output mode.
	 *
	 * @param enableDebugOutput
	 *            debug output flag
	 */
	public void setEnableDebugOutput(final boolean enableDebugOutput)
	{
		this.enableDebugOutput = enableDebugOutput;
	}

	/**
	 * Sets the diff verification mode.
	 *
	 * @param enableDiffVerification
	 *            diff verification mode
	 */
	public void setEnableDiffVerification(final boolean enableDiffVerification)
	{
		this.enableDiffVerification = enableDiffVerification;
	}

	/**
	 * Sets the encoding verification mode.
	 *
	 * @param enableEncodingVerification
	 *            diff verification mode
	 */
	public void setEnableEncodingVerification(
			final boolean enableEncodingVerification)
	{
		this.enableEncodingVerification = enableEncodingVerification;
	}

	/**
	 * Sets the database output flag.
	 *
	 * @param enableSQLDatabaseOutput
	 *            database output flag
	 */
	public void setEnableSQLDatabaseOutput(final boolean enableSQLDatabaseOutput)
	{
		this.enableSQLDatabaseOutput = enableSQLDatabaseOutput;
	}

	/**
	 * Sets the statistical output mode.
	 *
	 * @param enableStatsOutput
	 *            statistical output flag
	 */
	public void setEnableStatsOutput(final boolean enableStatsOutput)
	{
		this.enableStatsOutput = enableStatsOutput;
	}

	/**
	 * Sets the Zip-Compression mode.
	 *
	 * @param enableZipCompression
	 *            Zip-Compression flag
	 */
	public void setEnableZipCompression(final boolean enableZipCompression)
	{
		this.enableZipCompression = enableZipCompression;
	}

	/**
	 * Sets whether multiple output files should be used.
	 *
	 * @param multipleOutputFiles
	 *            multiple output files flag
	 */
	public void setMultipleOutputFiles(final boolean multipleOutputFiles)
	{
		this.multipleOutputFiles = multipleOutputFiles;
	}

	/**
	 * Sets the output compression mode.
	 *
	 * @param outputCompression
	 *            output compression mode
	 */
	public void setOutputCompression(
			final OutputCompressionEnum outputCompression)
	{
		this.outputCompression = outputCompression;
	}

	/**
	 * Sets the maximum size of an output file.
	 *
	 * @param outputFileLimit
	 *            maximum size of an output file
	 */
	public void setOutputFileLimit(final long outputFileLimit)
	{
		this.outputFileLimit = outputFileLimit;
	}

	/**
	 * Sets the reference to the component registry.
	 *
	 * @param registry
	 *            component registry
	 */
	public void setRegistry(final ComponentRegistry registry)
	{
		this.components = registry;
	}

	/**
	 * Sets the surrogate mode.
	 *
	 * @param surrogates
	 *            surrogate mode
	 */
	public void setSurrogates(final SurrogateModes surrogates)
	{
		this.surrogates = surrogates;
	}

	public boolean isEnableDataFileOutput()
	{
		return enableDataFileOutput;
	}

	public void setEnableDataFileOutput(boolean enableDataFileOutput)
	{
		this.enableDataFileOutput = enableDataFileOutput;
	}

}
