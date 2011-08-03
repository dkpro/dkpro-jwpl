/*******************************************************************************
 * Copyright (c) 2011 Ubiquitous Knowledge Processing (UKP) Lab
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     Oliver Ferschke
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

/**
 * This class gives access to the additional information created by
 * the TemplateInfoGenerator.
 *
 * @author Oliver Ferschke
 */
public class WikipediaTemplateInfo {

	private final Log logger = LogFactory.getLog(getClass());

    private final Wikipedia wiki;

    /**
     */
    public WikipediaTemplateInfo(Wikipedia pWiki) throws WikiApiException {
        this.wiki = pWiki;
    }

}
