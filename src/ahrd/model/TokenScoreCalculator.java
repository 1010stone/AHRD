package ahrd.model;

import static ahrd.controller.Settings.getSettings;
import static ahrd.controller.Utils.roundToNDecimalPlaces;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.applet.GetMemberPluginCallRequest;

/**
 * Functions calculating Token-Scores.
 */
public class TokenScoreCalculator {

	private Map<String, Double> cumulativeTokenBitScores = new HashMap<String, Double>();
	private Map<String, Double> cumulativeTokenBlastDatabaseScores = new HashMap<String, Double>();
	private Map<String, Double> cumulativeTokenOverlapScores = new HashMap<String, Double>();
	private Map<String, Double> cumulativeTokenDomainSimilarityScores = new HashMap<String, Double>();
	private double totalTokenBitScore = 0;
	private double totalTokenBlastDatabaseScore = 0;
	private double totalTokenOverlapScore = 0;
	private double totalTokenDomainSimilarityScore = 0;
	private Map<String, Double> tokenScores = new HashMap<String, Double>();
	private Protein protein;
	// Please enter your initials ___
	private double tokenHighScore = 0.0;

	public static boolean passesBlacklist(String token, List<String> blacklist) {
		// No Token passes being NULL or empty String
		boolean passed = (token != null && !token.equals(""));
		Iterator<String> i = blacklist.iterator();
		while (i.hasNext() && passed) {
			Pattern p = Pattern.compile(i.next());
			Matcher m = p.matcher(token);
			// A Match against a blacklisted RegExp lets the token fail:
			passed = !m.find();
		}
		return passed;
	}

	/**
	 * @param queryStart
	 * @param queryEnd
	 * @param queryLength
	 * @return The proportion of the query-sequence, that is covered by the
	 *         significant local alignment with the Blast-Hit's subject.
	 */
	public static double overlapScore(double queryStart, double queryEnd,
			double queryLength) {
		return (queryEnd - queryStart + 1.0) / queryLength;
	}

	public TokenScoreCalculator(Protein protein) {
		super();
		setProtein(protein);
	}

	/**
	 * Returns the sum of BlastResult's Tokens' Scores.
	 */
	public double descriptionLineSummedTokenScore(BlastResult br) {
		double sum = 0.0;
		for (String token : br.getTokens()) {
			sum += getTokenScores().get(token);
		}
		return sum;
	}

	/**
	 * Assigns each Token in each BlastResult it's TokenScore and stores it in
	 * the Map 'tokenScores' key is Token and value is its TokenScore.
	 */
	public void assignTokenScores() {
		// iterate through blast databases having BlastResults
		for (String iterBlastDb : getProtein().getBlastResults().keySet()) {
			// iterate through blast results in coming from the different blast
			// databases
			for (BlastResult iterResult : getProtein().getBlastResults().get(
					iterBlastDb)) {
				// iterate through tokens in different blast result desc-lines
				for (String token : iterResult.getTokens()) {
					if (!(getTokenScores().containsKey(token))) {
						double tokenscore = tokenScore(token);
						getTokenScores().put(token, new Double(tokenscore));
						// remember highest token score
						if (tokenscore > getTokenHighScore()) {
							setTokenHighScore(tokenscore);
						}
					}
				}
			}
		}
	}

	/**
	 * Iterates over all Tokens in the Map 'tokenScores' and re-assigns them new
	 * TokenScores. Each non-informative Token is assigned the new TokenScore :=
	 * (old TokenScore) - (tokenHighScore / 2).
	 */
	public void filterTokenScores() {
		for (String token : getTokenScores().keySet()) {
			if (!isInformativeToken(token)) {
				getTokenScores().put(
						token,
						new Double(getTokenScores().get(token)
								- getTokenHighScore() / 2));
			}
		}
	}

	/**
	 * Informative tokens have a token-score greater than half of the
	 * tokenHighScore.
	 * 
	 * @Note: This method cannot be invoked before having the tokenScores-Set
	 *        initialized!
	 */
	public boolean isInformativeToken(String token) {
		return getTokenScores().get(token) > getTokenHighScore() / 2;
	}

	/**
	 * Once per BlastResult's unique token the following <em>cumulative</em>
	 * scores are measured: 1. BitScore 2. DatabaseScore 3. OverlapScore 4.
	 * DomainSimilarityScore
	 * 
	 * @param BlastResult
	 *            br
	 */
	public void measureCumulativeScores(BlastResult br) {
		for (String token : br.getTokens()) {
			Double overlapScore = TokenScoreCalculator.overlapScore(br
					.getStart(), br.getEnd(), getProtein().getSequenceLength());
			addCumulativeTokenBitScore(token, br.getBitScore());
			addCumulativeTokenBlastDatabaseScore(token, br
					.getBlastDatabaseName());
			addCumulativeTokenOverlapScore(token, overlapScore);
		}
	}

