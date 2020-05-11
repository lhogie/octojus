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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jacaboo.HardwareNode;
import jacaboo.JVM;
import jacaboo.JavaNode;
import jacaboo.RemoteMain;
import jacaboo.SSHNode;
import octojus.perf.ConnectionPerformance;
import octojus.perf.ConnectionPerformance.CONNECTION_PRIORIY;
import toools.io.IORuntimeException;
import toools.io.file.Directory;
import toools.net.TCPConnection;

public class OctojusNode extends JavaNode
{
	public static final Directory localDirectory;

	static
	{
		Directory d = new Directory(Directory.getHomeDirectory(), "local_drive");
		localDirectory = d.exists() ? d : Directory.getHomeDirectory();
	}

	
	private DiscoverableNodeInfo discoveredInfo;
	public TCPConnection connection;
	private final List<NodeInfoListener> listeners = new ArrayList<>();
	private ArrayList<ConnectionPerformance> sortedIPs_latency;
	private ArrayList<ConnectionPerformance> sortedIPs_throughput;
	private int remoteExecPort;
	private int isLocalNode = - 1;

	/**
	 * For serialization, do not use.
	 */
	public OctojusNode()
	{
		super();
	}

	public OctojusNode(InetAddress addr, int remoteExecPort)
	{
		super(addr);
		this.remoteExecPort = remoteExecPort;
	}

	public OctojusNode(String nodeSpec, String defaultUsername)
	{
		super();
		fromString(nodeSpec, defaultUsername, NodeMain.getListeningPort());
	}

	public OctojusNode(String nodeSpec, String defaultUsername, int defautListeningPort)
	{
		super();
		fromString(nodeSpec, defaultUsername, defautListeningPort);
	}

	protected void fromString(String nodeSpec, String defaultUsername, int defautExecPort)
	{
		String comps[] = nodeSpec.split(":");

		// if there's no explicit port for computation server, use the default
		// one
		if (comps.length == 1)
		{
			this.remoteExecPort = defautExecPort;
			super.fromString(nodeSpec, defaultUsername);
		}
		else if (comps.length == 2)
		{
			this.remoteExecPort = Integer.valueOf(comps[1]);
			super.fromString(comps[0], defaultUsername);
		}
		else
		{
			throw new IORuntimeException(nodeSpec);
		}
	}

	@Override
	public String toString()
	{
		return super.toString() + ":" + getRemoteExecPort();
	}

	public int getRemoteExecPort()
	{
		return remoteExecPort;
	}


	@Override
	public  boolean isLocalNode()
	{
		if (isLocalNode != - 1)
			return isLocalNode == 1;

		boolean localHost = isLocalhost();
		boolean samePort = remoteExecPort == NodeMain.getListeningPort();
		
		if (localHost && samePort)
		{
			isLocalNode = 1;
		}
		else
		{
			isLocalNode = 0;
		}

		return isLocalNode == 1;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeInt(getRemoteExecPort());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		super.readExternal(in);
		remoteExecPort = in.readInt();
	}


	// public int index = -1;


	public DiscoverableNodeInfo getDiscoveredInfo()
	{
		ensureInfoIsUpToDate(false);
		return discoveredInfo;
	}

	public octojus.OctojusConnection connect(int port, int timeoutMs)
			throws UnknownHostException, IOException
	{
		InetAddress ip = getInetAddress();
		return new OctojusConnection(ip, port, timeoutMs);
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof OctojusNode && super.equals(o)
				&& remoteExecPort == ((OctojusNode) o).getRemoteExecPort();
	}

	@Override
	public int compareTo(HardwareNode o)
	{
		int same = super.compareTo(o);
		if (same != 0)
			return same;
		if ( ! (o instanceof OctojusNode))
			return 1;
		return remoteExecPort - ((OctojusNode) o).getRemoteExecPort();
	}

