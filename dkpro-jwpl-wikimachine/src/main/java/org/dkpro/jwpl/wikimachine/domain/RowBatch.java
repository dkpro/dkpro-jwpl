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
package org.dkpro.jwpl.wikimachine.domain;

import java.io.IOException;
import java.util.Arrays;

import org.dkpro.jwpl.wikimachine.dump.sql.CategoryLinkType;
import org.dkpro.jwpl.wikimachine.dump.sql.CategorylinksParser;
import org.dkpro.jwpl.wikimachine.dump.sql.PagelinksParser;
import org.dkpro.jwpl.wikimachine.dump.version.IDumpVersion;
import org.dkpro.jwpl.wikimachine.dump.xml.PageParser;

/**
 * A reusable buffer holding copies of consecutive rows of one table.
 * <p>
 * The parsers are mutable and positioned on one row at a time, so rows that are processed on
 * another thread have to be copied first. A batch is filled by the thread reading the dump and
 * replayed afterwards, possibly by several threads at once: {@link #replay} only reads the
 * buffered rows and hands them to a version through a view of its own, a parser instance that
 * never touches any input and is confined to the replaying thread.
 *
 * @param <P> The type of the parser whose rows are buffered.
 */
abstract class RowBatch<P>
{

    protected int size;

    /**
     * Copies the row the given parser is positioned on to the end of this batch.
     *
     * @param parser A parser positioned on a row.
     */
    abstract void add(P parser);

    /**
     * Hands every buffered row, in the order they were added, to the given version.
     *
     * @param version The version to process the rows.
     * @param handler Hands a single row to the version.
     * @throws IOException Thrown if the version fails to process a row.
     */
    abstract void replay(IDumpVersion version, RowHandler<P> handler) throws IOException;

    /**
     * @return The number of buffered rows.
     */
    int size()
    {
        return size;
    }

    /**
     * Discards all buffered rows, keeping the allocated capacity.
     */
    void clear()
    {
        clearReferences();
        size = 0;
    }

    /**
     * Releases the object references held by the buffered rows, so that a batch waiting to be
     * refilled does not keep them alive.
     */
    protected abstract void clearReferences();

    /**
     * @param current  The current capacity.
     * @return The capacity to grow to, if the batch is full.
     */
    protected static int grow(int current)
    {
        return Math.max(16, current * 2);
    }

    /**
     * Rows of the {@code page} table.
     */
    static final class Pages
        extends RowBatch<PageParser>
    {
        private int[] ids = new int[0];
        private int[] namespaces = new int[0];
        private String[] titles = new String[0];
        private boolean[] redirects = new boolean[0];

        @Override
        void add(PageParser parser)
        {
            if (size == ids.length) {
                final int capacity = grow(size);
                ids = Arrays.copyOf(ids, capacity);
                namespaces = Arrays.copyOf(namespaces, capacity);
                titles = Arrays.copyOf(titles, capacity);
                redirects = Arrays.copyOf(redirects, capacity);
            }
            ids[size] = parser.getPageId();
            namespaces[size] = parser.getPageNamespace();
            titles[size] = parser.getPageTitle();
            redirects[size] = parser.getPageIsRedirect();
            size++;
        }

        @Override
        void replay(IDumpVersion version, RowHandler<PageParser> handler) throws IOException
        {
            final View view = new View();
            for (int i = 0; i < size; i++) {
                view.row = i;
                handler.handle(version, view);
            }
        }

        @Override
        protected void clearReferences()
        {
            Arrays.fill(titles, 0, size, null);
        }

        private final class View
            extends PageParser
        {
            private int row;

            @Override
            public int getPageId()
            {
                return ids[row];
            }

            @Override
            public int getPageNamespace()
            {
                return namespaces[row];
            }

            @Override
            public String getPageTitle()
            {
                return titles[row];
            }

            @Override
            public boolean getPageIsRedirect()
            {
                return redirects[row];
            }

            @Override
            public boolean next()
            {
                throw new UnsupportedOperationException("A replayed row cannot be advanced.");
            }
        }
    }

