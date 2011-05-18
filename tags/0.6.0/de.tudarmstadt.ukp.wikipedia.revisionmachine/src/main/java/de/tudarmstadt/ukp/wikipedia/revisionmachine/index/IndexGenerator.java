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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.index;

import java.util.Iterator;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionAPIConfiguration;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.Time;

/**
 * Generates the indices for the database.
 *
 *
 *
 */
public class IndexGenerator
{

	/** Reference to the configuration */
	private final RevisionAPIConfiguration config;

	/**
	 * (Constructor) Creates a new IndexGenerator object.
	 *
	 * @param config
	 *            Reference to the configuration
	 */
	public IndexGenerator(final RevisionAPIConfiguration config)
	{
		this.config = config;
	}

	/**
	 * Starts the generation of the indices.
	 *
	 * @throws WikiApiException
	 *             if an error occurs
	 */
	public void generate()
		throws WikiApiException
	{
		Indexer data = null;
		try {
			data = new Indexer(config);

			System.out.println("GENERATING INDEX STARTED");

			long bufferSize = config.getBufferSize();
			Revision rev;
			long count = 0;
			long last = 0, now, start = System.currentTimeMillis();

			Iterator<Revision> it = new IndexIterator(config);
			while (it.hasNext()) {

				if (++count % bufferSize == 0) {
					now = System.currentTimeMillis() - start;
					System.out.println(Time.toClock(now) + "\t" + (now - last)
							+ "\tINDEXING " + count);
					last = now;
				}

				rev = it.next();
				data.index(rev);
			}

			System.out.println("GENERATING INDEX ENDED + ("
					+ Time.toClock(System.currentTimeMillis() - start) + ")");

		}
		catch (Exception e) {

			throw new WikiApiException(e);

		}
		finally {
			if (data != null) {
				data.close();
			}
		}
	}

	public static void main(String[] args)
	{

		RevisionAPIConfiguration config = new RevisionAPIConfiguration();

		config.setHost("bender.tk.informatik.tu-darmstadt.de");
		config.setDatabase("wiki_simple_20110406_rev");
		config.setUser("student");
		config.setPassword("student");

		config.setCharacterSet("UTF-8");
		config.setBufferSize(15000);
		config.setMaxAllowedPacket(16 * 1024 * 1023);

		config.setOutputPath("/home/oferschke/NLP/Resources/wiki_data/simplewiki-20110406/revisions/revisionIndex.sql");
//		config.setOutputType(OutputTypes.DATABASE);

		try {
			new IndexGenerator(config).generate();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("TERMINATED");
	}
}
