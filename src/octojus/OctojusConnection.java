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
import java.net.InetAddress;
import java.net.UnknownHostException;

import octojus.perf.ConnectionPerformance;
import toools.net.TCPConnection;

public class OctojusConnection extends TCPConnection
{
	private ConnectionPerformance performanceInformation;

	public OctojusConnection(InetAddress hostname, int port, int timeoutms) throws UnknownHostException, IOException
	{
		super(hostname, port, timeoutms, true);
		
		if (getSocket() != null)
		{
			getSocket().setTcpNoDelay(true);
		}
	}

	public OctojusConnection(ConnectionPerformance perf, int port, int timeoutms) throws UnknownHostException, IOException
	{
		this(perf.getIp(), port, timeoutms);
		this.performanceInformation = perf;
	}

	public ConnectionPerformance getPerformanceInformation()
	{
		return performanceInformation;
	}

	@Override
	public String toString()
	{
		return getSocket().getInetAddress() + (performanceInformation == null ? "no performance information available" : performanceInformation.toString());
	}

}
