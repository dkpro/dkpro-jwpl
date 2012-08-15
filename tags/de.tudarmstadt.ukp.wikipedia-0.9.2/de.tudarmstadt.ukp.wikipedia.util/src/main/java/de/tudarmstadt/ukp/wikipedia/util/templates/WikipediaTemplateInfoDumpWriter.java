package de.tudarmstadt.ukp.wikipedia.util.templates;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * This class generates data to write into sql dump file
 *
 * @author Artem Vovk
 *
 */
public class WikipediaTemplateInfoDumpWriter
{
	private final Log logger = LogFactory.getLog(getClass());

	private Map<String, Integer> tplNameToTplId;
	private String outputPath;
	private String charset;

	private boolean tableExists;

	public WikipediaTemplateInfoDumpWriter(String outputPath, String charset,
			Map<String, Integer> tplNameToTplId, boolean tableExists)
	{
		this.tplNameToTplId = tplNameToTplId;
		this.outputPath = outputPath;
		this.charset = charset;
		this.tableExists = tableExists;
	}

	/**
	 * Generate sql statement for data defined in dataSourceToUse and for table
	 * in tableToWrite
	 *
	 * @param dataSourceToUse
	 *            source to use for string generation
	 * @param tableToWrite
	 *            table to use to store data
	 * @return generated sql string
	 */
	private String generateSQLStatementForDataInTable(
			Map<String, Set<Integer>> dataSourceToUse, String tableToWrite)
	{
		StringBuffer output = new StringBuffer();
		for (Entry<String, Set<Integer>> e : dataSourceToUse.entrySet()) {

			String curTemplateName = e.getKey();

			Set<Integer> curPageIds = e.getValue();

			//FIXME Problem - we do reuse existing ids here, but we treat template names from the pages and from revisions separately - resulting in 
			if (!curTemplateName.isEmpty() && !curPageIds.isEmpty()) {
				//if template name does not have an id in the tplname-id map
				String id = "LAST_INSERT_ID()";
				if (!tplNameToTplId.containsKey(curTemplateName)) {
					output.append("INSERT INTO "
							+ WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME
							+ " (templateName) VALUES ('" + curTemplateName
							+ "');");
					output.append("\r\n");
				}
				else {
					//if template name has an id in the tplname-id map
					id = tplNameToTplId.get(curTemplateName).toString();
				}

				StringBuilder curValues = new StringBuilder();
				for (Integer pId : curPageIds) {
					if (curValues.length() > 0) {
						curValues.append(",");
					}
					curValues.append("(" + id + ", ");
					curValues.append(pId);
					curValues.append(")");
				}
				output.append("REPLACE INTO " + tableToWrite + " VALUES "
						+ curValues + ";");
				output.append("\r\n");

			}
		}

		return output.toString();
	}

	/**
	 * Generate sql statement for table template id -> page id
	 *
	 * @param tableExists
	 *            if table does not exists create index for this table
	 * @param dataSourceToUse
	 *            data source to use for sql statement generation
	 * @return sql statement string
	 */
	private String generatePageSQLStatement(boolean tableExists,
			Map<String, Set<Integer>> dataSourceToUse)
	{
		StringBuffer output = new StringBuffer();

		// Statement creates table for Template Id -> Page Id
		output.append("CREATE TABLE IF NOT EXISTS "
				+ WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID
				+ " ("
				+ "templateId INTEGER UNSIGNED NOT NULL,"
				+ "pageId INTEGER UNSIGNED NOT NULL, UNIQUE(templateId, pageId));\r\n");

		// Statement for data into templateId -> pageId
		output.append(this.generateSQLStatementForDataInTable(dataSourceToUse,
				WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID));

		if (!tableExists) {
			// Create index statement if table does not exists
			output.append("CREATE INDEX pageIdx ON "
					+ WikipediaTemplateInfoGenerator.TABLE_TPLID_PAGEID
					+ "(pageId);");
			output.append("\r\n");
		}

		return output.toString();

	}

	/**
	 * Generate sql statement for table template id -> template name
	 *
	 * @param tableExists
	 *            if this table does not exists create index
	 * @return sql statement
	 */
	private String generateTemplateIdSQLStatement(boolean tableExists)
	{
		StringBuffer output = new StringBuffer();

		// Statement creates table for Template Id -> Template Name
		output.append("CREATE TABLE IF NOT EXISTS "
				+ WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME + " ("
				+ "templateId INTEGER NOT NULL AUTO_INCREMENT,"
				+ "templateName MEDIUMTEXT NOT NULL, "
				+ "PRIMARY KEY(templateId)); \r\n");

		if (!tableExists) {
			output.append("CREATE INDEX tplIdx ON "
					+ WikipediaTemplateInfoGenerator.TABLE_TPLID_TPLNAME
					+ "(templateId);");
			output.append("\r\n");
		}
		return output.toString();

	}

	/**
	 * Generate sql statement for table template id -> revision id
	 *
	 * @param tableExists
	 *            if table does not exists create index for this table
	 * @param dataSourceToUse
	 *            data source to use for sql statement generation
	 * @return sql statement string
	 */
	private String generateRevisionSQLStatement(boolean tableExists,
			Map<String, Set<Integer>> dataSourceToUse)
	{
		StringBuffer output = new StringBuffer();

		// Statement creates table for Template Id -> Revision Id
		output.append("CREATE TABLE IF NOT EXISTS "
				+ WikipediaTemplateInfoGenerator.TABLE_TPLID_REVISIONID
				+ " ("
				+ "templateId INTEGER UNSIGNED NOT NULL,"
				+ "revisionId INTEGER UNSIGNED NOT NULL, UNIQUE(templateId, revisionId));\r\n");

		// Statement for data into templateId -> revisionId
		output.append(this.generateSQLStatementForDataInTable(dataSourceToUse,
				WikipediaTemplateInfoGenerator.TABLE_TPLID_REVISIONID));

		if (!tableExists) {
			// Create index statement if table does not exists
			output.append("CREATE INDEX revisionIdx ON "
					+ WikipediaTemplateInfoGenerator.TABLE_TPLID_REVISIONID
					+ "(revisionID);");
			output.append("\r\n");
		}

		return output.toString();
	}

	/**
	 * Generate and write sql statements to output file
	 *
	 * @param revTableExists
	 *            if revision table does not exists ->create index
	 * @param pageTableExists
	 *            if page table does not exists ->create index
	 * @param mode
	 *            generation mode
	 * @throws Exception
	 */
	public void writeSQL(boolean revTableExists, boolean pageTableExists,
			GeneratorMode mode)
		throws Exception
	{
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputPath), charset));
			StringBuffer dataToDump = new StringBuffer();

			dataToDump.append(generateTemplateIdSQLStatement(this.tableExists));

			if (mode.active_for_pages) {
				dataToDump.append(generatePageSQLStatement(pageTableExists,
						mode.templateNameToPageId));
			}

			if (mode.active_for_revisions) {
				dataToDump.append(generateRevisionSQLStatement(revTableExists,
						mode.templateNameToRevId));
			}

			writer.write(dataToDump.toString());
		}
		catch (IOException e) {
			logger.error("Error writing SQL file: " + e.getMessage());
		}
		finally {
			try {
				if (writer != null) {
					writer.close();
				}
			}
			catch (IOException e) {
				logger.error("Error closing stream: " + e.getMessage());
			}
		}
	}

}
