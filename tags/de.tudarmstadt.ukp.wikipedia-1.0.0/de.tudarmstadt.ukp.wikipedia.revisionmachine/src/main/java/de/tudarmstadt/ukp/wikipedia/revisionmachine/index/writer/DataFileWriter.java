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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.index.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionAPIConfiguration;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.index.indices.AbstractIndex;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.index.indices.ArticleIndex;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.index.indices.ChronoIndex;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.index.indices.RevisionIndex;

/**
 * This class writes the output of the index generator to an sql file.
 *
 *
 *
 */
public class DataFileWriter
	implements IndexWriterInterface
{

	/** Reference to the Writer object */
	private final Writer chronoIdxWriter;
	private final Writer revisionIdxWriter;
	private final Writer articleIdxWriter;

	/**
	 * (Constructor) Creates a new SQLFileWriter.
	 *
	 * @param config
	 *            Reference to the configuration paramters
	 * @throws IOException
	 *             if an error occured while writing the file
	 */
	public DataFileWriter(final RevisionAPIConfiguration config)
		throws IOException
	{

		File path=new File(config.getOutputPath());
		chronoIdxWriter = new BufferedWriter(new FileWriter(new File(path,"chronoIndex.csv")));
		revisionIdxWriter = new BufferedWriter(new FileWriter(new File(path,"revisionIndex.csv")));
		articleIdxWriter = new BufferedWriter(new FileWriter(new File(path,"articleIndex.csv")));

//		writer.write("CREATE TABLE index_articleID_rc_ts ("
//				+ "ArticleID INTEGER UNSIGNED NOT NULL, "
//				+ "FullRevisionPKs MEDIUMTEXT NOT NULL, "
//				+ "RevisionCounter MEDIUMTEXT NOT NULL, "
//				+ "FirstAppearance BIGINT NOT NULL, "
//				+ "LastAppearance BIGINT NOT NULL, "
//				+ "PRIMARY KEY(ArticleID));");
//
//		writer.write("CREATE TABLE index_revisionID ("
//				+ "RevisionID INTEGER UNSIGNED NOT NULL, "
//				+ "RevisionPK INTEGER UNSIGNED NOT NULL, "
//				+ "FullRevisionPK INTEGER UNSIGNED NOT NULL, "
//				+ "PRIMARY KEY(RevisionID));");
//
//		writer.write("CREATE TABLE index_chronological ("
//				+ "ArticleID INTEGER UNSIGNED NOT NULL, "
//				+ "Mapping MEDIUMTEXT NOT NULL, "
//				+ "ReverseMapping MEDIUMTEXT NOT NULL, "
//				+ "PRIMARY KEY(ArticleID));");
//		writer.write("\r\n");
//
//		//disable keys now - reenable at the end of the sql file
//		writer.write("ALTER TABLE index_articleID_rc_ts DISABLE KEYS;\r\n");
//		writer.write("ALTER TABLE index_revisionID DISABLE KEYS;\r\n");
//		writer.write("ALTER TABLE index_chronological DISABLE KEYS;\r\n");
//
//		writer.flush();
	}

	/**
	 * Writes the buffered finalzed queries to the output.
	 *
	 * @param index
	 *            Reference to an index
	 * @throws IOException
	 *             if an error occured while writing the output
	 */
	public void write(final AbstractIndex index)
		throws IOException
	{

		StringBuilder cmd;

		while (index.size() > 0) {

			System.out.println("Transmit Index [" + index + "]");

			cmd = index.remove();

			if(index instanceof ArticleIndex){
				articleIdxWriter.write(cmd.toString());
			}else if(index instanceof ChronoIndex){
				chronoIdxWriter.write(cmd.toString());
			}else if(index instanceof RevisionIndex){
				revisionIdxWriter.write(cmd.toString());
			}

		}

		if(index instanceof ArticleIndex){
			articleIdxWriter.flush();
		}else if(index instanceof ChronoIndex){
			chronoIdxWriter.flush();
		}else if(index instanceof RevisionIndex){
			revisionIdxWriter.flush();
		}
	}

	/**
	 * Closes the file or the database connection.
	 *
	 * @throws IOException
	 *             if an error occured while closing the file
	 */
	public void close()
		throws IOException
	{
		articleIdxWriter.close();
		chronoIdxWriter.close();
		revisionIdxWriter.close();
	}

	/**
	 * Wraps up the index generation process and writes all remaining statements
	 * e.g. concerning UNCOMPRESSED-Indexes on the created tables.
	 *
	 * @throws SQLException
	 *             if an error occured while writing to the file
	 */
	public void finish() throws IOException{
		articleIdxWriter.flush();
		chronoIdxWriter.flush();
		revisionIdxWriter.flush();
	}
}
