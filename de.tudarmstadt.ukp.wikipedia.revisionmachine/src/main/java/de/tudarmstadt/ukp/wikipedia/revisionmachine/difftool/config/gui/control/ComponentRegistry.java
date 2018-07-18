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
package de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.control;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.ConfigGUI;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.data.PanelKeys;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.difftool.config.gui.panels.AbstractPanel;

/**
 * ComponentsRegistry of the ConfigurationTool
 *
 *
 *
 */
public class ComponentRegistry
{

	/** Reference to the GUI */
	private ConfigGUI gui;

	/** Map that contains references to the important panels */
	private final Map<PanelKeys, AbstractPanel> map;

	/**
	 * (Constructor) Creates a ComponentRegistry.
	 */
	public ComponentRegistry()
	{
		this.map = new HashMap<PanelKeys, AbstractPanel>();
	}

	/**
	 * Registers the panel with the given key.
	 *
	 * @param key
	 *            key
	 * @param panel
	 *            panel
	 */
	public void register(final PanelKeys key, final AbstractPanel panel)
	{
		this.map.put(key, panel);
	}

	/**
	 * Sets the reference of the GUI.
	 *
	 * @param gui
	 *            GUI
	 */
	public void registerGUI(final ConfigGUI gui)
	{
		this.gui = gui;
	}

	/**
	 * Adds the xml description of the panels content to the StringBuilder.
	 * Errors which occur during the xml transformation will be added to the
	 * ConfigVerification.
	 *
	 * @param builder
	 *            Reference to a StringBuilder object
	 * @param errors
	 *            Reference to the ConfigVerification object
	 */
	public void toXML(final StringBuilder builder,
			final ConfigVerification errors)
	{

		map.get(PanelKeys.PANEL_VALUES).toXML(builder, errors);
		map.get(PanelKeys.PANEL_EXTERNALS).toXML(builder, errors);
		map.get(PanelKeys.PANEL_INPUT).toXML(builder, errors);
		map.get(PanelKeys.PANEL_OUTPUT).toXML(builder, errors);
		map.get(PanelKeys.PANEL_SQL).toXML(builder, errors);
		map.get(PanelKeys.PANEL_CACHE).toXML(builder, errors);
		map.get(PanelKeys.PANEL_LOGGING).toXML(builder, errors);
		map.get(PanelKeys.PANEL_DEBUG).toXML(builder, errors);
		map.get(PanelKeys.PANEL_FILTER).toXML(builder, errors);
	}

	/**
	 * Reads the configuration parameters described in the panel from the
	 * ConfigSettings and and sets the contained values.
	 *
	 * @param config
	 *            Reference to the ConfigSettings object
	 */
	public void applyConfig(final ConfigSettings config)
	{

		map.get(PanelKeys.PANEL_VALUES).applyConfig(config);
		map.get(PanelKeys.PANEL_EXTERNALS).applyConfig(config);
		map.get(PanelKeys.PANEL_INPUT).applyConfig(config);
		map.get(PanelKeys.PANEL_OUTPUT).applyConfig(config);
		map.get(PanelKeys.PANEL_SQL).applyConfig(config);
		map.get(PanelKeys.PANEL_CACHE).applyConfig(config);
		map.get(PanelKeys.PANEL_LOGGING).applyConfig(config);
		map.get(PanelKeys.PANEL_DEBUG).applyConfig(config);
		map.get(PanelKeys.PANEL_FILTER).applyConfig(config);
	}

	/**
	 * Returns the reference of the GUI.
	 *
	 * @return reference to the GUI
	 */
	public ConfigGUI getGUI()
	{
		return this.gui;
	}

	/**
	 * Repaints the GUI.
	 */
	public void repaint()
	{
		if (gui != null) {
			gui.repaint();
		}
	}
}
