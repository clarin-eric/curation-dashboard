package eu.clarin.cmdi.rasa.helpers.impl;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import javax.sql.DataSource;

import eu.clarin.cmdi.rasa.DAO.CheckedLink;
import eu.clarin.cmdi.rasa.DAO.LinkToBeChecked;
import eu.clarin.cmdi.rasa.DAO.Statistics.CategoryStatistics;
import eu.clarin.cmdi.rasa.DAO.Statistics.Statistics;
import eu.clarin.cmdi.rasa.DAO.Statistics.StatusStatistics;
import eu.clarin.cmdi.rasa.filters.CheckedLinkFilter;
import eu.clarin.cmdi.rasa.filters.LinkToBeCheckedFilter;
import eu.clarin.cmdi.rasa.helpers.RasaFactory;
import eu.clarin.cmdi.rasa.helpers.RasaFactoryBuilder;
import eu.clarin.cmdi.rasa.helpers.statusCodeMapper.Category;
import eu.clarin.cmdi.rasa.linkResources.CheckedLinkResource;
import eu.clarin.cmdi.rasa.linkResources.LinkToBeCheckedResource;

public class RasaFactoryBuilderImpl implements RasaFactoryBuilder {

   @Override
   public RasaFactory getRasaFactory() {

      return new MockFactory();
   }
   
   private class MockFactory implements RasaFactory{

      @Override
      public CheckedLinkResource getCheckedLinkResource() {

         return new MockCheckedLinkResource();
      }

      @Override
      public LinkToBeCheckedResource getLinkToBeCheckedResource() {
         return new MockLinkToBeCheckedResource();
      }

      @Override
      public void tearDown() {
         // TODO Auto-generated method stub
         
      }

      @Override
      public RasaFactory init(Properties properties) {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public RasaFactory init(DataSource dataSource) {
         // TODO Auto-generated method stub
         return null;
      }
  
   }
   
   private class MockCheckedLinkResource implements CheckedLinkResource{

      @Override
      public Stream<CheckedLink> get(CheckedLinkFilter filter) throws SQLException {
         return Stream.empty();
      }

      @Override
      public Boolean save(CheckedLink checkedLink) throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public int getCount(CheckedLinkFilter filter) throws SQLException {
         // TODO Auto-generated method stub
         return 0;
      }

      @Override
      public Statistics getStatistics(CheckedLinkFilter filter) throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Stream<CategoryStatistics> getCategoryStatistics(CheckedLinkFilter filter) throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Stream<StatusStatistics> getStatusStatistics(CheckedLinkFilter filter) throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Map<String, CheckedLink> get(Collection<String> urls, Optional<CheckedLinkFilter> filter)
            throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Map<String, CheckedLink> getMap(CheckedLinkFilter filter) throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public CheckedLinkFilter getCheckedLinkFilter() {

         return new CheckedLinkFilter() {

            @Override
            public CheckedLinkFilter setUrlIs(String url) {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setUrlIn(String... urls) {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setSourceIs(String source) {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setProviderGroupIs(String providerGroup) {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setRecordIs(String record) {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setIngestionDateIs(Timestamp ingestionDate) {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setLimit(int offset, int limit) {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setIsActive(boolean active) {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setOrderByCheckingDate(boolean isAscending) {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setStatusIs(Integer status) {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setStatusBetween(Integer statusFrom, Integer statusTo) {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setCheckedBetween(Timestamp checkedAfter, Timestamp checkedBefore) {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setCategoryIs(Category category) {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setCategoryIn(Category... categories) {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setGroupByCategory() {
               // TODO Auto-generated method stub
               return null;
            }

            @Override
            public CheckedLinkFilter setOrderByCategory(boolean isAscending) {
               // TODO Auto-generated method stub
               return null;
            }
            
         };
      }
      
   }
   
   private class MockLinkToBeCheckedResource implements LinkToBeCheckedResource{

      @Override
      public Stream<LinkToBeChecked> get(LinkToBeCheckedFilter filter) throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public int getCount(LinkToBeCheckedFilter filter) throws SQLException {
         // TODO Auto-generated method stub
         return 0;
      }

      @Override
      public Boolean save(LinkToBeChecked linkToBeChecked) throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Boolean save(List<LinkToBeChecked> linksToBeChecked) throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }


      @Override
      public Boolean delete(String url) throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Boolean delete(List<String> urls) throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public List<String> getProviderGroupNames() throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public int deleteOldLinks(Long date) throws SQLException {
         // TODO Auto-generated method stub
         return 0;
      }

      @Override
      public int deleteOldLinks(Long date, String collection) throws SQLException {
         // TODO Auto-generated method stub
         return 0;
      }

      @Override
      public LinkToBeCheckedFilter getLinkToBeCheckedFilter() {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Stream<LinkToBeChecked> getNextLinksToCheck() throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Boolean updateURLs() throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Boolean deactivateLinksAfter(int periodInDays) throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Boolean deleteLinksAfter(int periodInDays) throws SQLException {
         // TODO Auto-generated method stub
         return null;
      }
      
   }

   @Override
   public RasaFactory getRasaFactory(Properties properties) {
      // TODO Auto-generated method stub
      return null;
   }
}
