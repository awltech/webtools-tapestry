package net.atos.webtools.tapestry.ui.editors.tml;

import net.atos.webtools.tapestry.ui.util.UIConstants;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.wst.sse.ui.StructuredTextEditor;


public class TmlEditor extends StructuredTextEditor {
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		setKeyBindingScopes(new String[]{UIConstants.WST_HTMLSOURCE, 
				UIConstants.TAPESTRY_EDITOR_CONTEXT});
	}
	
	@Override
	public IStatusLineManager getStatusLineManager() {
		return super.getStatusLineManager();
	}
}
