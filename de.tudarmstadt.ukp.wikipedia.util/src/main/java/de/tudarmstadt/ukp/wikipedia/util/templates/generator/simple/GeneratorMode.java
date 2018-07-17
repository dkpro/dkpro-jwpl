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
package de.tudarmstadt.ukp.wikipedia.util.templates.generator.simple;

import java.util.Map;
import java.util.Set;

/**
 * This class represents different modes used in WikipediaTemplateInfoGenerator
 * and is a container for data used fot generation
 *
 *
 */
public class GeneratorMode
{
	public boolean active_for_pages;

	public boolean active_for_revisions;

	public boolean useRevisionIterator;

	public Map<String, Set<Integer>> templateNameToRevId;

	public Map<String, Set<Integer>> templateNameToPageId;

}
