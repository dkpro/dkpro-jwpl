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
package org.dkpro.jwpl.datamachine.dump.version;

import static org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion.formatBoolean;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;

import org.dkpro.jwpl.wikimachine.dump.version.AbstractDumpVersion;
import org.dkpro.jwpl.wikimachine.dump.xml.PageParser;
import org.dkpro.jwpl.wikimachine.dump.xml.RevisionParser;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;
import org.dkpro.jwpl.wikimachine.hashing.IStringHashCode;
import org.dkpro.jwpl.wikimachine.util.Redirects;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * A generic {@link org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion IDumpVersion} implementation.
 * <p>
 * Page, category and text ids are held in fastutil primitive collections to keep the heap footprint
 * low on large dumps. Titles are keyed by the full {@code KeyType} produced by the hash algorithm.
 *
 * @param <KeyType>         The type of keys to use.
 * @param <HashAlgorithm>   The hash algorithm to use.
 */
public class SingleDumpVersionJDKGeneric<KeyType, HashAlgorithm extends IStringHashCode>
    extends AbstractDumpVersion
{

    private static final String SQL_NULL = "NULL";
    // TODO This constant is used to flag page titles of discussion pages.
    // Is also defined in wikipedia.api:WikiConstants.DISCUSSION_PREFIX
    // It just doesn't make sense to add a dependency just for the constant
    private static final String DISCUSSION_PREFIX = "Discussion:";
    // Returned by the primitive maps for absent keys; page and text ids are always positive
    private static final int NO_ID = -1;

    private Int2ObjectOpenHashMap<String> pPageIdNameMap;
    private IntOpenHashSet cPageIdNameMap;
    private Object2IntOpenHashMap<KeyType> pNamePageIdMap;
    private Object2IntOpenHashMap<KeyType> cNamePageIdMap;
    private Int2ObjectOpenHashMap<String> rPageIdNameMap;
    private IntOpenHashSet disambiguations;
    private Int2IntOpenHashMap textIdPageIdMap;

    IStringHashCode hashAlgorithm;

    /**
     * Instantiates a {@link SingleDumpVersionJDKGeneric} object with the specified {@code hashAlgorithmClass}.
     *
     * @param hashAlgorithmClass        The concrete class of the {@link HashAlgorithm} to use.
     * @throws InstantiationException   Thrown if the underlying hash algorithm cannot be instantiated.
     * @throws IllegalAccessException   Thrown if the specified class can not be accessed.
     * @throws NoSuchMethodException    Thrown if there is no way to call the constructor of {@link HashAlgorithm}.
     * @throws InvocationTargetException Thrown in any other error cases during invocation of the constructor.
     */
    public SingleDumpVersionJDKGeneric(Class<HashAlgorithm> hashAlgorithmClass)
        throws InstantiationException, IllegalAccessException, NoSuchMethodException,
        InvocationTargetException
    {
        hashAlgorithm = hashAlgorithmClass.getDeclaredConstructor().newInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freeAfterCategoryLinksParsing()
    {
        cPageIdNameMap.clear();
        cNamePageIdMap.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freeAfterPageLinksParsing()
    {
        // nothing to free

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freeAfterPageParsing()
    {
        metaData.setNrOfCategories(cPageIdNameMap.size());
        metaData.setNrOfPages(pPageIdNameMap.size() + rPageIdNameMap.size());
        System.out.println("nrOfCategories: " + metaData.getNrOfCategories());
        System.out.println("nrOfPage: " + metaData.getNrOfPages());
        System.out.println("nrOfRedirects before testing the validity of the destination:"
                + rPageIdNameMap.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freeAfterRevisionParsing()
    {
        // nothing to free
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freeAfterTextParsing()
    {
        pPageIdNameMap.clear();
        cPageIdNameMap.clear();
        pNamePageIdMap.clear();
        cNamePageIdMap.clear();
        rPageIdNameMap.clear();
        disambiguations.clear();
        textIdPageIdMap.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Timestamp timestamp)
    {
        pPageIdNameMap = new Int2ObjectOpenHashMap<>(1_000_000);
        cPageIdNameMap = new IntOpenHashSet(1_000_000);
        pNamePageIdMap = new Object2IntOpenHashMap<>(1_000_000);
        pNamePageIdMap.defaultReturnValue(NO_ID);
        cNamePageIdMap = new Object2IntOpenHashMap<>(1_000_000);
        cNamePageIdMap.defaultReturnValue(NO_ID);
        rPageIdNameMap = new Int2ObjectOpenHashMap<>(1_000_000);
        disambiguations = new IntOpenHashSet(1_000_000);
        textIdPageIdMap = new Int2IntOpenHashMap(1_000_000);
        textIdPageIdMap.defaultReturnValue(NO_ID);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Integer categoryIdByTitle(String title)
    {
        return toId(cNamePageIdMap.getInt((KeyType) hashAlgorithm.hashCode(title)));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Integer pageIdByTitle(String title)
    {
        return toId(pNamePageIdMap.getInt((KeyType) hashAlgorithm.hashCode(title)));
    }

    /**
     * @param id An id returned by one of the name maps, or {@link #NO_ID} if the name is unknown.
     * @return The boxed {@code id}, or {@code null} if the name is unknown.
     */
    private static Integer toId(int id)
    {
        return id == NO_ID ? null : id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isKnownArticleId(int pageId)
    {
        return pPageIdNameMap.containsKey(pageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isKnownCategoryId(int pageId)
    {
        return cPageIdNameMap.contains(pageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordDisambiguation(int pageId)
    {
        disambiguations.add(pageId);
        metaData.addDisamb();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void processPageRow(PageParser pageParser)
    {
        int page_namespace = pageParser.getPageNamespace();
        int page_id = pageParser.getPageId();
        String page_title = pageParser.getPageTitle();
        if (page_title != null) {
            switch (page_namespace) {
            case NS_CATEGORY: {
                // skip redirect categories if skipCategory is enabled
                if (!(skipCategory && pageParser.getPageIsRedirect())) {
                    cPageIdNameMap.add(page_id);
                    cNamePageIdMap.put((KeyType) hashAlgorithm.hashCode(page_title), page_id);
                    txtFW.addRow(page_id, page_id, page_title);
                }
                break;
            }

            case NS_TALK: {
                page_title = DISCUSSION_PREFIX + page_title;
                // the NS_MAIN block will also be executed
                // for NS_TALK pages ...
            }

            case NS_MAIN: {
                if (pageParser.getPageIsRedirect()) {
                    rPageIdNameMap.put(page_id, page_title);
                }
                else {
                    pPageIdNameMap.put(page_id, page_title);
                    pNamePageIdMap.put((KeyType) hashAlgorithm.hashCode(page_title), page_id);
                }
                break;
            }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processRevisionRow(RevisionParser revisionParser)
    {
        textIdPageIdMap.put(revisionParser.getRevTextId(), revisionParser.getRevPage());
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void processTextRow(TextParser textParser)
    {
        int page_id = textIdPageIdMap.get(textParser.getOldId());
        if (page_id != NO_ID) {

            String page_idValueP = pPageIdNameMap.get(page_id);
            if (page_idValueP != null) { // pages
                page.addRow(page_id, page_id, page_idValueP, textParser.getOldText(),
                        formatBoolean(disambiguations.contains(page_id)));
                pageMapLine.addRow(page_id, page_idValueP, page_id, SQL_NULL, SQL_NULL);

            }
            else {
                String page_idValueR = rPageIdNameMap.get(page_id);
                if (page_idValueR != null) { // Redirects
                    String destination = Redirects.getRedirectDestination(textParser.getOldText());
                    if (destination != null) {
                        KeyType destinationHash = (KeyType) hashAlgorithm.hashCode(destination);
                        int destinationValue = pNamePageIdMap.getInt(destinationHash);
                        if (destinationValue != NO_ID) {

                            pageRedirects.addRow(destinationValue, page_idValueR);
                            pageMapLine.addRow(page_id, page_idValueR, destinationValue, SQL_NULL,
                                    SQL_NULL);
                            metaData.addRedirect();
                        }
                    }
                }
            }
        }
    }
}
