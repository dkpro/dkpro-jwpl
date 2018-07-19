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
package de.tudarmstadt.ukp.wikipedia.wikimachine.domain;

import de.tudarmstadt.ukp.wikipedia.wikimachine.debug.ILogger;
import de.tudarmstadt.ukp.wikipedia.wikimachine.decompression.IDecompressor;
import de.tudarmstadt.ukp.wikipedia.wikimachine.factory.IEnvironmentFactory;

public abstract class AbstractSnapshotGenerator implements ISnapshotGenerator {
	protected Configuration configuration = null;
	protected IDecompressor decompressor = null;
	protected ILogger logger = null;
	protected DumpVersionProcessor dumpVersionProcessor = null;
	protected IEnvironmentFactory environmentFactory = null;

	public AbstractSnapshotGenerator(IEnvironmentFactory environmentFactory) {
		this.decompressor = environmentFactory.getDecompressor();
		this.logger = environmentFactory.getLogger();
		this.dumpVersionProcessor = environmentFactory
				.getDumpVersionProcessor();

		this.environmentFactory = environmentFactory;
	}

	@Override
	public abstract void setFiles(Files files);

	@Override
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public abstract void start() throws Exception;
}
