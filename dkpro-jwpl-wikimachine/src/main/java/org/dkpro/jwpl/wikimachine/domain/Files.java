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
package org.dkpro.jwpl.wikimachine.domain;

import java.io.File;

import org.dkpro.jwpl.wikimachine.debug.ILogger;

/**
 * A common base class with file name constants and with the simple
 * input/output directory building rules.
 */
public abstract class Files
{

    /** The default name of the category file. */
    protected final static String OUTPUT_CATEGORY = "Category.txt";
    /** The default name of the page categories file. */
    protected final static String OUTPUT_PAGECATEGORIES = "page_categories.txt";
    /** The default name of the category pages file. */
    protected final static String OUTPUT_CATEGORYPAGES = "category_pages.txt";
    /** The default name of the category inlinks file. */
    protected final static String OUTPUT_CATEGORYINLINKS = "category_inlinks.txt";
    /** The default name of the category outlinks file. */
    protected final static String OUTPUT_CATEGORYOUTLINKS = "category_outlinks.txt";
    /** The default name of the page inlinks file. */
    protected final static String OUTPUT_PAGEINLINKS = "page_inlinks.txt";
    /** The default name of the page outlinks file. */
    protected final static String OUTPUT_PAGEOUTLINKS = "page_outlinks.txt";
    /** The default name of the page file. */
    protected final static String OUTPUT_PAGE = "Page.txt";
    /** The default name of the page map line file. */
    protected final static String OUTPUT_PAGEMAPLINE = "PageMapLine.txt";
    /** The default name of the page redirects file. */
    protected final static String OUTPUT_PAGEREDIRECTS = "page_redirects.txt";
    /** The default name of the meta data file. */
    protected final static String OUTPUT_METADATA = "MetaData.txt";

    /** The default name of the output directory. */
    protected final static String OUTPUT_DIRECTORY = "output";

    /** The current output directory. */
    protected File outputDirectory;

    /** The active {@link ILogger logger}. */
    protected ILogger logger;

    /**
     * Instantiates a {@link Files} object with the specified {@code logger}.
     *
     * @param logger The {@link ILogger logger} to use.
     */
    public Files(ILogger logger)
    {
        this.logger = logger;
        this.outputDirectory = new File(OUTPUT_DIRECTORY);
    }

    /**
     * Instantiates a copy of the specified {@link Files} object.
     *
     * @param files The {@link Files logger} to use for the copy.
     */
    public Files(Files files)
    {
        this(files.logger);
        this.outputDirectory = files.outputDirectory;
    }

    /**
     * Verifies if the output directory exists. In case it does not exist, an attempt is made to create it.
     *
     * @return  {@code true} if the output directory is present, {@code false} otherwise.
     */
    protected boolean checkOutputDirectory()
    {
        boolean result = outputDirectory.exists() && outputDirectory.isDirectory();
        if (!result) {
            result = outputDirectory.mkdirs();
        }
        if (!result) {
            logger.log("can't create the output directory");
        }
        return result;
    }

    /**
     * Configures the output directory via the specified parameter.
     *
     * @param outputDirectory The absolute or relative path to the output directory.
     */
    public void setOutputDirectory(String outputDirectory)
    {
        this.outputDirectory = new File(outputDirectory);
    }

    /**
     * @return Retrieves the current output directory as {@link File} reference.
     */
    public File getOutputDirectory()
    {
        return this.outputDirectory;
    }

    /**
     * Concatenates the specified {@code fileName} with the current output directory to an absolute representation.
     *
     * @param fileName The file name to combine.
     * @return A combined, absolute path for the file name within the output directory. In case the output directory
     *         does not exist or can not be created, the plain {@code fileName} is returned.
     */
    protected String getOutputPath(String fileName)
    {
        if (checkOutputDirectory()) {
            return outputDirectory.getAbsolutePath() + File.separator + fileName;
        } else {
            return fileName;
        }
    }

    /**
     * Concatenates {@link #OUTPUT_CATEGORY} with the current output directory to an absolute representation.
     *
     * @return A combined, absolute path within the output directory. In case the output directory
     *         does not exist or can not be created, the plain {@link #OUTPUT_CATEGORY} name is returned.
     */
    public String getOutputCategory()
    {
        return getOutputPath(OUTPUT_CATEGORY);
    }

