package net.atos.webtools.tapestry.ui.wizards;


import net.atos.webtools.tapestry.core.models.FeatureType;
import net.atos.webtools.tapestry.core.util.Constants;
import net.atos.webtools.tapestry.ui.util.UIConstants;
import net.atos.webtools.tapestry.ui.util.UIMessages;

import org.eclipse.jface.viewers.ISelection;

/**
 * Tapestry component creation wizard page
 * 
 */
public class NewTapestryPageClassWizard extends AbstractTapestryWizardPage {
	/**
	 * Constructor
	 * 
	 * @param selection: the current selection (project...)
	 */
	public NewTapestryPageClassWizard(ISelection selection) {
		super("wizardPage");
		setTitle(UIMessages.NEW_PAGE_TITLE);
		setDescription(UIMessages.NEW_PAGE_DESC);
	}

	@Override
	protected String getClassNameLabel() {
		return UIMessages.PAGE_NAME_LABEL;
	}

	@Override
	protected String getContextType() {
		return Constants.PAGE_CONTEXT_TYPE;
	}

	@Override
	protected String getSubPackage() {
		return FeatureType.PAGE.getSubPackage();
	}

	@Override
	protected String getPackageKey() {
		return UIConstants.DEFAULT_PAGES_PACKAGE_KEY;
	}
}