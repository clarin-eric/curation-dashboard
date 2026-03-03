package eu.clarin.cmdi.curation.web.controller;

import eu.clarin.cmdi.curation.web.conf.WebConfig;
import eu.clarin.cmdi.curation.web.exception.NoSuchReportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping(value = "/instance")
public class InstanceClt {

    @Autowired
    WebConfig conf;

    @RequestMapping (value = "/{instanceReportName}", method = {RequestMethod.GET,  RequestMethod.POST})
    public String getInstance(@RequestHeader(name = "Accept", required = false) Optional<String> acceptHeader, @RequestParam(name = "format", required = false) Optional <String> format, @PathVariable(value = "instanceReportName") String instanceReportName, Model model){

        Path reportPath = conf.getDirectory()
                .getOut()
                .resolve("html")
                .resolve("instance");

        String basename = FilenameUtils.getBaseName(instanceReportName);

        if(format.isPresent() ||
                ( acceptHeader.isPresent() &&
                        (acceptHeader.get().startsWith("application/json") ||
                                acceptHeader.get().startsWith("application/xml") ||
                                acceptHeader.get().startsWith("text/xml")
                        )
                )
        ) {

            return "forward:/download/instance/" + basename;
        }

        reportPath = reportPath.resolve(basename + ".html");

        if(Files.notExists(reportPath)){

            throw new NoSuchReportException();
        }

        model.addAttribute("insert", reportPath.toString());

        return "generic";
    }
}
