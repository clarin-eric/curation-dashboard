package eu.clarin.cmdi.curation.api.report;

import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@Getter
public abstract class Scoring {
   
	@XmlAttribute 
	private double score;	
	@XmlElement(name = "issue")
	private final Vector<Message> messages = new Vector<Message>();

	
	@XmlAttribute
	public boolean hasFatalMsg(){
	   
		return (messages.size() > 0 && messages.lastElement().severity == Severity.FATAL);
	
	}
	@XmlAttribute
	public abstract double getMaxScore();
	@XmlAttribute
	public abstract double getScore();
	@XmlAttribute
	public double getScorePctg() {
	   return (getScore()!=0.0?getMaxScore()/getScore():0.0);
	}
	
	public Scoring addMessage(Severity severity, String message) {
	   
	   messages.add(new Message(severity, message));
	   return this;
	   
	}
	
	@XmlType
	@XmlEnum
	public static enum Severity {
	   
	   FATAL, 
	   ERROR, 
	   WARNING, 
	   INFO, 
	   NONE;

	}
	
	@XmlRootElement(name = "details")
	@XmlAccessorType(XmlAccessType.FIELD)
	@Data
	@NoArgsConstructor(force = true)
	@RequiredArgsConstructor
	public static class Message {
	    
	    @XmlAttribute
	    private final Severity severity;    
	    @XmlAttribute
	    private final String message; 

	}
}
