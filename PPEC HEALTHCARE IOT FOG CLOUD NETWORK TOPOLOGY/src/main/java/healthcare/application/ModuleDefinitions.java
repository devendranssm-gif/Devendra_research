package healthcare.application;

import healthcare.utils.Constants;
import org.fog.application.AppModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ModuleDefinitions.java
 * Defines all application modules with their resource requirements
 * for the PPEC healthcare application.
 */
public class ModuleDefinitions {

    /**
     * Create AppModule descriptors for all pipeline stages.
     *
     * @param appId iFogSim2 application ID
     * @param userId broker/user ID
     * @return list of AppModules
     */
    public List<AppModule> buildModules(String appId, int userId) {
        TupleMappings tm = new TupleMappings();
        List<AppModule> modules = new ArrayList<>();

        // DataCollector — fog layer (lightweight)
        modules.add(new AppModule(userId,
            Constants.MODULE_DATA_COLLECTOR, appId, userId,
            1000,          // MIPS
            512,           // RAM MB
            1000,          // BW
            10_000L,       // image size
            1,             // PEs
            new HashMap<>(),                             // vmm
            tm.getSelectivity(Constants.MODULE_DATA_COLLECTOR),
            null
        ));

        // PreProcessor — fog layer
        modules.add(new AppModule(userId,
            Constants.MODULE_PREPROCESSOR, appId, userId,
            2000, 512, 1000, 10_000L, 1,
            new HashMap<>(),
            tm.getSelectivity(Constants.MODULE_PREPROCESSOR),
            null
        ));

        // DILPOptimizer — fog layer (compute-intensive)
        modules.add(new AppModule(userId,
            Constants.MODULE_DILP_OPTIMIZER, appId, userId,
            5000, 1024, 1000, 20_000L, 1,
            new HashMap<>(),
            tm.getSelectivity(Constants.MODULE_DILP_OPTIMIZER),
            null
        ));

        // ECCSecurityManager — fog layer
        modules.add(new AppModule(userId,
            Constants.MODULE_ECC_SECURITY, appId, userId,
            4000, 512, 1000, 15_000L, 1,
            new HashMap<>(),
            tm.getSelectivity(Constants.MODULE_ECC_SECURITY),
            null
        ));

        // TaskScheduler — fog layer
        modules.add(new AppModule(userId,
            Constants.MODULE_TASK_SCHEDULER, appId, userId,
            2000, 256, 1000, 10_000L, 1,
            new HashMap<>(),
            tm.getSelectivity(Constants.MODULE_TASK_SCHEDULER),
            null
        ));

        // CloudStorage — cloud layer
        modules.add(new AppModule(userId,
            Constants.MODULE_CLOUD_STORAGE, appId, userId,
            1000, 256, 1000, 5_000L, 1,
            new HashMap<>(),
            tm.getSelectivity(Constants.MODULE_CLOUD_STORAGE),
            null
        ));

        return modules;
    }
}
