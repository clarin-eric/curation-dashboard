package eu.clarin.cmdi.curation.ccr;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * The type Concept type adapter.
 */
public class ConceptTypeAdapter extends TypeAdapter<CCRConcept> {

	/**
	 * Read ccr concept.
	 *
	 * @param in the Json reader with all ccr concepts
	 * @return the ccr concept
	 * @throws IOException the io exception
	 */
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

	/**
	 * Write.
	 *
	 * @param arg0 the arg 0
	 * @param arg1 the arg 1
	 * @throws IOException the io exception
	 */
	@Override
	public void write(JsonWriter arg0, CCRConcept arg1) throws IOException {
		throw new IOException("I don't know what this is? But it was throwing not implemented exception before.");
	}
}
