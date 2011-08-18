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
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ArchiveRegistry;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigController;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigSettings;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigVerification;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.ConfigErrorKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.ConfigItem;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.ConfigItemTypes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.PanelKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.dialogs.InputDialog;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.SurrogateModes;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive.ArchiveDescription;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive.InputType;

/**
 * Panel class of the ConfigurationTool
 *
 * This panel contains all components for setting configuration parameters
 * related to the input data.
 *
 *
 *
 */
@SuppressWarnings("serial")
public class InputPanel
	extends AbstractPanel
{

	/**
	 * Subpanel of the InputPanel
	 *
	 * Contains the settings related to the surrogate mode
	 *
	 *
	 *
	 */
	private class SurrogatePanel
		extends AbstractPanel
	{

		private JLabel surrogateLabel;
		private JRadioButton replaceSurrogatesRadioButton;
		private JRadioButton faultySurrogatesRadioButton;
		private JRadioButton discardSurrogatesRevisionRadioButton;
		private JRadioButton discardSurrogatesArticleRadioButton;

		/**
		 * (Constructor) Creates a new SurrogatePanel
		 *
		 * @param controller
		 *            Reference to the controller
		 */
		public SurrogatePanel(final ConfigController controller)
		{
			super(controller);
			createButtons();
		}

		private void createButtons()
		{
			surrogateLabel = new JLabel("Surrogate Characters");
			surrogateLabel.setBounds(10, 10, 130, 25);
			this.add(surrogateLabel);

			/*
			 * DEFAULT MODE
			 */
			discardSurrogatesRevisionRadioButton = new JRadioButton(
					"Discard revision");
			discardSurrogatesRevisionRadioButton.setBounds(10, 90, 120, 25);

			discardSurrogatesRevisionRadioButton
					.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(final ActionEvent e)
						{

							if (controller.getSurrogates() != SurrogateModes.DISCARD_REVISION) {
								controller
										.setSurrogates(SurrogateModes.DISCARD_REVISION);
							}

							validateSurrogateSettings();
						}
					});

			// pre-activate default mode
			discardSurrogatesRevisionRadioButton.setSelected(true);

			this.add(discardSurrogatesRevisionRadioButton);

			/*
			 * REPLACE-Mode
			 */

			replaceSurrogatesRadioButton = new JRadioButton("Replace them");
			replaceSurrogatesRadioButton.setBounds(10, 40, 120, 25);

			replaceSurrogatesRadioButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{

					if (controller.getSurrogates() != SurrogateModes.REPLACE) {
						controller.setSurrogates(SurrogateModes.REPLACE);
					}

					validateSurrogateSettings();
				}
			});
			this.add(replaceSurrogatesRadioButton);

			/*
			 * THROW_ERROR-Mode
			 */

			faultySurrogatesRadioButton = new JRadioButton("Throw an error");
			faultySurrogatesRadioButton.setBounds(10, 65, 120, 25);

			faultySurrogatesRadioButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{

					if (controller.getSurrogates() != SurrogateModes.THROW_ERROR) {
						controller.setSurrogates(SurrogateModes.THROW_ERROR);
					}

					validateSurrogateSettings();
				}
			});
			this.add(faultySurrogatesRadioButton);

			/*
			 * DISCARD_REST-Mode
			 */

			discardSurrogatesArticleRadioButton = new JRadioButton(
					"Discard rest");
			discardSurrogatesArticleRadioButton.setBounds(10, 115, 120, 25);

			discardSurrogatesArticleRadioButton
					.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(final ActionEvent e)
						{

							if (controller.getSurrogates() != SurrogateModes.DISCARD_REST) {
								controller
										.setSurrogates(SurrogateModes.DISCARD_REST);
							}

							validateSurrogateSettings();
						}
					});
			this.add(discardSurrogatesArticleRadioButton);

		}

		/**
		 * A call of this method should validate the status of the panels
		 * components.
		 */
		@Override
		public void validate()
		{
			validateSurrogateSettings();
		}

		/**
		 * Validates the surrogate settings.
		 */
		private void validateSurrogateSettings()
		{


		/*
		 * TODO Uncomment this code as soon as the surrogate modes are reactivated
		 */

//			SurrogateModes sur = controller.getSurrogates();
//
//			replaceSurrogatesRadioButton
//					.setSelected(sur == SurrogateModes.REPLACE);
//			faultySurrogatesRadioButton
//					.setSelected(sur == SurrogateModes.THROW_ERROR);
//			discardSurrogatesRevisionRadioButton
//					.setSelected(sur == SurrogateModes.DISCARD_REVISION);
//			discardSurrogatesArticleRadioButton
//					.setSelected(sur == SurrogateModes.DISCARD_REST);

			/*
			 * DEACTIVATE UNSUPPORTED MODES
			 * TODO: remove config options for
			 * unsupported surrogates mode. Can be activated again as soon as
			 * the implementation of these modes have been checked.
			 * Then also uncomment the original code above.
			 */
			//BEGIN WORK AROUND FOR DEACTIVATED SURROGATE MODES
			faultySurrogatesRadioButton.setEnabled(false);
			discardSurrogatesArticleRadioButton.setEnabled(false);
			replaceSurrogatesRadioButton.setEnabled(false);
			discardSurrogatesRevisionRadioButton.setSelected(true);
			//END WORK AROUND FOR DEACTIVATED SURROGATE MODES


		}

		/**
		 * A call of this method should validate the positions of the panels
		 * components.
		 */
		@Override
		public void relocate()
		{

			int w = 120, h = 130;
			int x = (this.getWidth() - w) / 2, y = (this.getHeight() - h) / 2;

			surrogateLabel.setLocation(10, 10);
			faultySurrogatesRadioButton.setLocation(x, y + 55);
			replaceSurrogatesRadioButton.setLocation(x, y + 30);
			discardSurrogatesRevisionRadioButton.setLocation(x, y + 80);
			discardSurrogatesArticleRadioButton.setLocation(x, y + 105);
		}

		/**
		 * empty method
		 *
		 * @deprecated
		 * @throws UnsupportedOperationException
		 */
		@Deprecated
		@Override
		public void applyConfig(final ConfigSettings config)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * empty method
		 *
		 * @deprecated
		 * @throws UnsupportedOperationException
		 */
		@Deprecated
		@Override
		public void toXML(final StringBuilder builder,
				final ConfigVerification errors)
		{
			throw new UnsupportedOperationException();
		}
	}

	// --------------------------------------------------------------------------//
	// FIELDS & CONSTRUCTORS //
	// --------------------------------------------------------------------------//

	private JTable archiveTable;
	private JScrollPane archiveScrollPane;

	private JButton addArchiveButton;
	private JButton removeArchiveButton;

	private JLabel encodingLabel;
	private JTextField encodingField;

	private SurrogatePanel surrogatePanel;

	/**
	 * (Constructor) Creates a new InputPanel.
	 *
	 * @param controller
	 *            Reference to the controller
	 */
	public InputPanel(final ConfigController controller)
	{

		super(controller);
		controller.register(PanelKeys.PANEL_INPUT, this);

		createArchiveTable();
		createControllButtons();
		createEncodingSettings();
		createSurrogateSettings();
	}

	// --------------------------------------------------------------------------//
	// CONSTRUCTION METHODS //
	// --------------------------------------------------------------------------//

	private void createArchiveTable()
	{
		archiveTable = new JTable(controller.getArchives());
		archiveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		archiveScrollPane = new JScrollPane(archiveTable);
		archiveScrollPane.setBounds(10, 10, 410, 210);

		this.add(archiveScrollPane);
	}

	private void createControllButtons()
	{
		addArchiveButton = new JButton("Add");
		addArchiveButton.setBounds(445, 20, 100, 25);

		addArchiveButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{

				new InputDialog(controller).setVisible(true);
				repaint();
			}
		});

		this.add(addArchiveButton);

		removeArchiveButton = new JButton("Remove");
		removeArchiveButton.setBounds(445, 50, 100, 25);

		removeArchiveButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{

				controller.removeArchive(archiveTable.getSelectedRow());
				archiveTable.revalidate();
				repaint();
			}
		});

		this.add(removeArchiveButton);
	}

	private void createEncodingSettings()
	{
		encodingLabel = new JLabel("Wikipedia Character Encoding: ");
		encodingLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		encodingLabel.setBounds(10, 230, 200, 25);
		this.add(encodingLabel);

		encodingField = new JTextField();
		encodingField.setBounds(220, 230, 200, 25);
		this.add(encodingField);
	}

	private void createSurrogateSettings()
	{
		surrogatePanel = new SurrogatePanel(controller);
		surrogatePanel.setBorder(BorderFactory.createLoweredBevelBorder());
		surrogatePanel.setBounds(425, 95, 140, 160);
		this.add(surrogatePanel);
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

		this.archiveTable.revalidate();
		this.surrogatePanel.validate();
	}

	/**
	 * A call of this method should validate the positions of the panels
	 * components.
	 */
	@Override
	public void relocate()
	{

		int w = 555, h = 235;
		int x = (this.getWidth() - w) / 2, y = (this.getHeight() - h) / 2;

		archiveScrollPane.setLocation(x, y);

		addArchiveButton.setLocation(x + 435, y + 10);
		removeArchiveButton.setLocation(x + 435, y + 40);

		encodingLabel.setLocation(x, y + 220);
		encodingField.setLocation(x + 210, y + 220);

		surrogatePanel.setLocation(x + 415, y + 85);

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
				.getConfigParameter(ConfigurationKeys.WIKIPEDIA_ENCODING);
		if (o != null) {
			encodingField.setText((String) o);
		}
		else {
			encodingField.setText("");
		}

		o = config.getConfigParameter(ConfigurationKeys.MODE_SURROGATES);
		if (o != null) {
			controller.setSurrogates((SurrogateModes) o);
		}
		else {
			controller.setSurrogates(SurrogateModes.DISCARD_REVISION);
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

		SurrogateModes surMode = controller.getSurrogates();

		String wikiEncoding = encodingField.getText();
		if (wikiEncoding.length() == 0) {

			errors.add(new ConfigItem(ConfigItemTypes.WARNING,
					ConfigErrorKeys.MISSING_VALUE,
					"The CharacterEncoding was not set."));
		}

		builder.append("\t<input>\r\n");
		builder.append("\t\t<MODE_SURROGATES>" + surMode
				+ "</MODE_SURROGATES>\r\n");
		builder.append("\t\t<WIKIPEDIA_ENCODING>" + wikiEncoding
				+ "</WIKIPEDIA_ENCODING>\r\n");

		ArchiveRegistry reg = controller.getArchives();

		int size = reg.getRowCount();

		ArchiveDescription archive;
		InputType type;
		String archivePath;
		long start;

		if(size==0){
			errors.add(new ConfigItem(ConfigItemTypes.WARNING,
					ConfigErrorKeys.MISSING_VALUE,
			"No source file has been set."));
		}

		for (int i = 0; i < size; i++) {

			archive = reg.get(i);

			type = archive.getType();
			switch (type) {
			case XML:
				break;
			case BZIP2:
				//bzip is always enabled - nothing to check here
				break;
			case SEVENZIP:
				if (!controller.is7ZipEnabled()) {
					errors.add(new ConfigItem(ConfigItemTypes.ERROR,
							ConfigErrorKeys.ILLEGAL_INPUT_FILE,
							"The SevenUip mode is not " + "activated"));
				}
				break;
			}

			archivePath = archive.getPath();
			if (archivePath.length() == 0) {
				errors.add(new ConfigItem(ConfigItemTypes.ERROR,
						ConfigErrorKeys.PATH_NOT_SET,
						"The archive path is missing"));
			}

			start = archive.getStartPosition();
			if (start < 0) {
				errors.add(new ConfigItem(ConfigItemTypes.ERROR,
						ConfigErrorKeys.VALUE_OUT_OF_RANGE,
						"The archive start value should be at least 0"));
			}

			builder.append("\t\t<archive>\r\n");
			builder.append("\t\t\t<type>" + type + "</type>\r\n");
			builder.append("\t\t\t<path>\"" + archivePath + "\"</path>\r\n");
			builder.append("\t\t\t<start>" + start + "</start>\r\n");
			builder.append("\t\t</archive>\r\n");
		}
		builder.append("\t</input>\r\n");
	}
}
