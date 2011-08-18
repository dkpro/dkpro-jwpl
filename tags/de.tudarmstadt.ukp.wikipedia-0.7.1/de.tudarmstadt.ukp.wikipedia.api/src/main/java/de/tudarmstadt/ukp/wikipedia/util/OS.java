/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
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
