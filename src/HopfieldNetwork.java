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
    private int nonSlack;
    private int sqrt;                       // Math.sqrt(n)
    private int nonSlackSqrt;
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
//    private Catalog catalog;

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
                           int[][] transition_table, double step_size, ArrayList<ArrayList> category,
                           int numSlack) throws java.io.IOException
    {
//        catalog = new Catalog();
//        catalog.setInputs(input_vector);
//        catalog.setTMatrix(transition_table);

        this.k = k;
        this.n = n;
        this.nonSlack = n - numSlack;
        sqrt = (int) Math.sqrt(n);
        nonSlackSqrt = (int) Math.sqrt(nonSlack);
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
        run();

        //print to catalog and close
//        catalog.printToFile();
//        catalog.closeFileWriter();
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
//        catalog.setDuringTest(this.output);
//        System.out.println(sqrt);
        double[][] out2D;

        while (!finished)
        {
            nonDigital = 0;
            firing_order = shuffle(firing_order);

            for (int i = 0; i < convergence_count.length; i++)
                convergence_count[i] = 0;

            // Updates current neuron activation and output for each neuron in the firing order
            for(int index : firing_order) {
                update_neuron(index);
                neuron_activation(index);

                if ((output[index] * (1 - output[index])) >= epsilon)
                    nonDigital++;

                prev_output[index] = output[index];
            }

            out2D = genOutputMatrix();
//            converged = checkConvergence(out2D);
            converged = diagConvergence(converged, out2D);
            finished = check_all_bool(converged);
        }
//        catalog.setPostTest(this.getOuput());
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
        /*TODO fix convergence checking reliance on output 2d
         */
        double[][] out2D = new double[nonSlackSqrt][nonSlackSqrt];

        for (int i = 0; i < nonSlackSqrt; i++) {
            for (int j = 0; j < nonSlackSqrt; j++) {
                int cIndex = (i * nonSlackSqrt + j);
                out2D[i][j] = output[cIndex];
//                System.out.println(Arrays.toString(output));
            }
            System.out.println(Arrays.toString(out2D[i]));
        }
        System.out.print("\n");

        return out2D;
    }

    /**
     * Checks to see if the network has converged to a permutation matrix by summing all rows and
     * columns. If the sum for each row and column is equal to 1, then the network has converged.
     *
     * @param out2D: 2-dimensional array holding the output values for each neuron.
     * @return converged: an array holding boolean convergence values for each category.
     */
    private boolean[] checkConvergence(boolean[] converged, double[][] out2D) {
        for (int i = 0; i < nonSlackSqrt; i++) {
            int rowSum = 0, colSum = 0;
            for (int j = 0; j < nonSlackSqrt; j++) {
                if (out2D[i][j] == 1)
                    rowSum++;
                if (out2D[j][i] == 1)
                    colSum++;
            }
            System.out.printf("rowSum:\t%d\ncolSum:\t%d\n", rowSum, colSum);
            converged[i] = rowSum == 1;
            converged[nonSlackSqrt + i] = colSum == 1;
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
    private boolean[] diagConvergence(boolean[] converged, double[][] out2D) {
        converged = checkConvergence(converged, out2D);
        boolean[] negDiag = new boolean[2 * nonSlackSqrt - 1];
        boolean[] posDiag = new boolean[2 * nonSlackSqrt - 1];

        // Negative diagonal sum
        // TODO: make into while (pCount < sqrt)
        int pCount = 0;
        // bottom left (inclusive) to main diagonal (non-inclusive)
        for (int i = nonSlackSqrt - 1; i > 0; i--) {
            int diagSum = 0;
            for (int j = nonSlackSqrt - 1; (j - i) >= 0; j--)
                diagSum += out2D[j][j - i];
            if (diagSum <= 1)
                negDiag[pCount] = true;
            pCount++;
            
        }
        // main diagonal (inclusive) to top right (inclusive)
        for (int i = 0; i < nonSlackSqrt; i++) {
            int diagSum = 0;
            for (int j = nonSlackSqrt - 1; (j - i) >= 0; j--)
                diagSum += out2D[j - i][j];
            if (diagSum <= 1)
                negDiag[pCount] = true;
            pCount++;
        }

        // Positive diagonal sum
        for (int i = 0; i < 2 * nonSlackSqrt - 1; i++) {
            int sum = 0;
            for (int j = 0; j < nonSlackSqrt; j++) {
                for (int k = 0; k < nonSlackSqrt; k++) {
                    if (i == (j + k))
                        sum += out2D[j][k];
                }
            }
            if (sum <= 1)
                posDiag[i] = true;
        }

        /* Checking convergence of each by comparing the sets that intersect
           For sqrt(n) = 4:
            neg(S3)  intersects  pos(S3)
            neg(S2)  intersects  pos(S2, S4)
            neg(S1)  intersects  pos(S1, S3, S5)
            neg(S0)  intersects  pos(S0, S2, S4, S6)
            neg(S-1) intersects  pos(S1, S3, S5)
            neg(S-2) intersects  pos(S2, S4)
            neg(S-3) intersects  pos(S3)
         */
        // TODO: One issue is in this convergence checking - doesn't recognise feasible solutions.
//        int midpt = posDiag.length / 2;
//        int upIndex = 1;
//        System.out.println("Beginning convergence checks.\n" +
//                "categories =\t" + k.size() + "\n" + "converged =\t" + converged.length + "\n" +
//                "converged =\t" + Arrays.toString(converged) + "\n" +
//                "negDiag =\t" + Arrays.toString(negDiag) + "\n" +
//                "posDiag =\t" + Arrays.toString(posDiag) + "\n");
//        for (int i = 0; i < negDiag.length; i++) {
//            boolean diagConv = negDiag[i];
//            System.out.printf("negDiag%d:\t%b\n",i,negDiag[i]);
//            if (diagConv && (i < nonSlackSqrt)) {
//                for (int j = midpt - i; j <= midpt + i; j += 2) {
//                    if (!posDiag[j]) {
//                        System.out.printf("posDiag[%d]:\t%b\t\tBREAKING\n",j,posDiag[j]);
//                        diagConv = false;
//                        break;
//                    }
//                    System.out.printf("posDiag[%d]:\t%b\n",j,posDiag[j]);
//                }
//            } else if (diagConv && (i >= nonSlackSqrt)) {
//                for (int j = upIndex; j <= posDiag.length - upIndex; j += 2) {
//                    System.out.printf("upIndex:\t%d\nrange:\t(%d, %d)\n",upIndex,upIndex,posDiag.length - upIndex);
//                    if (!posDiag[j]) {
//                        System.out.printf("posDiag[%d]:\t%b\t\tBREAKING\n",j,posDiag[j]);
//                        diagConv = false;
//                        break;
//                    }
//                    System.out.printf("posDiag[%d]:\t%b\n",j,posDiag[j]);
//                }
//                upIndex++;
//            }
//            System.out.printf("Setting category %d to %b\n", (nonSlack - 1 + i), diagConv);
//            converged[(2*nonSlackSqrt) + i] = diagConv;
//            System.out.println(Arrays.toString(converged));
//        }
        for (int i = 0; i < negDiag.length; i++) {
            converged[2 * nonSlackSqrt + i] = negDiag[i];
            converged[(2 * nonSlackSqrt) + (2 * nonSlackSqrt - 1) + i] = posDiag[i];
        }
        System.out.println(Arrays.toString(converged));
        return converged;
    }

    /**
     * Checks all boolean values in an array. Returns false on the first false element found. Returns true
     *    if all elements are true in the entire array.
     *
     * @param bool_array: Array of boolean values to check.
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
