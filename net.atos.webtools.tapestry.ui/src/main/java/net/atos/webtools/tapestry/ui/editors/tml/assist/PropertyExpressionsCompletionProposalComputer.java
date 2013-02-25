 package net.atos.webtools.tapestry.ui.editors.tml.assist;

import static net.atos.webtools.tapestry.core.util.Constants.ASSET_BINDING;
import static net.atos.webtools.tapestry.core.util.Constants.ASSET_CLASSPATH_BINDING;
import static net.atos.webtools.tapestry.core.util.Constants.ASSET_CONTEXT_BINDING;
import static net.atos.webtools.tapestry.core.util.Constants.ASSET_CONTEXT_ONLY_BINDING;
import static net.atos.webtools.tapestry.core.util.Constants.MESSAGE_BINDING;
import static net.atos.webtools.tapestry.core.util.Constants.PROP_BINDING;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.atos.webtools.tapestry.core.models.JavaElement;
import net.atos.webtools.tapestry.core.models.features.AssetModel;
import net.atos.webtools.tapestry.core.models.features.ComponentModel;
import net.atos.webtools.tapestry.core.models.features.Parameter;
import net.atos.webtools.tapestry.core.util.Constants;
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

/**
 * <p>
 * Compute Tapestry <a href="http://tapestry.apache.org/property-expressions.html">Property Expressions (PE)</a> completion proposals
 * </p>
 * 
 * 
 */
@SuppressWarnings("restriction")
public class PropertyExpressionsCompletionProposalComputer extends AbstractTapestryCompletionProposalComputer {
	private static final String[] keywords = {"true", "false", "this", "null"};
	
	private static final String IMG_FILE_PATTERN =  "((.*?)+(\\.(?i)(jpg|jpeg|png|gif|bmp|tiff))$)";
	
	private static final String CSS_FILE_PATTERN = "((.*?)+(\\.(?i)(css))$)";
	
	private static final String JS_FILE_PATTERN =  "((.*?)+(\\.(?i)(js))$)";
	
