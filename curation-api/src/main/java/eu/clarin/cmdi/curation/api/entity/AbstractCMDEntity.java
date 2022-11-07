package eu.clarin.cmdi.curation.api.entity;

import java.nio.file.Path;

import eu.clarin.cmdi.curation.api.report.Report;
import lombok.Data;

@Data
public abstract class AbstractCMDEntity<T extends Report<?>> {
   
   private final Path path;
   
   public abstract T generateReport();

}
