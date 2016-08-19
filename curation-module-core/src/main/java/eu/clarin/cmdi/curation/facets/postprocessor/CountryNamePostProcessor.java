package eu.clarin.cmdi.curation.facets.postprocessor;

import eu.clarin.cmdi.curation.facets.postprocessor.utils.CommonUtils;
import eu.clarin.cmdi.curation.facets.postprocessor.utils.VLOConfigFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountryNamePostProcessor implements PostProcessor {

    private final static Logger LOG = LoggerFactory.getLogger(CountryNamePostProcessor.class);

    private Map<String, String> countryCodeMap;

    /**
     * Returns the country name based on the mapping defined in the CMDI
     * component:
     * http://catalog.clarin.eu/ds/ComponentRegistry/?item=clarin.eu:cr1:c_1271859438104
     * If no mapping is found the original value is returned.
     *
     * @param value extracted "country" value from CMDI file
     * @return List of country names
     */
    @Override
    public List<String> process(String value) {
        String result = value;
        if (result != null) {
            String name = getCountryCodeMap().get(value.toUpperCase());
            if (name != null) {
                result = name;
            }
        }
        List<String> resultList = new ArrayList<String>();
        resultList.add(result);
        return resultList;
    }

    private Map<String, String> getCountryCodeMap() {
        if (countryCodeMap == null) {
            countryCodeMap = createCountryCodeMap();
        }
        return countryCodeMap;
    }

    private Map<String, String> createCountryCodeMap() {
    	
        final String countryComponentUrl = VLOConfigFactory.getVloConfig().getCountryComponentUrl();
        LOG.info("Creating country code map from {}", countryComponentUrl);
        try {
            Map<String, String> result = CommonUtils.createCMDIComponentItemMap(countryComponentUrl);
            return result;
        } catch (Exception e) {
            if (CommonUtils.shouldSwallowLookupErrors()) {
                return new HashMap<String, String>();
            } else {
                throw new RuntimeException("Cannot instantiate postProcessor:", e);
            }
        }
    }

}
