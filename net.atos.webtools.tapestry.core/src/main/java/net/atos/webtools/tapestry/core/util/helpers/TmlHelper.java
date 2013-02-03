package net.atos.webtools.tapestry.core.util.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.atos.webtools.tapestry.core.util.Constants;

import org.w3c.dom.Node;

/**
 * Util class with static methods to manage Tapestry specific notions
 * 
 * @author a160420
 *
 */
public class TmlHelper {
	private static Pattern tagName = Pattern.compile("</?t:([\\w\\.]*).*>", Pattern.DOTALL); 
	private static Pattern typeAttributeName = Pattern.compile("<.*?t:type=\"([\\w\\.]*)\".*>", Pattern.DOTALL);
	
	/**
	 * Computes the Tapestry type in an XML node, managing both notations (<t:type or <div t:typ="type")
	 * 
	 * @param node the XM node to parse
	 * @param t the tapestry namespace
	 * @return
	 */
	public static String getComponentFullName(Node node, String t){
		//Case-1: the <t:componentName ...
		if(node.getPrefix() != null && node.getPrefix().equals(t) && node.getLocalName() != null){
			return node.getLocalName().replace('.', '/');
		}
		
		//Case-2: <div t:type="prefix/componentName" ...
		if(node.getAttributes() != null){
			for(int i = 0; i< node.getAttributes().getLength(); i++){
				Node attribute = node.getAttributes().item(i);
				if(attribute.getNodeName() != null && attribute.getNodeName().equals(t + ":" + Constants.TYPE)){
					return attribute.getNodeValue();
				}
			}
		}
		
		return null;
	}
	
	public static String getComponentFullName(String tagAsString){
		if(tagAsString != null){
			Matcher tagMatcher = tagName.matcher(tagAsString);
			if(tagMatcher.matches() && tagMatcher.groupCount() > 0){
				return tagMatcher.group(1).replace('.', '/');
			}
			
			Matcher typeAttributeMatcher = typeAttributeName.matcher(tagAsString);
			if(typeAttributeMatcher.matches() && typeAttributeMatcher.groupCount() > 0){
				return typeAttributeMatcher.group(1);
			}
		}
		return null;
	}
	
	/**
	 * get the value of attribute "t:mixins" and split it on ',' char (removing spaces)
	 * 
	 * @param tagNode
	 * @param tapestryFeatureModel
	 * @return
	 */
	public static List<String> getMixinTypes(Node tagNode,String t){
		List<String> mixinNames = new ArrayList<String>();
		String mixinsString = getAttributeValue(Constants.MIXINS, tagNode, t);
		if(mixinsString != null){
			//split on ',' char, not considering any extra space before/after this char
			String[] mixinTable = mixinsString.split("\\s*,\\s*");
			mixinNames.addAll(Arrays.asList(mixinTable));
		}
		
		return mixinNames;
	}
	
	public static String getAttributeValue(String attribute, Node tagNode, String t){
		if(tagNode != null){
			if(t == null){
				t = "";
			}
			else{
				t = t + ":";
			}
			
			if(tagNode.getAttributes() != null){
				Node mixinsNode = tagNode.getAttributes().getNamedItem(t + Constants.MIXINS);
				if(mixinsNode != null){
					return  mixinsNode.getNodeValue();
				}
			}
		}
		return "";
	}
	
	/**
	 * Looks for name of the attribute, when the cursor is placed in an attribute value place
	 * 
	 * To do that, it takes every character before the index (except '=' and '"') 
	 * until it gets a "whitespace character" 
	 * 
	 * @param wholeDocument : the whole doc as a String
	 * @param index from where the search starts
	 * @return the name of the attribute
	 */
	public static String getAttributeBefore(String wholeDocument, int index){
		StringBuilder sb = new StringBuilder();
		char currentChar = wholeDocument.charAt(index);
		while(! Character.isWhitespace(currentChar)){
			if(currentChar != '"' && currentChar != '='){
				sb.insert(0, currentChar);
			}
			index --;
			currentChar = wholeDocument.charAt(index);
		}
		
		return sb.toString();
	}

	/**
	 * lists the attributes name of the XLM node
	 * 
	 * @param node
	 * @return
	 */
	public static List<String> getNodeAttributeNames(Node node) {
		List<String> params = new ArrayList<String>();
		
		if(node.getAttributes() != null){
			for(int i = 0; i< node.getAttributes().getLength(); i++){
				params.add(node.getAttributes().item(i).getNodeName());
			}
		}
		return params;
	}
	
}
