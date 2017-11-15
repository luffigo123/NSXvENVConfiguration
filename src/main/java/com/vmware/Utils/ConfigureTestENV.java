package com.vmware.Utils;

import org.apache.log4j.Logger;

import com.vmware.AutoInfraVC.ClusterOperation;
import com.vmware.AutoInfraVC.DVSOperation;
import com.vmware.AutoInfraVC.DatacenterOperation;
import com.vmware.AutoInfraVC.HostUtils;
import com.vmware.AutoInfraVC.VC;
import com.vmware.AutoInfraVC.VMOperation;
import com.vmware.Utils.DatastoreUtils;
import com.vmware.Utils.DefaultEnvironment;
import com.vmware.Utils.Log4jInstance;
import com.vmware.vc.ManagedObjectReference;

public class ConfigureTestENV {
	private Logger log = Log4jInstance.getLoggerInstance();
	private String inputLanguage = null;
	private TestData testData = null;
	
	
	public ConfigureTestENV() {
		super();
		inputLanguage = DefaultEnvironment.inputLanguage;
		testData = new TestData(inputLanguage);
	}

	/**
	 * Configure the locale testing environment, such as create DC, CLuster, add ESXi host, porGroups 
	 * @return
	 */
	public boolean setupTestEnv(){
		try{
			String vc_IP = DefaultEnvironment.vcIP;
			String esx1_IP = DefaultEnvironment.esxiHost01_IPAddress;
//			String esx2_IP = DefaultEnvironment.esxiHost02_IPAddress;
			
			String temp_DataCenter_Name = testData.DatacenterName;
			String vcUserName = DefaultEnvironment.vcUserName;
			String vcPassword = DefaultEnvironment.vcPasswd;
			
			String esxiUsername = DefaultEnvironment.esxiUserName;
			String esxiPasswd = DefaultEnvironment.esxiPassword;
			
			if(esxiPasswd == null){
				esxiPasswd = "";
			}
			
			log.info("Connect to VC, and initial the DVs, Datacenter, VM, Host, Cluster, Datastore info!");
			VC vc = new VC(vc_IP, "443", vcUserName, vcPassword);	
			DVSOperation dvsOps = new DVSOperation(vc);
			DatacenterOperation dcOps = new DatacenterOperation(vc);
			VMOperation vmOps = new VMOperation(vc);
			HostUtils hostOps = new HostUtils(vc);
			ClusterOperation cluOps = new ClusterOperation(vc);
			DatastoreUtils datastoreUtils = new DatastoreUtils(vc);
			
			log.info("Create Datacenter, the name is - " + temp_DataCenter_Name);
			dcOps.createDatacenter(temp_DataCenter_Name);
			
			log.info("Add the vESX hosts to the Datacenter");
			ManagedObjectReference hostMor = hostOps.addStandaloneHost2(esx1_IP, esxiUsername, esxiPasswd, testData.DatacenterName);
//			hostOps.addStandaloneHost(esx2_IP, esxiUsername, esxiPasswd, testData.DatacenterName);


			log.info("Create clusters, names are - cluster1: " + testData.Cluster1);
			cluOps.createDefaultCluster(testData.DatacenterName, testData.Cluster1);
//			cluOps.createDefaultCluster(testData.DatacenterName, testData.Cluster2);

			log.info("Move the ESXi_01 to cluster1");
			cluOps.moveHostToCluster(testData.Cluster1, esx1_IP);
//			cluOps.moveHostToCluster(testData.Cluster2, esx2_IP);

			log.info("Enable Promious Mode for the 2 ESXi hosts");
			hostOps.enableVswitchPromiscuousMode(esx1_IP, "vSwitch0");
//			hostOps.enableVswitchPromiscuousMode(esx2_IP, "vSwitch0");
			
			log.info("Add 2 Portgroup to ESXi_01, the first one is - " + testData.PG_Internal01 + ", the second one is - " + testData.PG_Uplink01);
			hostOps.addPortGroup(esx1_IP, "vSwitch0", testData.PG_Internal01);
			hostOps.addPortGroup(esx1_IP, "vSwitch0", testData.PG_Uplink01);

			log.info("Connect VM2 to the portgroup internal");
			if(vmOps.reconfigVMToUsePortGroup(testData.VM2, testData.PG_Internal01))
			{
				log.info(inputLanguage + ": Failed to Connect VM2 to the portgroup - internal");
//				return false;
			}
			
			log.info("Change the largest datastore of ESXi_01's name");
			String destDatastore = datastoreUtils.getDatastoreName(esx1_IP);
			ManagedObjectReference datastoreMor = vc.datastore.getDatastore(hostMor, destDatastore);
			vc.datastore.renameDatastore(datastoreMor, testData.DataStore1);
			
			log.info("Create DVS for datacenter");
			if(!dvsOps.createDVSWithHost(testData.dvSwitchName, 4, esx1_IP))
			{
				log.info(inputLanguage + ": Failed to add dvs on esx1");
//				return false;
			}
//			log.info("Add host2 to the DVS");
//			dvsOps.addHostToDVS(testData.DatacenterName, testData.dvSwitchName, esx2_IP);
			
			log.info("Add dvPortgroup on dvs");
			String s1 = dvsOps.addPortGroup(testData.DatacenterName, testData.dvSwitchName, null, 4, testData.dvPortGroupName);
			if(s1 == null)
			{
				log.info(inputLanguage + ": Failed to add dvPortgroup on dvs");
//				return false;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}
	
}
