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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import octojus.ComputationRequest;
import octojus.OctojusNode;

public class ListBatcher<E extends Serializable> extends Batcher<E>
{
	private final List<ComputationRequest<E>> jobList;
	private final List<E> resultList = Collections.synchronizedList(new ArrayList<E>());

	public List<E> getResultList()
	{
		return resultList;
	}

	public ListBatcher(Collection<OctojusNode> nodes, List<ComputationRequest<E>> jobs)
	{
		super(nodes);
		this.jobList = Collections.synchronizedList(jobs);
	}

	@Override
	protected synchronized ComputationRequest<E> getNextJobForNode(OctojusNode n)
	{
		return jobList.remove(0);
	}

	@Override
	protected void newResult(OctojusNode n, ComputationRequest<E> job, E r)
	{
		resultList.add(r);
	}
}
