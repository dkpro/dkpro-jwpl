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
package org.dkpro.jwpl.datamachine.dump.xml;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

import org.dkpro.jwpl.datamachine.domain.DataMachineFiles;
import org.dkpro.jwpl.datamachine.file.DeleteFilesAtShutdown;
import org.dkpro.jwpl.mwdumper.importer.DumpWriter;
import org.dkpro.jwpl.mwdumper.importer.Page;
import org.dkpro.jwpl.mwdumper.importer.Revision;
import org.dkpro.jwpl.mwdumper.importer.Siteinfo;
import org.dkpro.jwpl.wikimachine.dump.sql.SQLEscape;
import org.dkpro.jwpl.wikimachine.util.Redirects;
import org.dkpro.jwpl.wikimachine.util.UTFDataOutputStream;

/**
 * A basic {@link DumpWriter} implementation to write binary dumps.
 * <p>
 * Two files are written: {@code page.bin} holds one
 * {@code (page id, namespace, title, is redirect)} record per page, and {@code text.bin} holds one
 * {@code (page id, text)} record per page, taken from the last revision of that page. Category
 * pages get no {@code text.bin} record, because the DataMachine never uses their text.
 *
 * @see DumpWriter
 */
public class SimpleBinaryDumpWriter
    implements DumpWriter
{

    // Namespace of category pages; their text is never used by the DataMachine
    private static final int NS_CATEGORY = 14;

    private final DataMachineFiles files;
    private UTFDataOutputStream pageFile;
    private UTFDataOutputStream textFile;

    private Page currentPage;
    private Revision lastRevision;

    /**
     * Instantiates a {@link SimpleBinaryDumpWriter} with the specified {@link DataMachineFiles}.
     *
     * @param files The {@link DataMachineFiles} to use for configuring the dump output.
     *              
     * @throws IOException Thrown if IO errors occurred.
     */
    public SimpleBinaryDumpWriter(DataMachineFiles files) throws IOException
    {
        this.files = files;
        if (this.files.isCompressGeneratedFiles()) {
            createCompressed();
        }
        else {
            createUncompressed();
        }
    }

    private void createUncompressed() throws IOException
    {
        pageFile = openUTFDataOutputStream(files.getGeneratedPage(), false);
        textFile = openUTFDataOutputStream(files.getGeneratedText(), false);
    }

    private void createCompressed() throws IOException
    {
        pageFile = openUTFDataOutputStream(files.getGeneratedPage(), true);
        textFile = openUTFDataOutputStream(files.getGeneratedText(), true);
    }

    private UTFDataOutputStream openUTFDataOutputStream(final String filePath, final boolean compressed)
        throws IOException
    {
        UTFDataOutputStream utfDataOutputStream;
        if (compressed) {
            utfDataOutputStream = new UTFDataOutputStream(
                    new GZIPOutputStream(openFileStreamAndRegisterDeletion(filePath)));
        }
        else {
            utfDataOutputStream = new UTFDataOutputStream(
                    openFileStreamAndRegisterDeletion(filePath));
        }
        return utfDataOutputStream;
    }

    private BufferedOutputStream openFileStreamAndRegisterDeletion(final String filePath)
        throws IOException
    {
        Path binaryOutputFilePath = Paths.get(filePath);
        // Javadoc says:
        // "truncate and overwrite an existing file, or create the file if it doesn't initially
        // exist"
        OutputStream fileOutputStream = Files.newOutputStream(binaryOutputFilePath);

        // Register a delete hook on JVM shutdown for this path
        DeleteFilesAtShutdown.register(binaryOutputFilePath);

        // Create a buffered version for this
        return new BufferedOutputStream(fileOutputStream);
    }

    private void updatePage(Page page, Revision revision) throws IOException
    {
        pageFile.writeInt(page.Id);
        pageFile.writeInt(page.Title.Namespace);
        pageFile.writeUTFAsArray(SQLEscape.escape(SQLEscape.titleFormat(page.Title.Text)));
        // pageFile.writeBoolean(revision.isRedirect());
        pageFile.writeBoolean(Redirects.isRedirect(revision.Text));
    }

    private void updateText(Page page, Revision revision) throws IOException
    {
        if (page.Title.Namespace != NS_CATEGORY) {
            // keyed by the page id, so the text can be joined against page.bin directly
            textFile.writeInt(page.Id);
            textFile.writeUTFAsArray(SQLEscape.escape(revision.Text));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        pageFile.close();
        textFile.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeEndPage() throws IOException
    {
        if (lastRevision != null) {
            updatePage(currentPage, lastRevision);
            updateText(currentPage, lastRevision);
        }
        currentPage = null;
        lastRevision = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeEndWiki() throws IOException
    {
        pageFile.flush();
        textFile.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeRevision(Revision revision)
    {
        // only the last revision of a page is written, see writeEndPage()
        lastRevision = revision;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeSiteinfo(Siteinfo info)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeStartPage(Page page)
    {
        currentPage = page;
        lastRevision = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeStartWiki()
    {
    }

}
