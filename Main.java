import java.util.*;
import java.io.*;

public class Main {

    public static Matrix loadMatrix(String file, int r, int c) {
        byte[] tmp = new byte[r * c];
        byte[][] data = new byte[r][c];
        try {
            FileInputStream fos = new FileInputStream(file);
            fos.read(tmp);
            fos.close();
        } catch (IOException e) { e.printStackTrace(); }
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                data[i][j] = tmp[i * c + j];
        return new Matrix(data);
    }

    // Affiche un syndrome (colonne) transposé en ligne : [[0 0 0 ... 0]]
    public static void printSyndromeLigne(Matrix s) {
        System.out.print("[[");
        for (int i = 0; i < s.getRows(); i++) {
            if (i > 0) System.out.print(" ");
            System.out.print(s.getElem(i, 0));
        }
        System.out.println("]]");
        System.out.println();
    }

    public static void main(String[] arg) {
        
                // ============================================================
        // EXERCICE 3 : test de addRow / addCol
        // (bloc de test isolé, ne fait pas partie du format de sortie validé)
        // ============================================================
 
        Matrix hAddTest = loadMatrix("data/matrix-15-20-3-4", 15, 20);
        System.out.println("=== Exercice 3 : test addRow / addCol sur matrix-15-20-3-4 ===");
        System.out.println();
 
        System.out.println("Matrice avant modification :");
        hAddTest.display();
 
        System.out.println("Ligne 0 : ");
        for (int j = 0; j < 20; j++) System.out.print(hAddTest.getElem(0, j));
        System.out.println();
        System.out.println("Ligne 1 (avant) : ");
        for (int j = 0; j < 20; j++) System.out.print(hAddTest.getElem(1, j));
        System.out.println();
 
        hAddTest.addRow(0, 1); // ajoute la ligne 0 à la ligne 1
        System.out.println("Ligne 1 (apres addRow(0,1)) : ");
        for (int j = 0; j < 20; j++) System.out.print(hAddTest.getElem(1, j));
        System.out.println("\n");
 
        System.out.println("Colonne 0 (avant) : ");
        for (int i = 0; i < 15; i++) System.out.print(hAddTest.getElem(i, 0));
        System.out.println();
        System.out.println("Colonne 2 (avant) : ");
        for (int i = 0; i < 15; i++) System.out.print(hAddTest.getElem(i, 2));
        System.out.println();
 
        hAddTest.addCol(0, 2); // ajoute la colonne 0 à la colonne 2
        System.out.println("Colonne 2 (apres addCol(0,2)) : ");
        for (int i = 0; i < 15; i++) System.out.print(hAddTest.getElem(i, 2));
        System.out.println();
 
        System.out.println("Matrice complete apres addRow(0,1) et addCol(0,2) :");
        hAddTest.display();
 
        System.out.println("=================================================");
        System.out.println();

        
        // ============================================================
        // TÂCHE 1 : ENCODAGE
        // ============================================================

        Matrix h15 = loadMatrix("data/matrix-15-20-3-4", 15, 20);

        System.out.println("Matrice de controle H :");
        System.out.println();
        h15.display();

        Matrix hSys = h15.sysTransform();
        System.out.println("Forme systematique de H :");
        System.out.println();
        hSys.display();

        Matrix G = hSys.genG();
        System.out.println("Matrice generatrice G :");
        System.out.println();
        G.display();

        byte[][] uTab = {{1,0,1,0,1}};
        Matrix u = new Matrix(uTab);
        System.out.println("Mot binaire u:");
        System.out.println();
        u.display();

        Matrix x = u.multiply(G);
        System.out.println("Encodage de u (x=u.G) :");
        System.out.println();
        x.display();

        Matrix syndX = hSys.multiply(x.transpose());
        System.out.println("Syndrome de x (s=H.x^t) :");
        System.out.println();
        printSyndromeLigne(syndX);

        // // ============================================================
        // // TÂCHE 2 : DÉCODAGE
        // // ============================================================

        // System.out.println("Graphe de Tanner :");
        // System.out.println();
        // TGraph tg = new TGraph(h15, 3, 4);
        // tg.display();
        // System.out.println();

        // System.out.println("-------------------------------------");
        // System.out.println("--Bruitage et correction du mot x :");
        // System.out.println("-------------------------------------");
        // System.out.println();

        // // Vecteurs d'erreur
        // byte[][] e1Tab = {{0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0}};
        // byte[][] e2Tab = {{0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}};
        // byte[][] e3Tab = {{0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0}};
        // byte[][] e4Tab = {{0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0}};
        // Matrix[] errors = {
        //     new Matrix(e1Tab), new Matrix(e2Tab),
        //     new Matrix(e3Tab), new Matrix(e4Tab)
        // };
        // String[] names = {"e1","e2","e3","e4"};
        // int rounds = 100;

        // for (int idx = 0; idx < 4; idx++) {
        //     System.out.println("Mot de code x :");
        //     System.out.println();
        //     x.display();

        //     System.out.println("Veceur d'erreurs " + names[idx] + " :");
        //     System.out.println();
        //     errors[idx].display();

        //     Matrix y = x.add(errors[idx]);
        //     System.out.println("Mot de code bruite y" + (idx+1) + "=x+" + names[idx] + " :");
        //     System.out.println();
        //     y.display();

        //     Matrix synd = h15.multiply(y.transpose());
        //     System.out.println("Syndrome de y" + (idx+1) + " :");
        //     System.out.println();
        //     printSyndromeLigne(synd);

        //     TGraph tgDec = new TGraph(h15, 3, 4);
        //     Matrix result = tgDec.decode(y, rounds);

        //     System.out.println("Correction x" + (idx+1) + " de y" + (idx+1) + " :");
        //     System.out.println();
        //     result.display();

        //     System.out.println("x" + (idx+1) + " = x : " + result.isEqualTo(x));
        //     System.out.println();
        //     System.out.println("------------------------------------------------");
        //     System.out.println();
        // }
    }
}
