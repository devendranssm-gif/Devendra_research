package healthcare.application;

import healthcare.utils.Constants;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;

import java.util.ArrayList;
import java.util.List;

/**
 * HealthcareApplication.java
 * Assembles the complete iFogSim2 Application model for the PPEC framework.
 *
 * Registers all modules, edges, and loops into an iFogSim2 Application object.
 */
public class HealthcareApplication {

    private final String appId;
    private final int    userId;

    public HealthcareApplication(String appId, int userId) {
        this.appId  = appId;
        this.userId = userId;
    }

    /**
     * Build and return the configured iFogSim2 Application.
     */
    public Application buildApplication() {
        Application app = Application.createApplication(appId, userId);

        // 1. Register modules
        addModules(app);

        // 2. Register edges
        addEdges(app);

        // 3. Register monitoring loops
        addLoops(app);

        return app;
    }

    private void addModules(Application app) {
        // DataCollector
        app.addAppModule(Constants.MODULE_DATA_COLLECTOR, 10);
        // PreProcessor
        app.addAppModule(Constants.MODULE_PREPROCESSOR, 10);
        // DILPOptimizer
        app.addAppModule(Constants.MODULE_DILP_OPTIMIZER, 10);
        // ECCSecurityManager
        app.addAppModule(Constants.MODULE_ECC_SECURITY, 10);
        // TaskScheduler
        app.addAppModule(Constants.MODULE_TASK_SCHEDULER, 10);
        // CloudStorage
        app.addAppModule(Constants.MODULE_CLOUD_STORAGE, 10);
    }

    private void addEdges(Application app) {
        // IoT sensor → DataCollector (upstream SENSOR edge)
        app.addAppEdge(
            Constants.MODULE_SENSOR,
            Constants.MODULE_DATA_COLLECTOR,
            Constants.SENSOR_CLOUDLET_LENGTH,
            Constants.TUPLE_SENSOR_SIZE,
            Constants.TUPLE_SENSOR_DATA,
            AppEdge.SENSOR,
            AppEdge.UP
        );

        // DataCollector → PreProcessor
        app.addAppEdge(
            Constants.MODULE_DATA_COLLECTOR,
            Constants.MODULE_PREPROCESSOR,
            Constants.COLLECTOR_CLOUDLET_LENGTH,
            Constants.TUPLE_PROC_SIZE,
            Constants.TUPLE_COLLECTED,
            AppEdge.MODULE,
            AppEdge.UP
        );

        // PreProcessor → DILPOptimizer
        app.addAppEdge(
            Constants.MODULE_PREPROCESSOR,
            Constants.MODULE_DILP_OPTIMIZER,
            Constants.PREPROCESSOR_CLOUDLET_LENGTH,
            Constants.TUPLE_PROC_SIZE,
            Constants.TUPLE_PREPROCESSED,
            AppEdge.MODULE,
            AppEdge.UP
        );

        // DILPOptimizer → ECCSecurityManager
        app.addAppEdge(
            Constants.MODULE_DILP_OPTIMIZER,
            Constants.MODULE_ECC_SECURITY,
            Constants.OPTIMIZER_CLOUDLET_LENGTH,
            Constants.TUPLE_ENC_SIZE,
            Constants.TUPLE_OPTIMIZED,
            AppEdge.MODULE,
            AppEdge.UP
        );

        // ECCSecurityManager → TaskScheduler
        app.addAppEdge(
            Constants.MODULE_ECC_SECURITY,
            Constants.MODULE_TASK_SCHEDULER,
            Constants.SECURITY_CLOUDLET_LENGTH,
            Constants.TUPLE_ENC_SIZE,
            Constants.TUPLE_ENCRYPTED,
            AppEdge.MODULE,
            AppEdge.UP
        );

        // TaskScheduler → CloudStorage
        app.addAppEdge(
            Constants.MODULE_TASK_SCHEDULER,
            Constants.MODULE_CLOUD_STORAGE,
            Constants.SCHEDULER_CLOUDLET_LENGTH,
            Constants.TUPLE_ENC_SIZE,
            Constants.TUPLE_SCHEDULED,
            AppEdge.MODULE,
            AppEdge.UP
        );

        // CloudStorage → Sensor (result back downstream)
        app.addAppEdge(
            Constants.MODULE_CLOUD_STORAGE,
            Constants.MODULE_SENSOR,
            Constants.STORAGE_CLOUDLET_LENGTH,
            Constants.TUPLE_RESULT_SIZE,
            Constants.TUPLE_RESULT,
            AppEdge.MODULE,
            AppEdge.DOWN
        );

        // ── Tuple Selectivities ───────────────────────────────────────────────
        app.addTupleMapping(Constants.MODULE_DATA_COLLECTOR,
            Constants.TUPLE_SENSOR_DATA,   Constants.TUPLE_COLLECTED,
            new FractionalSelectivity(1.0));

        app.addTupleMapping(Constants.MODULE_PREPROCESSOR,
            Constants.TUPLE_COLLECTED,     Constants.TUPLE_PREPROCESSED,
            new FractionalSelectivity(1.0));

        app.addTupleMapping(Constants.MODULE_DILP_OPTIMIZER,
            Constants.TUPLE_PREPROCESSED,  Constants.TUPLE_OPTIMIZED,
            new FractionalSelectivity(1.0));

        app.addTupleMapping(Constants.MODULE_ECC_SECURITY,
            Constants.TUPLE_OPTIMIZED,     Constants.TUPLE_ENCRYPTED,
            new FractionalSelectivity(1.0));

        app.addTupleMapping(Constants.MODULE_TASK_SCHEDULER,
            Constants.TUPLE_ENCRYPTED,     Constants.TUPLE_SCHEDULED,
            new FractionalSelectivity(1.0));

        app.addTupleMapping(Constants.MODULE_CLOUD_STORAGE,
            Constants.TUPLE_SCHEDULED,     Constants.TUPLE_RESULT,
            new FractionalSelectivity(1.0));
    }

    private void addLoops(Application app) {
        // End-to-end loop: Sensor → ... → CloudStorage
        List<String> loopModules = new ArrayList<>();
        loopModules.add(Constants.MODULE_SENSOR);
        loopModules.add(Constants.MODULE_DATA_COLLECTOR);
        loopModules.add(Constants.MODULE_PREPROCESSOR);
        loopModules.add(Constants.MODULE_DILP_OPTIMIZER);
        loopModules.add(Constants.MODULE_ECC_SECURITY);
        loopModules.add(Constants.MODULE_TASK_SCHEDULER);
        loopModules.add(Constants.MODULE_CLOUD_STORAGE);

        app.addAppLoop(new AppLoop(loopModules));
    }
}
