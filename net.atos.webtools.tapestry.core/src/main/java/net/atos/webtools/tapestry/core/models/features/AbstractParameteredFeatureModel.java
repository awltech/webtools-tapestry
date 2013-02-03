package net.atos.webtools.tapestry.core.models.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.atos.webtools.tapestry.core.TapestryCore;
import net.atos.webtools.tapestry.core.models.ProjectModel;
import net.atos.webtools.tapestry.core.util.Constants;
import net.atos.webtools.tapestry.core.util.ErrorMessages;
import net.atos.webtools.tapestry.core.util.Messages;
import net.atos.webtools.tapestry.core.util.helpers.JavaModelHelper;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Specification of {@link AbstractFeatureModel} when they have parameters.
 * 
 * @author A160420
 *
 */
public class AbstractParameteredFeatureModel extends AbstractFeatureModel{
	/**
	 * All parameters that are NOT of type Block
	 */
	private Set<Parameter> parameters = new HashSet<Parameter>();
	/**
	 * All parameters of type Block
	 */
	private Set<Parameter> blockParameters = new HashSet<Parameter>();
	
	/**
	 * Mandatory parameters: these params can also be in {@link #parameters}
	 */
	private Set<Parameter> mandatoryParameters;
	
	public Set<Parameter> getMandatoryParameters() {
		if(mandatoryParameters == null){
			mandatoryParameters = new HashSet<Parameter>();
			for (Parameter parameter : parameters) {
				if(parameter.isMandatory()){
					mandatoryParameters.add(parameter);
				}
			}
		}
		return mandatoryParameters;
	}
	
	public Set<Parameter> getParameters() {
		return parameters;
	}
	
	public Set<Parameter> getBlockParameters() {
		return blockParameters;
	}
	
	/**
	 * Seldom used constructor (for statically defined features only)
	 */
	AbstractParameteredFeatureModel() {
	}
	
	/**
	 * Standard constructor with parameters
	 * 
	 * @param prefix
	 * @param type
	 * @param projectModel
	 * @param source
	 * @param subPackage
	 */
	public AbstractParameteredFeatureModel(String prefix, IType type, ProjectModel projectModel, String source,
			String subPackage) {
		super(prefix, type, projectModel, source, subPackage);
		fillParameters(type);
	}

