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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import octojus.OctojusNode;
import toools.io.file.RegularFile;


public class DiskOperationPerformance extends PerformanceMetric
{
	@Override
	protected void oneTurn()
	{
		try
		{
			RegularFile f = RegularFile.createTempFile(OctojusNode.localDirectory, "lkj", "kj" + Math.random());
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

				if (i == -1)
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
}
