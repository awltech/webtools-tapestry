package net.atos.webtools.tapestry.ui.util;

/**
 * String constants that appear in the UI => they could be externalized/internationalized easily...
 * 
 * We don't consider here error messages
 * 
 * @see UIConstants
 * @see UIErrorMessages
 * @author a160420
 *
 */
public class UIMessages {
	/*
	 * WIZARDS
	 */
	public static String NEW_COMPONENT_TITLE = "Add Tapestry 5 Component";
	public static String NEW_PAGE_TITLE = "Add Tapestry 5 Page";
	
	public static String NEW_COMPONENT_DESC = "This will help to create Tapestry 5 component (including both template file and class file)";
	public static String NEW_PAGE_DESC = "This will help to create Tapestry 5 page (including both template file and class file)";
	
	public static String PACKAGE_SELECTION_DIALOG_TITLE = "Destination package";
	public static String PACKAGE_SELECTION_DIALOG_DESC = "Select target package from the list";
	public static String PACKAGE_SELECTION_DIALOG_MSG_NONE = "There is not any packages, create first";
	
	//Buttons:
	public static final String BROWSE_BUTTON = "Browse";
	
	//Labels:
	public static final String PACKAGE_LABEL = "Package:";
	public static final String PAGE_NAME_LABEL = "Page Name:";
	public static final String COMPONENT_NAME_LABEL = "Component Name:";
	public static final String PROJECT_LABEL = "Project:";
	public static final String TEMPLATE_LABEL = "Template:";
	public static final String JAVA_SOURCE_FOLDER_LABEL = "Java source folder:";
	public static final String TML_SOURCE_FOLDER_LABEL = "TML source folder:";
	public static final String CREATE_TML_LABEL = "Create Template:";
	public static final String CREATE_PROPERTIES_LABEL = "Create properties file:";
	
	//Tooltips:
	public static final String CREATE_TML_TOOLTIP = "Select this checkbox button to create template file for Tapestry 5 component";
	public static final String CREATE_PROPERTIES_TOOLTIP = "Select this checkbox button to create properties file";
	
	//Validation messages:
	public static final String PACKAGE_VALIDATION_MESSAGE = "Root package must be: ";
	public static final String WIZARD_WINDOW_TITLE = "Tapestry 5 wizard";
	
	/*
	 * EDITORS
	 */
	public static final String JAVA_TAB_LABEL = "Java";
	public static final String TEMPLATE_TAB_LABEL = "Template";
	
	public static final String FILE_NOT_FOUND = "No associated file (many components and mixins don't have any tml file)";
	
	public static final String T_TYPE_JAVADOC = "Tapestry type";
	public static final String T_ID_JAVADOC = "An optional identifier that is used to reference the block from inside the Java class";
	public static final String T_MIXINS_JAVADOC = "A Component Mixin is a way to supplement an existing Tapestry component with additional behavior";
	public static final String BLOCK_JAVADOC = 
	"A block is a collection of static text and elements, and components, " +
	"derived from a component template. In the template, a block is demarcated using the &lt;t:block&gt; or &lt;t:parameter&gt; elements." +
	" The interface defines no methods, but the provided implementations of Block are capable of rendering their contents on demand. <p/>" +
	" Tapestry includes coecions from String to {@link org.apache.tapestry5.Renderable} and {@link org.apache.tapestry5.Renderable} to Block." +
	" This means that components that take Block parameters may be bound to literal strings, to arbitrary numbers" +
	" (or other objects, with the expectation that they will be converted to strings), or to renderable objects such as components.";
	
	//Model loading
	public static final String JOB_DONE = "Job done";
	public static final String UNKNOWN_PUBLISHED_PARAMETER = "unknown published parameter";
	public static final String UNKNOWN = "UNKNOWN";
	public static final String NO_JAVADOC = "No javadoc";
	public static final String WEBTOOLS_PREFERENCE_MESSAGE = "XA Webtools Tapestry plugin configuration - please open subtree for more pages";
	public static final String EDIT_TEMPLATES = "<a>edit templates</a>";
}
