package Utility;






import com.vmware.vim25.HostVMotionCompatibility;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfEntityMetricCSV;
import com.vmware.vim25.PerfFormat;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;

import java.net.InetAddress;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;



import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

import config.Project2_Config;

public class Util {

	//Global variables
	public  static ServiceInstance si;
	public static ServiceInstance vCenterManagerSi;
	public static Folder rootFolder;
	public static Folder vCenterManagerRootFolder;
	public  static ManagedEntity[] dcs;
	public  static ManagedEntity[] hosts;
	public  static ManagedEntity[] vms;
	public static PerformanceManager perfMgr;

	public Util(){

		try{

			si = new ServiceInstance(new URL(Project2_Config.getVCenterURL()), Project2_Config.getVCenterUsername(), 
					Project2_Config.getVCenterPassword(), true);

			vCenterManagerSi= new ServiceInstance(new URL("https://130.65.132.14/sdk"), Project2_Config.getVCenterUsername(), 
					Project2_Config.getVCenterPassword(), true);

			rootFolder = si.getRootFolder();
			vCenterManagerRootFolder=vCenterManagerSi.getRootFolder();

			dcs = new InventoryNavigator(rootFolder).searchManagedEntities(
					new String[][] { {"Datacenter", "name" }, }, true);

			hosts = new InventoryNavigator(rootFolder).searchManagedEntities(
					new String[][] { {"HostSystem", "name" }, }, true);

			vms = new InventoryNavigator(rootFolder).searchManagedEntities(
					new String[][] { {"VirtualMachine", "name" }, }, true);
			
			perfMgr = si.getPerformanceManager();
		}
		catch (Exception e){
			System.out.println("VMMonitor object initialization eroor : " + e);

		}	
	}

	// pinging the given ip to check whether it is reachable

	public static boolean ping(String ip) throws Exception {
		String cmd = "";

		if (System.getProperty("os.name").startsWith("Windows")) {
			// For Windows
			cmd = "ping -n 3 " + ip;
		} else {
			// For Linux and OSX
			cmd = "ping -c 3 " + ip;
		}

		System.out.println("Ping "+ ip + "......");
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();		
		return process.exitValue() == 0;
	}


	//to get vHost name in Vcenter Manager

	public static String getHostInVcenter(String host){

		return VHOSTMAP.get(host);
	}

	// stored mapping of vHost Names in Vcenter Manager

	public static final HashMap<String, String> VHOSTMAP = new HashMap<String, String>() {
		{
			put("130.65.132.161", "T04-vHost01-cum1_IP=.132.161");
			put("130.65.132.162", "T04-vHost02-cum1_IP=.132.162");
			put("130.65.132.163", "T04-vHost03-cum1");
			put("130.65.132.164","T04-vHost04-cum1");
		}
	};
	
	
	public static void displayInventory() {
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

			for(int m=0; m<Util.vms.length; m++) {
				VirtualMachine vm = (VirtualMachine) Util.vms[m];
				VirtualMachineConfigInfo vminfo = vm.getConfig();
				VirtualMachinePowerState vmps = vm.getRuntime().getPowerState();
				vm.getResourcePool();
				System.out.println("---------------------------------------------------");
				System.out.println("Virtual Machine " + (m+1));
				System.out.println("VM Name: " + vm.getName());
			
				System.out.println("---------------------------------------------------");
				
				
			}
		}catch (Exception e){

		}
	}

	public static PerformanceManager getPerfMgr() {
		return perfMgr;
	}

	public static void setPerfMgr(PerformanceManager perfMgr) {
		Util.perfMgr = perfMgr;
	}
	
	public static HostSystem getvHost(String hostName){
		
		HostSystem vHost=null;
		
		try {
			vHost= (HostSystem) new InventoryNavigator(
				      rootFolder).searchManagedEntity("HostSystem", hostName);
		} catch (InvalidProperty e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	return vHost;	
	}
	
	
	public static void snapshot(VirtualMachine vm) {

		
		try {

			if(vm.getCurrentSnapShot() != null)
				vm.getCurrentSnapShot().removeSnapshot_Task(true);

			com.vmware.vim25.mo.Task snapshotTask = vm.createSnapshot_Task(vm.getName()+"_snapshot", new Date().toString(), false, false);
			String snapshotTaskResult = snapshotTask.waitForTask();
			if(!snapshotTaskResult.equalsIgnoreCase("Success"))
			{
				
				return;
			}


		} catch (Exception e) {
			
			e.printStackTrace();

		} 
		
	}
	
	public static boolean migrateToAnotherHost(VirtualMachine vm, String hostIp){

		try{
			

			HostSystem newHost = (HostSystem) new InventoryNavigator(
					Util.rootFolder).searchManagedEntity(
							"HostSystem", hostIp);
			ComputeResource cr = (ComputeResource) newHost.getParent();

			String[] checks = new String[] {"cpu", "software"};
			HostVMotionCompatibility[] vmcs =
					Util.si.queryVMotionCompatibility(vm, new HostSystem[] 
							{newHost},checks );

			String[] comps = vmcs[0].getCompatibility();
			if(checks.length != comps.length)
			{
				System.out.println("CPU/software NOT compatible. Exit.");
				//si.getServerConnection().logout();
				//return;
			}

			Task task = vm.migrateVM_Task(cr.getResourcePool(), newHost,
					VirtualMachineMovePriority.highPriority, 
					VirtualMachinePowerState.poweredOn);


			if(task.waitForTask()==Task.SUCCESS)
			{
				System.out.println("Migration Done!");
				return true;
			}
			else
			{
				System.out.println(" Migration Failed!");
				TaskInfo info = task.getTaskInfo();
				System.out.println(info.getError().getFault());
				return false;
			}
		}
		catch (Exception e){

			System.out.println("Error while doing cold migration : "+ e);
		}  
		return false;
	}


	
	
	
}
