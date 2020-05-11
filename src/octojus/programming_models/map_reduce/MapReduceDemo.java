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

import jacaboo.clusters.CoatiComputers;

import java.util.Map;

import octojus.ComputationRequest;
import octojus.OctojusCluster;
import octojus.OctojusNode;
import toools.io.file.RegularFile;

public class MapReduceDemo
{
	public static void main(String[] args) throws Throwable
	{
		OctojusCluster o = new OctojusCluster(System.getProperty("user.name"), null, new CoatiComputers());
		o.start();

		int n = new OctojusMapReduce<Boolean, Integer>()
		{

			@Override
			protected ComputationRequest<Boolean> map(OctojusNode n)
			{
				return new IsFedora();
			}

			@Override
			protected Integer reduce(Map<OctojusNode, Boolean> r)
			{
				int sum = 0;

				for (boolean b : r.values())
				{
					if (b)
						++sum;
				}

				return sum;
			}

		}.execute(o.getNodes());

		System.out.println(n);

	}

	public static class IsFedora extends ComputationRequest<Boolean>
	{
		@Override
		public Boolean compute() throws Throwable
		{
			return new RegularFile("/etc/fedora-release").exists();
		}
	};
}
