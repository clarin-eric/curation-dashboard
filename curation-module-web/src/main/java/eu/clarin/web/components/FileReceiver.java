package eu.clarin.web.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

public class FileReceiver  implements Receiver, StartedListener, ProgressListener, SucceededListener, FailedListener{
	
	private String uploadedFile;
	private boolean failed = false;
	
	private final boolean isInstance;
	
	public  FileReceiver(boolean isInstance) {
		this.isInstance = isInstance;
	}

	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		FileOutputStream fos = null;
		try{
			File file = File.createTempFile(filename, null);
			fos = new FileOutputStream(file);
			uploadedFile = file.getAbsolutePath();
			return fos;			
		}catch(IOException e){
			new Notification("Unable to upload file" + "</br>" + e.getMessage());
			return null;
		}	
	}
	
	@Override
	public void uploadFailed(FailedEvent event) {
		failed = true;
	}

	@Override
	public void uploadSucceeded(SucceededEvent event) {
		// all processing is done in the ResultView class
		UI.getCurrent().getNavigator().navigateTo("ResultView/" + (isInstance? "instance" : "profile") + "/file/" + uploadedFile);
	}

	@Override
	public void updateProgress(long readBytes, long contentLength) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void uploadStarted(StartedEvent event) {
		// TODO Auto-generated method stub		
	}

	public String getUploadedFile() {
		return uploadedFile;
	}

	public boolean isFailed() {
		return failed;
	}
}
