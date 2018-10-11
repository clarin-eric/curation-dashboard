package eu.clarin.cmdi.curation.subprocessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.instance_parser.ParsedInstance;
import eu.clarin.cmdi.curation.instance_parser.ParsedInstance.InstanceNode;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;


public class InstanceHeaderProcessor extends CMDSubprocessor {

	private static final String PROFILE_ID_FORMAT = "clarin\\.eu:cr1:p_[0-9]+";
	private static final Pattern PROFILE_ID_PATTERN = Pattern.compile(PROFILE_ID_FORMAT);
	
	boolean missingSchema = false;
	boolean schemaInCR = false;
	boolean missingMdprofile = false;
	boolean invalidMdprofile = false;
	boolean missingMdCollectionDisplayName = false;
	boolean missingMdSelfLink = false;

	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception {
		CRService crService = new CRService();
		ParsedInstance parsedInstance = entity.getParsedInstance();

		InstanceNode instanceNode = parsedInstance.getNodes().stream().filter(n -> n.getXpath().equals("/cmd:CMD/@xsi:schemaLocation")).findFirst().orElse(null); 
		String schemaLocation = instanceNode != null? instanceNode.getValue() : null;
		String profileIdFromSchema = null;
		if (schemaLocation == null || schemaLocation.isEmpty()){
			instanceNode = parsedInstance.getNodes().stream().filter(n -> n.getXpath().equals("/cmd:CMD/@xsi:noNamespaceSchemaLocation")).findFirst().orElse(null);
			schemaLocation = instanceNode != null? instanceNode.getValue() : null;
		}else{
			String[] locations = schemaLocation.split(" ");
			schemaLocation = locations[locations.length - 1];
			//try to extract profileId from schemaLocation
			profileIdFromSchema = extractProfile(schemaLocation);
		} 

		
		instanceNode = parsedInstance.getNodes().stream().filter(n -> n.getXpath().equals("/cmd:CMD/@CMDVersion")).findFirst().orElse(null);
		String cmdVersion = instanceNode != null? instanceNode.getValue() : null;

		instanceNode = parsedInstance.getNodes().stream().filter(n -> n.getXpath().equals("/cmd:CMD/cmd:Header/cmd:MdProfile/text()")).findFirst().orElse(null);
		String mdprofile = instanceNode != null? instanceNode.getValue() : null;
		instanceNode = parsedInstance.getNodes().stream().filter(n -> n.getXpath().equals("/cmd:CMD/cmd:Header/cmd:MdCollectionDisplayName/text()")).findFirst().orElse(null);
		String mdCollectionDisplayName = instanceNode != null? instanceNode.getValue() : null;
		instanceNode = parsedInstance.getNodes().stream().filter(n -> n.getXpath().equals("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()")).findFirst().orElse(null);
		String mdSelfLink = instanceNode != null? instanceNode.getValue() : null;

		
		missingSchema = schemaLocation == null || schemaLocation.isEmpty();
		missingMdprofile = mdprofile == null || mdprofile.isEmpty();
		missingMdCollectionDisplayName = mdCollectionDisplayName == null || mdCollectionDisplayName.isEmpty();
		missingMdSelfLink = mdSelfLink == null || mdSelfLink.isEmpty();

		if (missingSchema && missingMdprofile)
			throw new Exception("Unable to process " + entity + ", both schema and profile are not specified");

		if (missingSchema){
			schemaLocation = Configuration.vloConfig.getComponentRegistryProfileSchema(mdprofile);
			addMessage(Severity.ERROR, "Attribute schemaLocation is missing. " + schemaLocation + " is assumed");
		}else
			schemaInCR = crService.isSchemaCRResident(schemaLocation);

		if (cmdVersion != null && !cmdVersion.isEmpty() && !cmdVersion.equals("1.2"))
			addMessage(Severity.WARNING, "Current CMD version is 1.2 but this recordName is using " + cmdVersion);

		if (!missingMdprofile) {
			if(!mdprofile.matches(PROFILE_ID_FORMAT)){			
				invalidMdprofile = true;
				addMessage(Severity.ERROR, "Format for value in the element CMD/Header/MdProfile must be: clarin.eu:cr1:p_xxxxxxxxxxxxx!");
				mdprofile = extractProfile(mdprofile);
			}
			
			if(profileIdFromSchema != null && !mdprofile.equals(profileIdFromSchema)){
				invalidMdprofile = true;
				addMessage(Severity.ERROR, "ProfileId from CMD/Header/MdProfile: " + mdprofile + " and from schemaLocation: " + profileIdFromSchema + " must match!");
				mdprofile = profileIdFromSchema; //it is important to continue with ID from schemalocation otherwise cache will create extra entry
			}
		}

		if (missingMdprofile){
			addMessage(Severity.ERROR, "Value for CMD/Header/MdProfile is missing or invalid");
			mdprofile = profileIdFromSchema;
		}

		if (missingMdCollectionDisplayName)
			addMessage(Severity.ERROR, "Value for CMD/Header/MdCollectionDisplayName is missing");

		if (missingMdSelfLink)
			addMessage(Severity.ERROR, "Value for CMD/Header/MdSelfLink is missing");
		// collect mdSelfLinks when assessing collection
		else if (Configuration.COLLECTION_MODE) {
			if(!CMDInstance.mdSelfLinks.add(mdSelfLink))
				CMDInstance.duplicateMDSelfLink.add(mdSelfLink);
		}

		// at this point profile will be processed and cached		
		report.header = crService.createProfileHeader(schemaLocation, cmdVersion, false);
		report.header.id = mdprofile;
		report.header.cmdiVersion = cmdVersion;
		report.fileReport.collection = mdCollectionDisplayName;
		
		report.profileScore = crService.getScore(report.header);
		
		report.addSegmentScore(new Score(report.profileScore, CRService.PROFILE_MAX_SCORE, "profiles-score", null));

	}


	@Override
	public Score calculateScore(CMDInstanceReport report) {
		double score = 0;
		
		//schema exists
		if (!missingSchema){
			score++; // * weight
			if(schemaInCR)//schema comes from Component Registry
				score++;
		}
		
		//mdprofile exists and in correct format
		if (!missingMdprofile && !invalidMdprofile)
			score++;
		if (!missingMdCollectionDisplayName)
			score++;
		if (!missingMdSelfLink)
			score++;
		
		return new Score(score, 5.0, "cmd-header-schema", msgs);
	}
	
	
	private String extractProfile(String str){
		Matcher m = PROFILE_ID_PATTERN.matcher(str);
		return m.find() ? m.group() : null;

	}

}
