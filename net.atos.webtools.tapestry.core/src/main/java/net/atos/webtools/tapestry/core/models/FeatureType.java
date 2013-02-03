package net.atos.webtools.tapestry.core.models;

/**
 * Defines what we call the different "features" types in Tapestry:
 * In webtools, a feature, is a generic name for Tapestry page, Tapestry component,
 * Tapestry mixins...
 * 
 * @author a160420
 *
 */
public enum FeatureType {
	//IMPORTANT NOTE: the FeatureFinder Job scan the classpath for features, and uses
	//this enum: features are searched in this particular order!
	//For the moment, the only thing that we need is to have mixins BEFORE components
	//(because components can "publish parameters" of their declared mixins)
	
	PAGE("pages"),
	MIXIN("mixins"),
	COMPONENT("components"),
	SERVICE("services");
	
	private String subPackage;
	
	private FeatureType(String subPackage){
		this.subPackage = subPackage;
	}
	
	/**
	 * get the sub-package of the app package in which these features elements are found
	 * 
	 * Elements of that type can be in it, and also in any sub-sub-package.  
	 *  
	 * @return the sub package
	 */
	public String getSubPackage() {
		return subPackage;
	}
}
