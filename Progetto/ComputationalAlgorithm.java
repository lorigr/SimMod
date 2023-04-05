import java.util.ArrayList;

public class ComputationalAlgorithm {

    static final int DELAY = 0;
    static final int LD = 1;
    static final int Z = 2500;

    public void MVA(int M, int N, double[] V, double[] S, int[] ST){
        // initalization
        double[][] n = new double[M+1][N+1];
        double[][] w = new double[M+1][N+1];
        double[][] x = new double[M+1][N+1];
        double[][] u = new double[M+1][N+1];

        for(int i = 1; i<=M; i++){
            n[i][0] = 0.0;
        }

        // compute the performance measures
        for(int k = 1; k<= N; k++){
            for(int i = 1; i<=M; i++){
                if(ST[i]==DELAY) w[i][k] = Z;
                else w[i][k] = S[i]*(1+n[i][k-1]);
            }    
            double sum = 0.0;
            for(int i = 1; i<=M; i++){
                sum = sum +V[i]*w[i][k];
            }
            double xRef = k/sum;
            for(int i = 1; i<=M; i++){
                x[i][k] = V[i]*xRef;
                if(ST[i]==DELAY){
                    n[i][k]=Z*x[i][k];
                    u[i][k]=n[i][k]/k; 
                }else{
                    u[i][k] = S[i]*x[i][k];
                    n[i][k] = u[i][k]*(1+n[i][k-1]);
                }
            }
        }
        printResults(n, w, x, u, M, N);
    }

    public void printResults(double[][] n, double[][] w, double[][] x, double[][] u, int M, int N){
        System.out.println("\n-------------------------------------------------------------------------------\n");
        for(int k = 1; k<=N; k++){
            System.out.println("\nSystem with "+k+" customers inside ***************************\n");
            for(int i= 1 ; i<=M; i++){
                System.out.println("\nStation number: "+i+" ++++++++++++");
                System.out.println("Average number in queue:        "+n[i][k]);
                System.out.println("Average waiting time:           "+w[i][k]);
                System.out.println("Average throughput:             "+x[i][k]);
                System.out.println("Average utilization:            "+ u[i][k]);
            }
        }
    }

    public static void main(String[] args) {

        ComputationalAlgorithm ca = new ComputationalAlgorithm();
        int M = 6;
        int N = 50;
        double[] V = {-1,1, 1, 0.28, 0.5, 0.4, 0.12};
        double[] S = {-1, Z, 5, 4, 50, 200, 500};
        int[] ST = {-1 ,DELAY, LD, LD, LD, LD, LD};

        ca.MVA(M, N, V, S, ST);
    }
}


