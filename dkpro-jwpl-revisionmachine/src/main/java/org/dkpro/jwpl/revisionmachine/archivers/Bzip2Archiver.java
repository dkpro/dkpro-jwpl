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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class provides basic bzip2 compression/decompression functionality
 */
public class Bzip2Archiver
{

    private static final Logger logger = LoggerFactory.getLogger(Bzip2Archiver.class);

    // Size to write in memory while compressing (in bytes)
    private static final int COMPRESSION_CACHE = 10000000;

    // Size to write in memory while decompressing (in bytes)
    private static final int DECOMPRESSION_CACHE = 10000000;

    /**
     * Creates bz2 archive file from file in path
     *
     * @param path
     *            to file to compress
     */
    public void compress(String path)
    {
        File fileToArchive = new File(path);
        File archivedFile = new File(fileToArchive.getName() + ".bz2");

        try (BufferedInputStream input = new BufferedInputStream(
                new FileInputStream(fileToArchive))) {

            archivedFile.createNewFile();

            try (FileOutputStream fos = new FileOutputStream(archivedFile)) {
                BufferedOutputStream bufStr = new BufferedOutputStream(fos);
                // added bzip2 prefix
                fos.write("BZ".getBytes());

                try (BZip2CompressorOutputStream bzip2 = new BZip2CompressorOutputStream(bufStr)) {
                    byte[] bytes = new byte[COMPRESSION_CACHE];
                    int read;
                    while ((read = input.read(bytes)) != -1) {
                        bzip2.write(bytes, 0, read);
                    }
                }
            }
        }
        catch (IOException e) {
            logger.error("Could not compress file [{}]", path, e);
        }
    }

    /**
     * Creates stream for compression
     *
     * @param path
     *            path to file to compress
     * @return compression stream
     * @throws IOException
     */
    public OutputStream getCompressionStream(String path) throws IOException
    {
        File archivedFile = new File(path);

        archivedFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(archivedFile);

        BufferedOutputStream bufStr = new BufferedOutputStream(fos);
        // added bzip2 prefix
        fos.write("BZ".getBytes());

        return new BZip2CompressorOutputStream(bufStr);
    }

    /**
     * Creates Stream for decompression
     *
     * @param path
     *            path to file to decompress
     * @param encoding
     *            encoding to use
     * @return decompression stream
     * @throws IOException
     */
    public InputStreamReader getDecompressionStream(String path, String encoding) throws IOException
    {
        BZip2CompressorInputStream input = new BZip2CompressorInputStream(
                new BufferedInputStream(new FileInputStream(path)));
        return new InputStreamReader(input, encoding);
    }

    /**
     * Uncompress bz2 file
     *
     * @param path
     *            path to file to uncompress
     * @throws IOException
     */
    public void decompress(String path) throws IOException
    {
        File bzip2 = new File(path);

        //
        File unarchived = new File(bzip2.getName().replace(".bz2", ""));

        unarchived.createNewFile();

        try (BufferedInputStream inputStr = new BufferedInputStream(new FileInputStream(bzip2))) {

            // read bzip2 prefix
            inputStr.read();
            inputStr.read();

            try (BZip2CompressorInputStream input = new BZip2CompressorInputStream(
                    new BufferedInputStream(inputStr));
                    FileOutputStream outStr = new FileOutputStream(unarchived)) {

                byte[] compressedBytes = new byte[DECOMPRESSION_CACHE];
                int byteRead;
                // A short read is not the end of the stream - only a -1 is, which the previous
                // 'read fewer bytes than asked for' check mistook for it (and then wrote a -1
                // length chunk).
                while ((byteRead = input.read(compressedBytes)) != -1) {
                    outStr.write(compressedBytes, 0, byteRead);
                }
            }
        }
    }

}
