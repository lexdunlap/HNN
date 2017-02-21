import java.lang.Math;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Hopfield Network class.
 *
 * @author Michael J. Suggs
 * @version
 * @since SDK1.8
 * TODO: Fill out javadocs.
 */
public class HopfieldNetwork
{
    private int n;                          // k-neurons out of n total neurons
    private double alpha;                   // Programmer-defined alpha value
    private double tau;                     // Programmer-defined tau value
    private double step_size;               // Integration step size used in activation function
    private double epsilon;                 // Decimal-percent value for allowed deviation from binary convergence states
    private int[] input;                    // I-Vector
    private int[] firing_order;             // Shuffled to randomise order neurons are fired in
    private int[][] transition_table;       // T-Matrix
    private double[] activation;            // U-Vector
    private double[] output;                // V-Vector
    private double[] prev_output;           // V(t-1)-Vector
    private ArrayList<ArrayList> category;  // Keeps track of category for a given neuron
    private ArrayList<Integer> k;           // k-out-of-n. k-neurons out of n total neurons
    private Catalog catalog;

    /**
     * Creates Hopfield Network, setting all initial programmer-defined values. Also generates
     * the transition table weight matrix based on neuron connections, as well as generating
     * the initial values for the activation vector (u-vector).
     *
     * TODO: Is an input vector I even necessary (since I is just represented by 2k-1)? Confusion.
     *
     * @param k number of selected neurons out of total n
     * @param n total number of neurons
     * @param alpha activation threshold value for determining neuron response
     * @param input_vector
     * @param transition_table
     * @param step_size
     * @param category vector telling the category of each neuron - should be of length 'n'
     */
    public HopfieldNetwork(ArrayList<Integer> k, int n, double alpha, double epsilon, int[] input_vector,
                           int[][] transition_table, double step_size, ArrayList<ArrayList> category) throws java.io.IOException
    {
        catalog = new Catalog();
        catalog.setInputs(input_vector);
        catalog.setTMatrix(transition_table);

        this.k = k;
        this.n = n;
        this.step_size = step_size;
        this.alpha = alpha;
        tau = 2 * alpha;
        this.epsilon = epsilon;
        this.transition_table = transition_table;
        this.category = category;

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

        //print to catalog and close
        catalog.printToFile();
        catalog.closeFileWriter();
    }

    /**
     * Constructor for creating a single-set network.
     *
     * TODO: Combine overlapping code from constructors and split constructors at a more efficient breakpoint.
     *
     * @param k
     * @param n
     * @param alpha
     * @param input_vector
     * @param transition_table
     * @param step_size
     */
    public HopfieldNetwork(ArrayList<Integer> k, int n, double alpha, double epsilon,
                           int[] input_vector, int[][] transition_table, double step_size)
    {
        this.k = k;
        this.n = n;
        this.step_size = step_size;
        this.alpha = alpha;
        this.input = input_vector;
        tau = 2 * alpha;
        this.epsilon = epsilon;

        this.transition_table = transition_table;
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
     * TODO: Documentation!
     *
     * @param in
     */
    private void set_inputs(int[] in)
    {
        for (int i = 0; i < in.length; i++)
            input[i] = (2 * k.get(in[i])) - 1;
    }

    /**
     * Continually updates and calculates the activation of all neurons until convergence is reached.
     */
    public void run()
    {
        int[] convergence_count = new int[k.size()];
        boolean[] converged = new boolean[k.size()];
        boolean digital_states, finished = false;
        catalog.setDuringTest(this.output);

        for (int i = 0; i < k.size(); i++)
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

                if (output[index] == prev_output[index]) {
                    for (int j = 0; j < category.get(i).size(); j++) {
                        convergence_count[(int) category.get(i).get(j)]++;
                    }
                }

                if ((output[index] * (1 - output[index])) >= epsilon)
                    digital_states = false;

                prev_output[index] = output[index];
            }

            // TODO: Set convergence_count[i] == k[i] BUT currently this breaks convergence and makes it run forever.
            for (int i = 0; i < k.size(); i++)
                converged[i] = ((convergence_count[i] >= k.get(i)) && digital_states);

            finished = check_all_bool(converged);
        }
        catalog.setPostTest(this.getOuput());
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

    public ArrayList<Integer> getKValue() {
    	return k;
    }

    /**
     * Used for inputting an input vector into the inner input matrix of the
     * Hopfield Network class.
     *
     * @param i_vector row vector of input values.
     */
    public void load_inputs(int[] i_vector)
    {
        set_inputs(i_vector);
    }

    /**
     * Generates the initial activation level for each neuron - calculated by taking (2kα/n)-α.
     *
     * @param percent: Decimal value of percent allowed perturbation. E.g./ 0.03 for 3% perturbation.
     */
    private void init_activation(double percent)
    {
        double rand;
        int sum;
        for (int i = 0; i < n; i++)
        {
            sum = 0;
            rand = ThreadLocalRandom.current().nextDouble(1 - percent, 1 + percent);
            // TODO how to handle the initial activation for multiple sets?! Ahhh!
            for (int j = 0; j < category.get(j).size(); j++)
            {
                sum += rand * ((2 * alpha) * (k.get((int) category.get(i).get(j)) / n) - alpha);
            }
            activation[i] = sum;
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

//    /**
//     * @depreciated
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
}
