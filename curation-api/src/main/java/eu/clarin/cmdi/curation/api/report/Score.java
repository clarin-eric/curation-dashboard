package eu.clarin.cmdi.curation.api.report;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class Score {

	@XmlAttribute 
	private final String segment;
	@XmlAttribute 
	private final Double maxScore;
	@XmlAttribute 
	private Double score;	
	@XmlElement(name = "issue")
	private final Collection<Message> messages = new ArrayList<Message>();

	
	
	public boolean hasFatalMsg(){
		return messages.stream().filter(m -> m.lvl.equals(Severity.FATAL)).findAny().isPresent();
	}
	
	public Score addMessage(Severity severity, String message) {
	   
	   messages.add(new Message(severity, message));
	   return this;
	   
	}

}
