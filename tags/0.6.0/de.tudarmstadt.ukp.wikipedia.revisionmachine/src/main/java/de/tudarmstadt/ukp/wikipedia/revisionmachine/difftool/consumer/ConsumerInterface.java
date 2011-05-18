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

/**
 * The ConsumerInterface provides access to the standard consumer methods.
 * 
 * 
 * 
 */
public interface ConsumerInterface
{

	/**
	 * Returns the processing mode of the consumer.
	 * 
	 * @return ConsumerMode
	 */
	public ConsumerMode getConsumerMode();

	/**
	 * Returns the state of the consumer.
	 * 
	 * @return ConsumerState
	 */
	public ConsumerStates getConsumerState();

	/**
	 * Sends the start signal to the consumer. The consumer will not start
	 * processing until he received the signal.
	 * 
	 * @return timestamp
	 */
	public long sendStartSignal();

	/**
	 * Sends the stop signal to the consumer. The consumer will stop processing
	 * after he received the signal and finished his current task / set of task.
	 * 
	 * @return timestamp
	 */
	public long sendStopSignal();

	/**
	 * Sends a ping signal to the consumer to determine whether the consumer is
	 * still alive.
	 * 
	 * @deprecated currently unused, could be reactivated later
	 * @return timestamp
	 */
	@Deprecated
	public long sendPingSignal();

	/**
	 * Sends the kill signal to the consumer. The consumer will enter shutdown
	 * mode after he finished his current task / set of tasks.
	 * 
	 * The consumer will automatically call the ProducerConsumer to ask for
	 * deregistration.
	 * 
	 * @return timestamp
	 */
	public long sendKillSignal();

}
