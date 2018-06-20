package methods;

abstract class EdgeMath {
    int truncate(int a) {
        if      (a <   0) return 0;
        else if (a > 255) return 255;
        else              return a;
    }

    int[][] rotateConv(int[][] conv, int dir) {
        dir = 8-dir;
        for (int i = dir;i>0 && i<8;i--){
            conv = rotateMatrix(conv);
        }

//        for (int i=0;i<3;i++){
//            for (int j=0;j<3;j++){
//                System.out.print(conv[i][j]+" ");
//            }
//            System.out.println();
//        }
        return conv;
    }

    private int[][] rotateMatrix(int[][] mat) {
        return rotateMatrix(3,3, mat);
    }

    private int[][] rotateMatrix(int m, int n, int[][] mat) {
        int row = 0, col = 0;
        int prev, curr;

        while (row < m && col < n)
        {
            if (row + 1 == m || col + 1 == n)
                break;

            prev = mat[row + 1][col];

            for (int i = col; i < n; i++)
            {
                curr = mat[row][i];
                mat[row][i] = prev;
                prev = curr;
            }
            row++;

            for (int i = row; i < m; i++)
            {
                curr = mat[i][n-1];
                mat[i][n-1] = prev;
                prev = curr;
            }
            n--;

            if (row < m)
            {
                for (int i = n-1; i >= col; i--)
                {
                    curr = mat[m-1][i];
                    mat[m-1][i] = prev;
                    prev = curr;
                }
            }
            m--;

            if (col < n)
            {
                for (int i = m-1; i >= row; i--)
                {
                    curr = mat[i][col];
                    mat[i][col] = prev;
                    prev = curr;
                }
            }
            col++;
        }

        return mat;
    }

    private double LoG(double sigma, int x, int y){
        return (-(1/(Math.PI*sigma*sigma*sigma*sigma))*(1-(x*x+y*y)/(2*sigma*sigma))*Math.pow(Math.E,-(x*x+y*y)/ (2*sigma*sigma)));
    }
    
    double[][] logKernel(double sigma, int size){
        double [][] kernel = new double[size][size];
        int pyj = size/2;
        for(int i=0-pyj;i<pyj+1;i++){
            for(int j=0-pyj;j<pyj+1;j++){
                kernel[i+pyj][j+pyj]=LoG(sigma, i, j);
            }
        }

//        for (int i=0;i<size;i++){
//            for (int j=0;j<size;j++){
//                System.out.print(kernel[i][j]+" ");
//            }
//            System.out.println();
//        }

        return kernel;
    }
    
    private double gaussian(double sigma, int x, int y){
        return ((1/(2*Math.PI*sigma*sigma)) * Math.pow(Math.E,-(x*x+y*y)/ (2*sigma*sigma)));
    }

    double[][] gaussianKernel(double sigma, int size){
        double [][] kernel = new double[size][size];
        int pyj = size/2;
            for(int i=0-pyj;i<pyj+1;i++){
                for(int j=0-pyj;j<pyj+1;j++){
                    kernel[i+pyj][j+pyj]=gaussian(sigma, i, j);
            }
        }

//        for (int i=0;i<size;i++){
//            for (int j=0;j<size;j++){
//                System.out.print(kernel[i][j]+" ");
//            }
//            System.out.println();
//        }

        return kernel;
    }

    double getKernelSum(double[][] kernel){
        double sum = 0;
        for(int j=0;j<kernel.length;++j){
            for(int i=0;i<kernel.length;++i){
                sum = sum + kernel[i][j];
            }
        }
        return sum;
    }
}
