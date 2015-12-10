package eu.clarin.cmdi.davor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.ValidatorHandler;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import eu.clarin.cmdi.curation.component_registry.XSDCache;
import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.xml.CMDIContentHandler;
import eu.clarin.cmdi.curation.xml.CMDIErrorHandler;

/**
 * SchemaValidatorHandlerExample demonstrates the use of jaxp validation apis.
 *
 * This version was modified by Michael Kay from the sample application named SchemaValidator
 * issued with the JAXP 1.3 distribution. It has been changed to use a ValidatorHandler
 * and to display the types of elements and attributes as reported.
 *
 * The original file contained no explicit terms and conditions or copyright statement,
 * but it should be assumed that it is subject to the usual Apache rules.
 */

public class SchemaValidatorHandlerExample {

    /**
     * Class is never instantiated
     */
    private SchemaValidatorHandlerExample() {}

    /**
     * Inner class to implement a resource resolver. This version always returns null, which
     * has the same effect as not supplying a resource resolver at all. The LSResourceResolver
     * is part of the DOM Level 3 load/save module.
     */

    protected static class Resolver implements LSResourceResolver{

        /**
         * Resolve a reference to a resource
         * @param type The type of resource, for example a schema, source XML document, or query
         * @param namespace The target namespace (in the case of a schema document)
         * @param publicId The public ID
         * @param systemId The system identifier (as written, possibly a relative URI)
         * @param baseURI The base URI against which the system identifier should be resolved
         * @return an LSInput object typically containing the character stream or byte stream identified
         * by the supplied parameters; or null if the reference cannot be resolved or if the resolver chooses
         * not to resolve it.
         */

        public LSInput resolveResource(String type, String namespace, String publicId, String systemId, String baseURI) {
            return null;
        }

    }

    /**
     * Main entry point. Expects two arguments: the schema document, and the source document.
     * Allows "--" as the schema document, indicating that the schema is identified by xsi:schemaLocation
     * @param args
     */
    public static void main(String [] args) {
        try {
        	
            Path schema = Paths.get("D:/workspace/CLARIN/cmdi-curation-module/xsd/1288172614026.xsd");
            Path instance = Paths.get("D:/data/cmdi/__TEST/oai_sil_org_296.xml");
        	
        	Schema mySchema = XSDCache.getInstance().getSchema("1288172614026");
        	
            // Set a system property to force selection of the Saxon SchemaFactory implementation
            // This is commented out because it shouldn't be necessary if Saxon-SA is on the classpath;
            // but in the event of configuration problems, try reinstating it.

            //System.setProperty("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema", "com.saxonica.jaxp.SchemaFactoryImpl");
        	
/*
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            //schemaFactory.setProperty(FeatureKeys.VALIDATION_WARNINGS, Boolean.TRUE);
            System.err.println("Loaded schema validation provider " + schemaFactory.getClass().getName());

            LocalErrorHandler errorHandler = new LocalErrorHandler();
            schemaFactory.setErrorHandler(errorHandler);
            //create a grammar object.

                        
            Schema schemaGrammar = schemaFactory.newSchema(schema.toFile());
            
            System.err.println("Created Grammar object for schema : " + schema);
*/
        	Collection<Message> errors = new LinkedList<Message>();
        	CMDIErrorHandler errorHandler = new CMDIErrorHandler(errors);
            //Resolver resolver = new Resolver();
            
        	
            //create a validator to validate against the schema.
            //ValidatorHandler schemaValidator = schemaGrammar.newValidatorHandler();
            ValidatorHandler schemaValidator = mySchema.newValidatorHandler();
            //schemaValidator.setResourceResolver(resolver);
            schemaValidator.setErrorHandler(errorHandler);
            schemaValidator.setContentHandler(new CMDIContentHandler(schemaValidator.getTypeInfoProvider()));

            System.err.println("Validating "+ instance + " against grammar " + schema);
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            SAXParser parser = parserFactory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setContentHandler(schemaValidator);
            reader.parse(new InputSource(instance.toUri().toString())); //new File(args[1]).toURI().toString())

            // Note: It appears Xerces exits normally if validation errors were found. Saxon throws an exception.

            int errorCount = errors.size();
            if (errorCount == 0) {
                System.err.println("Validation successful");
            } else {
                System.err.println("Validation unsuccessful: " + errorCount + " error" + (errorCount==1?"":"s"));
            }
        } catch (SAXException saxe) {
            exit(1, "Error: " + saxe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            exit(2, "Fatal Error: " + e);
        }
    }

    private static void exit(int errCode, String msg) {
        System.err.println(msg);
        System.exit(errCode);
    }

    public static void printUsage(){
        System.err.println("Usage : java SchemaValidatorHandlerExample (<schemaFile>|--) <xmlFile>");
    }
}
