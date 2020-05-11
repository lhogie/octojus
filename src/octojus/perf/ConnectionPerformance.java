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


package octojus.perf;

import java.net.InetAddress;

import octojus.ComputationRequest;
import octojus.ComputationResponse;
import octojus.NodeMain;
import octojus.OctojusConnection;
import toools.StopWatch;
import toools.text.TextUtilities;

public class ConnectionPerformance
{
	public enum CONNECTION_PRIORIY
	{
		LATENCY, THROUGHPUT
	}

	private final InetAddress ip;
	private final long latencyNs;
	private final long throughput;

	public ConnectionPerformance(InetAddress ip, long latencyNs2, long throughput2)
	{
		this.ip = ip;
		this.latencyNs = latencyNs2;
		this.throughput = throughput2;
	}

	public InetAddress getIp()
	{
		return ip;
	}

	public long getLatencyNs()
	{
		if (latencyNs < 0)
			throw new IllegalStateException("latency information is not available");

		return latencyNs;
	}

	public long getThroughput()
	{
		if (throughput < 0)
			throw new IllegalStateException("throughput information is not available");

		return throughput;
	}

	@Override
	public String toString()
	{
		String s = "IP destination=" + ip;

		if (latencyNs >= 0)
		{
			s += ", latencyNs=" + latencyNs + "ns";
		}

		if (throughput >= 0)
		{
			s += ", throughput=" + TextUtilities.toHumanString(throughput) + "b/s";
		}

		return s;
	}

	public static ConnectionPerformance bench(InetAddress ip, boolean quick) throws Throwable
	{
		OctojusConnection connection = new OctojusConnection(ip, NodeMain.getListeningPort(), 1000);

		// not a test connection
		connection.out.writeBoolean(false);

		long latencyNs = benchLatencyInNanoSeconds(connection, quick ? 1 : 10, 1000000000);
		long throughput = quick ? -1 : benchThroughput(connection, latencyNs);
		ConnectionPerformance perf = new ConnectionPerformance(ip, latencyNs, throughput);
		System.out.println(perf);
		return perf;
	}

	private static long benchLatencyInNanoSeconds(OctojusConnection connection, int maxRun, long maxNs) throws Throwable
	{
		// System.out.println("test latency");
		StopWatch sw = new StopWatch(StopWatch.UNIT.ns);

		for (int nbRound = 0;; ++nbRound)
		{
			Job j = new Job(0);
			j.setCloseConnectionAfterReturn(false);
			ComputationResponse<Integer> r = j.computeThrought(connection);

			if (nbRound > maxRun || (sw.getElapsedTime() > maxNs && nbRound > 0))
			{
				Job closingJob = new Job(0);
				closingJob.setCloseConnectionAfterReturn(true);
				j.computeThrought(connection);
				return (sw.getElapsedTime() / nbRound) / 2;
			}
		}
	}

	private static long benchThroughput(OctojusConnection connection, long latencyNs) throws Throwable
	{
		// System.out.println("test benchThroughput");

		for (int szKb = 1024;; szKb *= 2)
		{
			Job j = new Job(1024 * szKb);
			j.setCloseConnectionAfterReturn(false);
			StopWatch sw = new StopWatch(toools.StopWatch.UNIT.ms);
			ComputationResponse<Integer> r = j.computeThrought(connection);
			double durationMS = sw.getElapsedTime() - 2 * (latencyNs / 1000000);

			if (durationMS > 0)
			{
				if (durationMS > 1000)
				{
					return (long) (szKb * 1000 / durationMS);
				}
			}
		}
	}

	static class Job extends ComputationRequest<Integer>
	{
		byte[] data;

		public Job(int nbByte)
		{
			data = new byte[nbByte];
		}

		@Override
		protected Integer compute() throws Throwable
		{
			return data.length;
		}
	}

}
