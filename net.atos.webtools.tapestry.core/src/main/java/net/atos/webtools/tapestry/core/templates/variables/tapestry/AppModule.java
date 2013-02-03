package net.atos.webtools.tapestry.core.templates.variables.tapestry;

import net.atos.webtools.tapestry.core.models.ProjectModel;
import net.atos.webtools.tapestry.core.templates.TapestryTemplateContext;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class AppModule extends TemplateVariableResolver {

	@Override
	protected String resolve(TemplateContext context) {
		String value = null;
		if(context instanceof TapestryTemplateContext){
			ProjectModel projectModel = ((TapestryTemplateContext)context).getProjectModel();
			if(projectModel != null){
				value = projectModel.getAppModule();
			}
		}
		
		return (value!=null) ? value : "";
	}

	@Override
	protected boolean isUnambiguous(TemplateContext context) {
		return true;
	}
}
