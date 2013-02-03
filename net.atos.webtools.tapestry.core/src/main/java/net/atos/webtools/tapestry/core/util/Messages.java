package net.atos.webtools.tapestry.core.util;

/**
 * String constants that appear in the UI => they could be externalized/internationalized easily...
 * 
 * We don't consider here error messages
 * 
 * @see Constants
 * @see ErrorMessages
 * @author a160420
 *
 */
public class Messages {
	/*
	 * Models
	 */
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
}
