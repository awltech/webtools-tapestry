package net.atos.webtools.tapestry.core.util;

import org.eclipse.jdt.core.Signature;

/**
 * <p>Constants class
 * 
 * <p>These Strings DON'T appear in the UI
 * 
 * @see Messages
 * @see ErrorMessages
 * @author a160420
 *
 */
public class Constants {
	
	//----------------------------------------------------
	//			ids from plugin.xml
	//----------------------------------------------------
	public static final String REGISTRY_ID = "net.atos.webtools.tapestry.core.tapestry-context-type-registry";
	
	public static final String PAGE_CONTEXT_TYPE = "net.atos.webtools.tapestry.core.page-context-type";
	public static final String COMPONENT_CONTEXT_TYPE = "net.atos.webtools.tapestry.core.component-context-type";
	public static final String CONTENTASSIST_CONTEXT_TYPE = "net.atos.webtools.tapestry.core.contentassist-context-type";
	
	public static final String COMPONENTS_LIBS_EXT = "components.libs"; //$NON-NLS-1$
	
	//----------------------------------------------------
	//					MODEL
	//----------------------------------------------------
	
	//default namespaces
	public static final String DEFAULT_NAMESPACE = "t";
	public static final String DEFAULT_PARAM_NAMESPACE = "p";
	
	//bindings:
	public static final String MESSAGE_BINDING = "message:";
	public static final String PROP_BINDING = "prop:";
	public static final String ASSET_BINDING = "asset:";
	public static final String ASSET_CONTEXT_BINDING = "asset:context:";
        public static final String ASSET_CONTEXT_ONLY_BINDING = "context:";
	public static final String ASSET_CLASSPATH_BINDING = "asset:classpath:";
	
	//XML attributes
	public static final String TYPE = "type";
	public static final String ID = "id";
	public static final String MIXINS = "mixins";
	public static final String PAGE_ATTRIBUTE = "page";
	
	//Classes:
	public static final String JAVA_OBJECT = "java.lang.Object";
	public static final String SLF4J_LOGGER = "org.slf4j.Logger";
	
	public static final String TAPESTRY5_COMPONENT = "org.apache.tapestry5.annotations.Component";
	public static final String TAPESTRY5_PUBLISH_PARAMETERS = "publishParameters";
	public static final String TAPESTRY5_PARAMETER = "org.apache.tapestry5.annotations.Parameter";
	public static final String TAPESTRY5_PROPERTY = "org.apache.tapestry5.annotations.Property";
	public static final String TAPESTRY5_MIXINS = "org.apache.tapestry5.annotations.Mixins";
	public static final String TAPESTRY5_SERVICES_LIBRARY_MAPPING = "org.apache.tapestry5.services.LibraryMapping";
	public static final String TAPESTRY5_IOC_CONFIGURATION_WRAPPER = "org.apache.tapestry5.ioc.internal.ValidatingConfigurationWrapper";
	public static final String TAPESTRY5_IOC_CONFIGURATION = "org.apache.tapestry5.ioc.Configuration";
	
	public static String ANNOTATION_PROPERTY = "Property";
	public static final String BLOCK = "Block";
	
	/**
	 * org.apache.tapestry5.Block class with Java signature format
	 * 
	 * @see Signature
	 */
	public static final String TAPESTRY5_BLOCK_SIGNATURE = "Lorg.apache.tapestry5.Block;";
	
	//file extensions:
	public static final String CLASS_FILE_EXTENSION = "class";
	public static final String JAVA_FILE_EXTENSION = "java";
	public static final String TML_FILE_EXTENSION = "tml";
	public static final String PROPERTIES_FILE_EXTENSION = "properties";

	//specific files
	public static final String META_INF = "META-INF";
	public static final String MANIFEST_MF = "MANIFEST.MF";

	//other:
	/**
	 * <p>Special Tapestry property present in Manifest files of Tapestry lib
	 * 
	 * <p>the value it fqe of the AppModule class
	 */
	public static final String TAPESTRY_MANIFEST_PROPERTY = "tapestry-module-classes";
	/**
	 * Application package for Tapestry 5 core library:
	 */
	public static final String TAPESTRY5_CORELIB_PACKAGE = "org.apache.tapestry5.corelib";
	
	/**
	 * Default "prefix" for Tapestry features that comes from the core library:
	 */
	public static final String TAPESTRY_CORE = "core";
	
}
