package net.atos.webtools.tapestry.core.util.helpers;

/**
 * Helper for simple java String manipulation
 * 
 * @author a160420
 *
 */
public class StringHelper {
	/**
	 * set the 1st char of the String to upper case
	 * 
	 * @param st the String
	 * @return a new String
	 */
	public static String toFirstUpper(String st){
		if(st == null || st.length()==0){
			return st;
		}
		return st.substring(0, 1).toUpperCase() + st.substring(1);
	}
	
	/**
	 * Set the 1st char of String to lower case
	 * 
	 * @param st the String
	 * @return a new String
	 */
	public static String toFirstLower(String st){
		if(st == null || st.length()==0){
			return st;
		}
		return st.substring(0, 1).toLowerCase() + st.substring(1);
	}

	/**
	 * This methods returns the next "block" of non-whitespace characters, starting after the index, 
	 * but passing the first whitespace block.
	 * 
	 * Example:
	 * <p>
	 * <code>That is        some text</code>
	 * </p>
	 * If the index is 0 -> "That"
	 * If the index is 2 -> "at"
	 * If the index is 4 -> "is"
	 * If the index is 5 -> "is"
	 * If the index is 8 (or 9, or 10...) -> "some"
	 * 
	 * @param wholeDocument
	 * @param index
	 * @return the next block of text
	 */
	public static String nextString(String wholeDocument, int index){
		StringBuilder sb = new StringBuilder();
		char currentChar = wholeDocument.charAt(index);
		while(index<wholeDocument.length() && Character.isWhitespace(currentChar)){
			index ++;
			currentChar = wholeDocument.charAt(index);
		}
		sb.append(currentChar);
		while((! Character.isWhitespace(currentChar)) && index < wholeDocument.length() - 1 ){
			index ++;
			currentChar = wholeDocument.charAt(index);
			sb.append(currentChar);
		}
		
		return sb.toString();
	}
}
