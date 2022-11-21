package eu.clarin.cmdi.curation.ccr;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ConceptTypeAdapter extends TypeAdapter<CCRConcept> {

	@Override
	public CCRConcept read(final JsonReader in) throws IOException {
		in.beginObject();
		String prefLabel = "";
		String uri = "";
		CCRStatus status = CCRStatus.UNKNOWN;
		while(in.hasNext()){
			switch (in.nextName()) {
				case "prefLabel@en" -> {
					in.beginArray();
					prefLabel = in.nextString();
					in.endArray();
				}
				case "uri" -> {
					uri = in.nextString();
				}
				case "status" -> {
					switch(in.nextString().toLowerCase()){
						case "candidate" -> status = CCRStatus.CANDIDATE;
						case "approved" -> status = CCRStatus.APPROVED;
						case "expired" -> status = CCRStatus.EXPIRED; 
						default -> status = CCRStatus.UNKNOWN;
					}
				}
				case "xmlns" -> in.skipValue(); 
			}
		}

		in.endObject();

		return new CCRConcept(uri, prefLabel, status);
	}

	@Override
	public void write(JsonWriter arg0, CCRConcept arg1) throws IOException {
		throw new IOException("I don't know what this is? But it was throwing not implemented exception before.");
	}
}
