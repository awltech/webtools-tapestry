package net.atos.webtools.tapestry.core.models;

import net.atos.webtools.tapestry.core.util.Constants;

/**
 * Lists file types that can be managed in the Tapestry project model
 * 
 * @author a160420
 *
 */
public enum FileType{
	TML(Constants.TML_FILE_EXTENSION, 0),
	JAVA(Constants.JAVA_FILE_EXTENSION, 1),
	CLASS(Constants.CLASS_FILE_EXTENSION, 1),
	PROPERTY(Constants.PROPERTIES_FILE_EXTENSION, 2);
	
	
	private String extension;
	private int rank;

	private FileType(String extension, int rank) {
		this.extension = extension;
		this.rank = rank;
	}

	/**
	 * rank is mainly used for ordering tabs in the editor 
	 * (so it's there because it's convenient, but it's not really describing the FileType)
	 * 
	 * @return the rank
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * 
	 * @return the file extension
	 */
	public String getExtension() {
		return extension;
	}
	
	/**
	 * get the right enum value for this file extension, or null
	 * 
	 * @param extension
	 * @return
	 */
	public static FileType getTypeFromExtension(String extension){
		if(extension != null){
			if(extension.equalsIgnoreCase(TML.getExtension())){
				return TML;
			}
			if(extension.equalsIgnoreCase(JAVA.getExtension())){
				return JAVA;
			}
			if(extension.equalsIgnoreCase(CLASS.getExtension())){
				return CLASS;
			}
			if(extension.equalsIgnoreCase(PROPERTY.getExtension())){
				return PROPERTY;
			}
		}
		return null;
	}
}