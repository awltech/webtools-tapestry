package net.atos.webtools.tapestry.ui.preferences;

/**
 * Preference keys for Tapestry plugin
 */
public class TapestryPreferenceNames {
	/**
	 * A named preference that controls if code assist gets auto activated.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String AUTO_PROPOSE = "autoPropose";
	
	/**
	 * A named preference that holds the characters that auto activate code
	 * assist.
	 * <p>
	 * Value is of type <code>String</code>. All characters that trigger
	 * auto code assist.
	 * </p>
	 */
	public static final String AUTO_PROPOSE_CODE = "autoProposeCode";
	
	/**
	 * A named preference that controls time before code assist gets auto activated.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 */
	public static final String AUTO_PROPOSE_DELAY = "autoProposeDelay";

	public static final String CONTENT_ASSIST_DO_NOT_DISPLAY_ON_DEFAULT_PAGE = "html_content_assist_display_on_default_page";

	public static final String CONTENT_ASSIST_DO_NOT_DISPLAY_ON_OWN_PAGE = "html_content_assist_display_on_own_page";

	public static final String CONTENT_ASSIST_OWN_PAGE_SORT_ORDER = "html_content_assist_own_page_sort_order";

	public static final String CONTENT_ASSIST_DEFAULT_PAGE_SORT_ORDER = "html_content_assist_default_page_sort_order";

}
