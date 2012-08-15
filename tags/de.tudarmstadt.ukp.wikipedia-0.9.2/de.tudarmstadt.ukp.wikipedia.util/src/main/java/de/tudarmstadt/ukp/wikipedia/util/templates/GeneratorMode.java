package de.tudarmstadt.ukp.wikipedia.util.templates;

import java.util.Map;
import java.util.Set;

/**
 * This class represents different modes used in WikipediaTemplateInfoGenerator
 * and is a container for data used fot generation
 *
 * @author Artem Vovk
 *
 */
public class GeneratorMode
{
	public boolean active_for_pages;

	public boolean active_for_revisions;

	public boolean useRevisionIterator;

	public Map<String, Set<Integer>> templateNameToRevId;

	public Map<String, Set<Integer>> templateNameToPageId;

}
