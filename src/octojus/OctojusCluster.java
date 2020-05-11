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

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import jacaboo.JavaCluster;
import jacaboo.JavaNode;
import jacaboo.NodeNameSet;
import jacaboo.RemoteMain;
import jacaboo.SSHNode;
import octojus.gui.OctojusClusterMonitorPane;
import toools.gui.Utilities;
import toools.io.FullDuplexDataConnection2;
import toools.net.ConnectionCloser;
import toools.net.NetUtilities;
import toools.thread.Threads;

public class OctojusCluster extends JavaCluster<OctojusNode>
{

	// @SuppressWarnings("serial")
	// static class DisseminateIPAddresses extends ComputationRequest<NoReturn>
	// {
	// private Map<InetAddress, DiscoverableNodeInfo> addressOfNodesInCluster =
	// new HashMap<>();
	//
	// public DisseminateIPAddresses(Set<OctojusNode> nodesInCluster)
	// {
	// for (OctojusNode n : nodesInCluster)
	// {
	// System.out.println("sending to " + n + ": " +
	// n.getDiscoveredInfo().localHardwareAddresses);
	// addressOfNodesInCluster.put(n.getPublicInetAddress(),
	// n.getDiscoveredInfo());
	// }
	// }
	//
	// @Override
	// protected NoReturn compute() throws Throwable
	// {
	// System.out.println("received from master " + addressOfNodesInCluster);
	//
	// for (InetAddress ip : addressOfNodesInCluster.keySet())
	// {
	// OctojusNode n = (OctojusNode) HardwareNode.getNode(ip);
	// n.setDiscoveredInfo(addressOfNodesInCluster.get(ip));
	// }
	//
	// return null;
	// }
	// }

	public OctojusCluster(String username, OctojusNode frontal, NodeNameSet nodeNames)
			throws UnknownHostException
	{
		this(frontal, toNodes(nodeNames, username));
	}

	public OctojusCluster(OctojusNode frontal, Set<OctojusNode> nodes)
	{
		super(frontal, nodes);
	}

	private static Set<OctojusNode> toNodes(Collection<String> nodenames, String username)
	{
		Set<OctojusNode> nodeSet = new HashSet<OctojusNode>();
		boolean duplicateNode = false;
		
		for (String nodename : nodenames)
		{
			OctojusNode node = new OctojusNode(nodename, username,
					NodeMain.getListeningPort());
			duplicateNode = false;
			
			for (OctojusNode existing : nodeSet)
			{
				if (node.equals(existing))
				{
					duplicateNode = true;
					break;
				}
			}

			if ( ! duplicateNode)
				nodeSet.add(node);
		}
		
		return nodeSet;
	}

	public static OctojusCluster localInstance;
	private OctojusNode localNode;

	public OctojusNode getLocalNode()
	{
		if (localNode == null)
		{
			for (OctojusNode node : getNodes())
			{
				if (node.isLocalNode())
				{
					localNode = node;
					break;
				}
			}
			
			if (localNode == null)
			{
				localNode = createLocalNode();
			}
		}
		
		return localNode;
	}

