import jdk.nashorn.internal.objects.annotations.Setter;
import java.lang.Math;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Hopfield Network class.
 *
 * TODO: Fill out javadocs.
 */
public class Hopfield_Network
{
    private boolean converged;              // Flips when the network converges to a stable energy level
    private int n;                          // k-neurons out of n total neurons

    private int[] k,                        // k-out-of-n. k-neurons out of n total neurons
            category,                       // Keeps track of category for a given neuron
            input,                          // I-Vector
            firing_order;                   // Shuffled to randomise order neurons are fired in
    private int[][] transition_table;       // T-Matrix

    private double alpha,                   // Programmer-defined alpha value
            tau,                            // Programmer-defined tau value
            step_size,                      // Integration step size used in activation function
            margin;                         // Decimal-percent value for allowed deviation from binary convergence states
    private double[] activation,            // U-Vector
            output,                         // V-Vector
            prev_output;                    // V(t-1)-Vector

    /**
     * Creates Hopfield Network, setting all initial programmer-defined values. Also generates
     * the transition table weight matrix based on neuron connections, as well as generating
     * the initial values for the activation vector (u-vector).
     *
     * TODO: Add int[][] transition_table pass-through from Main file.
     *
     * @param k number of selected neurons out of total n
     * @param n total number of neurons
     * @param alpha activation threshold value for determining neuron response
     * @param input_vector
     * @param transition_table
     * @param step_size
     * @param category vector telling the category of each neuron - should be of length 'n'
     */
    public Hopfield_Network(int[] k, int n, double alpha, int[] input_vector,
                            int[][] transition_table, double step_size, int[] category)
    {
//    	this.converged = false;
        this.k = k;
        this.n = n;
        this.step_size = step_size;
        this.alpha = alpha;
        tau = 2 * alpha;
        margin = .01;
        this.transition_table = transition_table;
        this.category = category;

//        transition_table = new int[n][n];
        input = new int[n];
        activation = new double[n];
        output = new double[n];
        prev_output = new double[n];
        firing_order = new int[n];

        for (int i = 0; i < n; i++)
        {
            firing_order[i] = i;
        }

        set_inputs(input_vector);
        init_activation(.3);
        run();
    }

    /**
     * Constructor for creating a single-set network.
     *
     * @param k
     * @param n
     * @param alpha
     * @param input_vector
     * @param transition_table
     * @param step_size
     */
    public Hopfield_Network(int k, int n, double alpha, int[] input_vector,
                            int[][] transition_table, double step_size)
    {
    }

    /**
     * TODO: Documentation!
     *
     * @param in
     */
    private void set_inputs(int[] in)
    {
        for (int i = 0; i < in.length; i++)
            input[i] = (2 * k[in[i]]) - 1;
    }

    /**
     * Continually updates and calculates the activation of all neurons until convergence is reached.
     */
    public void run()
    {
        int[] convergence_count = new int[k.length];
        boolean[] converged = new boolean[k.length];
        boolean digital_states, finished = false;

        for (int i = 0; i < k.length; i++)
            converged[i] = false;

        while (!finished)
        {
            digital_states = true;
            firing_order = shuffle(firing_order);
            for (int i = 0; i < convergence_count.length; i++)
                convergence_count[i] = 0;

            for(int i = 0; i < n; i++) {
                int index = firing_order[i];
                update_neuron(index);
                neuron_activation(index);

                if (output[index] == prev_output[index])
                    convergence_count[category[i]]++;

                if ((output[index] * (1 - output[index])) >= margin)
                    digital_states = false;

                prev_output[index] = output[index];
            }

            for (int i = 0; i < k.length; i++)
                converged[i] = ((convergence_count[i] >= k[i]) && digital_states);
            finished = check_all_bool(converged);
        }
    }

    /**
     * Checks all boolean values in an array. Returns false on the first false element found. Returns true
     *    if all elements are true in the entire array.
     *
     * @param bool_array Array of boolean values to check.
     * @return true if all values are true; false if any values are false
     */
    private boolean check_all_bool(boolean[] bool_array)
    {
        for (boolean value : bool_array)
            if (!value) return false;
        return true;
    }

    public int[][] getTransitionTable() {
    	return transition_table;
    }
    
    public double[] getOuput() {
    	return output;
    }

    public int[] getKValue() {
    	return k;
    }

    /**
     * Used for inputting an input vector into the inner input matrix of the
     * Hopfield Network class.
     *
     * @param i_vector row vector of input values.
     */
    @Setter public void load_inputs(int[] i_vector)
    {
        set_inputs(i_vector);
    }

//    /**
//     * DEPRECIATED.
//     *
//     * Generates the transition table (weight matrix). Currently, assigns weights of -2 for each
//     * neuron pair, excluding self connections, which are instead assigned values of 0.
//     *
//     * Automatically applies the calculated weights to the transition_table class variable for
//     * each connection from neuron i -> neuron j.
//     *
//     * TODO: Depreciated. Remove and allow for transition table pass-through from Main file.
//     */
//    private void gen_trans_table()
//    {
//        for (int i = 0; i < n; i++)
//        {
//            for (int j = 0; j < n; j++)
//            {
//                if (i == j)
//                    transition_table[i][j] = 0;
//                else
//                    transition_table[i][j] -= 2;
//            }
//        }
//    }

    /**
     * Generates the initial activation level for each neuron - calculated by taking (2kα/n)-α.
     *
     * @param percent: Decimal value of percent allowed perturbation. E.g./ 0.03 for 3% perturbation.
     */
    private void init_activation(double percent)
    {
        double rand;
        for (int i = 0; i < n; i++)
        {
            rand = ThreadLocalRandom.current().nextDouble(1 - percent, 1 + percent);
            activation[i] = rand * ((2 * alpha) * (k[category[i]] / n) - alpha);
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

    /**
     * Shuffles an array using the Fisher-Yates shuffling method - an optimally efficient shuffling
     *    algorithm returning unbiased results when paired with an unbiased random number generator.
     */
    private int[] shuffle(int[] array) {
        int temp;

        for (int i = n - 1; i > 0; i--) {
            int index = ThreadLocalRandom.current().nextInt(0, i);
            temp = array[i];
            array[i] = array[index];
            array[index] = temp;
        }

        return array;
    }
}
