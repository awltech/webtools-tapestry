package net.atos.webtools.tapestry.ui.editors.tml.assist;
import net.atos.webtools.tapestry.ui.TapestryUI;
import net.atos.webtools.tapestry.ui.preferences.TapestryPreferenceNames;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wst.sse.ui.preferences.AbstractCompletionProposalCategoriesConfiguration;


/**
 * This class is called through the following  extension point only:
 * <code> org.eclipse.wst.sse.ui.completionProposalCategoriesConfiguration</code>
 * only on the tml content type.
 * 
 * It allows to order/filter proposals by category
 * This is hard-coded for the moment, but could be related to some GUI, to let the user choose 
 * (as it's done in other editors)
 * 
 * @author a160420
 *
 */
public class ProposalCategoriesConfig extends AbstractCompletionProposalCategoriesConfiguration {

	@Override
	public boolean hasAssociatedPropertiesPage() {
		return true;
	}

	public String getPropertiesPageID() {
		return "net.atos.webtools.tapestry.preferences.ContentAssistPreferencePage";
	}

	protected IPreferenceStore getPreferenceStore() {
		return TapestryUI.getDefault().getPreferenceStore();
	}

	/**
	 * @see org.eclipse.wst.sse.ui.preferences.AbstractCompletionProposalCategoriesConfiguration#getShouldNotDisplayOnDefaultPagePrefKey()
	 */
	protected String getShouldNotDisplayOnDefaultPagePrefKey() {
		return TapestryPreferenceNames.CONTENT_ASSIST_DO_NOT_DISPLAY_ON_DEFAULT_PAGE;
	}

	/**
	 * @see org.eclipse.wst.sse.ui.preferences.AbstractCompletionProposalCategoriesConfiguration#getShouldNotDisplayOnOwnPagePrefKey()
	 */
	protected String getShouldNotDisplayOnOwnPagePrefKey() {
		return TapestryPreferenceNames.CONTENT_ASSIST_DO_NOT_DISPLAY_ON_OWN_PAGE;
	}

	/**
	 * @see org.eclipse.wst.sse.ui.preferences.AbstractCompletionProposalCategoriesConfiguration#getPageSortOrderPrefKey()
	 */
	protected String getPageSortOrderPrefKey() {
		return TapestryPreferenceNames.CONTENT_ASSIST_OWN_PAGE_SORT_ORDER;
	}
	
	/**
	 * @see org.eclipse.wst.sse.ui.preferences.AbstractCompletionProposalCategoriesConfiguration#getDefaultPageSortOrderPrefKey()
	 */
	protected String getDefaultPageSortOrderPrefKey() {
		return TapestryPreferenceNames.CONTENT_ASSIST_DEFAULT_PAGE_SORT_ORDER;
	}
}
