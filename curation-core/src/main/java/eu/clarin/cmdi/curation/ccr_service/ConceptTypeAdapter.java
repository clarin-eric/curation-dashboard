package eu.clarin.cmdi.curation.ccr_service;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

class ConceptTypeAdapter extends TypeAdapter<CCRConcept> {

	@Override
	public CCRConcept read(final JsonReader in) throws IOException {
		in.beginObject();
		String prefLabel = "";
		String uri = "";
		CCRStatus status = CCRStatus.UNKNOWN;
		while(in.hasNext()){
			switch (in.nextName()) {
				case "prefLabel@en":
					in.beginArray();
					prefLabel = in.nextString();
					in.endArray();
					break;
				case "uri":
					uri = in.nextString();
					break;
				case "status":
					switch(in.nextString().toLowerCase()){
						case "candidate": status = CCRStatus.CANDIDATE; break;
						case "approved": status = CCRStatus.APPROVED; break;
						case "expired": status = CCRStatus.EXPIRED; break;
						default: status = CCRStatus.UNKNOWN;
					}
					break;
				case "xmlns": in.skipValue(); break;
				default:break;
			}
		}

		in.endObject();

		return new CCRConcept(prefLabel, uri, status);
	}

	@Override
	public void write(JsonWriter arg0, CCRConcept arg1) throws IOException {
		throw new IOException("I don't know what this is? But it was throwing not implemented exception before.");
	}
}
