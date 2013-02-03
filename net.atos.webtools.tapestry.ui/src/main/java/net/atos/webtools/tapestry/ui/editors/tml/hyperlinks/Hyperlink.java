package net.atos.webtools.tapestry.ui.editors.tml.hyperlinks;


import net.atos.webtools.tapestry.ui.TapestryUI;
import net.atos.webtools.tapestry.ui.util.UIConstants;

import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Hyperlink for files within the workspace. (As long as there is an IFile,
 * this can be used) Opens the default editor for the file.
 * 
 * This is a "simplified" copy of the protected {@link WorkspaceFileHyperlink}
 */
@SuppressWarnings("restriction")
public class Hyperlink implements IHyperlink {

	private IRegion fRegion;
	private IRegion hightlight;
	private IEditorInput editorInput;
	
	/**
	 * 
	 * @param region
	 * @param file
	 */
	public Hyperlink(IRegion region, Object inputElement) {
		fRegion = region;
		this.editorInput = EditorUtility.getEditorInput(inputElement);
	}

	public Hyperlink(IRegion region, Object inputElement, IRegion hightlight) {
		this(region, inputElement);
		this.hightlight = hightlight;
	}
	
	public IRegion getHyperlinkRegion() {
		return fRegion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getTypeLabel()
	 */
	public String getTypeLabel() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkText()
	 */
	public String getHyperlinkText() {
		if(editorInput != null){
			String text = editorInput.getName();
			if (text.length() > 60) {
				text = text.substring(0, 25) + "..." + text.substring(text.length() - 25, text.length());
			}
			return "Open " + text;
		}
		return null;
	}
	
	/**
	 * Will be called when the link is clicked => we'll open the file 
	 */
	public void open() {
		if(editorInput != null){
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			
			try {
				IEditorPart editor = IDE.openEditor(page, editorInput, UIConstants.TAPESTRY_MULTI_PAGE_EDITOR, true);
				
				// highlight range in editor if possible
				if ((hightlight != null) && (editor instanceof ITextEditor)) {
					((ITextEditor) editor).setHighlightRange(hightlight.getOffset(), hightlight.getLength(), true);
				}
			}
			catch (PartInitException pie) {
				TapestryUI.logWarning(pie.getMessage(), pie);
			}
		}
	}
}

