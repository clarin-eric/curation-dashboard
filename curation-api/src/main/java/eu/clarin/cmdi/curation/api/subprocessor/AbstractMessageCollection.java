package eu.clarin.cmdi.curation.api.subprocessor;

import java.util.ArrayList;
import java.util.Collection;

import eu.clarin.cmdi.curation.api.report.Message;
import eu.clarin.cmdi.curation.api.report.Severity;
import lombok.Data;

@Data
public abstract class AbstractMessageCollection {
   
   private final Collection<Message> messages;
   
   protected AbstractMessageCollection() {
      
      this.messages = new ArrayList<>();
   
   }

   public void addMessage(Severity lvl, String message) {

       messages.add(new Message(lvl, message));
   
   }
}
