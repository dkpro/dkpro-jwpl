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
 *
 * @see DumpWriter
 */
public class SimpleBinaryDumpWriter
    implements DumpWriter
{

    private final DataMachineFiles files;
    private UTFDataOutputStream pageFile;
    private UTFDataOutputStream revisionFile;
    private UTFDataOutputStream textFile;

    private Page currentPage;
    private Revision lastRevision;

    /**
     * Instantiates a {@link SimpleBinaryDumpWriter} with the specified {@link DataMachineFiles configuration}.
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
        revisionFile = openUTFDataOutputStream(files.getGeneratedRevision(), false);
        textFile = openUTFDataOutputStream(files.getGeneratedText(), false);
    }

    private void createCompressed() throws IOException
    {
        pageFile = openUTFDataOutputStream(files.getGeneratedPage(), true);
        revisionFile = openUTFDataOutputStream(files.getGeneratedRevision(), true);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        pageFile.close();
        revisionFile.close();
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
        revisionFile.flush();
        textFile.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeRevision(Revision revision) throws IOException
    {
        lastRevision = revision;

        revisionFile.writeInt(currentPage.Id);
        revisionFile.writeInt(revision.Id);

        textFile.writeInt(revision.Id);
        textFile.writeUTFAsArray(SQLEscape.escape(revision.Text));
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
