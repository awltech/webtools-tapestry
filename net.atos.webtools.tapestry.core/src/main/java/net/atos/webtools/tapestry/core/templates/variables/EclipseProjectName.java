package net.atos.webtools.tapestry.core.templates.variables;

import net.atos.webtools.tapestry.core.models.ProjectModel;
import net.atos.webtools.tapestry.core.templates.TapestryTemplateContext;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class EclipseProjectName extends TemplateVariableResolver {

	@Override
	protected String resolve(TemplateContext context) {
		String value = null;
		if(context instanceof TapestryTemplateContext){
			ProjectModel projectModel = ((TapestryTemplateContext)context).getProjectModel();
			if(projectModel != null && projectModel.getJavaProject() != null 
					&& projectModel.getJavaProject().getProject() != null){
				value = projectModel.getJavaProject().getProject().getName();
			}
		}
		
		return (value!=null) ? value : "";
	}

	@Override
	protected boolean isUnambiguous(TemplateContext context) {
		return true;
	}
}
