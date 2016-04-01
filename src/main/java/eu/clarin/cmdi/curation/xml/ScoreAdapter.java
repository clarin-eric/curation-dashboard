package eu.clarin.cmdi.curation.xml;

import java.io.IOException;
import java.text.DecimalFormat;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.configuration.ConfigurationException;

import eu.clarin.cmdi.curation.main.Configuration;

public class ScoreAdapter extends XmlAdapter<String, Double> {

	private static DecimalFormat formatter = new DecimalFormat(Configuration.SCORE_NUMERIC_DISPLAY_FORMAT);

	@Override
	public Double unmarshal(String v) throws Exception {
		return new Double(v);
	}

	@Override
	public String marshal(Double v) throws Exception {
		
		return v.isNaN()? "NaN" : formatter.format(v);
	}

}
