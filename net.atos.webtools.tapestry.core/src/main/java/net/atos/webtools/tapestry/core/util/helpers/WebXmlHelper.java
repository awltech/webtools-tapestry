package net.atos.webtools.tapestry.core.util.helpers;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.atos.webtools.tapestry.core.TapestryCore;
import net.atos.webtools.tapestry.core.util.ErrorMessages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Helper class to get information from a web.xml file
 * 
 * There's a static method that returns all the files called web.xml, 
 * then the caller must choose one, and instantiate the helper (to parse the document)
 * and then you can get information on the created object. 
 * 
 * @author a160420
 *
 */
public class WebXmlHelper {
	private static final String FILTER_NAME = "filter-name";
	private static final String FILTER_CLASS = "filter-class";
	private static final String FILTER = "filter";
	private static final String PARAM_VALUE = "param-value";
	private static final String PARAM_NAME = "param-name";
	private static final String CONTEXT_PARAM = "context-param";
	
	public static final String TAPESTRY_APP_PACKAGE = "tapestry.app-package";
	public static final String APACHE_TAPESTRY_FILTER = "org.apache.tapestry5.TapestryFilter";
	public static final String WEB_XML = "web.xml";
	
	private Document document;
	
	/**
	 * Instantiate a helper from the web.xml IFile
	 * 
	 * @param IFile web.xml file reference
	 */
	public WebXmlHelper(IFile webXmlFile) {
		document = getDocument(webXmlFile);
	}
	
	/**
	 * Parses the xml file to find context-param called "tapestry.app-package":
	 * 
	 * <pre>
	 * &lt;context-param&gt;
     *	&lt;param-name&gt;tapestry.app-package&lt;/param-name&gt;
     *	&lt;param-value&gt;$THE_PACKAGE$&lt;/param-value&gt;
     * &lt;/context-param&gt;
     * </pre>
	 * 
	 * @return the name of the Tapestry app-package if it's found or null
	 */
	public String getTapestryPackage() {
		if(document != null){
			Element rootElement = document.getRootElement();
			
			@SuppressWarnings("unchecked")
			List<Element> contextParams = rootElement.getChildren(CONTEXT_PARAM, rootElement.getNamespace());
			
			for (Element contextParam : contextParams) {
				Element paramName = contextParam.getChild(PARAM_NAME, rootElement.getNamespace());
				if(paramName.getTextTrim().equals(TAPESTRY_APP_PACKAGE)) {
					return contextParam.getChild(PARAM_VALUE, rootElement.getNamespace()).getTextTrim();
				}
			}
		}
		return null;
	}
	
	/**
	 * Looks for the "application package". 
	 * It will be the prefix for xxxModule service class (and other things) 
	 * 
	 * Technically, it'll look for the filter name that have a filter class =
	 * org.apache.tapestry5.TapestryFilter
	 * 
	 * <pre>
	 * &lt;filter&gt;
     *	&lt;filter-name&gt;$APP_NAME&lt;/filter-name&gt;
     *	&lt;filter-value&gt;org.apache.tapestry5.TapestryFilter&lt;/filter-value&gt;
     * &lt;/filter&gt;
     * </pre>
	 * 
	 * @return app name
	 */
	public String getAppName() {
		if(document != null){
			Element rootElement = document.getRootElement();
			
			@SuppressWarnings("unchecked")
			List<Element> filters = rootElement.getChildren(FILTER, rootElement.getNamespace());
			
			for (Element filter : filters) {
				Element paramName = filter.getChild(FILTER_CLASS, rootElement.getNamespace());
				if(paramName.getTextTrim().equals(APACHE_TAPESTRY_FILTER)) {
					return filter.getChild(FILTER_NAME, rootElement.getNamespace()).getTextTrim();
				}
			}
		}
		return null;
	}
	
	/**
	 * Parses the file to get a jdom {@link Document}
	 * @param file the fils to parse
	 * @return the Document
	 */
	private static Document getDocument(IFile file){
		if(file != null){
			//refresh file before update
	    	try {
				file.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
			} 
	    	catch (CoreException e) {
				TapestryCore.logError(ErrorMessages.WEB_XML_FILE_COULD_NOT_BE_REFRESHED, e);
			}
	    	
			//Builds the web.xml file
			try {
				//disable XML validation
				SAXBuilder saxBuilder = new SAXBuilder(false);
				saxBuilder.setValidation(false);
				saxBuilder.setFeature("http://xml.org/sax/features/validation", false);
				saxBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
				saxBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
				return saxBuilder.build(file.getContents());
			} 
			catch (JDOMException e) {
				TapestryCore.logError(ErrorMessages.ERROR_WHILE_LOADING_WEB_XML_FILE_BECAUSE_AN_EXCEPTION_WAS_THROWN, e);
			} 
			catch (IOException e) {
				TapestryCore.logError(ErrorMessages.ERROR_WHILE_LOADING_WEB_XML_FILE_BECAUSE_AN_EXCEPTION_WAS_THROWN, e);
			}
			catch (CoreException e) {
				TapestryCore.logError(ErrorMessages.ERROR_WHILE_LOADING_WEB_XML_FILE_BECAUSE_AN_EXCEPTION_WAS_THROWN, e);
			}
		}
		return null;
	}
	
	/**
	 * finds all the web.xml files in the project (including the copies in target directories)
	 * @param project the project to search in
	 * @return the list of files called web.xml
	 */
	public static Set<IFile> findWebXmlFiles(IProject project){
		final Set<IFile> webXmlFiles = new HashSet<IFile>();
		
		if(project != null){
			try {
				project.accept(new IResourceVisitor() {
					@Override
					public boolean visit(IResource resource) throws CoreException {
						if(resource instanceof IFile && resource.getName().equals(WEB_XML)){
							webXmlFiles.add((IFile) resource);
						}
						
						return true;
					}
				});
			}
			catch (CoreException e) {
				TapestryCore.logError(ErrorMessages.PROBLEM_WHILE_SEARCHING_FOR_WEB_XML, e);
			}
		}
		
		return webXmlFiles;
	}
}
