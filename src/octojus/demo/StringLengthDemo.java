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

import java.util.Random;

import octojus.ComputationRequest;
import octojus.NodeMain;
import octojus.OctojusCluster;
import octojus.OctojusNode;
import toools.StopWatch;
import toools.StopWatch.UNIT;
import toools.text.TextUtilities;

public class StringLengthDemo
{
	@SuppressWarnings("serial")
	static class StringLengthRequest extends ComputationRequest<Integer>
	{
		String text;

		@Override
		protected Integer compute()
		{
			return text.length();
		}
	}

	public static void main(String[] args) throws Throwable
	{

		OctojusCluster o = new OctojusCluster(System.getProperty("user.name"), null, new NodeNameSet("localhost:" + NodeMain.getListeningPort()));
		System.out.println("<tarting");

		o.start();
		System.out.println("<tarted");

		StopWatch sw = new StopWatch(UNIT.us);

		for (int i = 0; i < 5; ++i)
		{
			for (OctojusNode n : o.getNodes())
			{
				// System.out.println(i);
				StringLengthRequest j = new StringLengthRequest();
				j.text = TextUtilities.pickRandomString(new Random(), 0, 50);
				int l = j.runOn(n);
				System.out.println(n + ": " + i + ": " + j.text + ": " + l);
			}
		}

		System.out.println(sw);

	}
}
