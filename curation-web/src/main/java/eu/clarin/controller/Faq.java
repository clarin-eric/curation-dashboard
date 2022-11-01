package eu.clarin.controller;

import eu.clarin.helpers.ResponseManager;
import eu.clarin.main.Configuration;
import lombok.extern.slf4j.Slf4j;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/faq")
public class Faq {

    @GetMapping("/")
    public Response getFaq() {
        try {            
            List<Extension> extensions = Arrays.asList(TablesExtension.create());
            Parser parser = Parser.builder()
                    .extensions(extensions)
                    .build();
            HtmlRenderer renderer = HtmlRenderer.builder()
                    .extensions(extensions)
                    .build();
            
            Node document = null;
            URL markdown = new URL(Configuration.DOC_URL + "faq.md");
            try(InputStreamReader in = new InputStreamReader(markdown.openStream())){
               
               document = parser.parseReader(in);
               
            }
            catch(Exception ex) {
               log.error("error in call markdown page with URL {}", markdown);
               throw ex;
            }
 
            return ResponseManager.returnHTML(200, renderer.render(document));
        } 
        catch (IOException e) {
            log.error("Error when reading faq.html: ", e);
            return ResponseManager.returnServerError();
        }
    }
}
