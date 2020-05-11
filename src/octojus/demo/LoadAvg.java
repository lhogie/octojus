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
import octojus.OctojusCluster;
import octojus.OctojusNode;
import toools.thread.Threads;

public class LoadAvg
{
	public static void main(String[] args) throws Throwable
	{
		OctojusCluster o = new OctojusCluster(System.getProperty("user.name"), null, new NodeNameSet("srv-coati", "musclotte"));
		o.start();

		while (true)
		{
			OctojusNode.refreshInParallel(o.getNodes(), true);
			for (OctojusNode n : o.getNodes())
			{
				System.out.println(n + " -- " + n.getDiscoveredInfo().getLoadAverage());
				Threads.sleepMs(1000);
			}
		}
	}
}
