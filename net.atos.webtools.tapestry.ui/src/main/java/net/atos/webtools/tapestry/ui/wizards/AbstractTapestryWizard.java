/**
 * 
 */
package net.atos.webtools.tapestry.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import net.atos.webtools.tapestry.core.templates.TapestryTemplateContext;
import net.atos.webtools.tapestry.ui.TapestryUI;
import net.atos.webtools.tapestry.ui.util.UIConstants;
import net.atos.webtools.tapestry.ui.util.UIErrorMessages;
import net.atos.webtools.tapestry.ui.util.UIMessages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Common {@link Wizard} class for new page & new component wizards
 * 
 * @author a154438
 *
 */
@SuppressWarnings("restriction")
public abstract class AbstractTapestryWizard extends Wizard implements INewWizard,
		IExecutableExtension {
	protected AbstractTapestryWizardPage page;
	protected ISelection selection;
	
	/**
	 * constructor
	 */
	public AbstractTapestryWizard() {
		super();
		setNeedsProgressMonitor(true);
		setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(TapestryUI.getDefault().getImage(UIConstants.IMG_TAPESTRY_WIZZARD)));
		setWindowTitle(UIMessages.WIZARD_WINDOW_TITLE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		final String projectName = page.getProjectName();
		final String javaFolderName = page.getJavaFolderName();
		final String tmlFolderName = page.getTmlFolderName();
		final String packageName = page.getPackageName();
		final String className = page.getClassName();
		final boolean createTemplate = page.isCreatingTemplate();
		final boolean createPropertiesFile = page.isCreatingPropertiesFile();
		final String templateName = page.templateNameCombo.getText();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(projectName, javaFolderName, tmlFolderName, packageName, className, createTemplate, createPropertiesFile, templateName,
							monitor);
				} catch (CoreException e) {
					TapestryUI.logError(UIErrorMessages.EXCEPTION_WHILE_CREATING_THE_FILE, e);
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} 
		catch (InterruptedException e) {
			TapestryUI.logError("Can't create Tapestry page/component", e);
			return false;
		} 
		catch (InvocationTargetException e) {
			TapestryUI.logError("Can't create Tapestry page/component", e);
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), UIErrorMessages.ERROR,
					realException.getMessage());
			return false;
		}
		return true;
	}
	
	
	/**
	 * Create java Class in the provided package.
	 * Get the writable IPackageFragmentRoot instance for the provided java folder name.e.g.,from src/main/java select java 
	 *  
	 * Create the  package in the IPackageFragmentRoot if it doesn't exist.
	 * @return 
	 * 
	 * 
	 */
	protected String createJavaClass(IJavaProject javaProject, String javaFolderName,
			String packageName, String className, IProgressMonitor monitor,
			IPackageFragmentRoot[] roots) throws JavaModelException {
		
		// Get a writeable IPackageFragmentRoot instance
		IPackageFragmentRoot src = null;
		String[] steps = javaFolderName.split("/");
		String srcName = steps[steps.length - 1];
		for (IPackageFragmentRoot pfr : roots) {
			if (pfr.getElementName().equals(srcName)) {
				src = pfr;
				break;
			}
		}
		if(src != null){
			src.createPackageFragment(packageName, false, monitor);
			IPackageFragment aimPackage = src.getPackageFragment(packageName);
			
			monitor.beginTask("Creating " + className, 3);
	
			ICompilationUnit parentCU = new CompilationUnit((PackageFragment) aimPackage, className, null);
			String content = TapestryTemplateContext.resolveJavaTemplate(javaProject, className, parentCU);
			
			content = CodeFormatterUtil.format(CodeFormatter.K_COMPILATION_UNIT, content, 0, UIConstants.LINE_DELIMITER, javaProject);
			content = Strings.trimLeadingTabsAndSpaces(content);
			
			monitor.worked(1);
			return content;
		}
		return null;
	}
	
	
	/**
	 * Creates a file in the provided package.
	 * Get the writable IPackageFragmentRoot instance for the provided tml folder name.e.g.,from src/main/resources select resources 
	 *  
	 * Create the  package in the IPackageFragmentRoot if it doesn't exist.
	 * 
	 * @param folderName :<code>java.lang.String</code>
	 * 							- Folder name where the provided package will be created/searched
	 * @param packageName :<code>java.lang.String</code>
	 * 							- Package in which the tml file will be created
	 * @param fileName :<code>java.lang.String</code> 
	 * 						- Name of the tml file
	 * @param monitor :<code>IProgressMonitor</code>
	 * 
	 * @param roots :<code>IPackageFragmentRoot[]</code>
	 * 
	 * @param template :<code>java.lang.String</code>
	 * 					- template that will be added in the tml file
	 * 
	 * @return <code>IFile</code> tml file
	 */
	protected IFile createFile(String folderName, String packageName,
			String fileName, IProgressMonitor monitor,
			IPackageFragmentRoot[] roots, String template) throws CoreException {

		IPackageFragmentRoot src = null;
		String[] steps = folderName.split("/");
		String srcName = steps[steps.length - 1];
		for (IPackageFragmentRoot pfr : roots) {
			if (pfr.getElementName().equals(srcName)) {
				src = pfr;
				break;
			}
		}
		
		if(src != null && template != null){
			if(monitor != null){
				monitor.beginTask("Creating file " + fileName, 3);
			}
			IPackageFragment aimPackage = src.createPackageFragment(packageName, false, monitor);
	
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile pageFile = root.getFile(aimPackage.getPath().append(fileName));
			final IFile file = pageFile;
			
			InputStream stream = new ByteArrayInputStream(template.trim().getBytes());
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			
			try {
				stream.close();
			}
			catch (IOException e) {
				TapestryUI.logError(UIErrorMessages.EXCEPTION_WHILE_CLOSING_THE_FILE + fileName, e);
			}
			if(monitor != null){
				monitor.worked(1);
			}
			return file;
		}
		return null;
	}
	
	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 */
	public void doFinish(String projectName, String javaFolderName, String tmlFolderName, String packageName,
			String className, boolean createTemplate, boolean createPropertiesFile, String templateName,
			IProgressMonitor monitor) throws CoreException {
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().findMember(projectName).getProject();
		IJavaProject javaProject = JavaCore.create(project);
		IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();

		String javaClassContent = createJavaClass(javaProject, javaFolderName, packageName, className, monitor, roots);
		IFile javaFile = createFile(javaFolderName, packageName, className + ".java", monitor, roots, javaClassContent);

		// create the component file
		IFile tmlFile = null;
		if (createTemplate) {
			String content = TapestryTemplateContext.resolveTemplate(page.getProjectModel(), templateName);
			tmlFile = createFile(tmlFolderName, packageName, className + ".tml", monitor, roots, content);
		}
		
		IFile propFile = null;
		if (createPropertiesFile) {
			String content = "##<key>=<value>";
			propFile = createFile(tmlFolderName, packageName, className + ".properties", monitor, roots, content);
		}

		// ---------- Opening tml File (or Java file) ----------
		if(tmlFile != null){
			openFile(tmlFile, monitor);
		}
		else if(javaFile != null){
			openFile(javaFile, monitor);
		}
		openFile(propFile, monitor);
		monitor.worked(1);
	}
	
	/**
	 * open the file in the standard editor defined in Eclipse.
	 * 
	 * @param file
	 * @param monitor
	 */
	private void openFile(final IFile file, IProgressMonitor monitor){
		if(file != null && file.exists()){
			monitor.setTaskName("Opening file for editing...");
			getShell().getDisplay().asyncExec(new Runnable() {
	
				public void run() {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, file, true);
					}
					catch (PartInitException e) {
						TapestryUI.logError(UIErrorMessages.WAS_NOT_ABLE_TO_OPEN_FILE + file, e);
					}
				}
			});
		}
	}
}
