@XmlJavaTypeAdapters({
        @XmlJavaTypeAdapter(value = ScoreAdapter.class, type = Double.class)
})

package eu.clarin.cmdi.curation.api.report;

import javax.xml.bind.annotation.adapters.*;

import eu.clarin.cmdi.curation.api.xml.ScoreAdapter;