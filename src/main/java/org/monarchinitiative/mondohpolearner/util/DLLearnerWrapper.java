package org.monarchinitiative.mondohpolearner.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.Score;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.owlapi.model.OWLIndividual;

public class DLLearnerWrapper{
	private static final Logger logger = Logger.getLogger(DLLearnerWrapper.class.getName());
	private OWLFile ks;
	private OWLAPIReasoner reasoner;
	private ClosedWorldReasoner closedWorldReasoner; 

	public DLLearnerWrapper(String inputFilename) {
		try {
			ks = new OWLFile();
			ks.setFileName(inputFilename);
			ks.init();

			/*
			closedWorldReasoner = new ClosedWorldReasoner();
			Set<KnowledgeSource> sources = new HashSet<>();
			sources.add(ks);
			closedWorldReasoner.setSources(sources);
			closedWorldReasoner.init();
			*/
			reasoner = new OWLAPIReasoner(ks);
			reasoner.setReasonerImplementation(ReasonerImplementation.ELK);
			reasoner.setUseFallbackReasoner(true);
			reasoner.init();
			Logger.getLogger(ElkReasoner.class).setLevel(Level.OFF);
			
			closedWorldReasoner = new ClosedWorldReasoner(ks);
			closedWorldReasoner.setReasonerComponent(reasoner);
			closedWorldReasoner.init();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public Set<? extends EvaluatedDescription<? extends Score>> run(Set<OWLIndividual> posExamples) {
		try {
			PosOnlyLP lp = new PosOnlyLP(closedWorldReasoner);
			lp.setPositiveExamples(posExamples);
			lp.init();

			CELOE alg = new CELOE();
			alg.setLearningProblem(lp);
			alg.setReasoner(closedWorldReasoner);
			alg.setWriteSearchTree(true);
			alg.setSearchTreeFile("log/search-tree.log");
			alg.setReplaceSearchTree(true);
			alg.setNoisePercentage(75);
			alg.setMaxNrOfResults(15);
			alg.init();
			alg.start();

			return alg.getCurrentlyBestEvaluatedDescriptions().descendingSet();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return null;
	}
}