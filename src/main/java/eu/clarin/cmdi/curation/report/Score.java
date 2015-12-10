package eu.clarin.cmdi.curation.report;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import eu.clarin.cmdi.curation.main.Config;

public class Score {
	
	private static final NumberFormat formatter = new DecimalFormat(Config.SCORE_NUMERIC_DISPLAY_FORMAT);

	private double score = 0;
	//private String score
	
	public Score(double score){
		this.score = score;
	}

	
	public double getScoreAsDouble(){
		return score;
	}
	
	@Override
	public String toString() {		
		return formatter.format(score);
	}
	
 
}
