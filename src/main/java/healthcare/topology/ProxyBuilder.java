package healthcare.topology;

import healthcare.utils.Constants;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * ProxyBuilder.java
 * Constructs the proxy server FogDevice that sits between cloud and fog nodes.
 */
public class ProxyBuilder {

    public FogDevice buildProxy(int cloudId) throws Exception {
        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < Constants.PROXY_NUM_PES; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(Constants.PROXY_MIPS)));
        }

        Host host = new Host(
            0,
            new RamProvisionerSimple(Constants.PROXY_RAM),
            new BwProvisionerSimple(Constants.PROXY_UPLINK_BW),
            500_000L,
            peList,
            new VmSchedulerTimeShared(peList)
        );

        List<Host> hostList = new ArrayList<>();
        hostList.add(host);

        FogDeviceCharacteristics chars = new FogDeviceCharacteristics(
            "x86", "Linux", "Xen", host, 10.0,
            0.005, 0.02, 0.001, 0.0);

        FogDevice proxy = new FogDevice(
            "Proxy",
            chars,
            new VmAllocationPolicySimple(hostList),
            new LinkedList<>(),
            10,
            Constants.PROXY_UPLINK_BW,
            Constants.PROXY_DOWNLINK_BW,
            100,                // uplink latency to cloud (ms)
            0.005
        );

        proxy.setParentId(cloudId);
        proxy.setUplinkLatency(100);
        return proxy;
    }
}
