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
package de.tudarmstadt.ukp.wikipedia.wikimachine.factory;

import java.io.File;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;
import de.tudarmstadt.ukp.wikipedia.wikimachine.decompression.IDecompressor;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.DumpVersionProcessor;
import de.tudarmstadt.ukp.wikipedia.wikimachine.domain.ISnapshotGenerator;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.version.IDumpVersion;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.DumpTableInputStream;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.PageParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.RevisionParser;
import de.tudarmstadt.ukp.wikipedia.wikimachine.dump.xml.TextParser;

public class SpringFactory implements IEnvironmentFactory {

	private static final String INNER_APPLICATION_CONTEXT = "context/applicationContext.xml";

	private static final String OUTER_APPLICATION_CONTEXT = "applicationContext.xml";

	private static final String LOG_BEAN = "logger";

	private static final String DECOMPRESSOR_BEAN = "decompressor";

	private static final String DUMPVERSIONPROCESSOR_BEAN = "dumpVersionProcessor";

	private static final String PAGEPARSER_BEAN = "pageParser";

	private static final String SNAPSHOTGENERATOR_BEAN = "snapshotGenerator";

	private static final String REVISIONPARSER_BEAN = "revisionParser";

	private static final String TEXTPARSER_BEAN = "textParser";

	private static final String DUMPVERSION_BEAN = "dumpVersion";

	private static final String DUMPTABLEINPUTSTREAM_BEAN = "dumpTableInputStream";

	private static XmlBeanFactory factory = getBeanFactory();

	private static final SpringFactory instance = new SpringFactory();

	private static XmlBeanFactory getBeanFactory() {
		File outerContextFile = new File(OUTER_APPLICATION_CONTEXT);
		boolean outerContextFileProper = outerContextFile.exists()
				&& outerContextFile.isFile() && outerContextFile.canRead();
		Resource res = (outerContextFileProper) ? new FileSystemResource(
				outerContextFile) : new ClassPathResource(
				INNER_APPLICATION_CONTEXT);
		return new XmlBeanFactory(res);
	}

	public static SpringFactory getInstance() {
		return instance;
	}

	public ILogger getLogger() {
		return (ILogger) factory.getBean(LOG_BEAN);
	}

	public IDecompressor getDecompressor() {
		return (IDecompressor) factory.getBean(DECOMPRESSOR_BEAN);
	}

	@Override
	public ISnapshotGenerator getSnapshotGenerator() {
		return (ISnapshotGenerator) factory.getBean(SNAPSHOTGENERATOR_BEAN);
	}

	@Override
	public DumpVersionProcessor getDumpVersionProcessor() {
		return (DumpVersionProcessor) factory
				.getBean(DUMPVERSIONPROCESSOR_BEAN);
	}

	public IDumpVersion getDumpVersion() {
		return (IDumpVersion) factory.getBean(DUMPVERSION_BEAN);
	}

	public DumpTableInputStream getDumpTableInputStream() {
		return (DumpTableInputStream) factory
				.getBean(DUMPTABLEINPUTSTREAM_BEAN);
	}

	public PageParser getPageParser() {
		return (PageParser) factory.getBean(PAGEPARSER_BEAN);
	}

	public RevisionParser getRevisionParser() {
		return (RevisionParser) factory.getBean(REVISIONPARSER_BEAN);
	}

	public TextParser getTextParser() {
		return (TextParser) factory.getBean(TEXTPARSER_BEAN);
	}

}
