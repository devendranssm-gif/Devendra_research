package healthcare.topology;

import healthcare.utils.Constants;
import healthcare.utils.Location;
import healthcare.utils.RandomGenerator;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * IoTDeployment.java
 * Deploys IoT sensor devices (FogDevices at the edge layer) clustered
 * around their assigned fog nodes.
 *
 * Each IoT device is assigned DEVICES_PER_FOG = 5 devices per fog node.
 * For scaled scenarios (42, 54, 66, 78 IoT), additional devices are
 * round-robin distributed across fog nodes.
 */
public class IoTDeployment {

    private static final Logger LOG = Logger.getLogger(IoTDeployment.class.getName());

    private final List<Location> iotLocations = new ArrayList<>();

    /**
     * Deploy numIoT sensor FogDevices across fog nodes.
     *
     * @param numIoT       total IoT devices to deploy
     * @param fogDevices   list of fog FogDevices
     * @param fogLocations geographic locations of fog nodes
     * @return list of IoT FogDevices
     */
    public List<FogDevice> deployIoTDevices(int numIoT,
                                             List<FogDevice> fogDevices,
                                             List<Location>  fogLocations) throws Exception {
        List<FogDevice> iotList = new ArrayList<>();
        int numFogs = fogDevices.size();

        for (int i = 0; i < numIoT; i++) {
            int fogIdx  = i % numFogs;
            int fogId   = fogDevices.get(fogIdx).getId();
            Location fl = fogLocations.get(fogIdx);
            Location il = RandomGenerator.iotLocationNearFog(fl);
            iotLocations.add(il);

            String sensorType = sensorName(i);
            FogDevice iot = createIoTDevice("IoT_" + sensorType + "_" + i, fogId);
            iotList.add(iot);

            LOG.fine(String.format("Deployed IoT_%d (%s) → Fog_%d at %s",
                    i, sensorType, fogIdx, il));
        }
        LOG.info("[IoTDeployment] Deployed " + numIoT + " IoT devices across " + numFogs + " fogs.");
        return iotList;
    }

    private FogDevice createIoTDevice(String name, int parentFogId) throws Exception {
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(Constants.IOT_MIPS)));

        Host host = new Host(
            0,
            new RamProvisionerSimple(Constants.IOT_RAM),
            new BwProvisionerSimple(Constants.IOT_UPLINK_BW),
            10_000L,
            peList,
            new VmSchedulerTimeShared(peList)
        );

        List<Host> hostList = new ArrayList<>();
        hostList.add(host);

        FogDeviceCharacteristics chars = new FogDeviceCharacteristics(
            "ARM", "TinyOS", "None", host, 0.0,
            0.0001, 0.0, 0.0, 0.0);

        FogDevice iot = new FogDevice(
            name,
            chars,
            new VmAllocationPolicySimple(hostList),
            new LinkedList<>(),
            10,
            Constants.IOT_UPLINK_BW,
            Constants.IOT_DOWNLINK_BW,
            1,
            0.0001
        );
        iot.setParentId(parentFogId);
        iot.setUplinkLatency(1);
        return iot;
    }

    /** Rotate through the 5 healthcare sensor types. */
    private String sensorName(int idx) {
        switch (idx % 5) {
            case 0: return "ECG";
            case 1: return "HeartRate";
            case 2: return "BloodPressure";
            case 3: return "BodyTemp";
            case 4: return "SpO2";
            default: return "Sensor";
        }
    }

    public List<Location> getIoTLocations() { return iotLocations; }
}
