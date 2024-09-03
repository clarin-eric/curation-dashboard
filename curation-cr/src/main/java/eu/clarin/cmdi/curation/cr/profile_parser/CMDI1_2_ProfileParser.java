package eu.clarin.cmdi.curation.cr.profile_parser;

import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.curation.ccr.CCRService;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode.Component;
import eu.clarin.cmdi.curation.cr.profile_parser.CRElement.NodeType;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

/**
 * The type Cmdi 1 2 profile parser.
 */
@Service
public class CMDI1_2_ProfileParser extends ProfileParser {

    public static final String ENVELOPE_URL = "https://infra.clarin.eu/CMDI/1.2/xsd/cmd-envelop.xsd";

    private final ApplicationContext ctx;


    /**
     * Instantiates a new Cmdi 1 2 profile parser.
     *
     * @param ccrService the ccr service
     */
    public CMDI1_2_ProfileParser(CCRService ccrService, ApplicationContext ctx) {

        super(ccrService);

        this.ctx = ctx;
    }

    /**
     * Gets cmd version.
     *
     * @return the cmd version
     */
    @Override
    protected String getCMDVersion() {
        return "1.2";
    }

    /**
     * Concept attribute name string.
     *
     * @return the string
     */
    @Override
    protected String conceptAttributeName() {
        return "cmd:ConceptLink";
    }

    /**
     * Process name attribute node cr element.
     *
     * @return the cr element
     * @throws VTDException the vtd exception
     */
    @Override
    protected CRElement processNameAttributeNode(VTDNav vn) throws VTDException {
        // ref attribute
        String ref = extractAttributeValue(vn,"ref");
        if ("cmd:ComponentId".equals(ref)) {
            CRElement elem = new CRElement();
            elem.isLeaf = true;
            elem.lvl = vn.getCurrentDepth();
            elem.type = NodeType.COMPONENT;
            elem.ref = extractAttributeValue(vn,"fixed");
            return elem;
        }

        // name attribute
        String name = extractAttributeValue(vn,"name");
        if (name != null) {
            String concept = extractAttributeValue(vn,"cmd:ConceptLink");
            CRElement elem = new CRElement();
            elem.isLeaf = true;
            elem.lvl = vn.getCurrentDepth();
            elem.type = NodeType.ATTRIBUTE;
            elem.ref = concept;
            elem.name = name;
            return elem;
        }

        return null;
    }

    /**
     * Create parsed profile.
     *
     *
     * @param nodes         the nodes
     * @return the parsed profile
     * @throws CCRServiceNotAvailableException
     * @throws CRServiceStorageException
     */
    @Override
    protected void fillMaps(Collection<CRElement> nodes, Map<String, CMDINode> xpathNode, Map<String, CMDINode> xpathElementNode, Map<String, CMDINode> xpathComponentNode) throws CCRServiceNotAvailableException, CRServiceStorageException {

        ProfileParser parser = ctx.getBean(CMDI1_1_ProfileParser.class);

        VTDGen vg = new VTDGen();

        vg.parseHttpUrl(ENVELOPE_URL, true);

        ParsedProfile envelope = parser.parse(vg.getNav(), ENVELOPE_URL, new ProfileHeader("1.x", ENVELOPE_URL, "", "", "", "", true));

        xpathNode.putAll(envelope.xpathNode());
        xpathElementNode.putAll(envelope.xpathElementNode());
        xpathComponentNode.putAll(envelope.xpathComponentNode());

        for(CRElement node : nodes){
            if(node.isLeaf || node.type == NodeType.COMPONENT){
                StringBuilder xpath = new StringBuilder();
                CRElement parent = node.parent;
                while (parent != null) {
                    xpath.insert(0, "cmdp:" + parent.name + "/");
                    parent = parent.parent;
                }
                xpath = new StringBuilder("/cmd:CMD/cmd:Components/" + xpath
                        + (node.type == NodeType.ATTRIBUTE || node.type == NodeType.CMD_VERSION_ATTR ? "@" + node.name
                        : "cmdp:" + node.name + "/text()"));

                CMDINode cmdiNode = new CMDINode();
                cmdiNode.isRequired = node.isRequired;

                if (node.type == NodeType.COMPONENT) {

                    cmdiNode.component = new Component(node.name, node.ref);

                    xpathComponentNode.put(xpath.toString(), cmdiNode);
                } else {

                    cmdiNode.concept = createConcept(node.ref);

                    xpathElementNode.put(xpath.toString(), cmdiNode);
                }

                xpathNode.put(xpath.toString(), cmdiNode);
            }
        }
    }
}
