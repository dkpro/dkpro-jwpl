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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.consumers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.ConsumerInterface;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.article.ArticleConsumer;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.diff.DiffConsumer;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer.sql.SQLConsumer;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.ConsumerData;

public class ConsumerManager
{

	/** Consumer ID Counter */
	private int idCount;

	/**
	 * Map containing a mapping of the ArticleConsumer name and the related
	 * ConsumerData object.
	 */
	private Map<String, ConsumerData> articleConsumers;

	/**
	 * Map containing a mapping of the DiffConsumer name and the related
	 * ConsumerData object.
	 */
	private Map<String, ConsumerData> diffConsumers;

	/**
	 * Map containing a mapping of the SQLConsumer name and the related
	 * ConsumerData object.
	 */
	private Map<String, ConsumerData> sqlConsumers;

	/** Prefix for ArticleConsumers */
	private static final String ARTICLECONSUMER = "ArticleConsumer";

	/** Prefix for DiffConsumers */
	private static final String DIFFCONSUMER = "DiffConsumer";

	/** Prefix for SQLConsumers */
	private static final String SQLCONSUMER = "SQLConsumer";

	/** Seperator, used for seperating consumer type and consumer id */
	private static final String SEPERATOR = "-";

	/**
	 * (Constructor) Creates the ConsumerManager.
	 */
	public ConsumerManager()
	{

		this.idCount = 0;

		this.articleConsumers = new HashMap<String, ConsumerData>();
		this.diffConsumers = new HashMap<String, ConsumerData>();
		this.sqlConsumers = new HashMap<String, ConsumerData>();
	}

	/**
	 * Registers the consumer at the ConsumerManager and receives. The name of
	 * the consumer will be assigned and returend.
	 * 
	 * @param consumer
	 *            reference to the consumer
	 * @return Name of the consumer
	 */
	public synchronized String register(final ConsumerInterface consumer)
	{

		String name;
		if (consumer instanceof ArticleConsumer) {
			name = ARTICLECONSUMER + SEPERATOR + ++idCount;
			this.articleConsumers.put(name, new ConsumerData(name, consumer));

		}
		else if (consumer instanceof SQLConsumer) {
			name = SQLCONSUMER + SEPERATOR + ++idCount;
			this.sqlConsumers.put(name, new ConsumerData(name, consumer));

		}
		else if (consumer instanceof DiffConsumer) {
			name = DIFFCONSUMER + SEPERATOR + ++idCount;
			this.diffConsumers.put(name, new ConsumerData(name, consumer));

		}
		else {
			throw new RuntimeException();
		}

		return name;
	}

	/**
	 * Unregisters the consumer with the given name.
	 * 
	 * @param name
	 *            name of the consumer
	 * @return TRUE if the consumer could be removed FALSE otherwise
	 */
	public synchronized boolean unregister(final String name)
	{

		if (name.startsWith(ARTICLECONSUMER)) {
			this.articleConsumers.remove(name);
		}
		else if (name.startsWith(DIFFCONSUMER)) {
			this.diffConsumers.remove(name);
		}
		else if (name.startsWith(SQLCONSUMER)) {
			this.sqlConsumers.remove(name);
		}
		else {
			// TODO: Should be replaced
			throw new RuntimeException("Consumer name could not be resolved");
		}

		return true;
	}

	/**
	 * Returns the number of registered article consumers.
	 * 
	 * @return number of article consumers
	 */
	public int getCountArticleConsumers()
	{
		return this.articleConsumers.size();
	}

	/**
	 * Returns the number of registered diff consumers.
	 * 
	 * @return number of diff consumers
	 */
	public int getCountDiffConsumers()
	{
		return this.diffConsumers.size();
	}

	/**
	 * Returns the number of registered sql consumers.
	 * 
	 * @return number of sql consumers
	 */
	public int getCountSQLConsumers()
	{
		return this.sqlConsumers.size();
	}

	/**
	 * Initiates shutdown mode.
	 */
	public synchronized void shutdown()
	{
		if (getCountArticleConsumers() > 0) {
			Iterator<ConsumerData> consumerIt = this.articleConsumers.values()
					.iterator();
			while (consumerIt.hasNext()) {
				consumerIt.next().kill();
			}
		}

		if (getCountDiffConsumers() > 0) {
			Iterator<ConsumerData> consumerIt = this.diffConsumers.values()
					.iterator();
			while (consumerIt.hasNext()) {
				consumerIt.next().kill();
			}
		}

		if (getCountSQLConsumers() > 0) {
			Iterator<ConsumerData> consumerIt = this.sqlConsumers.values()
					.iterator();
			while (consumerIt.hasNext()) {
				consumerIt.next().kill();
			}
		}
	}

	/**
	 * This method should be called to add additional data for a consumer.
	 * 
	 * @param name
	 *            name of a consumer
	 * @param workingTime
	 *            time the consumer was working
	 * @param sleepingTime
	 *            time the consumer was sleeping
	 */
	public synchronized void sendConsumerInformation(final String name,
			final long workingTime, final long sleepingTime)
	{

		if (name.startsWith(ARTICLECONSUMER)) {

			ConsumerData info = this.articleConsumers.get(name);
			info.setInformation(workingTime, sleepingTime);

		}
		else if (name.startsWith(DIFFCONSUMER)) {

			ConsumerData info = this.diffConsumers.get(name);
			info.setInformation(workingTime, sleepingTime);

		}
		else if (name.startsWith(SQLCONSUMER)) {

			ConsumerData info = this.sqlConsumers.get(name);
			info.setInformation(workingTime, sleepingTime);

		}
		else {
			// should never happen
			System.err.println("UNKNOWN SCI REPORT FROM " + name);
		}

	}

	/**
	 * Returns the string representation of the ConsumerManager's content.
	 * 
	 * @return String containg the description of all registered consumers
	 */
	public String toString()
	{

		Iterator<ConsumerData> consumerIt;
		StringBuilder buffer = new StringBuilder();

		if (getCountArticleConsumers() > 0) {
			synchronized (this.articleConsumers) {
				consumerIt = this.articleConsumers.values().iterator();
				while (consumerIt.hasNext()) {
					buffer.append(consumerIt.next().toString() + "\r\n");
				}
			}
		}

		if (getCountDiffConsumers() > 0) {
			synchronized (this.diffConsumers) {
				consumerIt = this.diffConsumers.values().iterator();
				while (consumerIt.hasNext()) {
					buffer.append(consumerIt.next().toString() + "\r\n");
				}
			}
		}

		if (getCountSQLConsumers() > 0) {
			synchronized (this.sqlConsumers) {
				consumerIt = this.sqlConsumers.values().iterator();
				while (consumerIt.hasNext()) {
					buffer.append(consumerIt.next().toString() + "\r\n");
				}
			}
		}

		return buffer.toString();
	}
}
