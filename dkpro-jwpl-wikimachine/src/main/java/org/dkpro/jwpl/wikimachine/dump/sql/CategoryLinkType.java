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
package org.dkpro.jwpl.wikimachine.dump.sql;

import java.util.Locale;

/**
 * The kind of membership a {@code categorylinks} row describes, as recorded in the
 * {@code cl_type} column MediaWiki introduced with the {@code categorylinks} table revision
 * of MediaWiki 1.17.
 * <p>
 * Dumps that predate that column - and dumps without a parseable {@code CREATE TABLE} header -
 * yield {@link #UNKNOWN}, in which case consumers have to fall back to inferring the membership
 * kind from the namespace of the member page.
 */
public enum CategoryLinkType
{

    /** The member of the category is an ordinary page. */
    PAGE,
    /** The member of the category is another category. */
    SUBCAT,
    /** The member of the category is a file. */
    FILE,
    /** The dump does not carry a {@code cl_type} column. */
    UNKNOWN;

    /**
     * @param value The raw value of the {@code cl_type} column, may be {@code null}.
     * @return The matching {@link CategoryLinkType}, or {@link #UNKNOWN} if {@code value} is
     *         {@code null} or not one of the values MediaWiki defines.
     */
    public static CategoryLinkType fromDumpValue(String value)
    {
        if (value == null) {
            return UNKNOWN;
        }
        switch (value.toLowerCase(Locale.ROOT)) {
        case "page":
            return PAGE;
        case "subcat":
            return SUBCAT;
        case "file":
            return FILE;
        default:
            return UNKNOWN;
        }
    }
}
