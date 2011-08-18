package de.tudarmstadt.ukp.wikipedia.revisionmachine.api;

import java.util.List;

/**
 * Provides basic user/conttributor information in a single object
 *
 * @author Oliver Ferschke
 *
 */
public class Contributor
{
	private String name;
	private Integer id;
	private List<String> groups;

	public Contributor(String name){
		this.name=name;
	}

	public Contributor(String name, Integer id){
		this.name=name;
		this.id=id;
	}

	public Contributor(String name, Integer id, List<String> groups){
		this.name=name;
		this.id=id;
		this.groups=groups;
	}

	public String getName()
	{
		return name;
	}
	public void setName(String aName)
	{
		name = aName;
	}
	public Integer getId()
	{
		return id;
	}
	public void setId(Integer aId)
	{
		id = aId;
	}
	public List<String> getGroups()
	{
		return groups;
	}
	public void setGroups(List<String> groups)
	{
		this.groups = groups;
	}

}
