package healthcare.application;

import healthcare.utils.Constants;
import org.fog.application.selectivity.FractionalSelectivity;

import java.util.HashMap;
import java.util.Map;

/**
 * TupleMappings.java
 * Defines input→output tuple type mappings for each application module.
 * Used by iFogSim2 to route tuples through the processing pipeline.
 */
public class TupleMappings {

    /**
     * Build the full module tuple mapping:
     *   moduleName → { inputTupleType → (outputTupleType, selectivity) }
     */
    public Map<String, Map<String, Object[]>> buildMappings() {
        Map<String, Map<String, Object[]>> mappings = new HashMap<>();

        // DataCollector: SENSOR_DATA → COLLECTED_DATA (100% pass-through)
        mappings.put(Constants.MODULE_DATA_COLLECTOR,
            Map.of(Constants.TUPLE_SENSOR_DATA,
                   new Object[]{Constants.TUPLE_COLLECTED, 1.0}));

        // PreProcessor: COLLECTED_DATA → PREPROCESSED_DATA
        mappings.put(Constants.MODULE_PREPROCESSOR,
            Map.of(Constants.TUPLE_COLLECTED,
                   new Object[]{Constants.TUPLE_PREPROCESSED, 1.0}));

        // DILPOptimizer: PREPROCESSED_DATA → OPTIMIZED_DATA
        mappings.put(Constants.MODULE_DILP_OPTIMIZER,
            Map.of(Constants.TUPLE_PREPROCESSED,
                   new Object[]{Constants.TUPLE_OPTIMIZED, 1.0}));

        // ECCSecurityManager: OPTIMIZED_DATA → ENCRYPTED_DATA
        mappings.put(Constants.MODULE_ECC_SECURITY,
            Map.of(Constants.TUPLE_OPTIMIZED,
                   new Object[]{Constants.TUPLE_ENCRYPTED, 1.0}));

        // TaskScheduler: ENCRYPTED_DATA → SCHEDULED_DATA
        mappings.put(Constants.MODULE_TASK_SCHEDULER,
            Map.of(Constants.TUPLE_ENCRYPTED,
                   new Object[]{Constants.TUPLE_SCHEDULED, 1.0}));

        // CloudStorage: SCHEDULED_DATA → RESULT
        mappings.put(Constants.MODULE_CLOUD_STORAGE,
            Map.of(Constants.TUPLE_SCHEDULED,
                   new Object[]{Constants.TUPLE_RESULT, 1.0}));

        return mappings;
    }

    /**
     * Build FractionalSelectivity map for iFogSim2 AppModule.
     *
     * @param moduleName the module whose selectivity to build
     * @return map of inputType → FractionalSelectivity
     */
    public Map<String, FractionalSelectivity> getSelectivity(String moduleName) {
        Map<String, FractionalSelectivity> sel = new HashMap<>();
        Map<String, Map<String, Object[]>> all = buildMappings();
        Map<String, Object[]> moduleMap = all.get(moduleName);
        if (moduleMap != null) {
            for (Map.Entry<String, Object[]> e : moduleMap.entrySet()) {
                String outputType  = (String) e.getValue()[0];
                double selectivity = (double) e.getValue()[1];
                sel.put(e.getKey(), new FractionalSelectivity(selectivity));
            }
        }
        return sel;
    }
}
