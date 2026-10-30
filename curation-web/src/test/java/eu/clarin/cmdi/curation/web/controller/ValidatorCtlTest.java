package eu.clarin.cmdi.curation.web.controller;

import eu.clarin.cmdi.curation.web.conf.WebConfig;
import eu.clarin.cmdi.curation.api.utils.FileNameEncoder;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ValidatorCtlTest extends BaseCtlTest{

    /**
     * linux and macOS differ here: linux has /tmp and macOS uses something like
     * /var/folders/../T
     */
    private static final String TMP_DIR = FileNameEncoder.encode(
            Paths.get(System.getProperty("java.io.tmpdir")).toString());

    @Autowired
    public ValidatorCtlTest(MockMvc mockMvc, WebConfig webConfig) {
        super(mockMvc, webConfig);
    }

    @Test
    void getInstance() {
    }

    @Test
    void postInstance() throws Exception {
        // creating a mock multipart from a cmdi file
        MockMultipartFile multiPartFile = new MockMultipartFile("file", "DE_2009_BergerEtAl_PolitikEntdecken_31_eng.xml", MediaType.MULTIPART_FORM_DATA_VALUE, this.getClass().getResourceAsStream("/testfiles/DE_2009_BergerEtAl_PolitikEntdecken_31_eng.xml"));
        // send it and expect status OK and a cmdi instance report as result
        this.mockMvc.perform(multipart("/validator").file(multiPartFile)).andDo(print()).andExpect(status().isOk()).andExpect(forwardedUrlPattern("/instance/" + TMP_DIR + "_*_curation_tmp"));
        // sending the same file with accept header xml should return status code OK and a xml file
        this.mockMvc.perform(multipart("/validator").file(multiPartFile).accept(MediaType.APPLICATION_XML)).andDo(print()).andExpect(status().isOk()).andExpect(forwardedUrlPattern("/download/instance/" + TMP_DIR + "_*_curation_tmp"));
        // sending the same file with accept header json should return status code OK and a json file
        this.mockMvc.perform(multipart("/validator").file(multiPartFile).accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(forwardedUrlPattern("/download/instance/" + TMP_DIR + "_*_curation_tmp"));

        // creating a mock multipart from a profile
        multiPartFile = new MockMultipartFile("file", "teiHeader.xsd", MediaType.MULTIPART_FORM_DATA_VALUE, this.getClass().getResourceAsStream("/testfiles/teiHeader.xsd"));
        // send it and expect status OK and a profile report as result
        this.mockMvc.perform(multipart("/validator").file(multiPartFile)).andDo(print()).andExpect(status().isOk()).andExpect(forwardedUrlPattern("/instance/file___" + TMP_DIR + "_*_curation_tmp"));
        // sending the same file with accept header xml should return status code OK and a xml file
        this.mockMvc.perform(multipart("/validator").file(multiPartFile).accept(MediaType.APPLICATION_XML)).andDo(print()).andExpect(status().isOk()).andExpect(forwardedUrlPattern("/download/instance/file___" + TMP_DIR + "_*_curation_tmp"));
        // sending the same file with accept header json should return status code OK and a json file
        this.mockMvc.perform(multipart("/validator").file(multiPartFile).accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(forwardedUrlPattern("/download/instance/file___" + TMP_DIR + "_*_curation_tmp"));

    }
}