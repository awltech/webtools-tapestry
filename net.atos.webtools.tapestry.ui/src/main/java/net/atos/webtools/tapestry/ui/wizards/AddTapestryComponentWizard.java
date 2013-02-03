package net.atos.webtools.tapestry.ui.wizards;




/**
 * Tapestry component creation wizard
 * 
 * @author a154438
 *
 */
public class AddTapestryComponentWizard extends AbstractTapestryWizard {
	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		page = new NewTapestryComponentPage(selection);
		addPage(page);
	}
}
