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
package de.tudarmstadt.ukp.wikipedia.parser;

/**
 * A NestedList can contain ContentElements or other NestedLists,
 * for this purpose and to avoid a improper use, this interface has been created.<br>
 *
 * Now, we got a NestedListContainer wich contains NestedLists<br>
 * A NestedList can be a NestedListContainer or a NestedListElement.
 *
 *
 */
public interface NestedList extends Content {
	public SrcSpan getSrcSpan();
}
