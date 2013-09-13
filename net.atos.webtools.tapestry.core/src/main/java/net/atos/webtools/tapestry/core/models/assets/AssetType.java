package net.atos.webtools.tapestry.core.models.assets;

import java.util.Arrays;

import net.atos.webtools.tapestry.core.util.Constants;

public enum AssetType {
	
	IMAGE(Constants.JPEG_FILE_EXTENSION, Constants.JPG_FILE_EXTENSION, Constants.PNG_FILE_EXTENSION,
			Constants.GIF_FILE_EXTENSION),
	STYLESHEET(Constants.CSS_FILE_EXTENSION),
	SCRIPT(Constants.JS_FILE_EXTENSION);
	
	private String[] extensions;
	
	public String[] getExtensions() {
		return this.extensions;
	}
	
	private AssetType(String... extensions) {
		this.extensions = extensions;
	}
	
	public static AssetType getTypeFromExtension(String extension) {
		if (extension != null) {
			if (Arrays.asList(IMAGE.getExtensions()).indexOf(extension) > -1) {
				return IMAGE;
			}
			if (Arrays.asList(STYLESHEET.getExtensions()).indexOf(extension) > -1) {
				return STYLESHEET;
			}
			if (Arrays.asList(SCRIPT.getExtensions()).indexOf(extension) > -1) {
				return SCRIPT;
			}
		}
		return null;
	};
}
