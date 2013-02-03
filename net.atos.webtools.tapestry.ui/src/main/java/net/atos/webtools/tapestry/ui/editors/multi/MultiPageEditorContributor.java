package net.atos.webtools.tapestry.ui.editors.multi;

import net.atos.webtools.tapestry.ui.editors.java.JavaEditor;
import net.atos.webtools.tapestry.ui.editors.tml.TmlEditor;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditorActionContributor;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.wst.html.ui.internal.edit.ui.ActionContributorHTML;

/**
 * Manages the installation/deinstallation of global actions for multi-page editors.
 * Responsible for the redirection of global actions to the active editor.
 * Multi-page contributor replaces the contributors for the individual editors in the multi-page editor.
 */
@SuppressWarnings("restriction")
public class MultiPageEditorContributor extends MultiPageEditorActionBarContributor {
	private CompilationUnitEditorActionContributor javaActionContributor;
	private ActionContributorHTML htmlActionContributor;
	
	private IActionBars bars;
	private IWorkbenchPage page;

	@Override
	public IActionBars getActionBars() {
		return bars;
	}


	@Override
	public IWorkbenchPage getPage() {
		return page;
	}

	/**
	 * Creates a multi-page contributor.
	 */
	public MultiPageEditorContributor() {
		super();
		htmlActionContributor = new ActionContributorHTML(){
			@Override
			public IActionBars getActionBars() {
				return bars;
			}
			@Override
			public IWorkbenchPage getPage() {
				return page;
			}
		};
		
		javaActionContributor = new CompilationUnitEditorActionContributor(){
			@Override
			public IActionBars getActionBars() {
				return bars;
			}
			@Override
			public IWorkbenchPage getPage() {
				return page;
			}
		};
	}
	
	
	@Override
	public void init(IActionBars bars) {
		this.bars = bars;
	}
	
	@Override
	public void init(IActionBars bars, IWorkbenchPage page) {
		init(bars);
		this.page = page;
	}
	
	private IEditorPart activeEditorPart;
	
	@Override
	public void setActivePage(IEditorPart editorPart) {
		if (activeEditorPart == editorPart){
			return;
		}
		activeEditorPart = editorPart;
		
		if (bars != null && editorPart instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) editorPart;
			
			//clear all
			bars.clearGlobalActionHandlers();
			
			bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), getAction(textEditor, ITextEditorActionConstants.DELETE));
			bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), getAction(textEditor, ITextEditorActionConstants.UNDO));
			bars.setGlobalActionHandler(ActionFactory.REDO.getId(), getAction(textEditor, ITextEditorActionConstants.REDO));
			bars.setGlobalActionHandler(ActionFactory.CUT.getId(), getAction(textEditor, ITextEditorActionConstants.CUT));
			bars.setGlobalActionHandler(ActionFactory.COPY.getId(), getAction(textEditor, ITextEditorActionConstants.COPY));
			bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), getAction(textEditor, ITextEditorActionConstants.PASTE));
			bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),getAction(textEditor, ITextEditorActionConstants.SELECT_ALL));
			bars.setGlobalActionHandler(ActionFactory.FIND.getId(), getAction(textEditor, ITextEditorActionConstants.FIND));
			bars.setGlobalActionHandler(ActionFactory.BACK.getId(), getAction(textEditor, ActionFactory.BACK.getId()));
			bars.setGlobalActionHandler(ActionFactory.BACKWARD_HISTORY.getId(), getAction(textEditor, ITextEditorActionConstants.PREVIOUS));
			bars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(), getAction(textEditor, IDEActionFactory.BOOKMARK.getId()));
		
			if(editorPart instanceof TmlEditor){
				initContributor(htmlActionContributor);
				htmlActionContributor.setActiveEditor(editorPart);
			}
			else if(editorPart instanceof JavaEditor){
				initContributor(javaActionContributor);
				javaActionContributor.setActiveEditor(editorPart);
			}
		}
		
		bars.updateActionBars();
	}


	/**
	 * Returns the action registed with the given text editor.
	 * @return IAction or null if editor is null.
	 */
	protected IAction getAction(ITextEditor editor, String actionID) {
		return (editor == null ? null : editor.getAction(actionID));
	}

	private void initContributor(EditorActionBarContributor contributor){
		bars.getStatusLineManager().removeAll();
		bars.getMenuManager().removeAll();
		bars.getToolBarManager().removeAll();
		if(bars instanceof IActionBars2){
			((IActionBars2)bars).getCoolBarManager().removeAll();
		}
		
		contributor.contributeToMenu(this.bars.getMenuManager());
		contributor.contributeToToolBar(this.bars.getToolBarManager());
		if (this.bars instanceof IActionBars2) {
			contributor.contributeToCoolBar(((IActionBars2) this.bars).getCoolBarManager());
		}
		contributor.contributeToStatusLine(this.bars.getStatusLineManager());
		
		
	}
	
	@Override
	public void dispose() {
		super.dispose();
		javaActionContributor.dispose();
		htmlActionContributor.dispose();
	}
}
