package net.atos.webtools.tapestry.ui.wizards;


import net.atos.webtools.tapestry.core.models.FeatureType;
import net.atos.webtools.tapestry.core.util.Constants;
import net.atos.webtools.tapestry.ui.util.UIConstants;
import net.atos.webtools.tapestry.ui.util.UIMessages;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Tapestry component creation wizard page
 * 
 */
public class NewTapestryComponentPage extends AbstractTapestryWizardPage {
	protected Button createTemplate;

	/**
	 * Constructor
	 * 
	 * @param selection: the current selection (project...)
	 */
	public NewTapestryComponentPage(ISelection selection) {
		super("wizardPage");
		setTitle(UIMessages.NEW_COMPONENT_TITLE);
		setDescription(UIMessages.NEW_COMPONENT_DESC);
	}

	/**
	 * In Tapestry it is possible to create components without templates. Please
	 * provide an additional checkbox, labeled with "Create template" which is
	 * checked by default.
	 */
	@Override
	protected void addCreateTemplate(Composite composite) {
		Label templateLabel = new Label(composite, SWT.LEFT);
		templateLabel.setText(UIMessages.CREATE_TML_LABEL);
		templateLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		createTemplate = new Button(composite, SWT.CHECK);
		createTemplate.setToolTipText(UIMessages.CREATE_TML_TOOLTIP);
		createTemplate.setSelection(true);
		createTemplate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createTemplate.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!createTemplate.getSelection()) {
					templateNameCombo.setEnabled(false);
				} else {
					templateNameCombo.setEnabled(true);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		new Label(composite, SWT.NONE);
	}
	
	public boolean isCreatingTemplate(){
		if(createTemplate != null){
			return createTemplate.getSelection();
		}
		return super.isCreatingTemplate();
	}

	/**
	 * @return String:COMPONENT_NAME_LABEL
	 */
	@Override
	protected String getClassNameLabel() {
		return UIMessages.COMPONENT_NAME_LABEL;
	}

	@Override
	protected String getContextType() {
		return Constants.COMPONENT_CONTEXT_TYPE;
	}

	@Override
	protected String getSubPackage() {
		return FeatureType.COMPONENT.getSubPackage();
	}

	@Override
	protected String getPackageKey() {
		return UIConstants.DEFAULT_COMPONENT_PACKAGE_KEY;
	}

}