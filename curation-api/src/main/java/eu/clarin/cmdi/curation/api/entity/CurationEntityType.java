package eu.clarin.cmdi.curation.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CurationEntityType {

   PROFILE("profiles"), 
   INSTANCE("instance"), 
   COLLECTION("collection"), 
   LINKCHECKER("linkchecker");

   private final String stringValue;

   @Override
   public String toString() {
      
      return this.stringValue;
   }
}
