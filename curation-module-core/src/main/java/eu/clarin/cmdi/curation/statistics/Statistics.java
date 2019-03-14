package eu.clarin.cmdi.curation.statistics;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * The class works as a wrapper for statistics elements (usually profile or collection)
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Statistics <T>{
    
    private List<T> entity = new ArrayList<T>();
    
    public void addLine(T statistic) {
        
    }

}
