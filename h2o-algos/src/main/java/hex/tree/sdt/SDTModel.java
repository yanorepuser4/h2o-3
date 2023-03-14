package hex.tree.sdt;

import hex.*;
import org.apache.log4j.Logger;
import water.*;

import java.util.Arrays;

public class SDTModel extends Model<SDTModel, SDTModel.SDTParameters, SDTModel.SDTOutput> {

    private static final Logger LOG = Logger.getLogger(SDTModel.class);


    public SDTModel(Key<SDTModel> selfKey, SDTModel.SDTParameters parms,
                    SDTModel.SDTOutput output) {
        super(selfKey, parms, output);
    }

    @Override
    public ModelMetrics.MetricBuilder makeMetricBuilder(String[] domain) {
        switch (_output.getModelCategory()) {
            case Binomial:
                return new ModelMetricsBinomial.MetricBuilderBinomial(domain);
            case Multinomial:
                return new ModelMetricsMultinomial.MetricBuilderMultinomial(_output.nclasses(), domain, _parms._auc_type);
            case Regression:
                return new ModelMetricsRegression.MetricBuilderRegression();
            default:
                throw H2O.unimpl();
        }
    }

    @Override
    protected double[] score0(double[] data, double[] preds) {
        assert _output._treeKey != null : "Output has no tree, check if tree is properly set to the output.";
        // compute score for given point
        CompressedSDT tree = DKV.getGet(_output._treeKey);
        SDTPrediction prediction = tree.predictRowStartingFromNode(data, 0, "");
        System.out.println(prediction.ruleExplanation);
        // for now, only pred. for class 0 is stored, will be improved later
        preds[0] = prediction.classPrediction;
        preds[1] = prediction.probability;
        preds[2] = 1 - prediction.probability;
        
        System.out.println(Arrays.toString(preds));
        return preds;
    }

    public static class SDTOutput extends Model.Output {
        public int _max_depth;
        public int _limitNumSamplesForSplit;

        public Key<CompressedSDT> _treeKey;

        public SDTOutput(SDT sdt) {
            super(sdt);
            _max_depth = sdt._parms._max_depth;
            _limitNumSamplesForSplit = sdt._parms._min_rows;
        }

    }

    @Override
    protected Futures remove_impl(Futures fs, boolean cascade) {
        Keyed.remove(_output._treeKey, fs, true);
        return super.remove_impl(fs, cascade);
    }

    @Override
    protected AutoBuffer writeAll_impl(AutoBuffer ab) {
        ab.putKey(_output._treeKey);
        return super.writeAll_impl(ab);
    }

    @Override
    protected Keyed readAll_impl(AutoBuffer ab, Futures fs) {
        ab.getKey(_output._treeKey, fs);
        return super.readAll_impl(ab, fs);
    }

    public static class SDTParameters extends Model.Parameters {
        long seed = -1; //ignored
        /**
         * Depth (max depth) of the tree
         */
        public int _max_depth;

        public int _min_rows;

        public SDTParameters() {
            super();
            _max_depth = 20;
            _min_rows = 10;
        }

        @Override
        public String algoName() {
            return "SDT";
        }

        @Override
        public String fullName() {
            return "Single Decision Tree";
        }

        @Override
        public String javaName() {
            return SDTModel.class.getName();
        }

        @Override
        public long progressUnits() {
            return 1;
        }
    }
}
