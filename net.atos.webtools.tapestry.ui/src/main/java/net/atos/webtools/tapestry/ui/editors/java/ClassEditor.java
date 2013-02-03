package net.atos.webtools.tapestry.ui.editors.java;

import net.atos.webtools.tapestry.ui.util.UIConstants;

import org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;


@SuppressWarnings("restriction")
public class ClassEditor extends ClassFileEditor {
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		setKeyBindingScopes(new String[]{UIConstants.WST_HTMLSOURCE, 
				UIConstants.TAPESTRY_EDITOR_CONTEXT});
	}
}
