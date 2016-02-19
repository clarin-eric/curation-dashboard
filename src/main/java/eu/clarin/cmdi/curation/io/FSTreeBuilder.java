package eu.clarin.cmdi.curation.io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDIInstance;
import eu.clarin.cmdi.curation.entities.Collection;


public class FSTreeBuilder implements FileVisitor<Path>{
	
	private Collection curDir = null;
	private Collection root = null;
	
	private Stack<Collection> stack = new Stack<Collection>();
	
	private static Logger _logger = LoggerFactory.getLogger(FSTreeBuilder.class);
				 
	
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		if(curDir != null){
			stack.push(curDir);
			curDir = (Collection) curDir.addChild(new Collection(dir));
		}else
			curDir = new Collection(dir);
				
		return FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		curDir.addChild(new CMDIInstance(file, attrs.size()));
		return FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		_logger.error("Failed to visit " + file);
		throw exc;
	}
	
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		_logger.trace("Finished processing directory: " + dir);
				
		if(stack.isEmpty()){
			root = curDir;
			curDir = null;
		}else{
			curDir = stack.pop();
		}		
		
		return FileVisitResult.CONTINUE;
	}

	public Collection getRoot() {
		return root;
	}

	public void setRoot(Collection root) {
		this.root = root;
	}	
	
}