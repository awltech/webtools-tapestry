package net.atos.webtools.tapestry.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class AddTapestryProjectWizard extends Wizard implements INewWizard {
	
	
	@Override
	public void addPages() {
		NewTapestryProjectWizard page = new NewTapestryProjectWizard();
		addPage(page);
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		
		
	}
	
	@Override
	public boolean performFinish() {
		return true;
	}
}
