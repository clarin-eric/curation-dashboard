package eu.clarin.cmdi.curation.api.subprocessor.instance;

import eu.clarin.cmdi.curation.api.CurationModule;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.Detail.Severity;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceHeaderReport;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.cr.CRServiceImpl;
import eu.clarin.cmdi.curation.cr.conf.CRConfig;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * The type Instance header processor.
 */
@Slf4j
@Component
public class InstanceHeaderProcessor extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {
    /**
     * The Conf.
     */

    private final CRConfig crConfig;

    private final CurationModule curationModule;

    public InstanceHeaderProcessor(CRConfig crConfig, CurationModule curationModule) {

        this.crConfig = crConfig;

        this.curationModule = curationModule;
    }

    /**
     * Process.
     *
     * @param instance       the instance
     * @param instanceReport the instance report
     */
    @Override
    public void process(CMDInstance instance, CMDInstanceReport instanceReport) {

        if (instance.getCmdiData().isEmpty()) {

            log.debug("can't create CMDData object from file '{}'", instance.getPath());
            instanceReport.details
                    .add(new Detail(Severity.FATAL, "file", "can't parse file '" + instance.getPath().getFileName() + "'"));
            instanceReport.isProcessable = false;

            return;
        }

        instanceReport.instanceHeaderReport = new InstanceHeaderReport();

        Map<String, List<ValueSet>> facetValuesMap = instance.getCmdiData().get().getDocument();


        if (facetValuesMap.containsKey("curation_schemaLocation") && !facetValuesMap.get("curation_schemaLocation").isEmpty()) {
            String[] schemaLocationArray = facetValuesMap.get("curation_schemaLocation").getFirst().getValue().split(" ");
            instanceReport.instanceHeaderReport.schemaLocation = (schemaLocationArray[schemaLocationArray.length - 1]);
        } else if (facetValuesMap.containsKey("curation_noNamespaceSchemaLocation") && !facetValuesMap.get("curation_noNamespaceSchemaLocation").isEmpty()) {
            instanceReport.instanceHeaderReport.schemaLocation = facetValuesMap.get("curation_noNamespaceSchemaLocation").getFirst().getValue();
        }

        if (facetValuesMap.containsKey("curation_mdProfile") && !facetValuesMap.get("curation_mdProfile").isEmpty()) {
            instanceReport.instanceHeaderReport.mdProfile = facetValuesMap.get("curation_mdProfile").getFirst().getValue();
        }

        if (facetValuesMap.containsKey("collection") && !facetValuesMap.get("collection").isEmpty()) {
            instanceReport.instanceHeaderReport.mdCollectionDisplayName = facetValuesMap.get("collection").getFirst().getValue();
        }

        if (facetValuesMap.containsKey("_selfLink") && !facetValuesMap.get("_selfLink").isEmpty()) {
            instanceReport.instanceHeaderReport.mdSelfLink = facetValuesMap.get("_selfLink").getFirst().getValue();
        }

        if (instanceReport.instanceHeaderReport.schemaLocation == null) { // no schemaLocation

            if (instanceReport.instanceHeaderReport.mdProfile == null || !instanceReport.instanceHeaderReport.mdProfile.matches(CRServiceImpl.PROFILE_ID_FORMAT)) {

                log.debug("Unable to process " + instance.getPath() + ", both schema and profile are not specified or invalid");
                instanceReport.details.add(new Detail(Severity.FATAL, "header", "Unable to process " + instance.getPath().getFileName() + ", both schema and profile are not specified"));
                instanceReport.isProcessable = false;

                return;

            } else {

                instanceReport.instanceHeaderReport.score++; // Availability if mdProfile
                instanceReport.details.add(new Detail(Severity.WARNING, "header", "Attribute schemaLocation is missing. Generating schema location from MDProfile"));

                instanceReport.instanceHeaderReport.schemaLocation = crConfig.getRestApi() + "/" + instanceReport.instanceHeaderReport.mdProfile + "/xsd";
            }
        } else {//schemaLocation available

            instanceReport.instanceHeaderReport.score++; // availability of schemaLocation

            if (instanceReport.instanceHeaderReport.mdProfile == null) {

                instanceReport.details.add(new Detail(Severity.WARNING, "header", "Value for CMD/Header/MdProfile is missing or invalid"));

            } else {


                if (instanceReport.instanceHeaderReport.mdProfile.matches(CRServiceImpl.PROFILE_ID_FORMAT)) {
                    instanceReport.instanceHeaderReport.score++; // Availability of valid mdProfile

                    String profileIdFromSchema = extractProfile(instanceReport.instanceHeaderReport.schemaLocation);

                    if (!instanceReport.instanceHeaderReport.mdProfile.equals(profileIdFromSchema)) {

                        instanceReport.details.add(new Detail(Severity.ERROR, "header", "ProfileId from CMD/Header/MdProfile: " + instanceReport.instanceHeaderReport.mdProfile
                                + " and from schemaLocation: " + profileIdFromSchema + " must match!"));

                    }
                } else {

                    instanceReport.details.add(new Detail(Severity.ERROR, "header",
                            "Format for value in the element /cmd:CMD/cmd:Header/cmd:MdProfile must be: clarin.eu:cr1:p_xxxxxxxxxxxxx!"));

                }

            }
        }

        if (instanceReport.instanceHeaderReport.mdCollectionDisplayName != null) {
            instanceReport.instanceHeaderReport.score++;
        } else {
            instanceReport.details.add(new Detail(Severity.WARNING, "header", "Value for CMD/Header/MdCollectionDisplayName is missing"));
        }


        if (instanceReport.instanceHeaderReport.mdSelfLink != null) {
            instanceReport.instanceHeaderReport.score++;
        } else {
            instanceReport.details.add(new Detail(Severity.WARNING, "header", "Value for CMD/Header/MdSelfLink is missing"));
        }
        /*
         * else if ("collection".equalsIgnoreCase(conf.getMode()) ||
         * "all".equalsIgnoreCase(conf.getMode())) {// collect mdSelfLinks when
         * assessing collection if (!CMDInstance.mdSelfLinks.add(mdSelfLink))
         * CMDInstance.duplicateMDSelfLink.add(mdSelfLink); }
         */

        // at this point profile will be processed and cached

        String schemaLocation = instanceReport.instanceHeaderReport.schemaLocation != null ?
                instanceReport.instanceHeaderReport.schemaLocation :
                crConfig.getRestApi() + "/" + instanceReport.instanceHeaderReport.mdProfile + "/xsd";

        CMDProfileReport profileReport = curationModule.processCMDProfile(schemaLocation);

        instanceReport.profileHeaderReport = profileReport.headerReport;

        instanceReport.profileScore = profileReport.score;
        instanceReport.instanceScore += profileReport.score;

        instanceReport.instanceScore += instanceReport.instanceHeaderReport.score;
    }

    private String extractProfile(String str) {
        Matcher m = CRServiceImpl.PROFILE_ID_PATTERN.matcher(str);
        return m.find() ? m.group() : null;

    }
}
