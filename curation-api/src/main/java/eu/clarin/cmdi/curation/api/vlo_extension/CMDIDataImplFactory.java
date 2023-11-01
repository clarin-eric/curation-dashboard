package eu.clarin.cmdi.curation.api.vlo_extension;

import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.CMDIDataFactory;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

import java.util.List;
import java.util.Map;

/**
 * The type Cmdi data impl factory.
 */
public class CMDIDataImplFactory implements CMDIDataFactory<Map<String, List<ValueSet>>> {
    private final FieldNameService fieldNameService;

    /**
     * Instantiates a new Cmdi data impl factory.
     *
     * @param fieldNameService the field name service
     */
    public CMDIDataImplFactory(FieldNameService fieldNameService) {
        this.fieldNameService = fieldNameService;
    }

    /**
     * New cmdi data instance cmdi data.
     *
     * @return the cmdi data
     */
    @Override
    public CMDIData<Map<String, List<ValueSet>>> newCMDIDataInstance() {

        return new CMDIDataImpl(this.fieldNameService);
    }

}
