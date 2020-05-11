/////////////////////////////////////////////////////////////////////////////////////////
// 
//                 Université de Nice Sophia-Antipolis  (UNS) - 
//                 Centre National de la Recherche Scientifique (CNRS)
//                 Copyright © 2015 UNS, CNRS All Rights Reserved.
// 
//     These computer program listings and specifications, herein, are
//     the property of Université de Nice Sophia-Antipolis and CNRS
//     shall not be reproduced or copied or used in whole or in part as
//     the basis for manufacture or sale of items without written permission.
//     For a license agreement, please contact:
//     <mailto: licensing@sattse.com> 
//
//
//
//     Author: Luc Hogie – Laboratoire I3S - luc.hogie@unice.fr
//
//////////////////////////////////////////////////////////////////////////////////////////

 
 package octojus;

import java.util.Collection;
import java.util.Map;

public abstract class SystolicAlgorithm<NodeResult extends NoReturn, GlobalResult>
{
	public GlobalResult execute(Collection<OctojusNode> nodes)
	{
		Map<OctojusNode, NodeResult> localResults;
		GlobalResult globalResult;

		for (long step = 0;; ++step)
		{
			final long s = step;
			localResults = new OneNodeOneRequest<NodeResult>() {

				@Override
				protected ComputationRequest<NodeResult> createComputationRequestForNode(OctojusNode n)
				{
					ComputationRequest<NodeResult> j = createIJobForNode(s, n);
					// j.globalResultAtPreviousStep = globalResult;
					// j.localResultsAtPreviousStep = localResults;
					return j;
				}

			}.execute(nodes);

			globalResult = reduce(localResults);

			if (resultsAreSatisfying(step, localResults, globalResult))
			{
				return globalResult;
			}
		}
	}

	protected abstract boolean resultsAreSatisfying(long step, Map<OctojusNode, NodeResult> localResults, GlobalResult globalResult);

	protected abstract ComputationRequest<NodeResult> createIJobForNode(long step, OctojusNode n);

	protected abstract GlobalResult reduce(Map<OctojusNode, NodeResult> execute);

}
