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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util;

/**
 * This class transform milliseconds to a clock representation.
 *
 * A clock representation describes the time (HH:MM:SS:sss) and is used for
 * measuring the processing times.
 *
 *
 *
 */
public class Time
{

	/** Weeks */
	private short weeks;

	/** Days */
	private short days;

	/** Hours */
	private short hours;

	/** Minutes */
	private short minutes;

	/** Seconds */
	private short seconds;

	/** Milliseconds */
	private short milliseconds;

	/**
	 * (Constructor) Creates a new time information transforming the millisecond
	 * value into a clock representation.
	 *
	 * @param time
	 *            milliseconds
	 */
	public Time(final long time)
	{

		long ttime = time;

		this.milliseconds = (short) (ttime % 1000);
		ttime = ttime / 1000;

		this.seconds = (short) (ttime % 60);
		ttime = ttime / 60;

		this.minutes = (short) (ttime % 60);
		ttime = ttime / 60;

		this.hours = (short) (ttime % 24);
		ttime = ttime / 24;

		this.days = (short) (ttime % 7);
		this.weeks = (short) (ttime / 7);
	}

	/**
	 * Returns the textual description of the time value.
	 */
	public String toString()
	{
		StringBuilder s = new StringBuilder();

		boolean appended = false;
		if (this.weeks != 0 || appended) {
			appended = true;
			s.append(this.weeks + " Wochen ");
		}
		if (this.days != 0 || appended) {
			appended = true;
			s.append(this.days + " Tage ");
		}
		if (this.hours != 0 || appended) {
			appended = true;
			s.append(this.hours + " Stunden ");
		}
		if (this.minutes != 0 || appended) {
			appended = true;
			s.append(this.minutes + " Minuten ");
		}
		if (this.seconds != 0 || appended) {
			appended = true;
			s.append(this.seconds + " Sekunden ");
		}
		if (this.milliseconds != 0 || appended) {
			appended = true;
			s.append(this.milliseconds + " Milisekunden");
		}

		return s.toString();
	}

	/**
	 * Returns the clock description of the time value.
	 */
	public String toClock()
	{
		StringBuilder s = new StringBuilder();

		s.append(((this.weeks * 7 + this.days) * 24 + this.hours) + ":");
		if (this.minutes < 10) {
			s.append('0');
		}
		s.append(this.minutes + ":");
		if (this.seconds < 10) {
			s.append('0');
		}
		s.append(this.seconds + ".");
		if (this.milliseconds < 100) {
			s.append('0');
		}
		if (this.milliseconds < 10) {
			s.append('0');
		}
		s.append(this.milliseconds);

		return s.toString();
	}

	/**
	 * Transforms a millisecond value to the clock representation.
	 *
	 * @param time
	 *            milliseconds
	 * @return clock representation
	 */
	public static String toClock(long time)
	{

		long ttime = time;

		short miliseconds = (short) (ttime % 1000);
		ttime = ttime / 1000;

		short seconds = (short) (ttime % 60);
		ttime = ttime / 60;

		short minutes = (short) (ttime % 60);
		ttime = ttime / 60;

		short hours = (short) (ttime % 24);
		ttime = ttime / 24;

		short days = (short) (ttime % 7);
		short weeks = (short) (ttime / 7);

		StringBuilder s = new StringBuilder();

		s.append(((weeks * 7 + days) * 24 + hours) + ":");

		if (minutes < 10) {
			s.append('0');
		}
		s.append(minutes + ":");

		if (seconds < 10) {
			s.append('0');
		}
		s.append(seconds + ".");

		if (miliseconds < 100) {
			s.append('0');
		}
		if (miliseconds < 10) {
			s.append('0');
		}
		s.append(miliseconds);

		return s.toString();
	}
}
