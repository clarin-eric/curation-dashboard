package eu.clarin.web.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Panel;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;
import eu.clarin.cmdi.curation.main.Configuration;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Statistics extends Panel implements View {


    Label label;

    public Statistics() {
        setSizeFull();
        label = new Label();
        label.setContentMode(ContentMode.HTML);


        setContent(label);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        try {
            byte[] out = Files.readAllBytes(Paths.get(Configuration.OUTPUT_DIRECTORY.toString() + "/statistics/linkCheckerStatistics.html"));

            label.setValue(new String(out));
        } catch (IOException e) {
            e.printStackTrace();

            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String msg = "Error while reading the statistics file:" + errors.toString();
            label.setValue("<pre>" + msg + "</pre>");
        }
    }
}
