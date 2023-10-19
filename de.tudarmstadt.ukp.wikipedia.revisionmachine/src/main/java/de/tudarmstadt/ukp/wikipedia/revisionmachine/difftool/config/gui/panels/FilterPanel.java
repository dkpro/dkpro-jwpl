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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigController;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigSettings;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control.ConfigVerification;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.PanelKeys;

/**
 * Panel class of the ConfigurationTool
 *
 * This panel contains all components for setting configuration parameters
 * related to the filtering.
 */
@SuppressWarnings("serial")
public class FilterPanel
	extends AbstractPanel
{
	// table with namespaces to filter
	private JTable namespaces;

	/**
	 * (Constructor) Creates a new SurrogatePanel
	 *
	 * @param controller
	 *            Reference to the controller
	 */
	public FilterPanel(ConfigController controller)
	{
		super(controller);

		controller.register(PanelKeys.PANEL_FILTER, this);

		initTable();

		initButtons();

		// init label
		JLabel hint = new JLabel();
		hint.setText("<html>If nothing is selected,<br> all namespaces are allowed.</html>");
		hint.setBounds(385, 70, 180, 60);
		this.add(hint);
	}

	/**
	 * Initialize JTable that contains namespaces
	 */
	private void initTable()
	{
		namespaces = new JTable(new FilterTableModel());

		namespaces.removeColumn(namespaces.getColumn("#"));

		namespaces.setFillsViewportHeight(true);
		namespaces.setPreferredScrollableViewportSize(new Dimension(500, 70));

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(namespaces);

		scrollPane.setBounds(70, 10, 300, 200);
		this.add(scrollPane);
	}

	/**
	 * Initialize two buttons: SelectAll and UnselectAll
	 */
	private void initButtons()
	{
		JButton selectAll = new JButton("Select all");
		selectAll.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				for (int i = 0; i < 22; i++) {
					namespaces.getModel().setValueAt(true, i, 1);
				}

			}
		});
		selectAll.setBounds(380, 10, 120, 25);
		this.add(selectAll);

		JButton unselectAll = new JButton("Unselect all");

		unselectAll.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (int i = 0; i < 22; i++) {
					namespaces.getModel().setValueAt(false, i, 1);
				}

			}
		});

		unselectAll.setBounds(380, 40, 120, 25);
		this.add(unselectAll);
	}

	@Override
	public void validate()
	{

	}

	@Override
	public void relocate()
	{

	}

	@Override
	public void toXML(StringBuilder builder, ConfigVerification errors)
	{
		builder.append("\t<filter>\r\n");
		builder.append("\t\t<namespaces>\r\n");
		int rows = this.namespaces.getModel().getRowCount();
		for (int j = 0; j < rows; j++) {

			if (this.namespaces.getModel().getValueAt(j, 1).equals(true)) {
				builder.append("\t\t\t<ns>");
				builder.append(this.namespaces.getModel().getValueAt(j, 2));
				builder.append("</ns>\r\n");
			}

		}

		builder.append("\t\t</namespaces>\r\n");
		builder.append("\t</filter>\r\n");

	}

	@Override
	public void applyConfig(ConfigSettings config)
	{
		@SuppressWarnings("unchecked")
		Set<Integer> namespaces = (Set<Integer>) config
				.getConfigParameter(ConfigurationKeys.NAMESPACES_TO_KEEP);

		if (namespaces != null) {

			int rows = this.namespaces.getModel().getRowCount();
			for (int j = 0; j < rows; j++) {
				if (namespaces.contains((this.namespaces.getModel().getValueAt(
						j, 2)))) {
					this.namespaces.getModel().setValueAt(true, j,
							1);
				}
				else {
					this.namespaces.getModel().setValueAt(false,
							j, 1);
				}

			}

		}

	}

	/**
	 * Custom model for JTable that contains a list of namespaces to filter
	 *
	 */
	class FilterTableModel
		extends AbstractTableModel
	{
		private final String[] columnNames = { "Namespace", "Allow", "#" };

		private final Object[][] data = { { "main(0)", false, 0 },
				{ "talk(1)", false, 1 },
				{ "user(2)", false, 2 },
				{ "user talk(3)", false, 3 },
				{ "wikipedia(4)", false, 4 },
				{ "wikipedia talk(5)", false, 5 },
				{ "file(6)", false, 6 },
				{ "file talk(7)", false, 7 },
				{ "mediawiki(8)", false, 8 },
				{ "mediawiki talk(9)", false, 9 },
				{ "template(10)", false, 10 },
				{ "template talk(11)", false, 11 },
				{ "help(12)", false, 12 },
				{ "help talk(13)", false, 13 },
				{ "category(14)", false, 14 },
				{ "category talk(15)", false, 15 },
				{ "portal(100)", false, 100 },
				{ "portal talk(101)", false, 101 },
				{ "book(108)", false, 108 },
				{ "book talk(109)", false, 109 },
				{ "special(-1)", false, -1 },
				{ "media(-2)", false, -2 }

		};

		@Override
		public int getColumnCount()
		{
			return columnNames.length;
		}

		@Override
		public int getRowCount()
		{
			return data.length;
		}

		@Override
		public String getColumnName(int col)
		{
			return columnNames[col];
		}

		@Override
		public Object getValueAt(int row, int col)
		{
			return data[row][col];
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Class getColumnClass(int c)
		{
			return getValueAt(0, c).getClass();
		}

		@Override
		public boolean isCellEditable(int row, int col)
		{
			return true;
		}

		@Override
		public void setValueAt(Object value, int row, int col)
		{
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}

	}

}
