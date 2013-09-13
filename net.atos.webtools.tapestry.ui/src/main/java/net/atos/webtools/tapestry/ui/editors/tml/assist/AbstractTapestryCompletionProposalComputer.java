package net.atos.webtools.tapestry.ui.editors.tml.assist;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import net.atos.webtools.tapestry.core.models.EditedFeatureModel;
import net.atos.webtools.tapestry.core.util.helpers.TmlHelper;
import net.atos.webtools.tapestry.ui.TapestryUI;
import net.atos.webtools.tapestry.ui.editors.multi.TapestryMultiPageEditor;
import net.atos.webtools.tapestry.ui.util.UIConstants;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.AbstractXMLCompletionProposalComputer;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;

/**
 * Parent of all Completion proposals for Tapestry files (tml only, not for Java).
 * It's based on an {@link AbstractXMLCompletionProposalComputer}, that is dedicated to XML
 * It has some empty methods that must be overridden to add some proposals for:
 * <ul>
 * 	<li>attribute name
 * 	<li>attribute value
 * 	<li>comment
 * 	<li>tag name
 * 	<li>tag close
 * 	<li>PCDATA
 * 	<li>...
 * </ul>
 * 
 * @author a160420
 *
 */
@SuppressWarnings("restriction")
public abstract class AbstractTapestryCompletionProposalComputer extends AbstractXMLCompletionProposalComputer {
	protected Image image = TapestryUI.getDefault().getImage(UIConstants.IMG_TAPESTRY_DEFAULT);
	protected Image imageC = TapestryUI.getDefault().getImage(UIConstants.IMG_TAPESTRY_COMPONENT);
	protected Image imagePE = TapestryUI.getDefault().getImage(UIConstants.IMG_TAPESTRY_PE);
	protected Image imageTemplate = TapestryUI.getDefault().getImage(UIConstants.IMG_TAPESTRY_TEMPLATE);
	
	protected EditedFeatureModel tapestryFeatureModel;
	protected String t;
	protected String p;
	
	//***************************************************************************************
	//
	//									UTIL METHODS
	//
	//***************************************************************************************
	
	/**
	 * get all the code typed before the index (typically cursor position for auto-completion).
	 * It stops on the first whitespace, or on some special chars
	 * 
	 * @param wholeDocument
	 * @param index
	 * @return the previous "block" of non-whitespace characters
	 */
	protected String getTypedBefore(String wholeDocument, int index){
		StringBuilder sb = new StringBuilder();
		index --;
		char currentChar = wholeDocument.charAt(index);
		while(! Character.isWhitespace(currentChar) 
				&& currentChar != '"'
				&& currentChar != ','
				&& currentChar != '=' 
				&& currentChar != '<' 
				&& currentChar != '>' 
				&& currentChar != '}'){
			sb.insert(0, currentChar);
			index --;
			currentChar = wholeDocument.charAt(index);
		}
		
		return sb.toString();
	}
	
	/**
	 * 
	 * 
	 * @param wholeDocument
	 * @param index
	 * @return
	 */
	protected int getAttributeReplacementLength(String wholeDocument, int index) {
		int replacementLength = 0;
		
		char currentChar = wholeDocument.charAt(index);
		
		while(currentChar != '.'
				&& currentChar != '}'
				&& currentChar != ','
				&& currentChar != '>'
				&& currentChar != '"'
				&& ! Character.isWhitespace(currentChar)) {
			index ++;
			replacementLength ++;
			if(index == wholeDocument.length()){
				break;
			}
			currentChar = wholeDocument.charAt(index);
			
		}
		
		return replacementLength;
	}
	
	//***************************************************************************************
	//
	//							 Methods that can be implemented:
	//
	//***************************************************************************************
	
	/**
	 * 
	 * 
	 * @param request
	 * @param context
	 * @param isTagInsertion
	 * @return
	 */
	protected List<ICompletionProposal> getTagProposals(ContentAssistRequest request, 
			CompletionProposalInvocationContext context, boolean isTagInsertion) {
		return Collections.emptyList();
	}
	

