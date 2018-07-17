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
package de.tudarmstadt.ukp.wikipedia.wikimachine.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;


/**
 * This class provides utilities for the conversion of timestamps.
 *
 */
public abstract class TimestampUtil {

//	@SuppressWarnings("deprecation")
	public static Timestamp parse(String mediaWikiString){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		long time = 0;
		try {
			time = sdf.parse(mediaWikiString).getTime();
		} catch (ParseException e) {
		}
		return new Timestamp(time);
	}
	
	//         0123456789012345678
	//example: 1970-01-04 18:11:40.0 to 19700104181140
	public static String toMediaWikiString(Timestamp timestamp){
		String original = timestamp.toString();
		StringBuffer result = new StringBuffer();
		result.append(original.substring(0,4));//year
		result.append(original.substring(5,7));//month
		result.append(original.substring(8,10));//date
		result.append(original.substring(11,13));//hour
		result.append(original.substring(14,16));//minute
		result.append(original.substring(17,19));//second
		return result.toString();
	}
	
	public static Timestamp getNextTimestamp(Timestamp previous, long nrDays){
		return new Timestamp(previous.getTime()+(nrDays*24*60*60*1000));
	}
}
