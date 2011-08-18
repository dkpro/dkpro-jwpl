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
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.archive.ArchiveDescription;

/**
 * ArchiveRegistry of the ConfigurationTool.
 * 
 * Contains all input archives and represents the table model to display the
 * archives in the InputPanel.
 * 
 * 
 * 
 */
@SuppressWarnings("serial")
public class ArchiveRegistry
	extends AbstractTableModel
{

	/** List of input archives */
	private final List<ArchiveDescription> archives;

	/** Name of columns */
	private final String[] columnNames;

	/**
	 * (Constructor) Creates a new ArchiveRegistry.
	 */
	public ArchiveRegistry()
	{
		this.columnNames = new String[] { "Input Type", "Start Position",
				"Path" };
		this.archives = new ArrayList<ArchiveDescription>();
	}

	/**
	 * Returns the name of the specified column.
	 * 
	 * @param col
	 *            index of the column
	 * 
	 * @return name of the column
	 */
	@Override
	public String getColumnName(final int col)
	{
		return this.columnNames[col];
	}

	/**
	 * Returns the number of columns.
	 */
	@Override
	public int getColumnCount()
	{
		return this.columnNames.length;
	}

	/**
	 * Returns the number of rows.
	 */
	@Override
	public int getRowCount()
	{
		return this.archives.size();
	}

	/**
	 * Returns the value at the specified position.
	 * 
	 * @param row
	 *            index of the row
	 * @param col
	 *            index of the column
	 * 
	 * @return string representation of the specified field
	 */
	@Override
	public Object getValueAt(final int row, final int col)
	{

		switch (col) {
		case 0:
			return archives.get(row).getType();
		case 1:
			return archives.get(row).getStartPosition();
		case 2:
			return archives.get(row).getPath();
		}

		return "---";
	}

	/**
	 * Adds an archive description.
	 * 
	 * @param description
	 *            archive description
	 */
	public void addArchive(final ArchiveDescription description)
	{
		this.archives.add(description);
	}

	/**
	 * Removes an archive description.
	 * 
	 * @param index
	 *            index of the archive.
	 */
	public void removeArchive(final int index)
	{
		this.archives.remove(index);
	}

	/**
	 * Returns the archive at the specified position.
	 * 
	 * @param index
	 *            position
	 * @return ArchiveDescription
	 */
	public ArchiveDescription get(final int index)
	{
		return this.archives.get(index);
	}

	/**
	 * Deletes all contained archive descriptions.
	 */
	public void clear()
	{
		this.archives.clear();
	}

	/**
	 * Adds the ArchiveDescriptions contained in the configuration.
	 * 
	 * @param config
	 *            Reference to the configuration
	 */
	public void applyConfiguration(final ConfigSettings config)
	{

		clear();

		Iterator<ArchiveDescription> aIt = config.archiveIterator();
		while (aIt.hasNext()) {
			addArchive(aIt.next());
		}
	}
}
