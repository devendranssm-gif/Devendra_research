package healthcare.application;

import healthcare.utils.Constants;
import org.fog.application.AppEdge;

import java.util.ArrayList;
import java.util.List;

/**
 * AppEdges.java
 * Defines the directed edges (data flows) in the PPEC application graph.
 *
 * The workflow follows:
 *   Sensor → DataCollector → PreProcessor → DILPOptimizer →
 *   ECCSecurityManager → TaskScheduler → CloudStorage → (result back)
 */
public class AppEdges {

    /**
     * Build and return all application edges for one IoT device.
     *
     * @param appId  iFogSim2 application identifier
     * @return list of AppEdge objects
     */
    public List<AppEdge> buildEdges(String appId) {
        List<AppEdge> edges = new ArrayList<>();

        // IoT → Fog: sensor to collector (SENSOR tuple)
        edges.add(new AppEdge(
            Constants.MODULE_SENSOR,
            Constants.MODULE_DATA_COLLECTOR,
            Constants.SENSOR_CLOUDLET_LENGTH,
            Constants.TUPLE_SENSOR_SIZE,
            Constants.TUPLE_SENSOR_DATA,
            AppEdge.SENSOR,
            AppEdge.UP
        ));

        // Fog: collector → preprocessor
        edges.add(new AppEdge(
            Constants.MODULE_DATA_COLLECTOR,
            Constants.MODULE_PREPROCESSOR,
            Constants.COLLECTOR_CLOUDLET_LENGTH,
            Constants.TUPLE_PROC_SIZE,
            Constants.TUPLE_COLLECTED,
            AppEdge.MODULE,
            AppEdge.UP
        ));

        // Fog: preprocessor → DILP optimizer
        edges.add(new AppEdge(
            Constants.MODULE_PREPROCESSOR,
            Constants.MODULE_DILP_OPTIMIZER,
            Constants.PREPROCESSOR_CLOUDLET_LENGTH,
            Constants.TUPLE_PROC_SIZE,
            Constants.TUPLE_PREPROCESSED,
            AppEdge.MODULE,
            AppEdge.UP
        ));

        // Fog: DILP optimizer → ECC security manager
        edges.add(new AppEdge(
            Constants.MODULE_DILP_OPTIMIZER,
            Constants.MODULE_ECC_SECURITY,
            Constants.OPTIMIZER_CLOUDLET_LENGTH,
            Constants.TUPLE_ENC_SIZE,
            Constants.TUPLE_OPTIMIZED,
            AppEdge.MODULE,
            AppEdge.UP
        ));

        // Fog: ECC manager → task scheduler
        edges.add(new AppEdge(
            Constants.MODULE_ECC_SECURITY,
            Constants.MODULE_TASK_SCHEDULER,
            Constants.SECURITY_CLOUDLET_LENGTH,
            Constants.TUPLE_ENC_SIZE,
            Constants.TUPLE_ENCRYPTED,
            AppEdge.MODULE,
            AppEdge.UP
        ));

        // Fog → Cloud: task scheduler → cloud storage
        edges.add(new AppEdge(
            Constants.MODULE_TASK_SCHEDULER,
            Constants.MODULE_CLOUD_STORAGE,
            Constants.SCHEDULER_CLOUDLET_LENGTH,
            Constants.TUPLE_ENC_SIZE,
            Constants.TUPLE_SCHEDULED,
            AppEdge.MODULE,
            AppEdge.UP
        ));

        // Cloud → Fog: result back to sensor (acknowledgement)
        edges.add(new AppEdge(
            Constants.MODULE_CLOUD_STORAGE,
            Constants.MODULE_SENSOR,
            Constants.STORAGE_CLOUDLET_LENGTH,
            Constants.TUPLE_RESULT_SIZE,
            Constants.TUPLE_RESULT,
            AppEdge.MODULE,
            AppEdge.DOWN
        ));

        return edges;
    }
}
