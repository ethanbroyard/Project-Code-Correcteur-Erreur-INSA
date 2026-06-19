import java.util.*;
import java.io.*;

public class Matrix {
    private byte[][] data = null;
    private int rows = 0, cols = 0;

    // ---------------------------------------------------------------
    // Constructeurs
    // ---------------------------------------------------------------

    public Matrix(int r, int c) {
        data = new byte[r][c];
        rows = r;
        cols = c;
    }

    public Matrix(byte[][] tab) {
        rows = tab.length;
        cols = tab[0].length;
        data = new byte[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                data[i][j] = tab[i][j];
    }

    // ---------------------------------------------------------------
    // Accesseurs / Mutateurs
    // ---------------------------------------------------------------

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public byte getElem(int i, int j) { return data[i][j]; }

    public void setElem(int i, int j, byte b) { data[i][j] = b; }

    // ---------------------------------------------------------------
    // Méthodes d'origine (fournies)
    // ---------------------------------------------------------------

    public boolean isEqualTo(Matrix m) {
        if ((rows != m.rows) || (cols != m.cols))
            return false;
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                if (data[i][j] != m.data[i][j])
                    return false;
        return true;
    }

    public void shiftRow(int a, int b) {
        byte tmp;
        for (int i = 0; i < cols; i++) {
            tmp = data[a][i];
            data[a][i] = data[b][i];
            data[b][i] = tmp;
        }
    }

    public void shiftCol(int a, int b) {
        byte tmp;
        for (int i = 0; i < rows; i++) {
            tmp = data[i][a];
            data[i][a] = data[i][b];
            data[i][b] = tmp;
        }
    }

    public void display() {
        System.out.print("[");
        for (int i = 0; i < rows; i++) {
            if (i != 0) System.out.print(" ");
            System.out.print("[");
            for (int j = 0; j < cols; j++) {
                System.out.printf("%d", data[i][j]);
                if (j != cols - 1) System.out.print(" ");
            }
            System.out.print("]");
            if (i == rows - 1) System.out.print("]");
            System.out.println();
        }
        System.out.println();
    }

    public Matrix transpose() {
        Matrix result = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                result.data[j][i] = data[i][j];
        return result;
    }

    public Matrix add(Matrix m) {
        if ((m.rows != rows) || (m.cols != cols)) {
            System.out.println("Erreur d'addition : dimensions incompatibles");
            return null;
        }
        Matrix r = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                r.data[i][j] = (byte) ((data[i][j] + m.data[i][j]) % 2);
        return r;
    }

    public Matrix multiply(Matrix m) {
        if (m.rows != cols) {
            System.out.println("Erreur de multiplication : dimensions incompatibles");
            return null;
        }
        Matrix r = new Matrix(rows, m.cols);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < m.cols; j++) {
                r.data[i][j] = 0;
                for (int k = 0; k < cols; k++)
                    r.data[i][j] = (byte) ((r.data[i][j] + data[i][k] * m.data[k][j]) % 2);
            }
        return r;
    }

    // ---------------------------------------------------------------
    // Exercice 3 : addRow / addCol
    // ---------------------------------------------------------------

    /**
     * Additionne (XOR) la ligne a à la ligne b (modifie la ligne b).
     */
    public void addRow(int a, int b) {
        for (int j = 0; j < cols; j++)
            data[b][j] = (byte) ((data[b][j] + data[a][j]) % 2);
    }

    /**
     * Additionne (XOR) la colonne a à la colonne b (modifie la colonne b).
     */
    public void addCol(int a, int b) {
        for (int i = 0; i < rows; i++)
            data[i][b] = (byte) ((data[i][b] + data[i][a]) % 2);
    }

    // ---------------------------------------------------------------
    // Exercice 4 : sysTransform — pivot de Gauss pour obtenir H' = (M|Id)
    // ---------------------------------------------------------------

    /**
     * Transforme H = (L|R) en H' = (M|Id) par pivot de Gauss sur les lignes.
     * H doit être de la forme (L|R) avec R inversible (les r dernières colonnes).
     * Renvoie la matrice sous forme systématique (sans modifier this).
     */
    public Matrix sysTransform() {
        int r = rows;
        int c = cols;
        // Copie de travail
        Matrix h = new Matrix(data);

        // La partie R est dans les colonnes [c-r .. c-1]
        int offset = c - r;

        for (int col = 0; col < r; col++) {
            // --- Chercher un pivot dans la colonne (offset+col) à partir de la ligne col ---
            int pivot = -1;
            for (int row = col; row < r; row++) {
                if (h.data[row][offset + col] == 1) {
                    pivot = row;
                    break;
                }
            }
            if (pivot == -1) {
                System.out.println("Erreur sysTransform : pas de pivot en colonne " + (offset + col));
                return null;
            }
            // Amener le pivot sur la diagonale
            if (pivot != col)
                h.shiftRow(pivot, col);

            // Éliminer les 1 dans toutes les autres lignes de cette colonne
            for (int row = 0; row < r; row++) {
                if (row != col && h.data[row][offset + col] == 1)
                    h.addRow(col, row);
            }
        }
        return h;
    }

    // ---------------------------------------------------------------
    // Exercice 5 : genG — matrice génératrice G = (Id | M^t)
    // ---------------------------------------------------------------

    /**
     * Construit la matrice génératrice G = (Id_k | M^t) à partir de H' = (M | Id_r).
     * this doit être sous la forme systématique H' = (M | Id_r).
     */
    public Matrix genG() {
        int r = rows;          // nombre de lignes de H' = redondance
        int n = cols;          // longueur des mots de code
        int k = n - r;         // dimension du code (longueur du mot source)

        // Extraire M : les k premières colonnes de H'
        Matrix M = new Matrix(r, k);
        for (int i = 0; i < r; i++)
            for (int j = 0; j < k; j++)
                M.data[i][j] = data[i][j];

        // M^t : transposée de M (dimensions k x r)
        Matrix Mt = M.transpose();

        // G = (Id_k | M^t) : matrice k x n
        Matrix G = new Matrix(k, n);
        // Partie identité (k premières colonnes)
        for (int i = 0; i < k; i++)
            G.data[i][i] = 1;
        // Partie M^t (colonnes k..n-1)
        for (int i = 0; i < k; i++)
            for (int j = 0; j < r; j++)
                G.data[i][k + j] = Mt.data[i][j];

        return G;
    }

    // ---------------------------------------------------------------
    // Exercice 12 : errGen — vecteur d'erreur de poids w
    // ---------------------------------------------------------------

    /**
     * Génère aléatoirement un vecteur ligne (1 x cols) de poids w.
     */
    public Matrix errGen(int w) {
        Random rand = new Random();
        Matrix e = new Matrix(1, cols);
        int count = 0;
        while (count < w) {
            int idx = rand.nextInt(cols);
            if (e.data[0][idx] == 0) {
                e.data[0][idx] = 1;
                count++;
            }
        }
        return e;
    }
}
