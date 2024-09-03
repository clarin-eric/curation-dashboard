/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.web.controller;

import eu.clarin.cmdi.curation.web.conf.WebConfig;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * The type Help ctl.
 */
@Controller
@RequestMapping("/help")
@Slf4j
public class HelpCtl {

   /**
    * The Conf.
    */
   @Autowired
   WebConfig conf;

   /**
    * Gets renderd markdown.
    *
    * @param model the model
    * @return the renderd markdown
    */
   @GetMapping()
   public String getRenderdMarkdown(Model model) {
      
      String markdownUrlString = conf.getDocUrl() + "/help.md";
      
          
      List<Extension> extensions = Arrays.asList(TablesExtension.create());
      Parser parser = Parser.builder()
              .extensions(extensions)
              .build();
      HtmlRenderer renderer = HtmlRenderer.builder()
              .extensions(extensions)
              .build();
         
      Node document = null;

      try(InputStreamReader in = new InputStreamReader(new URL(markdownUrlString).openStream())){
         
         document = parser.parseReader(in);
         
         model.addAttribute("insert", "fragments/markdown.html");
         model.addAttribute("renderedMarkdown", renderer.render(document));
      }
      catch(MalformedURLException e) {
         
         log.error("mal formed URL '{}' for markup download", markdownUrlString);
         
      }
      catch(IOException e) {

         log.error("error in call markdown page with URL {}", markdownUrlString);
      
      }
      
      return "generic";
      
   }
}
