@XmlJavaTypeAdapters({
	@XmlJavaTypeAdapter(value = ScoreAdapter.class, type=Double.class),
	@XmlJavaTypeAdapter(value = TimestampAdapter.class, type=Long.class)
})

package eu.clarin.cmdi.curation.report;

import eu.clarin.cmdi.curation.xml.ScoreAdapter;
import eu.clarin.cmdi.curation.xml.TimestampAdapter;
import javax.xml.bind.annotation.adapters.*;