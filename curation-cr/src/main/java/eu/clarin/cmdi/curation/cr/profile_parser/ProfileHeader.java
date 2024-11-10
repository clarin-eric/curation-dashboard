package eu.clarin.cmdi.curation.cr.profile_parser;

import java.io.Serializable;

public record ProfileHeader(
        String cmdiVersion,
        String schemaLocation,
        String id,
        String name,
        String description,
        String status,
        boolean isPublic,
        boolean isCrResident
) implements Serializable {
}
