/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 *     Samy Ateia - provided a patch via the JWPL mailing list
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.parser.mediawiki;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

/**
 * Class for easy creating a configurated MediaWiki Parser...<br/>
 * @author CJacobi
 *
 */
public class MediaWikiParserFactory {

	private final Log logger = LogFactory.getLog(getClass());

	private Class parserClass;
	private Class templateParserClass;
	private String lineSeparator;
	private List<String> deleteTemplates;
	private List<String> parseTemplates;
	private List<String> categoryIdentifers;
	private List<String> languageIdentifers;
	private List<String> imageIdentifers;
	private boolean showImageText;
	private boolean deleteTags;
	private boolean showMathTagContent;
	private boolean calculateSrcSpans;

	/**
	 * Creates a new UNCONFIGURATED Parser Factory.
	 */
	public MediaWikiParserFactory(){
		initVariables();
	}

	/**
	 * Creates a fully configurated parser factory for the specified language.<br/>
	 * Next step is .createParser()...
	 */
	public MediaWikiParserFactory(Language language){
        initVariables();
        if (language.equals(Language.german)) {
            initGermanVariables();
        }
        else if(language.equals(Language.english)){
        	initEnglishVariables();
        }else
        {
        	logger.warn("No language specific parser for "+language.toString()+" available. Using default values.");
        }
	}

	private void initVariables(){
        lineSeparator = "LF";
		parserClass = ModularParser.class;
		imageIdentifers = new ArrayList<String>();
		categoryIdentifers = new ArrayList<String>();
		languageIdentifers = new ArrayList<String>();
		deleteTemplates = new ArrayList<String>();
		parseTemplates = new ArrayList<String>();
		showImageText = false;
		deleteTags = true;
		showMathTagContent = true;
		calculateSrcSpans = false;
		templateParserClass = ShowTemplateNamesAndParameters.class;

        initLanguages();
	}

