package eu.clarin.cmdi.curation.web.controller;

import eu.clarin.cmdi.curation.web.conf.WebConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@RequestMapping("/img")
public class ImageCtl {

    @Autowired
    WebConfig conf;

    @GetMapping("/{curationEntityType}/{fileName}")
    public ResponseEntity<byte[]> getImage(@PathVariable("curationEntityType") String curationEntityType,@PathVariable("fileName") String fileName) throws IOException {

        Path imageFilePath = conf.getDirectory().getOut().resolve("img").resolve(curationEntityType).resolve(fileName).normalize();

        if (imageFilePath.startsWith(this.conf.getDirectory().getOut()) && Files.exists(imageFilePath)) {

            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(Files.readAllBytes(imageFilePath));
        }

        return ResponseEntity.notFound().build();
    }
}
