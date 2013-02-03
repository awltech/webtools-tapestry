/**
 * 
 */
package net.atos.webtools.tapestry.ui.editors.tml.hyperlinks;

import static net.atos.webtools.tapestry.core.util.Constants.MIXINS;
import static net.atos.webtools.tapestry.core.util.Constants.PAGE_ATTRIBUTE;

import java.text.StringCharacterIterator;

import net.atos.webtools.tapestry.core.models.ProjectModel;
import net.atos.webtools.tapestry.core.models.features.ComponentModel;
import net.atos.webtools.tapestry.core.models.features.MixinModel;
import net.atos.webtools.tapestry.core.models.features.PageModel;
import net.atos.webtools.tapestry.core.util.helpers.TmlHelper;
import net.atos.webtools.tapestry.ui.editors.multi.TapestryMultiPageEditor;
import net.atos.webtools.tapestry.ui.util.helpers.EclipseHelper;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;

/**
 * Manages hyperlink navigation from components to components or pages, mixins...
 * 
 * @author a160420
 *
 */
public class ComponentHyperlinkDetector extends AbstractHyperlinkDetector {

	/**
	 * 
	 */
	public ComponentHyperlinkDetector() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion, boolean)
	 */
	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		
		//If we're in Tapestry editor:
		IEditorPart currentEditor = EclipseHelper.getCurrentEditor();
		if(currentEditor instanceof TapestryMultiPageEditor){
			TapestryMultiPageEditor tapestryEditor = (TapestryMultiPageEditor) currentEditor;
			ProjectModel projectModel = tapestryEditor.getTapestryFeatureModel().getProjectModel();
			String t = tapestryEditor.getTapestryFeatureModel().getNamespace();
			
			if(projectModel != null){
				//----------------- 1: Inside a special parameter (t:mixins, t: pages?) ----------------------------------
				
				IRegion attributeValueRegion = getAttributeValueRegion(textViewer.getDocument(), region.getOffset());
				if(attributeValueRegion != null){
					String paramString = null;
					try {
						paramString = textViewer.getDocument().get(attributeValueRegion.getOffset(), attributeValueRegion.getLength());
					}
					catch (BadLocationException e) {
					}
					if(paramString != null){
						String attributeName = getAttributeNameBefore(textViewer.getDocument(), region.getOffset());
						if(attributeName.equalsIgnoreCase(t + ":"+ MIXINS)){
							MixinModel mixin = projectModel.getMixin(paramString);
							if(mixin != null && mixin.getInputElement() != null){
								return new Hyperlink[]{new Hyperlink(attributeValueRegion, mixin.getInputElement())};
							}
						}
						else if(attributeName.equalsIgnoreCase(t + ":"+ PAGE_ATTRIBUTE)){
							PageModel page = projectModel.getPage(paramString);
							projectModel.getPages();
							if(page != null && page.getInputElement() != null){
								return new Hyperlink[]{new Hyperlink(attributeValueRegion, page.getInputElement())};
							}
						}
					}
				}
				
				//---------------------------------------- 2: Inside a tag --------------------------------------------
				IRegion tagRegion = getTagRegion(textViewer.getDocument(), region.getOffset());
				
				//If we hover a tag:
				if(tagRegion != null){
					//Extract the tag (region between '<' and '>'):
					String tagString;
					try {
						tagString = textViewer.getDocument().get(tagRegion.getOffset(), tagRegion.getLength());
					}
					catch (BadLocationException e) {
						return null;
					}
					//Extract component full name
					String componentFullName = TmlHelper.getComponentFullName(tagString);
					if(componentFullName != null){
						ComponentModel component = projectModel.getComponent(componentFullName);
						if(component != null && component.getInputElement() != null){
							return new Hyperlink[]{new Hyperlink(tagRegion, component.getInputElement())};
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Extract the current tag (around the specified offset): it must be preceded by a '&lt;'
	 * and followed by a 'gt;' (and not the opposite)
	 * 
	 * @param document the doc to scan
	 * @param offset the position in the text around which the search will start
	 * @return the {@link Region} of the following String: &lt;t:comp t:attr="xxx" id="yyy" ... &gt;
	 */
	private IRegion getTagRegion(IDocument document, int offset){
		
		StringCharacterIterator characterIterator = new StringCharacterIterator(document.get(), offset);
		int beginIndex = offset;
		int endIndex = offset;
		
		char cur = characterIterator.current();
		while(cur != StringCharacterIterator.DONE && cur != '<' && cur != '>'){
			if(cur == '>' ){
				return null;
			}
			beginIndex --;
			cur = characterIterator.previous();
		}
		
		characterIterator.setIndex(offset);
		cur = characterIterator.next();
		endIndex ++;
		while(cur != StringCharacterIterator.DONE && cur != '>' && cur != '<'){
			if(cur == '<' ){
				return null;
			}
			endIndex ++;
			cur = characterIterator.next();
		}
		endIndex ++;
		
		return new Region(beginIndex, endIndex - beginIndex);
	}
	
	private IRegion getAttributeValueRegion(IDocument document, int offset){
		
		StringCharacterIterator characterIterator = new StringCharacterIterator(document.get(), offset);
		int beginIndex = offset;
		int endIndex = offset;
		
		char cur = characterIterator.current();
		while(cur != StringCharacterIterator.DONE && cur != '"' && cur != ','){
			if(cur == '>' || cur == '<'){
				return null;
			}
			beginIndex --;
			cur = characterIterator.previous();
		}
		beginIndex ++;
		
		characterIterator.setIndex(offset);
		cur = characterIterator.next();
		endIndex ++;
		while(cur != StringCharacterIterator.DONE && cur != '"' && cur != ','){
			if(cur == '>' || cur == '<'){
				return null;
			}
			endIndex ++;
			cur = characterIterator.next();
		}
		
		return new Region(beginIndex, endIndex - beginIndex);
	}
	
	private String getAttributeNameBefore(IDocument document, int offset){
		StringCharacterIterator characterIterator = new StringCharacterIterator(document.get(), offset);
		char cur = characterIterator.current();
		
		//Skip attribute value characters
		while(cur != StringCharacterIterator.DONE && cur != '"' ){
			cur = characterIterator.previous();
		}
		//Skip spaces, '=', '"'
		while(cur != StringCharacterIterator.DONE 
				&& (cur == '"' || cur == '=' || Character.isWhitespace(cur) )){
			cur = characterIterator.previous();
		}
		
		StringBuilder sb = new StringBuilder();
		while(cur != StringCharacterIterator.DONE && ! Character.isWhitespace(cur)){
			sb.insert(0, cur);
			cur = characterIterator.previous();
		}
		
		return sb.toString();
	}
}
