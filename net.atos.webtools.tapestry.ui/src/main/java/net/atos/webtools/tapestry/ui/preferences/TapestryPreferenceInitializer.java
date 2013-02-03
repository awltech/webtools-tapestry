package net.atos.webtools.tapestry.ui.preferences;

import net.atos.webtools.tapestry.ui.TapestryUI;
import net.atos.webtools.tapestry.ui.util.UIConstants;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
* Sets default values for Tapestry preferences
*/
public class TapestryPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = TapestryUI.getDefault().getPreferenceStore();
		
		store.setDefault(TapestryPreferenceNames.AUTO_PROPOSE, false);
		store.setDefault(TapestryPreferenceNames.AUTO_PROPOSE_CODE, "<");//$NON-NLS-1$
		store.setDefault(TapestryPreferenceNames.AUTO_PROPOSE_DELAY, 500);
		
		// Defaults for Content Assist preference page
		store.setDefault(TapestryPreferenceNames.CONTENT_ASSIST_DO_NOT_DISPLAY_ON_DEFAULT_PAGE, "");
		store.setDefault(TapestryPreferenceNames.CONTENT_ASSIST_DO_NOT_DISPLAY_ON_OWN_PAGE, UIConstants.PARAM_PROPOSAL_CATEGORY);
		
		store.setDefault(TapestryPreferenceNames.CONTENT_ASSIST_DEFAULT_PAGE_SORT_ORDER,
								UIConstants.PARAM_PROPOSAL_CATEGORY + "\0" +
								UIConstants.TAGS_PROPOSAL_CATEGORY + "\0" +
								UIConstants.PE_PROPOSAL_CATEGORY + "\0" +
								UIConstants.TEMPLATES_PROPOSAL_CATEGORY + "\0" +
								"org.eclipse.wst.html.ui.proposalCategory.htmlTags\0" +
								"org.eclipse.wst.css.ui.proposalCategory.css\0" +
								"org.eclipse.wst.html.ui.proposalCategory.htmlTemplates\0" +
								"org.eclipse.wst.css.ui.proposalCategory.cssTemplates");
		
		store.setDefault(TapestryPreferenceNames.CONTENT_ASSIST_OWN_PAGE_SORT_ORDER,
								UIConstants.PARAM_PROPOSAL_CATEGORY + "\0" +
								UIConstants.PE_PROPOSAL_CATEGORY + "\0" +
								UIConstants.TAGS_PROPOSAL_CATEGORY + "\0" +
								UIConstants.TEMPLATES_PROPOSAL_CATEGORY + "\0" +
								"org.eclipse.wst.html.ui.proposalCategory.htmlTemplates\0"+
								"org.eclipse.wst.css.ui.proposalCategory.cssTemplates\0" +
								"org.eclipse.wst.html.ui.proposalCategory.htmlTags\0" +
								"org.eclipse.wst.css.ui.proposalCategory.css");
	}
}

