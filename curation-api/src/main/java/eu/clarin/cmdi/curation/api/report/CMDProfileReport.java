package eu.clarin.cmdi.curation.api.report;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.clarin.cmdi.curation.pph.ProfileHeader;


/**
 * A selection of values from a single CMDProfileReport which will form a line in a statistical overview
 */
@XmlRootElement(name = "profile-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class CMDProfileReport extends Report<CMDProfileReport> {

    @XmlAttribute(name = "score")
    public Double score = 0.0;

    @XmlAttribute(name = "max-score")
    public double maxScore;

    @XmlAttribute(name = "creation-time")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime creationTime = LocalDateTime.now();

    @XmlElement(name = "header-section")
    public ProfileHeader header;

    @XmlElement(name = "cmd-components-section")
    public Components components;

    @XmlElement(name = "cmd-concepts-section")
    public Elements elements;

    @XmlElement(name = "facets-section")
    public FacetReport facet;
    
    @XmlElementWrapper( name = "usage-section")
    @XmlElement(name = "collection")
    public Collection<CollectionUsage> collectionUsage = new ArrayList<CollectionUsage>();

    @XmlElementWrapper(name = "score-section")
    @XmlElement(name = "score")
    public Collection<Score> segmentScores;
    
    @Override
    public void addReport(CMDProfileReport report) {
       
    }
    
    public void addCollectionUsage(String collectionName, long count) {
        this.collectionUsage.add(new CollectionUsage(collectionName, count));
    }

    @Override
    public String getName() {
        return header.getId();        
    }


    @XmlRootElement()
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Components {

        @XmlAttribute
        public int total;

        @XmlAttribute
        public int unique;

        @XmlAttribute
        public int required; // cardinality > 0

        @XmlElement(name = "component")
        public java.util.Collection<Component> components;

    }

    @XmlRootElement()
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Component {

        @XmlAttribute
        public String id;

        @XmlAttribute
        public String name;

        @XmlAttribute
        public int count;
    }

    @XmlRootElement()
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Elements {

        @XmlAttribute
        public int total;

        @XmlAttribute
        public int required; // cardinality > 0

        @XmlAttribute
        public int withConcept;

        @XmlAttribute
        public Double percWithConcept;

        public Concepts concepts;

    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Concepts {

        @XmlAttribute
        public int total;

        @XmlAttribute
        public int unique;

        @XmlAttribute
        public int required;

        @XmlElement(name = "concept")
        public java.util.Collection<Concept> concepts;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CollectionUsage{
        public CollectionUsage() {           
        }
        public CollectionUsage(String collectionName, long count) {
            this.collectionName = collectionName;
            this.count = count;
        }
        
        @XmlAttribute
        public String collectionName;
        
        @XmlAttribute
        public long count;
        
    }

    @Override
    public void addSegmentScore(Score segmentScore) {
        if (segmentScores == null)
            segmentScores = new ArrayList<>();

        segmentScores.add(segmentScore);
        maxScore += segmentScore.maxScore;
        score += segmentScore.score;
    }

    @Override
    public boolean isValid() {
        return segmentScores.stream().filter(Score::hasFatalMsg).findFirst().orElse(null) == null;
    }
    
    @XmlRootElement(name = "facets")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FacetReport {

       @XmlAttribute
        public int numOfFacets;
       
       @XmlElementWrapper
       @XmlElement(name="facet")
       public Collection<Coverage> coverage;
       
       @XmlAttribute
       public Double profileCoverage;
       
       @XmlAttribute
       public Double instanceCoverage;  
       
       @XmlElementWrapper
       @XmlElement(name="valueNode")
       public Collection<ValueNode> values;

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class ValueNode{
          
          @XmlElement public String value;
          
          @XmlElement public String xpath;
          
          @XmlElement public Concept concept;
          
          @XmlElement public Collection<FacetValueStruct> facet;
             
        }    
        
        @XmlRootElement
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class FacetValueStruct{
          
          @XmlAttribute public String name;
          
          @XmlAttribute public Boolean isDerived;
          
          @XmlAttribute public Boolean usesValueMapping;
          
          @XmlAttribute public String normalisedValue;    
          
        }
        
        @XmlRootElement
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Coverage{
          
          @XmlAttribute public String name;
          
          @XmlAttribute public Boolean coveredByProfile = false;
          
          @XmlAttribute public Boolean coveredByInstance = false;
          
          @Override
          public String toString() {
             return name + "\tprofile: " + coveredByProfile + "\tinstance: " + coveredByInstance;
          }
          
        }
    }
}
