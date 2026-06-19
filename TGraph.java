import java.util.*;

/**
 * Représente le graphe de Tanner associé à une matrice de contrôle H.
 *
 * - left  : tableau [n_r][w_r+1] — nœuds fonctionnels (lignes de H)
 *           left[i][0]   = valeur du nœud (toujours 0 à la construction)
 *           left[i][j]   = indice du j-ème voisin variable (j >= 1)
 *
 * - right : tableau [n_c][w_c+1] — nœuds variables (colonnes de H)
 *           right[i][0]  = valeur du bit associé au nœud variable i
 *           right[i][j]  = indice du j-ème voisin fonctionnel (j >= 1)
 */
public class TGraph {

    private int n_r;   // nombre de lignes de H  (nœuds fonctionnels)
    private int w_r;   // poids des lignes        (degré d'un nœud fonctionnel)
    private int n_c;   // nombre de colonnes de H (nœuds variables)
    private int w_c;   // poids des colonnes       (degré d'un nœud variable)

    private int[][] left;   // [n_r][w_r+1]
    private int[][] right;  // [n_c][w_c+1]

    // ---------------------------------------------------------------
    // Constructeur
    // ---------------------------------------------------------------

    /**
     * Construit un TGraph à partir de la matrice de contrôle H.
     * @param H  matrice de contrôle régulière
     * @param wc poids des colonnes de H
     * @param wr poids des lignes de H
     */
    public TGraph(Matrix H, int wc, int wr) {
        this.n_r = H.getRows();
        this.w_r = wr;
        this.n_c = H.getCols();
        this.w_c = wc;

        left  = new int[n_r][w_r + 1];
        right = new int[n_c][w_c + 1];

        // Initialisation des valeurs des nœuds à 0
        for (int i = 0; i < n_r; i++) left[i][0]  = 0;
        for (int i = 0; i < n_c; i++) right[i][0] = 0;

        // Remplissage de left : pour chaque ligne i, lister les colonnes j où H[i][j]==1
        int[] leftCount = new int[n_r];
        // Remplissage de right : pour chaque colonne j, lister les lignes i où H[i][j]==1
        int[] rightCount = new int[n_c];

        for (int i = 0; i < n_r; i++) {
            for (int j = 0; j < n_c; j++) {
                if (H.getElem(i, j) == 1) {
                    leftCount[i]++;
                    left[i][leftCount[i]] = j;   // voisin variable j pour le nœud fonctionnel i
                    rightCount[j]++;
                    right[j][rightCount[j]] = i; // voisin fonctionnel i pour le nœud variable j
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Exercice 7 : affichage
    // ---------------------------------------------------------------

    /**
     * Affiche le graphe de Tanner : right (nœuds variables) et left (nœuds fonctionnels)
     * côte à côte, ligne par ligne, comme dans le sujet du prof.
     * Format : "  i : val| v1 v2 v3        val| f1 f2 f3 f4"
     */
    public void display() {
        // On affiche ligne par ligne : pour chaque indice i de 0 à max(n_c, n_r)-1
        // colonne gauche = right[i] si i < n_c, colonne droite = left[i] si i < n_r
        int maxLines = Math.max(n_r, n_c);
        for (int i = 0; i < maxLines; i++) {
            System.out.printf("%3d : ", i);

            // Colonne gauche : nœud variable right[i]
            if (i < n_c) {
                System.out.printf("%d|", right[i][0]);
                for (int j = 1; j <= w_c; j++)
                    System.out.printf("%3d", right[i][j]);
            }

            // Espacement entre les deux colonnes
            System.out.print("        ");

            // Colonne droite : nœud fonctionnel left[i]
            if (i < n_r) {
                System.out.printf("%d|", left[i][0]);
                for (int j = 1; j <= w_r; j++)
                    System.out.printf("%3d", left[i][j]);
            }

            System.out.println();
        }
    }

    // ---------------------------------------------------------------
    // Exercice 8 : décodage par renversement de bits (bit-flipping)
    // ---------------------------------------------------------------

    /**
     * Calcule le syndrome du mot courant stocké dans right[][0].
     * Renvoie un tableau de n_r valeurs (0 = contrainte satisfaite, 1 = non satisfaite).
     */
    private int[] computeSyndrome() {
        int[] s = new int[n_r];
        for (int i = 0; i < n_r; i++) {
            int sum = 0;
            for (int j = 1; j <= w_r; j++)
                sum += right[left[i][j]][0];
            s[i] = sum % 2;
        }
        return s;
    }

    /**
     * Algorithme de décodage par renversement de bits.
     * @param code  mot reçu (matrice ligne 1 x n_c)
     * @param rounds nombre maximal d'itérations
     * @return mot corrigé, ou vecteur de -1 si échec
     */
    public Matrix decode(Matrix code, int rounds) {
        // Charger le mot reçu dans right[][0]
        for (int i = 0; i < n_c; i++)
            right[i][0] = code.getElem(0, i);

        for (int iter = 0; iter < rounds; iter++) {
            int[] s = computeSyndrome();

            // Vérifier si le syndrome est nul (décodage réussi)
            boolean success = true;
            for (int i = 0; i < n_r; i++) {
                if (s[i] != 0) { success = false; break; }
            }
            if (success) {
                // Construire et renvoyer le mot corrigé
                Matrix result = new Matrix(1, n_c);
                for (int i = 0; i < n_c; i++)
                    result.setElem(0, i, (byte) right[i][0]);
                return result;
            }

            // Pour chaque nœud variable, compter le nombre de voisins fonctionnels
            // dont la contrainte est violée
            int maxViol = -1;
            for (int i = 0; i < n_c; i++) {
                int viol = 0;
                for (int j = 1; j <= w_c; j++)
                    viol += s[right[i][j]];
                if (viol > maxViol) maxViol = viol;
            }

            // Renverser tous les bits dont le nombre de violations est maximal
            if (maxViol == 0) break; // aucun bit à renverser => échec
            for (int i = 0; i < n_c; i++) {
                int viol = 0;
                for (int j = 1; j <= w_c; j++)
                    viol += s[right[i][j]];
                if (viol == maxViol)
                    right[i][0] ^= 1;
            }
        }

        // Échec : renvoyer vecteur de -1
        Matrix failure = new Matrix(1, n_c);
        for (int i = 0; i < n_c; i++)
            failure.setElem(0, i, (byte) -1);
        return failure;
    }

    // ---------------------------------------------------------------
    // Calcul du syndrome d'un mot externe (sans modifier le graphe)
    // ---------------------------------------------------------------

    /**
     * Calcule le syndrome H * y^t pour un mot y passé en argument.
     * Utile pour l'exercice 9 sans perturber l'état interne du graphe.
     */
    public Matrix syndrome(Matrix y, Matrix H) {
        return H.multiply(y.transpose());
    }
}
