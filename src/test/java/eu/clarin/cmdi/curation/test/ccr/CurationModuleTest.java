package eu.clarin.cmdi.curation.test.ccr;

import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.main.CurationModuleInterface;
import eu.clarin.cmdi.curation.report.Report;

public class CurationModuleTest {

public static void main(String[] args) throws Exception{

	
	Path instancePath1 = FileSystems.getDefault().getPath("D:/data/cmdi/Deutsches_Textarchiv/dta_386.xml");
	Path instancePath2 = FileSystems.getDefault().getPath(
			"D:/data/cmdi/BAS_Repository/oai_BAS_repo_Corpora_aGender_101106.xml");
	
	URL instanceURL = new URL("https://vlo.clarin.eu/data/clarin/results/cmdi/CLARIN_DK_UCPH_Repository/oai_clarin_dk_dkclarin_1257002.xml");
	
	
	Path cmdi = FileSystems.getDefault().getPath("D:/data/cmdi");
	Path test = FileSystems.getDefault().getPath("D:/data/test/test1");

	Path ehu_18 = FileSystems.getDefault().getPath("D:/data/cmdi/Euskal_Herriko_Unibertsitatea");
	Path lbof_295 = FileSystems.getDefault().getPath("D:/data/cmdi/Language_Bank_of_Finland");
	Path eloftw_7K = FileSystems.getDefault().getPath("D:/data/cmdi/Ethnologue_Languages_of_the_World");
	Path bas_23K = FileSystems.getDefault()
		.getPath("D:/data/cmdi/BAS_Repository/oai_BAS_repo_Corpora_aGender_101104.xml");
	Path unname_65K = FileSystems.getDefault().getPath("D:/data/cmdi/Unnamed_provider_at_dspace_library_uu_nl");
	Path mee_240K = FileSystems.getDefault().getPath("D:/data/cmdi/Meertens_Institute_Metadata_Repository");
	
	
	URL profileURL1 = new URL("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1324638957718");
	URL profileURL2 = new URL("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1280305685223/");

	
	Path config = Paths.get("D:/git/clarin-curation-module/src/main/resources/config.properties");
	
	CurationModuleInterface module = new CurationModule(config);
	Configuration.OUTPUT_DIRECTORY = null;
	
	
	Report r = null;

	//r = module.processCMDProfile("clarin.eu:cr1:p_1423750293168");	
	
	//profile is not public
	//r = module.processCMDProfile("clarin.eu:cr1:p_1381926654438");
	
	
	//r = module.processCMDProfile(profileURL2);
	
	//profile is not public
	//r = module.processCMDInstance(instancePath1);
	
	//r = module.processCMDInstance(instanceURL);
	
	r = module.processCollection(test);	
	
	r.marshal(System.out);
}
	
	
}
