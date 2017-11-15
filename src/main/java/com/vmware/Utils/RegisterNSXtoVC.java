package com.vmware.Utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import com.vmware.Utils.DefaultEnvironment;
import com.vmware.Utils.PropertiesUtils;


public class RegisterNSXtoVC {
//	private PropertiesUtils pu = null;
	private String vc_IP;
	private String vsm_IP;
	private String vcRootName;
	private String vcRootPassword;
	private String vcUserName;
	private String vcPassword;

	private NSXRelateVCUtils nsxRelateVCUtils;
	
	
	public RegisterNSXtoVC() {
		super();
//		pu = new PropertiesUtils("tempResult.properties");
//		this.vc_IP = pu.getValueByKey("vcIP");
//		this.vsm_IP = pu.getValueByKey("vsm_IP");
		
		this.vc_IP = DefaultEnvironment.vcIP;
		this.vsm_IP = DefaultEnvironment.vsmIP;

		vcRootName = DefaultEnvironment.vcRootName;
		vcRootPassword = DefaultEnvironment.vcRootPassword;
		
		vcUserName = DefaultEnvironment.vcUserName;
		this.vcPassword = DefaultEnvironment.vcPasswd;
		
		nsxRelateVCUtils = new NSXRelateVCUtils();
	}

	public boolean registerNSXtoVC(){
		boolean result = false;
		String vcFingerprint = "";
		try {
			vcFingerprint = nsxRelateVCUtils.getVCThumbprint(vc_IP, vcRootName, vcRootPassword);
		} catch (InterruptedException | ExecutionException | TimeoutException e1) {
			e1.printStackTrace();
		}
		try {
			VSMManagement vmsMrg = new VSMManagement();

			result = vmsMrg.registerNSXtoVC(vc_IP, this.vcUserName, this.vcPassword, vsm_IP, vcFingerprint);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
}
