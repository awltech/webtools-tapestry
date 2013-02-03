package net.atos.webtools.tapestry.ui.editors.tml.assist;

import static net.atos.webtools.tapestry.core.util.Constants.ID;
import static net.atos.webtools.tapestry.core.util.Constants.MIXINS;
import static net.atos.webtools.tapestry.core.util.Constants.TYPE;

import java.util.ArrayList;
import java.util.List;

import net.atos.webtools.tapestry.core.models.features.AbstractFeatureModel;
import net.atos.webtools.tapestry.core.models.features.AbstractParameteredFeatureModel;
import net.atos.webtools.tapestry.core.models.features.ComponentModel;
import net.atos.webtools.tapestry.core.models.features.MixinModel;
import net.atos.webtools.tapestry.core.models.features.PageModel;
import net.atos.webtools.tapestry.core.models.features.Parameter;
import net.atos.webtools.tapestry.core.util.Constants;
import net.atos.webtools.tapestry.core.util.helpers.TmlHelper;
import net.atos.webtools.tapestry.ui.util.UIMessages;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.w3c.dom.Node;

/**
 * <p>Proposes the list of Components (as new tag or tag name) for untyped component, and directly inserts the 
 * corresponding mandatory parameters.
 * 
 * <p>Also proposes the other parameters as XML attributes, when the tag is already typed.
 * 
 * <p>Also proposes values for some specific attributes (t:type, t:mixins, t:page).
 * 
 * @author a160420
 *
 */
@SuppressWarnings("restriction")
public class TagsCompletionProposalComputer extends AbstractTapestryCompletionProposalComputer {
	/*---------------------------------------------------------------------------------
	 * 
	 *   Be careful when modifying that class:
	 *   this was very hard to have all the correct indexes working in all cases! 
	 * 
	 ---------------------------------------------------------------------------------*/
	
	@Override
	protected List<ICompletionProposal> getTagProposals(ContentAssistRequest request,
			CompletionProposalInvocationContext context, boolean isTagInsertion) {
		
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		
		if(tapestryFeatureModel != null){
			
			String wholeDocument = context.getDocument().get();
			String alreadyTyped = getTypedBefore(wholeDocument, context.getInvocationOffset());
			int replacementLength = getAttributeReplacementLength(wholeDocument, context.getInvocationOffset()) + alreadyTyped.length();
			int offset = context.getInvocationOffset() - alreadyTyped.length();
			
			
			for(ComponentModel componentModel : tapestryFeatureModel.getProjectModel().getComponents()){
				String compName = componentModel.getQualifiedName(t);
				StringBuilder toBeInsertedSB = new StringBuilder(35); 
				
				if(isTagInsertion){
					toBeInsertedSB.append('<');
				}
				
				toBeInsertedSB.append(compName);
				
				for(Parameter requiredParam : componentModel.getMandatoryParameters()){
					toBeInsertedSB.append(" ").append(requiredParam.getQualifiedName(t)).append("=\"\""); 
				}
				
				if(isTagInsertion){
					toBeInsertedSB.append(">");
				}
				
				String toBeInserted = toBeInsertedSB.toString();
				
				if(toBeInserted.toLowerCase().startsWith(alreadyTyped.toLowerCase())) {
					proposals.add(
							new CustomCompletionProposal(toBeInserted,					//replacementString 
														offset, 						//replacementOffset
														replacementLength,				//replacementLength
														toBeInserted.length(),			//cursorPosition
														imageC, 						//imageC
														compName,						//displayString
														null, 							//contextInformation
														componentModel.getJavadoc(),	//additionalProposalInfo 
														100,							//relevance
														true));							//updateReplacementLengthOnValidate
				}
			}
		}
		
		return proposals;
	}

	/**
	 * <p>Check if a Tapestry type has been defined:<ul>
	 * 	<li> whether with &lt;t:StandardType ...
	 * 	<li> or with &lt;prefix:MyType ...
	 * 	<li> or with &lt;div t:Type="prefix:MyType" ...
	 * </p><br/>
	 * <p>If it's not defined => proposes "t:type" attribute name</p>
	 * <p>If it's defined => find the parameters of this component, and proposes all of them</p>
	 * <br/>
	 * <p>It also adds some general parameters in both cases, like "t:id"</p>
	 * <br/>
	 * <p>At the end, we check existing parameters and remove them from the proposals</p>
	 */
	@Override
	protected void addAttributeNameProposals(ContentAssistRequest request, CompletionProposalInvocationContext context) {
		if(tapestryFeatureModel != null){
			
			//1- Add COMPONENT context specific parameters:
			String componentName = TmlHelper.getComponentFullName(request.getNode(), t);
			
			//case-A: the component is already defined -> we propose its parameters
			if(componentName != null){
				ComponentModel currentComponentModel = tapestryFeatureModel.getProjectModel().getComponent(componentName);

				if(currentComponentModel != null){
					for(Parameter parameter : currentComponentModel.getParameters()){
						String attributeName = parameter.getQualifiedName(t);
						
						addAttributeNameProposal(request, context, attributeName, parameter.getJavadoc(),
								imageC, currentComponentModel.getName());
					}
				}
			}
			//Case-B: the component is not defined -> we propose "t:type"
			else{
				addAttributeNameProposal(request, context, t + ":" + TYPE, UIMessages.T_TYPE_JAVADOC, 
						image, Constants.TAPESTRY_CORE);
			}
			
			
			//2- Mixins parameters
			List<String> mixinNames = TmlHelper.getMixinTypes(request.getNode(), t);
			if(mixinNames != null && mixinNames.size()>0){
				MixinModel currentMixinModel = null;
				for (String mixinName : mixinNames) {
					currentMixinModel = tapestryFeatureModel.getProjectModel().getMixin(mixinName);
					
					if(currentMixinModel != null){
						for(Parameter parameter : currentMixinModel.getParameters()){
							String attributeName = parameter.getQualifiedName(t);
							
							addAttributeNameProposal(request, context, attributeName, parameter.getJavadoc(), 
									imageC, currentMixinModel.getFullName());
						}
					}
				}
			}
			
			
			//3: add generals parameters (t:id...):
			addAttributeNameProposal(request, context, t + ":" + ID, 
					UIMessages.T_ID_JAVADOC, 
					image, Constants.TAPESTRY_CORE);
			
			addAttributeNameProposal(request, context, t + ":" + MIXINS, 
					UIMessages.T_MIXINS_JAVADOC, 
					image, Constants.TAPESTRY_CORE);
		}
	}
	
