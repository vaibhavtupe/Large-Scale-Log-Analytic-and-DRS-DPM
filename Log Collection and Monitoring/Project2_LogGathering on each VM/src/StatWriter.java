import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.VirtualMachine;


public class StatWriter extends Thread {
	
	public String m_vmName = null;
	public String m_hostName = null;
	
	@Override
	public void run() {
		super.run();
		try {
			StatExtractor s = null;
			StatExtractor s1 = null;
			
			if(getVmName() != null){
				//get the vm stats
				StringBuffer strBuffer = new StringBuffer();

				VirtualMachine vm = (VirtualMachine) new InventoryNavigator(GatherStat.si.getRootFolder()).searchManagedEntity("VirtualMachine", getVmName());
				if(vm != null){
					System.out.println("VM Name: " + vm.getName());
					s = new StatExtractor(vm);
					
					if(s.str.toString() != null){
						strBuffer.append(s.str.toString());
					} 
				}else{
					System.out.println("VM Not Found");
				}
				//now check if the vhost is not nul
				if(getHostName() != null){
					HostSystem currentHost = getHost(getVmName());
					if(currentHost != null){
						//get the host stats
						System.out.println("Host Name: " + currentHost.getName());
						s1 = new StatExtractor(currentHost);
						if (s1.str.toString() != null){
							strBuffer.append(s1.str.toString());
						}
					}
				}
				System.out.println("OUT:" +strBuffer.toString());
				
				
				System.out.println(Util.LOG_STORAGE_PATH + getVmName() + "-log.txt");
				PrintStream out = new PrintStream(new FileOutputStream(
						Util.LOG_STORAGE_PATH + getVmName() + "-log.txt", true));
				if(out != null){
					out.append(strBuffer.toString());
					populateTextFile("", strBuffer.toString());
				}
			}
		} catch (Exception e) {
			System.out.println("Error in the generate logs class: " + e.getMessage());
		} 
	}
	
	public void populateTextFile(String passFname, String passContent) {
		boolean result = false;
		try {
            FileWriter writer = new FileWriter(Util.TEMPLOG_STORAGE_PATH  + getVmName() + "-temp.txt" , false);
            BufferedWriter bufferedWriter = new BufferedWriter(writer); 
            bufferedWriter.write(passContent);
            bufferedWriter.newLine();
            bufferedWriter.close();
            //writer.write(passContent);
            //writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	
	public HostSystem getHost(String vmName){
		HostSystem vmHost = null;
		try {
			ManagedEntity[] hosts = new InventoryNavigator(
					GatherStat.si.getRootFolder()).searchManagedEntities("HostSystem");
			for (int i = 0; i < hosts.length; i++) {
				// System.out.println("host["+i+"]=" + hosts[i].getName());
				HostSystem h = (HostSystem) hosts[i];
				VirtualMachine vms[] = h.getVms();
				for (int p = 0; p < vms.length; p++) {
					VirtualMachine v = (VirtualMachine) vms[p];
					System.out.println(v.getName().toLowerCase() + " "
							+ vmName.toLowerCase());
					if ((v.getName().toLowerCase()).equals(vmName
							.toLowerCase())) {
						vmHost = (HostSystem) hosts[i];
						break;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error in get Statistics:" + e.getMessage());
		}
		return vmHost;
	}

	public String getVmName() {
		return m_vmName;
	}

	public void setVmName(String m_vmName) {
		this.m_vmName = m_vmName;
	}

	public String getHostName() {
		return m_hostName;
	}

	public void setHostName(String m_hostName) {
		this.m_hostName = m_hostName;
	}

}
