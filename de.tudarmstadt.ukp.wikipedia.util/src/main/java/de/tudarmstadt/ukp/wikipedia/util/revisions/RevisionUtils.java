package de.tudarmstadt.ukp.wikipedia.util.revisions;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;

/**
 * Provides several revision-related utilities that should not be part of the RevisionMachine 
 * package because of dependencies to the JWPL API. RevisionMachine should stay independent
 * from the RevisionMachine
 * 
 * @author Oliver Ferschke
 *
 */
public class RevisionUtils {
	RevisionApi revApi;
	Wikipedia wiki;
	
	public RevisionUtils(DatabaseConfiguration conf) throws WikiApiException{
		wiki = new Wikipedia(conf);
		revApi = new RevisionApi(conf);	
	}

	public RevisionUtils(Wikipedia wiki, RevisionApi revApi) throws WikiApiException{
		this.revApi=revApi;
		this.wiki=wiki;
	}
	
	/**
	 * For a given article revision, the method returns the revision of the article discussion 
	 * page which was current at the time the revision was created.
	 * 
	 * @param revisionId revision of the article for which the talk page revision should be retrieved
	 * @return the revision of the talk page that was current at the creation time of the given article revision
	 */
	public Revision getDiscussionRevisionForArticleRevision(int revisionId){
		//TODO not yet implemented
		return null;
	}
}
