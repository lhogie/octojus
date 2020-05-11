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
import jacaboo.clusters.CoatiComputers;

import java.io.IOException;

import octojus.OctojusCluster;

public class MonitorCluster
{
	public static void main(String[] args) throws IOException
	{
		OctojusCluster.localInstance = new OctojusCluster("lhogie", null, new NodeNameSet("musclotte", "oops"));
		OctojusCluster.localInstance.start();
		OctojusCluster.localInstance.monitor();
	}
}
