package eu.clarin.cmdi.curation.cr;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.ErrorReport;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.report.Score;

class ProfileScoreCacheFactory{
	
	static final long HOUR_IN_NS = 3600000000000L;
	
	private static final Logger _logger = LoggerFactory.getLogger(ProfileScoreCacheFactory.class);
	
	public static LoadingCache<ProfileHeader, Double> createScoreCache(boolean isPublicProfilesCache){
		return isPublicProfilesCache?
		
		CacheBuilder.newBuilder()
				.concurrencyLevel(4)
				.build(new ScoreCacheLoader())
		:
		CacheBuilder.newBuilder()
			.concurrencyLevel(4)
			.expireAfterWrite(8, TimeUnit.HOURS)//keep non public profiles 8 hours in cache
			.ticker(new Ticker() { @Override public long read() { return 9 * HOUR_IN_NS; } }) //cache tick 9 hours
			.build(new ScoreCacheLoader());		
	}
	
	
	private static class ScoreCacheLoader extends CacheLoader<ProfileHeader, Double>{		
		
		/*
		 * process profile and create new Score object by aggregating individual scores
		 */
		
		@Override
		public Double load(ProfileHeader header) throws Exception{
			CMDProfile profile = new CMDProfile(header.schemaLocation, header.cmdiVersion);			
			_logger.info("Calculating and caching score for {}", profile);
			
			Report<?> report = profile.generateReport();
			
			if(report instanceof ErrorReport)
				throw new Exception(((ErrorReport)report).error);
			
			if(CRService.PROFILE_MAX_SCORE.equals(Double.NaN)){
				CRService.PROFILE_MAX_SCORE = ((CMDProfileReport)report).segmentScores.stream().mapToDouble(Score::getMaxScore).sum();
			}
			
			return ((CMDProfileReport)report).segmentScores.stream().mapToDouble(Score::getScore).sum();
			
		}
		
	}

}
