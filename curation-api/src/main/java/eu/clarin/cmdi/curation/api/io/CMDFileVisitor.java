package eu.clarin.cmdi.curation.api.io;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;

@Slf4j
public class CMDFileVisitor implements FileVisitor<Path> {

    private CMDCollection root = null;


    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        log.trace("visiting {}", dir);

        if(this.root == null) {
           this.root = new CMDCollection(dir);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        root.addChild(new CMDInstance(file, attrs.size()));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        throw new IOException("Failed to process " + file, exc);
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

        log.trace("finished visiting {}, number of files: {}", dir, root.getNumOfFiles());

        return FileVisitResult.CONTINUE;
    }

    public CMDCollection getRoot() {
        return root;
    }

    public void setRoot(CMDCollection root) {
        this.root = root;
    }
}