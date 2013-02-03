package net.atos.webtools.tapestry.core;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.atos.webtools.tapestry.core.models.FeatureFinder;
import net.atos.webtools.tapestry.core.models.ProjectModel;
import net.atos.webtools.tapestry.core.util.Constants;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 */
public class TapestryCore extends AbstractUIPlugin {

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "net.atos.webtools.tapestry.core"; //$NON-NLS-1$
	
	/**
	 *  The shared instance
	 */
	private static TapestryCore plugin;
	
	/**
	 * The key to store customized code templates.
	 */
	public static final String CODE_TEMPLATES_KEY = PLUGIN_ID + ".custom_tml_templates";
	
	/**
	 * The code template context type registry for the java editor.
	 */
	private ContextTypeRegistry tmlTemplateContextTypeRegistry;
	
	/**
	 * The coded template store for the java editor.
	 */
	private TemplateStore tmlTemplateStore;
	
	/**
	 * <p>Very short time cache implemented with HashMap, that map project name to <code>SoftReference&lt;ProjectModel&gt;&gt;</code>
	 * 
	 * <p>In the end, 
	 * <li>when no more editor holds reference to the ProjectModel, it can be GC, and <code>get(project).get()</code>
	 * will return null
	 * 
	 */
	private Map<String, SoftReference<ProjectModel>> projectModels = new HashMap<String, SoftReference<ProjectModel>>();

	/**
	 * The constructor
	 */
	public TapestryCore() {
//		getWorkbench().getDisplay().
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static TapestryCore getDefault() {
		return plugin;
	}

	
	/**
	 * log info
	 * 
	 * @param message
	 */
	public static void logInfo(String message) {
		getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
	}
	
	/**
	 * log warning
	 * 
	 * @param message
	 */
	public static void logWarning(String message) {
		getDefault().getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message));
	}
	
	/**
	 * log warning with Exception
	 * 
	 * @param message
	 */
	public static void logWarning(String message, Throwable t) {
		getDefault().getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, t));
	}
	
	/**
	 * log error
	 * 
	 * @param message
	 */
	public static void logError(String message) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
	}
	
	/**
	 * log error with Exception
	 * 
	 * @param message
	 * @param t
	 */
	public static void logError(String message, Throwable t) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, t));
	}

	/**
	 * Returns the template store for the tml templates.
	 *
	 * @return the template store for the tml templates
	 */
	public TemplateStore getTmlTemplateStore() {
		if (tmlTemplateStore == null) {
			IPreferenceStore store = getPreferenceStore();
			tmlTemplateStore = new ContributionTemplateStore(getCodeTemplateContextRegistry(), store, CODE_TEMPLATES_KEY);
			try {
				tmlTemplateStore.load();
			} catch (IOException e) {
				logError("codeTemplateStore could not be loaded in plugin", e);
			}
	
			tmlTemplateStore.startListeningForPreferenceChanges();
		}
	
		return tmlTemplateStore;
	}
	
	
	
	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 *
	 * @return the template context type registry for the code generation
	 *         templates
	 */
	public ContextTypeRegistry getCodeTemplateContextRegistry() {
		if (tmlTemplateContextTypeRegistry == null) {
			//Creates contextType Registry and loads ContextTypes that have been added
			//through extension of the registry
			tmlTemplateContextTypeRegistry = new ContributionContextTypeRegistry(Constants.REGISTRY_ID);
		}
		return tmlTemplateContextTypeRegistry;
	}
	
	/**
	 * <p>Get the {@link ProjectModel} for a project.
	 * 
	 * <p>On most calls, it will re-process the project (launching {@link FeatureFinder} Job), 
	 * except if the same project has been requested very recently (&lt;2 sec). 
	 * This is mostly useful when all the opened tml editors are notified simultaneously by 
	 * their listeners that a reload is needed  
	 * 
	 * <p>Inner synch prevents this method to be executed by different threads.
	 * 
	 * @see TapestryMultiPageEditor#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 * 
	 * @param project
	 * @param forceReload true if you need to reload the model (i.e. rebuild of project), but it will only be executed if it hasn't been loaded &lt; 2sec.
	 * @return the ProjectModel
	 */
	public ProjectModel getProjectModel (IProject project, boolean forceReload){
		if(project == null){
			return null;
		}
		
		ProjectModel projectModel = null;
		boolean mustInit = false;
		
		synchronized (projectModels) {
			//not null if it's already been in the cache
			SoftReference<ProjectModel> softReference = projectModels.get(project.getName());
			if(softReference != null){
				//not null if an editor has kept a reference 
				projectModel = softReference.get();
			}
			
			if(projectModel != null){
				Date previousInitDate = projectModel.getInitDate();
				Date now = new Date();
				// > 2 second:
				if(forceReload && (now.getTime() - previousInitDate.getTime()) > 2000){
					projectModel = null;
				}
			}
			
			if(projectModel == null){
				projectModel = new ProjectModel(project);
				mustInit = true;
				projectModels.put(project.getName(), new SoftReference<ProjectModel>(projectModel));
			}
		}
		
		if(projectModel != null && mustInit){
			projectModel.init();
			projectModel.loadSubFeatures();
		}
		return projectModel;
	}
}
