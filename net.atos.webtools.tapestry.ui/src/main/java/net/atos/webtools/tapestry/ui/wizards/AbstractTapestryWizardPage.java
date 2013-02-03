/**
 * 
 */
package net.atos.webtools.tapestry.ui.wizards;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.atos.webtools.tapestry.core.TapestryCore;
import net.atos.webtools.tapestry.core.models.ProjectModel;
import net.atos.webtools.tapestry.ui.TapestryUI;
import net.atos.webtools.tapestry.ui.util.UIConstants;
import net.atos.webtools.tapestry.ui.util.UIErrorMessages;
import net.atos.webtools.tapestry.ui.util.UIMessages;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Abstract base class implements createControl method of WizardPage class.
 * Creates the tapestry wizard for new page/component creation.
 * 
 * @author a154438
 */
@SuppressWarnings("restriction")
public abstract class AbstractTapestryWizardPage extends WizardPage {
	private static final String EMPTY_STRING = "EMPTY_STRING";
	private static final String JAVA_PACKAGE_FRAGMENT_ROOT = "JAVA_PACKAGE_FRAGMENT_ROOT";
	
	private ProjectModel projectModel;
	
	protected TemplateStore templateStore;
	
	protected HashMap<String, Object> model = new HashMap<String, Object>();
	
	protected IStatus packageFieldStatus;
	protected IStatus pageNameFieldStatus;
	
	protected String projectName;
	
	//--------------------------------------------------
	// 				SWT Controls: 
	//--------------------------------------------------
	protected Combo javaFolderCombo;
	protected Combo tmlSourceFolderCombo;


	protected Text packageText;
	protected Button packageButton;
	protected Label packageLabel;

	protected Text classText;
	protected Label classLabel;

	protected Label projectNameLabel;
	protected Combo projectNameCombo;

	protected Button createPropertiesFile;
	protected Combo templateNameCombo;

	// --------------------------------------------------------------------------------------------------------------
	//
	// GETTERS SETTERS
	//
	// --------------------------------------------------------------------------------------------------------------

	
	public ProjectModel getProjectModel() {
		return projectModel;
	}

	public String getProjectName() {
		return projectNameCombo.getText();
	}

	public String getJavaFolderName() {
		return javaFolderCombo.getText();
	}

	public String getTmlFolderName() {
		return tmlSourceFolderCombo.getText();
	}

	public String getPackageName() {
		return packageText.getText();
	}

	public String getClassName() {
		return classText.getText();
	}

	/**
	 * By default, we create a template
	 * 
	 * @return true by default
	 */
	public boolean isCreatingTemplate() {
		return true;
	}

	public boolean isCreatingPropertiesFile() {
		return createPropertiesFile.getSelection();
	}

	// --------------------------------------------------------------------------------------------------------------
	//
	// CONSTRUCTORS
	//
	// --------------------------------------------------------------------------------------------------------------
	/**
	 * @param pageName
	 */
	public AbstractTapestryWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	public AbstractTapestryWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	// --------------------------------------------------------------------------------------------------------------
	//
	// ADDS ALL THE CONTROLS TO THE PAGE
	//
	// --------------------------------------------------------------------------------------------------------------

