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

 
 package octojus.java4unix;

import j4u.CommandLine;
import jacaboo.NodeNameSet;
import octojus.OctojusCluster;
import toools.io.file.RegularFile;

public abstract class AbstractClusterScript extends OctojusCommand
{
	public AbstractClusterScript(RegularFile launcher)
	{
		super(launcher);
		// TODO Auto-generated constructor stub
	}

	@Override
	public final int runScript(CommandLine cmdLine) throws Throwable
	{
		NodeNameSet c = new NodeNameSet();

		for (String h : cmdLine.findParameters())
		{
			c.add(h);
		}

		OctojusCluster o = new OctojusCluster(System.getProperty("user.name"), null, c);
		o.start();
		return runScript(cmdLine, o);
	}

	protected abstract int runScript(CommandLine cmdLine, OctojusCluster o) throws Throwable;
}
