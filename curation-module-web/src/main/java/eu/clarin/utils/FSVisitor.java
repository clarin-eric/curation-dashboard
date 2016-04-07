package eu.clarin.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FSVisitor implements FileVisitor<Path>{
	
	private Map<String, ArrayList<String>> fs = new HashMap<>();
	private ArrayList<String> cur = null;

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		cur = new ArrayList<>();
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		cur.add(file.getFileName().toString());
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		// TODO Auto-generated method stub
		return FileVisitResult.TERMINATE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		fs.put(dir.getFileName().toString(), cur);
		return FileVisitResult.CONTINUE;
	}
	
	public Map<String, ArrayList<String>> getFSTree(){
		return fs;
	}

}
