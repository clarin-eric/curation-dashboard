package eu.clarin.cmdi.curation.cr.profile_parser;

import java.util.Collection;
import java.util.Map;

import eu.clarin.cmdi.curation.pph.ProfileHeader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ParsedProfile {

   @Getter
   private final ProfileHeader header;

   private final Map<String, CMDINode> xpathNode;

   private final Map<String, CMDINode> xpathElementNode;

   private final Map<String, CMDINode> xpathComponentNode;


   public String getId() {
      
      return header.getId();
   }

   public String getName() {
      
      return header.getName();
   }

   public String getDescription() {
      
      return header.getDescription();
   }

   public String getCMDIVersion() {
      
      return header.getCmdiVersion();
   }

   public boolean isPublic() {
      
      return header.isPublic();
   }
   

   public Collection<String> getXPaths() {

      return xpathNode.keySet();
   }
   
   @Override
   public String toString() {
      
      StringBuilder sb = new StringBuilder();
      sb.append("CMD Version: " + getCMDIVersion()).append("\n");
      sb.append(header.getId()).append("\t").append(header.getName()).append("\t").append(header.getDescription())
            .append("\n");
      sb.append("\n").append("xpaths:").append("\n");
      
      xpathNode.forEach((x, n) -> sb.append("\t").append(x + ": " + n).append("\n"));

      return sb.toString();
   }
}
