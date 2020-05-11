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

package octojus.gui.streams;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import octojus.OctojusNode;
import octojus.OctojusStreamForwarder;
import octojus.gui.OctojusClusterMonitorPane;

public class StandardStreamsMonitor extends JPanel
{
	final JList<OctojusNode> list = new JList<>();
	final List<OctojusNode> nodes;
	Map<OctojusNode, StandardStreamsArea> areas = new HashMap<>();
	final JPanel textPanel = new JPanel();

	public StandardStreamsMonitor(OctojusClusterMonitorPane monitor)
	{
		nodes = new ArrayList<>(monitor.getCluster().getNodes());
		Collections.sort(nodes, new Comparator<OctojusNode>()
		{

			@Override
			public int compare(OctojusNode o1, OctojusNode o2)
			{
				return o1.getInetAddress().getHostName()
						.compareTo(o2.getInetAddress().getHostName());
			}
		});

		if (nodes.isEmpty())
			throw new IllegalStateException("cluster has zero active nodes");

		for (OctojusNode n : nodes)
		{
			StreamArea stdoutArea = new StreamArea(n.getInetAddress().getHostName(),
					" standard output");
			StreamArea stderrArea = new StreamArea(n.getInetAddress().getHostName(),
					" standard error");
			n.getStdoutLinesListeners().add(new OctojusStreamForwarder(stdoutArea));
			n.getStderrLinesListeners().add(new OctojusStreamForwarder(stderrArea));

			areas.put(n, new StandardStreamsArea(stdoutArea, stderrArea));
		}

		list.setListData(nodes.toArray(new OctojusNode[0]));
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.addListSelectionListener(new ListSelectionHandler());
		list.setSelectedIndex(0);
		setLayout(new BorderLayout());
		add(BorderLayout.WEST, new JScrollPane(list));
		add(BorderLayout.CENTER, textPanel);

	}

	class ListSelectionHandler implements ListSelectionListener
	{
		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			if ( ! e.getValueIsAdjusting())
			{
				textPanel.setLayout(new GridLayout(list.getSelectedIndices().length, 1));
				// textPanel.setVisible(false);
				textPanel.removeAll();

				for (int i : list.getSelectedIndices())
				{
					OctojusNode n = nodes.get(i);
					StandardStreamsArea a = areas.get(n);
					textPanel.add(a);
				}

				validate();
				repaint();// textPanel.setVisible(true);

			}
		}
	}

	private class StandardStreamsArea extends JPanel
	{
		final StreamArea stdoutSa, stderrSa;

		StandardStreamsArea(StreamArea stdout, StreamArea stderr)
		{
			this.stderrSa = stderr;
			this.stdoutSa = stdout;
			stderr.setForeground(Color.red);

			setLayout(new GridLayout(1, 2));
			add(stdoutSa);
			add(stderrSa);
		}
	}
}
