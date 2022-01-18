package hex.tree.gbm;

import hex.Model;
import hex.ModelBuilder;
import hex.genmodel.utils.DistributionFamily;
import water.MetricTest;
import org.junit.BeforeClass;
import org.junit.Test;
import water.Scope;
import water.fvec.Frame;

import java.util.function.Function;

public class GBMMetricTest extends MetricTest {
    
    private Function<Model.Parameters, ModelBuilder> gbmConstructor =  parameters -> {
        GBMModel.GBMParameters gbmParameters = (GBMModel.GBMParameters)parameters;
        return new GBM(gbmParameters);
    };

    @BeforeClass
    public static void setup() { stall_till_cloudsize(1); }

    @Test
    public void testIndependentModelMetricsCalculation_regression() {
        Scope.enter();
        try {
            String response = "AGE";
            Frame dataset = Scope.track(parseTestFile("smalldata/prostate/prostate.csv", new int[]{0}));
                    
            GBMModel.GBMParameters parms = new GBMModel.GBMParameters();
            parms._ntrees = 10;
            parms._response_column = response;
            parms._distribution = DistributionFamily.gaussian;
            
            double tolerance = 0.000001;
            testIndependentlyCalculatedSupervisedMetrics(dataset, parms, gbmConstructor, tolerance);
        } finally {
            Scope.exit();
        }
    }

    @Test
    public void testIndependentModelMetricsCalculation_binomial() {
        Scope.enter();
        try {
            String response = "CAPSULE";
            Frame dataset = Scope.track(parseTestFile("smalldata/prostate/prostate.csv", new int[]{0}));
            dataset.toCategoricalCol(response);
            
            GBMModel.GBMParameters parms = new GBMModel.GBMParameters();
            parms._ntrees = 10;
            parms._response_column = response;
            parms._distribution = DistributionFamily.bernoulli;
            
            double tolerance = 0.000001;
            testIndependentlyCalculatedSupervisedMetrics(dataset, parms, gbmConstructor, tolerance);
        } finally {
            Scope.exit();
        }
    }

    @Test
    public void testIndependentModelMetricsCalculation_multinomial() {
        Scope.enter();
        try {
            String response = "AGE";
            Frame dataset = Scope.track(parseTestFile("smalldata/prostate/prostate.csv", new int[]{0}));
            dataset.toCategoricalCol(response);

            GBMModel.GBMParameters parms = new GBMModel.GBMParameters();
            parms._ntrees = 10;
            parms._response_column = response;
            parms._distribution = DistributionFamily.multinomial;
            
            double tolerance = 0.000001;
            testIndependentlyCalculatedSupervisedMetrics(dataset, parms, gbmConstructor, tolerance);
        } finally {
            Scope.exit();
        }
    }

    @Test
    public void testIndependentModelMetricsCalculationWithOffsetColumn_regression() {
        Scope.enter();
        try {
            String response = "AGE";
            Frame dataset = Scope.track(parseTestFile("smalldata/prostate/prostate.csv"));

            GBMModel.GBMParameters parms = new GBMModel.GBMParameters();
            parms._ntrees = 10;
            parms._response_column = response;
            parms._distribution = DistributionFamily.gaussian;
            parms._offset_column = "ID";

            double tolerance = 0.000001;
            testIndependentlyCalculatedSupervisedMetrics(dataset, parms, gbmConstructor, tolerance);
        } finally {
            Scope.exit();
        }
    }

    @Test
    public void testIndependentModelMetricsCalculationWithOffsetColumn_binomial() {
        Scope.enter();
        try {
            String response = "CAPSULE";
            Frame dataset = Scope.track(parseTestFile("smalldata/prostate/prostate.csv"));
            dataset.toCategoricalCol(response);

            GBMModel.GBMParameters parms = new GBMModel.GBMParameters();
            parms._ntrees = 10;
            parms._response_column = response;
            parms._distribution = DistributionFamily.bernoulli;
            parms._offset_column = "ID";

            double tolerance = 0.000001;
            testIndependentlyCalculatedSupervisedMetrics(dataset, parms, gbmConstructor, tolerance);
        } finally {
            Scope.exit();
        }
    }
    

    @Test
    public void testIndependentModelMetricsCalculationWithWeightColumn_regression() {
        Scope.enter();
        try {
            String response = "AGE";
            Frame dataset = Scope.track(parseTestFile("smalldata/prostate/prostate.csv"));

            GBMModel.GBMParameters parms = new GBMModel.GBMParameters();
            parms._ntrees = 10;
            parms._response_column = response;
            parms._distribution = DistributionFamily.gaussian;
            parms._weights_column = "ID";

            double tolerance = 0.000001;
            testIndependentlyCalculatedSupervisedMetrics(dataset, parms, gbmConstructor, tolerance);
        } finally {
            Scope.exit();
        }
    }

    @Test
    public void testIndependentModelMetricsCalculationWithWeightColumn_binomial() {
        Scope.enter();
        try {
            String response = "CAPSULE";
            Frame dataset = Scope.track(parseTestFile("smalldata/prostate/prostate.csv"));
            dataset.toCategoricalCol(response);

            GBMModel.GBMParameters parms = new GBMModel.GBMParameters();
            parms._ntrees = 10;
            parms._response_column = response;
            parms._distribution = DistributionFamily.bernoulli;
            parms._weights_column = "ID";

            double tolerance = 0.000001;
            testIndependentlyCalculatedSupervisedMetrics(dataset, parms, gbmConstructor, tolerance);
        } finally {
            Scope.exit();
        }
    }

    @Test
    public void testIndependentModelMetricsCalculationWithWeightColumn_multinomial() {
        Scope.enter();
        try {
            String response = "AGE";
            Frame dataset = Scope.track(parseTestFile("smalldata/prostate/prostate.csv"));
            dataset.toCategoricalCol(response);

            GBMModel.GBMParameters parms = new GBMModel.GBMParameters();
            parms._ntrees = 10;
            parms._response_column = response;
            parms._distribution = DistributionFamily.multinomial;
            parms._weights_column = "ID";

            double tolerance = 0.000001;
            testIndependentlyCalculatedSupervisedMetrics(dataset, parms, gbmConstructor, tolerance);
        } finally {
            Scope.exit();
        }
    }
}