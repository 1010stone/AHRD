package ahrd.view;

import java.io.IOException;

import ahrd.controller.Parameters;
import ahrd.controller.Settings;

public class GeneticTrainerOutputWriter extends TrainerOutputWriter {

	public GeneticTrainerOutputWriter() throws IOException {
		super();
	}

	@Override
	public String generateHeader(boolean isFinalOutput) {
		String hdr = "Generation\t";
		if (isFinalOutput)
			hdr += "Average Maximum-Evaluation-Score\t";
		hdr += "Average Evaluation-Score(F-Score)";
		if (!isFinalOutput)
			hdr += "\tDiff-to-last-Generation\tOrigin";
		hdr += "\tToken-Score-Bit-Score-Weight\tToken-Score-Database-Score-Weight\tToken-Score-Overlap-Score-Weight";
		for (String blastDb : this.sortedBlastDatabases) {
			hdr += "\t" + blastDb + "-Weight";
			hdr += "\t" + blastDb + "-Description-Score-Bit-Score-Weight";
		}
		hdr += "\n";
		return hdr;
	}

	public void writeGeneticIterationOutput(int generation, Parameters currentGenerationsBestParameters,
			double diffAvgEvalScoreToLastGeneration, String Origin)
			throws IOException {
		this.pathBufWrtr.write(geneticSettingsRow(generation, currentGenerationsBestParameters,
				diffAvgEvalScoreToLastGeneration, Origin));
	}

	public String geneticSettingsRow(int generation, Parameters p,
			double diffAvgEvalScoreToLastGeneration, String Origin) {
		String col = generation + "\t"
				+ p.getAvgEvaluationScore() + "\t"
				+ diffAvgEvalScoreToLastGeneration + "\t" + Origin + "\t"
				+ FRMT.format(p.getTokenScoreBitScoreWeight()) + "\t"
				+ FRMT.format(p.getTokenScoreDatabaseScoreWeight()) + "\t"
				+ FRMT.format(p.getTokenScoreOverlapScoreWeight());
		for (String blastDb : this.sortedBlastDatabases) {
			col += "\t" + FRMT.format(p.getBlastDbWeight(blastDb));
			col += "\t"
					+ FRMT.format(p.getDescriptionScoreBitScoreWeight(blastDb));
		}
		col += "\n";
		return col;
	}
	
	@Override
	public String finalSettingsRow(Settings s, Integer sFoundInGeneration,
			Double avgMaxEvalScore) {
		String col = sFoundInGeneration + "\t" + avgMaxEvalScore + "\t"
				+ s.getAvgEvaluationScore() + "\t"
				+ FRMT.format(s.getTokenScoreBitScoreWeight()) + "\t"
				+ FRMT.format(s.getTokenScoreDatabaseScoreWeight()) + "\t"
				+ FRMT.format(s.getTokenScoreOverlapScoreWeight());
		for (String blastDb : this.sortedBlastDatabases) {
			col += "\t" + FRMT.format(s.getBlastDbWeight(blastDb));
			col += "\t"
					+ FRMT.format(s.getDescriptionScoreBitScoreWeight(blastDb));
		}
		col += "\n";
		return col;
	}

}