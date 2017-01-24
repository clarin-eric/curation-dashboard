package eu.clarin.cmdi.curation.cr;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@XmlRootElement(name="profileDescription")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProfileHeader {
	
	public String id;
	public String schemaLocation;	
	public String name;
	public String description;
	public String cmdiVersion;
	public String status;
	
	public transient boolean isLocalFile;
	public transient boolean isPublic = true;

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		ProfileHeader rhs = (ProfileHeader) obj;
		return new EqualsBuilder()
				.append(schemaLocation, rhs.schemaLocation)
				.isEquals();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.schemaLocation);
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append(id)
				.append(schemaLocation)
				.append(cmdiVersion)
				.toString();
	}
}
