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
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import octojus.perf.ConnectionPerformance.CONNECTION_PRIORIY;
import toools.io.FullDuplexDataConnection2;
import toools.math.MathsUtilities;

@SuppressWarnings("serial")
public abstract class ComputationRequest<ReturnType>
		implements Serializable
{
	// private transient final static Set<Job<?>> activeFutures = new
	// HashSet<Job<?>>();

	public enum STATUS
	{
		NOT_STARTED, RUNNING, TERMINATED
	};

	static private AtomicInteger launchedRequestsCount = new AtomicInteger(0);
	static private AtomicInteger completedRequestsCount = new AtomicInteger(0);

	private transient STATUS status = STATUS.NOT_STARTED;
	private transient ReturnType result;
	private transient Throwable error;
	private transient long requestEmissionDate = - 1;
	private transient long requestReceptionDate = - 1;
	private transient long jobExecutionStartDate = - 1;
	private transient long jobExecutionEndDate = - 1;
	private transient long responseEmissionDate = - 1;
	private transient long responseReceptionDate = - 1;

	private boolean expectResponse = true;
	transient FullDuplexDataConnection2 connection;
	private long ID = new Random().nextLong();
	private boolean lastRequestOnConnection = false;

	public long getResponseEmissionDate()
	{
		return responseEmissionDate;
	}

	public long getResponseReceptionDate()
	{
		return responseReceptionDate;
	}

	public long getRequestReceptionDate()
	{
		return requestReceptionDate;
	}

	public void setRequestReceptionDate(long requestReceptionDate)
	{
		this.requestReceptionDate = requestReceptionDate;
	}

	public long getRequestEmissionDate()
	{
		return requestEmissionDate;
	}

	public boolean isUseSpecificConnection()
	{
		return lastRequestOnConnection;
	}

	public void setCloseConnectionAfterReturn(boolean last)
	{
		this.lastRequestOnConnection = last;
	}

	public FullDuplexDataConnection2 getConnection()
	{
		return connection;
	}

	public ComputationRequest()
	{
		// if this is a non-static inner class
		if (getClass().getEnclosingClass() != null
				&& ! Modifier.isStatic(getClass().getModifiers()))
			System.err.println(
					"this is a VERY bad idea to make a job as a non-static inner class since its enclosing class will be serialized along with it");
	}

	public void cancelReply()
	{
		expectResponse = false;
	}

	public long getComputationDuration()
	{
		if (status != STATUS.TERMINATED)
			throw new IllegalStateException("request has not yet completed");

		return jobExecutionEndDate - jobExecutionStartDate;
	}

	public long getTransmissionDuration()
	{
		if (status != STATUS.TERMINATED)
			throw new IllegalStateException("request has not yet completed");

		return responseReceptionDate - requestEmissionDate
				- (jobExecutionEndDate - jobExecutionStartDate)
				- (jobExecutionStartDate - requestReceptionDate);
	}

	public long getExecutionDelay()
	{
		if (status != STATUS.TERMINATED)
			throw new IllegalStateException("request has not yet completed");

		return jobExecutionStartDate - requestReceptionDate;
	}

	public Object getResult()
	{
		if (status != STATUS.TERMINATED)
			throw new IllegalStateException("request has not yet completed");

		if (error != null)
			throw new IllegalStateException(
					"no result available, the job failed. Use getException() instead");

		return result;
	}

	public Throwable getError()
	{
		if (status != STATUS.TERMINATED)
			throw new IllegalStateException("request has not yet completed");

		if (error == null)
			throw new IllegalStateException(
					"no error available, the job succeeded. Use getResult() instead");

		return error;
	}

	protected abstract ReturnType compute() throws Throwable;

	public Thread runAsynchronouslyOn(final OctojusNode n,
			final ComputationListener<ReturnType> l) throws Throwable
	{
		return new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ReturnType r = runOn(n);
					l.completed(ComputationRequest.this, r, n);
				}
				catch (Throwable e)
				{
					l.failed(ComputationRequest.this, e, n);
				}
			}
		});
	}

	public ReturnType runOn(OctojusNode n) throws Throwable
	{
		return runOn(n, 3000);
	}

	public InetAddress getIP(OctojusNode n)
	{
		return n.getInetAddress();
	}

	protected CONNECTION_PRIORIY getMostImportantConnectionProperty()
	{
		return CONNECTION_PRIORIY.LATENCY;
	}

	// public static LongVariableStats requestSendStats = new
	// LongVariableStats();
	// public static LongVariableStats responseReceiveStats = new
	// LongVariableStats();

	private ReturnType runOn(OctojusNode n, int timeoutMs) throws Throwable
	{
		if (status != STATUS.NOT_STARTED)
			throw new IllegalStateException("request already submitted");

		// if (launchedRequestsCount.get() % 100 == 0)
		// {
		// ProcessBuilder pb = new ProcessBuilder("netstat", "-apl", "--tcp");
		// File log = new File("netstat.log");
		// pb.redirectErrorStream(true);
		// pb.redirectOutput(Redirect.appendTo(log));
		// Process p = pb.start();
		// p.waitFor();
		// }

		if (n.isLocalNode())
		{
			// System.out.println("executing on local node " + n);
			requestEmissionDate = jobExecutionStartDate = System.nanoTime();
			@SuppressWarnings("hiding")
			ReturnType result = compute();
			jobExecutionEndDate = responseEmissionDate = responseReceptionDate = System
					.nanoTime();
			return result;
		}
		else
		{
			// System.out.println("executing on remote node " + n);
			requestEmissionDate = System.nanoTime();
			status = STATUS.RUNNING;

			try
			{
				// do not execute 2 jobs simultaneously on the same server
				// synchronized (n)
				{
					if (lastRequestOnConnection)
					{
						connection = n.connect(n.getRemoteExecPort(), timeoutMs);
						/*
						 * The connection is not intended to be a test
						 * connection (a kind of 'ping')
						 * The boolean value sent here is read as the senseOnly
						 * variable in the
						 * TCPRequestServer.newIncomingConnection(TCPConnection)
						 * function.
						 */
						connection.out.writeBoolean(false);
					}
					else
					{
						if (n.connection == null)
						{
							n.connection = n.connect(n.getRemoteExecPort(), timeoutMs);
							n.connection.out.writeBoolean(false);
						}

						connection = n.connection;
					}

					ComputationResponse<ReturnType> response = computeThrought(
							connection);

					assert response.requestID == ID;

					// if that was the last request through the connection
					if (lastRequestOnConnection)
					{
						connection.close();
						connection = null;
					}

					if (response != null)
					{
						// if the job failed on the server
						if (response.error == null)
						{
							return response.result;
						}
						else
						{
							System.err.println("exception on " + n);
							throw response.error;
						}
					}
					else
					{
						return null;
					}
				}
			}
			catch (IOException e)
			{
				throw e;
			}
		}
	}

	public ComputationResponse<ReturnType> computeThrought(FullDuplexDataConnection2 c)
			throws IOException
	{
		// StopWatch sw = new StopWatch(UNIT.ns);
		c.out.writeObject(this);
		c.out.flush();
		// requestSendStats.addSample( sw.getElapsedTime() );
		launchedRequestsCount.incrementAndGet();
		// c.out.flush();

		if (expectResponse)
		{
			while (true)
			{
				// sw.reset();
				Object o = c.in.readObject2();
				// responseReceiveStats.addSample( sw.getElapsedTime() );

				if (o instanceof ComputationResponse)
				{
					@SuppressWarnings("unchecked")
					ComputationResponse<ReturnType> response = (ComputationResponse<ReturnType>) o;

					if (isUseSpecificConnection())
					{
						c.close();
						c = null;
					}

					requestReceptionDate = response.requestReceptionDate;
					jobExecutionStartDate = response.requestExecutionStartDate;
					jobExecutionEndDate = response.requestExecutionEndDate;
					responseEmissionDate = response.responseEmissionDate;
					responseReceptionDate = System.nanoTime();
					this.error = response.error;
					this.result = response.result;
					status = STATUS.TERMINATED;
					completedRequestsCount.incrementAndGet();
					return response;
				}
				else
				{
					feedbackReceived(o);
				}
			}
		}
		else
		{
			if (isUseSpecificConnection())
			{
				c.close();
				c = null;
			}

			return null;
		}
	}

	protected void sendFeedbackToclient(Serializable feedback)
	{
		connection.out.writeObject2(feedback);
		connection.out.flush2();
	}

	protected void feedbackReceived(Object o)
	{
		System.out.println("The following feedback was received from instance of "
				+ getClass()
				+ ", you should override the 'void feedbackReceived(Object o)' method to handle it properly:");
		System.out.println(o);
	}

	public STATUS getStatus()
	{
		return status;
	}

	public long evaluateAdequacyOf(OctojusNode w)
	{
		return 1;
	}

	public List<OctojusNode> sortWorkersByDecreasingAdequacy(Collection<OctojusNode> c)
	{
		final Map<OctojusNode, Long> note = new HashMap<OctojusNode, Long>();
		List<OctojusNode> r = new ArrayList<OctojusNode>(c);

		for (OctojusNode w : r)
		{
			note.put(w, evaluateAdequacyOf(w));
		}

		Collections.sort(r, new Comparator<OctojusNode>()
		{

			@Override
			public int compare(OctojusNode a, OctojusNode b)
			{
				return - MathsUtilities.compare((long) note.get(a), (long) note.get(b));
			}
		});

		return r;

	}

	public boolean expectResponse()
	{
		return expectResponse;
	}

	ComputationResponse<ReturnType> processLocally()
	{
		final ComputationResponse<ReturnType> response = new ComputationResponse<ReturnType>();
		response.requestID = ID;
		response.requestExecutionStartDate = System.nanoTime();
		/*
		 * In TCPRequestServer.newIncomingConnection() the field
		 * requestReceptionDate
		 * is used as a temporary placeholder for the value, but it needs to be
		 * stored in
		 * the ComputationResponse object in order to be sent back to the
		 * client. Ultimately
		 * it will be copied in the original request object on the client node
		 * when the
		 * response will be received on this node.
		 */
		response.requestReceptionDate = requestReceptionDate;

		// System.out.println("ComputationRequest.processLocally() on process/"
		// + Unix.getPID());
		try
		{
			response.result = compute();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			response.error = t;
		}
		response.requestExecutionEndDate = System.nanoTime();
		return response;
	}

}
