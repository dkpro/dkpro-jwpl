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

import java.util.List;

import org.dkpro.jwpl.wikimachine.debug.CompositeLogger;
import org.dkpro.jwpl.wikimachine.debug.FileMemoryLogger;
import org.dkpro.jwpl.wikimachine.debug.ILogger;
import org.dkpro.jwpl.wikimachine.debug.Slf4JLogger;
import org.dkpro.jwpl.wikimachine.decompression.IDecompressor;
import org.dkpro.jwpl.wikimachine.decompression.UniversalDecompressor;
import org.dkpro.jwpl.wikimachine.domain.DumpVersionProcessor;
import org.dkpro.jwpl.wikimachine.dump.xml.PageParser;
import org.dkpro.jwpl.wikimachine.dump.xml.TextParser;

/**
 * A base {@link IEnvironmentFactory} implementation that defines several common beans
 * for concrete subclasses.
 *
 * @see IEnvironmentFactory
 */
public abstract class AbstractEnvironmentFactory
    implements IEnvironmentFactory
{

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
          List<ILogger> loggers = List.of(new FileMemoryLogger(), new Slf4JLogger());
          LOG_BEAN = new CompositeLogger(loggers.toArray(new ILogger[0]));
        }
        return LOG_BEAN;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: Realized via a singleton instance.
     */
    @Override
    public IDecompressor getDecompressor()
    {
        if (DECOMPRESSOR_BEAN == null) {
            DECOMPRESSOR_BEAN = new UniversalDecompressor();
        }
        return DECOMPRESSOR_BEAN;
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
