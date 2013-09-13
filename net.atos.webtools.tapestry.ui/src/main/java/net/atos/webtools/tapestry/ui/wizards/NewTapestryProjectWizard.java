package net.atos.webtools.tapestry.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class NewTapestryProjectWizard extends WizardPage {

	public NewTapestryProjectWizard() {
		super("test");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		
		setControl(container);
	}

}
