package net.atos.webtools.tapestry.ui.preferences;

import net.atos.webtools.tapestry.core.TapestryCore;
import net.atos.webtools.tapestry.ui.TapestryUI;
import net.atos.webtools.tapestry.ui.util.UIConstants;
import net.atos.webtools.tapestry.ui.util.UIErrorMessages;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.html.ui.StructuredTextViewerConfigurationHTML;
import org.eclipse.wst.html.ui.internal.preferences.ui.HTMLTemplatePreferencePage;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.StructuredTextViewerConfiguration;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.sse.ui.internal.provisional.style.LineStyleProvider;
import org.osgi.service.prefs.BackingStoreException;


/**
 * <p>Preference page for Tml templates
 * 
 * <p>Derived from {@link HTMLTemplatePreferencePage}
 * 
 * @see HTMLTemplatePreferencePage
 */
@SuppressWarnings("restriction")
public class TapestryTemplatePreferencePage extends org.eclipse.ui.texteditor.templates.TemplatePreferencePage {

	class TapestryEditTemplateDialog extends EditTemplateDialog {

		public TapestryEditTemplateDialog(Shell parent, Template template, boolean edit, boolean isNameModifiable,
				ContextTypeRegistry registry) {
			super(parent, template, edit, isNameModifiable, registry);
		}

		protected SourceViewer createViewer(Composite parent) {
			SourceViewerConfiguration sourceViewerConfiguration = new StructuredTextViewerConfiguration() {

				StructuredTextViewerConfiguration baseConfiguration = new StructuredTextViewerConfigurationHTML();

				public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
					return baseConfiguration.getConfiguredContentTypes(sourceViewer);
				}

				public LineStyleProvider[] getLineStyleProviders(ISourceViewer sourceViewer, String partitionType) {
					return baseConfiguration.getLineStyleProviders(sourceViewer, partitionType);
				}

				public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
					ContentAssistant assistant = new ContentAssistant();
					assistant.enableAutoActivation(true);
					assistant.enableAutoInsert(true);
					assistant.setContentAssistProcessor(getTemplateProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
					return assistant;
				}
			};
			return doCreateViewer(parent, sourceViewerConfiguration);
		}
	}

	public TapestryTemplatePreferencePage() {
		TapestryCore htmlEditorPlugin = TapestryCore.getDefault();

		setPreferenceStore(htmlEditorPlugin.getPreferenceStore());
		setTemplateStore(htmlEditorPlugin.getTmlTemplateStore());
		setContextTypeRegistry(htmlEditorPlugin.getCodeTemplateContextRegistry());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean ok = super.performOk();
		try {
			InstanceScope.INSTANCE.getNode(TapestryCore.PLUGIN_ID).flush();
		}
		catch (BackingStoreException e) {
			TapestryUI.logError(UIErrorMessages.NOT_ABLE_TO_PERSIST_USER_PREFERENCES, e);
		}
		return ok;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#
	 * isShowFormatterSetting()
	 */
	protected boolean isShowFormatterSetting() {
		// template formatting has not been implemented
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	protected Control createContents(Composite ancestor) {
		Control c = super.createContents(ancestor);
		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createViewer
	 * (org.eclipse.swt.widgets.Composite)
	 */
	protected SourceViewer createViewer(Composite parent) {
		SourceViewerConfiguration sourceViewerConfiguration = new StructuredTextViewerConfiguration() {

			StructuredTextViewerConfiguration baseConfiguration = new StructuredTextViewerConfigurationHTML();

			public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
				return baseConfiguration.getConfiguredContentTypes(sourceViewer);
			}

			public LineStyleProvider[] getLineStyleProviders(ISourceViewer sourceViewer, String partitionType) {
				return baseConfiguration.getLineStyleProviders(sourceViewer, partitionType);
			}
		};
		return doCreateViewer(parent, sourceViewerConfiguration);
	}

	SourceViewer doCreateViewer(Composite parent, SourceViewerConfiguration viewerConfiguration) {
		SourceViewer viewer = null;
		String contentTypeID = UIConstants.TML_CONTENT_TYPE;
		viewer = new StructuredTextViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.getTextWidget().setFont(JFaceResources.getFont("org.eclipse.wst.sse.ui.textfont")); //$NON-NLS-1$
		IStructuredModel scratchModel = StructuredModelManager.getModelManager().createUnManagedStructuredModelFor(
				contentTypeID);
		IDocument document = scratchModel.getStructuredDocument();
		viewer.configure(viewerConfiguration);
		viewer.setDocument(document);
		return viewer;
	}

	/**
	 * Creates the edit dialog. Subclasses may override this method to provide a
	 * custom dialog.
	 * 
	 * @param template
	 *            the template being edited
	 * @param edit
	 *            whether the dialog should be editable
	 * @param isNameModifiable
	 *            whether the template name may be modified
	 * @return the created or modified template, or <code>null</code> if the
	 *         edition failed
	 * @since 3.1
	 */
	protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable) {
		EditTemplateDialog dialog = new TapestryEditTemplateDialog(getShell(), template, edit, isNameModifiable,
				getContextTypeRegistry());
		if (dialog.open() == Window.OK) {
			return dialog.getTemplate();
		}
		return null;
	}
}
