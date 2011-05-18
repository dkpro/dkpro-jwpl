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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.consumer;

import java.util.Random;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.ConfigurationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.LoggingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.exceptions.TimeoutException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.Logger;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.LoggerType;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.LoggingFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging.messages.consumer.ConsumerLogMessages;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.ConfigurationManager;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.producer.consumers.ProducerConsumerInterface;

/**
 * The AbstractConsumer class is the super class of all DiffTool Consumers.
 *
 *
 *
 */
public abstract class AbstractConsumer
	extends Thread
	implements ConsumerInterface
{

	/** The time when the consumer was created */
	protected long startTime;

	/** The time the consumer has been in sleeping mode */
	protected long sleepingTime;

	/** The time the consumer has been in working mode */
	protected long workingTime;

	/**
	 * Start flag - This flag has to be true otherwise the consumer will do
	 * nothing.
	 */
	protected boolean start;

	/**
	 * Shutdown flag - If this flag has been set the consumer will terminate
	 * itself and the thread he is using.
	 */
	protected boolean shutdown;

	/**
	 * Sleep flag - This flag has to be set otherwise the consumer will not be
	 * able to enter sleeping mode.
	 */
	protected boolean sleep;

	/** Mode of the consumer */
	private ConsumerMode mode;

	/** State of the consumer */
	private ConsumerStates state;

	/**
	 * Timeout counter - counts the number of consecutive sleep periods
	 */
	private int sleepCounter;

	/**
	 * Timeout counter - counts the number of milliseconds in the current sleep
	 * period
	 */
	private long sleepPeriod;


	/** Reference to the assigned logger */
	protected Logger logger;

	/** Reference to the consumer producer */
	protected ProducerConsumerInterface producer;

	/** Reference to a random number generator */
	private final Random seed;

	/**
	 * Constructor - A new abstract consumer will be created.
	 *
	 * @param type
	 *            Type of logger
	 * @param producer
	 *            Reference to the consumer producer
	 *
	 * @throws ConfigurationException
	 *             if a configuration value could not be located
	 *
	 * @throws LoggingException
	 *             if an error occured while accesing the logger
	 */
	public AbstractConsumer(final LoggerType type,
			final ProducerConsumerInterface producer)
		throws ConfigurationException, LoggingException
	{

		this.setConsumerState(ConsumerStates.INIT);
		ConfigurationManager config = ConfigurationManager.getInstance();

		this.producer = producer;

		this.setName(producer.register(this));
		this.logger = LoggingFactory.createLogger(type, this.getName());

		this.startTime = System.currentTimeMillis();
		this.sleepingTime = 0;
		this.workingTime = 0;

		this.start = false;
		this.shutdown = false;
		this.sleep = true;

		this.seed = new Random();

		ConsumerLogMessages.logInitialization(logger);
		System.out.println("Created\t" + this.getName());
	}

	/**
	 * Returns the current time.
	 *
	 * IConsumer-Method: Used to send the start signal to the consumer.
	 */
	@Override
	public long sendStartSignal()
	{

		this.start = true;
		ConsumerLogMessages.logStartSignalMessage(logger);

		return System.currentTimeMillis();
	}

	/**
	 * Returns the current time.
	 *
	 * IConsumer-Method: Used to send an interrupt signal to the consumer.
	 */
	@Override
	public long sendStopSignal()
	{

		this.start = false;
		ConsumerLogMessages.logStopSignal(logger);

		return System.currentTimeMillis();
	}

	/**
	 * Returns the current time.
	 *
	 * IConsumer-Method: Used to send an an alive signal to the producer.
	 */
	@Override
	public long sendPingSignal()
	{

		ConsumerLogMessages.logPingSignal(logger);

		/*
		 * if (this.producerGUI) {
		 * producer.sendConsumerInformation(this.getName(), workingTime,
		 * sleepingTime); }
		 */

		return System.currentTimeMillis();
	}

	/**
	 * Returns the current time.
	 *
	 * IConsumer-Method: Used to send the kill signal to the consumer.
	 */
	@Override
	public long sendKillSignal()
	{

		ConsumerLogMessages.logKillSignalMessage(logger);

		this.shutdown = true;
		this.interrupt();
		return System.currentTimeMillis();
	}

	/**
	 * This method will let the current thread sleep if the sleeping mode has
	 * been activated.
	 *
	 * The consumer state will be changed accordingly during the sleep period.
	 *
	 * @throws TimeoutException
	 *             If the sleep counter values have reached the configured
	 *             timeout value.
	 */
	public void sleep()
		throws TimeoutException
	{

		// Check whether the sleeping mode has been activated.
		if (this.sleep) {

			// Change consumer state
			ConsumerStates oldState = this.getConsumerState();
			switch (oldState) {
			case SLEEPING_PARTIAL_TASK:
			case PROCCESSING_PARTIAL_TASK:
				this.setConsumerState(ConsumerStates.SLEEPING_PARTIAL_TASK);
				break;
			case SLEEPING_TRANSMITTING:
			case TRANSMITTING:
				this.setConsumerState(ConsumerStates.SLEEPING_TRANSMITTING);
				break;
			default:
				if (this.start) {
					this.setConsumerState(ConsumerStates.SLEEPING);
				}
				break;
			}

			// Log begin of new sleep period
			if (this.sleepCounter == 0) {
				ConsumerLogMessages.logSleep(this.logger);
			}

			this.sleepCounter++;

			// sleep
			try {
				long sleepingInterval = 250 + this.seed.nextInt(250);

				if (this.start) {

					sleepPeriod += sleepingInterval;
					this.sleepingTime += sleepingInterval;
				}

				Thread.sleep(sleepingInterval);

			}
			catch (InterruptedException e) {
			}

			// Reset the consumer state
			this.setConsumerState(oldState);

			// reset timeout counters
		}
		else {
			timeoutReset();
		}

		// Garbage collection
		System.gc();
	}

	/**
	 * Returns a reference to the logger assigned to this consumer.
	 *
	 * @return Reference to the consumers logger
	 */
	public Logger getLogger()
	{
		return this.logger;
	}

	/**
	 * Returns the mode of the consumer.
	 *
	 * @return ConsumerMode
	 */
	@Override
	public ConsumerMode getConsumerMode()
	{
		return mode;
	}

	/**
	 * Set the mode of the consumer.
	 *
	 * @param mode
	 *            ConsumerMode
	 */
	public void setMode(final ConsumerMode mode)
	{
		this.mode = mode;
	}

	/**
	 * Sets the state of the consumer.
	 *
	 * @param state
	 *            ConsumerState
	 */
	public void setConsumerState(final ConsumerStates state)
	{
		this.state = state;
	}

	/**
	 * Returns the state of the consumer.
	 *
	 * @return ConsumerState
	 */
	@Override
	public ConsumerStates getConsumerState()
	{
		return this.state;
	}

	/**
	 * This method sets the timeout counter values to zero.
	 */
	public void timeoutReset()
	{
		this.sleepCounter = 0;
		this.sleepPeriod = 0;
	}

}
