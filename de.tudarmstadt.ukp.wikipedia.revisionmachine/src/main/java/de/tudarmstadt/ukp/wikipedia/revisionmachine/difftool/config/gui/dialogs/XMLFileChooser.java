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
