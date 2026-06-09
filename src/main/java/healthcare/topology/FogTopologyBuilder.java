package healthcare.topology;

import healthcare.utils.Constants;
import healthcare.utils.Location;
import healthcare.utils.RandomGenerator;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.utils.FogLinearPowerModel;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * FogTopologyBuilder.java
 * Builds all fog node FogDevices and assigns them geographic locations.
 * Each fog node is positioned on a grid across the 3000×3000 simulation area.
 */
public class FogTopologyBuilder {

    private static final Logger LOG = Logger.getLogger(FogTopologyBuilder.class.getName());

    private final List<Location> fogLocations = new ArrayList<>();

    /**
     * Build the specified number of fog FogDevices.
     *
     * @param numFogs  number of fog nodes to create
     * @param proxyId  parent proxy server ID
     * @return list of fog FogDevices
     */
    public List<FogDevice> buildFogNodes(int numFogs, int proxyId) throws Exception {
        List<FogDevice> fogs = new ArrayList<>();

        for (int i = 0; i < numFogs; i++) {
            Location loc = RandomGenerator.fogGridLocation(i, numFogs);
            fogLocations.add(loc);

            FogDevice fog = createFogNode("Fog_" + i, proxyId, i);
            fogs.add(fog);

            LOG.info(String.format("Created Fog_%d at %s, parentId=%d", i, loc, proxyId));
        }
        return fogs;
    }

    private FogDevice createFogNode(String name, int proxyId, int index) throws Exception {
        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < Constants.FOG_NUM_PES; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(Constants.FOG_MIPS)));
        }

        Host host = new Host(
            index,
            new RamProvisionerSimple(Constants.FOG_RAM),
            new BwProvisionerSimple(Constants.FOG_UPLINK_BW),
            200_000L,
            peList,
            new VmSchedulerTimeShared(peList)
        );

        List<Host> hostList = new ArrayList<>();
        hostList.add(host);

        FogDeviceCharacteristics chars = new FogDeviceCharacteristics(
            "x86", "Linux", "Xen", host, 10.0,
            Constants.FOG_RATE_PER_MIPS, 0.01, 0.001, 0.0);

        FogDevice fog = new FogDevice(
            name,
            chars,
            new VmAllocationPolicySimple(hostList),
            new LinkedList<>(),
            10,
            Constants.FOG_UPLINK_BW,
            Constants.FOG_DOWNLINK_BW,
            2,                        // uplink latency to proxy (ms)
            Constants.FOG_RATE_PER_MIPS
        );

        fog.setParentId(proxyId);
        fog.setUplinkLatency(2);

        // Attach linear power model
        fog.setPowerModel(new FogLinearPowerModel(
            Constants.FOG_BUSY_POWER,
            Constants.FOG_IDLE_POWER
        ));

        return fog;
    }

    public List<Location> getFogLocations() { return fogLocations; }
}
