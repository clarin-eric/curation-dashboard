/**
 * 
 */
package eu.clarin.cmdi.curation.entities;

/**
 * @author dostojic
 *
 */
public class CMDUrlNode {
	
	String value;
	String tag;
	
	public CMDUrlNode(String value) {
		this(value, null);
	}
	
	
	public CMDUrlNode(String value, String tag) {
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
	
	// since we care only about uniqueness of links
	//it is enough to compare link
	@Override
	public boolean equals(Object obj) {
	    if ( !(obj instanceof CMDUrlNode) ) return false;
	    return this.value.equals(((CMDUrlNode)obj).value);
	}

}
