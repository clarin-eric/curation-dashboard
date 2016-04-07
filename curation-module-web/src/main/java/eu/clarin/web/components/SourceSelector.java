package eu.clarin.web.components;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileDescriptions.ProfileHeader;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.utils.FSVisitor;

public class SourceSelector extends TabSheet {
	
	private final ObjectProperty reportOutput;

	public SourceSelector(boolean localFiles, ObjectProperty reportOutput){
		this.reportOutput = reportOutput;
		addTab(createPublicProfilesTab(), "Public Profiles");	
		//addTab(createFSBrowserTab(), "Collections");
		setSizeFull();
	}

	public Layout createPublicProfilesTab() {
		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		layout.setHeight(null);
		try {
			List<ProfileHeader> profiles = CRService.getInstance().getPublicProfiles();
			for (ProfileHeader profile : profiles) {
				layout.addComponent(new ProfilesButton(profile.getId(), profile.getName()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			layout.setComponentError(new UserError(e.getMessage()));
		}
		return layout;
	}
	
	
	public Layout createFSBrowserTab(){
		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		layout.setHeight(null);
		
		FSVisitor fsBuilder = new FSVisitor();
		Path cmdi = Paths.get("D:/data/cmdi/");
		try {
			Files.walkFileTree(cmdi, fsBuilder);
			Map<String, ArrayList<String>> fs = fsBuilder.getFSTree();
			
			Tree tree = new Tree();
			for(String collection: fs.keySet()){
				tree.addItem(collection);
				
				//add cmd files
				for(String cmdFile: fs.get(collection)){
					tree.addItem(cmdFile);
					tree.setParent(cmdFile, collection);
					tree.setChildrenAllowed(cmdFile, false);					
				}
			}
			tree.setImmediate(true);
			
			tree.addValueChangeListener(event ->{
				Report r = null;
								
				Object selectedVal = tree.getValue();				
				boolean folder = tree.hasChildren(selectedVal);
				Object folderName = folder? selectedVal : tree.getParent(selectedVal);
				
				try {
					CurationModule curator = new CurationModule();
					if(folder){
						Path collection = Paths.get("D:/data/cmdi/", (String)selectedVal);	
						r = curator.processCollection(collection);						
					}else{
						Path file = Paths.get("D:/data/cmdi/", (String)folderName, (String)selectedVal);					
						r = curator.processCMDInstance(file);						
					}
					
					
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					r.toXML(out);
					reportOutput.setValue(out.toString());
				} catch (Exception e) {
					e.printStackTrace();
					reportOutput.setValue("Error while curating " + (String)selectedVal + "\n" + e.getMessage());
				}
			});
			
			layout.addComponent(tree);
			
		} catch (IOException e) {
			layout.setComponentError(new UserError(e.getMessage()));
		}
		
		return layout;
		
	}
	
	class ProfilesButton extends Button{
		
		String id;
		
		public ProfilesButton(String id, String name){
			super(name);
			this.id = id;
			setDescription(id);
			
			setSizeFull();
			
			addClickListener(event -> {
				Report r = null;
				try {
					CurationModule curator = new CurationModule();						
					r = curator.processCMDProfile(id);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					r.toXML(out);
					reportOutput.setValue(out.toString());
				} catch (Exception e) {
					e.printStackTrace();
					reportOutput.setValue("Error while curating " + name + "\n" + e.getMessage());
				}
			});
			
		}
		
		
	}

}
