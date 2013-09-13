package net.atos.webtools.tapestry.ui.editors.proposal;

public class ProposalModel {
	
	private String name;
	
	private String prefix;
	
	private String suffix;
	
	private String fullName;
	
	public String getName() {
		return name;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getSuffix() {
		return suffix;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public int getCursorPosition() {
		String startName = prefix + name;
		return startName.length();
	}
	
	public ProposalModel(String name, String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
		this.name = name;
		
		fullName = prefix + name + suffix;
	}
	
	public ProposalModel(String name, String prefix) {
		this.name = name;
		this.prefix = prefix;
		
		fullName = prefix + name;
	}
	
	public ProposalModel(String name) {
		this.name = name;
		
		fullName = name;
	}
}
