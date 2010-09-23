/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.timemachine.domain;

import java.io.File;
import java.sql.Timestamp;

import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.Files;
import de.tudarmstadt.ukp.wikipedia.wikimachine.util.TimestampUtil;

public class TimeMachineFiles extends Files {

	private static final String NO_CATEGORYLINKS = "category links file not found";
	private static final String NO_METAHISTORY = "meta history file not found";
	private static final String NO_PAGELINKS = "page links file not found";

	private String metaHistoryFile;
	private String pageLinksFile;
	private String categoryLinksFile;
	private String timeStamp = "";

	public TimeMachineFiles(ILogger logger) {
		super(logger);
	}

	public TimeMachineFiles(TimeMachineFiles files) {
		super(files);
		this.metaHistoryFile = files.metaHistoryFile;
		this.pageLinksFile = files.metaHistoryFile;
		this.categoryLinksFile = files.categoryLinksFile;
	}

	/**
	 * Add a sub directory called "timestamp" to the current output directory
	 * 
	 * @param timestamp
	 *            - name of a new sub directory
	 */
	public void setTimestamp(Timestamp timestamp) {

		timeStamp = TimestampUtil.toMediaWikiString(timestamp) + File.separator;
	}

	public String getMetaHistoryFile() {
		return metaHistoryFile;
	}

	public void setMetaHistoryFile(String metaHistroyFile) {
		this.metaHistoryFile = metaHistroyFile;
	}

	public String getPageLinksFile() {
		return pageLinksFile;
	}

	public void setPageLinksFile(String pageLinksFile) {
		this.pageLinksFile = pageLinksFile;
	}

	public String getCategoryLinksFile() {
		return categoryLinksFile;
	}

	public void setCategoryLinksFile(String categoryLinksFile) {
		this.categoryLinksFile = categoryLinksFile;
	}

	public boolean checkInputFile(String fileName, String errorMessage) {
		File inputFile = new File(fileName);
		boolean result = inputFile.exists() && inputFile.canRead();
		if (!result) {
			logger.log(errorMessage);
		}
		return result;
	}

	@Override
	protected String getOutputPath(String fileName) {
		File outputSubDirectory = new File(outputDirectory.getAbsolutePath()
				+ File.separator + timeStamp);
		outputSubDirectory.mkdir();
		return outputDirectory.getAbsolutePath() + File.separator + timeStamp
				+ fileName;
	}

	@Override
	public boolean checkAll() {
		return checkOutputDirectory()
				&& checkInputFile(metaHistoryFile, NO_METAHISTORY)
				&& checkInputFile(pageLinksFile, NO_PAGELINKS)
				&& checkInputFile(categoryLinksFile, NO_CATEGORYLINKS);
	}
}
