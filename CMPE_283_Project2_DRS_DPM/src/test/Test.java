package test;

import com.vmware.vim25.ElementDescription;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfEntityMetricCSV;
import com.vmware.vim25.PerfInterval;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfMetricSeriesCSV;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PerfSampleInfo;
import com.vmware.vim25.PerformanceDescription;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.VirtualMachine;

import Utility.Util;

public class Test {
	
	public static PerfProviderSummary pps;
	public static int refreshRate;
	public static PerfMetricId[] pmis;
	public static PerfQuerySpec qSpec;
	public static PerfEntityMetricBase[] pValues;
	
	
	public static void main(String args[]){
		
		new Util();
		
		
		 
		
		//displayStatics();
		
		
		Util.displayInventory();
		
		
		
		
		
		
		
		
	}
	
	public static void displayStatics() {
		try{

			if(Util.hosts==null || Util.hosts.length==0) {
				return;
			}

			System.out.println("*****************vHost List*************************");
			for(int h=0; h<Util.hosts.length; h++) {
				System.out.println("Host IP " + (h+1) + ": "+ Util.hosts[h].getName());
			}

			System.out.println("***************************************************");

			System.out.println("********************VM List************************");

			for(int m=0; m<Util.vms.length-1; m++) {
				VirtualMachine vm = (VirtualMachine) Util.vms[m];
				VirtualMachineConfigInfo vminfo = vm.getConfig();
				VirtualMachinePowerState vmps = vm.getRuntime().getPowerState();
				vm.getResourcePool();
				System.out.println("---------------------------------------------------");
			/*	System.out.println("Virtual Machine " + (m+1));
				System.out.println("VM Name: " + vm.getName());
				System.out.println("VM OS: " + vminfo.getGuestFullName());
				System.out.println("VM CPU Number: " + vm.getConfig().getHardware().numCPU);
				System.out.println("VM Memory: " + vm.getConfig().getHardware().memoryMB);
				System.out.println("VM Power State: " + vmps.name());
				System.out.println("VM Running State: " + vm.getGuest().guestState);
				System.out.println("VM IP: " + vm.getGuest().getIpAddress());
				System.out.println("VM CPU: " + vm.getConfig().getHardware().getNumCPU());
				System.out.println("VM Memory: " + vm.getConfig().getHardware().getMemoryMB());
				System.out.println("VM VMTools: " + vm.getGuest().toolsRunningStatus);
				System.out.println("---------------------------------------------------");
				
				*/
				 // find out the refresh rate for the virtual machine
			     pps = Util.perfMgr.queryPerfProviderSummary(Util.vms[m]);
			    refreshRate = pps.getRefreshRate().intValue();
			  

			    // retrieve all the available perf metrics for vm
			     pmis = Util.perfMgr.queryAvailablePerfMetric(
			        vm, null, null, refreshRate);

			    PerfQuerySpec qSpec = createPerfQuerySpec(
			        vm, pmis, 3, refreshRate);
			    
			    
			   // pValues= Util.perfMgr.queryPerf();

			    while(true) 
			    {
			       pValues = Util.perfMgr.queryPerf(
			        new PerfQuerySpec[] {qSpec});
			      if(pValues != null)
			      {
			        displayValues(pValues);
			      }
			      System.out.println("Sleeping 60 seconds...");
			      Thread.sleep(refreshRate*3*1000);
			    }
				
			}
		}catch (Exception e){

		}
	}
	

	  static PerfQuerySpec createPerfQuerySpec(ManagedEntity me, 
	      PerfMetricId[] metricIds, int maxSample, int interval)
	  {
	    PerfQuerySpec qSpec = new PerfQuerySpec();
	    qSpec.setEntity(me.getMOR());
	    // set the maximum of metrics to be return
	    // only appropriate in real-time performance collecting
	    qSpec.setMaxSample(new Integer(maxSample));
//	    qSpec.setMetricId(metricIds);
	    // optionally you can set format as "normal"
	    qSpec.setFormat("csv");
	    // set the interval to the refresh rate for the entity
	    qSpec.setIntervalId(new Integer(interval));
	 
	    return qSpec;
	  }

	  static void displayValues(PerfEntityMetricBase[] values)
	  {
	    for(int i=0; i<values.length; ++i) 
	    {
	      String entityDesc = values[i].getEntity().getType() 
	          + ":" + values[i].getEntity().get_value();
	      System.out.println("Entity:" + entityDesc);
	      if(values[i] instanceof PerfEntityMetric)
	      {
	        printPerfMetric((PerfEntityMetric)values[i]);
	      }
	      else if(values[i] instanceof PerfEntityMetricCSV)
	      {
	        printPerfMetricCSV((PerfEntityMetricCSV)values[i]);
	      }
	      else
	      {
	        System.out.println("UnExpected sub-type of " +
	        		"PerfEntityMetricBase.");
	      }
	    }
	  }
	  
	  static void printPerfMetric(PerfEntityMetric pem)
	  {
	    PerfMetricSeries[] vals = pem.getValue();
	    PerfSampleInfo[]  infos = pem.getSampleInfo();
	    
	    System.out.println("Sampling Times and Intervales:");
	    for(int i=0; infos!=null && i <infos.length; i++)
	    {
	      System.out.println("Sample time: " 
	          + infos[i].getTimestamp().getTime());
	      System.out.println("Sample interval (sec):" 
	          + infos[i].getInterval());
	    }
	    System.out.println("Sample values:");
	    for(int j=0; vals!=null && j<vals.length; ++j)
	    {
	      System.out.println("Perf counter ID:" 
	          + vals[j].getId().getCounterId());
	      System.out.println("Device instance ID:" 
	          + vals[j].getId().getInstance());
	      
	      if(vals[j] instanceof PerfMetricIntSeries)
	      {
	        PerfMetricIntSeries val = (PerfMetricIntSeries) vals[j];
	        long[] longs = val.getValue();
	        for(int k=0; k<longs.length; k++) 
	        {
	          System.out.print(longs[k] + " ");
	        }
	        System.out.println("Total:"+longs.length);
	      }
	      else if(vals[j] instanceof PerfMetricSeriesCSV)
	      { // it is not likely coming here...
	        PerfMetricSeriesCSV val = (PerfMetricSeriesCSV) vals[j];
	        System.out.println("CSV value:" + val.getValue());
	      }
	    }
	  }
	    
	  static void printPerfMetricCSV(PerfEntityMetricCSV pems)
	  {
	    System.out.println("SampleInfoCSV:" 
	        + pems.getSampleInfoCSV());
	    PerfMetricSeriesCSV[] csvs = pems.getValue();
	    for(int i=0; i<csvs.length; i++)
	    {
	      System.out.println("PerfCounterId:" 
	          + csvs[i].getId().getCounterId());
	      System.out.println("CSV sample values:" 
	          + csvs[i].getValue());
	    }
	  }
	
	
	
}
