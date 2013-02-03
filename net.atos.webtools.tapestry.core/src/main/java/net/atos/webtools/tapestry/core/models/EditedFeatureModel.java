
package net.atos.webtools.tapestry.core.models;

import static net.atos.webtools.tapestry.core.models.FileType.JAVA;
import static net.atos.webtools.tapestry.core.models.FileType.PROPERTY;
import static net.atos.webtools.tapestry.core.models.FileType.TML;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.atos.webtools.tapestry.core.TapestryCore;
import net.atos.webtools.tapestry.core.models.features.ComponentModel;
import net.atos.webtools.tapestry.core.util.Constants;
import net.atos.webtools.tapestry.core.util.ErrorMessages;
import net.atos.webtools.tapestry.core.util.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Represents a Tapestry "feature" (i.e. a Component, a Page...) that the user is currently editing.
 * 
 * This is different from a Component that would be loaded from the classpath, and proposed for 
 * auto-completion 
 * 
 * @author a160420
 * @see ComponentModel
 *
 */
public class EditedFeatureModel {
	/**
	 * Reference to the project model: this has info on the Tapestry Eclipse project that owns this feature
	 */
	private ProjectModel projectModel;

	/**
	 * Name of the feature (typically, the file name without extension)
	 */
	private String name;

	/**
	 * javaIType of the file that was initially opened by the user 
	 * (so it's barely part of the model, but it's convenient to have it here)
	 */
	private FileType initialType;
	
	/**
	 * reference to the java "file" (in fact a IStorage is more general) for the feature
	 */
	private IStorage javaFile;
	
	/**
	 * reference to the java "file" (in fact a IStorage is more general) for the feature
	 */
	private IClassFile classFile;
	
	
	/**
	 * reference to the tml "file" (in fact a IStorage is more general) for the feature
	 * This file is not mandatory for a feature, so it can be null, and this feature only has a Java file 
	 */
	private IStorage tmlFile;


	/**
	 * Message catalog files, with name finishing by <code>.properties</code>
	 * It contains messages that can be called directly in tml files
	 */
	private IStorage messageCatalogFile;
	
	/**
	 * List the java properties name that are in the Java file, and are accessible in the tml file
	 */
	private Set<JavaElement> javaProperties = new HashSet<JavaElement>();
	/**
	 * List the java methods name that are in the Java file, and are accessible in the tml file
	 */
	private Set<JavaElement> javaMethods = new HashSet<JavaElement>();
	
	/**
	 * List the messages found in the bundle (*.properties files), if any;
	 */
	private Set<JavaElement> messages;

	private String className;
	private String packageName;
	private IType javaIType;
	

	//-----------------------------------------------------------------------------------------
	//
	//								getters/setters/construct:
	//
	//-----------------------------------------------------------------------------------------
	
	

	public ProjectModel getProjectModel() {
		return projectModel;
	}

	public String getName() {
		return name;
	}

	public FileType getInitialType() {
		return initialType;
	}

	public IStorage getJavaFile() {
		return javaFile;
	}
	
	public IClassFile getClassFile() {
		return classFile;
	}

	public IStorage getTmlFile() {
		return tmlFile;
	}

	public Set<JavaElement> getMessages() {
		return messages;
	}

	public Set<JavaElement> getJavaProperties() {
		return javaProperties;
	}

	public Set<JavaElement> getJavaMethods() {
		return javaMethods;
	}

	public String getNamespace(){
		return Constants.DEFAULT_NAMESPACE;
	}
	
	public String getParameterNamespace(){
		return Constants.DEFAULT_PARAM_NAMESPACE;
	}
	/**
	 * 
	 * @return the {@link IType} of the java class
	 */
	public IType getJavaIType() {
		return javaIType;
	}

