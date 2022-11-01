package eu.clarin.controller;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.ResponseManager;
import eu.clarin.main.Configuration;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//this route is to make records available to view
@RestController
@RequestMapping("/record")
public class Record {

    @GetMapping("/{filepath : .+}")
    public Response handleView(@PathVariable("filepath") String filePath) {
        try {
        	// the next three lines assure that the path is a sub-path of RECORDS_PATH
        	java.nio.file.Path path = Paths.get(Configuration.DATA_DIRECTORY.toString(), filePath).toRealPath(LinkOption.NOFOLLOW_LINKS);
        	if(!path.startsWith(Configuration.DATA_DIRECTORY))
        		return ResponseManager.returnError(404, "Path not permitted.");
        	
            String file = FileManager.readFile(path.toString());
            return ResponseManager.returnResponse(200, file, MediaType.TEXT_XML);

        } catch (IOException e) {
            return ResponseManager.returnError(404, "Requested file doesn't exist.");
        }

    }

}
