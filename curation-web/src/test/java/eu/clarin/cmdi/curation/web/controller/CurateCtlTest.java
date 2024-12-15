package eu.clarin.cmdi.curation.web.controller;

import eu.clarin.cmdi.curation.web.conf.WebConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CurateCtlTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebConfig webConfig;

    @Test
    void getInstance() {
    }

    @Test
    void postInstance() throws Exception {
        // creating a mock multipart from a cmdi file
        MockMultipartFile multiPartFile = new MockMultipartFile("file", "DE_2009_BergerEtAl_PolitikEntdecken_31_eng.xml", MediaType.MULTIPART_FORM_DATA_VALUE, this.getClass().getResourceAsStream("/testfiles/DE_2009_BergerEtAl_PolitikEntdecken_31_eng.xml"));
        // send it and expect status OK and a cmdi instance report as result
        this.mockMvc.perform(multipart("/curate").file(multiPartFile)).andDo(print()).andExpect(status().isOk()).andExpect(content().string(containsString("CMD Record Report")));
        // sending the same file with accept header xml should return status code OK and a xml file
        this.mockMvc.perform(multipart("/curate").file(multiPartFile).accept(MediaType.APPLICATION_XML)).andDo(print()).andExpect(status().isOk()).andExpect(forwardedUrlPattern("/download/instance/_tmp_*_curation_tmp"));
        // sending the same file with accept header json should return status code OK and a json file
        this.mockMvc.perform(multipart("/curate").file(multiPartFile).accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(forwardedUrlPattern("/download/instance/_tmp_*_curation_tmp?format=json"));

        // creating a mock multipart from a profile
        multiPartFile = new MockMultipartFile("file", "media-corpus-profile.xsd", MediaType.MULTIPART_FORM_DATA_VALUE, this.getClass().getResourceAsStream("/testfiles/media-corpus-profile.xsd"));
        // send it and expect status OK and a profile report as result
        this.mockMvc.perform(multipart("/curate").file(multiPartFile)).andDo(print()).andExpect(status().isOk()).andExpect(content().string(containsString("CMD Profile Report")));
        // sending the same file with accept header xml should return status code OK and a xml file
        this.mockMvc.perform(multipart("/curate").file(multiPartFile).accept(MediaType.APPLICATION_XML)).andDo(print()).andExpect(status().isOk()).andExpect(forwardedUrlPattern("/download/instance/file____tmp_*_curation_tmp"));
        // sending the same file with accept header json should return status code OK and a json file
        this.mockMvc.perform(multipart("/curate").file(multiPartFile).accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(forwardedUrlPattern("/download/instance/file____tmp_*_curation_tmp?format=json"));

    }

    @Test
    void getRobotsTxt() throws Exception {

        this.mockMvc.perform(get("/robots.txt")).andDo(print()).andExpect(status().isOk());
    }
}