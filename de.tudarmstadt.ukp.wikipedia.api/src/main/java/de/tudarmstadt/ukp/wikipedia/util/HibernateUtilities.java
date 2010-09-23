/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.util;

import java.util.*;

import org.hibernate.Session;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.hibernate.WikiHibernateUtil;

public class HibernateUtilities implements WikiConstants {

    private DatabaseConfiguration dbConfig;
    
    public HibernateUtilities(Language pLanguage, DatabaseConfiguration dbConfig) {
        this.dbConfig = dbConfig;
    }
    
    /** Hibernate IDs are needed to load an object from the database.
     *  Internal references are via pageIDs.
     * @return A mapping of pageIDs to hibernate IDs. 
     */
    public Map<Integer, Long> getIdMappingPages() {
        Map<Integer, Long> idMapping = new HashMap<Integer, Long>();
        
        Session session = WikiHibernateUtil.getSessionFactory(this.dbConfig).getCurrentSession();
        session.beginTransaction();
        Iterator results = session.createQuery("select page.id, page.pageId from Page as page").list().iterator();
        while (results.hasNext()) {
            Object[] row = (Object[]) results.next();
            // put (pageID, id)
            idMapping.put((Integer) row[1], (Long) row[0]);
        }
        session.getTransaction().commit();
        return idMapping;
    }

    /** Hibernate IDs are needed to load an object from the database.
     *  Internal references are via pageIDs.
     * @return A mapping of pageIDs to hibernate IDs. 
     */
    public Map<Integer, Long> getIdMappingCategories() {
        Map<Integer, Long> idMapping = new HashMap<Integer, Long>();
        
        Session session = WikiHibernateUtil.getSessionFactory(this.dbConfig).getCurrentSession();
        session.beginTransaction();
        Iterator results = session.createQuery("select cat.id, cat.pageId from Category as cat").list().iterator();
        while (results.hasNext()) {
            Object[] row = (Object[]) results.next();
            // put (pageID, id)
            idMapping.put((Integer) row[1], (Long) row[0]);
        }
        session.getTransaction().commit();
        return idMapping;
    }
}
