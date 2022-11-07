package eu.clarin.cmdi.curation.api.entity;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.processor.CollectionProcessor;
import eu.clarin.cmdi.curation.api.report.CollectionReport;
import lombok.Data;

@Component
@Scope(value="prototype")
@Data
public class CMDCollection {

   @Autowired
   CollectionProcessor processor;

   Deque<CMDInstance> children;

   private long numOfFiles;
   private long maxFileSize = 0;
   private long minFileSize = Long.MAX_VALUE;
   private Path path = null;
   private long size = 0;

   public CMDCollection(Path path) {
      this.path = path;
      children = new ArrayDeque<>();
   }

   public CollectionReport generateReport() {
      return processor.process(this);
   }

   public void addChild(CMDInstance child) {
      children.add(child);

      numOfFiles++;
      size += child.getSize();
      if (child.getSize() > maxFileSize)
         maxFileSize = child.getSize();
      if (child.getSize() < minFileSize)
         minFileSize = child.getSize();

   }

   public Deque<CMDInstance> getChildren() {
      return children;
   }
}
