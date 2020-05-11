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

import octojus.gui.streams.StreamArea;
import toools.text.LineStreamListener;

public class OctojusStreamForwarder implements LineStreamListener
{
	private final StringBuilder buf = new StringBuilder();
	private StreamArea textArea;

	public OctojusStreamForwarder(StreamArea sa)
	{
		this.textArea = sa;
	}
	
	public StreamArea getTextArea()
	{
		return textArea;
	}

	@Override
	public void newLine(String line)
	{
		line.trim();
		buf.append(line);
		buf.append('\n');

		if (textArea != null)
		{
			textArea.appendText(line + '\n');
		}
	}
}
