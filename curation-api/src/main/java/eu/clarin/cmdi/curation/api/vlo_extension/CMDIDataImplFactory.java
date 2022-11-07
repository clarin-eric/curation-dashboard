package eu.clarin.cmdi.curation.api.vlo_extension;

import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.CMDIDataFactory;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

import java.util.List;
import java.util.Map;

public class CMDIDataImplFactory implements CMDIDataFactory<Map<String, List<ValueSet>>> {
    private final FieldNameService fieldNameService;

    public CMDIDataImplFactory(FieldNameService fieldNameService) {
        this.fieldNameService = fieldNameService;
    }

    @Override
    public CMDIData<Map<String, List<ValueSet>>> newCMDIDataInstance() {

        return new CMDIDataImpl(this.fieldNameService);
    }

}
