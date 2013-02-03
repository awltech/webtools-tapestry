package net.atos.webtools.tapestry.core.models.features;

import java.util.HashSet;

/**
 * Class that represents a standard Tapestry parameter, i.e. a parameter for a {@link AbstractParameteredFeatureModel},
 * which is a Component ({@link ComponentModel}), or a Mixin ({@link MixinModel}).
 * 
 * It includes its name and javadoc and some Tapestry options that are just used for display in the javadoc.
 * 
 * @author a160420
 *
 */
public class Parameter{
	/**
	 * Every Parameter is contained in a "feature":
	 */
	private final AbstractParameteredFeatureModel parentFeature;

	/**
	 * The displayed javadoc has more information than the one found,
	 * as we also display some info coming from code
	 */
	private static final String paramJavadocTemplate = "<b>%s<br/><br/></b>" +
	"%s<br/><br/>" +
	"<b>required:</b> %s<br/>" +
	"<b>allowNull:</b> %s<br/>" +
	"<b>cache:</b> %s<br/>" +
	"<b>value:</b> %s<br/>" +
	"<b>defaultPrefix:</b> %s<br/>" +
	"<b>principal:</b> %s<br/>" +
	"<b>autoconnect:</b> %s<br/>";
	
	/**
	 * parameter name, that must be typed in tml code
	 */
	private String paramName;
	
	/**
	 * is the parameter "required"?
	 * This is only the value found for this param in the code, so this is Tapestry point-of-view
	 * of "required".
	 * That means that, it must be provided at some point, but not necessarily by an XML attribute in the tml.
	 * It could also come from a "default" method...
	 * 
	 * @see {@link Parameter#isMandatory() }
	 */
	private boolean required;
	private String paramJavadoc;
	private boolean allowNull;
	private boolean cache;
	private String value;
	private String defaultPrefix;
	private boolean principal;
	private boolean autoconnect;
	/**
	 * true if the parameter has a corresponding method that set a default value for it
	 */
	private boolean hasCalculatedDefault;
	
	public AbstractParameteredFeatureModel getParentFeature() {
		return parentFeature;
	}

	public String getParamName() {
		return paramName;
	}
	
	public String getQualifiedName(String namespace){
		return namespace + ":" + paramName;
	}

	public boolean isRequired() {
		return required;
	}
	
	public String getJavadoc() {
		return String.format(paramJavadocTemplate, 
								paramName, 
								paramJavadoc,
								required,
								allowNull,
								cache,
								value,
								defaultPrefix,
								principal,
								autoconnect);
	}
	
	public boolean isAllowNull() {
		return allowNull;
	}

	public boolean isCache() {
		return cache;
	}

	public String getValue() {
		return value;
	}

	public String getDefaultPrefix() {
		return defaultPrefix;
	}

	public boolean isPrincipal() {
		return principal;
	}

	public boolean isAutoconnect() {
		return autoconnect;
	}

	/**
	 * Simple constructor
	 * 
	 * @param componentModel
	 * @param paramName
	 * @param required
	 * @param allowNull
	 * @param cache
	 * @param value
	 * @param defaultPrefix
	 * @param principal
	 * @param autoconnect
	 * @param paramJavadoc
	 * @param hasCalculatedDefault
	 */
	public Parameter(AbstractParameteredFeatureModel componentModel, String paramName, 
						boolean required, 
						boolean allowNull, 
						boolean cache, 
						String value, 
						String defaultPrefix, 
						boolean principal, 
						boolean autoconnect, 
						String paramJavadoc,
						boolean hasCalculatedDefault) {
		parentFeature = componentModel;
		this.paramName = paramName;
		this.required = required;
		this.allowNull = allowNull;
		this.cache = cache;
		this.value = value;
		this.defaultPrefix = defaultPrefix;
		this.principal = principal;
		this.autoconnect = autoconnect;
		this.paramJavadoc = paramJavadoc;
		this.hasCalculatedDefault = hasCalculatedDefault;
	}

	/**
	 * Check if a parameter is required without (dafult) value parameter, nor paramDefault() method
	 * 
	 * @return true if the parameter is mandatory in the syntax
	 */
	boolean isMandatory() {
		return this.required && (this.value == null || this.value.length() == 0) && ! this.hasCalculatedDefault;
	}
	
	
	/**
	 * For debugging...
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Parameter [paramName=");
		builder.append(paramName);
		builder.append(", required=");
		builder.append(required);
		builder.append(", allowNull=");
		builder.append(allowNull);
		builder.append(", cache=");
		builder.append(cache);
		builder.append(", value=");
		builder.append(value);
		builder.append(", defaultPrefix=");
		builder.append(defaultPrefix);
		builder.append(", principal=");
		builder.append(principal);
		builder.append(", autoconnect=");
		builder.append(autoconnect);
		builder.append(", hasCalculatedDefault=");
		builder.append(hasCalculatedDefault);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Used to avoid duplicated parameters: they're added in a {@link HashSet}, so this method
	 * is used to assure unicity
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Parameter){
			return ((Parameter)obj).getParamName().equalsIgnoreCase(this.getParamName());
		}
		return super.equals(obj);
	}
	
	/**
	 * hashCode method is important to ensure unicity in the Set
	 */
	@Override
	public int hashCode() {
		return this.getParamName().toLowerCase().hashCode();
	}
}