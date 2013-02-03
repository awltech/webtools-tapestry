package net.atos.webtools.tapestry.ui.editors.multi;

import java.util.ArrayList;
import java.util.List;

import net.atos.webtools.tapestry.ui.TapestryUI;
import net.atos.webtools.tapestry.ui.util.UIErrorMessages;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * Represents a {@link ContentOutlinePage} for a multipage editor.
 * 
 * In Eclipse the {@link ContentOutline} is a single view (that can't be modified) that holds a page for each edited file
 * in a PageBook. It relies on a {@link Composite} Control, with a layering layout. Every time an editor gets focus, the right layer
 * is brought to top.
 * 
 * For an editor that holds more than one file (i.e. in different tabs), it was not possible to activate one of this page from the editor.
 * So, this custom Page was created, and it holds in itself another PageBook with a page for 
 * 
 * @author a160420
 *
 */
public class MultiPageOutlinePage extends ContentOutlinePage {
	/**
	 * The MultiPageEditorPart that is linked to this outline.
	 * This reference is only kept to find the opened editor on {@link MultiPageOutlinePage#createControl(Composite)}
	 */
	private MultiPageEditorPart multipageEditor;
	
	/**
	 * The pages of each sub-editor
	 */
	private IContentOutlinePage[] editorOutlinePages;
	
	/**
	 * The multi-layer control that holds all the {@link Control}s for the other IContentOutlinePage, of each sub-editor 
	 */
	private PageBook pageBookControl;
	
	
	/**
	 * Initialize the Page with the different editors.
	 * 
	 * <p>
	 * <b>WARNING:</b> The editors must be set in the same order than the page index in the {@link MultiPageEditorPart}
	 * 
	 * @param multipageEditor: the originating multi-page editor
	 * @param editorParts
	 */
	public MultiPageOutlinePage(MultiPageEditorPart multipageEditor, IEditorPart... editorParts) {
		super();
		this.multipageEditor = multipageEditor;
		editorOutlinePages = new IContentOutlinePage[editorParts.length];
		
		for(int i = 0; i< editorParts.length; i++){
			editorOutlinePages[i] = (IContentOutlinePage) editorParts[i].getAdapter(IContentOutlinePage.class);
		}
		
	}
	
	/**
	 * Initializes a new {@link PageBook} with all the Controls being the other's editor OutlinePage's controls.
	 * 
	 * Also 
	 */
	@Override
	public void createControl(Composite parent) {
		pageBookControl = new PageBook(parent, SWT.NONE);
		
		List<Control> controlList = new ArrayList<Control>();
		
		for (IContentOutlinePage contentOutlinePage : editorOutlinePages) {
			if(contentOutlinePage != null){
				if(contentOutlinePage instanceof Page){
					//For some editors (like JDT), the Site is not yet initialzed at this point, resulting in a NPE when creating Control:
					((Page) contentOutlinePage).init(getSite());
				}
				//Control must be created before the getControl():
				contentOutlinePage.createControl(pageBookControl);
				controlList.add(contentOutlinePage.getControl());
			}
		}
		
		Control[] controlArray = controlList.toArray(new Control[controlList.size()]);		
		
		//Sets the controls in the PageBook: 
		pageBookControl.setTabList(controlArray);
		
		int activePage = multipageEditor.getActivePage();
		showPage(activePage);
		
	}
	
	/**
	 * Activates the good page in the {@link PageBook}.
	 * 
	 * All verifications are done to assure that a page with the correct index exists.
	 * @param i
	 */
	public void showPage(int i){
		if(editorOutlinePages != null 
				&& editorOutlinePages.length>i 
				&& editorOutlinePages[i] != null
				&& editorOutlinePages[i].getControl()!=null){
			pageBookControl.showPage(editorOutlinePages[i].getControl());
		}
		else{
			TapestryUI.logWarning(this.getClass().getName() + UIErrorMessages.WAS_NOT_ABLE_TO_SHOW_THE_PAGE_WITH_ID + i);
		}
	}
	

	@Override
	public void dispose() {
		super.dispose();
		pageBookControl.dispose();
	}

	@Override
	public Control getControl() {
		return pageBookControl;
	}

	@Override
	public void setFocus() {
		pageBookControl.setFocus();
	}
	
	/**
	 * CAN BE REMOVED IF ONLY CALL SUPER:
	 */
	@Override
	public void setActionBars(IActionBars actionBars) {
		super.setActionBars(actionBars);
	}


	@Override
	public ISelection getSelection() {
		return super.getSelection();
	}


	@Override
	public void setSelection(ISelection selection) {
		super.setSelection(selection);
	}


	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		super.addSelectionChangedListener(listener);
	}
		

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		super.removeSelectionChangedListener(listener);
	}

}
