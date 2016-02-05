/**
 * 
 */
package eu.clarin.cmdi.curation.xml;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author dostojic
 *
 */
public interface XMLMarshaller<T>{
	
	public T unmarshal(InputStream is) throws Exception;
	
	public void marshal(T object, OutputStream os) throws Exception;	

}
