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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigController;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigSettings;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigVerification;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.panels.AbstractPanel;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive.ArchiveDescription;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive.InputType;

/**
 * InputDialog - Dialog to specify input archives.
 *
 *
 *
 */
@SuppressWarnings("serial")
public class InputDialog
	extends JDialog
{

	/**
	 * Panel of the InputDialog
	 *
	 *
	 *
	 */
	private class InputDialogPanel
		extends AbstractPanel
	{

		/**
		 * (Constructor) Creates the InputDialogPanel.
		 *
		 * @param controller
		 *            Reference to the controller
		 */
		public InputDialogPanel(final ConfigController controller)
		{
			super(controller);
			createPathSettings();
			createTypeChooser();
			createButtons();
			createStartLabel();
		}

		private JLabel pathLabel;
		private JTextField pathField;
		private JButton searchButton;

		private JLabel typeLabel;
		private JComboBox typeChooser;

		private JLabel startLabel;
		private JTextField startPosition;

		private JButton addButton;
		private JButton cancelButton;

		/**
		 * Creates the path input components.
		 */
		private void createPathSettings()
		{
			pathLabel = new JLabel("Please enter the path: ");
			pathLabel.setBounds(10, 10, 150, 25);
			this.add(pathLabel);

			pathField = new JTextField();
			pathField.setBounds(10, 40, 250, 25);
			this.add(pathField);

			searchButton = new JButton("Search");
			searchButton.setBounds(180, 10, 80, 25);

			searchButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{

					JFileChooser fc = new JFileChooser();
					if (fc.showOpenDialog(new JPanel()) == JFileChooser.APPROVE_OPTION) {
						pathField.setText(fc.getSelectedFile().getPath());
					}
				}
			});

			this.add(searchButton);
		}

		/**
		 * Creates the start input components.
		 */
		private void createStartLabel()
		{

			startLabel = new JLabel("Ignore all bytes before:");
			startLabel.setBounds(10, 120, 130, 25);
			this.add(startLabel);

			startPosition = new JTextField();
			startPosition.setBounds(150, 120, 110, 25);
			this.add(startPosition);
		}

		/**
		 * Creates the input type chooser.
		 */
		private void createTypeChooser()
		{

			typeLabel = new JLabel("Input type: ");
			typeLabel.setBounds(10, 80, 130, 25);
			this.add(typeLabel);

			typeChooser = new JComboBox();
			typeChooser.setBounds(150, 80, 110, 25);

			typeChooser.addItem(InputType.XML);

			if (this.controller.is7ZipEnabled()) {
				typeChooser.addItem(InputType.SEVENZIP);
			}

			typeChooser.addItem(InputType.BZIP2);

			this.add(typeChooser);
		}

		/**
		 * Creates the buttons of the dialog panel.
		 */
		private void createButtons()
		{
			addButton = new JButton("Add");
			addButton.setBounds(10, 170, 120, 25);
			addButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					String path = pathField.getText();
					if (path.length() == 0) {
						return;
					}

					InputType type = (InputType) typeChooser.getSelectedItem();

					controller.addArchive(new ArchiveDescription(type, path));
					controller.repaint();

					close();
				}
			});
			this.add(addButton);

			cancelButton = new JButton("Cancel");
			cancelButton.setBounds(140, 170, 120, 25);
			cancelButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					close();
				}
			});

			this.add(cancelButton);
		}

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

			int w = 250, h = 185;
			int x = (this.getWidth() - w) / 2, y = (this.getHeight() - h) / 2;

			pathLabel.setLocation(x, y);
			pathField.setLocation(x, y + 30);
			searchButton.setLocation(x + 170, y);

			typeLabel.setLocation(x, y + 70);
			typeChooser.setLocation(x + 140, y + 70);

			startLabel.setLocation(x, y + 110);
			startPosition.setLocation(x + 140, y + 110);

			addButton.setLocation(x, y + 160);
			cancelButton.setLocation(x + 130, y + 160);
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
	 * (Constructor) Creates a new InputDialog.
	 *
	 * @param controller
	 *            Reference to the controller
	 */
	public InputDialog(final ConfigController controller)
	{
		super(controller.getRegistry().getGUI(), true);

		this.setTitle("Add an input file");

		setSize(300, 250);
		setResizable(false);

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((d.width - getSize().width) / 2,
				(d.height - getSize().height) / 2);

		this.setContentPane(new InputDialogPanel(controller));
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
