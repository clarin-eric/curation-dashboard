/**
 * 
 */
package eu.clarin.cmdi.curation.api.xml;

import java.io.InputStream;
import java.io.OutputStream;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * The type Xml marshaller.
 *
 * @param <T> the type parameter
 */
public class XMLMarshaller<T> {

   /**
    * The Type param class.
    */
   final Class<T> typeParamClass;

   /**
    * Instantiates a new Xml marshaller.
    *
    * @param typeParamClass the type param class
    */
   public XMLMarshaller(Class<T> typeParamClass) {
      this.typeParamClass = typeParamClass;
   }

   /**
    * Unmarshal t.
    *
    * @param is the is
    * @return the t
    * @throws JAXBException the jaxb exception
    */
   @SuppressWarnings("unchecked")
   public T unmarshal(InputStream is) throws JAXBException {
      JAXBContext jc = JAXBContext.newInstance(typeParamClass);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      return (T) unmarshaller.unmarshal(is);
   }

   /**
    * Marshal.
    *
    * @param object the object
    * @param os     the os
    */
   public void marshal(Object object, OutputStream os) {
      try {
         JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
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
