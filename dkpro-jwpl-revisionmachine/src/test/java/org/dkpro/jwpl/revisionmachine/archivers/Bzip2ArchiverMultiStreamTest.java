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
package org.dkpro.jwpl.revisionmachine.archivers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Bzip2ArchiverMultiStreamTest
{

    @Test
    void testGetDecompressionStreamReadsAllStreamsOfMultistreamFile(@TempDir Path dir)
        throws IOException
    {
        final String contentA = "<page>first stream</page>\n";
        final String contentB = "<page>second stream</page>\n";
        final String contentC = "<page>third stream</page>\n";
        final Path multistream = dir.resolve("dump-multistream.xml.bz2");
        try (OutputStream os = Files.newOutputStream(multistream)) {
            for (String chunk : new String[] { contentA, contentB, contentC }) {
                final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                try (OutputStream bz2 = new BZip2CompressorOutputStream(stream)) {
                    bz2.write(chunk.getBytes(StandardCharsets.UTF_8));
                }
                stream.writeTo(os);
            }
        }

        try (InputStreamReader reader = new Bzip2Archiver()
                .getDecompressionStream(multistream.toString(), "UTF-8")) {
            final StringWriter decompressed = new StringWriter();
            reader.transferTo(decompressed);
            assertEquals(contentA + contentB + contentC, decompressed.toString());
        }
    }
}
