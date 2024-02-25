package eu.clarin.cmdi.curation.cr.profile_parser;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;

import eu.clarin.cmdi.curation.ccr.CCRService;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode.Component;
import eu.clarin.cmdi.curation.cr.profile_parser.CRElement.NodeType;

/**
 * The type Cmdi 1 2 profile parser.
 */
class CMDI1_2_ProfileParser extends ProfileParser {

    private static final String ENVELOPE_URL = "https://infra.clarin.eu/CMDI/1.2/xsd/cmd-envelop.xsd";

    private static ParsedProfile envelope;

    /**
     * Instantiates a new Cmdi 1 2 profile parser.
     *
     * @param ccrService the ccr service
     */
    public CMDI1_2_ProfileParser(CCRService ccrService) {

        super(ccrService);

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
    protected CRElement processNameAttributeNode() throws VTDException {
        // ref attribute
        String ref = extractAttributeValue("ref");
        if ("cmd:ComponentId".equals(ref)) {
            CRElement elem = new CRElement();
            elem.isLeaf = true;
            elem.lvl = vn.getCurrentDepth();
            elem.type = NodeType.COMPONENT;
            elem.ref = extractAttributeValue("fixed");
            return elem;
        }

        // name attribute
        String name = extractAttributeValue("name");
        if (name != null) {
            String concept = extractAttributeValue("cmd:ConceptLink");
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
     * Create parsed profile parsed profile.
     *
     * @param profileHeader the profile header
     * @param nodes         the nodes
     * @return the parsed profile
     * @throws NoProfileCacheEntryException the no profile cache entry exception
     */
    @Override
    protected ParsedProfile createParsedProfile(ProfileHeader profileHeader, Collection<CRElement> nodes)
            throws NoProfileCacheEntryException {


        // add xpaths from envelope
        if (envelope == null) {
            CMDI1_1_ProfileParser envelopeParser = new CMDI1_1_ProfileParser(ccrService);
            VTDGen vg = new VTDGen();

            vg.parseHttpUrl(ENVELOPE_URL, true);

            envelope = envelopeParser.parse(vg.getNav(), new ProfileHeader());
        }

        Map<String, CMDINode> xpathNode = new LinkedHashMap<>(envelope.getXpathNode());
        Map<String, CMDINode> xpathElementNode = new LinkedHashMap<>(envelope.getXpathElementNode());
        Map<String, CMDINode> xpathComponentNode = new LinkedHashMap<>(envelope.getXpathComponentNode());

        nodes.stream().filter(n -> n.isLeaf || n.type == NodeType.COMPONENT).forEach(node -> {
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

        });

        return new ParsedProfile(
                profileHeader,
                xpathNode,
                xpathElementNode,
                xpathComponentNode
        );
    }
}
