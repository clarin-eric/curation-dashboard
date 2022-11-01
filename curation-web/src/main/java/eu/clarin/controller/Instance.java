package eu.clarin.controller;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.ResponseManager;
import eu.clarin.main.Configuration;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.regex.Pattern;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/instance")
public class Instance {

    @GetMapping("/{instanceName}")
    public Response getInstance(@PathVariable("instanceName") String instanceName) {

        String[] split = instanceName.split("\\.");
        if (split.length != 2) {
            return ResponseManager.returnError(400, "Instance name must end with either '.xml' or '.html'.");
        }
        String extension = split[1];
        String location;

        try {

            switch (extension) {
                case "xml":
                    location = Configuration.OUTPUT_DIRECTORY + "/xml/instances/";
                    String instanceXML = FileManager.readFile(location + instanceName);
                    return ResponseManager.returnResponse(200, instanceXML, MediaType.TEXT_XML);
                case "html":
                    location = Configuration.OUTPUT_DIRECTORY + "/html/instances/";
                    String instanceHTML = FileManager.readFile(location + instanceName);

                    //replace to put the url based on the server (this way xml and html files are not server url dependent)
                    String xmlLink = Configuration.BASE_URL + "instance/" + split[0] + ".xml";
                    xmlLink = "<a href='"+xmlLink+"'>"+xmlLink+"</a>";
                    instanceHTML = instanceHTML.replaceFirst(Pattern.quote("selfURLPlaceHolder"), xmlLink);

                    return ResponseManager.returnHTML(200, instanceHTML);
                default:
                    return ResponseManager.returnError(400, "Instance name must end with either '.xml' or '.html'.");
            }
        } catch (IOException e) {
            log.error("There was an error reading the instance: " + instanceName, e);
            return ResponseManager.returnError(404, "The instance " + instanceName + " doesn't exist.");
        }

    }
}
