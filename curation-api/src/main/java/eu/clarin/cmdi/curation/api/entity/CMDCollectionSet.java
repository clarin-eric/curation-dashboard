package eu.clarin.cmdi.curation.api.entity;

import eu.clarin.cmdi.curation.api.processor.CMDCollectionSetProcessor;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Collection;

@Scope("prototype")
@Component
@RequiredArgsConstructor
@Data
public class CMDCollectionSet {


    private final CMDCollectionSetProcessor processor;

    private Collection<Path> paths;

    public Collection<CollectionReport> generateReport() {
        return processor.process(this);
    }
}