	private void initLanguages(){
		//Init the Languages...
		languageIdentifers.add("aa");languageIdentifers.add("ab");languageIdentifers.add("af");
		languageIdentifers.add("am");languageIdentifers.add("an");languageIdentifers.add("ar");
		languageIdentifers.add("as");languageIdentifers.add("av");languageIdentifers.add("ay");
		languageIdentifers.add("az");

		languageIdentifers.add("ba");languageIdentifers.add("be");languageIdentifers.add("bg");
		languageIdentifers.add("bh");languageIdentifers.add("bi");languageIdentifers.add("bm");
		languageIdentifers.add("bn");languageIdentifers.add("bo");languageIdentifers.add("br");
		languageIdentifers.add("bs");

		languageIdentifers.add("ca");languageIdentifers.add("ce");languageIdentifers.add("ch");
		languageIdentifers.add("co");languageIdentifers.add("cr");languageIdentifers.add("cs");
		languageIdentifers.add("cv");languageIdentifers.add("cy");

		languageIdentifers.add("da");languageIdentifers.add("de");languageIdentifers.add("dk");
		languageIdentifers.add("dv");languageIdentifers.add("dz");

		languageIdentifers.add("ee");languageIdentifers.add("el");languageIdentifers.add("en");
		languageIdentifers.add("eo");languageIdentifers.add("es");languageIdentifers.add("et");
		languageIdentifers.add("eu");

		languageIdentifers.add("fa");languageIdentifers.add("ff");languageIdentifers.add("fi");
		languageIdentifers.add("fj");languageIdentifers.add("fo");languageIdentifers.add("fr");
		languageIdentifers.add("fy");

		languageIdentifers.add("ga");languageIdentifers.add("gd");languageIdentifers.add("gl");
		languageIdentifers.add("gn");languageIdentifers.add("gu");languageIdentifers.add("gv");

		languageIdentifers.add("ha");languageIdentifers.add("he");languageIdentifers.add("hi");
		languageIdentifers.add("hr");languageIdentifers.add("ht");languageIdentifers.add("hu");
		languageIdentifers.add("hy");

		languageIdentifers.add("ia");languageIdentifers.add("id");languageIdentifers.add("ie");
		languageIdentifers.add("ig");languageIdentifers.add("ii");languageIdentifers.add("ik");
		languageIdentifers.add("io");languageIdentifers.add("is");languageIdentifers.add("it");
		languageIdentifers.add("iu");

		languageIdentifers.add("ja");languageIdentifers.add("jv");

		languageIdentifers.add("ka");languageIdentifers.add("kg");languageIdentifers.add("ki");
		languageIdentifers.add("kk");languageIdentifers.add("kl");languageIdentifers.add("km");
		languageIdentifers.add("kn");languageIdentifers.add("ko");languageIdentifers.add("ks");
		languageIdentifers.add("ku");languageIdentifers.add("kv");languageIdentifers.add("kw");
		languageIdentifers.add("ky");

		languageIdentifers.add("la");languageIdentifers.add("lb");languageIdentifers.add("li");
		languageIdentifers.add("ln");languageIdentifers.add("lo");languageIdentifers.add("lt");
		languageIdentifers.add("lv");

		languageIdentifers.add("mg");languageIdentifers.add("mh");languageIdentifers.add("mi");
		languageIdentifers.add("mk");languageIdentifers.add("ml");languageIdentifers.add("mn");
		languageIdentifers.add("mo");languageIdentifers.add("mr");languageIdentifers.add("ms");
		languageIdentifers.add("mt");languageIdentifers.add("my");

		languageIdentifers.add("na");languageIdentifers.add("nb");languageIdentifers.add("ne");
		languageIdentifers.add("ng");languageIdentifers.add("nl");languageIdentifers.add("nn");
		languageIdentifers.add("no");languageIdentifers.add("nv");languageIdentifers.add("ny");

		languageIdentifers.add("oc");languageIdentifers.add("os");languageIdentifers.add("pa");
		languageIdentifers.add("pl");languageIdentifers.add("ps");languageIdentifers.add("pt");

		languageIdentifers.add("qu");

		languageIdentifers.add("rm");languageIdentifers.add("rn");languageIdentifers.add("ro");
		languageIdentifers.add("ru");languageIdentifers.add("rw");

		languageIdentifers.add("sa");languageIdentifers.add("sc");languageIdentifers.add("sd");
		languageIdentifers.add("se");languageIdentifers.add("sg");languageIdentifers.add("sh");
		languageIdentifers.add("si");languageIdentifers.add("sk");languageIdentifers.add("sl");
		languageIdentifers.add("sm");languageIdentifers.add("sn");languageIdentifers.add("so");
		languageIdentifers.add("sq");languageIdentifers.add("sr");languageIdentifers.add("ss");
		languageIdentifers.add("st");languageIdentifers.add("su");languageIdentifers.add("sv");
		languageIdentifers.add("sw");

		languageIdentifers.add("ta");languageIdentifers.add("te");languageIdentifers.add("tg");
		languageIdentifers.add("th");languageIdentifers.add("ti");languageIdentifers.add("tk");
		languageIdentifers.add("tl");languageIdentifers.add("tn");languageIdentifers.add("to");
		languageIdentifers.add("tr");languageIdentifers.add("ts");languageIdentifers.add("tt");
		languageIdentifers.add("tw");languageIdentifers.add("ty");

		languageIdentifers.add("ug");languageIdentifers.add("uk");languageIdentifers.add("ur");
		languageIdentifers.add("uz");

		languageIdentifers.add("ve");languageIdentifers.add("vi");languageIdentifers.add("vo");

		languageIdentifers.add("wa");languageIdentifers.add("wo");

		languageIdentifers.add("xh");

		languageIdentifers.add("yi");languageIdentifers.add("yo");

		languageIdentifers.add("za");languageIdentifers.add("zh");languageIdentifers.add("zu");

		languageIdentifers.add("als");languageIdentifers.add("ang");languageIdentifers.add("arc");languageIdentifers.add("ast");
		languageIdentifers.add("bug");
		languageIdentifers.add("ceb");languageIdentifers.add("chr");languageIdentifers.add("chy");languageIdentifers.add("csb");
		languageIdentifers.add("frp");
		languageIdentifers.add("fur");
		languageIdentifers.add("got");
		languageIdentifers.add("haw");
		languageIdentifers.add("ilo");
		languageIdentifers.add("jbo");
		languageIdentifers.add("ksh");
		languageIdentifers.add("lad");languageIdentifers.add("lmo");
		languageIdentifers.add("nah");languageIdentifers.add("nap");languageIdentifers.add("nds");languageIdentifers.add("nrm");
		languageIdentifers.add("pam");languageIdentifers.add("pap");languageIdentifers.add("pdc");languageIdentifers.add("pih");languageIdentifers.add("pms");
		languageIdentifers.add("rmy");
		languageIdentifers.add("scn");languageIdentifers.add("sco");
		languageIdentifers.add("tet");languageIdentifers.add("tpi");languageIdentifers.add("tum");
		languageIdentifers.add("udm");
		languageIdentifers.add("vec");languageIdentifers.add("vls");
		languageIdentifers.add("war");
		languageIdentifers.add("xal");

		languageIdentifers.add("simple");
	}

