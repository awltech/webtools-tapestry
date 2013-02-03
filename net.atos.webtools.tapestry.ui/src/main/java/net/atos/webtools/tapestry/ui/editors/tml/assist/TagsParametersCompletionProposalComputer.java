package net.atos.webtools.tapestry.ui.editors.tml.assist;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.atos.webtools.tapestry.core.models.JavaElement;
import net.atos.webtools.tapestry.core.models.features.ComponentModel;
import net.atos.webtools.tapestry.core.models.features.Parameter;
import net.atos.webtools.tapestry.core.util.helpers.JavaModelHelper;
import net.atos.webtools.tapestry.core.util.helpers.TmlHelper;
import net.atos.webtools.tapestry.ui.TapestryUI;
import net.atos.webtools.tapestry.ui.util.UIErrorMessages;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Proposes "Parameters tags" (<p:... >) when inside Tapestry component with Block parameter.
 * 
 * It manages 2 kinds of "p:" parameters:
 * <li> "Block parameters": when a component has a parameter of type org.apache.tapestry5.Block
 * <li> "Special parameters": for some specific components (hard-coded list defined in SpecialParametersComponent enum), 
 * that dynamically defines these parameters tags, depending on an Object (BeanEditForm, Grid...)
 * 
 * 
 * @author a160420
 *
 */
@SuppressWarnings("restriction")
public class TagsParametersCompletionProposalComputer extends AbstractTapestryCompletionProposalComputer {
	
	/**
	 * As proposals are the nearly the same for addTagNameProposals & addTagInsertionProposals, 
	 * we delegate to this method, and return a list of proposals.
	 * The only difference, is that new tags starts with "&lt;", and not tag names: that's why we must
	 * set the isTagInsertion boolean.
	 * 
	 * We could also have set the proposals directly, and return void, but this way, it allows to 
	 * post-process the list differently in each case (if needed...)  
	 *  
	 * 
	 * @param request
	 * @param context
	 * @param isTagInsertion
	 * @return
	 */
	@Override
	protected List<ICompletionProposal> getTagProposals(ContentAssistRequest request, 
			CompletionProposalInvocationContext context, boolean isTagInsertion){
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		
		if(tapestryFeatureModel != null){
			String wholeDocument = context.getDocument().get();
			String alreadyTyped = getTypedBefore(wholeDocument, context.getInvocationOffset());
			int replacementLength = getAttributeReplacementLength(wholeDocument, context.getInvocationOffset())  + alreadyTyped.length();
			int offset = context.getInvocationOffset() - alreadyTyped.length();
			
			Node parentNode = request.getParent();
			String parentTypeName = TmlHelper.getComponentFullName(parentNode, t);
			ComponentModel parentComponent = tapestryFeatureModel.getProjectModel().getComponent(parentTypeName);
			
			NodeList childNodes = parentNode.getChildNodes();
			List<String> existingSiblings = new ArrayList<String>();
			for(int i = 0; i < childNodes.getLength(); i++){
				if(childNodes.item(i) != request.getNode() && childNodes.item(i).getNodeName() != null 
						&& childNodes.item(i).getNodeName().startsWith(p + ":")){
					existingSiblings.add(childNodes.item(i).getNodeName());
				}
			}
			
			if(parentComponent != null){
				List<JavaElement> properties = new ArrayList<JavaElement>();
				//CASE-1: Lists "static" Block parameters (<p:empty ...) 
				for(Parameter blockParameter : parentComponent.getBlockParameters()){
					properties.add(new JavaElement(
							blockParameter.getQualifiedName(p), 
							blockParameter.getJavadoc()));
				}
				
				//CASE-2: dynamically finds the parameters for some specific components
				try {
					for(SpecialParametersComponent spc : SpecialParametersComponent.values()){
						//test if we're in one of these special component:
						if(parentComponent.getFullName().equalsIgnoreCase(spc.getComponentName())){
							
							//test if there's an XML attribute with name == "source"/"object"
							if(parentNode.getAttributes() != null){
								String source;
								//source=""
								if(parentNode.getAttributes().getNamedItem(spc.getObjectParam()) != null){
									source = parentNode.getAttributes().getNamedItem(spc.getObjectParam()).getNodeValue();
								}
								//t:source=""
								else if(parentNode.getAttributes().getNamedItem(t + ":" + spc.getObjectParam()) != null){
									source = parentNode.getAttributes().getNamedItem(
											t + ":" + spc.getObjectParam()).getNodeValue();
								}
								else{
									//no param, we can't go further
									continue;
								}
								
								//loop on curent component properties to find the one in the source
								IType editedFeatureType = tapestryFeatureModel.getJavaIType();
								for(JavaElement property: JavaModelHelper.getPublicJavaBeanProperties(editedFeatureType)){
									if(property.getName().equalsIgnoreCase(source)){
										//get the type of source:
										IType sourceType = JavaModelHelper.getFieldOrMethodType(editedFeatureType, property.getName());
										
										//loop on the sourceType properties to find properties (excluding "getClass()")
										Set<JavaElement> sourceProps = JavaModelHelper.getPublicJavaBeanProperties(sourceType);
										for (JavaElement javaProperty : sourceProps) {
											if(javaProperty.getName() != null && ! javaProperty.getName().equalsIgnoreCase("class")){
												//Add the property with each suffix (if there's more than one):
												for(String suffix : spc.getSpecialParamSuffixes()){
													properties.add(new JavaElement(
															p + ":" + javaProperty.getName() + suffix, 
															"Override for " + javaProperty.getName() + " property of " + sourceType.getElementName()));
												}
											}
										}
									}
								}
							}
						}
					}
				}
				catch (JavaModelException e) {
					TapestryUI.logWarning(UIErrorMessages.ERROR_ON_AUTO_COMPLETION_CAN_T_PROPOSE_SUB_PROPERTIES, e);
				}
				
				//--------------------------- ADD the proposals from the JavaProperties---------------------------
				for(JavaElement prop : properties){
					if(! existingSiblings.contains(prop.getName())){
						if(prop.getName() != null && prop.getName().toLowerCase().startsWith(alreadyTyped.toLowerCase())){
							StringBuilder toBeInsertedSB = new StringBuilder();
							if(isTagInsertion){
								toBeInsertedSB.append('<');
							}
							
							toBeInsertedSB.append(prop.getName());
							
							if(isTagInsertion){
								toBeInsertedSB.append('>');
							}
							
							String toBeInserted = toBeInsertedSB.toString();
							
							proposals.add(
									new CustomCompletionProposal(toBeInserted, 			//replacementString
																offset,					//replacementOffset
																replacementLength,		//replacementLength
																toBeInserted.length(),	//cursorPosition
																imageC,		 			//imageC
																prop.getName(),			//displayString
																null, 					//contextInformation
																prop.getJavadoc(),		//additionalProposalInfo 
																100,					//relevance
																true));					//updateReplacementLengthOnValidate
						}
					}
				}
			}
		}
		return proposals;
	}
	
