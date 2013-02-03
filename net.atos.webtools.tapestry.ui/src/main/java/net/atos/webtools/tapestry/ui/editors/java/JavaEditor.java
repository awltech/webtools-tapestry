package net.atos.webtools.tapestry.ui.editors.java;

import net.atos.webtools.tapestry.core.models.EditedFeatureModel;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.breadcrumb.IBreadcrumb;
import org.eclipse.jface.action.IStatusLineManager;

/**
 * Exactly the same Java editor than in JDT, but BreadCrumbs have been removed for performance reasons
 * 
 * @author A160420
 *
 */
@SuppressWarnings("restriction")
public class JavaEditor extends CompilationUnitEditor {
	
	public JavaEditor() {
		super();
		setKeyBindingScopes(new String[]{"org.eclipse.jdt.ui.javaEditorScope", 
				"net.atos.webtools.tapestry.editor-context"});
	}
	/**
	 * 
	 * Constructor that takes a EditedFeatureModel
	 * 
	 * @param tapestryFeatureModel : not used yet
	 */
	public JavaEditor(EditedFeatureModel tapestryFeatureModel) {
		super();
		setKeyBindingScopes(new String[]{"org.eclipse.jdt.ui.javaEditorScope", 
				"net.atos.webtools.tapestry.editor-context"});
	}
	
	/**
	 * remove breadcrumb for performance reasons (when switching tabs)
	 */
	@Override
	protected IBreadcrumb createBreadcrumb() {
		// No Breadcrumb for performance reason (until we found a better way): 
		//switching between the java->tml page is too long because resources (especially Actions?) are released every time 
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class required) {
		return super.getAdapter(required);
	}
	
	@Override
	public IStatusLineManager getStatusLineManager() {
		return super.getStatusLineManager();
	}
}
