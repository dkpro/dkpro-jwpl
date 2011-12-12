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
					namespaces.getModel().setValueAt(new Boolean(true), i, 1);
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
					namespaces.getModel().setValueAt(new Boolean(false), i, 1);
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
					this.namespaces.getModel().setValueAt(new Boolean(true), j,
							1);
				}
				else {
					this.namespaces.getModel().setValueAt(new Boolean(false),
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

		private final Object[][] data = { { "main(0)", new Boolean(false), 0 },
				{ "talk(1)", new Boolean(false), 1 },
				{ "user(2)", new Boolean(false), 2 },
				{ "user talk(3)", new Boolean(false), 3 },
				{ "wikipedia(4)", new Boolean(false), 4 },
				{ "wikipedia talk(5)", new Boolean(false), 5 },
				{ "file(6)", new Boolean(false), 6 },
				{ "file talk(7)", new Boolean(false), 7 },
				{ "mediawiki(8)", new Boolean(false), 8 },
				{ "mediawiki talk(9)", new Boolean(false), 9 },
				{ "template(10)", new Boolean(false), 10 },
				{ "template talk(11)", new Boolean(false), 11 },
				{ "help(12)", new Boolean(false), 12 },
				{ "help talk(13)", new Boolean(false), 13 },
				{ "category(14)", new Boolean(false), 14 },
				{ "category talk(15)", new Boolean(false), 15 },
				{ "portal(100)", new Boolean(false), 100 },
				{ "portal talk(101)", new Boolean(false), 101 },
				{ "book(108)", new Boolean(false), 108 },
				{ "book talk(109)", new Boolean(false), 109 },
				{ "special(-1)", new Boolean(false), -1 },
				{ "media(-2)", new Boolean(false), -2 }

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
