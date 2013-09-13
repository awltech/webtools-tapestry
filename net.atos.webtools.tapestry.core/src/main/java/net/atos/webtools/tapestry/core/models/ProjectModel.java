package net.atos.webtools.tapestry.core.models;

import static net.atos.webtools.tapestry.core.util.helpers.StringHelper.toFirstUpper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.jar.Manifest;

import net.atos.webtools.tapestry.core.TapestryCore;
import net.atos.webtools.tapestry.core.models.assets.AssetFinder;
import net.atos.webtools.tapestry.core.models.assets.AssetModel;
import net.atos.webtools.tapestry.core.models.features.ComponentModel;
import net.atos.webtools.tapestry.core.models.features.MixinModel;
import net.atos.webtools.tapestry.core.models.features.PageModel;
import net.atos.webtools.tapestry.core.models.features.ServiceModel;
import net.atos.webtools.tapestry.core.models.features.ValidatorModel;
import net.atos.webtools.tapestry.core.util.Constants;
import net.atos.webtools.tapestry.core.util.ErrorMessages;
import net.atos.webtools.tapestry.core.util.Messages;
import net.atos.webtools.tapestry.core.util.helpers.WebXmlHelper;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.text.templates.Template;

/**
 * Represents a Tapestry Project with reference to its {@link JavaProject}, and a list of {@link ComponentModel}
 * 
 * @author a160420
 *
 */
@SuppressWarnings("restriction")
public class ProjectModel {
	private static final String MODULE = "Module";
	private static final String SERVICES_PACK = ".services.";

	/**
	 * The Java project in Eclipse
	 */
	private IJavaProject javaProject;
	
	private String appPackage;
	
	private String appName;
	
	private String appModule;
	
	/**
	 * Message catalog files, with name finishing by <code>.properties</code>
	 * It contains messages that can be called directly in tml files
	 */
	private IStorage appMessageCatalogFile;
	
	
	/**
	 * List of the components dynamically found.
	 * 
	 * A {@link ConcurrentLinkedQueue} is used internally to ensure it is thread safe 
	 * (in case Ctrl-space is pressed while this Collection is still being filled by the {@link FeatureFinder} Job)
	 */
	private Collection<ComponentModel> components = new ConcurrentLinkedQueue<ComponentModel>();
	
	private Map<String, ComponentModel> componentsByFullName = new ConcurrentHashMap<String, ComponentModel>();
	
	/**
	 * List of the mixins dynamically found.
	 */
	private Collection<MixinModel> mixins = new ConcurrentLinkedQueue<MixinModel>();
	
	private Map<String, MixinModel> mixinsByFullName = new ConcurrentHashMap<String, MixinModel>();
	
	/**
	 * List of the pages dynamically found.
	 */
	private Collection<PageModel> pages = new ConcurrentLinkedQueue<PageModel>();
	
	private Map<String, PageModel> pagesByFullName = new ConcurrentHashMap<String, PageModel>();
	
	/**
	 * List of the services dynamically found.
	 */
	private Collection<ServiceModel> services = new ConcurrentLinkedQueue<ServiceModel>();

	private Map<String, ServiceModel> servicesByFullName = new ConcurrentHashMap<String, ServiceModel>();
	
	/**
	 * List of the validators dynamically found.
	 */
	private Collection<ValidatorModel> validators = new ConcurrentLinkedQueue<ValidatorModel>();
	
	/**
	 * List of the images dynamically found.
	 */
	private Collection<AssetModel> images = new ConcurrentLinkedQueue<AssetModel>();

	/**
	 * List of the stylesheets dynamically found.
	 */
	private Collection<AssetModel> stylesheets = new ConcurrentLinkedQueue<AssetModel>();

	/**
	 * List of the scripts dynamically found.
	 */
	private Collection<AssetModel> scripts = new ConcurrentLinkedQueue<AssetModel>();
	
