import java.util.*;
import java.io.*;

/**
 * Tâche 3 : évaluation des performances du code LDPC (2048 x 6144).
 * Exercices 10 à 13.
 */
public class Eval {

    public static Matrix loadMatrix(String file, int r, int c) {
        byte[] tmp = new byte[r * c];
        byte[][] data = new byte[r][c];
        try {
            FileInputStream fos = new FileInputStream(file);
            fos.read(tmp);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                data[i][j] = tmp[i * c + j];
        return new Matrix(data);
    }

    public static void main(String[] arg) throws Exception {

        // ============================================================
        // Exercice 10 : paramètres du code
        // ============================================================
        System.out.println("=== Exercice 10 : paramètres du code ===");
        int n_r = 2048, n_c = 6144;
        int wr = 15, wc = 5;
        int n = n_c;
        int r_red = n_r;  // redondance
        int k = n - r_red;
        double rate = (double) k / n;
        System.out.println("n (longueur du mot de code) = " + n);
        System.out.println("k (longueur du mot source)  = " + k);
        System.out.println("r (redondance)              = " + r_red);
        System.out.printf( "τ (rendement)               = %.4f%n", rate);

        double p1 = 0.02, p2 = 0.025;
        System.out.printf("Erreurs moyennes pour p=%.3f : %.1f bits%n", p1, p1 * n);
        System.out.printf("Erreurs moyennes pour p=%.3f : %.1f bits%n", p2, p2 * n);
        System.out.println();

        // ============================================================
        // Exercice 11 : encodage du mot u avec la grande matrice
        // ============================================================
        System.out.println("=== Exercice 11 : chargement H et encodage ===");
        System.out.println("Chargement de la matrice H (2048 x 6144)...");
        long t0 = System.currentTimeMillis();
        Matrix H = loadMatrix("data/Matrix-2048-6144-5-15", 2048, 6144);
        System.out.println("Chargé en " + (System.currentTimeMillis()-t0) + " ms");

        System.out.println("Calcul de H' (sysTransform)...");
        t0 = System.currentTimeMillis();
        Matrix Hsys = H.sysTransform();
        System.out.println("Fait en " + (System.currentTimeMillis()-t0) + " ms");

        System.out.println("Calcul de G (genG)...");
        t0 = System.currentTimeMillis();
        Matrix G = Hsys.genG();
        System.out.println("Fait en " + (System.currentTimeMillis()-t0) + " ms");

        // Mot u : 1 aux indices pairs, 0 sinon
        System.out.println("Génération de u et encodage...");
        Matrix u = new Matrix(1, k);
        for (int i = 0; i < k; i++)
            u.setElem(0, i, (byte)(i % 2 == 0 ? 1 : 0));
        t0 = System.currentTimeMillis();
        Matrix x = u.multiply(G);
        System.out.println("Encodage fait en " + (System.currentTimeMillis()-t0) + " ms");

        // Vérification syndrome
        Matrix syndX = Hsys.multiply(x.transpose());
        boolean syndOk = true;
        for (int i = 0; i < n_r; i++)
            if (syndX.getElem(i, 0) != 0) { syndOk = false; break; }
        System.out.println("Syndrome de x nul ? " + syndOk);

        System.out.println("Construction du graphe de Tanner...");
        t0 = System.currentTimeMillis();
        // On va reconstruire un TGraph frais à chaque décodage (nécessaire car decode modifie l'état)
        // Pour éviter de reconstruire G pour chaque test, on stocke juste x et H
        System.out.println("Fait en " + (System.currentTimeMillis()-t0) + " ms\n");

        // ============================================================
        // Exercice 13 : évaluation statistique
        // ============================================================
        System.out.println("=== Exercice 13 : évaluation statistique ===");
        int[] wValues = {122, 124, 134, 144, 154};
        int iterations = 10000; 
        int decodingRounds = 200;

        // Note : la construction du TGraph est faite une fois, mais decode() modifie right[][0]
        // => on doit créer un nouveau TGraph (ou le réinitialiser) à chaque décodage.
        // Pour la performance, on va créer le TGraph une seule fois et réinitialiser les bits.

        for (int w : wValues) {
            int success = 0, failure = 0, wrong = 0;
            System.out.printf("w = %d (p ≈ %.3f)... ", w, (double)w/n);
            System.out.flush();
            t0 = System.currentTimeMillis();

            for (int iter = 0; iter < iterations; iter++) {
                // Vecteur d'erreur de poids w
                Matrix e = x.errGen(w);
                Matrix y = x.add(e);

                // Nouveau TGraph à chaque fois pour repartir d'un état propre
                TGraph tg = new TGraph(H, wc, wr);
                Matrix decoded = tg.decode(y, decodingRounds);

                if (decoded.getElem(0, 0) == -1) {
                    failure++;
                } else if (decoded.isEqualTo(x)) {
                    success++;
                } else {
                    wrong++;
                }
            }

            long elapsed = System.currentTimeMillis() - t0;
            System.out.printf("(en %d ms)%n", elapsed);
            System.out.printf("  Succès  : %5d / %d = %.2f%%%n", success, iterations, 100.0*success/iterations);
            System.out.printf("  Échecs  : %5d / %d = %.2f%%%n", failure, iterations, 100.0*failure/iterations);
            System.out.printf("  Erreurs : %5d / %d = %.2f%%%n", wrong,   iterations, 100.0*wrong/iterations);
            System.out.println();
        }
    }
}