	/**
	 * Constructs the whole model given, one of the "files" (tml or Java)
	 * It constructs the feature model, and also loads the {@link ProjectModel}  
	 * 
	 * @param initialStorage: one of the file for the feature
	 * 
	 * @see ProjectModel#ProjectModel(IFile)
	 */
	public EditedFeatureModel(IStorage initialStorage, boolean forceReload) {
		if(initialStorage == null){
			throw new IllegalArgumentException("initialStorage can't be null");
		}
		//Only executed if the storage is in the workspace (i.e. while opening a java/tml file in a project)
		//but not when trying to open an external class/tml (i.e. in a classpath jar)
		if(initialStorage instanceof IResource){
			projectModel = TapestryCore.getDefault().getProjectModel(((IResource) initialStorage).getProject(), forceReload);
		}
		else if(initialStorage instanceof IJarEntryResource){
			IJarEntryResource jarEntryFile = (IJarEntryResource) initialStorage;
			projectModel = TapestryCore.getDefault().getProjectModel(jarEntryFile.getPackageFragmentRoot().getJavaProject().getProject(), forceReload);
			
		}
		else if(initialStorage instanceof IClassFile){
			
		}
		
		String openedFileExtension = initialStorage.getFullPath().getFileExtension();
		String openedFileName = initialStorage.getName();
		
		if(openedFileExtension != null && openedFileExtension.length() != 0){
			name = openedFileName.substring(0, openedFileName.length() - openedFileExtension.length() - 1);
		}
		else{
			name = openedFileName;
		}
		
		Object correspondingFile = findCorrespondingFile(initialStorage);
		
		//CASE-1: User has opened the Java File
		if(openedFileExtension != null 
				&& (openedFileExtension.equalsIgnoreCase(FileType.JAVA.getExtension()) 
						|| openedFileExtension.equalsIgnoreCase(FileType.CLASS.getExtension()))){
			initialType = FileType.JAVA;
			
			javaFile = initialStorage;
			if(correspondingFile instanceof IStorage 
					&& correspondingFile instanceof IResource && ((IResource)correspondingFile).exists()){
				tmlFile = (IStorage) correspondingFile;
			}
		}
		//CASE-2: User has opened the tml file (or any other kind of file, that is then considered the tml file) 
		else{
			initialType = FileType.TML;
			
			tmlFile = initialStorage;
			if(correspondingFile instanceof IStorage 
					&& correspondingFile instanceof IResource && ((IResource)correspondingFile).exists()){
				javaFile = (IStorage)correspondingFile;
			}
		}
		
		loadJavaType();
		
	}
	
	public EditedFeatureModel(IClassFile initialStorage, boolean forceReload) {
		if(initialStorage == null){
			throw new IllegalArgumentException("initialStorage can't be null");
		}
		projectModel = TapestryCore.getDefault().getProjectModel(initialStorage.getJavaProject().getProject(), forceReload);
		
		final String openedFileExtension = "class";
		String openedFileName = initialStorage.getElementName();
		
		name = openedFileName.substring(0, openedFileName.length() - openedFileExtension.length() - 1);
		
		Object correspondingFile = findCorrespondingFile(initialStorage);
		
		if(openedFileExtension.equalsIgnoreCase(FileType.JAVA.getExtension()) 
						|| openedFileExtension.equalsIgnoreCase(FileType.CLASS.getExtension())){
			initialType = FileType.CLASS;
			
			classFile = initialStorage;
			if(correspondingFile instanceof IStorage 
					&& correspondingFile instanceof IResource && ((IResource)correspondingFile).exists()){
				tmlFile = (IStorage) correspondingFile;
			}
		}
		
		loadJavaType();
		
	}
	
