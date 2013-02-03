package net.atos.webtools.tapestry.ui.editors.multi;

import net.atos.webtools.tapestry.core.models.FileType;
import net.atos.webtools.tapestry.ui.TapestryUI;
import net.atos.webtools.tapestry.ui.util.UIErrorMessages;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IStorageEditorInput;

/**
 * Allow a specific match between editors: it's used when we try to open an already opened file
 * and it focuses on this already opened editor, instead of opening it twice.
 * 
 * In this case, we have to match {@link TapestryMultiPageEditor}'s input whether it's a tml or java file.
 * But if we try to open the same file in another kind of editor, it's possible.
 * 
 * Also open the right page (java/tml) when a file is selected in the explorer.
 * 
 * @author A160420
 *
 */
@SuppressWarnings("restriction")
public class MatchingStrategy implements IEditorMatchingStrategy {

	@Override
	public boolean matches(IEditorReference editorRef, IEditorInput input) {
		
		IEditorPart iEditorPart = editorRef.getEditor(false);
		if(iEditorPart instanceof TapestryMultiPageEditor){
			TapestryMultiPageEditor multiEditor = (TapestryMultiPageEditor) iEditorPart;
			
			if(input instanceof IStorageEditorInput){
				IStorageEditorInput storageInput = (IStorageEditorInput) input;
				IStorage tmlFile = multiEditor.getTapestryFeatureModel().getTmlFile();
				IStorage javaFile = multiEditor.getTapestryFeatureModel().getJavaFile();
				
				try {
					if((tmlFile != null && tmlFile.equals(storageInput.getStorage()))){
						multiEditor.pageChangeUsingShortcut(FileType.TML.getRank());
						return true;
					}
					if((javaFile != null && javaFile.equals(storageInput.getStorage()))){
						multiEditor.pageChangeUsingShortcut(FileType.JAVA.getRank());
						return true;
					}
				}
				catch (CoreException e) {
					TapestryUI.logError(UIErrorMessages.MATCHING_STRATEGY_FAILED, e);
				}
			}
			if(input instanceof IClassFileEditorInput){
				IClassFile classFileInput = ((IClassFileEditorInput) input).getClassFile();
				IClassFile classFile = multiEditor.getTapestryFeatureModel().getClassFile();
				if(classFile != null && classFile.equals(classFileInput)){
					multiEditor.pageChangeUsingShortcut(FileType.CLASS.getRank());
					return true;
				}
			}
			
		}

		return false;
	}
}
