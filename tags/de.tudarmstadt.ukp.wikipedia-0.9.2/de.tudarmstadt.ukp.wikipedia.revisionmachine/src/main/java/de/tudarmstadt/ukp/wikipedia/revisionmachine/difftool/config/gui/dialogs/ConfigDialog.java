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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.dialogs;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigController;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigSettings;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigVerification;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.panels.AbstractPanel;

/**
 * ConfigDialog - Displays the ConfigVerification elements.
 * 
 * 
 * 
 */
@SuppressWarnings("serial")
public class ConfigDialog
	extends JDialog
{

	/**
	 * Panel of the ConfigDialog
	 * 
	 * 
	 * 
	 */
	private class ConfigDialogPanel
		extends AbstractPanel
	{

		private JTable itemTable;
		private JScrollPane itemScrollPane;

		private JButton returnButton;
		private JButton saveButton;

		/**
		 * (Constructor) Creates the ConfigDialogPanel.
		 * 
		 * @param controller
		 *            Reference to the controller
		 */
		public ConfigDialogPanel(final ConfigController controller)
		{
			super(controller);
			createItemTable();
			createButtons();
		}

		/**
		 * Creates the buttons of the dialog panel.
		 */
		private void createButtons()
		{

			returnButton = new JButton("Return");
			returnButton.setBounds(105, 195, 120, 25);
			returnButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					close();
				}
			});

			this.add(returnButton);

			saveButton = new JButton("Save");
			saveButton.setBounds(235, 195, 120, 25);
			saveButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{

					XMLFileChooser fc = new XMLFileChooser();
					if (fc.showSaveDialog(new JPanel()) == XMLFileChooser.APPROVE_OPTION) {

						String path = fc.getSelectedFile().getPath();
						if (path.indexOf('.') == -1) {
							path += ".xml";
						}

						if (controller.saveConfiguration(path)) {
							System.out.println("SAVE CONFIG SUCCESSFULL");
						}
						else {
							System.out.println("SAVE CONFIG FAILED");
						}
					}
				}
			});

			this.add(saveButton);
		}

		/**
		 * Creates the JTable for displaying the input archives.
		 */
		private void createItemTable()
		{
			itemTable = new JTable(controller.getConfigErrors());
			itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			itemScrollPane = new JScrollPane(itemTable);
			itemScrollPane.setBounds(10, 10, 470, 180);

			this.add(itemScrollPane);
		}

		/**
		 * empty method
		 */
		@Override
		public void relocate()
		{

		}

		/**
		 * A call of this method should validate the positions of the panels
		 * components.
		 */
		@Override
		public void validate()
		{

			ConfigVerification verification = controller.getConfigErrors();
			if (verification != null) {
				saveButton.setEnabled(!verification.hasFailed());
			}
			else {
				saveButton.setEnabled(false);
			}
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
	}

	/**
	 * (Constructor) Creates a new ConfigDialog.
	 * 
	 * @param controller
	 *            Reference to the controller
	 */
	public ConfigDialog(final ConfigController controller)
	{
		super(controller.getRegistry().getGUI(), true);

		this.setTitle("Verification");

		setSize(500, 250);
		setResizable(false);

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((d.width - getSize().width) / 2,
				(d.height - getSize().height) / 2);

		this.setContentPane(new ConfigDialogPanel(controller));
	}

	/**
	 * Closes the dialog.
	 */
	public void close()
	{
		this.setVisible(true);
		this.dispose();
	}
}
