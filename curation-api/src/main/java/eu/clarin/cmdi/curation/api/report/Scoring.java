package eu.clarin.cmdi.curation.api.report;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@Getter
public abstract class Scoring {
   	
	@XmlElement(name = "message")
	private final Collection<Message> messages = new ArrayList<Message>();

	
	@XmlAttribute
	public boolean hasFatalMessage(){
	   
		return messages.stream().anyMatch(message -> message.getSeverity() == Severity.FATAL);
	
	}
	@XmlAttribute
	public abstract double getMaxScore();
	@XmlAttribute
	public abstract double getScore();
	@XmlAttribute
	public double getScorePctg() {
	   return (getScore()!=0.0?getMaxScore()/getScore():0.0);
	}
	
	public Scoring addMessage(Severity severity, String issue) {
	   
	   messages.add(new Message(severity, issue));
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
	@XmlAccessorType(XmlAccessType.NONE)
	@NoArgsConstructor(force = true)
	@RequiredArgsConstructor
	@Getter
	public class Message {
	    
	    @XmlAttribute
	    private final Severity severity;    
	    @XmlAttribute
	    private final String issue; 

	}
}
