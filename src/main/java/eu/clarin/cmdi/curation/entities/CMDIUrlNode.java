/**
 * 
 */
package eu.clarin.cmdi.curation.entities;

/**
 * @author dostojic
 *
 */
public class CMDIUrlNode {
	
	String value;
	String tag;
	
	public CMDIUrlNode(String value) {
		this(value, null);
	}
	
	
	public CMDIUrlNode(String value, String tag) {
		this.value = value;
		this.tag = tag;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public String getTag() {
		return tag;
	}


	public void setTag(String tag) {
		this.tag = tag;
	}

}
