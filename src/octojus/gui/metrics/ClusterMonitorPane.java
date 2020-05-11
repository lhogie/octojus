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

package octojus.gui.metrics;

import java.util.ArrayList;
import java.util.List;

import jacaboo.Cluster;
import octojus.OctojusNode;
import oscilloscup.data.rendering.DataElementRenderer;
import oscilloscup.multiscup.Multiscope;
import oscilloscup.multiscup.Property;

public class ClusterMonitorPane extends Multiscope<OctojusNode>
{

	public ClusterMonitorPane(Cluster<OctojusNode> c, List<Property<OctojusNode>> props)
	{
		super(props);
		setRows(new ArrayList<>(c.getNodes()));
		setRefreshPeriodMs(1000);
	}


	@Override
	protected String getRowNameFor(OctojusNode n)
	{
		return n.getInetAddress().getHostName();
	}

	@Override
	protected int getNbPointsInSlidingWindow(OctojusNode row, Property<OctojusNode> p)
	{
		return 20;
	}


	@Override
	protected DataElementRenderer getSpecificRenderer(OctojusNode row,
			Property<OctojusNode> property)
	{
		return null;
	}
}
