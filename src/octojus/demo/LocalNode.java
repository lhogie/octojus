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

import jacaboo.NodeNameSet;
import octojus.ComputationRequest;
import octojus.OctojusCluster;
import octojus.OctojusNode;

public class LocalNode
{
	@SuppressWarnings("serial")
	static class StringLengthJob extends ComputationRequest<Integer>
	{
		String s;

		@Override
		protected Integer compute()
		{
			return s.length();
		}
	}

	public static void main(String[] args) throws Throwable
	{
		OctojusCluster oCluster = new OctojusCluster(System.getProperty("user.name"), null, new NodeNameSet("localhost"));
		oCluster.start();
		System.out.println("Local node is: " + oCluster.getLocalNode());
		StringLengthJob j = new StringLengthJob();
		j.s = "oucou";
		System.out.println(j.runOn((OctojusNode) oCluster.getLocalNode()));

	}
}
