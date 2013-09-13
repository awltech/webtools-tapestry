package net.atos.webtools.tapestry.ui.editors.proposal;

import java.util.List;

import net.atos.webtools.tapestry.ui.TapestryUI;
import net.atos.webtools.tapestry.ui.util.UIConstants;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;

@SuppressWarnings("restriction")
public class ProposalHelper {
	
	/**
	 * Add a new assist proposal.
	 */
	public static void addProposal(ProposalModel proposal, List<ICompletionProposal> proposals, String alreadyTyped,
			int replacementLengthAfterCursor, int startOffset) {
		
		String toBeInserted = proposal.getFullName();
		
		proposals.add(new CustomCompletionProposal(
				toBeInserted, 
				startOffset, 													//replacementOffset
				alreadyTyped.length() + replacementLengthAfterCursor, 			//replacementLength
				proposal.getCursorPosition(), 									//cursorPosition
				TapestryUI.getDefault().getImage(UIConstants.IMG_TAPESTRY_PE), 
				proposal.getName(), 											//displayString
				null, 
				null, 
				100, 
				true
			));
	}
}
