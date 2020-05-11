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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import toools.io.file.RegularFile;

public class Performance
{
	public enum TYPE
	{
		floatingPointOperations, integerOperations, RAM, diskAccess
	};

	public static long[] stress(long durationMs)
	{
		long[] r = new long[TYPE.values().length];

		for (TYPE t : TYPE.values())
		{
			r[t.ordinal()] = stress(durationMs, t);
		}

		return r;
	}

	public static long stress(long durationMs, TYPE type)
	{
		long numberOfTurns = 0;
		long start = System.currentTimeMillis();

		// work during one second
		while (System.currentTimeMillis() - start < durationMs)
		{
			if (type == TYPE.floatingPointOperations)
			{
				stressFlops(durationMs);
			}
			else if (type == TYPE.integerOperations)
			{
				stressIntOps(durationMs);
			}
			else if (type == TYPE.RAM)
			{
				stressRAM();
			}
			else if (type == TYPE.diskAccess)
			{
				stressDisk();
			}
			else
				throw new IllegalArgumentException("unsupported");

			++numberOfTurns;
		}

		return numberOfTurns;
	}

	private static void stressDisk()
	{
		try
		{
			RegularFile f = RegularFile.createTempFile(OctojusNode.localDirectory, "lkj",
					"kj" + Math.random());
			OutputStream os = f.createWritingStream(false, 0);

			for (int i = 0; i < 1000; ++i)
			{
				os.write(i);
				os.flush();
			}

			os.close();

			InputStream is = f.createReadingStream();

			while (true)
			{
				int i = is.read();

				if (i == - 1)
				{
					break;
				}
			}

			f.delete();
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static void stressRAM()
	{
		@SuppressWarnings("rawtypes")
		List l = new ArrayList<>();

		for (int i = 0; i < 100000; ++i)
		{
			l.add(l);
		}
	}

	private static void stressFlops(long durationMs)
	{
		for (double i = 0; i < 1000000d; ++i)
		{
		}
	}

	private static void stressIntOps(long durationMs)
	{
		for (int x = 0; x < 1000000d; ++x)
		{
		}
	}

	public static void main(String... s)
	{
		for (TYPE t : TYPE.values())
		{
			System.out.print(t.name());
			System.out.println("\t" + stress(100, t));
		}
	}

}
