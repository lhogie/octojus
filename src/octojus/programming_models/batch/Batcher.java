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

 
 package octojus.programming_models.batch;

import java.io.Serializable;
import java.util.Collection;

import octojus.ComputationRequest;
import octojus.OctojusNode;
import octojus.OneNodeOneThread;

public abstract class Batcher<E extends Serializable> extends OneNodeOneThread
{
	public Batcher(Collection<OctojusNode> nodes)
	{
		super(nodes);
	}

	@Override
	protected void process(OctojusNode node) throws Throwable
	{
		while (true)
		{
			ComputationRequest<E> job = getNextJobForNode(node);

			if (job == null)
			{
				break;
			}

			E jobResult = job.runOn(node);
			newResult(node, job, jobResult);
		}
	}

	protected abstract ComputationRequest<E> getNextJobForNode(OctojusNode n);

	protected abstract void newResult(OctojusNode node, ComputationRequest<E> job, E result);

}
