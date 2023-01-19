package eu.clarin.cmdi.curation.api.entity;

import java.nio.file.Path;

import lombok.Data;

@Data
public abstract class AbstractCMDEntity<T> {
   
   private final Path path;
   
   public abstract T generateReport();

}
