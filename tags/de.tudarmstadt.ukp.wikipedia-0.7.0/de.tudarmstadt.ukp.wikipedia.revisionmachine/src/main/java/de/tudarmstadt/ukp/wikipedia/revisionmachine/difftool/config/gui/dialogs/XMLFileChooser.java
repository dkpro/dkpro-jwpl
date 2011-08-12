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

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * This object represents a xml file filter.
 * 
 * 
 * 
 */
@SuppressWarnings("serial")
public class XMLFileChooser
	extends JFileChooser
{

	/**
	 * (Constructor) Creates an FileChooser with a xml file filter.
	 */
	public XMLFileChooser()
	{

		setFileFilter(new FileFilter()
		{

			@Override
			public String getDescription()
			{
				return ".xml";
			};

			@Override
			public boolean accept(final File f)
			{

				// Always accept directories
				if (f.isDirectory()) {
					return true;
				}

				int p = f.getName().indexOf(".");

				// Files need a ending
				if (p == -1) {
					return false;
				}

				// Verify the ending
				return f.getName().substring(p).equals(".xml");
			}
		});
	}
}
