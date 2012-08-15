package de.tudarmstadt.ukp.wikipedia.util.templates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

/**
 * Starter, which parsed configuration properties file and starts
 * WikipediaTemplateInfoGenerator
 *
 * @author Artem Vovk
 * @author Oliver Ferschke
 *
 */
public class TemplateInfoGeneratorStarter
{

	private final static String FILTERING_ACTIVE_FOR_PAGES = "create_templates_for_pages";
	private final static String FILTERING_ACTIVE_FOR_REVISIONS = "create_templates_for_revisions";
	private final static String USE_REVISION_ITERATOR = "use_revision_iterator";

	private final static String PAGES_WHITE_LIST = "pages_white_list";
	private final static String PAGES_BLACK_LIST = "pages_black_list";
	private final static String PAGES_WHITE_PREFIX_LIST = "pages_white_prefix_list";
	private final static String PAGES_BLACK_PREFIX_LIST = "pages_black_prefix_list";

	private final static String REVISIONS_WHITE_LIST = "revisions_white_list";
	private final static String REVISIONS_BLACK_LIST = "revisions_black_list";
	private final static String REVISIONS_WHITE_PREFIX_LIST = "revisions_white_prefix_list";
	private final static String REVISIONS_BLACK_PREFIX_LIST = "revisions_black_prefix_list";

	protected final static String TABLE_TPLID_REVISIONID = "templateId_revisionId";
	protected final static String TABLE_TPLID_PAGEID = "templateId_pageId";


	/**
	 * Starts index generation using the database credentials in the properties
	 * file specified in args[0].<br/>
	 * The properties file should have the following structure:
	 * <ul>
	 * <li>host=dbhost</li>
	 * <li>db=revisiondb</li>
	 * <li>user=username</li>
	 * <li>password=pwd</li>
	 * <li>language=english</li>
	 * <li>output=outputFile</li>
	 * <li>charset=UTF8 (or others) (optional)</li>
	 * <li>pagebuffer=5000 (optional)</li>
	 * <li>maxAllowedPackets=16760832 (optional)</li>
	 * </ul>
	 * <br/>
	 *
	 * @param args
	 *            allows only one entry that contains the path to the config
	 *            file
	 */
	public static void main(String[] args)
	{

		if (args == null || args.length != 1) {
			System.out
					.println(("You need to specify the database configuration file. \n"
							+ "It should contain the access credentials to you revision database in the following format: \n"
							+ "  host=dbhost \n"
							+ "  db=revisiondb \n"
							+ "  user=username \n"
							+ "  password=pwd \n"
							+ "  language=english \n"
							+ "  output=outputFile \n"
							+ "  charset=UTF8 (optional)\n"
							+ "  pagebuffer=5000 (optional)\n"
							+ "  maxAllowedPackets=16760832 (optional)"));
			throw new IllegalArgumentException();
		}
		else {
			Properties props = load(args[0]);

			DatabaseConfiguration config = new DatabaseConfiguration();

			config.setHost(props.getProperty("host"));
			config.setDatabase(props.getProperty("db"));
			config.setUser(props.getProperty("user"));
			config.setPassword(props.getProperty("password"));
			config.setLanguage(Language.valueOf(props.getProperty("language")));

			String charset = props.getProperty("charset");

			String pagebufferString = props.getProperty("pagebuffer");
			int pageBuffer;

			String maxAllowedPacketsString = props.getProperty("maxAllowedPackets");
			long maxAllowedPackets;

			try {
				if (charset == null) {
					charset = "UTF-8";
				}

				if (pagebufferString != null) {
					pageBuffer = Integer.parseInt(pagebufferString);
				}
				else {
					pageBuffer = 5000;
				}

				if (maxAllowedPacketsString != null) {
					maxAllowedPackets = Long.parseLong(maxAllowedPacketsString);
				}
				else {
					maxAllowedPackets = (16 * 1024 * 1023);
				}

				String output = props.getProperty("output");
				File outfile = new File(output);
				if (outfile.isDirectory()) {
					try {
						output = outfile.getCanonicalPath()
								+ File.separatorChar + "templateInfo.sql";
					}
					catch (IOException e) {
						output = outfile.getPath() + File.separatorChar
								+ "templateInfo.sql";
					}
				}

				String active_for_pages = props
						.getProperty(FILTERING_ACTIVE_FOR_PAGES);
				String active_for_revisions = props
						.getProperty(FILTERING_ACTIVE_FOR_REVISIONS);
				String useRevisionIterator = props
						.getProperty(USE_REVISION_ITERATOR);

				GeneratorMode mode = new GeneratorMode();

				if (active_for_pages.equals("true")) {
					mode.active_for_pages = true;
				}

				if (active_for_revisions.equals("true")) {
					mode.active_for_revisions = true;
				}

				if(useRevisionIterator.equals("true")||useRevisionIterator==null||useRevisionIterator.equals("")){
					mode.useRevisionIterator=true;
				}


				TemplateFilter pageFilter = new TemplateFilter(
						createSetFromProperty(props
								.getProperty(PAGES_WHITE_LIST)),
						createSetFromProperty(props
								.getProperty(PAGES_WHITE_PREFIX_LIST)),
						createSetFromProperty(props
								.getProperty(PAGES_BLACK_LIST)),
						createSetFromProperty(props
								.getProperty(PAGES_BLACK_PREFIX_LIST)));

				TemplateFilter revisionFilter = new TemplateFilter(
						createSetFromProperty(props
								.getProperty(REVISIONS_WHITE_LIST)),
						createSetFromProperty(props
								.getProperty(REVISIONS_WHITE_PREFIX_LIST)),
						createSetFromProperty(props
								.getProperty(REVISIONS_BLACK_LIST)),
						createSetFromProperty(props
								.getProperty(REVISIONS_BLACK_PREFIX_LIST)));

				WikipediaTemplateInfoGenerator generator = new WikipediaTemplateInfoGenerator(
						config, pageBuffer, charset, output,
						maxAllowedPackets, pageFilter, revisionFilter, mode);

				// Start processing now
				generator.process();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Loads a properties file from disk
	 *
	 * @param propsName
	 *            path to the configuration file
	 * @return Properties the properties object containing the configuration
	 *         data
	 * @throws IOException
	 *             if an error occurs while accessing the configuration file
	 */
	private static Properties load(String configFilePath)
	{
		Properties props = new Properties();
		FileInputStream fis = null;
		try {
			File configFile = new File(configFilePath);
			fis = new FileInputStream(configFile);
			props.load(fis);
		}
		catch (IOException e) {
			System.err.println("Could not load configuration file "
					+ configFilePath);
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				}
				catch (IOException e) {
					System.err
							.println("Error closing file stream of configuration file "
									+ configFilePath);
				}
			}
		}
		return props;
	}

	/**
	 * Parses property string into HashSet
	 *
	 * @param property
	 *            string to parse
	 * @return
	 */
	public static HashSet<String> createSetFromProperty(String property)
	{
		HashSet<String> properties = new HashSet<String>();

		if (property != null && !property.equals("null")) {
			// "([\\w]*)=([\\w]*);"
			Pattern params = Pattern.compile("([\\w]+)[;]*");
			Matcher matcher = params.matcher(property.trim());
			while (matcher.find()) {
				properties.add(matcher.group(1));
			}

		}

		return properties;
	}

}
