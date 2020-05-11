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
import java.net.SocketException;

import toools.net.TCPConnection;
import client_server.net.TCPServer;

public class TCPRequestServer extends TCPServer
{

	public TCPRequestServer(int port)
	{
		super(port);
//		setPort(port);
	}


	@SuppressWarnings("unchecked")
	@Override
	protected void newIncomingConnection(TCPConnection connection)
	{
		if (connection.getSocket() != null)
		{
			try
			{
				connection.getSocket().setTcpNoDelay(true);
			}
			catch (SocketException e)
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			boolean senseOnly = connection.in.readBoolean();

			if (!senseOnly)
			{
				while ( ! getCurrentThread().mustStop() )
				{
					@SuppressWarnings("rawtypes")
					ComputationRequest r = (ComputationRequest) connection.in.readObject2();
					r.connection = connection;
					/* The following is a hack: we need to store the request reception date
					 * somewhere. Eventually it will be stored in the ComputationResponse
					 * object and transmitted back to the client node. But currently this
					 * object is not yet created and we use the field in the request object.
					 */
					r.setRequestReceptionDate(System.nanoTime());
//					System.out.println("process/" + Unix.getPID() + ": request received from " + connection.getSocket().getPort() + "->" + connection.getSocket().getLocalPort());
//					System.out.println(nbStartedThreads.get() + " " + nbFinishedThreads.get() + " " + nbJoinedThreads.get());
					if ( ! NodeMain.queue.offer(r))
					{
						System.err.println("Failed to enqueue ComputationRequest from node "
								+ connection.getSocket().getInetAddress().getHostName());
						break;
//						throw new IllegalStateException();
					}
					
					if (r.isUseSpecificConnection())
					{
						// the connection will be closed by the client when it will have received the
						// result of the computation
						break;
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			// client unexpectedly quit
			// no matter
		}
	}
}