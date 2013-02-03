package net.atos.webtools.tapestry.ui.editors.tml.sourcevalidators;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.wst.sse.core.internal.FileBufferModelManager;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator;
import org.eclipse.wst.validation.internal.core.ValidationException;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;


@SuppressWarnings("restriction")
@Deprecated
public class AttributeUnicity implements IValidator, ISourceValidator  {
	//---------------------------------------------------------
	//					IValidator (whole doc)
	//---------------------------------------------------------
	@Override
	public void cleanup(IReporter reporter) {
	}

	@Override
	public void validate(IValidationContext helper, IReporter reporter) throws ValidationException {
		
	}
	
	//---------------------------------------------------------
	//				ISourceValidator (partial doc)
	//---------------------------------------------------------
	private IDocument doc;
	private IPath filePath;
	private IFile file;
//	private EditedFeatureModel tapestryFeatureModel;
	
//	private IEditorPart editorPart;
	
	@Override
	public void connect(IDocument document) {
		doc = document;

		//This MUST run in the UI Thread:
//		Display.getDefault().syncExec(new Runnable() {
//		  public void run() {
//			  editorPart = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
//		  }
//		});

//		if(editorPart instanceof TapestryMultiPageEditor){
//			tapestryFeatureModel = ((TapestryMultiPageEditor) editorPart).getTapestryFeatureModel();
//		}
		
		
		ITextFileBuffer fb = FileBufferModelManager.getInstance().getBuffer(doc);
		if (fb != null) {
			filePath = fb.getLocation();

			if (filePath.segmentCount() > 1) {
				file = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
				if (!file.isAccessible()) {
					file = null;
				}
			}
		}
	}

	@Override
	public void disconnect(IDocument document) {
		doc = null;
		filePath = null;
		file = null;
//		tapestryFeatureModel = null;
//		editorPart = null;
	}

	@Override
	public void validate(IRegion dirtyRegion, IValidationContext helper, IReporter reporter) {
//		String modifiedRegion = "";
//		try {
//			modifiedRegion = doc.get(dirtyRegion.getOffset(), dirtyRegion.getLength());
//		}
//		catch (BadLocationException e) {
//			Activator.logError("Validation failed to read document part", e);
//		}
		
//		System.out.println("----------\n" + modifiedRegion + "\n-------------");
		
		
//		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
//		builderFactory.setNamespaceAware(false);
//		
//		try {
//			DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
//			Document document = documentBuilder.parse(new InputSource(new StringReader(modifiedRegion)));
//			NodeList childNodes = document.getChildNodes();
//			
//			for(int i = 0; i < childNodes.getLength(); i++) {
//				Node childNode = childNodes.item(i);
////				System.out.println("# " + childNode.getNodeName());
//				
//				NamedNodeMap attributes = childNode.getAttributes();
//				Set<String> attributeNames = new HashSet<String>();
//				for(int j = 0; j < attributes.getLength(); j++){
//					Node attribute = attributes.item(j);
//					if(attribute.getNodeName().equals("t:id")){
//						IMessage m = new LocalizedMessage(IMessage.HIGH_SEVERITY, "found foo");
//						m.setOffset(dirtyRegion.getOffset());
//						m.setLength(dirtyRegion.getLength());
//						try {
//							m.setLineNo(doc.getLineOfOffset(dirtyRegion.getOffset()) + 1);
//						}
//						catch (BadLocationException e) {
//							m.setLineNo(-1);
//						}
//						reporter.addMessage(this, m);
//						
////						reporter.addMessage(this, new Message(Activator.PLUGIN_ID, IMessage.NORMAL_SEVERITY, this.getClass().getName()));
//					}
//					attributeNames.add(attribute.getNodeName());
////					System.out.println("\t ->" + attribute.getNodeName() + " - " + attribute.getNodeValue());
//				}
//			}
//			
//		}
//		catch (ParserConfigurationException e) {
//			e.printStackTrace();
//		}
//		catch (SAXException e) {
//			e.printStackTrace();
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
		
		
	}

}