	public void ensureInfoIsUpToDate(boolean force)
	{
		if (discoveredInfo == null || force || ! discoveredInfo.isFresh())
		{
			try
			{
				DiscoverableNodeInfo newInfo = new FreshInfoRequest().runOn(this);
				newInfo.receptionTime = System.currentTimeMillis();
				setDiscoveredInfo(newInfo);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
				throw new IllegalStateException(t);
			}
		}
	}


	public List<ConnectionPerformance> getIPs_sorted_by_latency()
	{
		if (sortedIPs_latency == null)
		{
			ensureInfoIsUpToDate(false);
			benchIPs(true);
		}

		return sortedIPs_latency;
	}

	public List<ConnectionPerformance> getIPs_sorted_by_throughput()
	{
		if (sortedIPs_throughput == null)
		{
			ensureInfoIsUpToDate(false);
			benchIPs(false);
		}

		return sortedIPs_throughput;
	}

	public ConnectionPerformance getFastestInterface()
	{
		// List<ConnectionPerformance> l = getIPs_sorted_by_latency();
		// List<ConnectionPerformance> t = getIPs_sorted_by_throughput();
		//
		// if (l.get(0) == t.get(0))
		// {
		// return l.get(0);
		// }
		//
		// throw new
		// NoSuchElementException("there's no such 'best' connection: you need
		// to balance latency and throughput");

		return getIPs_sorted_by_latency().get(0);
	}

	private void benchIPs(boolean quickBench)
	{
		this.sortedIPs_latency = new ArrayList<>();
		this.sortedIPs_throughput = quickBench ? new ArrayList<ConnectionPerformance>()
				: null;

		for (InetAddress ip : discoveredInfo.getLocalHardwareAddresses())
		{
			try
			{
				ConnectionPerformance perf = ConnectionPerformance.bench(ip, quickBench);
				System.out.println("testing connection " + ip + " to node "
						+ getInetAddress() + ": " + perf);
				sortedIPs_latency.add(perf);

				if (sortedIPs_throughput != null)
				{
					sortedIPs_throughput.add(perf);
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				System.err.println("discarding connection to " + ip);
			}
		}

		Collections.sort(sortedIPs_latency, new Comparator<ConnectionPerformance>()
		{
			@Override
			public int compare(ConnectionPerformance c1, ConnectionPerformance c2)
			{
				return Long.compare(c1.getLatencyNs(), c2.getLatencyNs());
			}
		});

		if (sortedIPs_throughput != null)
		{
			Collections.sort(sortedIPs_throughput, new Comparator<ConnectionPerformance>()
			{
				@Override
				public int compare(ConnectionPerformance c1, ConnectionPerformance c2)
				{
					return - Long.compare(c1.getThroughput(), c2.getThroughput());
				}
			});
		}
	}

	public List<NodeInfoListener> getListeners()
	{
		return listeners;
	}

	public static void refreshInParallel(Collection<OctojusNode> nodes,
			final boolean force)
	{
		synchronized (OctojusNode.class)
		{
			new OneNodeOneThread(nodes)
			{
				@Override
				protected void process(OctojusNode node)
				{
					node.ensureInfoIsUpToDate(force);
				}
			};
		}
	}

	public void setDiscoveredInfo(DiscoverableNodeInfo newNodeInfo)
	{
		if (discoveredInfo != null
				&& discoveredInfo.dateMs > newNodeInfo.dateMs)
			throw new IllegalStateException("local info is newer");

		this.discoveredInfo = newNodeInfo;

		for (NodeInfoListener l : listeners)
		{
			l.updated(this);
		}
	}

	public List<ConnectionPerformance> getIPs(CONNECTION_PRIORIY priorityCriterion)
	{
		if (priorityCriterion == CONNECTION_PRIORIY.LATENCY)
		{
			return getIPs_sorted_by_latency();
		}
		else if (priorityCriterion == CONNECTION_PRIORIY.THROUGHPUT)
		{
			return getIPs_sorted_by_throughput();
		}

		throw new IllegalArgumentException("" + priorityCriterion);
	}
}
