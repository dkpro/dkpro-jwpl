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
package de.tudarmstadt.ukp.wikipedia.wikimachine.domain;

import java.io.File;

import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;

/**
 * File name constants and with the simple input/output directory building
 * rules.
 *
 *
 */
public class Files {

	protected final static String OUTPUT_CATEGORY = "Category.txt";
	protected final static String OUTPUT_PAGECATEGORIES = "page_categories.txt";
	protected final static String OUTPUT_CATEGORYPAGES = "category_pages.txt";
	protected final static String OUTPUT_CATEGORYINLINKS = "category_inlinks.txt";
	protected final static String OUTPUT_CATEGORYOUTLINKS = "category_outlinks.txt";
	protected final static String OUTPUT_PAGEINLINKS = "page_inlinks.txt";
	protected final static String OUTPUT_PAGEOUTLINKS = "page_outlinks.txt";
	protected final static String OUTPUT_PAGE = "Page.txt";
	protected final static String OUTPUT_PAGEMAPLINE = "PageMapLine.txt";
	protected final static String OUTPUT_PAGEREDIRECTS = "page_redirects.txt";
	protected final static String OUTPUT_METADATA = "MetaData.txt";

	protected final static String OUTPUT_DIRECTORY = "output";

	protected File outputDirectory = new File(OUTPUT_DIRECTORY);

	protected ILogger logger;

	public Files(ILogger logger) {
		this.logger = logger;
	}

	public Files(Files files) {
		this.outputDirectory = files.outputDirectory;
	}

	protected boolean checkOutputDirectory() {
		boolean result = outputDirectory.exists()
				&& outputDirectory.isDirectory();
		if (!result) {
			result = outputDirectory.mkdir();
		}
		if (!result) {
			logger.log("can't create the output directory");
		}
		return result;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = new File(outputDirectory);
	}

	public File getOutputDirectory() {
		return this.outputDirectory;
	}

	protected String getOutputPath(String fileName) {
		this.outputDirectory.mkdir();
		return outputDirectory.getAbsolutePath() + File.separator + fileName;
	}

	public String getOutputCategory() {
		return getOutputPath(OUTPUT_CATEGORY);
	}

	public String getOutputPageCategories() {
		return getOutputPath(OUTPUT_PAGECATEGORIES);
	}

	public String getOutputCategoryPages() {
		return getOutputPath(OUTPUT_CATEGORYPAGES);
	}

	public String getOutputCategoryInlinks() {
		return getOutputPath(OUTPUT_CATEGORYINLINKS);
	}

	public String getOutputCategoryOutlinks() {
		return getOutputPath(OUTPUT_CATEGORYOUTLINKS);
	}

	public String getOutputPageInlinks() {
		return getOutputPath(OUTPUT_PAGEINLINKS);
	}

	public String getOutputPageOutlinks() {
		return getOutputPath(OUTPUT_PAGEOUTLINKS);
	}

	public String getOutputPage() {
		return getOutputPath(OUTPUT_PAGE);
	}

	public String getOutputPageMapLine() {
		return getOutputPath(OUTPUT_PAGEMAPLINE);
	}

	public String getOutputPageRedirects() {
		return getOutputPath(OUTPUT_PAGEREDIRECTS);
	}

	public String getOutputMetadata() {
		;
		return getOutputPath(OUTPUT_METADATA);
	}

	public boolean checkAll() {
		return checkOutputDirectory();
	}

}
