package healthcare.placement;

import healthcare.optimization.DILPOptimizer;
import healthcare.utils.Constants;
import org.fog.application.AppModule;
import org.fog.entities.FogDevice;
import org.fog.placement.ModulePlacement;
import org.fog.placement.ModuleMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ModulePlacementPPEC.java
 * Custom iFogSim2 module placement strategy powered by the DILP optimizer.
 *
 * Placement rules:
 *   - Sensor module         → IoT device
 *   - DataCollector,
 *     PreProcessor,
 *     DILPOptimizer,
 *     ECCSecurityManager,
 *     TaskScheduler         → fog nodes (DILP-assigned)
 *   - CloudStorage          → cloud
 */
public class ModulePlacementPPEC extends ModulePlacement {

    private static final Logger LOG = Logger.getLogger(ModulePlacementPPEC.class.getName());

    private final DILPOptimizer dilpOptimizer;
    private final List<FogDevice> fogDevices;

    public ModulePlacementPPEC(List<FogDevice> fogDevices,
                                List<FogDevice> iotDevices,
                                FogDevice cloud,
                                org.fog.application.Application application,
                                ModuleMapping moduleMapping,
                                int numIoT) {
        super(fogDevices, application, moduleMapping);
        this.fogDevices    = fogDevices;
        this.dilpOptimizer = new DILPOptimizer(fogDevices.size());

        // Run DILP optimisation to get task→fog assignment
        int[] assignment = dilpOptimizer.optimise(numIoT);
        LOG.info("[ModulePlacementPPEC] DILP assignment complete. Avg CPU util: "
                + String.format("%.2f%%", dilpOptimizer.avgCpuUtilisation() * 100));

        applyPlacement(application, fogDevices, cloud, assignment);
    }

    /**
     * Map application modules to devices.
     * Fog modules go to DILP-chosen fog; cloud module to cloud.
     */
    private void applyPlacement(org.fog.application.Application app,
                                  List<FogDevice> fogs,
                                  FogDevice cloud,
                                  int[] assignment) {

        // Determine primary fog node for fog modules
        // (use the fog chosen most often by DILP)
        Map<Integer, Integer> fogCount = new HashMap<>();
        for (int f : assignment) fogCount.merge(f, 1, Integer::sum);
        int primaryFog = fogCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);

        FogDevice primaryFogDev = fogs.get(Math.min(primaryFog, fogs.size() - 1));

        // Map fog-layer modules
        String[] fogModules = {
            Constants.MODULE_DATA_COLLECTOR,
            Constants.MODULE_PREPROCESSOR,
            Constants.MODULE_DILP_OPTIMIZER,
            Constants.MODULE_ECC_SECURITY,
            Constants.MODULE_TASK_SCHEDULER
        };

        for (String mod : fogModules) {
            moduleMapping.addModuleToDevice(mod, primaryFogDev.getName());
            LOG.info("  Placed [" + mod + "] → " + primaryFogDev.getName());
        }

        // Map cloud module
        moduleMapping.addModuleToDevice(Constants.MODULE_CLOUD_STORAGE, cloud.getName());
        LOG.info("  Placed [" + Constants.MODULE_CLOUD_STORAGE + "] → " + cloud.getName());
    }

    @Override
    protected void mapModules() {
        // Placement is done in constructor via applyPlacement()
    }

    public DILPOptimizer getDilpOptimizer() { return dilpOptimizer; }
}
