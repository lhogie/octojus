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

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import toools.net.NetUtilities;
import toools.os.JVM;
import toools.os.OperatingSystem;

@SuppressWarnings("serial")
public class DiscoverableNodeInfo implements Serializable
{
	static private int validityDurationMs = 10000;
	public static Class<? extends DiscoverableNodeInfo> infoClass = DiscoverableNodeInfo.class;

	public static void setValidityDuration(int validityDurationMs)
	{
		if (validityDurationMs < 1000)
			throw new IllegalArgumentException("too short " + validityDurationMs);

		DiscoverableNodeInfo.validityDurationMs = validityDurationMs;
	}

	public static long getValidityDurationMs()
	{
		return DiscoverableNodeInfo.validityDurationMs;
	}

	final long dateMs;
	long receptionTime;

	final int numberOfProcessors;
	final long[] performance;
	final long memoryAvailableInBytes;
	final long jvmProcessSize;
	final double loadAverage;
	final int numberOfJobsAlreadyProcessed;
	final int numberOfJobsInQueue;
	final List<InetAddress> localHardwareAddresses;
	final long pid;
	final String cmd;

	public DiscoverableNodeInfo()
	{
		this(500);
	}

	public DiscoverableNodeInfo(long stressDurationMs)
	{
		dateMs = System.currentTimeMillis();
		numberOfProcessors = Runtime.getRuntime().availableProcessors();
		loadAverage = OperatingSystem.getLocalOperatingSystem().getLoadAverage();

		performance = Performance.stress(stressDurationMs);

		// if the master is the local node in the cluster, no need to start the
		// services, so the queue is not created
		if (NodeMain.queue == null)
		{
			numberOfJobsAlreadyProcessed = - 1;
			numberOfJobsInQueue = - 1;
		}
		else
		{
			numberOfJobsAlreadyProcessed = NodeMain.queue
					.getNumberOfJobsAlreadyProcessed();
			numberOfJobsInQueue = NodeMain.queue.size();
		}

		System.gc();
		// memoryAvailableInBytes = localOS.getMemoryAvailableInBytes();
		memoryAvailableInBytes = Runtime.getRuntime().freeMemory();
		jvmProcessSize = Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().freeMemory();
		localHardwareAddresses = new ArrayList<>(NetUtilities.getHardwareIPv4Addresses());

		pid = JVM.getPID();
		cmd = ManagementFactory.getRuntimeMXBean().getInputArguments().toString();

		// Linux.getCommandLine(pid);
	}

	public long getJvmProcessSize()
	{
		return jvmProcessSize;
	}

	public boolean isFresh()
	{
		return System.currentTimeMillis()
				- receptionTime < DiscoverableNodeInfo.validityDurationMs;
	}

	public boolean isOverloaded()
	{
		return getLoadAverage() > getNumberOfProcessors();
	}

	public long getCreationTimeMs()
	{
		return dateMs;
	}

	public int getNumberOfProcessors()
	{
		return numberOfProcessors;
	}

	public long[] getPerformance()
	{
		return performance;
	}

	public long getMemoryAvailableInBytes()
	{
		return memoryAvailableInBytes;
	}

	public double getLoadAverage()
	{
		return loadAverage;
	}

	public int getNumberOfJobsAlreadyProcessed()
	{
		return numberOfJobsAlreadyProcessed;
	}

	public int getNumberOfJobsInQueue()
	{
		return numberOfJobsInQueue;
	}

	public List<InetAddress> getLocalHardwareAddresses()
	{
		return localHardwareAddresses;
	}

	public String getCommandLine()
	{
		return cmd;
	}

	@Override
	public String toString()
	{
		return "[creationTimeMs=" + dateMs + ", receptionTime=" + receptionTime
				+ ", numberOfProcessors=" + numberOfProcessors + ", performance="
				+ Arrays.toString(performance) + ", memoryAvailableInBytes="
				+ memoryAvailableInBytes + ", jvmProcessSize=" + jvmProcessSize
				+ ", loadAverage=" + loadAverage + ", numberOfJobsAlreadyProcessed="
				+ numberOfJobsAlreadyProcessed + ", numberOfJobsInQueue="
				+ numberOfJobsInQueue + ", localHardwareAddresses="
				+ localHardwareAddresses + "]";
	}


}