	private void initGermanVariables(){
		templateParserClass = FlushTemplates.class;
		//deleteTemplates.add( "Prettytable" );
		//parseTemplates.add( "Dieser Artikel" );
		//parseTemplates.add( "Audio" );
		//parseTemplates.add( "Video" );
		imageIdentifers.add("Bild");
		imageIdentifers.add("Image");
		imageIdentifers.add("Datei");
		categoryIdentifers.add( "Kategorie" );
		languageIdentifers.remove("de");
	}

	private void initEnglishVariables(){
		templateParserClass = FlushTemplates.class;

		imageIdentifers.add("Image");
		imageIdentifers.add("File");
		imageIdentifers.add("media");
		categoryIdentifers.add( "Category" );
		languageIdentifers.remove("en");
	}

	private String resolveLineSeparator(){
		if( lineSeparator.equals("CRLF")) {
			return "\r\n";
		}
		if( lineSeparator.equals("LF")) {
			return "\n";
		}

		logger.error(
				"LineSeparator is UNKNOWN: \""+lineSeparator+"\"\n" +
				"Set LineSeparator to \"LF\" or \"CRLF\" for a Error free configuration" );

		return lineSeparator;
	}

	/**
	 * Creates a MediaWikiParser with the configurations which has been set.
	 */
	public MediaWikiParser createParser(){
		logger.debug( "Selected Parser: " + parserClass );

		if( parserClass == ModularParser.class ){
			ModularParser mwgp = new ModularParser(
//					resolveLineSeparator(),
					"\n",
					languageIdentifers,
					categoryIdentifers,
					imageIdentifers,
					showImageText,
					deleteTags,
					showMathTagContent,
					calculateSrcSpans,
					null );

			StringBuilder sb = new StringBuilder();
			sb.append( lineSeparator + "languageIdentifers: ");
			for( String s: languageIdentifers ) {
				sb.append( s + " ");
			}
			sb.append( lineSeparator + "categoryIdentifers: ");
			for( String s: categoryIdentifers ) {
				sb.append( s + " ");
			}
			sb.append( lineSeparator + "imageIdentifers: ");
			for( String s: imageIdentifers ) {
				sb.append( s + " ");
			}
			logger.debug( sb.toString() );

			MediaWikiTemplateParser mwtp;

			logger.debug( "Selected TemplateParser: "+ templateParserClass);
			if( templateParserClass == GermanTemplateParser.class ){
				for( String s: deleteTemplates) {
					logger.debug( "DeleteTemplate: '" + s + "'");
				}
				for( String s: parseTemplates) {
					logger.debug( "ParseTemplate: '" + s + "'");
				}
				mwtp = new GermanTemplateParser( mwgp, deleteTemplates, parseTemplates );
			}
			else if( templateParserClass == FlushTemplates.class ) {
				mwtp = new FlushTemplates();
			} else if( templateParserClass == ShowTemplateNamesAndParameters.class ){
				mwtp = new ShowTemplateNamesAndParameters();
			}
			else{
				logger.error("TemplateParser Class Not Found!");
				return null;
			}

			mwgp.setTemplateParser( mwtp );

			return mwgp;
		}
		else{
			logger.error("Parser Class Not Found!");
			return null;
		}
	}

	/**
	 * Adds a Template which should be deleted while the parsing process.
	 */
	public void addDeleteTemplate( String deleteTemplate ){
		deleteTemplates.add( deleteTemplate );
	}

	/**
	 * Adds a Template which should be "parsed" while the parsing process.
	 */
	public void addParseTemplate( String parseTemplate ){
		parseTemplates.add( parseTemplate );
	}

	/**
	 * Retuns the Class of the selected Parser.
	 */
	public Class getParserClass(){
		return parserClass;
	}

	/**
	 * Set the Parser which should be configurated and returned by createParser().
	 */
	public void setParserClass(Class parserClass){ this.parserClass = parserClass; }

