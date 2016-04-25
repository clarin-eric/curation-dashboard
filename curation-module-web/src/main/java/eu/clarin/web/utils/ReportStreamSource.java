package eu.clarin.web.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.vaadin.server.StreamResource.StreamSource;

@SuppressWarnings("serial")
public class ReportStreamSource implements StreamSource{
	
	String content;
	
	public ReportStreamSource(String content){
		this.content = content;
	}

	@Override
	public InputStream getStream() {
		return new ByteArrayInputStream(content.getBytes());
	}
	
	public void setContent(String content){
		this.content = content;
	}

}
