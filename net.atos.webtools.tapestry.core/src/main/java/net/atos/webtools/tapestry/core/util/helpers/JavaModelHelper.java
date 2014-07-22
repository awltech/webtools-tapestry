package net.atos.webtools.tapestry.core.util.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import net.atos.webtools.tapestry.core.TapestryCore;
import net.atos.webtools.tapestry.core.models.JavaElement;
import net.atos.webtools.tapestry.core.util.Constants;
import net.atos.webtools.tapestry.core.util.ErrorMessages;
import net.atos.webtools.tapestry.core.util.Messages;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.ui.JavadocContentAccess;

/**
 * Helper class with static methods to simplify the use of the JDT JavaModel
 * (org.eclipse.jdt.core).
 * 
 * Especially, this methods, should work equally on IType defined in source or
 * compiled classes.
 * 
 * @author a160420
 *
 */
@SuppressWarnings("restriction")
public class JavaModelHelper {
	/**
	 * Used to "reload" all informations of the IType, to be up-to-date before
	 * calling getFields(), getMethods()...
	 * 
	 * @param type
	 */
	public static void reconcile(IType type) {
		try {
			if (type.getCompilationUnit() != null) {
				JavaModelUtil.reconcile(type.getCompilationUnit());
			} else if (type.getClassFile() != null) {
				JavaModelUtil.reconcile(type.getClassFile().getWorkingCopy(
						new WorkingCopyOwner() {/* subclass */
						}, new NullProgressMonitor()));
			}
		} catch (JavaModelException e) {
		}
	}

	/**
	 * Search in containingType, for a public property getter or a public
	 * property annotated with .
	 * 
	 * field or getter must not be static.
	 * 
	 * 
	 * @param containingType
	 * @param propertyType
	 * @return
	 * @throws JavaModelException
	 */
	public static Set<JavaElement> getPublicJavaBeanProperties(
			IType containingType) throws JavaModelException {
		if (containingType == null || !containingType.exists()) {
			return new HashSet<JavaElement>();
		}

		Set<JavaElement> beanProperties;

		// parent:
		IType parentType = getParentType(containingType);
		if (parentType != null
				&& parentType.exists()
				&& !containingType.getFullyQualifiedName().equals(
						Constants.JAVA_OBJECT)) {
			beanProperties = getPublicJavaBeanProperties(parentType);
		} else {
			beanProperties = new HashSet<JavaElement>();
		}

		IMethod[] methods = containingType.getMethods();
		for (IMethod method : methods) {
			if (method != null
					&& method.exists()
					&& !isStatic(method)
					&& isPublic(method)
					&& (method.getParameters() == null || method
							.getParameters().length == 0)) {
				String propertyName = getterProperty(method);
				if (propertyName != null) {
					beanProperties.add(new JavaElement(propertyName,
							JavaModelHelper.loadJavadoc(method), containingType
									.getElementName()));
				}
			}
		}

		IField[] fields = containingType.getFields();
		for (IField field : fields) {
			if (field != null
					&& field.exists()
					&& !isStatic(field)
					&& (isPublic(field) || getAnnotation(field,
							Constants.TAPESTRY5_PROPERTY) != null)) {
				beanProperties.add(new JavaElement(field.getElementName(),
						JavaModelHelper.loadJavadoc(field)));
			}
		}

		return beanProperties;
	}

	public static Set<JavaElement> getPublicMethodsNoGetterNoVoid(
			IType containingType) throws JavaModelException {
		if (containingType == null || !containingType.exists()) {
			return new HashSet<JavaElement>();
		}

		Set<JavaElement> methodsProperties;

		// parent:
		IType parentType = getParentType(containingType);
		if (parentType != null
				&& parentType.exists()
				&& !containingType.getFullyQualifiedName().equals(
						Constants.JAVA_OBJECT)) {
			methodsProperties = getPublicMethodsNoGetterNoVoid(parentType);
		} else {
			methodsProperties = new HashSet<JavaElement>();
		}

		IMethod[] methods = containingType.getMethods();
		for (IMethod method : methods) {
			if (method != null && method.exists() && !isStatic(method)
					&& isPublic(method) && !method.isConstructor()
					&& !method.getReturnType().equalsIgnoreCase("V")
					&& getterProperty(method) == null) {

				StringBuilder methodSignature = new StringBuilder(
						method.getElementName()).append("(");
				String[] parameterNames = method.getParameterNames();
				for (int i = 0; i < parameterNames.length; i++) {
					methodSignature.append(parameterNames[i]);
					if (i < parameterNames.length - 1) {
						methodSignature.append(",");
					}
				}
				methodSignature.append(")");

				String methodLabel = Signature.toString(method.getSignature(),
						method.getElementName(), method.getParameterNames(),
						false, false)
						+ " - " + Signature.toString(method.getReturnType());

				methodsProperties.add(new JavaElement(methodSignature
						.toString(), JavaModelHelper.loadJavadoc(method), null,
						methodLabel));
			}
		}

		return methodsProperties;
	}

