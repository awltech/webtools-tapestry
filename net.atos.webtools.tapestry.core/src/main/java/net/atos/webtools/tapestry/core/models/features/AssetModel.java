package net.atos.webtools.tapestry.core.models.features;

public class AssetModel {
	/**
	 * attribute the name
	 */
	protected String name;
	
	/**
	 * attribute the path
	 */
	protected String path;
	
	/**
	 * @param name
	 * @param path
	 */
	public AssetModel(String name, String path) {
		super();
		this.name = name;
		this.path = path;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
}
