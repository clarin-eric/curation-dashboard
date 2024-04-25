/**
 * 
 */
package eu.clarin.cmdi.curation.cr.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.InputStream;
import java.io.OutputStream;

/**

 *
 */
public class XMLMarshaller1<T> {

   final Class<T> typeParamClass;

   public XMLMarshaller1(Class<T> typeParamClass) {
      this.typeParamClass = typeParamClass;
   }

   @SuppressWarnings("unchecked")
   public T unmarshal(InputStream is) throws JAXBException {
      JAXBContext jc = JAXBContext.newInstance(typeParamClass);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      return (T) unmarshaller.unmarshal(is);
   }

   public void marshal(T object, OutputStream os) {
      try {
         JAXBContext jaxbContext = JAXBContext.newInstance(typeParamClass);
         Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

         // output pretty printed
         jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         jaxbMarshaller.setProperty(jakarta.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");

         jaxbMarshaller.marshal(object, os);

      }
      catch (JAXBException e) {
         e.printStackTrace();
      }
   }

}