	/**
	 * Scan the type of the feature to set its {@link Parameter}s.
	 * 
	 * <p>First adds the parameter defined in the class: fields that have the 
	 * <code>org.apache.tapestry5.annotations.Parameter</code> annotation.
	 * 
	 * <p>Then, <b>for components</b>, it searches for published parameters (with publishedParameters param in the annotation) 
	 * from the class, and from the mixins.
	 * 
	 * <p>Simple & publishedParameters from the class are extracted using 
	 * {@link #extractOneParam(IAnnotation, IField, IType, List)}.
	 * 
	 * <p>publishedParameters from the mixins are copied from the mixin: that's why all mixins must have been initialized 
	 * before we start to set components.  
	 *  
	 * 
	 * @param type the IType of the feature
	 */
	protected void fillParameters(IType type) {
		IField[] fields;
		try {
			fields = type.getFields();
			
			for (IField field : fields) {
				//CASE-1: simple parameters:					
				IAnnotation paramAnnotation = JavaModelHelper.getAnnotation(field, Constants.TAPESTRY5_PARAMETER);
				if(paramAnnotation != null){
					extractOneParam(paramAnnotation, field, type, null);
				}
				
				//CASE-2: published parameters:
				IAnnotation componentAnnotation = JavaModelHelper.getAnnotation(field, Constants.TAPESTRY5_COMPONENT);
				if(componentAnnotation != null){
					IType innerComponentType = JavaModelHelper.getFieldType(type, field);
					if(innerComponentType != null && innerComponentType.exists()){
						List<String> publishedParams = new ArrayList<String>();
						for (IMemberValuePair memberValuePair : componentAnnotation.getMemberValuePairs()) {
							if(memberValuePair.getMemberName() != null 
									&& memberValuePair.getMemberName().equals(Constants.TAPESTRY5_PUBLISH_PARAMETERS)
									&& memberValuePair.getValueKind() ==  org.eclipse.jdt.core.IMemberValuePair.K_STRING){
								String publishedParamString = (String) memberValuePair.getValue();
								List<String> publishedParamList = Arrays.asList(publishedParamString.split(","));
								//trim on every param, to manage spaces
								for (String param : publishedParamList) {
									publishedParams.add(param.trim());
								}
							}
						}
						
						if(publishedParams.size()>0){
							//CASE2-1: published param from the same component class
							IField[] innerTypeFields = innerComponentType.getFields();
							for (IField innerTypeField : innerTypeFields) {
								IAnnotation innerParamAnnotation = JavaModelHelper.getAnnotation(innerTypeField, Constants.TAPESTRY5_PARAMETER); 
								if(innerParamAnnotation != null){
									extractOneParam(innerParamAnnotation, innerTypeField, innerComponentType, publishedParams);
								}
							}
							//CASE 2-2: published parameters from mixins:
							IAnnotation mixinsAnnotation = JavaModelHelper.getAnnotation(field, Constants.TAPESTRY5_MIXINS);
							if(mixinsAnnotation != null && mixinsAnnotation.exists()){
								mixinsAnnotation.getJavaModel().open(null);
								
								
								IMemberValuePair[] memberValuePairs = mixinsAnnotation.getMemberValuePairs();
								
								List<MixinModel> definedMixins = new ArrayList<MixinModel>();
								for(MixinModel mixin : getProjectModel().getMixins()){
									for(IMemberValuePair valuePair : memberValuePairs){
										//When the mixins defined in @Mixins are found in projectModel:
										if(valuePair.getMemberName().equals("value") && valuePair.getValueKind() == IMemberValuePair.K_STRING){
											//simple element ( @Mixins(value="Autocomplete") )
											if(valuePair.getValue().equals(mixin.getName())){
												definedMixins.add(mixin);
											}
											//table element ( @Mixins(value={"Autocomplete"}) )
											else if(valuePair.getValue() instanceof Object[]){
												Object[] values = (Object[]) valuePair.getValue();
												for (Object value : values) {
													if(value.equals(mixin.getName())){
														definedMixins.add(mixin);
													}
												}
											}
										}
									}
								}
								
								for (MixinModel mixin : definedMixins) {
									for(Parameter param : mixin.getParameters()){
										if(publishedParams.contains(param.getParamName())){
											parameters.add(param);
											publishedParams.remove(param.getParamName());
										}
									}
									for(Parameter blockParam : mixin.getBlockParameters()){
										if(publishedParams.contains(blockParam.getParamName())){
											blockParameters.add(blockParam);
											publishedParams.remove(blockParam.getParamName());
										}
									}
								}
								
							}
							//if some "publishedParameters" are not corresponding to a param that we find:
							for(String remainingParam : publishedParams){
								parameters.add(new Parameter(this, remainingParam, false, false, false, 
										Messages.UNKNOWN, Messages.UNKNOWN, false, false, Messages.UNKNOWN_PUBLISHED_PARAMETER, false));
							}
						}
					}
				}
			}
			
			//--------------- recursive call on superclass ----------------
			String superclassName = type.getSuperclassName();
			if(superclassName != null){
				IType superType = projectModel.getJavaProject().findType(superclassName);
				if(superType != null && superType.exists()){
					fillParameters(superType);
				}
			}
		}
		catch (JavaModelException e) {
			TapestryCore.logError(ErrorMessages.CAN_T_LOAD_PARAMETERS_FOR_TYPE + this.getFullName(), e);
		}
	}

