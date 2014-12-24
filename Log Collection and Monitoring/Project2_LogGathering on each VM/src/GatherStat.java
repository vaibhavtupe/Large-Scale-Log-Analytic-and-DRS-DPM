import java.io.IOException;
import java.net.URL;

import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;


public class GatherStat {
	public static ServiceInstance si;
	public static HostSystem vmHost = null;
	public static void main(String[] args) throws IOException,
	InterruptedException {
		/* vmName = "T04_VM02_Ubuntu32"; */
		VirtualMachine vm;
		String hostName = "130.65.132.162";
		String vmName = "T04-VM01-Ubuntu32";
		

		try {
			si = new ServiceInstance(new URL(Util.VCENTER_URL), Util.VCENTER_USERNAME, Util.VCENTER_PASSWORD, true);

			/*
			if(args.length < 2){
				if(args[0] != "" && args[0].length() > 0 && args[0] != null){
					vmName = args[0];
				}
			}
			if(args.length == 2){
				if(args[0] != "" && args[0].length() > 0 && args[0] != null){
					vmName = args[0];
				}

				if(args[1] != "" && args[1].length() > 0 && args[1] != null){
					hostName = args[1];
				}	
			}*/
			while(true){
				StatWriter writeStat = new StatWriter();
				writeStat.setVmName(vmName);
				writeStat.setHostName(hostName);
				writeStat.start();
				writeStat.join();
	 
			}


		}
		catch (Exception e) {
			System.out.println("Error in getStatistics" + e.getMessage());
		}
	}


}
