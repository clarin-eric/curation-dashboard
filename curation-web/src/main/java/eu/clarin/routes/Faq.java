package eu.clarin.routes;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.ResponseManager;
import eu.clarin.main.Configuration;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Path("/faq")
public class Faq {

    @GET
    @Path("/")
    public Response getFaq() {
        try {
            String faq = FileManager.readFile(Configuration.VIEW_RESOURCES_PATH + "/html/faq.html");
            
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
 
            return ResponseManager.returnHTML(200, faq.replace("<!-- replacement -->", renderer.render(document)));
        } catch (IOException e) {
            log.error("Error when reading faq.html: ", e);
            return ResponseManager.returnServerError();
        }
    }

    @GET
    @Path("/faq.md")
    public Response getFaqMD() {

        String faqJsonPath = Configuration.VIEW_RESOURCES_PATH + "/markdown/faq.md";
        final InputStream fileInStream;
        try {
            fileInStream = new FileInputStream(faqJsonPath);
            return ResponseManager.returnFile(200, fileInStream, "text/markdown", "faq.md");

        } catch (FileNotFoundException e) {
            log.error("There was an error getting the faq.md file: ", e);
            return ResponseManager.returnServerError();
        }
    }
}
