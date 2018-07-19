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
package de.tudarmstadt.ukp.wikipedia.timemachine.domain;

/**
 * Routines for the conversion of the Wikipedia revisions
 *
 */
public class Revision {

	/**
	 * Calendar.getInstance().set(2000,0,1) out relative time zero to saving
	 * memory
	 */
	private static final Long TIME_ZERO = 946724195435l;
	/**
	 * We measure the time not from 1th January 1900 but from 1th January 2000
	 */
	private static final Integer MS_IN_SEC = 1000;

	private Revision() {

	}

	/**
	 * Compress time from long- to the integer-format: reduce the resolution to
	 * "seconds" and zero time to 1th January 2000
	 *
	 * @param date
	 *            date/time in the long format
	 * @return date/time in the compressed integer format
	 */
	public static int compressTime(long date) {
		Long lowResolutionDate = new Long((date - TIME_ZERO) / MS_IN_SEC);
		return lowResolutionDate.intValue();
	}

	/**
	 * Extract time, that was compressed with
	 * {@link Revision#compressTime(long)}
	 *
	 * @param compressedDate
	 *            compressed date/time in the integer format
	 * @return date/time in the long format
	 */
	public static long extractTime(int compressedDate) {
		return new Long(compressedDate) * MS_IN_SEC + TIME_ZERO;
	}

	/**
	 * Merge two unsigned integer values (text id and time stamp) to one long
	 * value (revision) to use GNU Trove container.
	 */
	public static long createRevision(int textId, int timestamp) {
		return (long) textId << 32 | (long) timestamp;
	}

	/**
	 * Extract a time stamp from the revision long.
	 *
	 * @return time stamp
	 */
	public static int getTimestamp(long revision) {
		return (int) (revision & 0x00000000FFFFFFFFl);
	}

	/**
	 * Extract a text ID from the revision long
	 *
	 * @return text ID
	 */
	public static int getTextId(long revision) {
		return (int) (revision >>> 32);
	}
}
