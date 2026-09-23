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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.dkpro.jwpl.api.hibernate.WikiHibernateUtil;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Checks that plain field getters of already loaded entities read the field directly, rather than
 * opening a Hibernate session and a JDBC transaction per call.
 */
public class TransactionCountTest
    extends BaseJWPLTest
{

    private static final int A_FAMOUS_PAGE_ID = 1017;

    private static Statistics statistics;

    @BeforeAll
    public static void setupWikipedia() throws Exception
    {
        DatabaseConfiguration db = obtainDbConfiguration();
        wiki = new Wikipedia(db);
        statistics = WikiHibernateUtil.getSessionFactory(db).getStatistics();
        statistics.setStatisticsEnabled(true);
    }

    @AfterAll
    public static void tearDownStatistics()
    {
        statistics.setStatisticsEnabled(false);
    }

    @Test
    public void testPageFieldGettersRunNoTransaction() throws Exception
    {
        Page page = wiki.getPage(A_FAMOUS_PAGE_ID);

        long before = statistics.getTransactionCount();
        page.getTitle();
        page.getText();
        page.isDisambiguation();
        assertEquals(before, statistics.getTransactionCount());
    }

    @Test
    public void testCategoryFieldGettersRunNoTransaction() throws Exception
    {
        Category cat = wiki.getCategory("UKP");

        long before = statistics.getTransactionCount();
        cat.getTitle();
        cat.getPageId();
        cat.__getId();
        assertEquals(before, statistics.getTransactionCount());
    }

    @Test
    public void testMetaDataFieldGettersRunNoTransaction() throws Exception
    {
        MetaData metaData = wiki.getMetaData();

        long before = statistics.getTransactionCount();
        metaData.getId();
        metaData.getNumberOfCategories();
        metaData.getNumberOfPages();
        metaData.getNumberOfDisambiguationPages();
        metaData.getNumberOfRedirectPages();
        metaData.getVersion();
        assertEquals(before, statistics.getTransactionCount());
    }

    @Test
    public void testCategoryCountsRunASingleTransaction() throws Exception
    {
        Category cat = wiki.getCategory("UKP");

        long before = statistics.getTransactionCount();
        cat.getNumberOfParents();
        assertEquals(before + 1, statistics.getTransactionCount());

        before = statistics.getTransactionCount();
        cat.getNumberOfChildren();
        assertEquals(before + 1, statistics.getTransactionCount());

        before = statistics.getTransactionCount();
        cat.getNumberOfPages();
        assertEquals(before + 1, statistics.getTransactionCount());
    }
}
