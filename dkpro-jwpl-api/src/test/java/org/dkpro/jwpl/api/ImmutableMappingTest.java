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
package org.dkpro.jwpl.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.MappingMetamodel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests that the read-only entities and their element collections are mapped as immutable, so
 * that Hibernate neither keeps snapshots of loaded link sets nor dirty checks them at flush time.
 */
public class ImmutableMappingTest
    extends BaseJWPLTest
{

    private static final String A_FAMOUS_PAGE = "Wikipedia API";

    @BeforeAll
    public static void setupWikipedia()
    {
        try {
            wiki = new Wikipedia(obtainDbConfiguration());
        }
        catch (Exception e) {
            fail("Wikipedia could not be initialized: " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testAllEntitiesAndCollectionsAreImmutable()
    {
        List<String> mutable = wiki.__inTransaction(session -> {
            MappingMetamodel metamodel = session.getSessionFactory()
                    .unwrap(SessionFactoryImplementor.class).getMappingMetamodel();
            List<String> roles = new ArrayList<>();
            metamodel.forEachEntityDescriptor(p -> {
                if (p.isMutable()) {
                    roles.add(p.getEntityName());
                }
            });
            metamodel.forEachCollectionDescriptor(p -> {
                if (p.isMutable()) {
                    roles.add(p.getRole());
                }
            });
            return roles;
        });
        assertTrue(mutable.isEmpty(), "Mutable mappings: " + mutable);
    }

    @Test
    public void testLoadedLinkCollectionKeepsNoSnapshot() throws Exception
    {
        int pageId = wiki.getPage(A_FAMOUS_PAGE).getPageId();
        wiki.__inTransaction(session -> {
            org.dkpro.jwpl.api.hibernate.Page page = session
                    .createQuery("from Page where pageId = :id",
                            org.dkpro.jwpl.api.hibernate.Page.class)
                    .setParameter("id", pageId).uniqueResult();
            PersistentCollection<?> inLinks = assertInstanceOf(PersistentCollection.class,
                    page.getInLinks());
            assertFalse(page.getInLinks().isEmpty());
            assertTrue(inLinks.wasInitialized());
            assertNull(inLinks.getStoredSnapshot());
            return null;
        });
    }
}
