package eu.clarin.web.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.StreamVariable;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Html5File;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;

public class FileDropBox extends DragAndDropWrapper implements DropHandler{
    private static final long FILE_SIZE_LIMIT = 10 * 1024 * 1024; // 10MB
    
    private final ProgressBar progressBar;
    
    
    public FileDropBox(final Component root, ProgressBar progressBar) {
        super(root);
        setDropHandler(this);
        this.progressBar = progressBar;
    }

    @Override
    public void drop(final DragAndDropEvent dropEvent) {

        // expecting this to be an html5 drag
        final WrapperTransferable tr = (WrapperTransferable) dropEvent.getTransferable();
        final Html5File[] files = tr.getFiles();
        if (files != null) {
            for (final Html5File html5File : files) {
                final String fileName = html5File.getFileName();
                if (html5File.getFileSize() > FILE_SIZE_LIMIT) {
                    Notification.show("File rejected. Max 2Mb files are accepted by Sampler",
                                    Notification.Type.WARNING_MESSAGE);
                }else if(!html5File.getType().equals("application/xml") && !html5File.getType().equals("text/xml")){
                	Notification.show("Only XML files are accepted", Notification.Type.WARNING_MESSAGE);
                }else {
                	try {                		
                		File file = new File(System.getProperty("java.io.tmpdir"), fileName);					
	                    final FileOutputStream fas = new FileOutputStream(file);
	                    final StreamVariable streamVariable = new StreamVariable() {
	
	                        @Override
	                        public OutputStream getOutputStream() {
	                            return fas;
	                        }
	
	                        @Override
	                        public boolean listenProgress() {
	                            return false;
	                        }
	
	                        @Override
	                        public void onProgress(final StreamingProgressEvent event) {}
	
	                        @Override
	                        public void streamingStarted(final StreamingStartEvent event) {}
	
	                        @Override
	                        public void streamingFinished(StreamingEndEvent event) {
	                            progressBar.setVisible(false);
	                            UI.getCurrent().getNavigator().navigateTo("ResultView/instance/file/" + fileName);
 	                            //showFile(fileName, html5File.getType(), bas);
	                           
	                        }
	
	                        @Override
	                        public void streamingFailed(final StreamingErrorEvent event) {
	                        	progressBar.setVisible(false);
	                        }
	
	                        @Override
	                        public boolean isInterrupted() {
	                            return false;
	                        }
	                    };
	                    
	                    html5File.setStreamVariable(streamVariable);
	                    progressBar.setVisible(true);
                	} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}                	
                }
            }

        }
    }
    
    @Override
    public AcceptCriterion getAcceptCriterion() {
        return AcceptAll.get();
    }
}
