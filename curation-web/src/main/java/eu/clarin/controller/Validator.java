package eu.clarin.controller;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.ResponseManager;
import eu.clarin.main.Configuration;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/")
public class Validator {

    @Context
    HttpServletRequest request;

    @GetMapping("/")
    public Response getValidator() {
        try {
            String instance = FileManager.readFile(Configuration.VIEW_RESOURCES_PATH + "/html/validator.html");

            return ResponseManager.returnHTML(200, instance);
        } catch (IOException e) {
            log.error("Error when reading validator.html: ", e);
            return ResponseManager.returnServerError();
        }
    }
}