	/**
	 * Enum of some specific Components that have "p:" "parameter tags" dynamically 
	 * deduced from a IType of one of its param (in addition of any statically defined "Block parameter"). 
	 * 
	 * @author a160420
	 *
	 */
	public enum SpecialParametersComponent{
		/**
		 * org.apache.tapestry5.corelib.components.Grid
		 */
		GRID("Grid", "source", new String[]{"Header", "Cell"}),
		/**
		 * org.apache.tapestry5.corelib.components.BeanEditor
		 */
		BEAN_EDITOR("BeanEditor", "object"),
		/**
		 * org.apache.tapestry5.corelib.components.BeanDisplay
		 */
		BEAN_DISPLAY("BeanDisplay", "object"),
		/**
		 * org.apache.tapestry5.corelib.components.BeanEditForm
		 */
		BEAN_EDIT_FORM("BeanEditForm", "object");
		
		/**
		 * The name of the component class
		 */
		String componentName;
		/**
		 * the name of the component parameter that is used to set the object
		 */
		String objectParam;
		/**
		 * Sometime the special parameter tag has the same name that the source properties,
		 * but sometimes it must be suffixed (Grid)
		 */
		String[] specialParamSuffixes;
		
		
		public String getComponentName() {
			return componentName;
		}
		public String getObjectParam() {
			return objectParam;
		}
		
		public String[] getSpecialParamSuffixes() {
			return specialParamSuffixes;
		}
		private SpecialParametersComponent(String componentName, String objParam) {
			this(componentName, objParam, new String[]{""});
		}
		private SpecialParametersComponent(String componentName, String objectParam, String[] specialParamSuffixes) {
			this.componentName = componentName;
			this.objectParam = objectParam;
			this.specialParamSuffixes = specialParamSuffixes;
		}
	}
}
