package net.atos.webtools.tapestry.core.models.assets;

import net.atos.webtools.tapestry.core.util.Constants;

import org.eclipse.core.resources.IResource;

/**
 * Default model for a Tapestry asset.
 */
public class AssetModel {
	
	/** Asset name */
	private String name;
	
	public String getName() {
		return name;
	}
	
	/** Asset file path */
	private String path;
	
	public String getPath() {
		return path;
	}
	
	public AssetModel(IResource resource) {
		this.name = resource.getName();
		
		String fullPath = resource.getFullPath().toString();
		this.path = fullPath.substring(fullPath.indexOf(Constants.ASSET_PATH) + 16);
	}
}