	/**
	 * add proposal with the good format
	 * 
	 * @param proposals list to enrich
	 * @param attributeName the fully qualified name of the attribute
	 * @param javadoc the javadoc to show
	 * @param offset calculated offset to put text
	 * @param theImage the proposal image
	 */
	protected void addAttributeNameProposal(ContentAssistRequest request, CompletionProposalInvocationContext context, 
			String attributeName, String javadoc, Image theImage, String source) {
				
		@SuppressWarnings("unchecked")
		List<ICompletionProposal> proposals = request.getProposals();
		
		String wholeDocument = context.getDocument().get();
		String alreadyTyped = getTypedBefore(wholeDocument, context.getInvocationOffset());
		int replacementLength = getAttributeReplacementLength(wholeDocument, context.getInvocationOffset()) + alreadyTyped.length();
		int offset = context.getInvocationOffset() - alreadyTyped.length();
		
		//remove the one that are already present 
		//(be carefull, for Tapestry component params, it must be prefixed with namespace to be considered the same)
		List<String> existingParams = TmlHelper.getNodeAttributeNames(request.getNode());
		
		if(attributeName.startsWith(alreadyTyped) && ! existingParams.contains(attributeName)){
			String toBeInserted = attributeName + "=\"\"";
			proposals.add(new CustomCompletionProposal(toBeInserted, 			//replacementString 
												offset, 		//replacementOffset
												replacementLength,				//replacementLength
												toBeInserted.length(),			//cursorPosition
												theImage, 						//imageC
												attributeName + " - " + source ,//displayString
												null, 							//contextInformation
												javadoc,						//additionalProposalInfo 
												100,							//relevance
												true));							//updateReplacementLengthOnValidate
		}
	}

	
	//----------------------------------------------------------------------------------
	//
	//			Implementation of AbstractXMLCompletionProposalComputer
	//
	//	addXXX methods only have empty implementation, to remove them from sub-classes	
	//----------------------------------------------------------------------------------
	
	/**
	 * called before all the other methods, to do some init: here we load the model from the editor.
	 */
	@Override
	public void sessionStarted() {
		IEditorPart editorPart = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if(editorPart instanceof TapestryMultiPageEditor){
			tapestryFeatureModel = ((TapestryMultiPageEditor) editorPart).getTapestryFeatureModel();
			t = tapestryFeatureModel.getNamespace();
			p = tapestryFeatureModel.getParameterNamespace();
		}
	}

	@Override
	public void sessionEnded() {
		
	}
	
	//------------ Tags ------------
	@SuppressWarnings("unchecked")
	@Override
	protected void addTagNameProposals(ContentAssistRequest request, int childPosition,
			CompletionProposalInvocationContext context) {
		request.getProposals().addAll(getTagProposals(request, context, false));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void addTagInsertionProposals(ContentAssistRequest request, int childPosition,
			CompletionProposalInvocationContext context) {
		request.getProposals().addAll(getTagProposals(request, context, true));
	}
	
	//----------------------------------------

	@Override
	protected void addAttributeNameProposals(ContentAssistRequest contentAssistRequest,
			CompletionProposalInvocationContext context) {
		
	}

	@Override
	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest,
			CompletionProposalInvocationContext context) {
		
	}

	@Override
	protected void addCommentProposal(ContentAssistRequest contentAssistRequest,
			CompletionProposalInvocationContext context) {
		
	}

	@Override
	protected void addEmptyDocumentProposals(ContentAssistRequest contentAssistRequest,
			CompletionProposalInvocationContext context) {
		
	}

	@Override
	protected void addEndTagNameProposals(ContentAssistRequest contentAssistRequest,
			CompletionProposalInvocationContext context) {
		
	}

	@Override
	protected void addEndTagProposals(ContentAssistRequest contentAssistRequest,
			CompletionProposalInvocationContext context) {
		
	}

	@Override
	protected void addEntityProposals(ContentAssistRequest contentAssistRequest, ITextRegion completionRegion,
			IDOMNode treeNode, CompletionProposalInvocationContext context) {
		
	}

	@Override
	protected void addEntityProposals(@SuppressWarnings("rawtypes") Vector proposals, Properties map, String key, int nodeOffset,
			IStructuredDocumentRegion sdRegion, ITextRegion completionRegion,
			CompletionProposalInvocationContext context) {
		
	}

	@Override
	protected void addPCDATAProposal(String nodeName, ContentAssistRequest contentAssistRequest,
			CompletionProposalInvocationContext context) {
		
	}

	@Override
	protected void addStartDocumentProposals(ContentAssistRequest contentAssistRequest,
			CompletionProposalInvocationContext context) {
		
	}

	@Override
	protected void addTagCloseProposals(ContentAssistRequest contentAssistRequest,
			CompletionProposalInvocationContext context) {
		
	}
}
