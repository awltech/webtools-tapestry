package net.atos.webtools.tapestry.ui.util.helpers;

import net.atos.webtools.tapestry.ui.TapestryUI;
import net.atos.webtools.tapestry.ui.util.UIErrorMessages;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.Workbench;

/**
 * Some util methods to manipulate complex Eclipse API
 * 
 * @author a160420
 *
 */
@SuppressWarnings("restriction")
public class EclipseHelper {
	private static IEditorPart editor;
	private static final Object getCurrentEditorLock = new Object();
	
	/**
	 * Gets the current editor, looking for different windows, different pages, and 
	 * then editor.
	 * 
	 * It can be called from anywhere, and the call will always occur in the Display thread
	 * 
	 * @return the currently active editor or null
	 */
	public static IEditorPart getCurrentEditor(){
		synchronized (getCurrentEditorLock) {
			editor = null;
			
			Display.getDefault().syncExec(
					new Runnable(){
						public void run(){
							try{
								if(Workbench.getInstance() != null 
										&& Workbench.getInstance().getActiveWorkbenchWindow() != null
										&& Workbench.getInstance().getActiveWorkbenchWindow().getActivePage() != null){
									editor = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
								}
							}
							catch(Exception e){
								TapestryUI.logWarning(UIErrorMessages.ERROR_WHILE_FETCHING_THE_CURRENT_EDITOR, e);
							}
						}
					});
			
			return editor;
		}
	}
	
}
