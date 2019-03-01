/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import com.ximpleware.VTDException;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.io.FileSizeException;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author dostojic
 *
 */
public abstract class CMDSubprocessor extends ProcessingStep<CMDInstance, CMDInstanceReport> {

    @Override
    public abstract void process(CMDInstance entity, CMDInstanceReport report) throws IOException, ExecutionException, ParserConfigurationException, SAXException, TransformerException, FileSizeException, VTDException;

}
