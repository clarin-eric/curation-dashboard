package eu.clarin.cmdi.curation.api.entities;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.processor.CollectionProcessor;
import eu.clarin.cmdi.curation.api.report.CollectionReport;

@Component
@Scope("prototype")
public class CMDCollection {

   @Autowired
   CollectionProcessor processor;

   Deque<CMDInstance> children;

   long numOfFiles;
   long maxFileSize = 0;
   long minFileSize = Long.MAX_VALUE;
   protected Path path = null;
   protected long size = 0;

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

   public long getNumOfFiles() {
      return numOfFiles;
   }

   public void setNumOfFiles(long numOfFiles) {
      this.numOfFiles = numOfFiles;
   }

   public long getMaxFileSize() {
      return maxFileSize;
   }

   public void setMaxFileSize(long maxFileSize) {
      this.maxFileSize = maxFileSize;
   }

   public long getMinFileSize() {
      return minFileSize;
   }

   public void setMinFileSize(long minFileSize) {
      this.minFileSize = minFileSize;
   }

   public Path getPath() {
      return path;
   }

   public void setPath(Path path) {
      this.path = path;
   }

   public long getSize() {
      return size;
   }

   public void setSize(long size) {
      this.size = size;
   }

   @Override
   public String toString() {
      return "Collection: " + path.toString();
   }
}
