package eu.clarin.cmdi.curation.api.report;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Score {

	@XmlAttribute String segment;
	@XmlAttribute Double score;
	@XmlAttribute Double maxScore;
	
	@XmlElement(name = "issue")
	public Collection<Message> messages;
	
	
	public Score(Double score, Double maxScore, String segment, Collection<Message> messages) {
		this.score = score;
		this.maxScore = maxScore;
		this.segment = segment;
		this.messages = messages;
	}
	
	//for JAXB
	Score(){}
	
	
	public boolean hasFatalMsg(){
		return messages != null? messages.stream().filter(m -> m.lvl.equals(Severity.FATAL)).findAny().orElse(null) != null : false;
	}

	public Double getScore() {
		return score;
	}

	public Double getMaxScore() {
		return maxScore;
	}	
	
}
