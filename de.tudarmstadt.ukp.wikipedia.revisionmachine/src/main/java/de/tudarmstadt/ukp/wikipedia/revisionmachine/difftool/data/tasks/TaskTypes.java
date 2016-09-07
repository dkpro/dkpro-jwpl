/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.data.tasks;

/**
 * This Enumerator lists the different types of tasks.
 *
 *
 *
 */
public enum TaskTypes
{

	/** dummy task */
	DUMMY,

	/** if this task is received from a consumer, it will shutdown afterwards */
	ENDTASK,

	/** if the article id is black listed */
	BANNED_TASK,

	/** full task containing all revisions of one article */
	TASK_FULL,

	/** task containing the first part of revisions of one article */
	TASK_PARTIAL_FIRST,

	/** task containing some revisions of one article */
	TASK_PARTIAL,

	/** task containing the last part of revisions from one article */
	TASK_PARTIAL_LAST
}
