/**
 * 
 */
package net.atos.webtools.tapestry.ui.editors.multi;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler class for the command switchEditor
 *
 */
public class SwitchEditorHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		TapestryMultiPageEditor textEditor = null;
		if (part instanceof TapestryMultiPageEditor){
			textEditor = (TapestryMultiPageEditor) part;
			if(textEditor.getActivePage()!=-1){
				textEditor.pageChangeUsingShortcut(textEditor.getActivePage() == 0? 1: 0);
			}
		}
		return null;
	}

}