	/**
	 * Returns the Class of the selected TemplateParser.
	 */
	public Class getTemplateParserClass(){ return templateParserClass; }

	/**
	 * Set the Parser which should be used for Template parsing.
	 */
	public void setTemplateParserClass(Class templateParserClass){ this.templateParserClass = templateParserClass; }

	/**
	 * Retuns the List of templates which should be deleted in the parseing process.
	 */
	public List<String> getDeleteTemplates(){ return deleteTemplates; }

	/**
	 * Set the List of templates which should be deleted in the parseing process.
	 */
	public void setDeleteTemplates(List<String> deleteTemplates){ this.deleteTemplates = deleteTemplates; }

	/**
	 * Returns the CharSequence/String which should be used as line separator.
	 */
	public String getLineSeparator(){ return lineSeparator; }

	/**
	 * Sets the CharSequence/String which should be used as line separator.
	 */
	public void setLineSeparator(String lineSeparator){	this.lineSeparator = lineSeparator; }

	/**
	 * Returns the List of templates which should be "parsed" in the parseing process.
	 */
	public List<String> getParseTemplates(){ return parseTemplates; }

	/**
	 * Sets the List of templates which should be "parsed" in the parseing process.
	 */
	public void setParseTemplates(List<String> parseTemplates){ this.parseTemplates = parseTemplates; }

	/**
	 * Returns the List of Strings which are used to specifiy that a link is a link to a
	 * wikipedia i another language.
	 */
	public List<String> getLanguageIdentifers(){ return languageIdentifers; }

	/**
	 * Sets the list of language identifiers.
	 */
	public void setLanguageIdentifers( List<String> languageIdentifers){ this.languageIdentifers = languageIdentifers; }

	/**
	 * Returns the List of Strings which are used to specifiy that a link is a link to a
	 * cathegory. E.g. in german "Kathegorie" is used. But it could be usefull to use more
	 * than one identifier, mainly the english identifier "cathegory" should be used too.
	 */
	public List<String> getCategoryIdentifers( ){ return categoryIdentifers; }

	/**
	 * Set the list of cathegory identifers.
	 */
	public void setCategoryIdentifers( List<String> categoryIdentifers){ this.categoryIdentifers = categoryIdentifers; }

	/**
	 * Returns the List of Strings which are used to specifiy that a link is an Image.
	 */
	public List<String> getImageIdentifers( ){ return imageIdentifers; }

	/**
	 * Sets the image identifer list.
	 */
	public void setImageIdentifers( List<String> imageIdentifers){ this.imageIdentifers = imageIdentifers; }

	/**
	 * Returns if the Parser should show the Text of an Image, or delete it. If the Text is deleted,
	 * it will be added as a Parameter to the Link.
	 * @return true, if the Text should be shown.
	 */
	public boolean getShowImageText(){ return showImageText; }

	/**
	 * Sets if the Parser should show the Text of an Image, or delete it.
	 */
	public void setShowImageText( boolean showImageText ){ this.showImageText = showImageText; }

	/**
	 * Returns if &lf; * > tags should be deleted or annotaded.
	 * @return true if the tags should be deleted.
	 */
	public boolean getDeleteTags() { return deleteTags; }

	/**
	 * Sets if &lf; * > tags should be deleted or annotaded.
	 */
	public void setDeleteTags(boolean deleteTags) {	this.deleteTags = deleteTags; }

	/**
	 * Retruns if the Content of math tags (&lf;math>&lf;CONTENT/math>) should be deleted or annotated.
	 * @return true, if the tag content should be annotated.
	 */
	public boolean getShowMathTagContent() { return showMathTagContent; }

	/**
	 * Set if the Contetn of math tags should be deleted or annotated.
	 */
	public void setShowMathTagContent(boolean showMathTagContent) { this.showMathTagContent = showMathTagContent; }

	/**
	 * Returns if the Parser should calculate the positions in the original source of the elements
	 * which are parsed.
	 * @return true, if the positions should be calulated.
	 */
	public boolean getCalculateSrcSpans() {	return calculateSrcSpans; }

	/**
	 * Sets if the Parser should calculate the positions in the original source of the elements
	 * which are parsed.
	 */
	public void setCalculateSrcSpans(boolean calculateSrcSpans) { this.calculateSrcSpans = calculateSrcSpans; }
}
