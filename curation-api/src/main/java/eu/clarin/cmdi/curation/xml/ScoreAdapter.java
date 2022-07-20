package eu.clarin.cmdi.curation.xml;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.springframework.beans.factory.annotation.Autowired;

import eu.clarin.cmdi.curation.configuration.CurationConfig;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class ScoreAdapter extends XmlAdapter<String, Double> {

    private static DecimalFormat formatter;
    
    @Autowired
    private static CurationConfig props;

    static {
        formatter = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        formatter.applyPattern(props.getScoreNumericDisplayFormat());
    }

    @Override
    public Double unmarshal(String v) {
        return Double.parseDouble(v);
    }

    @Override
    public String marshal(Double v) {
        if (v == null)
            return null;
        return v.isNaN() ? "NaN" : formatter.format(v);
    }

}
