package net.atos.webtools.tapestry.core.models.features;

import net.atos.webtools.tapestry.core.models.ProjectModel;
import net.atos.webtools.tapestry.core.util.helpers.JavaModelHelper;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IType;

/**
 * Parent class for the model classes for each feature type (Components, Mixins...). 
 * 
 * @see AbstractParameteredFeatureModel
 * @author a160420
 *
 */
public abstract class AbstractFeatureModel {
	protected String prefix;
	protected String name;
	protected String javadoc;
	protected IType type;
	protected ProjectModel projectModel;
	
	protected String source;
	protected String subPackage;
	
	private Object inputElement;

	public String getPrefix() {
		return prefix;
	}
	public String getName() {
		return name;
	}
	public String getJavadoc() {
		return "<b>" + getFullName() + "<br/></b>" + javadoc + "<br/><br/><b>From file:</b> <i>" + source + "</i>";
	}
	public IType getType() {
		return type;
	}

	
	public Object getInputElement() {
		return inputElement;
	}
	public ProjectModel getProjectModel() {
		return projectModel;
	}
	
	public IResource getResource() {
		return type.getResource();
	}

	/**
	 * return the "full name" of the feature, this is the notation used in XML attributes (t:type, t:mixins) 
	 * i.e.:
	 * 
	 * <li>ActionLink
	 * <li>jquery/SomeComponent
	 * <li>my/pack/subpath/MyComponent
	 * 
	 * @return
	 */
	public String getFullName() {
		StringBuilder sb = new StringBuilder();
		if(prefix != null && prefix.length()>0){
			sb.append(prefix + "/");
		}
		if(subPackage != null && subPackage.length() > 0){
			sb.append(subPackage.replace('.', '/') + "/");
		}
		sb.append(getName());
		return sb.toString();
	}
	
	/**
	 * return the qualified name of the feature, this is used for visible notation for components
	 * i.e.:
	 * 
	 * <li>t:ActionLink
	 * <li>t:jquery.SomeComponent
	 * <li>t:my.pack.subpath.MyComponent
	 * 
	 * @param namespace
	 * @return
	 */
	public String getQualifiedName(String namespace) {
		StringBuilder sb = new StringBuilder(namespace + ":");
		if(prefix != null && prefix.length()>0){
			sb.append(prefix + ".");
		}
		if(subPackage != null && subPackage.length()>0){
			sb.append(subPackage + ".");
		}
		sb.append(getName());
		return sb.toString();
	}
	
	/**
	 * Seldom used constructor (for statically defined features only)
	 */
	AbstractFeatureModel() {
	}
	
	/**
	 * Standard constructor with parameters
	 * 
	 * @param prefix
	 * @param type
	 * @param projectModel
	 * @param source
	 * @param subPackage
	 */
	public AbstractFeatureModel(String prefix, IType type, ProjectModel projectModel, String source, String subPackage) {
		if(! "core".equals(prefix)){
			this.prefix = prefix;
		}
		this.name = type.getElementName();
		this.javadoc = JavaModelHelper.loadJavadoc(type);
		this.type = type;
		
		inputElement = type.getResource();
		if(inputElement == null){
			inputElement = type.getClassFile();
		}
		
		this.projectModel = projectModel;
		this.source = source;
		
		this.subPackage = subPackage;
		JavaModelHelper.reconcile(type);
	}

}
