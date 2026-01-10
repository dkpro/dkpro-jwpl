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

import java.sql.Timestamp;

import org.dkpro.jwpl.wikimachine.debug.ILogger;

/**
 * Wraps all the parameters for the execution of the DBMapping tool.
 */
public class Configuration
{

    private static final Timestamp TIMESTAMP_UNDEFINED = new Timestamp(Long.MIN_VALUE);

    private Timestamp fromTimestamp = TIMESTAMP_UNDEFINED;
    private Timestamp toTimestamp = TIMESTAMP_UNDEFINED;
    private int each;
    private String language;
    private String mainCategory;
    private String disambiguationCategory;

    private final ILogger logger;

    /**
     * Instantiates a {@link Configuration}.
     *
     * @param logger The {@link ILogger} to use at runtime.
     */
    public Configuration(ILogger logger)
    {
        this.logger = logger;
    }

    /**
     * @return Retrieves the current {@code from} {@link Timestamp} of a configuration.
     */
    public Timestamp getFromTimestamp()
    {
        return fromTimestamp;
    }

    /**
     * Sets the {@code from} timestamp value.
     *
     * @param fromTimestamp A valid {@link Timestamp} instance.
     */
    public void setFromTimestamp(Timestamp fromTimestamp)
    {
        this.fromTimestamp = fromTimestamp;
    }

    /**
     * @return Retrieves the current {@code to} {@link Timestamp} of a configuration.
     */
    public Timestamp getToTimestamp()
    {
        return toTimestamp;
    }

    /**
     * Sets the {@code to} timestamp value.
     *
     * @param toTimestamp A valid {@link Timestamp} instance.
     */
    public void setToTimestamp(Timestamp toTimestamp)
    {
        this.toTimestamp = toTimestamp;
    }

    /**
     * Validates if the time frame of the {@code from} and {@code to} timestamps are valid,
     * that is, these do not collide in a chronological sense.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean checkTimestamp()
    {
        boolean result = !toTimestamp.equals(TIMESTAMP_UNDEFINED)
                && !fromTimestamp.equals(TIMESTAMP_UNDEFINED)
                && (this.toTimestamp.after(this.fromTimestamp)
                        || this.toTimestamp.equals(this.fromTimestamp));
        if (!result) {
            logger.log("fromTimestamp is after toTimestamp");
        }
        return result;
    }

    /**
     * @return Retrieves the current {@code each} of a configuration.
     */
    public int getEach()
    {
        return each;
    }

    /**
     * Sets the {@code each} value.
     *
     * @param each A positive value for the {@code each} parameter.
     */
    public void setEach(int each)
    {
        this.each = each;
    }

    /**
     * @return {@code true} if the (current) {@code each} parameter is valid, {@code false} otherwise.
     */
    public boolean checkEach()
    {
        boolean result = each > 0;
        if (!result) {
            logger.log("'each' must be positive");
        }
        return result;
    }

    /**
     * @return Retrieves the current {@code language} of a configuration.
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * Sets the {@code language} value.
     *
     * @param language The value of the {@code language} parameter.
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * @return Retrieves the current {@code main category} of a configuration.
     */
    public String getMainCategory()
    {
        return mainCategory;
    }

    /**
     * Sets the {@code mainCategory} value.
     *
     * @param mainCategory The value of the {@code mainCategory} parameter.
     */
    public void setMainCategory(String mainCategory)
    {
        this.mainCategory = mainCategory;
    }

    /**
     * @return Retrieves the current {@code disambiguation category} of a configuration.
     */
    public String getDisambiguationCategory()
    {
        return disambiguationCategory;
    }

    /**
     * Sets the {@code disambiguationCategory} value.
     *
     * @param disambiguationCategory The value of the {@code disambiguationCategory} parameter.
     */
    public void setDisambiguationCategory(String disambiguationCategory)
    {
        this.disambiguationCategory = disambiguationCategory;
    }

    /**
     * @return Retrieves {@code true} if time configuration is valid.
     */
    public boolean checkTimeConfig()
    {
        return checkEach() && checkTimestamp();
    }
}
