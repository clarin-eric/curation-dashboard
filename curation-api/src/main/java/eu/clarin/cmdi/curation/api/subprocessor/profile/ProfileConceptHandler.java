/**
 *
 */
package eu.clarin.cmdi.curation.api.subprocessor.profile;

import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.Detail.Severity;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ComponentReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ConceptReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoCRCacheEntryException;
import eu.clarin.cmdi.curation.cr.exception.PPHCacheException;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The type Profile concept handler.
 */
@Slf4j
@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ProfileConceptHandler extends AbstractSubprocessor<CMDProfile, CMDProfileReport> {

    @Autowired
    private CRService crService;

    /**
     * Process.
     *
     * @param profile the profile
     * @param report  the report
     */
    public void process(CMDProfile profile, CMDProfileReport report) throws MalFunctioningProcessorException {

        ParsedProfile parsedProfile;

        try {

            parsedProfile = crService.getParsedProfile(profile.getSchemaLocation());
        }
        catch (NoCRCacheEntryException e) {

            report.details.add(new Detail(Severity.FATAL, "concept", "can't parse profile '" + profile.getSchemaLocation() + "'"));
            log.debug("can't parse profile '{}'", profile.getSchemaLocation());
            return;
        }
        catch (CRServiceStorageException | CCRServiceNotAvailableException | PPHCacheException e) {

            throw new MalFunctioningProcessorException(e);
        }

        report.conceptReport = new ConceptReport();

        final Map<String, ConceptReport.Concept> conceptMap = new HashMap<>();

        parsedProfile
                .xpathElementNode()
                .entrySet()
                .stream()
                .filter(entrySet -> entrySet.getKey().startsWith("/cmd:CMD/cmd:Components/"))
                .map(Entry::getValue)
                .forEach(n -> {
                    report.conceptReport.total++;

                    if (n.isRequired)
                        report.conceptReport.required++;

                    if (n.concept != null) {
                        report.conceptReport.withConcept++;


                        conceptMap.computeIfAbsent(n.concept.getUri(), k -> new ConceptReport.Concept(n.concept)).count++;

                    }
                });

        report.conceptReport.unique = conceptMap.size();
        report.conceptReport.percWithConcept = (report.conceptReport.total != 0 ? (double) report.conceptReport.withConcept / report.conceptReport.total : 0.0);
        report.conceptReport.concepts = conceptMap.values();

        report.conceptReport.score = report.conceptReport.percWithConcept;
        report.score += report.conceptReport.score;

        report.componentReport = new ComponentReport();

        Map<String, ComponentReport.Component> componentMap = new HashMap<>();

        parsedProfile.xpathComponentNode().values().forEach(crc -> {
            report.componentReport.total++;

            if (crc.isRequired) {
                report.componentReport.required++;
            }

            componentMap
                    .computeIfAbsent(crc.component.id, k -> new ComponentReport.Component(crc.component.id, crc.component.name))
                    .count++;

        });

        report.componentReport.unique = componentMap.size();

        report.componentReport.components = componentMap.values();
    }
}
