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
	
	private String id;
	private String schemaLocation;	
	private String name;
	private String description;
	private String cmdiVersion;
	private String status;
	
	private transient boolean isLocalFile;
	private transient boolean isPublic = true;
	
	

	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCmdiVersion() {
        return cmdiVersion;
    }

    public void setCmdiVersion(String cmdiVersion) {
        this.cmdiVersion = cmdiVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isLocalFile() {
        return isLocalFile;
    }

    public void setLocalFile(boolean isLocalFile) {
        this.isLocalFile = isLocalFile;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

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
		return Objects.hash(this.id);
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append(id)
				//.append(schemaLocation)
				.append(cmdiVersion)
				.toString();
	}
}
