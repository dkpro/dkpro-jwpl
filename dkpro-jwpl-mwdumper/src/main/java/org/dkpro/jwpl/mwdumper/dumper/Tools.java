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
package org.dkpro.jwpl.mwdumper.dumper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

public class Tools
{
    static final int IN_BUF_SZ = 1024 * 1024;
    private static final int OUT_BUF_SZ = 1024 * 1024;

    // Start of files written by earlier versions: a superfluous "BZ" followed by the real header
    private static final byte[] LEGACY_BZIP2_HEADER = { 'B', 'Z', 'B', 'Z', 'h' };

    public static InputStream openInputFile(String arg) throws IOException
    {
        if (arg.equals("-")) {
            return openStandardInput();
        }
        InputStream infile = new BufferedInputStream(new FileInputStream(arg), IN_BUF_SZ);
        if (arg.endsWith(".gz")) {
            return new GZIPInputStream(infile);
        }
        else if (arg.endsWith(".bz2")) {
            return openBZip2Stream(infile);
        }
        else {
            return infile;
        }
    }

    static InputStream openStandardInput() throws IOException
    {
        return new BufferedInputStream(System.in, IN_BUF_SZ);
    }

    static InputStream openBZip2Stream(InputStream infile) throws IOException
    {
        InputStream in = infile.markSupported() ? infile : new BufferedInputStream(infile);
        // Earlier versions wrote an extra "BZ" in front of the stream header; skip it if present
        in.mark(LEGACY_BZIP2_HEADER.length);
        byte[] head = in.readNBytes(LEGACY_BZIP2_HEADER.length);
        in.reset();
        if (Arrays.equals(head, LEGACY_BZIP2_HEADER)) {
            in.skipNBytes(2);
        }
        return new BZip2CompressorInputStream(in);
    }

    static OutputStream openStandardOutput()
    {
        return new BufferedOutputStream(System.out, OUT_BUF_SZ);
    }

    static OutputStream createBZip2File(String param) throws IOException
    {
        // BZip2CompressorOutputStream writes the complete "BZh" header itself
        return new BZip2CompressorOutputStream(createOutputFile(param));
    }

    static OutputStream createOutputFile(String param) throws IOException
    {
        File file = new File(param);
        file.createNewFile();
        return new BufferedOutputStream(new FileOutputStream(file), OUT_BUF_SZ);
    }

    // ----------------

}
