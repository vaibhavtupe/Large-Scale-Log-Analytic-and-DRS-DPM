package Utility;

import java.util.ArrayList;
import java.util.List;



import com.vmware.vim25.ComputeResourceConfigSpec;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfEntityMetricCSV;
import com.vmware.vim25.PerfFormat;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.Permission;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class LoadBalanceUtility {
	
	
	public static float getHostUsage(HostSystem vHost) {

		PerformanceManager perfMan = Util.getPerfMgr();

		List<PerfQuerySpec> querySpecList = new ArrayList<PerfQuerySpec>();

		com.vmware.vim25.PerfQuerySpec newSpec = new PerfQuerySpec();
		newSpec.setEntity(vHost.getMOR());
		newSpec.setMaxSample(1);
		newSpec.setIntervalId(20);
		newSpec.setFormat(PerfFormat.csv.name());
		PerfMetricId[] metricId = new PerfMetricId[1];
		PerfMetricId metric = new PerfMetricId();
		metric.setCounterId(2);
		metric.setInstance("");
		metricId[0] = metric;
		newSpec.setMetricId(metricId);
		querySpecList.add(newSpec);

		float cpuUsagePercent = 0;

		PerfEntityMetricBase[] metricBase = null;
		try {
			metricBase = perfMan.queryPerf(querySpecList
					.toArray(new PerfQuerySpec[1]));

			PerfEntityMetricCSV perfEntityMetricCSV = null;

			perfEntityMetricCSV = (PerfEntityMetricCSV) metricBase[0];

			float hertz = ((vHost).getHardware().cpuInfo.getHz()) / 1000000;
			float cpuCores = (vHost).getHardware().cpuInfo
					.getNumCpuCores();

			float cpuUtilization = Long
					.parseLong((perfEntityMetricCSV.getValue()[0].value));
			//logger.debug(cpuUtilization+" "+hertz+ " "+cpuCores);
			cpuUsagePercent = (cpuUtilization / (hertz * cpuCores)) * 100;

			if(cpuCores < 4){
				cpuUsagePercent = cpuUsagePercent/2;
			}

		} catch (NullPointerException e){

		} catch (Exception e){
			e.printStackTrace();
		}

		return cpuUsagePercent;
	}

	public static float getVMUsage(VirtualMachine vm, HostSystem vHost) {

		float cpuUsagePercent = 0;

		try {

			float vHostHertz = ((vHost).getHardware().cpuInfo.getHz()) / 1000000;
			float vHostCpuCores = (vHost).getHardware().cpuInfo
					.getNumCpuCores();

			float cpuUtilization = vm.getSummary().getQuickStats().getOverallCpuDemand();
			cpuUsagePercent = (cpuUtilization / (vHostHertz * vHostCpuCores) * 100);
			//			logger.debug(vHostHertz+" "+vHostCpuCores+" "+cpuUtilization+" "+cpuUsagePercent+" "+(vHostHertz*vHostCpuCores));

		} catch (Exception e){
			e.printStackTrace();
		}

		return cpuUsagePercent;

	}
	
	
public static String  addNewVhost(){
	
	  System.out.println("----Adding New vHost in the Data Center---");
		
		String newHostIp="130.65.132.163";
		HostConnectSpec hcSpec = new HostConnectSpec();
		hcSpec.setHostName("130.65.132.163");
		hcSpec.setUserName("root");
		hcSpec.setPassword("12!@qwQW");
		hcSpec.setSslThumbprint("33:D1:6E:8A:17:35:BB:4C:76:BA:78:65:71:07:DC:88:B2:9F:16:C6");
		ComputeResourceConfigSpec compResSpec = new ComputeResourceConfigSpec();
		Task task  = null;
		try {
			
			Permission permission = new Permission();
			permission.setPropagate(true);
			permission.setEntity(Util.si.getMOR());

			task = ((Datacenter)Util.dcs[0]).getHostFolder().addStandaloneHost_Task(hcSpec, compResSpec, true);
			try {
				if(task.waitForMe() == Task.SUCCESS){
					System.out.println("-----New vHost Created Succesfully in the Data Center !!!------");
					return newHostIp;
				}
			} catch (Exception e) {
				System.out.println("Error in creating a new vHost2 : " + e);
			}
		} catch (Exception e) {
			System.out.println("Error in creating a new vHost : " + e);
		}

		return "";
	}




}
