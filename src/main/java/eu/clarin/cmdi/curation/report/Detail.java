/**
 * 
 */
package eu.clarin.cmdi.curation.report;

/**
 * @author dostojic
 *
 */
public class Detail {
    
    Severity lvl;
    
    String message;    

    public Detail(Severity lvl, String message) {
	super();
	this.lvl = lvl;
	this.message = message;
    }

    public Severity getLvl() {
        return lvl;
    }

    public void setLvl(Severity lvl) {
        this.lvl = lvl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    

    @Override
    public String toString() {
        return lvl.label + " - " + message;
    }

}
