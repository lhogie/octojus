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

package octojus.demo;

import java.util.Map;

import jacaboo.NodeNameSet;
import octojus.ComputationRequest;
import octojus.OctojusCluster;
import octojus.OctojusNode;
import octojus.OneNodeOneRequest;

public class ParallelDistributedComputing
{
	public static void main(String[] args) throws Throwable
	{
		NodeNameSet set = new NodeNameSet();
		set.add("srv-coati");
		set.add("musclotte");
		OctojusCluster o = new OctojusCluster(System.getProperty("user.name"), null, set);
		o.start();

		final String s = "Salut toi, alors dis moi un peu ce que tu pense de cette API ?";

		Map<OctojusNode, Integer> r = new OneNodeOneRequest<Integer>()
		{
			@Override
			protected ComputationRequest<Integer> createComputationRequestForNode(
					OctojusNode n)
			{
				SizeComputer c = new SizeComputer();
				c.s = s;
				return c;
			}
		}.execute(o.getNodes());

		System.out.println(r);
	}

	@SuppressWarnings("serial")
	public static class SizeComputer extends ComputationRequest<Integer>
	{
		public String s;

		@Override
		protected Integer compute() throws Throwable
		{
			return s.length();
		}
	}
}