	/**
	 * <p>If the attribute is called "t:Type" => we propose all the component names
	 * 
	 * <p>If the attribute is called "t:mixins" => all the mixins
	 * 
	 * <p>If the attribute is called "t:page" => all the pages
	 * 
	 * <p>The last case should only appear in &lt;t:PageLink, whereas the other 2 are standard Tapestry 5 parameters
	 * for any component
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void addAttributeValueProposals(ContentAssistRequest request, CompletionProposalInvocationContext context) {
		
		List<ICompletionProposal> proposals = request.getProposals();
		
		if(tapestryFeatureModel != null){
			String wholeDocument = context.getDocument().get();
			String alreadyTyped = getTypedBefore(wholeDocument, context.getInvocationOffset());
			int replacementLengthAfterCursor = getAttributeReplacementLength(wholeDocument, context.getInvocationOffset());
			int startOffset = context.getInvocationOffset() - alreadyTyped.length();
			
			String attributeName = TmlHelper.getAttributeBefore(wholeDocument, request.getStartOffset());
			Node node = request.getNode();
			
			//---------- ONLY if we're in a t:type attribute ------------------
			if(attributeName.equals(t + ":" + TYPE)){
				for(AbstractParameteredFeatureModel feature : tapestryFeatureModel.getProjectModel().getComponents()){
						//updateReplacementLengthOnValidate
					addAttributeValueProposal(feature, proposals, node, request, replacementLengthAfterCursor, startOffset, alreadyTyped);
				}
			}
			//---------- ONLY if we're in a t:mixins attribute ------------------
			else if(attributeName.equals(t + ":" + MIXINS)){
				List<String> existingMixins = TmlHelper.getMixinTypes(node, t);
				for(AbstractParameteredFeatureModel feature : tapestryFeatureModel.getProjectModel().getMixins()){
					if(! existingMixins.contains(feature.getName())){
						addAttributeValueProposal(feature, proposals, node, request, replacementLengthAfterCursor, startOffset, alreadyTyped);
					}
				}
			}
			//---------- ONLY if we're in a t:type attribute ------------------
			else if(attributeName.equals(t + ":" + Constants.PAGE_ATTRIBUTE )){
				for(PageModel feature : tapestryFeatureModel.getProjectModel().getPages()){
					addAttributeValueProposal(feature, proposals, node, request, replacementLengthAfterCursor, startOffset, alreadyTyped);
				}
			}
		}
	}
	
	/**
	 * <p>Add the "feature" (Component or Mixin or Page) to the proposals. It will instantiate a new {@link ICompletionProposal}
	 * and set all the parameters
	 * 
	 * <p>Parameters are automatically inserted if it's an {@link AbstractParameteredFeatureModel} 
	 * 
	 * @param feature the feature to insert
	 * @param proposals the list of proposals to insert in
	 * @param node the XML node in which we propose the feature
	 * @param request 
	 * @param replacementLengthAfterCursor
	 * @param startOffset
	 * @param alreadyTyped
	 */
	private void addAttributeValueProposal(AbstractFeatureModel feature, List<ICompletionProposal> proposals, Node node, 
			ContentAssistRequest request, int replacementLengthAfterCursor, int startOffset, String alreadyTyped){
		String toBeInserted = feature.getFullName();
		
		if(toBeInserted.toLowerCase().startsWith(alreadyTyped.toLowerCase())){
			if(feature instanceof AbstractParameteredFeatureModel){
				List<String> existingParams = TmlHelper.getNodeAttributeNames(node);
				for(Parameter requiredParam : ((AbstractParameteredFeatureModel)feature).getMandatoryParameters()){
					//doesn't add already entered params:
					if(! existingParams.contains(requiredParam.getQualifiedName(t))){
						toBeInserted += "\" " + requiredParam.getQualifiedName(t) + "=\""; 
					}
				}
			}
			
			proposals.add(
					new CustomCompletionProposal(toBeInserted, 			//replacementString 
												startOffset, 			//replacementOffset
												alreadyTyped.length() + replacementLengthAfterCursor,		//replacementLength
												toBeInserted.length(),	//cursorPosition
												imageC, 				//imageC
												feature.getFullName(),	//displayString
												null, 					//contextInformation
												feature.getJavadoc(),	//additionalProposalInfo 
												100,					//relevance
												true));	
		}
	}
}
