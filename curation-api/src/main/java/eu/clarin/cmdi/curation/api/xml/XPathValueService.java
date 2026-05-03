/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.xml;

import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The type X path value service.
 */
@Service
@Slf4j
public class XPathValueService {

    public Collection<ValueTriple> getValueTriples(Path xmlFilePath) {
        
        Vector<String> tags = new Vector<>();
        List<ValueTriple> valuesTriples = new ArrayList<>();

        VTDGen vg = new VTDGen();
        vg.parseFile(xmlFilePath.toString(), true);

        VTDNav nav = vg.getNav();

        IntStream.rangeClosed(0, nav.getTokenCount()).forEach(vtdIndex -> {

                    switch (nav.getTokenType(vtdIndex)) {

                        case VTDNav.TOKEN_ATTR_VAL:
                            try {
                                valuesTriples.add(
                                        new ValueTriple(
                                                vtdIndex,
                                                IntStream.rangeClosed(0, nav.getTokenDepth(vtdIndex)).mapToObj(tags::get).collect(Collectors.joining("/", "/", "/@" + nav.toString(vtdIndex - 1))),
                                                nav.toString(vtdIndex)
                                        )
                                );
                            }
                            catch (NavException e) {
                                log.error("can't read token number {} from file {}", vtdIndex, xmlFilePath);
                            }
                            break;
                        case VTDNav.TOKEN_STARTING_TAG:
                            try {
                                if (nav.getTokenDepth(vtdIndex) < tags.size()) {
                                    tags.add(nav.getTokenDepth(vtdIndex), nav.toString(vtdIndex));
                                }
                                else {
                                    tags.add(nav.toString(vtdIndex));
                                }
                            }
                            catch (NavException e) {
                                log.error("can't read token number {} from file {}", vtdIndex, xmlFilePath);
                            }
                            break;
                        case VTDNav.TOKEN_CHARACTER_DATA:

                            try {
                                valuesTriples.add(
                                        new ValueTriple(
                                                vtdIndex,
                                                IntStream.rangeClosed(0, nav.getTokenDepth(vtdIndex)).mapToObj(tags::get).collect(Collectors.joining("/", "/", "/text()")),
                                                nav.toString(vtdIndex)
                                        )
                                    );
                            }
                            catch (NavException e) {
                                log.error("can't read token number {} from file {}", vtdIndex, xmlFilePath);
                            }
                            break;
                        default:
                    }
                });
        
        return valuesTriples;
    }
 
    public record ValueTriple(int vtdIndex, String xPath, String value) {
    }
}
