package LAB1;

public class Main {
    public static void main(String[] args) {
        //ex1
       if(args.length != 2) {
           System.out.println("Insufficient arguments");
           return;
       }
       int n = Integer.parseInt(args[0]);
       int k = Integer.parseInt(args[1]);

       //ex 2
       int[][] adjacencyMatrix = createAdjacencyMatrix(n,k);

       //ex3
       System.out.println("Matrix is: ");
       printMatrix(adjacencyMatrix);

       //ex4
        int edges_number = printEdgesNumber(adjacencyMatrix);
        System.out.println("Number of edges: " + edges_number);

        //ex5
        printMaxAndMinDegree(adjacencyMatrix);

        //ex6
        verifyDegrees(adjacencyMatrix,edges_number);


    }

    private static void verifyDegrees(int[][] matrix,int edges_number){
        int degree_number=0;
        for(int[] row:matrix)
            for(int cell:row)
                degree_number +=cell;

        if(degree_number == 2*edges_number)
            System.out.println("The sum of degrees is equal to 2*m");
        else System.out.println("The sum of degrees IS NOT EQUAL to 2 *m");

    }

    private static void printMaxAndMinDegree(int[][] matrix) {
        int maxDegree = 0;
        int minDegree = Integer.MAX_VALUE;
        for(int[] row: matrix)
        {
            int degree = 0;
            for(int cell: row)
                degree += cell;
            if(degree > maxDegree)
                maxDegree = degree;
            if(degree < minDegree)
                minDegree = degree;
        }
        System.out.println("Max degree ( Δ(G) ) : " + maxDegree);
        System.out.println("Min degree ( δ(G) ) : " + minDegree);

    }

    private static int printEdgesNumber(int[][] matrix) {
        int edges = 0;
        for(int i = 0; i < matrix.length; i++)
            for(int j = i + 1; j < matrix.length; j++)
                edges += matrix[i][j];
        return edges;

    }

    private static void printMatrix(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        sb.append("\u2B1C for 1 and \u2B1B for 0\n\n");
        for (int[] row : matrix) {
            for (int cell : row) {
                sb.append(cell == 1 ? "\u2B1C " : "\u2B1B ");
            }
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }

    private static int[][] createAdjacencyMatrix(int n,int k) {
        int[][] matrix = new int[n][n];

        // Create a clique of size k
        for (int i = 0; i < k; i++) {
            for (int j = i + 1; j < k; j++) {
                matrix[i][j] = 1;
                matrix[j][i] = 1;
            }
        }

        // Create a stable set of size k
        for (int i = k; i < 2 * k; i++) {
            for (int j = k; j < 2 * k; j++) {
                matrix[i][j] = 0;
            }
        }

        // Fill the rest of the matrix randomly
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (matrix[i][j] == 0 && matrix[j][i] == 0) {
                    matrix[i][j] = Math.random() < 0.5 ? 0 : 1;
                    matrix[j][i] = matrix[i][j];
                }
            }
        }

        return matrix;
    }
}