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
package org.dkpro.jwpl.wikimachine.factory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.dkpro.jwpl.wikimachine.debug.CompositeLogger;
import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.debug.Slf4JLogger;
import org.dkpro.jwpl.wikimachine.decompression.IDecompressor;
import org.dkpro.jwpl.wikimachine.decompression.UniversalDecompressor;
import org.dkpro.jwpl.wikimachine.domain.DumpVersionProcessor;
import org.dkpro.jwpl.wikimachine.dump.xml.PageParser;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base {@link IEnvironmentFactory} implementation that defines several common beans
 * for concrete subclasses.
 *
 * @see IEnvironmentFactory
 */
public abstract class AbstractEnvironmentFactory
    implements IEnvironmentFactory
{

    /**
     * Name of the system property that points to an optional {@code decompressor.xml}, which
     * configures external decompression utilities (for instance {@code lbzip2} or {@code pigz})
     * per archive extension. The external utilities are only used if this property is set.
     */
    public static final String DECOMPRESSOR_CONFIG_PROPERTY = "jwpl.decompressor.xml";

    /**
     * Name of the system property that, if set to {@code true}, runs the built-in decompression
     * on a separate thread, so that it overlaps with the parsing of the decompressed data. It is
     * disabled by default. External decompression utilities always run in a process of their own.
     */
    public static final String DECOMPRESSOR_READ_AHEAD_PROPERTY = "jwpl.decompressor.readahead";

    private static final Logger LOG = LoggerFactory.getLogger(AbstractEnvironmentFactory.class);

    private static ILogger LOG_BEAN;
    private static IDecompressor DECOMPRESSOR_BEAN;
    private static DumpVersionProcessor DUMPVERSIONPROCESSOR_BEAN;
    private static PageParser PAGEPARSER_BEAN;
    private static TextParser TEXTPARSER_BEAN;

    /**
     * Instantiates a {@link AbstractEnvironmentFactory}.
     */
    protected AbstractEnvironmentFactory() {}

    /**
     * {@inheritDoc}
     * <p>
     * Note: Realized via a singleton instance.
     */
    @Override
    public ILogger getLogger()
    {
        if (LOG_BEAN == null) {
            /* Note:
             * FileMemoryLogger not configured any longer as it spams new log files per run,
             * and it is easily replaceable by Slf4J config
             */
            List<ILogger> loggers = List.of(new Slf4JLogger());
            LOG_BEAN = new CompositeLogger(loggers.toArray(new ILogger[0]));
        }
        return LOG_BEAN;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: Realized via a singleton instance. External decompression utilities are configured
     * via the system property {@value #DECOMPRESSOR_CONFIG_PROPERTY}. Decompressing on a
     * separate thread is enabled via the system property
     * {@value #DECOMPRESSOR_READ_AHEAD_PROPERTY}.
     */
    @Override
    public IDecompressor getDecompressor()
    {
        if (DECOMPRESSOR_BEAN == null) {
            DECOMPRESSOR_BEAN = createDecompressor(System.getProperty(DECOMPRESSOR_CONFIG_PROPERTY),
                    Boolean.getBoolean(DECOMPRESSOR_READ_AHEAD_PROPERTY));
        }
        return DECOMPRESSOR_BEAN;
    }

    /**
     * Creates a {@link UniversalDecompressor} that additionally uses the external utilities
     * configured in {@code configLocation}. If no location is given, or the referenced file
     * does not exist, only the built-in decompression is available.
     *
     * @param configLocation The path to a {@code decompressor.xml} file, may be {@code null}.
     * @return A {@link UniversalDecompressor} instance, never {@code null}.
     */
    static UniversalDecompressor createDecompressor(String configLocation)
    {
        return createDecompressor(configLocation, false);
    }

    /**
     * Creates a {@link UniversalDecompressor} that additionally uses the external utilities
     * configured in {@code configLocation}. If no location is given, or the referenced file
     * does not exist, only the built-in decompression is available.
     *
     * @param configLocation The path to a {@code decompressor.xml} file, may be {@code null}.
     * @param readAhead      Whether the built-in decompression runs on a separate thread.
     * @return A {@link UniversalDecompressor} instance, never {@code null}.
     */
    static UniversalDecompressor createDecompressor(String configLocation, boolean readAhead)
    {
        if (readAhead) {
            LOG.info("Decompressing on a separate thread, ahead of the parser.");
        }
        if (configLocation == null || configLocation.isBlank()) {
            return new UniversalDecompressor(readAhead);
        }
        final Path config = Path.of(configLocation.trim());
        if (!Files.isRegularFile(config)) {
            LOG.warn("External decompressor configuration '{}' does not exist, "
                    + "using the built-in decompression.", config.toAbsolutePath());
            return new UniversalDecompressor(readAhead);
        }
        LOG.info("Using external decompressor configuration '{}'.", config.toAbsolutePath());
        return new UniversalDecompressor(config, readAhead);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: Realized via a singleton instance.
     */
    @Override
    public DumpVersionProcessor getDumpVersionProcessor()
    {
        if (DUMPVERSIONPROCESSOR_BEAN == null) {
          DUMPVERSIONPROCESSOR_BEAN = new DumpVersionProcessor(getLogger());
          DUMPVERSIONPROCESSOR_BEAN.setStep2Log(10000);
          DUMPVERSIONPROCESSOR_BEAN.setStep2GC(1000000);
          DUMPVERSIONPROCESSOR_BEAN.setStep2Flush(1000000);
        }
        return DUMPVERSIONPROCESSOR_BEAN;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: Realized via a singleton instance.
     */
    @Override
    public PageParser getPageParser()
    {
        if (PAGEPARSER_BEAN == null) {
          PAGEPARSER_BEAN = new PageParser();
        }
        return PAGEPARSER_BEAN;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: Realized via a singleton instance.
     */
    @Override
    public TextParser getTextParser()
    {
        if (TEXTPARSER_BEAN == null) {
          TEXTPARSER_BEAN = new TextParser();
        }
        return TEXTPARSER_BEAN;
    }

}
