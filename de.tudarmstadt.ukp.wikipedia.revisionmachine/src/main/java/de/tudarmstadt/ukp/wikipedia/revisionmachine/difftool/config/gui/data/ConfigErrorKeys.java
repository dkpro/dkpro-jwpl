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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data;

/**
 * Contains the keys for the configuration verification error types.
 *
 *
 *
 */
public enum ConfigErrorKeys
{

	/** Mode was enabled, but no value was set */
	COMMAND_NOT_SET,

	/** Configuration value out of range */
	VALUE_OUT_OF_RANGE,

	/** Path was not set */
	PATH_NOT_SET,

	/** Illegal configuration value */
	ILLEGAL_INPUT,

	/** Illegal input file type */
	ILLEGAL_INPUT_FILE,

	/** Required value is missing */
	MISSING_VALUE
}