	/**
	 * 
	 * Iteratively measures the cumulative domain similarity score for each
	 * token in the argument set.
	 * 
	 * @Note: Only after the Interpro annotations for the BlastResults are
	 *        initialized and the <em>complete</em> set of description
	 *        candidates (BlastResults) has been constructed, the domain
	 *        similarity scores can be calculated. Hence this method is invoked
	 *        <em>after</em> all BlastResults have been parsed.
	 * 
	 * @param BlastResult
	 *            br
	 */
	public void measureCumulativeDomainSimilarityScores(BlastResult br) {
		Double dss = br.getDomainSimilarityScore();
		if (dss != null && dss > 0.0) {
			for (String token : br.getTokens()) {
				if (!getCumulativeTokenDomainSimilarityScores().containsKey(
						token)) {
					getCumulativeTokenDomainSimilarityScores().put(token, dss);
				} else {
					getCumulativeTokenDomainSimilarityScores()
							.put(
									token,
									dss
											+ getCumulativeTokenDomainSimilarityScores()
													.get(token));
				}
			}
		}
	}

	/**
	 * Iteratively computes the total domain similarity score.
	 * 
	 * @Note: Only after the Interpro annotations for the BlastResults are
	 *        initialized and the <em>complete</em> set of description
	 *        candidates (BlastResults) has been constructed, the domain
	 *        similarity scores can be calculated. Hence this method is invoked
	 *        <em>after</em> all BlastResults have been parsed.
	 * 
	 * @param BlastResult
	 *            br
	 */
	public void measureTotalDomainSimilarityScore(BlastResult br) {
		if (br.getDomainSimilarityScore() != null)
			setTotalTokenDomainSimilarityScore(getTotalTokenDomainSimilarityScore()
					+ br.getDomainSimilarityScore());
	}

	/**
	 * Once per BlastResult the following <em>total</em> scores are measured: 1.
	 * BitScore 2. DatabaseScore 3. OverlapScore
	 * 
	 * @param BlastResult
	 *            br
	 */
	public void measureTotalScores(BlastResult br) {
		Double overlapScore = TokenScoreCalculator.overlapScore(br.getStart(),
				br.getEnd(), getProtein().getSequenceLength());
		setTotalTokenBlastDatabaseScore(getTotalTokenBlastDatabaseScore()
				+ getSettings().getBlastDbWeight(br.getBlastDatabaseName()));
		setTotalTokenOverlapScore(getTotalTokenOverlapScore() + overlapScore);
		setTotalTokenBitScore(getTotalTokenBitScore() + br.getBitScore());
	}

	/**
	 * @param token
	 * @return token-score
	 */
	public double tokenScore(String token) {
		// Validate:
		Double bitScoreWeight = getSettings().getTokenScoreBitScoreWeight();
		Double databaseScoreWeight = getSettings()
				.getTokenScoreDatabaseScoreWeight();
		Double overlapScoreWeight = getSettings()
				.getTokenScoreOverlapScoreWeight();
		Double domainSimilarityWeight = getSettings()
				.getTokenScoreDomainSimilarityWeight();
		Double validateSumToOne = roundToNDecimalPlaces(bitScoreWeight
				+ databaseScoreWeight + overlapScoreWeight
				+ domainSimilarityWeight, 9);
		// Tolerate rounding error <= 10^-3
		if (!(validateSumToOne >= 0.999 && validateSumToOne <= 1.001))
			throw new IllegalArgumentException(
					"The four weights 'bitScoreWeight', 'databaseScoreWeight', 'overlapScoreWeight' and 'domainSimilarityScoreWeight' should sum up to 1.0, but actually sum up to: "
							+ (bitScoreWeight + databaseScoreWeight
									+ overlapScoreWeight + domainSimilarityWeight));
		// Calculate Token-Score:
		return (bitScoreWeight * getCumulativeTokenBitScores().get(token)
				/ getTotalTokenBitScore() + databaseScoreWeight
				* getCumulativeTokenBlastDatabaseScores().get(token)
				/ getTotalTokenBlastDatabaseScore() + overlapScoreWeight
				* getCumulativeTokenOverlapScores().get(token)
				/ getTotalTokenOverlapScore() + domainSimilarityFraction(token));
	}

	public double domainSimilarityFraction(String token) {
		double dsf = 0.0;
		if (getTotalTokenDomainSimilarityScore() > 0
				&& getCumulativeTokenDomainSimilarityScores().get(token) != null)
			dsf = getSettings().getTokenScoreDomainSimilarityWeight()
					* getCumulativeTokenDomainSimilarityScores().get(token)
					/ getTotalTokenDomainSimilarityScore();
		return dsf;
	}

	public void addCumulativeTokenBitScore(String token, double bitScore) {
		if (!getCumulativeTokenBitScores().containsKey(token))
			getCumulativeTokenBitScores().put(token, new Double(bitScore));
		else
			getCumulativeTokenBitScores().put(
					token,
					new Double(bitScore
							+ getCumulativeTokenBitScores().get(token)));
	}

