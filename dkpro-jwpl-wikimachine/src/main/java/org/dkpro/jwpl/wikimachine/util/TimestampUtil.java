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
package org.dkpro.jwpl.wikimachine.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Provides utilities for the conversion of timestamps.
 *
 * @see Timestamp
 */
public abstract class TimestampUtil
{
    /**
     * Parses a given Mediawiki time string into a {@link Timestamp} object.
     *
     * @param mediaWikiString The input parameter; must respect the format {@code yyyyMMddHHmmss}.
     *
     * @return A valid {@link Timestamp} representation of {@code mediaWikiString}.
     */
    public static Timestamp parse(String mediaWikiString)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        long time = 0;
        try {
            time = sdf.parse(mediaWikiString).getTime();
        }
        catch (ParseException e) {
            // Ignore
        }

        return new Timestamp(time);
    }

    /**
     * Converts a {@link Timestamp} object into a valid Mediawiki time string.
     * <p>
     * Example:<br/>
     * The timestamp {@code 1970-01-04 18:11:40.0} will result in {@code 19700104181140}.
     *
     * @param timestamp The {@link Timestamp} to convert.
     *
     * @return A valid MediaWiki time string in the format {@code yyyyMMddHHmmss}.
     */
    public static String toMediaWikiString(Timestamp timestamp)
    {
        // 0123456789012345678
        String original = timestamp.toString();
        StringBuffer result = new StringBuffer();
        result.append(original, 0, 4);// year
        result.append(original, 5, 7);// month
        result.append(original, 8, 10);// date
        result.append(original, 11, 13);// hour
        result.append(original, 14, 16);// minute
        result.append(original, 17, 19);// second
        return result.toString();
    }

    /**
     * Computes the 'next' timestamp for a specified shift {@code nrDays}.
     *
     * @param previous  The input {@link Timestamp} to compute the next one from.
     * @param nrDays    The positive offset of days to shift {@code previous}.
     *
     * @return A valid {@link Timestamp} representation shifted by {@code nrDays}.
     */
    public static Timestamp getNextTimestamp(Timestamp previous, long nrDays)
    {
        return new Timestamp(previous.getTime() + (nrDays * 24 * 60 * 60 * 1000));
    }
}
