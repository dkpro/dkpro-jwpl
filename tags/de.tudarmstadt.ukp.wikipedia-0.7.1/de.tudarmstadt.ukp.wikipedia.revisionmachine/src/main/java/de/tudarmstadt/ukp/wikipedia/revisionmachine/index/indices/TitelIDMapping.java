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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Creates the TitleIndexes
 * 
 * 
 * 
 */
public class TitelIDMapping
{

	private BufferedReader reader;
	private Writer writer;
	private TitleIndex index;

	public TitelIDMapping(final String path)
		throws Exception
	{
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				path), "UTF-8"));

		writer = new OutputStreamWriter(new FileOutputStream("titleIndex.sql"),
				"UTF-8");

		index = new TitleIndex(16000000);

		writer.write("CREATE TABLE index_id_title ("
				+ "ArticleID INTEGER UNSIGNED NOT NULL, "
				+ "ArticleTitle MEDIUMTEXT NOT NULL, "
				+ "PRIMARY KEY(ArticleID));\r\n");
	}

	public void run()
		throws Exception
	{

		long count = 0;
		String t;
		String[] str;
		String line = reader.readLine();
		while (line != null) {
			str = line.split("\t");

			if (++count % 100000 == 0) {
				System.out.println("Indexing ... " + count);
			}

			index.add(Integer.parseInt(str[0]), str[1]);
			if (index.size() > 0) {
				t = index.remove().toString();
				// System.out.println(t);
				writer.write(t + "\r\n");
				writer.flush();
			}

			line = reader.readLine();
		}

		index.finalizeIndex();
		while (index.size() > 0) {
			writer.write(index.remove().toString());
			writer.flush();
		}
	}

	public void close()
		throws Exception
	{
		reader.close();
		writer.close();
	}

	public static void main(String[] args)
		throws Exception
	{
		TitelIDMapping map = new TitelIDMapping("ID_Title.log");
		map.run();
		map.close();
	}
}
