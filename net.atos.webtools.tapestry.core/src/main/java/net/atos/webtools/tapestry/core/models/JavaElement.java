package net.atos.webtools.tapestry.core.models;

/**
 * Simple model for any java element that could be further proposed for auto-completion, hover
 * or anything else.
 * 
 * It's not the model of a component or a mixin, but rather something that is already pre-processed,
 * a class holder for a few simple properties that will be used.
 * 
 * @author a160420
 *
 */
public class JavaElement{
	private String name;
	private String javadoc;
	
	private String label;
	
	
	public String getLabel() {
		return label;
	}

	
	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}


	public String getJavadoc() {
		return javadoc;
	}

	/**
	 * construct that sets name & javadoc
	 * 
	 * @param name
	 * @param javadoc
	 */
	public JavaElement(String name, String javadoc) {
		this.name = name;
		this.javadoc = javadoc;
		this.label = name;
	}
	
	/**
	 * also set some "origin", that is the location from this properties for information
	 * 
	 * @param name
	 * @param javadoc
	 * @param origin
	 */
	public JavaElement(String name, String javadoc, String origin) {
		this(name, javadoc);
		this.javadoc = this.javadoc + "<p><b>From:</b> " + origin;
		this.label = name + " - " + origin;
	}
	
	/**
	 * also set a label, if we want to show something more meaningful than the name 
	 * (like the fqn, or the signature of a method) 
	 * 
	 * @param name
	 * @param javadoc
	 * @param origin
	 * @param label
	 */
	public JavaElement(String name, String javadoc, String origin, String label) {
		this(name, javadoc, origin);
		this.label = label;
	}
	
	@Override
	public int hashCode() {
		return label.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof JavaElement){
			JavaElement theProp = (JavaElement) obj;
			return (this.label == null) ? theProp.label == null : this.label.equals(theProp.label); 
		}
		return super.equals(obj);
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JavaElement [name=");
		builder.append(name);
		builder.append(", label=");
		builder.append(label);
		builder.append("]");
		return builder.toString();
	}
	
	
}