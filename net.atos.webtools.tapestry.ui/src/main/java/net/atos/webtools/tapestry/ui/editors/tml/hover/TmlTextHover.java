package net.atos.webtools.tapestry.ui.editors.tml.hover;

import static net.atos.webtools.tapestry.core.util.Constants.MIXINS;
import static net.atos.webtools.tapestry.core.util.Constants.TYPE;

import java.util.List;

import net.atos.webtools.tapestry.core.models.EditedFeatureModel;
import net.atos.webtools.tapestry.core.models.JavaElement;
import net.atos.webtools.tapestry.core.models.features.ComponentModel;
import net.atos.webtools.tapestry.core.models.features.MixinModel;
import net.atos.webtools.tapestry.core.models.features.PageModel;
import net.atos.webtools.tapestry.core.models.features.Parameter;
import net.atos.webtools.tapestry.core.util.Constants;
import net.atos.webtools.tapestry.core.util.helpers.TmlHelper;
import net.atos.webtools.tapestry.ui.editors.multi.TapestryMultiPageEditor;
import net.atos.webtools.tapestry.ui.util.helpers.EclipseHelper;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.taginfo.XMLTagInfoHoverProcessor;

/**
 * Manages the hover behavior in tml files: for the moment, we show some javadoc on component 
 * and component attibutes
 * 
 * @author a160420
 *
 */
@SuppressWarnings("restriction")
public class TmlTextHover extends XMLTagInfoHoverProcessor {
	private EditedFeatureModel tapestryFeatureModel;
	private String t;
	private int offset;

	/**
	 * initialization: we get current editor & set it's {@link EditedFeatureModel} 
	 */
	private void init(){
		IEditorPart currentEditor = EclipseHelper.getCurrentEditor();
		if(currentEditor instanceof TapestryMultiPageEditor){
			tapestryFeatureModel = ((TapestryMultiPageEditor) currentEditor).getTapestryFeatureModel();
		}
		
		if(tapestryFeatureModel != null){
			t = tapestryFeatureModel.getNamespace();
			tapestryFeatureModel.initJavaFields();
		}
	}

	@Override
	protected String computeTagNameHelp(IDOMNode xmlnode, IDOMNode parentNode, IStructuredDocumentRegion flatNode,
			ITextRegion region) {
		init();
		
		if(tapestryFeatureModel != null){
			String tapestryType = TmlHelper.getComponentFullName(xmlnode, t);
			ComponentModel component = tapestryFeatureModel.getProjectModel().getComponent(tapestryType);
			if(component != null){
				return component.getJavadoc();
			}
		}
		
		return super.computeTagNameHelp(xmlnode, parentNode, flatNode, region);
	}

	@Override
	protected String computeTagAttNameHelp(IDOMNode xmlnode, IDOMNode parentNode, IStructuredDocumentRegion flatNode,
			ITextRegion region) {
		init();
		
		if(tapestryFeatureModel != null && tapestryFeatureModel.getProjectModel() != null){
			String attributeName = flatNode.getFullText(region);
			
			String tapestryType = TmlHelper.getComponentFullName(xmlnode, t);
			ComponentModel component = tapestryFeatureModel.getProjectModel().getComponent(tapestryType);
			List<String> mixinTypes = TmlHelper.getMixinTypes(xmlnode, t);
			
			if(component != null && attributeName != null){
				if(attributeName.equalsIgnoreCase(t + ":" + TYPE)){
					return component.getJavadoc();
				}
				else if(attributeName.equalsIgnoreCase(t + ":" + MIXINS)){
					//t:mixins definition...
					if(mixinTypes != null && mixinTypes.size() > 0){
						//if there's ionly one => show the mixin javadoc
						if(mixinTypes.size() == 1){
							MixinModel mixin = tapestryFeatureModel.getProjectModel().getMixin(mixinTypes.get(0));
							return mixin.getJavadoc();
						}
						//if there's > 1 => show the mixins names
						else {
							return mixinTypes.toString();
						}
					}
				}
				//attribute = Parameter from the component
				else{
					for(Parameter param : component.getParameters()){
						if(param.getQualifiedName(t).equalsIgnoreCase(attributeName)){
							return param.getJavadoc();
						}
					}
				}
			}
			//attribute = Parameter of an eventual mixin
			for(String mixinType : mixinTypes){
				MixinModel mixin = tapestryFeatureModel.getProjectModel().getMixin(mixinType);
				for(Parameter param : mixin.getParameters()){
					if(param.getQualifiedName(t).equalsIgnoreCase(attributeName)){
						return param.getJavadoc();
					}
				}
			}
		}
		
		return super.computeTagAttNameHelp(xmlnode, parentNode, flatNode, region);
	}
	
	@Override
	protected String computeTagAttValueHelp(IDOMNode xmlnode, IDOMNode parentNode, IStructuredDocumentRegion flatNode,
			ITextRegion region) {
		init();
		
		if(tapestryFeatureModel != null){
			//------------ CASE-1 java prop -----------------------
			String attributeValue = flatNode.getText(region).replace("\"", "");
			for(JavaElement prop : tapestryFeatureModel.getJavaProperties()){
				if(attributeValue.contains(prop.getName())){
					return prop.getJavadoc();
				}
			}
			
			//------------------ CASE-2: -----------------------
			String wholeDocument = flatNode.getParentDocument().get();
			int index = flatNode.getStart() + region.getStart();
			String attributeName = TmlHelper.getAttributeBefore(wholeDocument, index);
			
			String tapestryType = TmlHelper.getComponentFullName(xmlnode, t);
			ComponentModel component = tapestryFeatureModel.getProjectModel().getComponent(tapestryType);
			if(component != null){
				if((t + ":" + Constants.PAGE_ATTRIBUTE).equalsIgnoreCase(attributeName)){
					PageModel page = tapestryFeatureModel.getProjectModel().getPage(attributeValue);
					if(page != null){
						return page.getJavadoc();
					}
				}
				else if((t + ":" + Constants.MIXINS).equalsIgnoreCase(attributeName)){
					String tagString = xmlnode.getSource();
					for(MixinModel mixin : tapestryFeatureModel.getProjectModel().getMixins()){
						int startIndex = tagString.indexOf(mixin.getFullName());
						if(startIndex >= 0){
							int endIndex = startIndex + mixin.getFullName().length();
							if((offset> flatNode.getStart() + startIndex) && (offset < flatNode.getStart() + endIndex)){
								return mixin.getJavadoc();
							}
						}
					}
				}
				if(! (t + ":" + TYPE).equalsIgnoreCase(attributeName)){
					for(Parameter param : component.getParameters()){
						if(param.getQualifiedName(t).equalsIgnoreCase(attributeName)){
							return param.getJavadoc();
						}
					}
				}
				return component.getJavadoc();
			}
		}		
		
		return super.computeTagAttValueHelp(xmlnode, parentNode, flatNode, region);
	}
	
	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		this.offset = offset;
		return super.getHoverRegion(textViewer, offset);
	}
	
	@Deprecated
	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset, boolean enabled) {
		this.offset = offset;
		return super.getHoverRegion(textViewer, offset, enabled);
	}
}
