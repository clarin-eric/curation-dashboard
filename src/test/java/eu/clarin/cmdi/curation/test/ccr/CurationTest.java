/**
 * 
 */
package eu.clarin.cmdi.curation.test.ccr;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import eu.clarin.cmdi.curation.main.Config;
import eu.clarin.cmdi.curation.main.Curator;

/**
 * @author dostojic
 *
 */
public class CurationTest {

    public static void main(String[] args) {
	
	Config.initDefault();
	Config.setOUTPUT_DIRECTORY(null);

	Path path1 = FileSystems.getDefault().getPath(
		"Q:/data/vlo/results/cmdi/BAS_Repository/oai_BAS_repo_Corpora_aGender_101106.xml");
	Path path2 = FileSystems.getDefault().getPath("D:/data/cmdi/Deutsches_Textarchiv/dta_386.xml");
	Path cmdi = FileSystems.getDefault().getPath("D:/data/cmdi");
	Path test = FileSystems.getDefault().getPath("D:/data/test/test1");

	Path ehu_18 = FileSystems.getDefault().getPath("D:/data/cmdi/Euskal_Herriko_Unibertsitatea");
	Path lbof_295 = FileSystems.getDefault().getPath("D:/data/cmdi/Language_Bank_of_Finland");
	Path eloftw_7K = FileSystems.getDefault().getPath("D:/data/cmdi/Ethnologue_Languages_of_the_World");
	Path bas_23K = FileSystems.getDefault()
		.getPath("D:/data/cmdi/BAS_Repository/oai_BAS_repo_Corpora_aGender_101104.xml");
	Path unname_65K = FileSystems.getDefault().getPath("D:/data/cmdi/Unnamed_provider_at_dspace_library_uu_nl");
	Path mee_240K = FileSystems.getDefault().getPath("D:/data/cmdi/Meertens_Institute_Metadata_Repository");
	
	
	Path profile1 =  FileSystems.getDefault().getPath("D:/xsd/p_1361876010587.xsd");

	
	new Curator().curate(path1);
	

    }

}