	/**
	 * Return the name of the property if it's a getter, null if it's not
	 * 
	 * Manages get & is prefix.
	 * 
	 * Tests for public/not static/no parameter
	 * 
	 * @return
	 * @throws JavaModelException
	 */
	public static String getterProperty(IMethod method)
			throws JavaModelException {
		String propertyName = null;
		if (method != null
				&& method.exists()
				&& !isStatic(method)
				&& isPublic(method)
				&& (method.getParameters() == null || method.getParameters().length == 0)) {
			String methodName = method.getElementName();
			if (methodName.startsWith("get")) {
				propertyName = StringHelper.toFirstLower(methodName
						.substring(3));
			} else if (methodName.startsWith("is")) {
				propertyName = StringHelper.toFirstLower(methodName
						.substring(2));
			}
		}

		return propertyName;
	}

	/**
	 * Returns the IType of the return type, for the getter of this field.
	 * 
	 * If it's not found, returns the field type, named with fieldName in the
	 * containingType.
	 * 
	 * Normally for a javabean, it's the same, but if there's only the getter,
	 * or only the field, it'll work. And if they're both there, but with
	 * inconsistent types, the getter type will be returned (as it's what
	 * Tapestry does when looking for properties).
	 * 
	 * @param containingType
	 * @param fieldName
	 *            the name of the field
	 * @return
	 * @throws JavaModelException
	 */
	public static IType getFieldOrMethodType(IType containingType,
			String fieldName) throws JavaModelException {
		if (fieldName == null || containingType == null) {
			return null;
		}
		IType returnType = null;
		// Case-1: if it's a method call:
		if (fieldName.contains("(") && fieldName.endsWith(")")
				&& fieldName.indexOf('(') < fieldName.lastIndexOf(')')) {
			String methodName = fieldName.substring(0, fieldName.indexOf('('));
			String parameterString = fieldName.substring(
					fieldName.indexOf('(') + 1, fieldName.lastIndexOf(')'));
			// remove sub
			// parameterString = parameterString.replaceAll("(.*)", "");
			int paramCount = 0;
			if (parameterString.trim().length() > 0) {
				paramCount = 1;
			}
			if (parameterString.contains(",")) {
				paramCount = parameterString.split(",").length;
			}
			IMethod method = getMethodAsTapestry(containingType, methodName,
					paramCount);
			String returnTypeSignature = method.getReturnType();

			returnType = getTypeUsedInType(containingType,
					Signature.toString(returnTypeSignature));
		}

		// Case-2: if it's a getter (get or is for boolean):
		if (returnType == null || !returnType.exists()) {
			returnType = getGetterReturnType(containingType,
					containingType.getMethod(
							"get" + StringHelper.toFirstUpper(fieldName),
							new String[] {}));
		}
		if (returnType == null || !returnType.exists()) {
			returnType = getGetterReturnType(containingType,
					containingType.getMethod(
							"is" + StringHelper.toFirstUpper(fieldName),
							new String[] {}));
		}

		// Case-3: field:
		if (returnType == null || !returnType.exists()) {
			returnType = getFieldType(containingType,
					containingType.getField(fieldName));
		}
		if (returnType != null && returnType.exists() && isPublic(returnType)) {
			return returnType;
		}

		// recursive call on the parent class:
		if (!containingType.getFullyQualifiedName().equals(
				Constants.JAVA_OBJECT)) {
			IType parentType = getParentType(containingType);
			return getFieldOrMethodType(parentType, fieldName);
		}
		return null;
	}

	private static IMethod getMethodAsTapestry(IType type, String methodName,
			int paramCount) throws JavaModelException {
		for (IMethod method : type.getMethods()) {
			if (method.getElementName().equalsIgnoreCase(methodName)
					&& method.getParameters().length == paramCount) {
				return method;
			}
		}
		if (!type.getFullyQualifiedName().equals(Constants.JAVA_OBJECT)) {
			IType parentType = getParentType(type);
			return getMethodAsTapestry(parentType, methodName, paramCount);
		}

		return null;
	}

	/**
	 * Returns the IType of the field, when this field is defined in the
	 * containingType. For that, it will get the signature, and convert it to
	 * the type name. Unfortunately, it might be the simple name (for sources -
	 * ICompilationUnit), or fully qualified name (for jar - IclassFile), so we
	 * must use getTypeUsedInType().
	 * 
	 * @param containingType
	 * @param field
	 * @return the type of the field
	 * @throws JavaModelException
	 */
	public static IType getFieldType(IType containingType, IField field)
			throws JavaModelException {
		if (field.exists() && field.getTypeSignature() != null) {
			String typeName = Signature.toString(field.getTypeSignature());
			return getTypeUsedInType(containingType, typeName);
		}
		return null;
	}

	public static IType getGetterReturnType(IType containingType, IMethod method)
			throws JavaModelException {
		if (method != null && method.exists() && method.getReturnType() != null) {
			String typeName = Signature.toString(method.getReturnType());
			return getTypeUsedInType(containingType, typeName);
		}
		return null;
	}

