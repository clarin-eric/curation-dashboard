package eu.clarin.cmdi.curation.xml;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.clarin.cmdi.curation.main.Configuration;
import humanize.Humanize;

public class TimestampAdapter extends XmlAdapter<String, Long>{

	@Override
	public Long unmarshal(String timestamp) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(Configuration.TIMESTAMP_DISPLAY_FORMAT);
		Date date = sdf.parse(timestamp);
		return new Long(date.getTime());
	}

	@Override
	public String marshal(Long v) throws Exception {
		return Humanize.formatDate(new Date(v), Configuration.TIMESTAMP_DISPLAY_FORMAT);
	}

}
