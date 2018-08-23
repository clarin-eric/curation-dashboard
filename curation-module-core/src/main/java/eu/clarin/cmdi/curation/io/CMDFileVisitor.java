package eu.clarin.cmdi.curation.io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDCollection;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.utils.TimeUtils;

public class CMDFileVisitor implements FileVisitor<Path> {

    private static final Logger _logger = LoggerFactory.getLogger(CMDFileVisitor.class);

    private CMDCollection curDir = null;
    private CMDCollection root = null;

    private Stack<CMDCollection> stack = new Stack<CMDCollection>();
    

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		_logger.trace("visiting {}", dir);
		if (curDir != null)
		    stack.push(curDir);
	
		curDir = new CMDCollection(dir);
	
		return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {	
		curDir.addChild(new CMDInstance(file, attrs.size()));
		return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		throw new IOException("Failed to process " + file, exc);
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

		_logger.trace("finished visiting {}, number of files: {}", dir, curDir.getNumOfFiles());
	
		long startTime = System.currentTimeMillis();
		// fire processors for children
		try {
			curDir.generateReport(null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		_logger.info("validation for {} lasted {}", dir, TimeUtils.humanizeToTime(end - startTime));
		
		
		if(!stack.empty()){
		    root = stack.pop();
		    root.addChild(curDir);
		    curDir = root;
		}else{
		    root = curDir;
		}
		
		//else: last elem
		
		return FileVisitResult.CONTINUE;
    }

    public CMDCollection getRoot() {
		return root;
    }

    public void setRoot(CMDCollection root) {
		this.root = root;
    }

}