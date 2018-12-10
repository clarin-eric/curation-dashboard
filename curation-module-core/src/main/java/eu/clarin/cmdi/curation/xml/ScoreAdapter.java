package eu.clarin.cmdi.curation.xml;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.clarin.cmdi.curation.main.Configuration;

public class ScoreAdapter extends XmlAdapter<String, Double> {
	
	private static DecimalFormat formatter;
	
	static{
		formatter = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
		formatter.applyPattern(Configuration.SCORE_NUMERIC_DISPLAY_FORMAT);
	}
	

	@Override
	public Double unmarshal(String v) throws Exception {
		return new Double(v);
	}

	@Override
	public String marshal(Double v) throws Exception {
		if(v == null)
			return null;
		return v.isNaN()? "NaN" : formatter.format(v);
	}

}