    /**
     * Concatenates {@link #OUTPUT_PAGECATEGORIES} with the current output directory to an absolute representation.
     *
     * @return A combined, absolute path within the output directory. In case the output directory
     *         does not exist or can not be created, the plain {@link #OUTPUT_PAGECATEGORIES} name is returned.
     */
    public String getOutputPageCategories()
    {
        return getOutputPath(OUTPUT_PAGECATEGORIES);
    }

    /**
     * Concatenates {@link #OUTPUT_CATEGORYPAGES} with the current output directory to an absolute representation.
     *
     * @return A combined, absolute path within the output directory. In case the output directory
     *         does not exist or can not be created, the plain {@link #OUTPUT_CATEGORYPAGES} name is returned.
     */
    public String getOutputCategoryPages()
    {
        return getOutputPath(OUTPUT_CATEGORYPAGES);
    }

    /**
     * Concatenates {@link #OUTPUT_CATEGORYINLINKS} with the current output directory to an absolute representation.
     *
     * @return A combined, absolute path within the output directory. In case the output directory
     *         does not exist or can not be created, the plain {@link #OUTPUT_CATEGORYINLINKS} name is returned.
     */
    public String getOutputCategoryInlinks()
    {
        return getOutputPath(OUTPUT_CATEGORYINLINKS);
    }

    /**
     * Concatenates {@link #OUTPUT_CATEGORYOUTLINKS} with the current output directory to an absolute representation.
     *
     * @return A combined, absolute path within the output directory. In case the output directory
     *         does not exist or can not be created, the plain {@link #OUTPUT_CATEGORYOUTLINKS} name is returned.
     */
    public String getOutputCategoryOutlinks()
    {
        return getOutputPath(OUTPUT_CATEGORYOUTLINKS);
    }

    /**
     * Concatenates {@link #OUTPUT_PAGEINLINKS} with the current output directory to an absolute representation.
     *
     * @return A combined, absolute path within the output directory. In case the output directory
     *         does not exist or can not be created, the plain {@link #OUTPUT_PAGEINLINKS} name is returned.
     */
    public String getOutputPageInlinks()
    {
        return getOutputPath(OUTPUT_PAGEINLINKS);
    }

    /**
     * Concatenates {@link #OUTPUT_PAGEOUTLINKS} with the current output directory to an absolute representation.
     *
     * @return A combined, absolute path within the output directory. In case the output directory
     *         does not exist or can not be created, the plain {@link #OUTPUT_PAGEOUTLINKS} name is returned.
     */
    public String getOutputPageOutlinks()
    {
        return getOutputPath(OUTPUT_PAGEOUTLINKS);
    }

    /**
     * Concatenates {@link #OUTPUT_PAGE} with the current output directory to an absolute representation.
     *
     * @return A combined, absolute path within the output directory. In case the output directory
     *         does not exist or can not be created, the plain {@link #OUTPUT_PAGE} name is returned.
     */
    public String getOutputPage()
    {
        return getOutputPath(OUTPUT_PAGE);
    }

    /**
     * Concatenates {@link #OUTPUT_PAGEMAPLINE} with the current output directory to an absolute representation.
     *
     * @return A combined, absolute path within the output directory. In case the output directory
     *         does not exist or can not be created, the plain {@link #OUTPUT_PAGEMAPLINE} name is returned.
     */
    public String getOutputPageMapLine()
    {
        return getOutputPath(OUTPUT_PAGEMAPLINE);
    }

    /**
     * Concatenates {@link #OUTPUT_PAGEREDIRECTS} with the current output directory to an absolute representation.
     *
     * @return A combined, absolute path within the output directory. In case the output directory
     *         does not exist or can not be created, the plain {@link #OUTPUT_PAGEREDIRECTS} name is returned.
     */
    public String getOutputPageRedirects()
    {
        return getOutputPath(OUTPUT_PAGEREDIRECTS);
    }

    /**
     * Concatenates {@link #OUTPUT_METADATA} with the current output directory to an absolute representation.
     *
     * @return A combined, absolute path within the output directory. In case the output directory
     *         does not exist or can not be created, the plain {@link #OUTPUT_METADATA} name is returned.
     */
    public String getOutputMetadata()
    {
        return getOutputPath(OUTPUT_METADATA);
    }

    /**
     * Verifies if the output directory exists. In case it does not exist, an attempt is made to create it.
     *
     * @return  {@code true} if the output directory is present, {@code false} otherwise.
     */
    public boolean checkAll()
    {
        return checkOutputDirectory();
    }

}
