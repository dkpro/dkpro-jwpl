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
package org.dkpro.jwpl.wikimachine.decompression;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Factory to create {@link InputStream} depending on the filename's extension. If
 * there are a supported archive type we decorate {@link FileInputStream} with special
 * {@link InputStream} derivatives to decompress it on the fly. Otherwise, the possible
 * compression will be ignored and the plain unmodified byte stream will be returned. <br>
 * <br>
 * <p>
 * Current supported archives are: GZip, BZip2, and 7Zip. Each other archive type can be added
 * using the file "decompressor.xml" where you should specify the file extension as a key and the
 * according utility (incl. parameters), that have to be started.
 * <p>
 * Please note that the unpack utility has to use the standard output and external unpack
 * utilities are in preference to the internal.
 * Also, there could be more heap memory necessary to use start external programs.
 * <p>
 * The compressed file should be specified with the placeholder <code>%f</code>. <br>
 * For instance, the entry for the native 7z utility could look like that: <br>
 * {@code <entry key="7z">C:/Program Files/7-Zip/7z.exe e -so %f</entry>}. <br>
 * The properties file should conform to
 * <a href="http://java.sun.com/dtd/properties.dtd">Java Properties DTD</a>
 *
 * @see UniversalDecompressor#getInputStream(String)
 */
public class UniversalDecompressor
    implements IDecompressor
{

    /**
     * Placeholder for compressed file path in external command
     */
    public static final String FILEPLACEHOLDER = "%f";

    /**
     * Archive extensions which are supported by external utilities
     */
    private final Map<String, String> externalSupport;

    /**
     * Archive extensions which are supported by <code>ReaderFactory</code>
     */
    private final Map<String, IDecompressor> internalSupport;

    /**
     * Check if the file extension is supported by the external utility
     *
     * @param extension The file extension to check for.
     * @return {@code True} if this extension is supported with external utilities,
     *         {@code false} otherwise.
     */
    private boolean isExternalSupported(String extension)
    {
        return externalSupport.containsKey(extension);
    }

    /**
     * Check if the file extension is supported by the internal {@link IDecompressor}.
     *
     * @param extension The file extension to check for.
     * @return {@code True} if supported with internal utilities,
     *         {@code false} otherwise.
     */
    private boolean isInternalSupported(String extension)
    {
        return internalSupport.containsKey(extension);
    }

  /**
   * Instantiates a {@link UniversalDecompressor} supporting bz2, gz and 7z
   * compressed archives.
   */
    public UniversalDecompressor()
    {
        internalSupport = new HashMap<>();
        internalSupport.put("bz2", new BZip2Decompressor());
        internalSupport.put("gz", new GZipDecompressor());
        internalSupport.put("7z", new SevenZipDecompressor());
        externalSupport = new HashMap<>();
    }

    /**
     * Instantiates a {@link UniversalDecompressor} via an external
     * {@link Path} reference to a custom "decompressor.xml" file.
     * <p>
     * By default, bz2, gz and 7z compressed archives are supported.
     *
     * @param externalXML A valid {@link Path} reference to a
     *                    "decompressor.xml" file.
     */
    public UniversalDecompressor(Path externalXML)
    {
        this();
        loadExternal(externalXML);
    }

    /**
     * Load the properties for external utilities from an XML file.
     */
    private void loadExternal(Path externalConfig)
    {
        try {
          loadExternal(Files.newInputStream(externalConfig, StandardOpenOption.READ));
        }
        catch (IOException ignore) {
          // silently ignore it
        }
    }

    /**
     * Load the properties for external utilities from an XML file.
     */
    private void loadExternal(InputStream externalConfig)
    {
        Properties properties = new Properties();
        try {
          properties.loadFromXML(externalConfig);
          for (String key : properties.stringPropertyNames()) {
            externalSupport.put(key, properties.getProperty(key));
          }
        }
        catch (IOException ignore) {
            // silently ignore it
        }
    }


    /**
     * Return the extension of the filename.
     *
     * @param fileName The file's name or (relative) path to get the extension of.
     * @return file extension or {@code null}
     */
    private String getExtension(String fileName)
    {
        if (fileName == null) {
            return null;
        }

        String ext = null;
        int i = fileName.lastIndexOf('.');

        if (i > 0 && i < fileName.length() - 1) {
            ext = fileName.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * Check if the file is supported by the internal or external decompressor.
     *
     * @param fileName The file's name or (relative) path to check support for.
     * @return {@code True} if the file extension is supported, {@code false} otherwise.
     */
    public boolean isSupported(String fileName)
    {
        String extension = getExtension(fileName);

        return isInternalSupported(extension) || isExternalSupported(extension);
    }

    /**
     * Start an external utility to read the archive.
     *
     * @param fileName The file's name or (relative) path to read the archive from.
     * @return InputStream to read the decompressed data
     */
    private InputStream startExternal(String fileName)
    {
        InputStream result = null;
        try {
            String extension = getExtension(fileName);
            String command = externalSupport.get(extension).replace(FILEPLACEHOLDER, fileName);
            Process externalProcess = Runtime.getRuntime().exec(command);
            result = externalProcess.getInputStream();
        }
        catch (IOException ignore) {
          ignore.printStackTrace();
        }
        return result;
    }

    /**
     * Get default InputStream to read the data from the file
     *
     * @param fileName The file's name or (relative) path to read the archive from.
     * @return FileInputStream(fileName)
     */
    private InputStream getDefault(String fileName)
    {
        InputStream result = null;
        try {
            result = new BufferedInputStream(new FileInputStream(fileName));
        }
        catch (IOException ignore) {
        }

        return result;
    }

    /**
     * Creates a {@link InputStream} where the unpacked data could be read from. Internal GZip, BZip2,
     * and 7z archive formats are supported.
     * The list of archive formats can be increased with the {@code decompressor.xml}. Thereby
     * <ul>
     * <li>key is the archive extension</li>
     * <li>value is the external command. The archive file should be specified with a place holder
     * {@link UniversalDecompressor#FILEPLACEHOLDER}</li>
     * </ul>
     * External decompression utilities are in preference to the internal. If there is neither
     * external nor internal possibilities to unpack the file - the standard
     * {@link FileInputStream} will be returned
     *
     * @param fileName The file's name or (relative) path to read the archive from.
     */
    @Override
    public InputStream getInputStream(String fileName) throws IOException
    {
        InputStream inputStream;
        String extension = getExtension(fileName);

        if (isExternalSupported(extension) && fileExists(fileName)) {
            inputStream = startExternal(fileName);
        }
        else if (isInternalSupported(extension)) {
            inputStream = internalSupport.get(extension).getInputStream(fileName);
        }
        else {
            inputStream = getDefault(fileName);
        }
        return inputStream;
    }

    /**
     * Check if the {@link File} specified via {@code fileName} exists.
     *
     * @param fileName
     *            file path to check
     * @return {@code true} if the file exists and can be read, {@code false} otherwise.
     */
    private boolean fileExists(String fileName)
    {
        return new File(fileName).exists();
    }

}
