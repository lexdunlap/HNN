import jdk.nashorn.internal.objects.annotations.Setter;

import java.lang.Math;

/**
 * Created by Michael on 13/12/16.
 */
public class Hopfield_Network
{

    private boolean converged;                      // Flips when the network converges to a stable energy level
    private int k, n;                               // k-neurons out of n total neurons
    private int[] input;                            // I-Vector
    private int[][] transition_table;               // T-Matrix

    private double alpha, tau, step_size;           // Programmer-defined alpha value
    private double[] activation,                    // U-Vector
            output,                                 // V-Vector
            prev_output;                            // V(t-1)-Vector

    /**
     * Creates Hopfield Network, setting all initial programmer-defined values. Also generates
     * the transition table weight matrix based on neuron connections, as well as generating
     * the initial values for the activation vector (u-vector).
     *
     * @param k number of selected neurons out of total n
     * @param n total number of neurons
     * @param alpha activation threshold value for determining neuron response
     */
    public Hopfield_Network(int k, int n, double alpha, int[] inputValue, double step_size)
    {
    	this.converged = false;
        this.k = k;
        this.n = n;
        this.step_size = step_size;
        this.alpha = alpha;
        this.input = inputValue;
        tau = 2 * alpha;
        transition_table = new int[n][n];
        activation = new double[n];
        output = new double[n];
        prev_output = new double[n];
        gen_trans_table();
        init_activation();
        run();
    }

    /**
     * Continually updates and calculates the activation of all neurons until convergence is reached.
     *
     * #TODO: Set stability when g(u) is non-decreasing (?)
     */
    public void run()
    {
        while (!converged)
        {
            int convergence_count = 0;
            for(int i = 0; i < n; i++)
            {
                prev_output[i] = output[i];
                update_neuron(i);
                neuron_activation(i);

                if (output[i] == prev_output[i])
                {
                    convergence_count++;
                }
            }
        if (convergence_count >= k)
        {
            converged = true;
        }
        }
    }
    public int[][] getTransitionTable(){
    	return transition_table;
    }
    
    public double[] getOuput(){
    	return output;
    }

    /**
     * Used for inputting an input vector into the inner input matrix of the
     * Hopfield Network class.
     *
     * @param i_vector row vector of input values.
     */
    @Setter public void load_inputs(int[] i_vector)
    {
        input = i_vector;
    }

    /**
     * Generates the transition table (weight matrix). Currently, assigns weights of -2 for each
     * neuron pair, excluding self connections, which are instead assigned values of 0.
     *
     * Automatically applies the calculated weights to the transition_table class variable for
     * each connection from neuron i -> neuron j.
     *
     * TODO: Allow for multiple categories of neurons, assigning -2 for each common category.
     */
    private void gen_trans_table()
    {
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                if (i == j)
                    transition_table[i][j] = 0;
                else
                    transition_table[i][j] -= 2;
            }
        }
    }

    /**
     * Generates the initial activation level for each neuron - calculated by taking (2kα/n)-α.
     *
     * TODO: Add perturbation to these initial u-values.
     */
    private void init_activation()
    {
        for (int i = 0; i < n; i++)
        {
            activation[i] = (2 * alpha) * (k / n) - alpha;
        }
    }

    /**
     * Calculates the dynamical system formula for a Hopfield Network detailing the change in activation
     * energy for a specific neuron against a change in time.
     *
     * @param index of neuron to be updated
     */
    private void update_neuron(int index)
    {
        double trans_output_dot = 0;
        for (int j = 0; j < n; j++)
        {
            if (j == index)
                continue;
            else
                trans_output_dot += transition_table[index][j] * output[j];
        }

        trans_output_dot += input[index] + (-1 * activation[index] / tau);
        activation[index] += trans_output_dot * step_size;
    }

    /**
     * Lyapunov Energy equation. Represents the cost function for a given Hopfield Network to minimize.
     *
     * NOTE: Apparently only used for 'network design'? Might not be the loss function for gradient descent learning.
     * Has been stated that the dynamical system might be a better computational representation.
     *
     * @return calculated lyapunov energy value for the current Hopfield system.
     */
    private double lyapunov()
    {
        double weighted_output_sum = 0,
                input_output_sum = 0;

        for (int i = 0; i < n; i++)
        {
            input_output_sum += input[i] * output[i];

            for (int j = 0; j < n; j++)
            {
                weighted_output_sum += transition_table[i][j] * output[i] * output[j];
            }
        }

        return (-.5 * weighted_output_sum - input_output_sum);
    }

    /**
     * Corresponds to g(u_i). Given a programmer-defined constant alpha, which represents the activation
     * thresholding (or bounds) for all neurons in the network. Comparing the current neuron response,
     * represented by an index i in the activation vector (u-vector), against this alpha determines the
     * current output response of the given neuron.
     *
     * @param index
     * @return
     */
    private void neuron_activation(int index)
    {
        if(activation[index] >= this.alpha)
            output[index] = 1.0;
        else if (Math.abs(activation[index]) <= this.alpha)
            output[index] = ((activation[index] + this.alpha) / (2 * this.alpha));
        else if (activation[index] <= -1 * this.alpha)
            output[index] = 0.0;
    }

}
