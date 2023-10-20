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
package org.dkpro.jwpl.parser.html;

public class ParsedPageCSS {

    private static final String LF = "\n";

    public static String getFileText() {
        StringBuilder sb = new StringBuilder();

        sb.append("body");
        sb.append("{");
        sb.append(" font-size: 10pt;");
        sb.append(" font-family: Arial;");
        sb.append("}");
        sb.append(LF);
        sb.append("table");
        sb.append("{");
        sb.append(" border-collapse: collapse;");
        sb.append(" border-spacing: 10px;");
        sb.append(" margin: 10px;");
        sb.append(" vertical-align: top;");
        sb.append("}");
        sb.append(LF);
        sb.append("th{");
        sb.append(" text-align: left;");
        sb.append(" border-width: 1px;");
        sb.append(" border-color: #000000;");
        sb.append(" border-style: solid;");
        sb.append(LF);
        sb.append(" font-size: 10pt;");
        sb.append(" font-family: Arial;");
        sb.append(" font-weight: normal;");
        sb.append(" ");
        sb.append(" padding: 10px;");
        sb.append("}");
        sb.append(LF);
        sb.append("td{");
        sb.append(" border-width: 1px;");
        sb.append(" border-color: #000000;");
        sb.append(" border-style: solid;");
        sb.append(" ");
        sb.append(" font-size: 10pt;");
        sb.append(" font-family: monospace;");
        sb.append(" vertical-align: top;");
        sb.append(" ");
        sb.append(" padding: 10px;");
        sb.append("}");
        sb.append(LF);
        sb.append("table.ParsedPage{}");
        sb.append("th.ParsedPage{ background-color: #FF8900; }");
        sb.append("td.ParsedPage{ background-color: #FFD29E; }");
        sb.append(LF);
        sb.append("table.Section{ width: 100%; }");
        sb.append("th.Section{ margin: 0px; padding: 0px; background-color: #FFFF00; }");
        sb.append(" table.SectionTh{ margin: 0px;}");
        sb.append(" th.SectionTh{ border-width: 0px; border-style:none; background-color: #FFFF00; vertical-align: middle; }");
        sb.append("td.Section{ background-color: #EEEEEE; }");
        sb.append(LF);
        sb.append("table.Template{ margin: 2px; }");
        sb.append("th.Template{ font-size: 7pt; padding: 1px; background-color: #99CCCC; }");
        sb.append("td.Template{ padding: 5px; }");
        sb.append("");
        sb.append("table.Table{ margin: 2px; background-color: #EEEEEE; }");
        sb.append("th.Table{ font-size: 7pt; padding: 1px; background-color: #FF0000; }");
        sb.append("td.Table{ padding: 5px; background-color: #FFCCCC;}");
        sb.append(LF);
        sb.append(LF);
        sb.append("b.Link{ color: #0000FF; }");
        sb.append("div.Link{");
        sb.append(" padding-left: 5px;");
        sb.append(" padding-right: 5px;");
        sb.append(" margin: 1px;");
        sb.append(" border-width: 1px;");
        sb.append(" border-color: #999999;");
        sb.append(" border-style: solid; ");
        sb.append(" background-color: #EEEEEE;");
        sb.append("}");
        sb.append(LF);
        sb.append("table.ContentElement{ margin: 2px; }");
        sb.append("th.ContentElement{ font-size: 7pt; padding: 1px; background-color: #6699CC; }");
        sb.append("td.ContentElement{ padding: 5px; background-color: #FFFFFF;}");
        sb.append(LF);
        sb.append("table.Paragraph{ margin: 2px; }");
        sb.append("th.Paragraph{ font-size: 7pt; padding: 1px; background-color: #66CC00; }");
        sb.append("td.Paragraph{ padding: 5px; background-color: #FFFFFF; }");
        sb.append(LF);
        sb.append("table.NestedList{ margin: 2px; }");
        sb.append("th.NestedList{ font-size: 7pt; padding: 1px; background-color: #66CC00; }");
        sb.append("td.NestedList{ padding: 5px; background-color: #CCFFCC; }");
        sb.append(LF);
        sb.append("table.DefinitionList{ margin: 2px; }");
        sb.append("th.DefinitionList{ font-size: 7pt; padding: 1px; background-color: #66CC00; }");
        sb.append("td.DefinitionList{ padding: 5px; background-color: #CCFFCC; }");
        sb.append(LF);

        return sb.toString();
    }

}
