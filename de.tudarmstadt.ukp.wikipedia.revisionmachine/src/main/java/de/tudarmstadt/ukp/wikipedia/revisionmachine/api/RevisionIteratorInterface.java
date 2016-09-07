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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.api;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * The RevisionIteratorInterface extends the generic java.util.Iterator
 * Interface with a close() function.
 *
 * Since the IOException does not have inner exception in JAVA 1.5 the close
 * method has to throw both exception for both input components.
 *
 *
 *
 */
public interface RevisionIteratorInterface
	extends Iterator<Revision>
{

	/**
	 * Closes the reader or connection to the input component.
	 *
	 * @throws IOException
	 *             if an error occurs while reading from the input archive.
	 * @throws SQLException
	 *             if an error occurs while accessing the sql database.
	 */
	void close()
		throws IOException, SQLException;
}