	protected OctojusNode createLocalNode()
	{
		try
		{
			return new OctojusNode(InetAddress.getLocalHost().getHostName(), System.getProperty("user.name"),
					NodeMain.getListeningPort());
		}
		catch (UnknownHostException e)
		{
			// Should not happen: resolution of localhost always work.
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void start()
	{
		super.start();

		// new OneNodeOneRequest()
		// {
		// @Override
		// protected ComputationRequest
		// createComputationRequestForNode(OctojusNode n)
		// {
		// return new DisseminateIPAddresses(getNodes());
		// }
		// }.execute(getNodes());
	}

	public int getNumberOfProcessors()
	{
		int sum = 0;

		for (OctojusNode s : getNodes())
		{
			if (s.getDiscoveredInfo().getNumberOfProcessors() > 0)
			{
				sum += s.getDiscoveredInfo().getNumberOfProcessors();
			}
		}

		return sum;
	}

	@Override
	public Class<? extends RemoteMain> getMainClass(JavaNode n)
	{
		return NodeMain.class;
	}

	/**
	 * Returns the list of arguments to use to launch a cluster node on the
	 * argument node. This list has to be consistent with the function
	 * {@link NodeMain#main(List)}.
	 */
	@Override
	public List<String> getMainClassParameters(JavaNode node)
	{
		List<String> parms = new ArrayList<>();
		parms.add(String.valueOf(((OctojusNode) node).getRemoteExecPort()));
		parms.add(Integer.toString(getNumberOffParallelRequests()));
		return parms;
	}

	public int getNumberOffParallelRequests()
	{
		return 1;
	}

	@Override
	protected void runClass(OctojusNode n) throws Throwable
	{
		super.runClass(n);
		ensureServerRuns(n, System.currentTimeMillis() + 1000 * getTimeoutInSecond());
		System.out.println("Server running on " + n);
		// n.ensureInfoIsUpToDate();
		// NewNodesInClusterInfo i = new NewNodesInClusterInfo();

		// i.nodes.addAll(getNodes());

		// i.runOn(n);
	}

	@SuppressWarnings({ "serial", "unused" })
	private static class NewNodesInClusterInfo extends ComputationRequest<NoReturn>
	{
		// Set<InetAddress> ips = new HashSet<InetAddress>();
		Set<SSHNode> nodes = new HashSet<SSHNode>();

		@Override
		protected NoReturn compute() throws Throwable
		{
			if (OctojusCluster.localInstance == null)
				OctojusCluster.localInstance = new OctojusCluster(
						System.getProperty("user.name"), null, new NodeNameSet());

			// for (SSHNode node : nodes)
			// {
			// OctojusNode n = (OctojusNode)
			// HardwareNode.getNode(node.getPublicInetAddress(),
			// node.getRemoteExecPort());
			// OctojusCluster.localInstance.add(n);
			// }
			// for (InetAddress ip : ips)
			// {
			// OctojusNode n = (OctojusNode) HardwareNode.getNode(ip);
			// OctojusCluster.localInstance.add(n);
			// }

			return null;
		}
	}

	private static ConnectionCloser connectionCleanCloser = new ConnectionCloser()
	{
		@Override
		public void closeCleanly(FullDuplexDataConnection2 c) throws IOException
		{
			c.out.writeBoolean(true);
			c.out.flush();
		}
	};

	private static void ensureServerRuns(OctojusNode node, long ultimatumTimeMs)
	{
		while (true)
		{
			if ( ! node.isLocalhost() && node.isProcessTerminated())
			{
				throw new IllegalStateException("server process terminated on "
						+ node.getInetAddress());
			}
			else if (System.currentTimeMillis() > ultimatumTimeMs)
			{
				throw new IllegalStateException("connection timeout on "
						+ node.getInetAddress());
			}
			else
			{
				if (NetUtilities.isServerRunningOnPort(node.getInetAddress(),
						node.getRemoteExecPort(), 500, connectionCleanCloser))
				{
					break;
				}
				else
				{
					Threads.sleepMs(100);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private static void ensureServerIsStopped(OctojusNode node, int timeoutSec)
	{
		while (true)
		{
			if (NetUtilities.isServerRunningOnPort(node.getInetAddress(),
					node.getRemoteExecPort(), 500, connectionCleanCloser))
			{
				Threads.sleepMs(100);
			}
			else
			{
				break;
			}
		}
	}

	public long computeTotalMemoryUsedByJVMs()
	{
		long n = 0;

		for (OctojusNode w : getNodes())
		{
			n += w.getDiscoveredInfo().getJvmProcessSize();
		}

		return n;
	}

	public double computeTotalLoadAverage()
	{
		double n = 0;

		for (OctojusNode w : getNodes())
		{
			n += w.getDiscoveredInfo().getLoadAverage();
		}

		return n;
	}

	public long computeTotalNumberOfCores()
	{
		long n = 0;

		for (OctojusNode w : getNodes())
		{
			n += w.getDiscoveredInfo().getNumberOfProcessors();
		}

		return n;
	}

	public void monitor()
	{
		Utilities.displayInJFrame(getMonitoringComponent(), "Octojus");
	}

	public static NodeNameSet localhostClusterNames(int nNodes,
			boolean includeLocalProcess)
	{
		int execPortBase = NodeMain.getListeningPort();
		NodeNameSet nodeNames = new NodeNameSet();
		for (int i = (includeLocalProcess ? 0 : 1); i < (includeLocalProcess ? nNodes
				: nNodes + 1); i++)
		{
			nodeNames.add("localhost:" + (execPortBase + i));
		}
		return nodeNames;
	}

	public static OctojusCluster localhostCluster(int nNodes, boolean includeLocalProcess)
			throws UnknownHostException
	{
		NodeNameSet nodeNames = localhostClusterNames(nNodes, includeLocalProcess);
		return new OctojusCluster(System.getProperty("user.name"), null, nodeNames);
	}

	protected JComponent getMonitoringComponent()
	{
		return new OctojusClusterMonitorPane(this);
	}
	
	public MemoryAndGCInfo getOverallMemoryAndGCStats()
	{
		MemoryAndGCInfo res = new MemoryAndGCInfo();
		res.gcTimeMin = Long.MAX_VALUE;
		res.gcTimeMax = Long.MIN_VALUE;
		
		for (Map.Entry<OctojusNode, MemoryAndGCInfo> entry : getMemoryAndGCStatsPerNode().entrySet())
		{
			res.gcCount += entry.getValue().gcCount;
			long time = entry.getValue().gcTimeMs;
			res.gcTimeMs += time;
			res.usedHeapMem += entry.getValue().usedHeapMem;
			if (time < res.gcTimeMin)
				res.gcTimeMin = time;
			if (time > res.gcTimeMax)
				res.gcTimeMax = time;
		}
		return res;
	}

	public Map<OctojusNode, MemoryAndGCInfo> getMemoryAndGCStatsPerNode()
	{
		return new OneNodeOneRequest<MemoryAndGCInfo>()
		{
			@Override
			protected ComputationRequest<MemoryAndGCInfo> createComputationRequestForNode(
					OctojusNode n)
			{
				return new MemoryAndGCInfoRequest();
			}
		}.execute(this.getNodes());
	}

	@SuppressWarnings("serial")
	public static final class MemoryAndGCInfo implements Serializable
	{
		public long gcCount = 0;      // Number of GC collections
		public long gcTimeMs = 0;     // Time spent in GC
		public long gcTimeMin = 0;
		public long gcTimeMax = 0;
		public long usedHeapMem = 0;  // Used memory (heap)
	}

	@SuppressWarnings("serial")
	private static final class MemoryAndGCInfoRequest extends
			ComputationRequest<MemoryAndGCInfo>
	{
		@Override
		protected MemoryAndGCInfo compute() throws Throwable
		{
			MemoryAndGCInfo res = new MemoryAndGCInfo();
			for (GarbageCollectorMXBean gc : ManagementFactory
					.getGarbageCollectorMXBeans())
			{
				long count = gc.getCollectionCount();
				long time = gc.getCollectionTime();
				if (count != -1 && time != -1)
				{
					res.gcCount += count;
					res.gcTimeMs += time;
				}
			}
			res.usedHeapMem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()
					.getUsed();
			return res;
		}
	}

}
