package forecasting.combinationStrategies.ann;

import org.apache.commons.lang3.NotImplementedException;
import org.encog.engine.network.activation.ActivationLinear;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.pattern.ElmanPattern;
import org.encog.neural.pattern.FeedForwardPattern;
import org.encog.neural.pattern.JordanPattern;
import org.encog.neural.pattern.NeuralNetworkPattern;
import org.jetbrains.annotations.Nullable;

/**
 * Factory pattern to create several Neural Network types.
 *
 * Sigmoid: value range of 0 to 1.
 * TANH: value range of -1 to 1.
 * https://en.wikipedia.org/wiki/Activation_function
 *
 * Created by oc6admin on 08.03.2016.
 */
public interface ANNFactory {
    enum TYPES {
        ELMAN_RECURRENT {
            public BasicNetwork create(int inputNeurons, int hiddenNeurons, int outputNeurons) {
                NeuralNetworkPattern pattern = new ElmanPattern();
                pattern.setActivationFunction(new ActivationTANH());
                pattern.setInputNeurons(inputNeurons);
                pattern.addHiddenLayer(hiddenNeurons);
                pattern.setOutputNeurons(outputNeurons);
                return (BasicNetwork) pattern.generate();
            }
        },
        FEED_FORWARD {
            public BasicNetwork create(int inputNeurons, int hiddenNeurons, int outputNeurons) {
                NeuralNetworkPattern pattern = new FeedForwardPattern();

                pattern.setInputNeurons(inputNeurons);

                pattern.setActivationFunction(new ActivationTANH());
                pattern.addHiddenLayer(hiddenNeurons);
               // pattern.addHiddenLayer(5);

                pattern.setActivationFunction(new ActivationTANH());
                pattern.setOutputNeurons(outputNeurons);

                return (BasicNetwork) pattern.generate();
            }
        },
        JORDAN_RECURRENT {
            public BasicNetwork create(int inputNeurons, int hiddenNeurons, int outputNeurons) {
                NeuralNetworkPattern pattern = new JordanPattern();
                pattern.setActivationFunction(new ActivationTANH());
                pattern.setInputNeurons(inputNeurons);
                pattern.addHiddenLayer(hiddenNeurons);
                pattern.setOutputNeurons(outputNeurons);
                return (BasicNetwork) pattern.generate();
            }
        };

        @Nullable
        public BasicNetwork create(int inputNeurons, int hiddenNeurons, int outputNeurons) {
            throw new NotImplementedException("Implement method");
        }
    }
}
