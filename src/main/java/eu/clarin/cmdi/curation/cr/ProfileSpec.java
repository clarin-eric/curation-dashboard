/**
 * 
 */
package eu.clarin.cmdi.curation.cr;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author dostojic
 *
 */

@XmlRootElement (name = "CMD_ComponentSpec")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProfileSpec{
   
    @XmlElement(name = "CMD_Component")
    private CMD_Component component;
    
    @XmlElement(name = "Header")
    private Header header;

    public CMD_Component getComponent() {
        return component;
    }

    public Header getHeader() {
        return header;
    }



    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return component.toString();
    }
    

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Header {
	private String ID;
	private String Name;
	private String Description;
	
	public String getID() {
	    return ID;
	}
	public String getName() {
	    return Name;
	}
	public String getDescription() {
	    return Description;
	}

    }
    
    
    @XmlRootElement(name = "CMD_Component")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CMD_Component {
        
        @XmlAttribute
        String name;
        
        @XmlAttribute(name = "ComponentId")
        String componetId;
        
        @XmlAttribute(name = "CardinalityMin")
        String cardinalityMin;
        
        @XmlAttribute(name = "CardinalityMax")
        String cardinalityMax;
            
        @XmlElement(name = "CMD_Component")
        List<CMD_Component> components;

	public String getName() {
	    return name;
	}

	public String getComponetId() {
	    return componetId;
	}

	public String getCardinalityMin() {
	    return cardinalityMin;
	}

	public String getCardinalityMax() {
	    return cardinalityMax;
	}

	public List<CMD_Component> getComponents() {
	    return components;
	}

	@Override
	public String toString() {
	    String s = "name: " + name + ", componentId: " + componetId + ", cardinalityMin: " + cardinalityMin + ", cardinalityMax: " + cardinalityMax;
	    if(components != null){
		s += "\nComponents:\n";
		for(CMD_Component c: components)
		    s += c + "\n";
	    }
	    
	    return s;
	}
        
    }
    
}



