package eu.clarin.cmdi.curation.subprocessor;

import com.ximpleware.VTDException;
import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.exception.ProfileNotFoundException;
import eu.clarin.cmdi.curation.io.FileSizeException;
import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**

 */


public abstract class ProcessingStep<T extends CurationEntity, R extends Report> {

    protected Collection<Message> msgs = null;

    /**
     * @param entity
     * @param report
     * @return true if processing finished without fatal errors
     * @throws ExecutionException
     */
    public abstract void process(T entity, R report) throws ExecutionException, IOException, VTDException, TransformerException, FileSizeException, SAXException, ParserConfigurationException, ProfileNotFoundException;

    public abstract Score calculateScore(R report);

    protected void addMessage(Severity lvl, String message) {
        if (msgs == null) {
            msgs = new ArrayList<>();
        }
        msgs.add(new Message(lvl, message));
    }
}