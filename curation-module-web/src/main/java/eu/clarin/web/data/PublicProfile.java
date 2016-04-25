package eu.clarin.web.data;

public class PublicProfile {
	String id;
	String name;
	double score;
	double facetCoverage;
	double elementsWithConcepts;
	
	public PublicProfile(String id, String name, double score, double facetCoverage, double elementsWithConcepts){
		this.id = id;
		this.name = name;
		this.score = score;
		this.facetCoverage = facetCoverage;
		this.elementsWithConcepts = elementsWithConcepts;
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getFacetCoverage() {
		return facetCoverage;
	}

	public void setFacetCoverage(double facetCoverage) {
		this.facetCoverage = facetCoverage;
	}

	public double getElementsWithConcepts() {
		return elementsWithConcepts;
	}

	public void setElementsWithConcepts(double elementsWithConcepts) {
		this.elementsWithConcepts = elementsWithConcepts;
	}
	
	
}
