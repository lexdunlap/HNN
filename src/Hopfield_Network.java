import java.lang.Math;

/**
 * Created by Michael on 13/12/16.
 */
public class Hopfield_Network
{

    private int k, n;                               // k-neurons out of n total neurons
    private int[] input, output;                    // I-Vector ; V-Vector
    private int[][] transition_table;               // T-Matrix

    private double alpha, tau, step_size;           // Programmer-defined alpha value
    private double[] activation;                    // U-Vector

    /**
     *
     *
     * @param k number of selected neurons out of total n
     * @param n total number of neurons
     * @param alpha
     * @param num_inputs
     */
    public Hopfield_Network(int k, int n, double alpha, int num_inputs)
    {
        this.k = k;
        this.n = n;
        this.alpha = alpha;
        tau = 2 * alpha;
        input = new int[num_inputs];
        gen_trans_table();
    }

    /**
     * Generates the transition table (weight matrix).
     *
     */
    private void gen_trans_table()
    {
        transition_table = new int[n][n];

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
     *
     */
    private void k_out_of_n()
    {
    }

    /**
     * Calculates the dynamical system formula for a Hopfield Network detailing the change in activation
     * energy for a specific neuron against a change in time.
     *
     * @param index of neuron to be updated
     */
    private void update_neurons(int index)
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
     * @return
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

    private double neuron_activation(double u)
    {
        if(u >= this.alpha)
            return 1.0;
        else if (Math.abs(u) <= this.alpha)
            return ((u + this.alpha) / (2 * this.alpha));
        else if (u <= -1 * this.alpha)
            return 0.0;
        else
            return -1.0;
    }

}
