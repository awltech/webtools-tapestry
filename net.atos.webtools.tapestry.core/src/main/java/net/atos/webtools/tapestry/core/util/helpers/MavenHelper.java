package net.atos.webtools.tapestry.core.util.helpers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;


public class MavenHelper {
	/**
	 * 
	 * @param context : the template context defining an uml element in the project
	 * @return : the ArtifactKey if the project is a maven project
	 */
	public static ArtifactKey getArtifactKey(IProject project){
		if(project != null){
			IMavenProjectRegistry mavenProjectRegistry = MavenPlugin.getMavenProjectRegistry();
			final IMavenProjectFacade projectFacade = mavenProjectRegistry.create(project, new NullProgressMonitor());
			if (projectFacade != null) {
				return projectFacade.getArtifactKey();
			}
		}
		return null;
	}
}
