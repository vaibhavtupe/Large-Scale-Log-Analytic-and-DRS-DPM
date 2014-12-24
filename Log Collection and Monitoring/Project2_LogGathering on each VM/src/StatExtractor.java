import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;

import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfEntityMetricCSV;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricSeriesCSV;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;


public class StatExtractor {
	// public static HostSystem hm;
		public static int FOR_VM = 0;
		public static int FOR_HOST = 1;
		
		public ManagedEntity vm;
		public String[] PerfCounters = { "cpu.usage.average",
			"mem.usage.average", "net.usage.average", "disk.usage.average" };
		private PerformanceManager perfMgr;
		private HashMap<Integer, PerfCounterInfo> countersInfoMap;
		private HashMap<String, Integer> countersMap;
		private PerfMetricId[] pmis;
		public  StringBuffer str;
		public String currentLog;

		StatExtractor(ManagedEntity vm) throws RemoteException, IOException {
			this.vm = vm;
			continueProgram();
		}

		public void continueProgram() {
			perfMgr = GatherStat.si.getPerformanceManager();
			PerfCounterInfo[] pcis = perfMgr.getPerfCounter();

			// create map between counter ID and PerfCounterInfo, counter name and
			// ID
			countersMap = new HashMap<String, Integer>();
			countersInfoMap = new HashMap<Integer, PerfCounterInfo>();

			for (int i = 0; i < pcis.length; i++) {
				countersInfoMap.put(pcis[i].getKey(), pcis[i]);
				countersMap.put(pcis[i].getGroupInfo().getKey() + "."
								+ pcis[i].getNameInfo().getKey() + "."
								+ pcis[i].getRollupType(), pcis[i].getKey());
			}

			pmis = createPerfMetricId(PerfCounters);
			System.out.println("Performance manager is set up.");

			try {
				printPerf(vm);
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private PerfMetricId[] createPerfMetricId(String[] counters) {
			PerfMetricId[] metricIds = new PerfMetricId[counters.length];
			for (int i = 0; i < counters.length; i++) {
				PerfMetricId metricId = new PerfMetricId();
				metricId.setCounterId(countersMap.get(counters[i]));
				metricId.setInstance("*");
				metricIds[i] = metricId;
			}
			return metricIds;
		}

		public void printPerf(ManagedEntity me) throws Exception {
			PerfProviderSummary pps = perfMgr.queryPerfProviderSummary(me);
			int refreshRate = pps.getRefreshRate().intValue();
			// only return the latest one sample
			PerfQuerySpec qSpec = createPerfQuerySpec(me, 1, refreshRate);

			PerfEntityMetricBase[] pValues = perfMgr
					.queryPerf(new PerfQuerySpec[] { qSpec });
			if (pValues != null) {
				displayValues(pValues);
			}
		}

		private void displayValues(PerfEntityMetricBase[] values) throws FileNotFoundException {
			for (int i = 0; i < values.length; ++i) {
				printPerfMetricCSV((PerfEntityMetricCSV) values[i]);
			}
		}

		private void printPerfMetricCSV(PerfEntityMetricCSV pem)
				throws FileNotFoundException {

			try {
				PerfMetricSeriesCSV[] csvs = pem.getValue();
				HashMap<Integer, PerfMetricSeriesCSV> stats = new HashMap<Integer, PerfMetricSeriesCSV>();

				for (int i = 0; i < csvs.length; i++) {
					stats.put(csvs[i].getId().getCounterId(), csvs[i]);
				}
				str = new StringBuffer();
				str.append(vm.getName()).append(",");
				str.append(pem.getSampleInfoCSV()).append(",");
				for (String counter : PerfCounters) {
					Integer counterId = countersMap.get(counter);
					PerfCounterInfo pci = countersInfoMap.get(counterId);
					String value = null;
					System.out.println("Counter id: " + counterId);
					if (stats.containsKey(counterId))
						value = stats.get(counterId).getValue();
					if (value == null ||Integer.parseInt(value)<0 || value.length() == 0) {
						value = "0";
					}
					// str.append(pci.getGroupInfo().getKey() + "."
					 //+ pci.getNameInfo().getKey() + "." +
				 //pci.getRollupType()).append("=");
					//str.append(",");
					 
					str.append(value).append(",");
				}
				str.deleteCharAt(str.length()-1);
				str.append(System.getProperty("line.separator"));
				if (str.toString() != null)
					setCurrentLog(str.toString());
			} catch (Exception e) {
				//System.out.println("error in print perf " + e.getMessage());
			}
		}

		synchronized private PerfQuerySpec createPerfQuerySpec(
				ManagedEntity me, int maxSample, int interval) {

			PerfQuerySpec qSpec = new PerfQuerySpec();
			qSpec.setEntity(me.getMOR());
			// set the maximum of metrics to be return
			qSpec.setMaxSample(new Integer(maxSample));
			qSpec.setMetricId(pmis);
			qSpec.setFormat("csv");
			qSpec.setIntervalId(new Integer(interval));
			return qSpec;
		}

		public String getCurrentLog() {
			return currentLog;
		}

		public void setCurrentLog(String currentLog) {
			currentLog = currentLog;
		}

}
