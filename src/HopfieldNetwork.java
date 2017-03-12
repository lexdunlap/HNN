import java.util.ArrayList;
import java.util.Arrays;
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
        System.out.println("initial activation: " + Arrays.toString(activation));
//        System.out.println("Running:");
        run();
//        System.out.println("Printing outputs:");
//        System.out.println(Arrays.toString(output));

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
    private void run()
    {
        int[] convergence_count = new int[k.size()];
        boolean[] converged = new boolean[k.size()];
        int nonDigital;
        boolean finished = false;
        catalog.setDuringTest(this.output);
        System.out.println((int) Math.sqrt(n));
        double[][] out2D;

        while (!finished)
        {
            nonDigital = 0;
            firing_order = shuffle(firing_order);
//            System.out.println("activation: " + Arrays.toString(activation));
//            System.out.println("\n\nfiring_order: " + Arrays.toString(firing_order));
//            System.out.println(Arrays.toString(firing_order));

            for (int i = 0; i < convergence_count.length; i++)
                convergence_count[i] = 0;

            // Updates current neuron activation and output for each neuron in the firing order
            for(int index : firing_order) {
                update_neuron(index);
                neuron_activation(index);

//                System.out.println("output:\t\t " + Arrays.toString(output));
//                System.out.println("prev_output: " + Arrays.toString(prev_output));

//                if (output[index] == prev_output[index]) {
//                    for (int j = 0; j < category.get(index).size(); j++) {
//                        convergence_count[(int) category.get(index).get(j)]++;
//                    }
//                }
//                System.out.println(Arrays.toString(convergence_count));

                if ((output[index] * (1 - output[index])) >= epsilon)
                    nonDigital++;

                prev_output[index] = output[index];
            }

            out2D = genOutputMatrix();
//            converged = checkConvergence(out2D);
            converged = diagConvergence(out2D);
            finished = check_all_bool(converged);

            // TODO: Set convergence_count[i] == k[i] BUT currently this breaks convergence and makes it run forever.
//            System.out.println("Convergence:    " + Arrays.toString(convergence_count));
//            System.out.println("Digital States: " + nonDigital);
//            for (int i = 0; i < k.size(); i++)
//                converged[i] = ((convergence_count[i] == k.get(i)) && nonDigital);

//            System.out.println(Arrays.toString(converged));
        }
        catalog.setPostTest(this.getOuput());
    }

    /**
     * Generates a 2D array (matrix) from the calculated output array. This is done by splitting
     * up the output array, which is of length n, into sqrt(n) rows of sqrt(n) length. Effectively,
     * this breaks the 1D output vector after sqrt(n) elements have been seen, storing the first
     * sqrt(n) elements as the first row. Then, the same is done for all elements after the
     * breakpoint until all elements have been seen.
     *
     * @return out2D: A 2D array generated from the output array, having sqrt(n) rows and columns.
     */
    private double[][] genOutputMatrix() {
        double[][] out2D = new double[(int) Math.sqrt(n)][(int) Math.sqrt(n)];

        for (int i = 0; i < Math.sqrt(n); i++) {
            for (int j = 0; j < Math.sqrt(n); j++) {
                int cIndex = ((int) (i * Math.sqrt(n)) + j);
                out2D[i][j] = output[cIndex];
                System.out.println(Arrays.toString(output));
            }
            System.out.println(Arrays.toString(out2D[i]));
        }

        return out2D;
    }

    /**
     * Checks to see if the network has converged to a permutation matrix by summing all rows and
     * columns. If the sum for each row and column is equal to 1, then the network has converged.
     *
     * @param out2D: 2-dimensional array holding the output values for each neuron.
     * @return converged: an array holding boolean convergence values for each category.
     */
    private boolean[] checkConvergence(double[][] out2D) {
        boolean[] converged = new boolean[k.size()];

        for (int i = 0; i < Math.sqrt(n); i++) {
            int rowSum = 0, colSum = 0;
            for (int j = 0; j < Math.sqrt(n); j++) {
                if (out2D[i][j] == 1)
                    rowSum++;
                if (out2D[j][i] == 1)
                    colSum++;
            }

//                System.out.printf("row sum: \t %d\ncol sum: \t %d\n", rowSum, colSum);

            converged[i] = rowSum == 1;
            converged[(int) (Math.sqrt(n)) + i] = colSum == 1;
        }
        return converged;
    }

    /**
     * Used for checking diagonal sums. Diagonal convergence is reached when at most one neuron
     * is active for a given diagonal category.
     *
     * @param out2D: Matrix of output values for all neurons in the network split into sqrt(n) rows.
     * @return
     */
    private boolean[] diagConvergence(double[][] out2D) {
        boolean[] converged = checkConvergence(out2D);          // Checks rows & columns
        boolean[] negDiag = new boolean[(int) (2 * Math.sqrt(n) - 1)];
        boolean[] posDiag = new boolean[(int) (2 * Math.sqrt(n) - 1)];
        System.out.println(negDiag.length);
        System.out.println(posDiag.length);

        // Negative diagonal sum
        int pCount = 0;
        System.out.println("Upper negative diagonal");
        for (int i = 0; i < Math.sqrt(n); i++) {
            int diagSum = 0;
            for (int j = i; j > 0; j--) {
                diagSum += out2D[i][(int) Math.sqrt(n) - j];
                System.out.println("i: " + i + "\tj: " + (Math.sqrt(n) - j));
            }
            if (diagSum <= 1)
                negDiag[pCount] = true;
            pCount++;
        }
        System.out.println("Lower negative diagonal");
        for (int i = 1; i < Math.sqrt(n); i++) {
            int diagSum = 0;
            for (int j = 0; j <= i; j++) {
                diagSum += out2D[i][j];
                System.out.println("i: " + i + "\tj: " + j);
            }
            if (diagSum <= 1)
                negDiag[pCount] = true;
            pCount++;
        }

        // Positive diagonal sum
        System.out.println("Upper positive diagonal");
        pCount = 0;
        for (int i = 0; i < Math.sqrt(n); i++) {
            int diagSum = 0;
            for (int j = 0; j < i; j++) {
                diagSum += out2D[i - j][j];
                System.out.println("i: " + (i - j) + "\tj: " + j);
            }
            if (diagSum <= 1)
                posDiag[pCount] = true;
            System.out.println(pCount);
            pCount++;
        }
        System.out.println("Lower positive diagonal");
        for (int i = 1; i < Math.sqrt(n); i++) {
            int diagSum = 0;
            for (int j = i; j < Math.sqrt(n); j++) {
                diagSum += out2D[(int) Math.sqrt(n) - j][j];
                System.out.println("i: " + (int) (Math.sqrt(n) - j) + "\tj: " + j);
            }
            if (diagSum <= 1)
                posDiag[pCount] = true;
            System.out.println(pCount);
            pCount++;
        }

        for (int i = 0; i <= (4 * n - 4 * Math.sqrt(n) + 1); i++) {
            converged[(int) (2 * Math.sqrt(n) - 1) + i] = posDiag[i] && negDiag[i];
        }

        return converged;
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
        {
            if (!value)
            {
//                System.out.println("FALSE");
                return false;
            }
        }
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
        double sum;
        for (int i = 0; i < n; i++)
        {
            sum = 0;
            rand = ThreadLocalRandom.current().nextDouble(1 - percent, 1 + percent);
            // TODO how to handle the initial activation for multiple sets?! Ahhh!
            for (int j = 0; j < category.get(j).size(); j++)
            {
                sum += rand * (((2 * alpha) * (k.get((int) category.get(i).get(j))) / n) - alpha);
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
