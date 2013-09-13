package net.atos.webtools.tapestry.core.util;

import java.util.HashMap;

/**
 * Utility class used to link tapestry components to html elements. Used for Tapestry
 * invisible instrumentation.
 */
public class InvisibleInstrumentation {
	
	private static HashMap<String, String> htmlComponentsList = null;
	
	private static void initHtmlComponentsList() {
		htmlComponentsList = new HashMap<String, String>();
		
		// Link components
		htmlComponentsList.put(Components.ACTION_LINK, Components.A);
		htmlComponentsList.put(Components.EVENT_LINK, Components.A);
		htmlComponentsList.put(Components.PAGE_LINK, Components.A);
		
		// Form components
		htmlComponentsList.put(Components.FORM, Components.FORM);
		htmlComponentsList.put(Components.CHECKBOX, Components.INPUT);
		
		// Layout components
		htmlComponentsList.put(Components.ZONE, Components.DIV);
	}
	
	/**
	 * Returns the html element used with a given tapestry component.
	 * 
	 * For example, with the component ActionLink, we usually write <a t:type="ActionLink"...
	 */
	public static String getHTMLComponent(String tapestryComponent) {
		if (htmlComponentsList == null) {
			initHtmlComponentsList();
		}
		return htmlComponentsList.get(tapestryComponent);
	}
}