    /**
     * Rows of the {@code categorylinks} table.
     */
    static final class Categorylinks
        extends RowBatch<CategorylinksParser>
    {
        private int[] froms = new int[0];
        private String[] tos = new String[0];
        private long[] targetIds = new long[0];
        private CategoryLinkType[] types = new CategoryLinkType[0];

        @Override
        void add(CategorylinksParser parser)
        {
            if (size == froms.length) {
                final int capacity = grow(size);
                froms = Arrays.copyOf(froms, capacity);
                tos = Arrays.copyOf(tos, capacity);
                targetIds = Arrays.copyOf(targetIds, capacity);
                types = Arrays.copyOf(types, capacity);
            }
            froms[size] = parser.getClFrom();
            tos[size] = parser.getClTo();
            targetIds[size] = parser.getClTargetId();
            types[size] = parser.getClType();
            size++;
        }

        @Override
        void replay(IDumpVersion version, RowHandler<CategorylinksParser> handler)
            throws IOException
        {
            final View view = new View();
            for (int i = 0; i < size; i++) {
                view.row = i;
                handler.handle(version, view);
            }
        }

        @Override
        protected void clearReferences()
        {
            Arrays.fill(tos, 0, size, null);
            Arrays.fill(types, 0, size, null);
        }

        private final class View
            extends CategorylinksParser
        {
            private int row;

            @Override
            public int getClFrom()
            {
                return froms[row];
            }

            @Override
            public String getClTo()
            {
                return tos[row];
            }

            @Override
            public long getClTargetId()
            {
                return targetIds[row];
            }

            @Override
            public CategoryLinkType getClType()
            {
                return types[row];
            }

            @Override
            public boolean next()
            {
                throw new UnsupportedOperationException("A replayed row cannot be advanced.");
            }

            @Override
            public void close()
            {
                // not attached to any input
            }
        }
    }

    /**
     * Rows of the {@code pagelinks} table.
     */
    static final class Pagelinks
        extends RowBatch<PagelinksParser>
    {
        private int[] froms = new int[0];
        private int[] namespaces = new int[0];
        private String[] tos = new String[0];
        private long[] targetIds = new long[0];

        @Override
        void add(PagelinksParser parser)
        {
            if (size == froms.length) {
                final int capacity = grow(size);
                froms = Arrays.copyOf(froms, capacity);
                namespaces = Arrays.copyOf(namespaces, capacity);
                tos = Arrays.copyOf(tos, capacity);
                targetIds = Arrays.copyOf(targetIds, capacity);
            }
            froms[size] = parser.getPlFrom();
            namespaces[size] = parser.getPlNamespace();
            tos[size] = parser.getPlTo();
            targetIds[size] = parser.getPlTargetId();
            size++;
        }

        @Override
        void replay(IDumpVersion version, RowHandler<PagelinksParser> handler)
            throws IOException
        {
            final View view = new View();
            for (int i = 0; i < size; i++) {
                view.row = i;
                handler.handle(version, view);
            }
        }

        @Override
        protected void clearReferences()
        {
            Arrays.fill(tos, 0, size, null);
        }

        private final class View
            extends PagelinksParser
        {
            private int row;

            @Override
            public int getPlFrom()
            {
                return froms[row];
            }

            @Override
            public int getPlNamespace()
            {
                return namespaces[row];
            }

            @Override
            public String getPlTo()
            {
                return tos[row];
            }

            @Override
            public long getPlTargetId()
            {
                return targetIds[row];
            }

            @Override
            public boolean next()
            {
                throw new UnsupportedOperationException("A replayed row cannot be advanced.");
            }

            @Override
            public void close()
            {
                // not attached to any input
            }
        }
    }
}
