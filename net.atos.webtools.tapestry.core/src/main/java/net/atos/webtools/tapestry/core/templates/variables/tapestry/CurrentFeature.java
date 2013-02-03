package net.atos.webtools.tapestry.core.templates.variables.tapestry;

import net.atos.webtools.tapestry.core.models.EditedFeatureModel;
import net.atos.webtools.tapestry.core.templates.TapestryTemplateContext;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class CurrentFeature extends TemplateVariableResolver {

	@Override
	protected String resolve(TemplateContext context) {
		String value = null;
		if(context instanceof TapestryTemplateContext){
			EditedFeatureModel editedModel = ((TapestryTemplateContext)context).getFeatureModel();
			if(editedModel != null){
				value = editedModel.getName();
			}
		}
		
		return (value!=null) ? value : "";
	}

	@Override
	protected boolean isUnambiguous(TemplateContext context) {
		return true;
	}
}
