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

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@Getter
public abstract class Score {
   
	@XmlAttribute 
	private double score;	
	@XmlElement(name = "issue")
	private final Collection<Message> messages = new ArrayList<Message>();

	
	@XmlAttribute
	public boolean hasFatalMsg(){
	   
		return messages.stream().filter(message -> message.getSeverity() == Severity.FATAL).findAny().isPresent();
	
	}
	@XmlAttribute
	public abstract double getMax();
	@XmlAttribute
	public abstract double getCurrent();
	
	public Score addMessage(Severity severity, String message) {
	   
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
