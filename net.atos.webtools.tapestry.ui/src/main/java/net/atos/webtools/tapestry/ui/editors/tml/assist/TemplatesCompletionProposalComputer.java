package net.atos.webtools.tapestry.ui.editors.tml.assist;

import java.util.List;

import net.atos.webtools.tapestry.core.TapestryCore;
import net.atos.webtools.tapestry.core.templates.TapestryTemplateContext;

import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;

/**
 * Proposes static templates of code, with their corresponding code as a "doc".
 * 
 * These templates are defined in: <pre>tmlTemplate.xml</pre>
 * 
 * and referenced in the plugin.xml under: <pre>org.eclipse.ui.editors.templates</pre>
 * 
 * ContextType is: <pre>net.atos.webtools.tapestry.editors.tml.templates.ContentAssistContextType</pre>
 * 
 * @author a160420
 *
 */
@SuppressWarnings("restriction")
public class TemplatesCompletionProposalComputer extends AbstractTapestryCompletionProposalComputer {
	
	/**
	 * Add the template using the existing {@link TemplateProposal} to do all the magic easily :) 
	 */
	@Override
	protected void addTagInsertionProposals(ContentAssistRequest request, int childPosition, CompletionProposalInvocationContext context) {
		if(tapestryFeatureModel != null){
			Template[] contentAssistTemplates = tapestryFeatureModel.getProjectModel().getContentAssistTemplates();
			
			@SuppressWarnings("unchecked")
			List<ICompletionProposal> proposals = request.getProposals();
			
			String wholeDocument = context.getDocument().get();
			String alreadyTyped = getTypedBefore(wholeDocument, context.getInvocationOffset());
			
			for (Template template : contentAssistTemplates) {
				TemplateContextType contextType = 
						TapestryCore.getDefault().getCodeTemplateContextRegistry().getContextType(template.getContextTypeId());
				TemplateContext templateContext = new TapestryTemplateContext(tapestryFeatureModel, contextType);
				proposals.add(
						new TemplateProposal(template, 
								templateContext, 
								new Region(context.getInvocationOffset() - alreadyTyped.length(), alreadyTyped.length()), 
								imageTemplate));
			}
		}
	}
}
