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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.util.MathUtilities;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.ConsumerInterface;

/**
 * This class wraps the ConsumerInterface and will be stored in the consumer
 * producer.
 *
 * This class contain additional information for the efficiency evaluation of
 * consumers.
 *
 *
 *
 */
public class ConsumerData
{

	/**
	 * This object contains the measured timing information for tasks.
	 *
	 *
	 *
	 */
	private class Timing
	{

		/** Sleeping time */
		private final long sleepingTime;

		/** Current time */
		private final long timestamp;

		/** Working time */
		private final long workingTime;

		/**
		 * (Constructor) Creates a new Timing object.
		 *
		 * @param timestamp
		 *            current time
		 * @param workingTime
		 *            working time
		 * @param sleepingTime
		 *            sleeping time
		 */
		public Timing(final long timestamp, final long workingTime,
				final long sleepingTime)
		{
			this.timestamp = timestamp;
			this.workingTime = workingTime;
			this.sleepingTime = sleepingTime;
		}
	}

	/**
	 * Constant - Describe the size of the time window
	 * TODO: should be a configuration parameter
	 */
	private final static long WINDOWSIZE = 600000;

	/** Reference to the ConsumerInterface */
	private final ConsumerInterface consumer;

	/** Name of the consumer */
	private final String name;

	/** Number of tasks the consumer processed */
	private long nrTasks;

	/** Sleeping time */
	private long sleepingTime;

	/**
	 * List of timing information - used to measure the efficiency over the last
	 * couple of minutes
	 */
	private final List<Timing> window;

	/** Working time */
	private long workingTime;

	/**
	 * (Constructor) Creates a new ConsumerData object.
	 *
	 * @param name
	 *            name of the consumer
	 * @param consumer
	 *            reference to the consumer
	 */
	public ConsumerData(final String name, final ConsumerInterface consumer)
	{
		this.name = name;
		this.consumer = consumer;

		this.nrTasks = 0;
		this.workingTime = 0;
		this.sleepingTime = 0;

		this.window = new ArrayList<Timing>();
	}

	/**
	 * Returns efficiency of the consumer using the current time window.
	 *
	 * @return efficiency in percent [xx.xx]
	 */
	public double calcCurrentWindow()
	{

		long current = System.currentTimeMillis();
		int i = 0;
		Timing t;
		long work = 1, sleep = 0;

		synchronized (window) {
			while (i < window.size()) {
				t = window.get(i);
				if (t.timestamp < current - WINDOWSIZE) {
					window.remove(i);
				}
				else {
					work += t.workingTime;
					sleep += t.sleepingTime;
					i++;
				}
			}
		}

		return MathUtilities.percRoundPlus(work, sleep);
	}

	/**
	 * Calculates the overall efficiency.
	 *
	 * @return efficiency [xx.xx]
	 */
	public double calcEfficiency()
	{
		return MathUtilities.percentPlus(workingTime, sleepingTime);
	}

	/**
	 * Send the kill signal to the consumer.
	 */
	public void kill()
	{
		this.consumer.sendKillSignal();
	}

	/**
	 * Adds the measured time information for a task.
	 *
	 * @param workingTime
	 *            working time
	 * @param sleepingTime
	 *            sleeping time
	 */
	public void setInformation(final long workingTime, final long sleepingTime)
	{
		this.nrTasks++;

		if (this.workingTime != 0) {
			synchronized (window) {
				this.window.add(new Timing(System.currentTimeMillis(),
						workingTime - this.workingTime, sleepingTime
								- this.sleepingTime));
			}
		}

		this.workingTime = workingTime;
		this.sleepingTime = sleepingTime;
	}

	/**
	 * Returns the string representation of the content stored in the
	 * ConsumerData object.
	 *
	 * @return consumer name consumer state < nrTasks > < current efficiency > [
	 *         number of tasks in current window ] [ overall working time |
	 *         overall sleeping time ]
	 */
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();

		buffer.append(name);
		while (buffer.length() < 20) {
			buffer.append(' ');
		}

		buffer.append(consumer.getConsumerState());
		while (buffer.length() < 45) {
			buffer.append(' ');
		}

		buffer.append('<');
		buffer.append(this.nrTasks);
		buffer.append(">\t<");
		buffer.append(calcCurrentWindow());
		buffer.append("\t[");
		buffer.append(window.size());
		buffer.append("]>\t<");
		buffer.append(calcEfficiency());
		buffer.append("\t[");
		buffer.append(this.workingTime);
		buffer.append(" | ");
		buffer.append(this.sleepingTime);
		buffer.append("]>");

		return buffer.toString();
	}
}
