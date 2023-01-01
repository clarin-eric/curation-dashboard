package eu.clarin.cmdi.curation.api.entity;

import java.nio.file.Path;

import eu.clarin.cmdi.curation.api.report.ScoreReport;
import lombok.Data;

@Data
public abstract class AbstractCMDEntity<T extends ScoreReport> {
   
   private final Path path;
   
   public abstract T generateReport();

}
