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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.common.logging;

/**
 * This class contains all keys for diff tool loggers.
 *
 */
public enum LoggerType
{

	/** DiffTool Error Logger */
	DIFF_TOOL_ERROR,

	/** DiffTool Logger */
	DIFF_TOOL,

	/** Article Output Logger */
	ARTICLE_OUTPUT,

	/** UNCOMPRESSED Consumer Logger */
	CONSUMER_SQL,

	/** Diff Consumer Logger */
	CONSUMER_DIFF,

	/** Task Consumer Logger */
	CONSUMER_TASK,

	/** Artcile Producer Logger */
	PRODUCER_ARTICLES,

	/** Producer Archives Logger */
	PRODUCER_ARCHIVES,

	/** Diff Producer Logger */
	PRODUCER_DIFFS,

	/** Consumer Producer Logger */
	PRODUCER_CONSUMERS
}
