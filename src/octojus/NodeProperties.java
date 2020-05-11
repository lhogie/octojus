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

import java.util.ArrayList;
import java.util.List;

import oscilloscup.multiscup.Property;

/*
 * 		properties.add(
				new NodeProperty("hostname", getPublicInetAddress().getHostName(), null));
		properties.add(new NodeProperty("abs loadavg",
				getDiscoveredInfo().getLoadAverage(), null));
		properties.add(new NodeProperty("#CPU",
				getDiscoveredInfo().getNumberOfProcessors(), null));
		properties.add(new NodeProperty("#jobs processed",
				getDiscoveredInfo().getNumberOfJobsAlreadyProcessed(), null));
		properties.add(new NodeProperty("#jobs in queue",
				getDiscoveredInfo().getNumberOfJobsInQueue(), null));
		properties.add(new NodeProperty("RAM size",
				MathsUtilities.round(
						getDiscoveredInfo().getMemoryAvailableInBytes() / 1000000000d, 1),
				"Gb"));
		properties.add(new NodeProperty("utilization",
				getDiscoveredInfo().getLoadAverage(), null));
		properties.add(new NodeProperty("#NIC",
				getDiscoveredInfo().localHardwareAddresses.size(), null));
		properties.add(new NodeProperty("rel loadavg",
				MathsUtilities.round((100d * getDiscoveredInfo().getLoadAverage())
						/ getDiscoveredInfo().getNumberOfProcessors(), 2),
				"%"));
		properties.add(new NodeProperty("command line", getDiscoveredInfo().cmd, null));

		for (TYPE p : TYPE.values())
		{
			properties.add(new NodeProperty("Perf: " + p.name(),
					getDiscoveredInfo().getPerformance()[p.ordinal()], null));
		}

 */

public class NodeProperties
{

	public static List<Property<OctojusNode>> getOSProperties()
	{
		List<Property<OctojusNode>> props = new ArrayList<>();

		Property.addProperty(props, new Property<OctojusNode>("load avg", null)
		{

			@Override
			public Object getRawValue(OctojusNode n)
			{
				return n.getDiscoveredInfo().loadAverage;
			}

		});

		return props;
	}

	public static List<Property<OctojusNode>> getJVMProperties()
	{
		List<Property<OctojusNode>> props = new ArrayList<>();

		Property.addProperty(props, new Property<OctojusNode>("jvmProcessSize", null)
		{
			@Override
			public Object getRawValue(OctojusNode n)
			{
				return n.getDiscoveredInfo().jvmProcessSize;
			}
		});

		return props;
	}

	public static List<Property<OctojusNode>> getJobsProperties()
	{
		List<Property<OctojusNode>> props = new ArrayList<>();

		Property.addProperty(props,
				new Property<OctojusNode>("numberOfJobsAlreadyProcessed", null)
				{

					@Override
					public Object getRawValue(OctojusNode n)
					{
						return n.getDiscoveredInfo().numberOfJobsAlreadyProcessed;
					}

				});

		return props;
	}
}
