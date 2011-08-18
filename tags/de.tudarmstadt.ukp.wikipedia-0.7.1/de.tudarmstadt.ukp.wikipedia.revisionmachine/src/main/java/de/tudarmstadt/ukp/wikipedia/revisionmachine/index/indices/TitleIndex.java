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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.index.indices;

public class TitleIndex
	extends AbstractIndex
{

	public TitleIndex(final long MAX_ALLOWED_PACKET)
	{

		super("INSERT INTO index_id_title VALUES ", MAX_ALLOWED_PACKET);
	}

	public void add(final int articleID, final String articleName)
	{

		StringBuilder buf = new StringBuilder();
		buf.append("(");
		buf.append(articleID);
		buf.append(",\"");

		String name = articleName;
		name = name.replace("\'", "\\'");
		name = name.replace("\\", "\\\\");

		buf.append(name);
		buf.append("\")");

		if (buffer.length() + buf.length() + 10 >= MAX_ALLOWED_PACKET) {

			storeBuffer();
		}

		if (buffer.length() > insertStatement.length()) {
			buffer.append(",");
		}

		buffer.append(buf);
	}
}