	@Override
	public void sessionStarted() {
		super.sessionStarted();
		if(tapestryFeatureModel != null){
			tapestryFeatureModel.initJavaFields();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addTagInsertionProposals(ContentAssistRequest contentAssistRequest, int childPosition,
			CompletionProposalInvocationContext context) {
		
		if(tapestryFeatureModel != null){
			List<ICompletionProposal> proposals = contentAssistRequest.getProposals();
			
			String wholeDocument = context.getDocument().get();
			String alreadyTyped = getTypedBefore(wholeDocument, context.getInvocationOffset());
			
			//------------ PART-1 assets ----------------------
			/*if(alreadyTyped != null && alreadyTyped.toLowerCase().startsWith(ASSET_BINDING + ":")){
				//TODO: implement asset proposals
				return;
			}*/
			//------------ PART-2 field properties: with subsequent sub-properties after '.' or '.?' ----------------------
			
			//If the property expression is "${person.adress.z", it will have {"person", "adress", "z"}:
			List<String> peCallsList = new ArrayList<String>(); 
			StringTokenizer stringTokenizer = new StringTokenizer(alreadyTyped.replace("${", "") + " ", ".");
			while(stringTokenizer.hasMoreElements()){
				String token = stringTokenizer.nextToken();
				StringBuilder builder = new StringBuilder(token);
				if(token.contains("(")){
					while(stringTokenizer.hasMoreElements() && ! token.contains(")")){
						token = stringTokenizer.nextToken();
						builder.append('.').append(token);
					}
				}
				peCallsList.add(builder.toString());
			}
			
			try {
				IType parsedType = tapestryFeatureModel.getJavaIType();
				if(peCallsList.size() > 0){
					for(int i = 0; i < peCallsList.size() - 1; i++){
						String fieldName = peCallsList.get(i).replace("?", "");
						parsedType = JavaModelHelper.getFieldOrMethodType(parsedType, fieldName);
					}
					
					//add properties:
					for(JavaElement property: JavaModelHelper.getPublicJavaBeanProperties(parsedType)){
						String lastTypedField = peCallsList.get(peCallsList.size() - 1).trim();
						if(property.getName().toLowerCase().startsWith(lastTypedField.toLowerCase())){
							int replacementLength = getAttributeReplacementLength(wholeDocument, context.getInvocationOffset()) + lastTypedField.length();
							int offset = context.getInvocationOffset() - lastTypedField.length();
							String toBeInserted = property.getName();
							int cursorPosition = toBeInserted.length();
							
							proposals.add(
									new CustomCompletionProposal(toBeInserted, 			//replacementString 
																offset, 				//replacementOffset
																replacementLength,		//replacementLength
																cursorPosition,			//cursorPosition
																imagePE, 				//image
																property.getName(), 	//displayString
																null, 					//contextInformation
																property.getJavadoc(),	//additionalProposalInfo 
																10,						//relevance
																true));					//updateReplacementLengthOnValidate
						}
					}
					
					//add methods:
					String lastTypedField = peCallsList.get(peCallsList.size() - 1).trim();
					for(JavaElement property: JavaModelHelper.getPublicMethodsNoGetterNoVoid(parsedType)){
						if(property.getName().toLowerCase().startsWith(lastTypedField.toLowerCase())){
							int offset = context.getInvocationOffset() - lastTypedField.length();
							int replacementLength = getAttributeReplacementLength(wholeDocument, context.getInvocationOffset()) + lastTypedField.length();
							String toBeInserted = property.getName();
							int cursorPosition = toBeInserted.length();
							
							proposals.add(
									new CustomCompletionProposal(toBeInserted, 			//replacementString 
																offset, 				//replacementOffset
																replacementLength,		//replacementLength
																cursorPosition,			//cursorPosition
																imagePE, 				//image
																property.getLabel(),	//displayString
																null, 					//contextInformation
																property.getJavadoc(),	//additionalProposalInfo 
																10,						//relevance
																true));					//updateReplacementLengthOnValidate
						}
					}
				}
			}
			catch (JavaModelException e) {
				TapestryUI.logWarning(UIErrorMessages.ERROR_WHILE_PROPOSING_PROPERTY_EXPRESSIONS, e);
			}
			
			
			//------------------ PART-3 messages & special keywords (no sub properties) -----------------------------------
			
			List<JavaElement> peProposals= new ArrayList<JavaElement>();
			peProposals.addAll(tapestryFeatureModel.getMessages());
			
			for (String keyword : keywords) {
				peProposals.add(new JavaElement(keyword, ""));
			}
			
			for(JavaElement peProposal: peProposals){
				if(peProposal.getName() != null && peProposal.getName().toLowerCase().startsWith(alreadyTyped.toLowerCase().replace("${", ""))){
					int offset = context.getInvocationOffset() - alreadyTyped.length();
					int replacementLength = getAttributeReplacementLength(wholeDocument, context.getInvocationOffset()) + alreadyTyped.length();
					String toBeInserted = "${" + peProposal.getName();
					int cursorPosition = toBeInserted.length();
					
					proposals.add(
							new CustomCompletionProposal(toBeInserted, 					//replacementString 
														offset, 	//replacementOffset
														replacementLength,								//replacementLength
														cursorPosition,			//cursorPosition
														imagePE, 						//image
														peProposal.getName(), 			//displayString
														null, 							//contextInformation
														peProposal.getJavadoc(),		//additionalProposalInfo 
														100,							//relevance
														true));							//updateReplacementLengthOnValidate
				}
			}
		}
	}

	
	@SuppressWarnings("unchecked")
	@Override
	protected void addAttributeValueProposals(ContentAssistRequest request, CompletionProposalInvocationContext context) {
		if(tapestryFeatureModel != null){
			String wholeDocument = context.getDocument().get();
			String attributeName = TmlHelper.getAttributeBefore(wholeDocument, request.getStartOffset());
			
			//No property expression in t:type, t:mixins, and t:page attribute:
			if(attributeName.equals(t + ":" + Constants.TYPE) 
					|| attributeName.equals(t + ":" + Constants.MIXINS) 
					|| attributeName.equals(t + ":" + Constants.PAGE_ATTRIBUTE)){
				return;
			}
			
			List<ICompletionProposal> proposals = request.getProposals();
			String alreadyTyped = getTypedBefore(wholeDocument, context.getInvocationOffset());
			
			//if there's a Tapestry prefix binding, we remove it: 
			if(!(alreadyTyped.contains("asset:")||alreadyTyped.contains("context:"))&& (alreadyTyped.contains(":"))){
				alreadyTyped = alreadyTyped.substring(alreadyTyped.indexOf(':') + 1);
			}
			
			String defaultPrefix = PROP_BINDING;
			Parameter currentParameter = getCurrentParameter(wholeDocument, request);
			if(currentParameter != null){
				defaultPrefix = currentParameter.getDefaultPrefix();
			}
			String prefix = "";
			if(! PROP_BINDING.equals(defaultPrefix)){
				prefix = PROP_BINDING;
			}
            
			//------------ PART-1 proposal for asset---------------------
			//Proposal for asset:,asset:context:,context: for attribute value
			
			String alreadyTypedAsset = getTypedBefore(wholeDocument, context.getInvocationOffset());
			String[] assetBindins = {ASSET_CONTEXT_BINDING,ASSET_CONTEXT_ONLY_BINDING,ASSET_BINDING,ASSET_CLASSPATH_BINDING}; 
			
			if(attributeName.toLowerCase().equals(Constants.SRC_ATTRIBUTE) 
					|| attributeName.toLowerCase().equals(Constants.HREF_ATTRIBUTE) 
					|| isStyleElement(wholeDocument, context.getInvocationOffset())){
				for(String assetBinding:assetBindins){
					if(assetBinding.toLowerCase().startsWith(alreadyTypedAsset.toLowerCase().replace("${", ""))){
						int offset = context.getInvocationOffset() - alreadyTypedAsset.length();
						int replacementLength = getAttributeReplacementLengthForAsset(wholeDocument, context.getInvocationOffset()) + alreadyTypedAsset.length();
						String toBeInserted = null;
						if(!isAssetTagEnds(wholeDocument, context.getInvocationOffset())){
							toBeInserted = "${"+assetBinding +"}";
						}else{
							toBeInserted = "${"+assetBinding;
						}
						proposals.add(
								new CustomCompletionProposal(toBeInserted, 				//replacementString 
														offset, 						//replacementOffset
														replacementLength,				//replacementLength
														toBeInserted.length(),			//cursorPosition
														imagePE, 						//image
														assetBinding, 					//displayString
														null, 							//contextInformation
														null,							//additionalProposalInfo 
														100,							//relevance
														true));							//updateReplacementLengthOnValidate
						
					}
				}
			}
			// proposal to add value for asset:,asset:context:,context:
			// e.g.${asset:context:static/css/k-structure.css
			for(String assetBinding:assetBindins){
				if(assetBinding.toLowerCase().equals(alreadyTypedAsset.toLowerCase().replace("${", ""))){// add filtering
					int offset = context.getInvocationOffset();
					int replacementLength = getAttributeReplacementLengthForAsset(wholeDocument, context.getInvocationOffset());
					Pattern pattern = null;
					if(isImageElement(wholeDocument, context.getInvocationOffset())){
						pattern = Pattern.compile(IMG_FILE_PATTERN);
					}else if(isLinkToStyleSheetElement(wholeDocument, context.getInvocationOffset())||isStyleElement(wholeDocument, context.getInvocationOffset())){
						pattern = Pattern.compile(CSS_FILE_PATTERN);
					}else if(isScriptElement(wholeDocument, context.getInvocationOffset())){
						pattern = Pattern.compile(JS_FILE_PATTERN);
					}
					//propose all the assets from class path 
					if(assetBinding.equals(ASSET_CLASSPATH_BINDING)||assetBinding.equals(ASSET_BINDING)){
						for(AssetModel assetModel: tapestryFeatureModel.getProjectModel().getAssetsFromClassPath()){
							String	toBeInserted = assetModel.getPath();
							Matcher matcher = pattern.matcher(assetModel.getName());
							if(!matcher.matches()){
								continue;
							}
							proposals.add(
									new CustomCompletionProposal(toBeInserted, 					//replacementString 
																offset, 						//replacementOffset
																replacementLength,				//replacementLength
																toBeInserted.length(),			//cursorPosition
																imagePE, 						//image
																assetModel.getName(), 			//displayString
																null, 							//contextInformation
																null,							//additionalProposalInfo 
																100,							//relevance
																true));							//updateReplacementLengthOnValidate
						}
					} else {
						//propose all the assets from context path
						for(AssetModel assetModel: tapestryFeatureModel.getProjectModel().getAssets()){
							String	toBeInserted = assetModel.getPath();
							Matcher matcher = pattern.matcher(assetModel.getName());
							if(!matcher.matches()){
								continue;
							}
							proposals.add(
									new CustomCompletionProposal(toBeInserted, 					//replacementString 
																offset, 						//replacementOffset
																replacementLength,				//replacementLength
																toBeInserted.length(),			//cursorPosition
																imagePE, 						//image
																assetModel.getName(), 			//displayString
																null, 							//contextInformation
																null,							//additionalProposalInfo 
																100,							//relevance
																true));							//updateReplacementLengthOnValidate
						}
					}
				}
			}
			
			//------------ PART-1 proposal for asset ends ---------------------
			
			//------------ PART-1 field properties: with subsequent sub-properties after '.' or '.?' ----------------------
			
			List<String> peCallsList = new ArrayList<String>(); 
			StringTokenizer stringTokenizer = new StringTokenizer(alreadyTyped.replace("${", "") + " ", ".");
			while(stringTokenizer.hasMoreElements()){
				String token = stringTokenizer.nextToken();
				StringBuilder builder = new StringBuilder(token);
				if(token.contains("(")){
					while(stringTokenizer.hasMoreElements() && ! token.contains(")")){
						token = stringTokenizer.nextToken();
						builder.append('.').append(token);
					}
				}
				peCallsList.add(builder.toString());
			}
			
			try {
				IType parsedType = tapestryFeatureModel.getJavaIType();
				if(peCallsList.size() > 0){
					
					for(int i = 0; i < peCallsList.size() - 1; i++){
						String fieldName = peCallsList.get(i).replace("?", "");
						parsedType = JavaModelHelper.getFieldOrMethodType(parsedType, fieldName);
					}
					
					//add properties:
					for(JavaElement property: JavaModelHelper.getPublicJavaBeanProperties(parsedType)){
						String lastTypedField = peCallsList.get(peCallsList.size() - 1).trim();
						if(property.getName().startsWith(lastTypedField)){
							String toBeInserted = property.getName().substring(lastTypedField.length());
							int replacementOffset = context.getInvocationOffset();
							int replacementLength = getAttributeReplacementLength(wholeDocument, context.getInvocationOffset()) + lastTypedField.length();
							
							//for inserting the 1st field only: we add the prefix when needed
							if(peCallsList.size() == 1){
								toBeInserted = prefix + property.getName();
								replacementOffset = request.getStartOffset() + 1;
							}
							
							proposals.add(
									new CustomCompletionProposal(toBeInserted, 				//replacementString 
																replacementOffset, 			//replacementOffset
																replacementLength,			//replacementLength
																toBeInserted.length(),		//cursorPosition
																imagePE, 					//image
																property.getName(), 		//displayString
																null, 						//contextInformation
																property.getJavadoc(),		//additionalProposalInfo 
																10,							//relevance
																true));						//updateReplacementLengthOnValidate
						}
					}
					
					//add methods:
					for(JavaElement property: JavaModelHelper.getPublicMethodsNoGetterNoVoid(parsedType)){
						String lastTypedField = peCallsList.get(peCallsList.size() - 1).trim();
						if(property.getName().toLowerCase().startsWith(lastTypedField.toLowerCase())){
							int offset = context.getInvocationOffset() - lastTypedField.length();
							int replacementLength = getAttributeReplacementLength(wholeDocument, context.getInvocationOffset()) + lastTypedField.length();

							String toBeInserted = property.getName();
							
							proposals.add(
									new CustomCompletionProposal(toBeInserted, 					//replacementString 
																offset, 	//replacementOffset
																replacementLength,								//replacementLength
																toBeInserted.length(),			//cursorPosition
																imagePE, 						//image
																property.getLabel(), 			//displayString
																null, 							//contextInformation
																property.getJavadoc(),			//additionalProposalInfo 
																10,								//relevance
																true));							//updateReplacementLengthOnValidate
						}
					}
				}
			}
			catch (JavaModelException e) {
				TapestryUI.logWarning(UIErrorMessages.ERROR_WHILE_PROPOSING_PROPERTY_EXPRESSIONS, e);
			}
			
			//------------------ PART-2 messages & special keywords (no sub properties) -----------------------------------
			List<JavaElement> propertiesList= new ArrayList<JavaElement>();
			
			for (JavaElement message : tapestryFeatureModel.getMessages()) {
				propertiesList.add(message);
			}
			for (String keyword : keywords) {
				propertiesList.add(new JavaElement(keyword, ""));
			}
			
			for(JavaElement peProposal: propertiesList){
				String toBeInserted;
				//For tapestry parameters:
				if(peProposal.getName().startsWith(MESSAGE_BINDING)){
					toBeInserted = peProposal.getName();
				}
				else{
					toBeInserted = prefix + peProposal.getName();
				}
				
				//For HTML parameters:
				if(! attributeName.startsWith(t + ":")){
					toBeInserted = "${" + toBeInserted + "}";
				}
				
				int replacementLength = getAttributeReplacementLength(wholeDocument, context.getInvocationOffset()) + alreadyTyped.length();

				if(peProposal.getName().toLowerCase().startsWith(alreadyTyped.toLowerCase())){
					proposals.add(
							new CustomCompletionProposal(toBeInserted, 					//replacementString 
														request.getStartOffset() + 1, 	//replacementOffset
														replacementLength,				//replacementLength
														toBeInserted.length(),			//cursorPosition
														imagePE, 						//image
														peProposal.getName(), 			//displayString
														null, 							//contextInformation
														peProposal.getJavadoc(),		//additionalProposalInfo 
														100,							//relevance
														true));							//updateReplacementLengthOnValidate
				}
			}
		}
	}
	
	/**
	 * find the Tapestry type of the tag, then finds the attribute name, and looks in type for this parameter name. 
	 * 
	 * @param wholeDocument the whole doc to search in
	 * @param request the request
	 * @return the current parameter of the request
	 */
	protected Parameter getCurrentParameter(String wholeDocument, ContentAssistRequest request){
		String tapestryType = TmlHelper.getComponentFullName(request.getNode(), t);
		String attributeName = TmlHelper.getAttributeBefore(wholeDocument, request.getStartOffset());
		
		ComponentModel component = tapestryFeatureModel.getProjectModel().getComponent(tapestryType);
		if(component != null){
			for(Parameter param : component.getParameters()){
				if(param.getQualifiedName(t).equals(attributeName)){
					return param;
				}
			}
		}
		return null;
	}
}
