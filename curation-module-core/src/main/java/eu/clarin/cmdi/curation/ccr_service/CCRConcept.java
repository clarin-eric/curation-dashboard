package eu.clarin.cmdi.curation.ccr_service;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

public class CCRConcept{

	String uri;
	
	String prefLabel;
	
	CCRStatus status;

	public CCRConcept(String prefLabel, String uri, CCRStatus status) {
		this.prefLabel = prefLabel;
		this.uri = uri;
		this.status = status;
	}
	
	
	public CCRConcept(){}

	// getters

	public String getPrefLabel() {
		return prefLabel;
	}

	public String getUri() {
		return uri;
	}

	public CCRStatus getStatus() {
		return status;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CCRConcept))
			return false;
		if (obj == this)
			return true;

		CCRConcept rhs = (CCRConcept) obj;
		return new EqualsBuilder().append(uri, rhs.uri).isEquals();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.uri);
	}

	public String toString() {
		return new ToStringBuilder(this)
				.append("preferedLabel", prefLabel)
				.append("uri", uri)
				.append("status", status.name())
				.toString();
	}

}
