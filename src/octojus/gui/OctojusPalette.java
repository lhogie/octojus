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

 
 package octojus.gui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import octojus.OctojusNode;
import toools.gui.ListColorPalette;

public class OctojusPalette extends ListColorPalette
{
	public static OctojusPalette defaultPalette = new OctojusPalette();
	
	public OctojusPalette()
	{
		getColorList().add(Color.blue);
		getColorList().add(Color.green);
		getColorList().add(Color.red);
		getColorList().add(Color.pink);
		getColorList().add(Color.yellow);
		getColorList().add(Color.black);
		getColorList().add(Color.cyan);
		getColorList().add(Color.magenta);
		getColorList().add(Color.gray);
	}

	private final Map<OctojusNode, Color> nodeColors = new HashMap<>();
	int counter = 0;

	public Color getColor(OctojusNode n)
	{
		Color c = nodeColors.get(n);

		if (c == null)
		{
			nodeColors.put(n, c = getColor(counter++));
		}

		return c;
	}

}