	/**
	 * Creates and add a {@link Parameter} from a field and its annotation.
	 * 
	 * <p>params from the <code>org.apache.tapestry5.annotations.Parameter</code> annotation are set.
	 * 
	 * <p>when the type of the field is <code>org.apache.tapestry5.Block</code> it's added to {@link #blockParameters}, 
	 * otherwise, it's added to {@link #parameters}
	 * 
	 * @param paramAnnotation the <code>org.apache.tapestry5.annotations.Parameter</code> annotation
	 * @param field the field corresponding to the parameter
	 * @param type the parent type in which the field is defined
	 * @param publishedParams the list of published parameters of the including component (while adding component OR mixin param)
	 * @throws JavaModelException
	 */
	private void extractOneParam(IAnnotation paramAnnotation, IField field, IType type, List<String> publishedParams) throws JavaModelException {
		//------------- DEFAULT VALUES ------------------------
		//param name is the field name... 
		String paramName = field.getElementName();
		boolean required = false;
		
		boolean allowNull = true;
		boolean cache = true;
		
		String value = "";
		String defaultPrefix = Constants.PROP_BINDING;
		
		boolean principal = false;
		boolean autoconnect = false;
		
		//------------ Values defined in Annotation -----------------
		IMemberValuePair[] memberValuePairs = paramAnnotation.getMemberValuePairs();
		for (IMemberValuePair memberValuePair : memberValuePairs) {
			String memberName = memberValuePair.getMemberName();
			//...param name can be overridden by annotation:
			if(memberName.equals("name")) {
				paramName = (String) memberValuePair.getValue();
			}
			else if(memberName.equals("required")) {
				required = (Boolean) memberValuePair.getValue();
			}
			else if(memberName.equals("allowNull")) {
				allowNull = (Boolean) memberValuePair.getValue();
			}
			else if(memberName.equals("cache")) {
				cache = (Boolean) memberValuePair.getValue();
			}
			else if(memberName.equals("value")) {
				value = (String) memberValuePair.getValue();
			}
			else if(memberName.equals("defaultPrefix")) {
				defaultPrefix = (String) memberValuePair.getValue() + ":";
			}
			else if(memberName.equals("principal")) {
				principal = (Boolean) memberValuePair.getValue();
			}
			else if(memberName.equals("autoconnect")) {
				autoconnect = (Boolean) memberValuePair.getValue();
			}
		}
		
		//searches for a method in the component, that loads a default value for the parameter (named defaultMyParam())
		boolean hasCalculatedDefault = false;
		IMethod method = type.getMethod("default" + paramName.substring(0, 1).toUpperCase() + paramName.substring(1), null);
		if(method != null && method.exists()){
			hasCalculatedDefault = true;
		}
		
		String paramJavadoc = JavaModelHelper.loadJavadoc(field);
		if(publishedParams == null || publishedParams.contains(paramName)){
			if(field.getTypeSignature().equals(Constants.TAPESTRY5_BLOCK_SIGNATURE)){
				blockParameters.add(
						new Parameter(this, paramName, required, allowNull, cache, value, defaultPrefix, principal, autoconnect, paramJavadoc, hasCalculatedDefault));
			}
			else{
				parameters.add(
						new Parameter(this, paramName, required, allowNull, cache, value, defaultPrefix, principal, autoconnect, paramJavadoc, hasCalculatedDefault));
			}
			if(publishedParams != null){
				publishedParams.remove(paramName);
			}
		}
	}

	/**
	 * For debugging...
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ComponentModel [prefix=");
		builder.append(prefix);
		builder.append(", name=");
		builder.append(getName());
		builder.append(", source=");
		builder.append(source);
		builder.append(", parameters=");
		builder.append(parameters);
		builder.append(", blockParameters=");
		builder.append(blockParameters);
		builder.append(", mandatoryParameters=");
		builder.append(mandatoryParameters);
		builder.append(", javaProject=");
		builder.append(projectModel.getJavaProject() != null?projectModel.getJavaProject().getElementName():"null");
		builder.append("]");
		return builder.toString();
	}
	
}
