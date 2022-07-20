/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import com.ximpleware.VTDException;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.io.FileSizeException;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Severity;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

public abstract class CMDSubprocessor {

    protected Collection<Message> msgs = null;

    public abstract void process(CMDInstance entity, CMDInstanceReport report) throws IOException, ExecutionException, ParserConfigurationException, SAXException, TransformerException, FileSizeException, VTDException;

    protected void addMessage(Severity lvl, String message) {
        if (msgs == null) {
            msgs = new ArrayList<>();
        }
        msgs.add(new Message(lvl, message));
    }
}
