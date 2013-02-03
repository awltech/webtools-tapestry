package net.atos.webtools.tapestry.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 */
public class TapestryUI extends AbstractUIPlugin {

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "net.atos.webtools.tapestry.ui"; //$NON-NLS-1$
	
	/**
	 *  The shared instance
	 */
	private static TapestryUI plugin;
	
	/**
	 * The constructor
	 */
	public TapestryUI() {
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
	public static TapestryUI getDefault() {
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
	 * Returns image in this plugin
	 * 
	 * @param imageFilePath
	 *            : image File Path in this plugin
	 * @return Image if exists
	 */
	public Image getImage(String imageFilePath) {
		Image image = this.getImageRegistry().get(TapestryUI.PLUGIN_ID + ":" + imageFilePath);
		if (image == null){
			image = loadImage(TapestryUI.PLUGIN_ID, imageFilePath);
		}
		return image;
	}

	/**
	 * Returns image in plugin
	 * 
	 * @param pluginId
	 *            : Id of the plugin containing thie image
	 * @param imageFilePath
	 *            : image File Path in plugin
	 * @return Image if exists
	 */
	public Image getImage(String pluginId, String imageFilePath) {
		Image image = this.getImageRegistry().get(pluginId + ":" + imageFilePath);
		if (image == null) {
			image = loadImage(pluginId, imageFilePath);
		}
		return image;
	}

	/**
	 * Loads image in Image Registry is not available in it
	 * 
	 * @param pluginId
	 *            : Id of the plugin containing thie image
	 * @param imageFilePath
	 *            : image File Path in plugin
	 * @return Image if loaded
	 */
	private synchronized Image loadImage(String pluginId, String imageFilePath) {
		String id = pluginId + ":" + imageFilePath;
		Image image = this.getImageRegistry().get(id);
		if (image != null) {
			return image;
		}
		ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, imageFilePath);
		if (imageDescriptor != null) {
			image = imageDescriptor.createImage();
			this.getImageRegistry().put(pluginId + ":" + imageFilePath, image);
		}
		return image;
	}
}