	/**
	 * Overridden method of WizardPage.
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.widthHint = 300;
		composite.setLayoutData(data);

		addProjectNameGroup(composite);
		addJavaFolderComboGroup(composite);
		addTmlSourceFolderComboGroup(composite);

		addSeperator(composite, 3);

		addPackageGroup(composite);
		addClassnameGroup(composite);
		addCreatePropertiesFile(composite);
		addCreateTemplate(composite);
		// select a template from the list
		addTemplateGroup(composite);

		updateSrcFolders();

		// set the cursor focus
		// - to the "Java package" if it is empty
		// - to the "Class paramName" - otherwise
		if (packageText.getText().trim().length() == 0) {
			packageText.setFocus();
		} else {
			classText.setFocus();
		}
		//set the finish button disabled.
		setPageComplete(false);
		setControl(composite);
	}
	
	// --------------------------------------------------------------------------------------------------------------
	//
	// GETTERS SETTERS
	//
	// --------------------------------------------------------------------------------------------------------------
	
	@Override
	public void dispose() {
		super.dispose();
		
		if(javaFolderCombo != null){
			javaFolderCombo.dispose();
			javaFolderCombo = null;
		}
		if(tmlSourceFolderCombo != null){
			tmlSourceFolderCombo.dispose();
			tmlSourceFolderCombo = null;
		}
		
		if(packageText != null){
			packageText.dispose();
			packageText = null;
		}
		if(packageButton != null){
			packageButton.dispose();
			packageButton = null;
		}
		if(packageLabel != null){
			packageLabel.dispose();
			packageLabel = null;
		}
		
		if(classText != null){
			classText.dispose();
			classText = null;
		}
		if(classLabel != null){
			classLabel.dispose();
			classLabel = null;
		}
		
		if(projectNameLabel != null){
			projectNameLabel.dispose();
			projectNameLabel = null;
		}
		if(projectNameCombo != null){
			projectNameCombo.dispose();
			projectNameCombo = null;
		}
		
		if(createPropertiesFile != null){
			createPropertiesFile.dispose();
			createPropertiesFile = null;
		}
		if(templateNameCombo != null){
			templateNameCombo.dispose();
			templateNameCombo = null;
		}
		
		model = null;
		templateStore = null;
	}

	/**
	 * Add package name group to composite that will be used to select a project
	 * from the workspace.</br> The group consist of one Label and one
	 * Combo</br> Label:name of combo</br> Combo: contains a list of active
	 * projects from the workspace.</br> Create a Label in the Composite,set
	 * layout data of the label as GridLayout,set a text to it.</br> Create
	 * Combo in the Composite,set layout data of the label as GridLayout</br>
	 * Call initializeProjectList() to initialize the Combo.</br> Add a
	 * selection listener that will be listening to a selection event in the
	 * Combo.</br> On each selection event,change the values of source folder
	 * combo of java and tml.</br> Add a empty label to the composite to bring
	 * the cursor to the next line.
	 * 
	 * @param parent
	 *            :<code>Composite</code> - composite on which the project combo
	 *            will be added
	 */
	protected void addProjectNameGroup(Composite parent) {
		// set up project paramName label
		projectNameLabel = new Label(parent, SWT.NONE);
		projectNameLabel.setText(UIMessages.PROJECT_LABEL);
		GridData data = new GridData();
		projectNameLabel.setLayoutData(data);
		// set up project paramName entry field
		projectNameCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 300;
		data.horizontalSpan = 1;
		projectNameCombo.setLayoutData(data);
		projectNameCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				updateSrcFolders();

				IProject iProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectNameCombo.getText());
				setDefaultPackage(getTapestryPackage(iProject));
			}
		});
		initializeProjectList();
		new Label(parent, SWT.NONE);
	}

	/**
	 * Add java folder name group to composite
	 */
	protected void addJavaFolderComboGroup(Composite composite) {
		Label folderLabel = new Label(composite, SWT.LEFT);
		folderLabel.setText(UIMessages.JAVA_SOURCE_FOLDER_LABEL);
		folderLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		javaFolderCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 300;
		data.horizontalSpan = 1;
		javaFolderCombo.setLayoutData(data);

		new Label(composite, SWT.NONE);

	}

	/**
	 * Add tml folder group to composite
	 */
	protected void addTmlSourceFolderComboGroup(Composite composite) {
		// folder
		Label folderLabel = new Label(composite, SWT.LEFT);
		folderLabel.setText(UIMessages.TML_SOURCE_FOLDER_LABEL);
		folderLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		tmlSourceFolderCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 300;
		data.horizontalSpan = 1;
		tmlSourceFolderCombo.setLayoutData(data);
	}

	private void updateSrcFolders() {
		if (projectNameCombo != null) {
			String projectName = projectNameCombo.getText();
			if (projectName != null && projectName.length() > 0) {
				String[] listOfFolders = initializeFolderList();

				if (javaFolderCombo != null) {
					// initialize the src folder list
					javaFolderCombo.setItems(listOfFolders);
					if (listOfFolders != null && listOfFolders.length > 0) {
						String defaultFolder = null;
						for (String folderName : listOfFolders) {
							if (UIConstants.JAVA_RESOURCES_FOLDER
									.equals(folderName)) {
								defaultFolder = folderName;
							}
						}
						if (defaultFolder != null) {
							javaFolderCombo.setText(defaultFolder);
						} else {
							javaFolderCombo.setText(listOfFolders[0]);
						}
					}
				}

				if (tmlSourceFolderCombo != null) {
					// initialize the src folder list
					tmlSourceFolderCombo.setItems(listOfFolders);
					if (listOfFolders != null && listOfFolders.length > 0) {
						String defaultFolder = null;
						for (String folderName : listOfFolders) {
							if (UIConstants.TML_RESOURCES_FOLDER
									.equals(folderName)) {
								defaultFolder = folderName;
							}
						}
						if (defaultFolder != null) {
							tmlSourceFolderCombo.setText(defaultFolder);
						} else {
							tmlSourceFolderCombo.setText(listOfFolders[0]);
						}
					}
				}
			}
		}
	}

	/**
	 * Add separator to composite
	 */
	protected void addSeperator(Composite composite, int horSpan) {
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.widthHint = 300;
		// Separator label
		Label seperator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = horSpan;
		seperator.setLayoutData(data);
	}
	
	/**
	 * Add package group to composite
	 */
	protected void addPackageGroup(Composite composite) {
		// package
		packageLabel = new Label(composite, SWT.LEFT);
		packageLabel.setText(UIMessages.PACKAGE_LABEL);
		packageLabel
				.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		packageText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		packageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Get the selected package on which tml/java files will be created
		IPackageFragment packageFragment = getSelectedPackageFragment();
		String targetProject = projectNameCombo.getText();

		// get the package name from web.xml
		String tapestryPackage = getTapestryPackage(getSelectedProject());

		// set default package name in the packageText for pages/component
		setDefaultPackage(tapestryPackage);

		if (packageFragment != null
				&& packageFragment.exists()
				&& packageFragment.getJavaProject().getElementName()
						.equals(targetProject)) {
			IPackageFragmentRoot root = getPackageFragmentRoot(packageFragment);
			model.put(JAVA_PACKAGE_FRAGMENT_ROOT, root);
			// model.put(INewJavaClassDataModelProperties.JAVA_PACKAGE,
			// packageFragment.getElementName());
			packageText.setText(packageFragment.getElementName());
		}
		// validate the package name for the page
		packageText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				packageFieldStatus = JavaConventions.validatePackageName(
						packageText.getText(), "5.0", "5.0");
				if (packageFieldStatus.getCode() == -1
						&& packageFieldStatus.getSeverity() == 2) {
					setPageComplete(true);
					StatusUtil
							.applyToStatusLine(AbstractTapestryWizardPage.this,
									packageFieldStatus);
				} else if (packageFieldStatus.getCode() == -1
						&& packageFieldStatus.getSeverity() == 4) {
					setPageComplete(false);
					StatusUtil
							.applyToStatusLine(AbstractTapestryWizardPage.this,
									packageFieldStatus);
				} else {
					validateRootPackageName(packageText.getText());
				}
				if (packageFieldStatus.isOK()){ 
						if(pageNameFieldStatus != null) {
							checkPageNameStatus();
						}else{
							setPageComplete(false);
							setErrorMessage(null);
							setMessage(null);
						}
				}
			}
		});
		packageButton = new Button(composite, SWT.PUSH);
		packageButton.setText(UIMessages.BROWSE_BUTTON);
		packageButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		packageButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				handlePackageButtonPressed();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}
	
	public void checkPageNameStatus(){
		if (pageNameFieldStatus != null && pageNameFieldStatus.getCode() == -1 && pageNameFieldStatus.getSeverity() == 2) {
			setErrorMessage(null);
			setMessage(pageNameFieldStatus.getMessage(), IStatus.WARNING);
			setPageComplete(true);
		} else if (pageNameFieldStatus != null && pageNameFieldStatus.getCode() == -1 && pageNameFieldStatus.getSeverity() == 4) {
			setPageComplete(false);
			StatusUtil.applyToStatusLine(this, pageNameFieldStatus);
		} else {
			setErrorMessage(null);
			setMessage(null);
			setPageComplete(true);
		}
	}

	public void checkPackageStatus() {
		if (packageFieldStatus.getCode() == -1
				&& packageFieldStatus.getSeverity() == 2) {
			setPageComplete(true);
			StatusUtil.applyToStatusLine(this, packageFieldStatus);

		} else if (packageFieldStatus.getCode() == -1
				&& packageFieldStatus.getSeverity() == 4) {
			setPageComplete(false);
			StatusUtil.applyToStatusLine(this, packageFieldStatus);
		} else {
			setPageComplete(true);
			setErrorMessage(null);
			setMessage(null);
		}
	}

	/**
	 * Add a text box group to composite for the class name.
	 * 
	 * @param composite
	 *            <code>Composite</code> - Composite where the text field for
	 *            the class name will be added.
	 */
	protected void addClassnameGroup(Composite composite) {
		// class paramName
		classLabel = new Label(composite, SWT.LEFT);
		classLabel.setText(getClassNameLabel());
		classLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		classText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		classText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		classText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				//Validate the tml page name(Apply all validation for Java class name)
				pageNameFieldStatus = validatePageName();
				
				if (pageNameFieldStatus.getCode() == -1
						&& pageNameFieldStatus.getSeverity() == 2) {
					setErrorMessage(null);
					setMessage(pageNameFieldStatus.getMessage(),
							IStatus.WARNING);
					setPageComplete(true);
				} else if (pageNameFieldStatus.getCode() == -1
						&& pageNameFieldStatus.getSeverity() == 4) {
					setPageComplete(false);
					StatusUtil.applyToStatusLine(
							AbstractTapestryWizardPage.this,
							pageNameFieldStatus);
				} else {
					setPageComplete(true);
					setErrorMessage(null);
					setMessage(null);
					if(packageFieldStatus!=null)
						checkPackageStatus();
				}
			}

			private IStatus validatePageName() {
				//First validate that the Name doesn't contain '.' character(qualified character)  
				if (classText.getText().indexOf('.') != -1) {
					pageNameFieldStatus = new Status(
							IStatus.ERROR,
							JavaCore.PLUGIN_ID,
							-1,
							NewWizardMessages.NewTypeWizardPage_error_QualifiedName,
							null);
					return pageNameFieldStatus;
				}
				// ----------------------------------------------------------------------------
				// Validate the tml page name.
				// If page name is valid then check the page is already exist in the package.
				// ----------------------------------------------------------------------------
				pageNameFieldStatus = JavaConventions.validateJavaTypeName(
						classText.getText(), "5.0", "5.0");
				if(pageNameFieldStatus.getCode() == -1){
					return pageNameFieldStatus;
				} else {
					IPackageFragment pack = null;
					IProject targetProject = ResourcesPlugin.getWorkspace()
							.getRoot().findMember(projectName).getProject();
					try {
						IPackageFragmentRoot[] roots = JavaCore.create(
								targetProject).getAllPackageFragmentRoots();
						for (IPackageFragmentRoot root : roots) {
							IPackageFragment packageFragment = root.getPackageFragment(packageText.getText());
							String comboText = javaFolderCombo.getText();
							String fullPath = root.getPath().toString().replace("/" + projectName + "/", "").trim();
							if (packageFragment != null && comboText.equals(fullPath)) {
								pack = packageFragment;
								break;
							}
						}
					} catch (JavaModelException e) {
						TapestryUI.logError(
								UIErrorMessages.EXCEPTION_WHILE_SELECTING_THE_PACKAGE, e);
					}
					//check if the resources already exist
					if(pack != null){
						ICompilationUnit cu = pack.getCompilationUnit(getCompilationUnitName(classText.getText()));
						IResource resource = cu.getResource();
						if (resource.exists()) {
							pageNameFieldStatus = new Status(
									IStatus.ERROR,
									JavaCore.PLUGIN_ID,
									-1,
									NewWizardMessages.NewTypeWizardPage_error_TypeNameExists,
									null);
							return pageNameFieldStatus;
						}
						//check if Type with same name but different case exists.
						URI location= resource.getLocationURI();
						if (location != null) {
							try {
								IFileStore store= EFS.getStore(location);
								if (store.fetchInfo().exists()) {
									pageNameFieldStatus = new Status(
											IStatus.ERROR,
											JavaCore.PLUGIN_ID,
											-1,
											NewWizardMessages.NewTypeWizardPage_error_TypeNameExistsDifferentCase,
											null);
									return pageNameFieldStatus;
								}
							} catch (CoreException e) {
								TapestryUI.logError(UIErrorMessages.EXCEPTION_WHILE_SELECTING_THE_PACKAGE,
										e);
							}
						}
					}
				}
				return pageNameFieldStatus;
			}
		});
		new Label(composite, SWT.LEFT);
	}
	
	protected String getCompilationUnitName(String typeName) {
		return typeName + JavaModelUtil.DEFAULT_CU_SUFFIX;
	}
	
	protected void addCreateTemplate(Composite composite) {
		// Nothing by default
	}

	protected void addCreatePropertiesFile(Composite parent) {
		Label templateLabel = new Label(parent, SWT.LEFT);
		templateLabel.setText(UIMessages.CREATE_PROPERTIES_LABEL);
		templateLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		createPropertiesFile = new Button(parent, SWT.CHECK);
		createPropertiesFile.setToolTipText(UIMessages.CREATE_PROPERTIES_TOOLTIP);
		createPropertiesFile.setSelection(false);
		createPropertiesFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(parent, SWT.NONE);
	}

	protected void addTemplateGroup(Composite composite) {
		Label templateNameLabel = new Label(composite, SWT.NONE);
		templateNameLabel.setText(UIMessages.TEMPLATE_LABEL);
		GridData data = new GridData();
		templateNameLabel.setLayoutData(data);
		// set up project paramName entry field
		templateNameCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 300;
		data.horizontalSpan = 1;
		templateNameCombo.setLayoutData(data);
		String[] templates = getListOfTemplate();
		templateNameCombo.setItems(templates);
		if(templates.length > 0){
			templateNameCombo.setText(templates[0]);
		}
		templateNameCombo.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				String formerSelection = templateNameCombo.getText();
				String[] templates = getListOfTemplate();
				templateNameCombo.setItems(templates);
				templateNameCombo.setText(formerSelection);
			}
		});
		
		Link editTemplateLink = new Link(composite, SWT.NONE);
		editTemplateLink.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
	    String message = UIMessages.EDIT_TEMPLATES;
	    editTemplateLink.setText(message);
	    editTemplateLink.setSize(400, 100);
	    editTemplateLink.addSelectionListener(new SelectionAdapter(){
	        @Override
	        public void widgetSelected(SelectionEvent e) {
	        	PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(getShell(), 
	        			UIConstants.TEMPLATE_PREFERENCE_PAGE, null, null);
	        	if (pref != null){
	        	pref.open();
	        	}
	        }
	    });
	}

	/**
	 * Get list of template from the external tmlTemplate.xml file for a
	 * contextType
	 * 
	 * @return <code>java.lang.String[]</code>
	 */
	private String[] getListOfTemplate() {
		List<String> list = new ArrayList<String>();
		templateStore = TapestryCore.getDefault().getTmlTemplateStore();
		Template[] compTemplates = templateStore.getTemplates(getContextType());
		for (Template template : compTemplates) {
			list.add(template.getName());
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Get the package name from the web.xml file for the selected project.
	 * 
	 * @return <code>java.lang.String</code>
	 */
	private String getTapestryPackage(IProject iProject) {
		if(iProject == null){
			return null;
		}
		projectModel = new ProjectModel(iProject);
		projectModel.init();
		return projectModel.getAppPackage();
	}

	private IPackageFragmentRoot getPackageFragmentRoot(
			IPackageFragment packageFragment) {
		if (packageFragment == null)
			return null;
		else if (packageFragment.getParent() instanceof IPackageFragment)
			return getPackageFragmentRoot((IPackageFragment) packageFragment
					.getParent());
		else if (packageFragment.getParent() instanceof IPackageFragmentRoot)
			return (IPackageFragmentRoot) packageFragment.getParent();
		else
			return null;
	}

	private IPackageFragment getSelectedPackageFragment() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null)
			return null;
		ISelection selection = window.getSelectionService().getSelection();
		if (selection == null)
			return null;

		IJavaElement element = getInitialJavaElement(selection);
		
		// getInitialJavaElementForTml
		if (element != null) {
			if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
				return (IPackageFragment) element;
			} else if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
				IJavaElement parent = ((ICompilationUnit) element).getParent();
				if (parent.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
					return (IPackageFragment) parent;
				}
			} else if (element.getElementType() == IJavaElement.TYPE) {
				return ((IType) element).getPackageFragment();
			}
			
		}
		return null;
	}

	/**
	 * This method gets called when the browse button for package is pressed.
	 * Open a ElementListSelectionDialog box with all the existing packages for
	 * the pages.
	 * 
	 */
	private void handlePackageButtonPressed() {
		IPackageFragmentRoot packRoot = (IPackageFragmentRoot) model
				.get(JAVA_PACKAGE_FRAGMENT_ROOT);
		if (packRoot == null && !this.javaFolderCombo.getText().isEmpty()) {
			String projectName = this.projectNameCombo.getText();
			String folderName = this.javaFolderCombo.getText();
			if (projectName != null && projectName.length() > 0) {
				IProject targetProject = ResourcesPlugin.getWorkspace()
						.getRoot().findMember(projectName).getProject();
				try {
					IPackageFragmentRoot[] roots = JavaCore.create(targetProject).getAllPackageFragmentRoots();
					for (IPackageFragmentRoot root : roots) {
						if ((folderName).equals(root.getPath().toString().replace("/" + projectName + "/", "").trim())) {
							packRoot = root;
							break;
						}
					}
				} catch (JavaModelException e) {
					TapestryUI.logError(UIErrorMessages.EXCEPTION_WHILE_SELECTING_THE_PACKAGE,
							e);
				}
			}
		}
		if (packRoot == null)
			return;

		IJavaElement[] packages = getJavaPackages(packRoot);

		if (packages == null)
			packages = new IJavaElement[0];
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), new JavaElementLabelProvider(1));
		dialog.setTitle(UIMessages.PACKAGE_SELECTION_DIALOG_TITLE);
		dialog.setMessage(UIMessages.PACKAGE_SELECTION_DIALOG_DESC);
		dialog.setEmptyListMessage(UIMessages.PACKAGE_SELECTION_DIALOG_MSG_NONE);
		dialog.setElements(packages);
		if (dialog.open() == Window.OK) {
			IPackageFragment fragment = (IPackageFragment) dialog
					.getFirstResult();
			if (fragment != null) {
				packageText.setText(fragment.getElementName());
			} else {
				packageText.setText(EMPTY_STRING);
			}
		}
	}

	/**
	 * Initialize the projectNameCombo Combo.</br> Get all the IProjects from
	 * the root resource of the workspace. Add all the valid projects to the
	 * list.
	 * 
	 * Set the list in the projectNameCombo Combo. If the wizard is opened for a
	 * project then set the project name as selected in the Combo and make the
	 * Combo disabled. Else set the 1st element of the list as the selected
	 * project.
	 */
	private void initializeProjectList() {

		IProject[] workspaceProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List<String> items = new ArrayList<String>();
		for (IProject iProject : workspaceProjects) {
			if (isProjectValid(iProject))
				items.add(iProject.getName());
		}
		if (items.isEmpty())
			return;
		String[] names = items.toArray(new String[items.size()]);

		projectNameCombo.setItems(names);
		IProject selectedProject = getSelectedProject();
		
		if (selectedProject != null && selectedProject.isAccessible()) {
			projectName=selectedProject.getName();
			projectNameCombo.setText(projectName);
			projectNameCombo.setEnabled(false);
		}

//		if (projectName == null && names.length > 0)
//			projectName = names[0];
//
//		if ((projectNameCombo.getText() == null || projectNameCombo.getText()
//				.length() == 0) && projectName != null) {
//			projectNameCombo.setText(projectName);
//		}

	}

	private boolean isProjectValid(IProject project) {
		return project.isAccessible();
	}

	/**
	 * Get the selected project or the project of the selected resource for
	 * which the wizard is opened.
	 * 
	 * 
	 * 
	 * @return IProject
	 */
	private IProject getSelectedProject() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			ISelection selection = window.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection) {
				IJavaElement element = getInitialJavaElement(selection);
				if (element != null && element.getJavaProject() != null) {
					return element.getJavaProject().getProject();
				} else if (selection instanceof TreeSelection
						&& (((TreeSelection) selection).getPaths().length > 0)) {
					TreePath path = (((TreeSelection) selection).getPaths()[0]);
					if (path.getSegmentCount() > 0
							&& path.getSegment(0) instanceof IProject) {
						return (IProject) path.getSegment(0);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get the selected project or the project of the selected resource for
	 * which the wizard is opened. Get the firstElement for the selection. Check
	 * if the firstElement is of type IJavaElement.If it is not a IJavaElement
	 * check if the firstElement is of type IResource. Get the parent resource
	 * of the IResource object.Call the getAdopter() method to get the
	 * IJavaElement object for the resource. If it still not able to find the
	 * IJavaElement,then make the resoure an IJavaElement by calling the create
	 * method of JavaCore. return IJavaElement.
	 * 
	 * @param selection
	 *            :<code>ISelection</code> - Selected element for which wizard
	 *            is opened
	 * @return <code>IJavaElement</code>
	 * 
	 */
	private IJavaElement getInitialJavaElement(ISelection selection) {
		IJavaElement jelem = null;
		if (selection != null && !selection.isEmpty()
				&& selection instanceof IStructuredSelection) {
			Object selectedElement = ((IStructuredSelection) selection)
					.getFirstElement();
			jelem = getJavaElement(selectedElement);
			if (jelem == null) {
				IResource resource = getResource(selectedElement);
				if (resource != null && resource.getType() != IResource.ROOT) {
					while (jelem == null
							&& resource.getType() != IResource.PROJECT) {
						resource = resource.getParent();
						jelem = (IJavaElement) resource
								.getAdapter(IJavaElement.class);
					}
					if (jelem == null) {
						jelem = JavaCore.create(resource);
					}
				}
			}
		}
		if (jelem == null) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window == null){
				return null;
			}
		}

		if (jelem == null || jelem.getElementType() == IJavaElement.JAVA_MODEL) {
			try {
				IJavaProject[] projects = JavaCore.create(getWorkspaceRoot()).getJavaProjects();
				if (projects.length == 1) {
					jelem = projects[0];
				}
			} 
			catch (JavaModelException e) {
				TapestryUI.logError(UIErrorMessages.EXCEPTION_WHILE_CREATING_THE_JAVA_PROJECT, e);
			}
		}
		return jelem;
	}

	/**
	 * Returns IJavaElement if the obj is of type IJavaElement or IAdaptable.
	 * 
	 * Returns an object which is an instance of the given class associated with
	 * the given object. Returns null if no such object can be found.
	 * 
	 * @param obj
	 *            :<code>Object</code> - firstElement of the selected element
	 * @return <code>IJavaElement</code>
	 * 
	 */
	private IJavaElement getJavaElement(Object obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof IJavaElement) {
			return (IJavaElement) obj;
		}
		if (obj instanceof IAdaptable) {
			return (IJavaElement) ((IAdaptable) obj)
					.getAdapter(IJavaElement.class);
		}
		return (IJavaElement) Platform.getAdapterManager().getAdapter(obj,
				IJavaElement.class);
	}

	/**
	 * Returns IJavaElement if the obj is of type IJavaElement or IAdaptable.
	 * 
	 * Returns an object which is an instance of IResource class associated with
	 * the given firstElement of the selected element. Returns null if no such
	 * object can be found.
	 * 
	 * @param obj
	 *            :<code>Object</code> - firstElement of the selected element
	 * @return <code>IResource</code>
	 * 
	 */
	private IResource getResource(Object obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof IResource) {
			return (IResource) obj;
		}

		if (obj instanceof IAdaptable) {
			return (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
		}
		return (IResource) Platform.getAdapterManager().getAdapter(obj,
				IResource.class);
	}

	/**
	 * Initialize the java folder combo list with all the java source folder
	 * names for the selected project.
	 * 
	 */
	private String[] initializeFolderList() {
		IJavaProject iJavaProject = null;
		IProject iProject = getSelectedProject();
		if (iProject != null) {
			if (iProject.isOpen() && JavaProject.hasJavaNature(iProject)) {
				iJavaProject = JavaCore.create(iProject);
			}
		} else {
			iProject = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectNameCombo.getText());
			if (iProject != null && iProject.isOpen()
					&& JavaProject.hasJavaNature(iProject)) {
				iJavaProject = JavaCore.create(iProject);
			}
		}
		List<String> items = new ArrayList<String>();
		if (iJavaProject != null) {
			try {
				IPackageFragmentRoot[] iPackageFragmentRoots = iJavaProject.getPackageFragmentRoots();
				for (IPackageFragmentRoot fragmentRoot : iPackageFragmentRoots) {
					if (fragmentRoot.getClass().isAssignableFrom(
							JarPackageFragmentRoot.class)) {
						if (fragmentRoot.getCorrespondingResource() != null) {
							items.add(fragmentRoot.getCorrespondingResource()
									.getProjectRelativePath().toString());
						}
					}
				}
			} catch (JavaModelException javaModelException) {

			}
		}
		String[] listOfFolders = items.toArray(new String[items.size()]);
		return listOfFolders;
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * Append .components to the default package name of the project and set it
	 * to package text.
	 * 
	 * @param tapestryPackage
	 *            : <code>java.lang.String</code> - Default tapestry package
	 *            name provided in the web.xml file
	 */
	private void setDefaultPackage(String tapestryPackage) {
		if (tapestryPackage != null) {
			tapestryPackage = tapestryPackage + "." + getSubPackage();
		}
		else{
			tapestryPackage = getSubPackage();
		}
		model.put(getPackageKey(), tapestryPackage);
		packageText.setText(tapestryPackage);
	}

	/**
	 * Validate the package name for the component. Set the error message if
	 * package name does not start with the default package name provided in the
	 * web.xml.
	 * 
	 * @param packageName
	 *            : <code>java.lang.String</code> - package Name to be validate.
	 * 
	 */
	private void validateRootPackageName(String packageName) {
		Object rootPackage = model.get(getPackageKey());
		if (packageName == null
				|| (rootPackage != null &&!packageName.startsWith((String) rootPackage) )) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(UIMessages.PACKAGE_VALIDATION_MESSAGE);
			stringBuilder.append((String) model.get(getPackageKey()));
			packageFieldStatus = new Status(IStatus.ERROR, JavaCore.PLUGIN_ID,
					-1, stringBuilder.toString(), null);
			StatusUtil.applyToStatusLine(AbstractTapestryWizardPage.this,
					packageFieldStatus);
			setPageComplete(false);
		}
	}

	/**
	 * @param packRoot
	 * @return
	 */
	private IJavaElement[] getJavaPackages(IPackageFragmentRoot packRoot) {
		IJavaElement[] packages = null;
		List<IJavaElement> javaElements = new ArrayList<IJavaElement>();
		try {
			String defaultPackage = (String) model.get(getPackageKey());
			for (IJavaElement iJavaElement : packRoot.getChildren()) {
				if(defaultPackage == null){
					javaElements.add(iJavaElement);
				}
				else if (iJavaElement.getElementName().contains(defaultPackage)) {
					javaElements.add(iJavaElement);
				}
			}
			packages = javaElements.toArray(new IJavaElement[javaElements.size()]);
		} 
		catch (JavaModelException e) {
			TapestryUI.logError(UIErrorMessages.EXCEPTION_WHILE_LOADING_PACKAGES, e);
		}
		return packages;
	}

	// --------------------------------------------------------------------------------------------------------------
	//
	// ABSTRACT GETTERS FOR SPECIFIC VALUES:
	//
	// --------------------------------------------------------------------------------------------------------------

	protected abstract String getClassNameLabel();

	protected abstract String getContextType();

	protected abstract String getSubPackage();

	protected abstract String getPackageKey();

}