package net.atos.webtools.tapestry.core.models.assets;

import net.atos.webtools.tapestry.core.TapestryCore;
import net.atos.webtools.tapestry.core.models.ProjectModel;
import net.atos.webtools.tapestry.core.util.Constants;
import net.atos.webtools.tapestry.core.util.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.core.JavaProject;

/**
 * Job task used to get all project assets (images, styles and scripts).
 */
public class AssetFinder extends Job {
	
	private ProjectModel projectModel;
	
	/**
	 * Initialize the {@link Job} with the {@link ProjectModel}
	 * 
	 * @param projectModel the projectModel that must have its {@link JavaProject} already set
	 */
	public AssetFinder(ProjectModel projectModel) {
		super("Assets Finder for " + projectModel.getJavaProject().getProject().getName());
		this.projectModel = projectModel;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		IFolder webappFolder = projectModel.getJavaProject().getProject().getFolder(Constants.ASSET_PATH);
		if (webappFolder != null) {
			try {
				webappFolder.accept(new IResourceVisitor() {
					@Override
					public boolean visit(IResource resource) throws CoreException {
						if(resource instanceof IFile) {
							AssetType type = AssetType.getTypeFromExtension(resource.getFileExtension());
							if (type != null) {
								switch (type) {
									case IMAGE:
										projectModel.addImage(new AssetModel(resource));
										break;
									case SCRIPT:
										projectModel.addScript(new AssetModel(resource));
										break;
									case STYLESHEET:
										projectModel.addStylesheet(new AssetModel(resource));
										break;
									default:
										break;
								}
							}
						}
						return true;
					}
				});
			} catch (CoreException e) {
				
			}
		}
		return new Status(IStatus.OK, TapestryCore.PLUGIN_ID, Messages.JOB_DONE);
	}

}
