package eu.clarin.cmdi.curation.api.report;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.RequiredArgsConstructor;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Scoring {
   @XmlAttribute
   public double maxScore; 
   @XmlAttribute
   public double score;

   public final Collection<Message> messages = new ArrayList<Message>();


   public boolean hasFatalMessage() {

      return messages.stream().anyMatch(message -> message.severity == Severity.FATAL);

   }

   public static enum Severity {
      FATAL, ERROR, WARNING, INFO, NONE;
   }

   @RequiredArgsConstructor
   public static class Message {
      
      public final Severity severity;
      public final String issue;

   }
}
