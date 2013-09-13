package net.atos.webtools.tapestry.ui.wizards;

/**
 * Tapestry page creation wizard
 * 
 * @author a154438
 * 
 */
public class AddTapestryPageWizard extends AbstractTapestryWizard {
	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		page = new NewTapestryPageClassWizard(selection);
		addPage(page);
	}
}