	/**
	 * Technical property, used to avoid reloading the model too often
	 * 
	 * @see TapestryCore
	 */
	private Date initDate;
	
	
	public IJavaProject getJavaProject() {
		return javaProject;
	}


	public String getAppPackage() {
		return appPackage;
	}

	public String getAppName() {
		return appName;
	}

	public String getAppModule() {
		return appModule;
	}


	public Template[] getContentAssistTemplates() {
		return TapestryCore.getDefault().getTmlTemplateStore().getTemplates(Constants.CONTENTASSIST_CONTEXT_TYPE);
	}

	public IStorage getAppMessageCatalogFile() {
		return appMessageCatalogFile;
	}

	void addComponent(ComponentModel component){
		if(component != null){
			components.add(component);
			componentsByFullName.put(component.getFullName().toLowerCase().trim(), component);
		}
	}


	public Collection<ComponentModel> getComponents() {
		return components;
	}
	
	public ComponentModel getComponent(String fullName) {
		if(fullName != null){
			return componentsByFullName.get(fullName.toLowerCase().trim());
		}
		return null;
	}


	void addMixin(MixinModel mixin){
		if(mixin != null){
			mixins.add(mixin);
			mixinsByFullName.put(mixin.getFullName().toLowerCase().trim(), mixin);
		}
	}


	public Collection<MixinModel> getMixins() {
		return mixins;
	}


	public MixinModel getMixin(String fullName) {
		if(fullName != null){
			return mixinsByFullName.get(fullName.toLowerCase().trim());
		}
		return null;
	}


	void addPage(PageModel page){
		if(page != null){
			pages.add(page);
			pagesByFullName.put(page.getFullName().toLowerCase().trim(), page);
		}
	}


	public Collection<PageModel> getPages() {
		return pages;
	}
	
	public PageModel getPage(String fullName) {
		if(fullName != null){
			return pagesByFullName.get(fullName.toLowerCase().trim());
		}
		return null;
	}
	
	void addService(ServiceModel service){
		if(service != null){
			services.add(service);
			servicesByFullName.put(service.getFullName().toLowerCase().trim(), service);
		}
	}
	
	public Collection<ServiceModel> getServices() {
		return services;
	}

	public ServiceModel getService(String fullName) {
		if(fullName != null){
			return servicesByFullName.get(fullName.toLowerCase().trim());
		}
		return null;
	}
	
	public void addValidator(ValidatorModel validator) {
		if (validator != null) {
			validators.add(validator);
		}
	}
	
	public Collection<ValidatorModel> getValidators() {
		return validators;
	}
	
	public void addImage(AssetModel image) {
		if (image != null && !images.contains(image)) {
			images.add(image);
		}
	}
	
	public Collection<AssetModel> getImages() {
		return images;
	}
	
	
	public void addStylesheet(AssetModel stylesheet) {
		if (stylesheet != null && !stylesheets.contains(stylesheet)) {
			stylesheets.add(stylesheet);
		}
	}
	
	public Collection<AssetModel> getStylesheets() {
		return stylesheets;
	}
	
	public void addScript(AssetModel script) {
		if (script != null && !images.contains(script)) {
			scripts.add(script);
		}
	}
	
	public Collection<AssetModel> getScripts() {
		return scripts;
	}

	public Date getInitDate() {
		return initDate;
	}


	/**
	 * Constructs asynchronously the project model: only the {@link JavaProject} is set directly, 
	 * but then a {@link FeatureFinder} {@link Job} is launched to scan 
	 * 
	 * You must explicitly call loadSubFeatures() after constructor to finish initialization
	 * 
	 * @param project
	 */
	public ProjectModel(IJavaProject javaProject) {
		this(javaProject.getProject());
	}
	
