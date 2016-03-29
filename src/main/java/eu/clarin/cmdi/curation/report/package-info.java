@XmlJavaTypeAdapters({
	@XmlJavaTypeAdapter(value = ScoreAdapter.class, type=Double.class)
})

package eu.clarin.cmdi.curation.report;

import eu.clarin.cmdi.curation.xml.ScoreAdapter;
import javax.xml.bind.annotation.adapters.*;