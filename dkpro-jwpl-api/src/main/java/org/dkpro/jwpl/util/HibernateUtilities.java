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
package org.dkpro.jwpl.util;

import java.util.HashMap;
import java.util.Map;

import org.dkpro.jwpl.api.WikiConstants;
import org.hibernate.Session;

import org.dkpro.jwpl.api.DatabaseConfiguration;
import org.dkpro.jwpl.api.hibernate.WikiHibernateUtil;

/**
 * @deprecated To be removed without replacement.
 */
@Deprecated(since="2.0.0", forRemoval=true)
public class HibernateUtilities implements WikiConstants {

  private final DatabaseConfiguration dbConfig;

  public HibernateUtilities(Language pLanguage, DatabaseConfiguration dbConfig) {
    this.dbConfig = dbConfig;
  }

  /**
   * Hibernate IDs are needed to load an object from the database.
   * Internal references are via pageIDs.
   *
   * @return A mapping of pageIDs to hibernate IDs.
   */
  public Map<Integer, Long> getIdMappingPages() {
    Map<Integer, Long> idMapping = new HashMap<>();

    Session session = WikiHibernateUtil.getSessionFactory(this.dbConfig).getCurrentSession();
    session.beginTransaction();
    for (Object o : session.createQuery("select page.id, page.pageId from Page as page").list()) {
      Object[] row = (Object[]) o;
      // put (pageID, id)
      idMapping.put((Integer) row[1], (Long) row[0]);
    }
    session.getTransaction().commit();
    return idMapping;
  }

  /**
   * Hibernate IDs are needed to load an object from the database.
   * Internal references are via pageIDs.
   *
   * @return A mapping of pageIDs to hibernate IDs.
   */
  public Map<Integer, Long> getIdMappingCategories() {
    Map<Integer, Long> idMapping = new HashMap<>();

    Session session = WikiHibernateUtil.getSessionFactory(this.dbConfig).getCurrentSession();
    session.beginTransaction();
    for (Object o : session.createQuery("select cat.id, cat.pageId from Category as cat").list()) {
      Object[] row = (Object[]) o;
      // put (pageID, id)
      idMapping.put((Integer) row[1], (Long) row[0]);
    }
    session.getTransaction().commit();
    return idMapping;
  }
}
