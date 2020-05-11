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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class OneNodeOneRequest<JobResult>
{
	public static boolean SHOW_LOG = false;

	public Map<OctojusNode, JobResult> execute(Collection<OctojusNode> nodes)
	{
		final Map<OctojusNode, JobResult> results = Collections
				.synchronizedMap(new HashMap<OctojusNode, JobResult>());

		new OneNodeOneThread(nodes)
		{

			@Override
			protected final void process(OctojusNode node) throws Throwable
			{
				ComputationRequest<JobResult> job = createComputationRequestForNode(node);

				if (job != null)
				{
					JobResult result = job.runOn(node);

					if (SHOW_LOG)
					{
						System.out.println("Got result from node " + node);
					}

					results.put(node, result);
				}
			}
		};

		return results;
	}

	protected abstract ComputationRequest<JobResult> createComputationRequestForNode(
			OctojusNode n);
}
