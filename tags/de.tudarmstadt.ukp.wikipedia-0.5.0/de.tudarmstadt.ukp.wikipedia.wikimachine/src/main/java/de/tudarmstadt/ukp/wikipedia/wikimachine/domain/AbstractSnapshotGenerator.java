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
