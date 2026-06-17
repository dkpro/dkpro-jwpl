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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.dkpro.jwpl.datamachine.domain.DataMachineFiles;
import org.dkpro.jwpl.mwdumper.importer.DumpWriter;
import org.dkpro.jwpl.mwdumper.importer.NamespaceFilter;
import org.dkpro.jwpl.mwdumper.importer.XmlDumpReader;
import org.dkpro.jwpl.wikimachine.dump.xml.MultiPartXmlDumpReader;

/**
 * Use org.mediawiki.importer engine to parse the XML-Dump (only useful fields) and store it to
 * binary file. Compression of the output files is possible.
 *
 * @see SimpleBinaryDumpWriter
 * @see SimpleXmlDumpReader
 * @see XmlDumpReader
 */
public class XML2Binary
{

    /*
     * Enable the main and category pages as well as discussions
     */
    private static final String ENABLED_NAMESPACES = "NS_MAIN,NS_TALK,NS_CATEGORY";

    private static final boolean USE_MODIFIED_PARSER = true;

    /**
     * Instantiates a {@link XML2Binary} object with the specified parameters.
     *
     * @param iStream   The {@link InputStream} containing the XML data to process.
     * @param files     The {@link DataMachineFiles} configuration to apply.
     * @throws IOException Thrown if IO errors occurred during processing.
     */
    public XML2Binary(InputStream iStream, DataMachineFiles files) throws IOException
    {
        final DumpWriter writer = new NamespaceFilter(new SimpleBinaryDumpWriter(files),
                ENABLED_NAMESPACES);
        if (USE_MODIFIED_PARSER) {
            // modified parser, skips faulty tags
            new SimpleXmlDumpReader(iStream, writer).readDump();
        }
        else {
            // original MWDumper parser, very sensible to not closed tags
            new XmlDumpReader(iStream, writer).readDump();
        }
    }

    /**
     * Instantiates an {@link XML2Binary} for a multi-part Wikipedia XML dump. Every stream in
     * {@code iStreams} must be a self-contained XML document with its own {@code <mediawiki>}
     * root; events across parts are collapsed into a single logical document by the underlying
     * {@link MultiPartXmlDumpReader}.
     *
     * @param iStreams Ordered list of XML part streams (ascending page-range). Must not be
     *                 {@code null} or empty; must not contain {@code null} elements.
     * @param files    The {@link DataMachineFiles} configuration to apply.
     * @throws IOException Thrown if IO errors occurred during processing.
     */
    public XML2Binary(List<InputStream> iStreams, DataMachineFiles files) throws IOException
    {
        final DumpWriter writer = new NamespaceFilter(new SimpleBinaryDumpWriter(files),
                ENABLED_NAMESPACES);
        // The modified parser is always used for multi-part — the original XmlDumpReader is
        // only kept as a fallback for its stricter single-document parsing.
        MultiPartXmlDumpReader.readDumps(iStreams, writer, SimpleXmlDumpReader::new);
    }

}
