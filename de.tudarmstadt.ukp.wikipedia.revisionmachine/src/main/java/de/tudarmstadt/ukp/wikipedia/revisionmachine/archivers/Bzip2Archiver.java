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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.archivers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;

/**
 * Class provides basic bzip2 compression/decompression functionality
 *
 *
 */
public class Bzip2Archiver
{

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
		try {

			File fileToArchive = new File(path);

			BufferedInputStream input = new BufferedInputStream(new FileInputStream(fileToArchive));

			File archivedFile = new File(fileToArchive.getName() + ".bz2");
			archivedFile.createNewFile();

			FileOutputStream fos = new FileOutputStream(archivedFile);
			BufferedOutputStream bufStr = new BufferedOutputStream(fos);
			// added bzip2 prefix
			fos.write("BZ".getBytes());
			CBZip2OutputStream bzip2 = new CBZip2OutputStream(bufStr);

			while (input.available() > 0) {
				int size = COMPRESSION_CACHE;

				if (input.available() < COMPRESSION_CACHE) {
					size = input.available();
				}
				byte[] bytes = new byte[size];

				input.read(bytes);

				bzip2.write(bytes);
			}
			bzip2.close();
			bufStr.close();
			fos.close();
			input.close();

		}
		catch (IOException e) {
			e.printStackTrace();
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
	public OutputStream getCompressionStream(String path)
		throws IOException
	{
		File archivedFile = new File(path);

		archivedFile.createNewFile();

		FileOutputStream fos = new FileOutputStream(archivedFile);

		BufferedOutputStream bufStr = new BufferedOutputStream(fos);
		// added bzip2 prefix
		fos.write("BZ".getBytes());

		CBZip2OutputStream bzip2 = new CBZip2OutputStream(bufStr);
		return bzip2;
	}

	/**
	 * Creates Stream for decompression
	 *
	 * @param path
	 *            path to file to uncompress
	 * @param encoding
	 *            ecoding to use
	 * @return decompression stream
	 * @throws IOException
	 */
	public InputStreamReader getDecompressionStream(String path, String encoding)
		throws IOException
	{
		File fileToUncompress = new File(path);

		BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(fileToUncompress));

		// read bzip2 prefix: BZ
		fileStream.read();
		fileStream.read();

		BufferedInputStream bufferedStream = new BufferedInputStream(fileStream);

		CBZip2InputStream input = new CBZip2InputStream(bufferedStream);

		return new InputStreamReader(input, encoding);

	}

	/**
	 * Uncompress bz2 file
	 *
	 * @param path
	 *            path to file to uncompress
	 * @throws IOException
	 */
	public void decompress(String path)
		throws IOException
	{
		File bzip2 = new File(path);

		//
		File unarchived = new File(bzip2.getName().replace(".bz2", ""));

		unarchived.createNewFile();

		BufferedInputStream inputStr = new BufferedInputStream(new FileInputStream(bzip2));

		// read bzip2 prefix
		inputStr.read();
		inputStr.read();

		BufferedInputStream buffStr = new BufferedInputStream(inputStr);

		CBZip2InputStream input = new CBZip2InputStream(buffStr);

		FileOutputStream outStr = new FileOutputStream(unarchived);

		while (true) {
			byte[] compressedBytes = new byte[DECOMPRESSION_CACHE];

			int byteRead = input.read(compressedBytes);

			outStr.write(compressedBytes, 0, byteRead);
			if (byteRead != DECOMPRESSION_CACHE) {
				break;
			}
		}

		input.close();
		buffStr.close();
		inputStr.close();
		outStr.close();
	}

}