	/**
	 * When a type is used in another type (field type, return type, method
	 * parameter type...), it can be defined with a simple name or a FQE, so
	 * this method will manage both cases with JDT: it will search for the
	 * typeName, in project, and then try to find an import declaration to find
	 * the FQE, and then search for that FQE.
	 * 
	 * @param externalType
	 * @param typeName
	 * @return
	 * @throws JavaModelException
	 */
	public static IType getTypeUsedInType(IType externalType, String typeName)
			throws JavaModelException {
		// Case-1: we have the FQE (normally it's the case for compiled classes)
		// -> we can find it:
		IType fieldType = externalType.getJavaProject().findType(typeName);

		// Case-2: we look into the imports for the FQE (only works for
		// compilation unit - i.e. the source):
		if (fieldType == null || !fieldType.exists()) {
			if (externalType.getCompilationUnit() != null) {
				IImportDeclaration[] imports = externalType
						.getCompilationUnit().getImports();
				for (IImportDeclaration importDecl : imports) {
					if (importDecl.getElementName().endsWith("." + typeName)) {
						fieldType = externalType.getJavaProject().findType(
								importDecl.getElementName());
						break;
					}
				}
			}

			// Case-3: No import when the class is in the same package:
			if (fieldType == null || !fieldType.exists()) {
				fieldType = externalType.getJavaProject().findType(
						externalType.getPackageFragment().getElementName(),
						typeName);

				// Case-4: it's in "java.lang" (no need to be imported):
				if (fieldType == null || !fieldType.exists()) {
					fieldType = externalType.getJavaProject().findType(
							"java.lang", typeName);
				}
			}
		}
		return fieldType;
	}

	/**
	 * Returns the annotation if it exists or null
	 * 
	 * @param member
	 *            IAnnotatable member
	 * @param annotationFQN
	 *            the fully qualified name of the annotation
	 * @return
	 * @throws JavaModelException
	 */
	public static IAnnotation getAnnotation(IAnnotatable member,
			String annotationFQN) throws JavaModelException {
		if (annotationFQN == null || annotationFQN.length() == 0) {
			return null;
		}
		IAnnotation[] annotations = member.getAnnotations();
		for (IAnnotation annotation : annotations) {
			if (annotationFQN.endsWith(annotation.getElementName())) {
				return annotation;
			}
		}

		return null;
	}

	/**
	 * Return the type of the super class
	 * 
	 * <code>java.lang.Object</code> is the parent of all, so it never returns
	 * null
	 * 
	 * @param parentType
	 * @return
	 * @throws JavaModelException
	 */
	public static IType getParentType(IType parentType)
			throws JavaModelException {
		String superclassName = parentType.getSuperclassName();
		if (superclassName == null) {
			superclassName = Constants.JAVA_OBJECT;
		}
		return getTypeUsedInType(parentType, superclassName);
	}

	// ---------------------------------------------------------------------------------------------
	//
	// MODIFIERS
	//
	// ---------------------------------------------------------------------------------------------

	public static boolean isPublic(IMember member) {
		if (member == null) {
			return false;
		}
		int flags;
		try {
			flags = member.getFlags();
		} catch (JavaModelException e) {
			return false;
		}

		IType declaringType = member.getDeclaringType();
		try {
			if (declaringType != null && declaringType.isInterface()) {
				return Flags.isPublic(flags) || Flags.isPackageDefault(flags);
			}
		} catch (JavaModelException e) {
			return false;
		}

		return Flags.isPublic(flags);
	}

	public static boolean isStatic(IMember member) {
		if (member == null) {
			return false;
		}
		int flags;
		try {
			flags = member.getFlags();
		} catch (JavaModelException e) {
			return false;
		}

		return Flags.isStatic(flags);
	}

	// ---------------------------------------------------------------------------------------------
	//
	// JAVADOC
	//
	// ---------------------------------------------------------------------------------------------

	/**
	 * Returns the HTML content of the Javadoc associated with the member. It
	 * must work with javadoc coming from a compilation unit (code is in
	 * worspace), or from a class file, with which a javadoc has been associated
	 * in Eclipse (most of the time by m2e, but also manually, as it makes no
	 * difference here).
	 * 
	 * @param member
	 *            , the element from which we search javadoc
	 * @return the content of the javadoc
	 */
	public static String loadJavadoc(IMember member) {
		StringBuilder classJavadoc = new StringBuilder();
		try {
			Reader htmlContentReader = JavadocContentAccess
					.getHTMLContentReader(member, true, true);
			if (htmlContentReader != null) {
				BufferedReader reader = new BufferedReader(htmlContentReader);
				String line;
				try {
					while ((line = reader.readLine()) != null) {
						classJavadoc.append(line).append("\n");
					}
				} catch (IOException e) {
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
						}
					}
				}
			} else {
				classJavadoc.append(Messages.NO_JAVADOC);
			}
		} catch (JavaModelException e) {
			TapestryCore.logError(
					ErrorMessages.CAN_T_READ_JAVADOC_FOR + member, e);
		}

		return classJavadoc.toString();
	}

}