	public void addCumulativeTokenOverlapScore(String token, double overlapScore) {
		if (!getCumulativeTokenOverlapScores().containsKey(token))
			getCumulativeTokenOverlapScores().put(token,
					new Double(overlapScore));
		else
			getCumulativeTokenOverlapScores().put(
					token,
					new Double(overlapScore
							+ getCumulativeTokenOverlapScores().get(token)));
	}

	public void addCumulativeTokenBlastDatabaseScore(String token,
			String blastDatabaseName) {
		Integer blastDatabaseWeight = getSettings().getBlastDbWeight(
				blastDatabaseName);
		if (!getCumulativeTokenBlastDatabaseScores().containsKey(token))
			getCumulativeTokenBlastDatabaseScores().put(token,
					new Double(blastDatabaseWeight));
		else
			getCumulativeTokenBlastDatabaseScores().put(
					token,
					new Double(blastDatabaseWeight
							+ getCumulativeTokenBlastDatabaseScores()
									.get(token)));
	}

	public double sumOfAllTokenScores(BlastResult blastResult) {
		double sum = 0.0;
		for (String token : blastResult.getTokens()) {
			sum += getTokenScores().get(token);
		}
		return sum;
	}

	public Protein getProtein() {
		return protein;
	}

	public void setProtein(Protein protein) {
		this.protein = protein;
	}

	public Map<String, Double> getCumulativeTokenBitScores() {
		return cumulativeTokenBitScores;
	}

	public void setCumulativeTokenBitScores(
			Map<String, Double> cumulativeTokenBitScores) {
		this.cumulativeTokenBitScores = cumulativeTokenBitScores;
	}

	public Map<String, Double> getCumulativeTokenBlastDatabaseScores() {
		return cumulativeTokenBlastDatabaseScores;
	}

	public void setCumulativeTokenBlastDatabaseScores(
			Map<String, Double> cumulativeTokenBlastDatabaseScores) {
		this.cumulativeTokenBlastDatabaseScores = cumulativeTokenBlastDatabaseScores;
	}

	public Map<String, Double> getCumulativeTokenOverlapScores() {
		return cumulativeTokenOverlapScores;
	}

	public void setCumulativeTokenOverlapScores(
			Map<String, Double> cumulativeTokenOverlapScores) {
		this.cumulativeTokenOverlapScores = cumulativeTokenOverlapScores;
	}

	public double getTotalTokenBitScore() {
		return totalTokenBitScore;
	}

	public void setTotalTokenBitScore(double totalTokenBitScore) {
		this.totalTokenBitScore = totalTokenBitScore;
	}

	public double getTotalTokenBlastDatabaseScore() {
		return totalTokenBlastDatabaseScore;
	}

	public void setTotalTokenBlastDatabaseScore(
			double totalTokenBlastDatabaseScore) {
		this.totalTokenBlastDatabaseScore = totalTokenBlastDatabaseScore;
	}

	public double getTotalTokenOverlapScore() {
		return totalTokenOverlapScore;
	}

	public void setTotalTokenOverlapScore(double totalTokenOverlapScore) {
		this.totalTokenOverlapScore = totalTokenOverlapScore;
	}

	/**
	 * Get tokenHighScore.
	 * 
	 * @return tokenHighScore as double.
	 */
	public double getTokenHighScore() {
		return tokenHighScore;
	}

	/**
	 * Set tokenHighScore.
	 * 
	 * @param tokenHighScore
	 *            the value to set.
	 */
	public void setTokenHighScore(double tokenHighScore) {
		this.tokenHighScore = tokenHighScore;
	}

	/**
	 * Get tokenScores.
	 * 
	 * @return tokenScores as Map<String, Double>.
	 */
	public Map<String, Double> getTokenScores() {
		return tokenScores;
	}

	/**
	 * Set tokenScores.
	 * 
	 * @param tokenScores
	 *            the value to set.
	 */
	public void setTokenScores(Map<String, Double> tokenScores) {
		this.tokenScores = tokenScores;
	}

	public Map<String, Double> getCumulativeTokenDomainSimilarityScores() {
		return cumulativeTokenDomainSimilarityScores;
	}

	public void setCumulativeTokenDomainSimilarityScores(
			Map<String, Double> cumulativeTokenDomainSimilarityScores) {
		this.cumulativeTokenDomainSimilarityScores = cumulativeTokenDomainSimilarityScores;
	}

	public double getTotalTokenDomainSimilarityScore() {
		return totalTokenDomainSimilarityScore;
	}

	public void setTotalTokenDomainSimilarityScore(
			double totalTokenDomainSimilarityScore) {
		this.totalTokenDomainSimilarityScore = totalTokenDomainSimilarityScore;
	}

}