	/**
	 * Constructs asynchronously the project model: only the {@link JavaProject} is set directly, 
	 * but then a {@link FeatureFinder} {@link Job} is launched to scan 
	 * 
	 * You must explicitly call loadSubFeatures() after constructor to finish initialization
	 * 
	 * @param project
	 */
	public ProjectModel(IProject project) {
		if(project == null){
			throw new IllegalArgumentException("Can't init ProjectModel from null project");
		}
		
		if (project.isOpen() && JavaProject.hasJavaNature(project)) {
			javaProject = JavaCore.create(project);
		}
		
		//MUST be the last operation to be more accurate:
		initDate = new Date();
	}
	
	public void init(){
		//-------- parses web.xml ---------------
		for(IFile webXmlFile : WebXmlHelper.findWebXmlFiles(this.getJavaProject().getProject())){
			WebXmlHelper webXmlHelper = new WebXmlHelper(webXmlFile);
			appPackage = webXmlHelper.getTapestryPackage();
			appName = webXmlHelper.getAppName();
			if(appPackage != null && appName != null){
				appModule = appPackage + SERVICES_PACK + toFirstUpper(appName) + MODULE;
				break;
			}
		}
		
		//-------- MANIFEST.MF -------------------- 
		if(this.getAppPackage() == null){
			try {
				for (IPackageFragmentRoot packageFragmentRoot : javaProject.getPackageFragmentRoots()) {
					Manifest manifest = FeatureFinder.getManifest(packageFragmentRoot);
					if(manifest != null){
						appModule = manifest.getMainAttributes().getValue(Constants.TAPESTRY_MANIFEST_PROPERTY);
						if(appModule != null && appModule.contains(SERVICES_PACK)){
							appPackage = appModule.substring(0, appModule.lastIndexOf(SERVICES_PACK));
							appName = appModule.substring(appModule.lastIndexOf(SERVICES_PACK) + SERVICES_PACK.length(),
															appModule.lastIndexOf(MODULE));
							appName = appName.substring(0,1).toUpperCase() + appName.substring(1);
							break;
						}
					}
				}
			}
			catch (JavaModelException e) {
				TapestryCore.logWarning(ErrorMessages.PROBLEM_WHILE_SEARCHING_FOR_MANIFEST_MF, e);
			}
		}
		
		//-------------- SEARCHES for app.properties ------------
		if (appMessageCatalogFile == null) {
			final List<IResource> appPropertiesFiles = new ArrayList<IResource>();
			final String appPropertiesFileName = this.getAppName() + '.' + Constants.PROPERTIES_FILE_EXTENSION;
			
			try {
				javaProject.getProject().accept(new IResourceVisitor() {
					@Override
					public boolean visit(IResource resource) throws CoreException {
						if(resource instanceof IFile && (resource.getName()).equals(appPropertiesFileName)){
							appPropertiesFiles.add(resource);
						}
						return true;
					}
				});
			} 
			catch (CoreException e) {
				TapestryCore.logError(ErrorMessages.PROBLEM_WHILE_SEARCHING_FOR_APP_PROPERTIES, e);
			}
			if(appPropertiesFiles.size()>0){
				IResource appMessageCatalogFound = appPropertiesFiles.get(0);
				
				if(appMessageCatalogFound instanceof IStorage && appMessageCatalogFound.exists()){
					appMessageCatalogFile = (IStorage) appMessageCatalogFound;
				}
			}
		}
	}
	
	/**
	 * Asynchronously launch a FeatureFinder that will load the Components, Pages, Mixins... 
	 * that are contained in the project, and dependencies. 
	 */
	public void loadSubFeatures(){
		//------------ defaults components -------------
		this.addComponent(new ComponentModel(Constants.TAPESTRY_CORE, Constants.BLOCK, Messages.BLOCK_JAVADOC, Constants.TAPESTRY_CORE));
		
		//---- Searches asynchronously for other components in the classpath ----
		FeatureFinder featureFinder = new FeatureFinder(this);
		featureFinder.schedule();
	}
	
	/**
	 * Asynchronously launches a job which will find all assets (css/js/images) in the project.
	 */
	public void loadAssets() {
		AssetFinder assetFinder = new AssetFinder(this);
		assetFinder.schedule();
	}
}
