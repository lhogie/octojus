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

 
 package octojus.allocation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import octojus.ComputationRequest;
import octojus.OctojusNode;
import toools.math.MathsUtilities;

public class IdlestLoadBalancingStrategy<E extends Serializable> implements Allocation<E>
{
	@Override
	public OctojusNode allocate(ComputationRequest<E> j, Collection<OctojusNode> f) throws Throwable
	{
		List<OctojusNode> l = new ArrayList<OctojusNode>(f);
		sortByLoad(l);
		return l.get(0);
	}

	public static void sortByLoad(List<OctojusNode> f)
	{
		Collections.sort(f, new Comparator<OctojusNode>() {

			@Override
			public int compare(OctojusNode a, OctojusNode b)
			{
				return MathsUtilities.compare(a.getDiscoveredInfo().getLoadAverage(), b.getDiscoveredInfo().getLoadAverage());
			}
		});
	}

}
