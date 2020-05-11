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

import java.util.Collection;

import j4u.ArgumentSpecification;
import j4u.CommandLine;
import j4u.OptionSpecification;
import octojus.ComputationRequest;
import octojus.NoReturn;
import octojus.OctojusCluster;
import octojus.OctojusNode;
import octojus.OneNodeOneRequest;
import toools.io.file.RegularFile;
import toools.os.JVM;

public class test extends AbstractClusterScript
{

	public test(RegularFile launcher)
	{
		super(launcher);
	}

	@Override
	public String getShortDescription()
	{
		return "test the given cluster";
	}

	public static void main(String[] args) throws Throwable
	{
		new test(null).run("localhost");
	}

	

	@Override
	protected int runScript(CommandLine cmdLine, OctojusCluster o) throws Throwable
	{
		TestComputationRequest j = new TestComputationRequest();

		new OneNodeOneRequest()
		{
			@Override
			protected ComputationRequest createComputationRequestForNode(OctojusNode n)
			{
				return new TestComputationRequest();
			}
		}.execute(o.getNodes());

		return 0;
	}

	public static class TestComputationRequest extends ComputationRequest<NoReturn>
	{

		@Override
		protected NoReturn compute() throws Throwable
		{
			System.out.println("I'm running on " + JVM.getPID());
			return null;
		}
	}
}
