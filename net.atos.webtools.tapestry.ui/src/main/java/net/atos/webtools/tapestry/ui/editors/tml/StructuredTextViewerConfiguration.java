package net.atos.webtools.tapestry.ui.editors.tml;

import java.util.Map;

import net.atos.webtools.tapestry.ui.editors.tml.hover.TmlTextHover;
import net.atos.webtools.tapestry.ui.util.UIConstants;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.wst.html.ui.StructuredTextViewerConfigurationHTML;
import org.eclipse.wst.sse.ui.internal.contentassist.StructuredContentAssistant;

/**
 * Configuration class for the embedded tml editor
 * 
 * @author a160420
 *
 */
@SuppressWarnings("restriction")
public class StructuredTextViewerConfiguration extends StructuredTextViewerConfigurationHTML {
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		IContentAssistant assistant = super.getContentAssistant(sourceViewer);
		
		if(assistant instanceof StructuredContentAssistant){
			IInformationControlCreator informationControlCreator = getInformationControlCreator(sourceViewer);

			//some customization on informationControlCreator could be done here
			
			((StructuredContentAssistant)assistant).setInformationControlCreator(informationControlCreator);
		}
		
		return assistant;
	}
	
	public ITextHover getTextHover(ISourceViewer sv, String contentType) {
		return new TmlTextHover();
	}
	
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		return new TmlTextHover();
	}
	
	/**
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
		Map detectorTargets = super.getHyperlinkDetectorTargets(sourceViewer);
		detectorTargets.put(UIConstants.HYPERLINK_DETECTOR_TARGET, null);
				
		return detectorTargets;
	}

}
