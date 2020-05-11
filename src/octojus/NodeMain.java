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

package octojus;

import jacaboo.RemoteMain;

import java.util.List;
import java.util.Random;

import toools.math.MathsUtilities;
import client_server.net.Server;

public class NodeMain extends RemoteMain
{
	/**
	 * Remote execution port.
	 */
	private static final int DEFAULT_PORT;
	private static int LISTENING_PORT = - 1;

	static
	{
		DEFAULT_PORT = MathsUtilities.pickRandomBetween(49152, 65535, new Random());
	}

	public static boolean allowLocalWorker = true;

	public static RequestQueue queue;

	public static int getListeningPort()
	{
		if (LISTENING_PORT == - 1)
		{
			LISTENING_PORT = DEFAULT_PORT;
		}
	
		return LISTENING_PORT;
	}

	public static void setListeningPort(int port)
	{
		LISTENING_PORT = port;
	}

	private Server[] servers;

	@Override
	public void main(List<String> args)
	{
		setListeningPort(Integer.valueOf(args.get(0)));
		int nbThreads = Integer.valueOf(args.get(1));
		// System.out.println("Running octojus.NodeMain " + getListeningPort() +
		// " " + nbThreads);

		if (nbThreads < 1)
		{
			nbThreads = Runtime.getRuntime().availableProcessors();
		}

		queue = new RequestQueue(nbThreads);

		servers = new Server[] { new TCPRequestServer(getListeningPort()) };

		for (final Server s : servers)
		{
			s.startInBackground();
		}
	}

	@Override
	public void stop()
	{
		for (final Server s : servers)
		{
			s.stop();
		}
		queue.stopWorkerThreads();
	}
}
