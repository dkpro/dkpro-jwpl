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
/**
 * @(#)Revision.java
 */
package de.tudarmstadt.ukp.wikipedia.timemachine.domain;

/**
 * Routines for the conversion of the Wikipedia revisions
 * 
 * @author ivan.galkin
 */
public class Revision {

	/**
	 * Calendar.getInstance().set(2000,0,1) out relative time zero to saving
	 * memory
	 */
	private static final Long TIME_ZERO = 946724195435l;
	/**
	 * We measure the time not from 1th Janury 1900 but from 1th January 2000
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
	 * 
	 * @param textId
	 * @param timestamp
	 * @return
	 */
	public static long createRevision(int textId, int timestamp) {
		return (long) textId << 32 | (long) timestamp;
	}

	/**
	 * Extract a time stamp from the revision long.
	 * 
	 * @see DumpVersion#createRevision
	 * @param revision
	 * @return time stamp
	 */
	public static int getTimestamp(long revision) {
		return (int) (revision & 0x00000000FFFFFFFFl);
	}

	/**
	 * Extract a text ID from the revision long
	 * 
	 * @see DumpVersion#createRevision
	 * @param revision
	 * @return text ID
	 */
	public static int getTextId(long revision) {
		return (int) (revision >>> 32);
	}
}
