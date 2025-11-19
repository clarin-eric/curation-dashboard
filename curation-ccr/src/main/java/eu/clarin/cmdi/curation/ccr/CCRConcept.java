package eu.clarin.cmdi.curation.ccr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * The type Ccr concept.
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class CCRConcept implements Serializable {

    private final String uri;

    private String prefLabel = "invalid concept";

    private CCRStatus status = CCRStatus.UNKNOWN;

}
