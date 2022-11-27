package eu.clarin.cmdi.curation.api.report;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.api.utils.TimeUtils;

@XmlRootElement(name = "error-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorReport extends Report<ErrorReport> {
   @XmlAttribute(name = "creation-time")
   public String creationTime = TimeUtils.humanizeToDate(System.currentTimeMillis());

   public String name;
   public String error;

   public ErrorReport(String name, String error) {
      this.name = name;
      this.error = error;
   }

   // for JAXB
   public ErrorReport() {
   }


   @Override
   public String getName() {
      
      return name;
   
   }
   
   @Override
   public void addReport(ErrorReport report) {
      
   }

   @Override
   public boolean isValid() {
      
      return false;
   
   }


   @Override
   public void addSegmentScore(Score segmentScore) {
      throw new UnsupportedOperationException();
   }

   /*
    * when processing collection we are adding the invalid records to the
    * collection list
    */


   public void mergeWithParent(CollectionReport parentReport) {
      if (parentReport.file == null) {
         parentReport.file = new ArrayList<>();
      }

      CollectionReport.InvalidFile invalidFile = new CollectionReport.InvalidFile();
      invalidFile.recordName = name;
      invalidFile.reason = error;
      parentReport.addInvalidFile(invalidFile);
   }
}
