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
package org.dkpro.jwpl.revisionmachine.difftool.config.gui.dialogs;

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

import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigController;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigVerification;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.panels.AbstractPanel;

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
