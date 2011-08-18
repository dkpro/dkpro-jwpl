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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.ConfigItem;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.ConfigItemTypes;

/**
 * This class contains the list of error or warning messages that have been
 * generated during the verification of the configuration settings.
 * 
 * 
 * 
 */
@SuppressWarnings("serial")
public class ConfigVerification
	extends AbstractTableModel
{

	/** If an error message was added to the list. */
	private boolean failed;

	/** List of configuration items */
	private final List<ConfigItem> list;

	/** Column names of the table representation */
	private final String[] columnNames;

	/**
	 * (Constructor) Creates an empty ConfigVerification object.
	 */
	public ConfigVerification()
	{
		this.list = new ArrayList<ConfigItem>();
		this.failed = false;

		this.columnNames = new String[] { "Type", "Error", "Message" };
	}

	/**
	 * Adds a configuration item to the list.
	 * 
	 * @param item
	 *            configuration item
	 */
	public void add(final ConfigItem item)
	{
		failed = failed || item.getType() == ConfigItemTypes.ERROR;
		this.list.add(item);
	}

	/**
	 * Returns the name of the column with the index col.
	 * 
	 * @return column name of the specified column.
	 */
	@Override
	public String getColumnName(final int col)
	{
		return this.columnNames[col];
	}

	/**
	 * Returns the number of columns.
	 * 
	 * @return number of columns
	 */
	@Override
	public int getColumnCount()
	{
		return 3;
	}

	/**
	 * Returns the number of rows.
	 * 
	 * @return number of rows
	 */
	@Override
	public int getRowCount()
	{
		return list.size();
	}

	/**
	 * Returns the value at the specified column of the specified row.
	 * 
	 * @return value
	 */
	@Override
	public Object getValueAt(final int row, final int column)
	{

		ConfigItem item = this.list.get(row);

		switch (column) {
		case 0:
			return item.getType();
		case 1:
			return item.getKey();
		case 2:
			return item.getMessage();
		}
		return null;
	}

	/**
	 * Returns whether the configuration item list contains an error message or
	 * not.
	 * 
	 * @return TRUE | FALSE
	 */
	public boolean hasFailed()
	{
		return this.failed;
	}
}
