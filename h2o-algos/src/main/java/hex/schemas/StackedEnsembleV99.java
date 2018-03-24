package hex.schemas;

import hex.ensemble.StackedEnsemble;
import hex.StackedEnsembleModel;
import water.api.API;
import water.api.schemas3.KeyV3;
import water.api.schemas3.ModelParametersSchemaV3;
import hex.Model;
import water.api.schemas3.FrameV3;
import water.exceptions.H2OIllegalArgumentException;
import water.util.IcedHashMap;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class StackedEnsembleV99 extends ModelBuilderSchema<StackedEnsemble,StackedEnsembleV99,StackedEnsembleV99.StackedEnsembleParametersV99> {
  public static final class StackedEnsembleParametersV99 extends ModelParametersSchemaV3<StackedEnsembleModel.StackedEnsembleParameters, StackedEnsembleParametersV99> {
    static public String[] fields = new String[] {
      "model_id",
      "training_frame",
      "response_column",
      "validation_frame",
      "base_models",
      "metalearner_algorithm",
      "metalearner_nfolds",
      "metalearner_fold_assignment",
      "metalearner_fold_column",
      "keep_levelone_frame",
      "metalearner_params",
      "seed"
    };


    // Base models
    @API(level = API.Level.critical,
            help = "List of models (or model ids) to ensemble/stack together. Models must have been cross-validated using nfolds > 1, and folds must be identical across models.", required = true)
    public KeyV3.ModelKeyV3 base_models[];

    
    // Metalearner algorithm
    @API(level = API.Level.critical, direction = API.Direction.INOUT,
            gridable = true,
            values = {"AUTO", "glm", "gbm", "drf", "deeplearning"},
            help = "Type of algorithm to use as the metalearner. " +
                    "Options include 'AUTO' (GLM with non negative weights; if validation_frame is present, a lambda search is performed), 'glm' (GLM with default parameters), 'gbm' (GBM with default parameters), " +
                    "'drf' (Random Forest with default parameters), or 'deeplearning' (Deep Learning with default parameters).")
    public StackedEnsembleModel.StackedEnsembleParameters.MetalearnerAlgorithm metalearner_algorithm;

    // For ensemble metalearner cross-validation
    @API(level = API.Level.critical, direction = API.Direction.INOUT, 
            help = "Number of folds for K-fold cross-validation of the metalearner algorithm (0 to disable or >= 2).")
    public int metalearner_nfolds;

    // For ensemble metalearner cross-validation
    @API(level = API.Level.secondary, direction = API.Direction.INOUT, 
            values = {"AUTO", "Random", "Modulo", "Stratified"},
            help = "Cross-validation fold assignment scheme for metalearner cross-validation.  Defaults to AUTO (which is currently set to Random)." + 
                  " The 'Stratified' option will stratify the folds based on the response variable, for classification problems.")
    public Model.Parameters.FoldAssignmentScheme metalearner_fold_assignment;

    // For ensemble metalearner cross-validation
    @API(level = API.Level.secondary, direction = API.Direction.INOUT, 
            is_member_of_frames = {"training_frame"},
            //is_mutually_exclusive_with = {"ignored_columns", "response_column", "weights_column", "offset_column"},
            is_mutually_exclusive_with = {"ignored_columns", "response_column"},
            help = "Column with cross-validation fold index assignment per observation for cross-validation of the metalearner.")
    public FrameV3.ColSpecifierV3 metalearner_fold_column;

    @API(level = API.Level.secondary,
            help = "Keep level one frame used for metalearner training.")
    public boolean keep_levelone_frame;

    @API(help = "Parameters for metalearner algorithm", direction = API.Direction.INOUT, gridable = true)
    public IcedHashMap<String, Object[]> metalearner_params;

    @API(help = "Seed for random numbers; passed through to the metalearner algorithm. Defaults to -1 (time-based random number)", gridable = true)
    public long seed;

    @Override public StackedEnsembleParametersV99 fillFromParms(Properties parms) {
      if (parms.containsKey("metalearner_params")) {
        Map<String, Object> m;
        try {
          m = water.util.JSONUtils.parse(parms.getProperty("metalearner_params"));

          // Convert lists and singletons into arrays
          for (Map.Entry<String, Object> e : m.entrySet()) {
            Object o = e.getValue();
            Object[] o2 = o instanceof List ? ((List) o).toArray() : new Object[]{o};

            metalearner_params.put(e.getKey(), o2);
          }
        } catch (Exception e) {
          // usually JsonSyntaxException, but can also be things like IllegalStateException or NumberFormatException
          throw new H2OIllegalArgumentException("Can't parse the metalearner_params dictionary; got error: " + e.getMessage() + " for raw value: " + parms.getProperty("metalearner_params"));
        }
        parms.remove("metalearner_params");
      }
      this.fillFromParms(parms, false);

      return this;
    }
  }
}
