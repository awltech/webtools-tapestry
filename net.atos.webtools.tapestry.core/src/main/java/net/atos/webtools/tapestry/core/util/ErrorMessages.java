package net.atos.webtools.tapestry.core.util;

/**
 * Error messages: they appear to the user (and could be internationalized), but only in the errorLog view, 
 * or error message pop-up
 * 
 *  @see Messages
 * @author a160420
 *
 */
public class ErrorMessages {
	/*
	 * Util classes
	 */
	public static final String ERROR_WHILE_FETCHING_THE_CURRENT_EDITOR = "Error while fetching the current editor";
	public static final String GET_RESOURCES_NOT_IMPLEMENTED = "getResources() not implemented";
	public static final String GET_RESOURCE_NOT_IMPLEMENTED = "getResource() not implemented: ";
	public static final String UNABLE_TO_GET_ENTRY_FROM_JAR = "Unable to get entry from jar: ";
	public static final String COULD_NOT_FIND_CLASS = "Could not find class ";
	public static final String CLASSLOADER_CLOSED = "Classloader closed: ";
	public static final String FAILED_TO_CLOSE_JAR = "Failed to close jar: ";
	public static final String FAILED_TO_CLOSE_STREAM = "Failed to close stream";
	public static final String UNABLE_TO_CREATE_JAR_FILE_FOR_FILE = "Unable to create JarFile for file: ";
	public static final String CAN_T_READ_JAVADOC_FOR = "Can't read javadoc for: ";
	public static final String WEB_XML_FILE_COULD_NOT_BE_REFRESHED = "web.xml file could not be refreshed.";
	public static final String PROBLEM_WHILE_SEARCHING_FOR_WEB_XML = "Problem while searching for web.xml";
	public static final String ERROR_WHILE_LOADING_WEB_XML_FILE_BECAUSE_AN_EXCEPTION_WAS_THROWN = "Error while loading web.xml file because an exception was thrown";
	
	/*
	 * Templates 
	 */
	public static final String PROBLEM_EVALUATING_THE_JAVA_TEMPLATE_FOR_CLASS = "Problem evaluating the Java Template for class ";
	public static final String PROBLEM_EVALUATING_TML_TEMPLATE = "Problem evaluating tml template ";
	
	/*
	 * model
	 */
	public static final String PROBLEM_WHILE_SEARCHING_FOR_MANIFEST_MF = "Problem while searching for MANIFEST.MF";
	public static final String PROBLEM_WHILE_SEARCHING_FOR_APP_PROPERTIES = "Problem while searching for app.properties";
	public static final String CAN_T_READ_JAVA_FILE_FOR_AST_PARSING = "Can't read Java File for AST parsing";
	public static final String CAN_T_OPEN_PROPERTY_FILE = "Can't open property file: ";
	public static final String CAN_T_LOAD_PROPERTIES_FROM = "Can't load properties from: ";
	public static final String CAN_T_GET_CONTENT_OF_JAVA_FILE_FOR_AST_PARSING = "Can't get content of Java file for AST parsing";
	public static final String CAN_T_FIND_CORRESPONDING_FILE_IN_PROJECT_SOURCE_S_DIRECTORIES = "Can't find corresponding file in project source's directories";
	public static final String CAN_T_LOAD_THE_MANIFEST_FOR_PACKAGE = "Can't load the Manifest for package ";
	public static final String CAN_T_FIND_COMPILATION_UNITS_IN_PACKAGE = "Can't find compilation units in Package: ";
	public static final String CAN_T_FIND_CLASSES_IN_PACKAGE = "Can't find classes in Package: ";
	public static final String CAN_T_LOAD_TAPESTRY_LIBS_FROM = "Can't load Tapestry libs from ";
	public static final String CAN_T_SEARCH_DYNAMICALLY_FOR_TAPESTRY_LIBS = "Can't search dynamically for Tapestry libs";
	public static final String CAN_T_LOAD_THE_PACKAGES = "Can't load the packages";
	public static final String PROJECT_CAN_T_BE_PARSED_FOR_PACKAGES = "Project can't be parsed for packages";
	public static final String CAN_T_LOAD_PARAMETERS_FOR_TYPE = "Can't load parameters for type";
	public static final String CAN_T_LOOK_FOR_FEATURE_S_MESSAGES = "Can't look for feature's messages";
	public static final String NOT_ABLE_TO_PERSIST_USER_PREFERENCES = "Not able to persist user preferences";
	// Asset
	public static final String CAN_T_LOAD_TAPESTRY_ASSETS_FROM = "Can't load Tapestry assets from ";
}
