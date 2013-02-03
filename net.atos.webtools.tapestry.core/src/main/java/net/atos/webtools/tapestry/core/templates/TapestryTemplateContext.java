/**
 * 
 */
package net.atos.webtools.tapestry.core.templates;

import net.atos.webtools.tapestry.core.TapestryCore;
import net.atos.webtools.tapestry.core.models.EditedFeatureModel;
import net.atos.webtools.tapestry.core.models.ProjectModel;
import net.atos.webtools.tapestry.core.util.ErrorMessages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.eclipse.jface.text.templates.persistence.TemplateStore;


/**
 * Manages templates for wizards or auto-completion
 * 
 * @author a154438
 *
 */
public class TapestryTemplateContext extends TemplateContext {

	private static final String LINE_DELIMITER = "\n";

	private ProjectModel projectModel = null;
	private EditedFeatureModel featureModel = null;

	/**
	 * <p>WARNING: must only be used in resolvers available in
	 * <code>net.atos.webtools.tapestry.contentassist-context-type</code> context type.
	 * 
	 * <p>In other context types (both wizards), it'll always be null.
	 * 
	 * @return the featureModel or null
	 */
	public EditedFeatureModel getFeatureModel() {
		return featureModel;
	}
	
	
	public ProjectModel getProjectModel() {
		return projectModel;
	}

	/**
	 * Called from Wizard
	 * Be Careful: sub Features are NOT loaded (list of components, mixins...)
	 * 
	 * @see ProjectModel#loadSubFeatures()
	 * 
	 * @param projectModel
	 * @param contextType
	 */
	public TapestryTemplateContext(ProjectModel projectModel, TemplateContextType contextType) {
		super(contextType);
		this.projectModel = projectModel;
	}
	
	/**
	 * Called from Template Auto-completion
	 * 
	 * Everything should be loaded normally
	 * 
	 * @see ProjectModel#loadSubFeatures()
	 * 
	 * @param featureModel
	 * @param contextType
	 */
	public TapestryTemplateContext(EditedFeatureModel featureModel, TemplateContextType contextType) {
		super(contextType);
		this.featureModel = featureModel;
		if(featureModel != null){
			projectModel = featureModel.getProjectModel();
		}
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.TemplateContext#evaluate(org.eclipse.jface.text.templates.Template)
	 */
	@Override
	public TemplateBuffer evaluate(Template template)
			throws BadLocationException, TemplateException {
		if (!canEvaluate(template)){
			return null;
		}

		TemplateTranslator translator= new TemplateTranslator();
		TemplateBuffer templateBuffer = translator.translate(template);
		getContextType().resolve(templateBuffer, this);

		return templateBuffer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.TemplateContext#canEvaluate(org.eclipse.jface.text.templates.Template)
	 */
	@Override
	public boolean canEvaluate(Template template) {
		return true;
	}
	
	/**
	 * Method to get the given Template as a <code>String</code> with all variables resolved
	 * 
	 * The template is found from the ProjectTemplateStore (the project is found by the model where
	 * the given element is defined).
	 * If it fails, defaults templateStore is used, but project specific templates won't be used!
	 * 
	 * It is resolved in a new <code>TapestryTemplateContext</code> context created with the contextType of
	 * the template.
	 * 
	 * @param project
	 * @param templateId
	 * @return <code>java.lang.String</code>
	 * 
	 */
	public static String resolveTemplate(ProjectModel projectModel, String templateName){
		Template template = null;
		//Find the template from the templateStore for the templateId.
		TemplateStore templateStore = TapestryCore.getDefault().getTmlTemplateStore();
		template = templateStore.findTemplate(templateName);
		
		return resolveTemplate(projectModel, template);
	}
	/**
	 * Get the template as a String
	 * 
	 * @param template
	 * @return the template as a String
	 */
	public static String resolveTemplate(ProjectModel projectModel, Template template){
		String comment = "";
		if(template != null){
			//Find the ContextType from the Template in the plugin registry:
			TemplateContextType contextType = TapestryCore.getDefault().getCodeTemplateContextRegistry().getContextType(template.getContextTypeId());
			//Initialize a new Context of that type:
			TemplateContext templateContext = new TapestryTemplateContext(projectModel, contextType);
	
			try {
				//----- EVALUATION-----
				comment = templateContext.evaluate(template).getString();
			}
			catch (BadLocationException e) {
				TapestryCore.logError(ErrorMessages.PROBLEM_EVALUATING_TML_TEMPLATE + template.getName(), e);
			}
			catch (TemplateException e) {
				TapestryCore.logError(ErrorMessages.PROBLEM_EVALUATING_TML_TEMPLATE + template.getName(), e);
			}
		}
		else{
			TapestryCore.logWarning("Unable to find template");
		}
		return comment;
	}
	
	/**
	 * Get the String of the new class, using the standard (jdt) class template.
	 * 
	 * @param project the project in which it's created
	 * @param className the name of the class
	 * @param parentCU Compilation unit in which it'll be created
	 * @return the new class as a String
	 */
	public static String resolveJavaTemplate(IJavaProject project, String className, ICompilationUnit parentCU){
		String javaClassCode = "";
		try {
			String typeBody = CodeGeneration.getTypeBody(CodeGeneration.CLASS_BODY_TEMPLATE_ID, parentCU, className, LINE_DELIMITER);
			if(typeBody == null){
				typeBody = "";
			}
			StringBuffer typeDeclaration= new StringBuffer("public class "); //$NON-NLS-1$
			typeDeclaration.append(className);
			typeDeclaration.append("{\n").append(typeBody).append("\n");
			typeDeclaration.append("}"); //$NON-NLS-1$
			
			String fileComment = CodeGeneration.getFileComment(parentCU, LINE_DELIMITER);
			String typeComment = CodeGeneration.getTypeComment(parentCU, className, LINE_DELIMITER);
			
			
			javaClassCode = CodeGeneration.getCompilationUnitContent(parentCU, fileComment, typeComment, typeDeclaration.toString(), LINE_DELIMITER);
		} 
		catch (CoreException e) {
			TapestryCore.logError(ErrorMessages.PROBLEM_EVALUATING_THE_JAVA_TEMPLATE_FOR_CLASS + className, e);
		}
		return javaClassCode;
	}
	
}
