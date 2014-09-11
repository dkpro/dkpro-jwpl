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
