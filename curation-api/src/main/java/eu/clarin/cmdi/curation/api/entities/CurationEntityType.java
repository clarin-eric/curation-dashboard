package eu.clarin.cmdi.curation.api.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CurationEntityType {

   PROFILE("profiles"), 
   INSTANCE("instances"), 
   COLLECTION("collections"), 
   STATISTICS("statistics");

   private final String stringValue;

   @Override
   public String toString() {
      
      return this.stringValue;
   }
}
