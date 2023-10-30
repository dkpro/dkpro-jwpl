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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigController;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigSettings;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.control.ConfigVerification;
import org.dkpro.jwpl.revisionmachine.difftool.config.gui.panels.AbstractPanel;
import org.dkpro.jwpl.revisionmachine.difftool.data.archive.ArchiveDescription;
import org.dkpro.jwpl.revisionmachine.difftool.data.archive.InputType;

/**
 * InputDialog - Dialog to specify input archives.
 */
@SuppressWarnings("serial")
public class InputDialog
        extends JDialog {

  /**
   * Panel of the InputDialog
   */
  private class InputDialogPanel
          extends AbstractPanel {

    /**
     * (Constructor) Creates the InputDialogPanel.
     *
     * @param controller Reference to the controller
     */
    public InputDialogPanel(final ConfigController controller) {
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
    private JComboBox<InputType> typeChooser;

    private JLabel startLabel;
    private JTextField startPosition;

    private JButton addButton;
    private JButton cancelButton;

    /**
     * Creates the path input components.
     */
    private void createPathSettings() {
      pathLabel = new JLabel("Please enter the path: ");
      pathLabel.setBounds(10, 10, 150, 25);
      this.add(pathLabel);

      pathField = new JTextField();
      pathField.setBounds(10, 40, 250, 25);
      this.add(pathField);

      searchButton = new JButton("Search");
      searchButton.setBounds(180, 10, 80, 25);

      searchButton.addActionListener(e -> {

        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(new JPanel()) == JFileChooser.APPROVE_OPTION) {
          pathField.setText(fc.getSelectedFile().getPath());
        }
      });

      this.add(searchButton);
    }

    /**
     * Creates the start input components.
     */
    private void createStartLabel() {

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
    private void createTypeChooser() {

      typeLabel = new JLabel("Input type: ");
      typeLabel.setBounds(10, 80, 130, 25);
      this.add(typeLabel);

      typeChooser = new JComboBox<>();
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
    private void createButtons() {
      addButton = new JButton("Add");
      addButton.setBounds(10, 170, 120, 25);
      addButton.addActionListener(e -> {
        String path = pathField.getText();
        if (path.length() == 0) {
          return;
        }

        InputType type = (InputType) typeChooser.getSelectedItem();

        controller.addArchive(new ArchiveDescription(type, path));
        controller.repaint();

        close();
      });
      this.add(addButton);

      cancelButton = new JButton("Cancel");
      cancelButton.setBounds(140, 170, 120, 25);
      cancelButton.addActionListener(e -> close());

      this.add(cancelButton);
    }

    /**
     * empty method
     */
    @Override
    public void validate() {

    }

    /**
     * A call of this method should validate the positions of the panels
     * components.
     */
    @Override
    public void relocate() {

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
     * @throws UnsupportedOperationException
     * @deprecated
     */
    @Deprecated
    @Override
    public void toXML(final StringBuilder builder,
                      final ConfigVerification errors) {
      throw new UnsupportedOperationException();
    }

    /**
     * empty method
     *
     * @throws UnsupportedOperationException
     * @deprecated
     */
    @Deprecated
    @Override
    public void applyConfig(final ConfigSettings config) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * (Constructor) Creates a new InputDialog.
   *
   * @param controller Reference to the controller
   */
  public InputDialog(final ConfigController controller) {
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
  public void close() {
    this.setVisible(true);
    this.dispose();
  }
}
