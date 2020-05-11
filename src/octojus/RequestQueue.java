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
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import toools.StopWatch;
import toools.StopWatch.UNIT;
import toools.math.LongVariableStats;

@SuppressWarnings("serial")
public class RequestQueue extends LinkedBlockingQueue<ComputationRequest<Serializable>>
{
	private int numberOfJobsAlreadyProcessed = 0;
	private ArrayList<Thread> workerThreads = new ArrayList<Thread>();
	private boolean mustStop = false;

	public RequestQueue(int nbThreads)
	{
		// System.out.println("Starting " + nbThreads + " threads for processing
		// computation requests in parallel");

		for (int i = 0; i < nbThreads; ++i)
		{
			startQueueProcessingThread(i);
		}
	}

	public int getNumberOfJobsAlreadyProcessed()
	{
		return numberOfJobsAlreadyProcessed;
	}

	private void startQueueProcessingThread(int rank)
	{
		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				while ( ! mustStop)
				{
					try
					{
						@SuppressWarnings("rawtypes")
						ComputationRequest newRequest = take();

						// synchronized (newRequest.connection)
						// {
						process(newRequest);
						// }
					}
					catch (InterruptedException e)
					{
						if (mustStop)
							break;
						else
						{
							e.printStackTrace();
							break;
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
						break;
					}
				}
			}
		});
		thread.setName("RequestWorker" + rank);
		workerThreads.add(thread);
		thread.start();
	}

	public void stopWorkerThreads()
	{
		mustStop = true;
		for (Thread worker : workerThreads)
		{
			worker.interrupt();
		}
		for (Thread worker : workerThreads)
		{
			try
			{
				worker.join(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	static public LongVariableStats responseSendStats = new LongVariableStats();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void process(ComputationRequest r) throws IOException
	{
		ComputationResponse response = r.processLocally();

		if (r.expectResponse())
		{
			try
			{
				response.responseEmissionDate = System.nanoTime();
				StopWatch sw = new StopWatch(UNIT.ns);

				synchronized (r.connection)
				{
					r.connection.out.writeObject(response);
					r.connection.out.flush();
				}


				// r.connection.out.flush();
				responseSendStats.addSample(sw.getElapsedTime());
			}
			catch (IOException e)
			{
				e.printStackTrace();
				throw e;
			}
		}

		++numberOfJobsAlreadyProcessed;
	}
}
