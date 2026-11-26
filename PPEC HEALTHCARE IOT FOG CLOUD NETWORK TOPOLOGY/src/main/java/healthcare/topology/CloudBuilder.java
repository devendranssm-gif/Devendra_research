package healthcare.topology;

import healthcare.utils.Constants;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.utils.FogLinearPowerModel;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * CloudBuilder.java
 * Constructs the cloud data-centre FogDevice for the PPEC topology.
 */
public class CloudBuilder {

    /**
     * Create and return the cloud FogDevice.
     *
     * @param userId   simulation user (broker) ID
     * @return configured cloud FogDevice
     */
    public FogDevice buildCloud(int userId) throws Exception {
        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < Constants.CLOUD_NUM_PES; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(Constants.CLOUD_MIPS)));
        }

        int    hostId  = 0;
        long   storage = 1_000_000;

        Host host = new Host(
            hostId,
            new RamProvisionerSimple(Constants.CLOUD_RAM),
            new BwProvisionerSimple(Constants.CLOUD_UPLINK_BW),
            storage,
            peList,
            new VmSchedulerTimeShared(peList)
        );

        List<Host> hostList = new ArrayList<>();
        hostList.add(host);

        String arch        = "x86";
        String os          = "Linux";
        String vmm         = "Xen";
        double timeZone    = 10.0;
        double costPerSec  = Constants.CLOUD_RATE_PER_MIPS;
        double costPerMem  = 0.05;
        double costPerStr  = 0.001;
        double costPerBw   = 0.0;

        FogDeviceCharacteristics chars = new FogDeviceCharacteristics(
            arch, os, vmm, host, timeZone,
            costPerSec, costPerMem, costPerStr, costPerBw);

        FogDevice cloud = new FogDevice(
            "Cloud",
            chars,
            new VmAllocationPolicySimple(hostList),
            new LinkedList<>(),
            10,                              // scheduling interval
            Constants.CLOUD_UPLINK_BW,
            Constants.CLOUD_DOWNLINK_BW,
            0,                               // uplink latency to parent (none)
            Constants.CLOUD_RATE_PER_MIPS
        );

        cloud.setParentId(-1);
        cloud.setUplinkLatency(0);
        return cloud;
    }
}
