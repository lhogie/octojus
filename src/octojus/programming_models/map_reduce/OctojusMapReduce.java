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

 
 package octojus.programming_models.map_reduce;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import octojus.ComputationRequest;
import octojus.OctojusNode;
import octojus.OneNodeOneRequest;

public abstract class OctojusMapReduce<NodeResult extends Serializable, GlobalResult>
{

	public GlobalResult execute(Collection<OctojusNode> nodes)
	{
		return reduce(new OneNodeOneRequest<NodeResult>()
		{

			@Override
			protected ComputationRequest<NodeResult> createComputationRequestForNode(OctojusNode n)
			{
				return map(n);
			}

		}.execute(nodes));
	}

	protected abstract ComputationRequest<NodeResult> map(OctojusNode node);

	protected abstract GlobalResult reduce(Map<OctojusNode, NodeResult> nodeResults);
}
