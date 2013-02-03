package net.atos.webtools.tapestry.core.templates.variables.maven;


import net.atos.webtools.tapestry.core.models.ProjectModel;
import net.atos.webtools.tapestry.core.templates.TapestryTemplateContext;
import net.atos.webtools.tapestry.core.util.helpers.MavenHelper;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.m2e.core.embedder.ArtifactKey;

/**
 * Resolves to the maven version found in the pom.xml from the current project
 * 
 * @author a160420
 *
 */
public class GroupId extends TemplateVariableResolver {
	
	@Override
	protected String resolve(TemplateContext context) {
		String value = null;
		if(context instanceof TapestryTemplateContext){
			ProjectModel projectModel = ((TapestryTemplateContext)context).getProjectModel();
			if(projectModel != null && projectModel.getJavaProject() != null 
					&& projectModel.getJavaProject().getProject() != null){
				IProject project = projectModel.getJavaProject().getProject();
				ArtifactKey artifact = MavenHelper.getArtifactKey(project);
				if(artifact != null){
					value = artifact.getGroupId();
				}
			}
		}
		
		return (value!=null) ? value : "";
	}
	
	@Override
	protected boolean isUnambiguous(TemplateContext context) {
		return true;
	}
}
