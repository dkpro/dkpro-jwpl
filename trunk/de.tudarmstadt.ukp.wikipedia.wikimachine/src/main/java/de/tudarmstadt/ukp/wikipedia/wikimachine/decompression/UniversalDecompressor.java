/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.wikimachine.decompression;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

/**
 * Factory to create <code>java.io.InputStream</code> depending on the
 * filename's extension. If there are a supported archive type we decorate
 * <code>FileInputStream</code> with special <code>InputStream</code>
 * derivatives to uncompress it on the fly. Otherwise the possible compression
 * will be ignored and the plain unmodified byte stream will be returned. <br>
 * <br>
 * 
 * Current supported archives are: GZip, BZip2. Each other archive type can be
 * added using the file "decompressor.xml" where you should specify the file
 * extension as a key and the accordant utility (incl. parameters), that have to
 * be started. Please notice, that the unpack utility have to use the standard
 * output and external unpack utilities are in preference to the internal. Also
 * there could be more heap memory necessary to use start external programs. The
 * compressed file should be specified with the place holder <code>%f</code>. <br>
 * E.g. the entry for the 7z utility could look like that: <br> {@code <entry
 * key="7z">7z e -so %f</entry>}. The properties file should confirm to
 * {@link http://java.sun.com/dtd/properties.dtd}
 * 
 * @author ivan.galkin
 * 
 * @see UniversalDecompressor#getInputStream(String)
 * 
 */

public class UniversalDecompressor implements IDecompressor {

	/**
	 * Place holder for compressed file path in external command
	 */
	public static final String FILEPLACEHOLDER = "%f";
	/**
	 * File path to decompressor properties files
	 */
	private static final String PROPERTIES_PATH = "decompressor.xml";

	/**
	 * Archive extensions which are supported by external utilities
	 */
	private HashMap<String, String> externalSupport;

	/**
	 * Archive extensions which are supported by <code>ReaderFactory</code>
	 */
	private HashMap<String, IDecompressor> internalSupport;

	/**
	 * Check if the file extension is supported by the external utility
	 * 
	 * @param extension
	 * @return true if this extension is supported with external utilities
	 */
	private boolean isExternalSupported(String extension) {
		return externalSupport.containsKey(extension);
	}

	/**
	 * Check if the file extension is supported by the internal
	 * <code>IDecompressor</code>
	 * 
	 * @param extension
	 * @return
	 */
	private boolean isInternalSupported(String extension) {
		return internalSupport.containsKey(extension);
	}

	/**
	 * Don't let anyone instantiate this class - set the constructor to private
	 */
	public UniversalDecompressor() {
		internalSupport = new HashMap<String, IDecompressor>();
		internalSupport.put("bz2", new BZip2Decompressor());
		internalSupport.put("gz", new GZipDecompressor());

		externalSupport = new HashMap<String, String>();
		loadExternal();
	}

	/**
	 * Load the properties for external utilities from a XML file
	 */
	private void loadExternal() {
		Properties properties = new Properties();
		try {
			properties.loadFromXML(new FileInputStream(PROPERTIES_PATH));
			for (String key : properties.stringPropertyNames()) {
				externalSupport.put(key, properties.getProperty(key));
			}
		} catch (IOException ignore) {
		}
	}

	/**
	 * Return the extension of the filename
	 * 
	 * @param fileName
	 *            that should be inputed
	 * @return file extension or null
	 */
	private String getExtension(String fileName) {
		if (fileName == null)
			return null;

		String ext = null;
		int i = fileName.lastIndexOf('.');

		if (i > 0 && i < fileName.length() - 1) {
			ext = fileName.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	/**
	 * Check if the file is supported by the internal or external decompressor
	 * 
	 * @param fileName
	 * @return true if the file extension is supported
	 */
	public boolean isSupported(String fileName) {
		String extension = getExtension(fileName);

		return isInternalSupported(extension) || isExternalSupported(extension);
	}

	/**
	 * Start an external utility to unpack the the archive
	 * 
	 * @param fileName
	 * @return InputStream to read the decompressed data
	 */
	private InputStream startExternal(String fileName) {
		InputStream result = null;
		try {
			String extension = getExtension(fileName);
			String command = externalSupport.get(extension).replace(
					FILEPLACEHOLDER, fileName);
			Process externalProcess = Runtime.getRuntime().exec(command);
			result = externalProcess.getInputStream();
		} catch (IOException ignore) {
		}
		return result;
	}

	/**
	 * Get default InputStream to read the data from the file
	 * 
	 * @param fileName
	 * @return FileInputStream(fileName)
	 */
	private InputStream getDefault(String fileName) {
		InputStream result = null;
		try {
			result = new FileInputStream(fileName);
		} catch (IOException ignore) {
		}

		return result;
	}

	/**
	 * Creates a InputStream where the unpacked data could be read from.
	 * Internal GZip and BZip2 archive formats are supported. The list archive
	 * formats can be increased with settings file decompressor.xml. Thereby
	 * <ul>
	 * <li>key is the archive extension</li>
	 * <li>value is the external command. The archive file should be specified
	 * with an place holder <code>UniversalDecompressor.FILEPLACEHOLDER</code></li>
	 * </ul>
	 * External decompression utilities are in preference to the internal. If
	 * there is nether external nor internal possibilities to unpack the file -
	 * the standard <code>FileInputSteam</code> will be returned
	 * 
	 * @see UniversalDecompressor
	 */
	@Override
	public InputStream getInputStream(String fileName) throws IOException {
		InputStream inputStream = null;
		if (fileExists(fileName)) {
			String extension = getExtension(fileName);

			if (isExternalSupported(extension)) {
				inputStream = startExternal(fileName);
			} else if (isInternalSupported(extension)) {
				inputStream = internalSupport.get(extension).getInputStream(
						fileName);
			} else {
				inputStream = getDefault(fileName);
			}
		}
		return inputStream;
	}

	/**
	 * Check if the specified file exists
	 * 
	 * @param fileName
	 *            file path to check
	 * @return bool if the file exists and can be read
	 */
	private boolean fileExists(String fileName) {
		return new File(fileName).exists();
	}

}
