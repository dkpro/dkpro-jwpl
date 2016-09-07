/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.util;

public class OS {

    /** Tries to determine the tpye of OS the application is running on.
     * At the moment only Windows and Linux are supported.
     * @return The type of OS the application is running on. Or "unknown" if the system is unknown.
     */
    public static String getOsType() {
        String osType = "unknown";
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")) {
            osType = "Windows";
        }
        else if (osName.contains("Linux")) {
            osType = "Linux";
        }
        return osType;
    }

    /** Gets the memory used by the JVM in MB.
     * @return Returns how much memory (in MB) is used by the JVM at the moment.
     */
    public static double getUsedMemory() {
    	Runtime rt = Runtime.getRuntime();
    	
    	long memLong = rt.totalMemory() - rt.freeMemory();
    	double memDouble = memLong / (1024.0 * 1024.0);
    	memDouble = Math.round(memDouble * 100) / 100.0;
    	return memDouble;
    }
}
