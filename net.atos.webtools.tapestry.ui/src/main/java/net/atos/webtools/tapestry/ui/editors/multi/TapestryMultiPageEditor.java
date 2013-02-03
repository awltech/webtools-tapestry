package net.atos.webtools.tapestry.ui.editors.multi;


import static net.atos.webtools.tapestry.core.models.FileType.CLASS;
import static net.atos.webtools.tapestry.core.models.FileType.JAVA;
import static net.atos.webtools.tapestry.core.models.FileType.TML;
import net.atos.webtools.tapestry.core.models.EditedFeatureModel;
import net.atos.webtools.tapestry.core.models.FileType;
import net.atos.webtools.tapestry.ui.TapestryUI;
import net.atos.webtools.tapestry.ui.editors.java.ClassEditor;
import net.atos.webtools.tapestry.ui.editors.java.JavaEditor;
import net.atos.webtools.tapestry.ui.editors.tml.TmlEditor;
import net.atos.webtools.tapestry.ui.util.UIErrorMessages;
import net.atos.webtools.tapestry.ui.util.UIMessages;

import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jdt.internal.ui.javaeditor.InternalClassFileEditorInput;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.text.revisions.RevisionInformation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.IStatusField;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.ITextEditorExtension2;
import org.eclipse.ui.texteditor.ITextEditorExtension3;
import org.eclipse.ui.texteditor.ITextEditorExtension4;
import org.eclipse.ui.texteditor.ITextEditorExtension5;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * This is the main Tapestry editor that is opened by the user.
 * It is contributed to the UI with extension point.
 * It is automatically associated with *.tml files, but you can also directly open the *.java file.
 * In both cases, the other file will be found using {@link FileUtil}.
 * 
 * This will result in a {@link MultiPageEditorPart} with 2 pages:
 * 
 * <ul>
 * <li> page 0, delegated to a custom XML editor (defined in {@link TmlEditor})
 * <li> page 1, delegated to the JDT Java editor (slightly modified in {@link JavaEditor}, see {@link TapestryMultiPageEditor#createJavaEditorPage()})
 * </ul>
 * 
 * If the other page is not found, the sub-editor is replaced with an error page (see {@link TapestryMultiPageEditor#createErrorControl()})
 * 
 * Some work has been done to always show the corresponding outline: overriding {@link MultiPageEditorPart#getAdapter(Class)} 
 * and implementing {@link IPageChangedListener}
 * Details of the outline implementations are in {@link MultiPageOutlinePage}
 * 
 */
@SuppressWarnings("restriction")
public class TapestryMultiPageEditor extends MultiPageEditorPart 
		implements IResourceChangeListener,	IPageChangedListener, IGotoMarker, IElementStateListener, 
		ITextEditorExtension, ITextEditorExtension2,  ITextEditorExtension3, ITextEditorExtension4, ITextEditorExtension5 {
	private final static String TML_EDITOR_ID = "theTmlEditor";
	private final static String JAVA_EDITOR_ID = "theJavaEditor";
	
	private EditedFeatureModel tapestryFeatureModel;
	
	private IEditorInput tmlEditorInput;
	private IEditorInput javaEditorInput;
	
	private TextEditor tmlEditor;
	private org.eclipse.jdt.internal.ui.javaeditor.JavaEditor javaEditor;
	
	private IStorage storage;
	private IClassFile openedClassFile;
	
	/**
	 * true when the other file is not found
	 */
	boolean soloMode = false;
	
	/**
	 * reference to the Outline
	 */
	MultiPageOutlinePage outline;

	/**
	 * Creates a multi-page tmlEditor example.
	 */
	public TapestryMultiPageEditor() {
		super();
		addPageChangedListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, 
				IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE  
				| IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.POST_BUILD );
	}
	
	
	
	public EditedFeatureModel getTapestryFeatureModel() {
		return tapestryFeatureModel;
	}
	

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	@Override
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (editorInput instanceof IStorageEditorInput) {
			try {
				storage = ((IStorageEditorInput) editorInput).getStorage();
			}
			catch (CoreException e) {
				TapestryUI.logError(UIErrorMessages.CAN_T_OPEN_FILE, e);
				throw new PartInitException(UIErrorMessages.CAN_T_OPEN_FILE, e);
			}
		}
		else if(editorInput instanceof IClassFileEditorInput){
			openedClassFile = ((IClassFileEditorInput) editorInput).getClassFile();
			
		}
		else{
			throw new PartInitException(UIErrorMessages.INVALID_INPUT_MUST_BE_I_STORAGE_EDITOR_INPUT);
		}
		
		if(storage != null){
			tapestryFeatureModel = new EditedFeatureModel(storage, false);
		}
		else if(openedClassFile != null){
			tapestryFeatureModel = new EditedFeatureModel(openedClassFile, false);
		}
		
		//The editor's paramName for MyPage.java/MyPage.tml couple is "MyPage":
		setPartName(tapestryFeatureModel.getName());
		
		//CASE-1: User has opened the Java File 
		if(tapestryFeatureModel.getInitialType() == JAVA || tapestryFeatureModel.getInitialType() == CLASS){
			javaEditorInput = editorInput;
			
			if(tapestryFeatureModel.getTmlFile() instanceof IFile){
				tmlEditorInput = new FileEditorInput((IFile) tapestryFeatureModel.getTmlFile());
			}
		}
		//CASE-2: User has opened the tml file (or any other kind of file, that is then considered the tml file) 
		else{
			tmlEditorInput = editorInput;
			if(tapestryFeatureModel.getJavaFile() instanceof IFile){
				javaEditorInput = new FileEditorInput((IFile) tapestryFeatureModel.getJavaFile());
			}
			else if(tapestryFeatureModel.getClassFile() != null){
				javaEditorInput = new InternalClassFileEditorInput(tapestryFeatureModel.getClassFile());
			}
		}
		
		IEditorInput globalInput;
		if (tmlEditorInput == null) {
			soloMode = true;
			globalInput = javaEditorInput;
		}
		else if (javaEditorInput == null) {
			soloMode = true;
			globalInput = tmlEditorInput;
		}
		else{
			//set the default editor for tmlEditor input
			globalInput = new MultiEditorInput(new String[]{TML_EDITOR_ID, JAVA_EDITOR_ID}, 
					new IEditorInput[]{tmlEditorInput, javaEditorInput}).getInput()[0];
		}
		super.init(site, globalInput);
	}
	

	/**
	 * Creates the pages of the multi-page tmlEditor.
	 */
	protected void createPages() {
		createTmlEditorPage();
		createJavaEditorPage();
		if(tmlEditor != null){
			IDocumentProvider tmlDocumentProvider= tmlEditor.getDocumentProvider();
			tmlDocumentProvider.addElementStateListener(this);
		}
		if(javaEditor != null){
			IDocumentProvider javaDocumentProvider= javaEditor.getDocumentProvider();
			javaDocumentProvider.addElementStateListener(this);
		}
		
		setActivePage(tapestryFeatureModel.getInitialType().getRank());
	}


	/**
	 * Creates page 0 of the multi-page tmlEditor,
	 * which contains a text tmlEditor.
	 * 
	 * tmlEditorInput must be set before, or it'll replace the page by an error page
	 * (calling {@link #createErrorControl()}
	 */
	protected void createTmlEditorPage() {
		if(tmlEditorInput == null){
			addPage(TML.getRank(), createErrorControl());
		}
		else{
			try {
				tmlEditor = new TmlEditor();
				addPage(TML.getRank(), tmlEditor, tmlEditorInput);
			} 
			catch (PartInitException e) {
				TapestryUI.logError(UIErrorMessages.ERROR_CREATING_NESTED_TAPESTRY_EDITOR, e);
			}
		}
		setPageText(TML.getRank(), UIMessages.TEMPLATE_TAB_LABEL);
	}
	
	/**
	 * Creates page 1 of the multi-page tmlEditor,
	 * which contains a "java" tmlEditor.
	 * 
	 * If the input is a .java file, it'll open it in {@link JavaEditor} which is nearly the standard
	 * {@link CompilationUnitEditor}
	 * 
	 *  If it's a class, it'll directly open the standard ClassFileEditor, and code will appear only 
	 *  if sources are associated in Eclipse 
	 * 
	 * javaEditorInput must be set before, or it'll replace the page by an error page
	 * (calling {@link #createErrorControl()}
	 */
	protected void createJavaEditorPage() {
		if(javaEditorInput == null){
			addPage(JAVA.getRank(), createErrorControl());
		}
		else if(javaEditorInput instanceof IClassFileEditorInput){
			javaEditor = new ClassEditor();
			try {
				addPage(JAVA.getRank(), javaEditor, javaEditorInput);
			} 
			catch (PartInitException e) {
				TapestryUI.logError(UIErrorMessages.ERROR_CREATING_NESTED_JAVA_EDITOR, e);
			}
		}
		else{
			javaEditor = new JavaEditor(tapestryFeatureModel);
	
			try {
				addPage(JAVA.getRank(), javaEditor, javaEditorInput);
			} 
			catch (PartInitException e) {
				TapestryUI.logError(UIErrorMessages.ERROR_CREATING_NESTED_JAVA_EDITOR, e);
			}
		}
		setPageText(JAVA.getRank(), UIMessages.JAVA_TAB_LABEL);
	}
	/**
	 * When a tml/java file is opened, and the corresponding java/tml file is not found,
	 * We replace the editor with a composite, with an error message 
	 * => this allow to use it as a Java-only or xml-only editor 
	 *  
	 * @return
	 */
	protected Control createErrorControl(){
		Text textControl = new Text(getContainer(), SWT.NONE);
		textControl.setText(UIMessages.FILE_NOT_FOUND);
		textControl.setEditable(false);

		return textControl;
	}

	
	/**
	 * The <code>MultiPageEditorPart</code> implementation of this 
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	@Override
	public void dispose() {
		//Don't call super.dispose() because it permanently removes the tapestry image used in the "open-with" sub-menu...
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		tapestryFeatureModel = null;
		super.dispose();
	}
	
	/**
	 * Saves the multi-page tmlEditor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		if(javaEditor != null){
			javaEditor.doSave(monitor);
		}
		if(tmlEditor != null){
			tmlEditor.doSave(monitor);
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	/**
	 * Saves the multi-page tmlEditor's document as another file.
	 * Also updates the text for page 0's tab, and updates this multi-page tmlEditor's input
	 * to correspond to the nested tmlEditor's.
	 */
	@Override
	public void doSaveAs() {
		IEditorPart editor = getActiveEditor();
		if(editor != null){
			editor.doSaveAs();
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		IResource resource = marker.getResource();
		if(resource != null){
			switch (FileType.getTypeFromExtension(resource.getFileExtension())) {
			case JAVA:
			case CLASS:
				setActivePage(JAVA.getRank());
				IDE.gotoMarker(getEditor(JAVA.getRank()), marker);
				break;
			case TML:
				setActivePage(TML.getRank());
				IDE.gotoMarker(getEditor(TML.getRank()), marker);
				break;
			default:
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapterClass) {
		if (IContentOutlinePage.class.equals(adapterClass) && ! soloMode) {
			if(outline == null){
				//WARNING: editors must be added by order of pageRank
				outline = new MultiPageOutlinePage(this, tmlEditor, javaEditor);
			}
			return outline;
		}
		return super.getAdapter(adapterClass);
	}
	
	
	public void pageChangeUsingShortcut(int newPageIndex){
		setActivePage(newPageIndex);
	}
	
	/**
	 * Overridden for "link with editor function": we must return the opened editor.
	 */
	@Override
	public IEditorInput getEditorInput() {
		//CASE-1: one is null => we return the other one (they can't be both null...)
		if(javaEditorInput == null){
			return tmlEditorInput;
		}
		if(tmlEditorInput == null){
			return javaEditorInput;
		}
		
		//CASE-2: we return the active page's editor
		if(getActivePage() == FileType.JAVA.getRank() || getActivePage() == FileType.CLASS.getRank()){
			return javaEditorInput;
		}
		return tmlEditorInput;
	}
	
	private void closeEditor(final Object element) {
		Display.getDefault().asyncExec(
				new Runnable(){
					public void run(){
						IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
						for (int i = 0; i<pages.length; i++){
							if(tmlEditor != null 
									&& tmlEditor.getEditorInput() instanceof FileEditorInput
									&& (((FileEditorInput)tmlEditor.getEditorInput()).equals(element) 
											|| ((FileEditorInput)tmlEditor.getEditorInput()).getFile().getProject().equals(element))){
								
								IEditorPart editorPart = pages[i].findEditor(tmlEditor.getEditorInput());
								pages[i].closeEditor(editorPart,true);
							}
							else if(javaEditor != null 
									&& javaEditor.getEditorInput() instanceof FileEditorInput
									&& (((FileEditorInput)javaEditor.getEditorInput()).equals(element) 
											|| ((FileEditorInput)javaEditor.getEditorInput()).getFile().getProject().equals(element))){
								
								IEditorPart editorPart = pages[i].findEditor(javaEditor.getEditorInput());
								pages[i].closeEditor(editorPart,true);
							}
						}
					}            
				});
	}

	//----------------------------------------------------------------------------------------------
	//
	//        						Listeners Implementation:
	//
	//----------------------------------------------------------------------------------------------

	//--------------- IPageChangedListener -----------------------------
	public void pageChanged(PageChangedEvent event) {
		if(outline != null){
			int activePage = getActivePage();
			outline.showPage(activePage);
		}
	}
	
	//-------------------- IResourceChangeListener ----------------------
	
	/**
	 * When the project is closed/removed with an opened editor, we must close it, because the file is not accessible anymore
	 */
	public void resourceChanged(final IResourceChangeEvent event){
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE || event.getType() == IResourceChangeEvent.PRE_DELETE){
			if(event.getResource() != null){
				closeEditor(event.getResource());
			}
		}
		else if(event.getType() == IResourceChangeEvent.POST_BUILD){
			if(storage != null){
				tapestryFeatureModel = new EditedFeatureModel(storage, true);
			}
			else if(openedClassFile != null){
				tapestryFeatureModel = new EditedFeatureModel(openedClassFile, true);
			}
		}
	}

	//------------- IElementStateListener -----------------------------
	
	/**
	 * One of the opened files has been deleted => we must close editor
	 */
	public void elementDeleted(Object element) {
		closeEditor(element);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementDirtyStateChanged(java.lang.Object, boolean)
	 */
	public void elementDirtyStateChanged(Object element, boolean isDirty) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementContentAboutToBeReplaced(java.lang.Object)
	 */
	public void elementContentAboutToBeReplaced(Object element) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementContentReplaced(java.lang.Object)
	 */
	public void elementContentReplaced(Object element) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementMoved(java.lang.Object, java.lang.Object)
	 */
	public void elementMoved(Object originalElement, Object movedElement) {
	}
	
	
	//-----------------------------------------------------------------------------------
	//
	//						ITextEditorExtension: StatusField/ReadOnly/Ruler
	//
	//-----------------------------------------------------------------------------------
	
	@Override
	public void setStatusField(IStatusField field, String category) {
		if(getActiveEditor() instanceof ITextEditorExtension){
			((ITextEditorExtension)getActiveEditor()).setStatusField(field, category);
		}
	}
	
	@Override
	public boolean isEditorInputReadOnly() {
		if(getActiveEditor() instanceof ITextEditorExtension){
			return ((ITextEditorExtension)getActiveEditor()).isEditorInputReadOnly();
		}
		return false;
	}
	
	@Override
	public void addRulerContextMenuListener(IMenuListener listener) {
		if(getActiveEditor() instanceof ITextEditorExtension){
			((ITextEditorExtension)getActiveEditor()).addRulerContextMenuListener(listener);
		}
	}
	
	@Override
	public void removeRulerContextMenuListener(IMenuListener listener) {
		if(getActiveEditor() instanceof ITextEditorExtension){
			((ITextEditorExtension)getActiveEditor()).removeRulerContextMenuListener(listener);
		}
	}
	
	//-----------------------------------------------------------------------------------
	//
	//								ITextEditorExtension2
	//
	//-----------------------------------------------------------------------------------
	
	@Override
	public boolean isEditorInputModifiable() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean validateEditorInputState() {
		// TODO Auto-generated method stub
		return true;
	}
	
	
	//-----------------------------------------------------------------------------------
	//
	//								ITextEditorExtension3: Smart insert
	//
	//-----------------------------------------------------------------------------------

	@Override
	public InsertMode getInsertMode() {
		if(getActiveEditor() instanceof ITextEditorExtension3){
			return ((ITextEditorExtension3)getActiveEditor()).getInsertMode();
		}
		return ITextEditorExtension3.SMART_INSERT;
	}

	@Override
	public void setInsertMode(InsertMode mode) {
		if(getActiveEditor() instanceof ITextEditorExtension3){
			((ITextEditorExtension3)getActiveEditor()).setInsertMode(mode);
		}
	}

	@Override
	public void showChangeInformation(boolean show) {
		if(getActiveEditor() instanceof ITextEditorExtension3){
			((ITextEditorExtension3)getActiveEditor()).showChangeInformation(show);
		}
	}

	@Override
	public boolean isChangeInformationShowing() {
		if(getActiveEditor() instanceof ITextEditorExtension3){
			return ((ITextEditorExtension3)getActiveEditor()).isChangeInformationShowing();
		}
		return true;
	}

	//-----------------------------------------------------------------------------------
	//
	//								ITextEditorExtension4: Annotation
	//
	//-----------------------------------------------------------------------------------
	
	@Override
	public Annotation gotoAnnotation(boolean forward) {
		if(getActiveEditor() instanceof ITextEditorExtension4){
			return ((ITextEditorExtension4)getActiveEditor()).gotoAnnotation(forward);
		}
		return null;
	}
	
	@Override
	public void showRevisionInformation(RevisionInformation info, String quickDiffProviderId) {
		if(getActiveEditor() instanceof ITextEditorExtension4){
			((ITextEditorExtension4)getActiveEditor()).showRevisionInformation(info, quickDiffProviderId);
		}
	}

	
	//-----------------------------------------------------------------------------------
	//
	//								ITextEditorExtension5: Block selection
	//
	//-----------------------------------------------------------------------------------
	
	@Override
	public boolean isBlockSelectionModeEnabled() {
		if(getActiveEditor() instanceof ITextEditorExtension5){
			return ((ITextEditorExtension5)getActiveEditor()).isBlockSelectionModeEnabled();
		}
		return false;
	}

	@Override
	public void setBlockSelectionMode(boolean state) {
		if(getActiveEditor() instanceof ITextEditorExtension5){
			((ITextEditorExtension5)getActiveEditor()).setBlockSelectionMode(state);
		}
	}
}
