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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import octojus.NodeProperties;
import octojus.OctojusCluster;
import octojus.OctojusNode;
import octojus.gui.metrics.ClusterMonitorPane;
import octojus.gui.streams.StandardStreamsMonitor;
import oscilloscup.multiscup.Property;
import toools.gui.Utilities;
import toools.text.TextUtilities;

public class OctojusClusterMonitorPane extends JPanel
{
	private final OctojusCluster cluster;
	private final JTabbedPane tabbedPane = new JTabbedPane();

	public OctojusClusterMonitorPane(OctojusCluster cluster)
	{
		System.out.println("Starting monitor");

		this.cluster = cluster;

		JLabel descriptionLabel = new JLabel();

		addClusterMonitorPane(NodeProperties.getOSProperties(), "OS");
		addClusterMonitorPane(NodeProperties.getJVMProperties(), "JVM");
		addClusterMonitorPane(NodeProperties.getJobsProperties(), "OS");
		addMonitoringPane(new StandardStreamsMonitor(this), "Std I/O");

		setLayout(new BorderLayout());
		add(BorderLayout.NORTH, descriptionLabel);
		add(BorderLayout.CENTER, tabbedPane);

		descriptionLabel.setText("<html>Cluster"
				+ (cluster.getName() == null ? "" : " " + cluster.getName())
				+ " has <ul><li>" + cluster.getNumberOfProcessors() + " cores <li> "
				+ TextUtilities.toHumanString(cluster.computeTotalMemoryUsedByJVMs())
				+ "b of memory<li>" + cluster.getNodes().size() + " nodes");

	}

	public OctojusCluster getCluster()
	{
		return cluster;
	}

	@Override
	public Dimension getPreferredSize()
	{
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension d = Utilities.scale(sd, 0.8);
		return d;
	}

	public void addMonitoringPane(JPanel p, String title)
	{
		tabbedPane.addTab(title, p);
	}

	private void addClusterMonitorPane(List<Property<OctojusNode>> props, String title)
	{
		tabbedPane.addTab(title, new ClusterMonitorPane(cluster, props));
	}
}
