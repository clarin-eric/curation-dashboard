package eu.clarin.rest;

import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.CMDProfileReport;

@Path("/")
public class CurationRestService {
	
	

	@GET
	@Path("/instance/")
	@Produces(MediaType.APPLICATION_XML)
	public CMDInstanceReport assessInstance(@QueryParam("url") String url) throws Exception {
		System.out.println("curating " + url);
		return (CMDInstanceReport) new CurationModule().processCMDInstance(new URL(url));
	}

	@GET
	@Path("/profile/")
	@Produces(MediaType.APPLICATION_XML)
	public CMDProfileReport assesProfileByUrl(@QueryParam("url") String url) throws Exception {
		System.out.println("curating profile " + url);
		return (CMDProfileReport) new CurationModule().processCMDProfile(new URL(url));
	}
	
	@GET
	@Path("/profile/id/{id}")
	@Produces(MediaType.APPLICATION_XML)
	public CMDProfileReport assesProfileById(@PathParam("id") String id) throws Exception {
		System.out.println("curating profile " + id);
		return (CMDProfileReport) new CurationModule().processCMDProfile(id);
	}
}
