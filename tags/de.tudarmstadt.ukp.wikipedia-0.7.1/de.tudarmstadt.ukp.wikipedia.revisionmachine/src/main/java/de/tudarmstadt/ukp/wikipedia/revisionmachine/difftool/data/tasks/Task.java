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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks;

import java.util.ArrayList;
import java.util.Iterator;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks.info.ArticleInformation;

/**
 * The task class contains the information of a task.
 * 
 * 
 * 
 * 
 * @param <D>
 *            Class of data the task contains
 */
public class Task<D>
{

	/*
	 * +STATICS++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Creates a dummy task without data.
	 * 
	 * @return dummy task
	 */
	@SuppressWarnings("rawtypes")
	public static Task createDummy()
	{
		return new Task(TaskTypes.DUMMY);
	}

	/**
	 * Creates an end task.
	 * 
	 * @return end task
	 */
	@SuppressWarnings("rawtypes")
	public static Task createEndTask()
	{
		return new Task(TaskTypes.ENDTASK);
	}

	/**
	 * Creates a banned task.
	 * 
	 * @return banned task
	 */
	@SuppressWarnings("rawtypes")
	public static Task createBannedTask()
	{
		return new Task(TaskTypes.BANNED_TASK);
	}

	/*
	 * +ATTRIBUTES+AND+CONSTRUCTORS++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/** Type of the task */
	private TaskTypes taskType;

	/** Additional information concerning the article */
	private ArticleInformation header;

	/** Data of the task */
	private final ArrayList<D> container;

	/** Counter of the task parts (1-based) */
	private final int partCounter;

	/** Size of this task */
	private int byteSize;

	/**
	 * Constructor - A new task object of the specified type will be created.
	 * 
	 * @param taskType
	 *            Type of task
	 */
	protected Task(final TaskTypes taskType)
	{
		this.taskType = taskType;
		this.container = null;

		this.byteSize = 0;
		this.partCounter = 0;
	}

	/**
	 * Constructor - A new task object of the type TASK_FULL will be created.
	 * 
	 * @param header
	 *            reference to the article information
	 * @param taskPartCounter
	 *            task part counter
	 */
	public Task(final ArticleInformation header, final int taskPartCounter)
	{
		this.header = header;

		this.byteSize = 0;
		this.partCounter = taskPartCounter;
		this.taskType = TaskTypes.TASK_FULL;

		this.container = new ArrayList<D>();
	}

	/*
	 * +METHODS++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Returns the reference to the article header.
	 */
	public ArticleInformation getHeader()
	{
		return this.header;
	}

	/**
	 * Returns the type of this task.
	 * 
	 * @return TaskType
	 */
	public TaskTypes getTaskType()
	{
		return this.taskType;
	}

	/**
	 * Adds data to this task.
	 * 
	 * @param data
	 *            Reference to the data object.
	 */
	public void add(final D data)
	{
		this.container.add(data);

		// if the size of data is known add the value to the task size
		if (data instanceof ISizeable) {
			this.byteSize += ((ISizeable) data).byteSize();
		}
	}

	/**
	 * Returns the data of this task.
	 * 
	 * @return data
	 */
	public ArrayList<D> getContainer()
	{
		return this.container;
	}

	/**
	 * Returns the data at the specified index.
	 * 
	 * The index will not be check whether it is out of range or not. If you do
	 * not know the appropriate index call the size() method before calling this
	 * method.
	 * 
	 * @param index
	 *            index
	 * @return data
	 */
	public D get(final int index)
	{
		return this.container.get(index);
	}

	/**
	 * Returns the number of data parts the task contains.
	 * 
	 * @return number of data parts.
	 */
	public int size()
	{
		return this.container.size();
	}

	/**
	 * Returns an iterator over the data.
	 * 
	 * @return Iterator
	 */
	public Iterator<D> iterator()
	{
		return this.container.iterator();
	}

	/**
	 * Returns the size estimation of this task in bytes.
	 * 
	 * The size can only be estimated if the data contains the ISizeable
	 * interface.
	 * 
	 * @return size estimation
	 */
	public int byteSize()
	{
		return this.byteSize;
	}

	/**
	 * Returns the type of the task.
	 * 
	 * @param taskType
	 *            TaskType
	 */
	public void setTaskType(final TaskTypes taskType)
	{
		this.taskType = taskType;
	}

	/**
	 * Returns the part counter.
	 * 
	 * @return Part counter
	 */
	public int getPartCounter()
	{
		return this.partCounter;
	}

	/**
	 * Returns an unique task identifier consisting of article id and part
	 * counter.
	 * 
	 * @return unique task identifier
	 */
	public String uniqueIdentifier()
	{
		return this.header.getArticleId() + "-" + this.partCounter;
	}

	/*
	 * +DELEGATERS+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * Returns a string representation of the task.
	 * 
	 * @return string representation
	 */
	@Override
	public String toString()
	{
		return "[" + this.taskType.toString() + " <" + this.partCounter + ">"
				+ "\t" + this.byteSize + "\t| " + this.header.getArticleId()
				+ "\tR" + this.container.size() + "\t"
				+ this.header.getArticleName() + "]";
	}
}
