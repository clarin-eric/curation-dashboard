package eu.clarin.cmdi.curation.entities;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityTree implements FileVisitor<Path> {

    private static final Logger _logger = LoggerFactory.getLogger(EntityTree.class);

    private Directory curDir = null;
    private Directory root = null;

    private Stack<Directory> stack = new Stack<Directory>();
    

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
	_logger.trace("visiting {}", dir);
	if (curDir != null)
	    stack.push(curDir);

	curDir = new Directory(dir);

	return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	CurationEntity entity;
	if (file.endsWith(".xsd"))
	    entity = new CMDIProfile(file, attrs.size());
	else // restricted only to CMDI files
	    entity = new CMDIRecord(file, attrs.size());

	curDir.addChild(entity);
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
	curDir.genReport();
	long end = System.currentTimeMillis();
	_logger.info("validation for {} lasted {} ms", dir, end - startTime);
	
	
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

    public Directory getRoot() {
	return root;
    }

    public void setRoot(Directory root) {
	this.root = root;
    }

}