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
package org.dkpro.jwpl.util.templates.generator.simple;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dkpro.jwpl.util.templates.generator.GeneratorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class generates data to write into sql dump file
 */
public class WikipediaTemplateInfoDumpWriter {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Map<String, Integer> tplNameToTplId;
  private final String outputPath;
  private final String charset;

  private final boolean tableExists;

  public WikipediaTemplateInfoDumpWriter(String outputPath, String charset, Map<String, Integer> tplNameToTplId,
                                         boolean tableExists) {
    this.tplNameToTplId = tplNameToTplId;
    this.outputPath = outputPath;
    this.charset = charset;
    this.tableExists = tableExists;
  }

  /**
   * Generate sql statement for data defined in {@code dataSourceToUse} and for table
   * in {@code tableToWrite}
   *
   * @param dataSourceToUse source to use for string generation
   * @param tableToWrite    table to use to store data
   * @return generated sql string
   */
  private String generateSQLStatementForDataInTable(Map<String, Set<Integer>> dataSourceToUse, String tableToWrite) {
    StringBuffer output = new StringBuffer();
    for (Entry<String, Set<Integer>> e : dataSourceToUse.entrySet()) {
      String curTemplateName = e.getKey();
      Set<Integer> curPageIds = e.getValue();

      // FIXME Problem - we do reuse existing ids here, but we treat template names from the pages and from revisions separately - resulting in
      if (!curTemplateName.isEmpty() && !curPageIds.isEmpty()) {
        //if template name does not have an id in the tplname-id map
        String id = "LAST_INSERT_ID()";
        if (!tplNameToTplId.containsKey(curTemplateName)) {
          output.append("INSERT INTO " + GeneratorConstants.TABLE_TPLID_TPLNAME
                  + " (templateName) VALUES ('" + curTemplateName + "');");
          output.append("\r\n");
        } else {
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
        output.append("REPLACE INTO " + tableToWrite + " VALUES " + curValues + ";");
        output.append("\r\n");

      }
    }

    return output.toString();
  }

  /**
   * Generate sql statement for table template id -> page id
   *
   * @param tableExists     if table does not exists create index for this table
   * @param dataSourceToUse data source to use for sql statement generation
   * @return sql statement string
   */
  private String generatePageSQLStatement(boolean tableExists,
                                          Map<String, Set<Integer>> dataSourceToUse) {
    StringBuffer output = new StringBuffer();

    // Statement creates table for Template Id -> Page Id
    output.append("CREATE TABLE IF NOT EXISTS " + GeneratorConstants.TABLE_TPLID_PAGEID
            + " (templateId INTEGER UNSIGNED NOT NULL,"
            + "pageId INTEGER UNSIGNED NOT NULL, UNIQUE(templateId, pageId));\r\n");

    // Statement for data into templateId -> pageId
    output.append(this.generateSQLStatementForDataInTable(dataSourceToUse, GeneratorConstants.TABLE_TPLID_PAGEID));

    if (!tableExists) {
      // Create index statement if table does not exists
      output.append("CREATE INDEX pageIdx ON " + GeneratorConstants.TABLE_TPLID_PAGEID + "(pageId);");
      output.append("\r\n");
    }

    return output.toString();

  }

  /**
   * Generate sql statement for table template id -> template name
   *
   * @param tableExists if this table does not exists create index
   * @return sql statement
   */
  private String generateTemplateIdSQLStatement(boolean tableExists) {
    StringBuffer output = new StringBuffer();

    // Statement creates table for Template Id -> Template Name
    output.append("CREATE TABLE IF NOT EXISTS "
            + GeneratorConstants.TABLE_TPLID_TPLNAME + " ("
            + "templateId INTEGER NOT NULL AUTO_INCREMENT,"
            + "templateName MEDIUMTEXT NOT NULL, "
            + "PRIMARY KEY(templateId)); \r\n");

    if (!tableExists) {
      output.append("CREATE INDEX tplIdx ON " + GeneratorConstants.TABLE_TPLID_TPLNAME + "(templateId);");
      output.append("\r\n");
    }
    return output.toString();

  }

  /**
   * Generate sql statement for table template id -> revision id
   *
   * @param tableExists     if table does not exists create index for this table
   * @param dataSourceToUse data source to use for sql statement generation
   * @return sql statement string
   */
  private String generateRevisionSQLStatement(boolean tableExists,
                                              Map<String, Set<Integer>> dataSourceToUse) {
    StringBuffer output = new StringBuffer();

    // Statement creates table for Template Id -> Revision Id
    output.append("CREATE TABLE IF NOT EXISTS " + GeneratorConstants.TABLE_TPLID_REVISIONID
            + " (templateId INTEGER UNSIGNED NOT NULL,"
            + "revisionId INTEGER UNSIGNED NOT NULL, UNIQUE(templateId, revisionId));\r\n");

    // Statement for data into templateId -> revisionId
    output.append(this.generateSQLStatementForDataInTable(dataSourceToUse,
            GeneratorConstants.TABLE_TPLID_REVISIONID));

    if (!tableExists) {
      // Create index statement if table does not exist
      output.append("CREATE INDEX revisionIdx ON " + GeneratorConstants.TABLE_TPLID_REVISIONID + "(revisionID);");
      output.append("\r\n");
    }

    return output.toString();
  }

  /**
   * Generate and write sql statements to output file
   *
   * @param revTableExists  if revision table does not exists -&gt; create index
   * @param pageTableExists if page table does not exists -&gt; create index
   * @param mode            generation mode
   */
  void writeSQL(boolean revTableExists, boolean pageTableExists, GeneratorMode mode) {
    try (Writer writer = new BufferedWriter(new OutputStreamWriter(
            new BufferedOutputStream(new FileOutputStream(outputPath)), charset))) {
      StringBuilder dataToDump = new StringBuilder();
      dataToDump.append(generateTemplateIdSQLStatement(this.tableExists));

      if (mode.active_for_pages) {
        dataToDump.append(generatePageSQLStatement(pageTableExists, mode.templateNameToPageId));
      }
      if (mode.active_for_revisions) {
        dataToDump.append(generateRevisionSQLStatement(revTableExists, mode.templateNameToRevId));
      }
      writer.write(dataToDump.toString());
    } catch (IOException e) {
      logger.error("Error writing SQL file: {}", e.getMessage(), e);
    }
  }

}