	//-----------------------------------------------------------------------------------------
	//
	//								find other file:
	//
	//-----------------------------------------------------------------------------------------
	/**
	 * Finds the tml file given the Java file and vice-versa
	 * 
	 * Also sets the "Component Message Catalog" (property file with messages)
	 * 
	 * @param openedStorage
	 * @return
	 */
	private IResource findCorrespondingFile(IStorage openedStorage) {
		IResource otherFile = null;
		
		if(openedStorage instanceof IResource){
			IResource openedFile = (IResource) openedStorage;
			
			//get path
			IPath otherFileInDirPath = changeExtension(openedFile.getFullPath());
			IPath messageCatalogInDirPath = openedFile.getFullPath().removeFileExtension().addFileExtension(PROPERTY.getExtension());
			
			// 1 - Search in the same dir
			otherFile = openedFile.getParent().findMember(otherFileInDirPath);
			IResource messageCatalogFound = openedFile.getProject().getParent().findMember(messageCatalogInDirPath);
			if(messageCatalogFound instanceof IStorage && messageCatalogFound.exists()){
				messageCatalogFile = (IStorage) messageCatalogFound;
			}
			
			//If they are all found in the same dir/package, it's done
			if(otherFile != null && messageCatalogFile != null){
				return otherFile;
			}
			
			// 2 - Search in project
			if (projectModel != null && projectModel.getJavaProject() != null) {
				try {
					IPackageFragmentRoot[] allPackageFragmentRoots = projectModel.getJavaProject().getAllPackageFragmentRoots();
					//a - looking for the source dir containing the opened file, to have the relative path
					IPath otherFileRelativePath = null;
					IPath messageCatalogRelativePath = null;
					
					for (IPackageFragmentRoot packageFragmentRoot : allPackageFragmentRoots) {
						if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE 
								&& packageFragmentRoot.getPath().isPrefixOf(openedFile.getFullPath())) {
							
							int srcDirSgmentCount = packageFragmentRoot.getPath().segmentCount();
							otherFileRelativePath = otherFileInDirPath.removeFirstSegments(srcDirSgmentCount);
							messageCatalogRelativePath = messageCatalogInDirPath.removeFirstSegments(srcDirSgmentCount);
							break;
						}
					}
					
					//b - looking for the source dir containing the other file
					if(otherFileRelativePath != null){
						for (IPackageFragmentRoot packageFragmentRoot : allPackageFragmentRoots) {
							if(packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE){
								if (otherFile == null) {
									otherFile = openedFile.getProject().getParent().findMember(packageFragmentRoot.getPath().append(otherFileRelativePath));
								}
								if (messageCatalogFile == null) {
									IPath relPath = packageFragmentRoot.getPath().append(messageCatalogRelativePath);
									messageCatalogFound = openedFile.getProject().getParent().findMember(relPath);
									if(messageCatalogFound instanceof IStorage && messageCatalogFound.exists()){
										messageCatalogFile = (IStorage) messageCatalogFound;
									}
								}
								
							}
							if(otherFile != null && messageCatalogFile != null){
								break;
							}
						}
					}
				} 
				catch (JavaModelException e) {
					TapestryCore.logError(ErrorMessages.CAN_T_FIND_CORRESPONDING_FILE_IN_PROJECT_SOURCE_S_DIRECTORIES, e);
				}
			}
		}
		else if(openedStorage instanceof IJarEntryResource){
			IJarEntryResource jarEntry = (IJarEntryResource) openedStorage;
			if(jarEntry.isFile()){
				String classFileName = jarEntry.getName().replace(".tml", ".class");
				String messageFileName = jarEntry.getName().replace(".tml", ".properties");
				
				if(jarEntry.getName().endsWith(".tml") && jarEntry.getParent() instanceof IPackageFragment){
					IPackageFragment packageFragment = (IPackageFragment) jarEntry.getParent();
					classFile = packageFragment.getClassFile(classFileName);

					messageCatalogFile = getNonJavaFile(packageFragment, messageFileName);
				}
			}
		}
		return otherFile;
	}
	
	private IResource findCorrespondingFile(IClassFile classFile) {
		IPackageFragment packageFragment = (IPackageFragment) classFile.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		
		String tmlFileName = classFile.getElementName().replace(".tml", ".class");
		String messageFileName = classFile.getElementName().replace(".tml", ".properties");
		
		tmlFile = getNonJavaFile(packageFragment, tmlFileName);
		messageCatalogFile = getNonJavaFile(packageFragment, messageFileName);
		
		return null;
	}
	
	private IStorage getNonJavaFile(IPackageFragment packageFragment, String fileName){
		try {
			for(Object object : packageFragment.getNonJavaResources()){
				if(object instanceof IJarEntryResource){
					IJarEntryResource nonJavaJarEntry = (IJarEntryResource) object;
					if( nonJavaJarEntry.getName().equalsIgnoreCase(fileName)){
						return nonJavaJarEntry;
					}
				}
			}
		}
		catch (JavaModelException e) {
			TapestryCore.logWarning(ErrorMessages.CAN_T_LOOK_FOR_FEATURE_S_MESSAGES, e);
		}
		return null;
	}
	
	/**
	 * <p>Swap between .tml and .java extensions of the path. 
	 * If the incoming path has none of these extension, it will remain unchanged. 
	 * 
	 * 
	 * @param path the path that will change its extension
	 * @return
	 */
	private IPath changeExtension(IPath path){
		IPath otherFilePath = null;
		if (path.getFileExtension().equals(JAVA.getExtension())) {
			otherFilePath = path.removeFileExtension().addFileExtension(TML.getExtension());
		} 
		else {
			otherFilePath = path.removeFileExtension().addFileExtension(JAVA.getExtension());
		}
		
		return otherFilePath;
	}
	
	/**
	 * <p>AST parsing of the .java file, if it exists (and it was found), to find it's javaIType: 
	 * <li>AST is used to extract class name & package name
	 * <li>Uses {@link IJavaProject#findType(String, IProgressMonitor)} to load the {@link IType}
	 * 
	 * <p>In the case of a compiled class, we ignore this point (not a problem, as we can't edit these files: 
	 * no completion needed)
	 */
	private void loadJavaType() {
		if(getJavaFile() != null){
			String classContent;
			try {
				classContent = inputStream2String(getJavaFile().getContents());
				ASTParser parser = ASTParser.newParser(AST.JLS3);
				parser.setSource(classContent.toCharArray());
				parser.setKind(ASTParser.K_COMPILATION_UNIT);
				final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
				
				cu.accept(new ASTVisitor() {
					@Override
					public void endVisit(PackageDeclaration node) {
						packageName = node.getName().getFullyQualifiedName();
					}
					@Override
					public void endVisit(TypeDeclaration node) {
						className = node.getName().getFullyQualifiedName();
					}
				});
				
				if(className != null){
					javaIType = projectModel.getJavaProject().findType(packageName + "." + className, (IProgressMonitor) null);
				}
			}
			catch (CoreException e) {
				TapestryCore.logError("Was not able to load the java class javaIType", e);
			}
		}
	}
	
	//-----------------------------------------------------------------------------------------
	//
	//							Java Class parsing (finding fields/methods with AST):
	//
	//-----------------------------------------------------------------------------------------
	/**
	 * <p>Loads the following info from source (if resource is available & has been set):
	 * 
	 * <li>java properties
	 * <li>java public getter methods
	 * <li>messages from application
	 * <li>messages from the edited feature
	 * 
	 */
	public void initJavaFields(){
		//1- Load java properties & getter methods:
		if(getJavaFile() != null){
			try {
				javaProperties = new HashSet<JavaElement>();
				javaMethods = new HashSet<JavaElement>();
				
				parseClassPropertiesWithAST(inputStream2String(getJavaFile().getContents()));
			} 
			catch (CoreException e) {
				TapestryCore.logError(ErrorMessages.CAN_T_GET_CONTENT_OF_JAVA_FILE_FOR_AST_PARSING, e);
			}
			
		}
		
		//2- Load messages...
		messages = new HashSet<JavaElement>();
		//...from project message catalog:
		if(projectModel != null && projectModel.getAppMessageCatalogFile() != null){
			loadPropertFiles(projectModel.getAppMessageCatalogFile());
		}
		//... from feature catalog file:
		if(messageCatalogFile != null){
			loadPropertFiles(messageCatalogFile);
		}
	}
	
	/**
	 * <p>Uses an ASTVisitor to parse the java file, and list properties and methods (with their javadoc):
	 * 
	 * <p>Following properties are loaded:
	 * <li>field with annotation @Property (except if parameter "read = false")
	 * <li>"field" deduced from public getter name (starting with "get" or "is" when they return boolean)
	 * 
	 * @param classContent
	 */
	private void parseClassPropertiesWithAST(String classContent) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(classContent.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(new ASTVisitor() {
	
			private String elNodeName;
			private boolean intoEL;
	
			public void endVisit(FieldDeclaration node) {
				elNodeName = "";
				intoEL = false;
				String javadoc = extractJavadocString(node.getJavadoc());
				
				node.accept(new ASTVisitor() {
					
					public void endVisit(MarkerAnnotation node) {
						intoEL = intoEL | node.getTypeName().toString().equals(Constants.ANNOTATION_PROPERTY);
						super.endVisit(node);
					}
	
					public void endVisit(NormalAnnotation node) {
						intoEL = intoEL | node.getTypeName().toString().equals(Constants.ANNOTATION_PROPERTY);
						
						List<?> values = node.values();
						for (int i = 0; i < values.size(); i++) {
							MemberValuePair pair = (MemberValuePair) values.get(i);
							if (node.getTypeName().toString().equals(Constants.ANNOTATION_PROPERTY)
									&& pair.getName().toString().equals("read") && pair.getValue().toString().equals("false")){
								intoEL = false;
							}
						}
						super.endVisit(node);
					}
	
					public void endVisit(VariableDeclarationFragment node) {
						elNodeName = node.getName().toString();
						super.endVisit(node);
					}
				});
				
				super.endVisit(node);
				if (intoEL){
					javaProperties.add(new JavaElement(elNodeName, javadoc));
				}
			}
	
			public boolean visit(MethodDeclaration node) {
				SimpleName name = node.getName();
				String methodName = name.toString();
				String javadoc = node.getJavadoc() != null? node.getJavadoc().toString() : "";
				
				if (node.getModifiers() == Modifier.PUBLIC
						&& methodName.startsWith("get")
						&& methodName.length() > 3) {
					
					
					
					String propName = getPropertyName(methodName.substring(3));
					javaProperties.add(new JavaElement(propName, javadoc));
				}
	
				if (node.getReturnType2().isPrimitiveType()) {
					PrimitiveType type = (PrimitiveType) node.getReturnType2();
					if (type.getPrimitiveTypeCode() == PrimitiveType.BOOLEAN
							&& node.getModifiers() == Modifier.PUBLIC
							&& methodName.startsWith("is")
							&& methodName.length() > 2) {
						String propName = getPropertyName(methodName.substring(2));
						javaProperties.add(new JavaElement(propName, javadoc));
					}
				}
				return false;
			}
	
			private String getPropertyName(String name) {
				if (name.length() > 1){
					return name.substring(0, 1).toLowerCase() + name.substring(1);
				}
				else{
					return name.toLowerCase();
				}
			}
		});
	}

	/**
	 * <p>Load a property file, and add its property/key to the {@link #messages} Set
	 * 
	 * <p>It automatically add "message:" binding to the property name
	 * 
	 * @param file
	 */
	private void loadPropertFiles(IStorage file){
		try{
			Properties messagesProperties = new Properties();
			messagesProperties.load(file.getContents());
			Set<String> stringPropertyNames = messagesProperties.stringPropertyNames();
			for (String propertyName : stringPropertyNames) {
				messages.add(new JavaElement(Constants.MESSAGE_BINDING + propertyName, messagesProperties.getProperty(propertyName)));
			}
		}
		catch (IOException e) {
			TapestryCore.logError(ErrorMessages.CAN_T_LOAD_PROPERTIES_FROM + messageCatalogFile.getFullPath(), e);
		}
		catch (CoreException e) {
			TapestryCore.logError(ErrorMessages.CAN_T_OPEN_PROPERTY_FILE + messageCatalogFile.getFullPath(), e);
		}
	}
	
	/**
	 * <p>Converts a {@link Javadoc} element to String
	 * 
	 * <p> it removes '*' and replace newlines by "&lt;br/&gt;"
	 * 
	 * @param javadoc
	 * @return
	 */
	private String extractJavadocString(Javadoc javadoc){
		if(javadoc == null){
			return Messages.NO_JAVADOC;
		}
		StringBuilder sb = new StringBuilder();
		for(Object tag : javadoc.tags()){
			if(tag instanceof TagElement){
				sb.append(tag.toString().replace('*', ' ')).append("<br/>");
			}
		}
		return sb.toString().replaceAll("\n", "<br/>");
	}

	/**
	 * Converts {@link InputStream} to String
	 * 
	 * @param ins
	 * @return
	 */
	private String inputStream2String(InputStream ins) {
		String all_content = "";
		try {
			ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
			byte[] str_b = new byte[1024];
			int i = -1;
			while ((i = ins.read(str_b)) > 0) {
				outputstream.write(str_b, 0, i);
			}
			all_content = outputstream.toString();
		} 
		catch (IOException e) {
			TapestryCore.logError(ErrorMessages.CAN_T_READ_JAVA_FILE_FOR_AST_PARSING, e);
		}
		return all_content;
	}
}
