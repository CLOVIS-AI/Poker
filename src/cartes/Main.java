/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cartes;

/**
 * Une main de poker.
 * @author Ivan CANET
 * @since 3 avr. 2017
 */
public class Main implements Comparable<Main> {
    
    private Combinaison combinaison;
    private Carte rang;
    private Carte secondaire;
    
    private float valeur;
    
    public Main(Combinaison c, Carte r, Carte s){
        combinaison = c;
        rang = r;
        secondaire = s;
        switch(combinaison){
            case FULL:
                if(s == null)
                    throw new IllegalArgumentException("'s' ne devrait pas être 'null' dans ce cas !");
            case HAUTE: case PAIRE: case BRELAN: case QUINTE: case COULEUR:
            case CARRE: case QUINTE_FLUSH_ROYALE: case QUINTE_FLUSH: case DEUX_PAIRES:
                if(r == null)
                    throw new IllegalArgumentException("'r' ne devrait pas être 'null' dans ce cas !");
                break;
            default:
                throw new IllegalArgumentException("Cas impossible.");
        }
        
        valeur = 0;
        switch(combinaison){
            case QUINTE_FLUSH_ROYALE:           valeur += 100;
            case QUINTE_FLUSH:                  valeur += 100;
            case CARRE:                         valeur += 100;
            case FULL:                          valeur += 100;
            case COULEUR:                       valeur += 100;
            case QUINTE:                        valeur += 100;
            case BRELAN:                        valeur += 100;
            case DEUX_PAIRES:                   valeur += 100;
            case PAIRE:                         valeur += 100;
        }
        valeur += rang.getValeurAs();
        if(combinaison == Combinaison.FULL)
            valeur += secondaire.getValeurAs()/100f;
    }
    
    public final float getValeur(){
        return valeur;
    }

    @Override
    public int compareTo(Main o) {
        return (int) (this.getValeur() - o.getValeur());
    }
    
    @Override
    public String toString(){
        switch(combinaison){
            case QUINTE_FLUSH_ROYALE:   return "Quinte Flush Royale";
            case QUINTE_FLUSH:          return "Quinte Flush aux " + rang.toString();
            case CARRE:                 return "Carré de " + rang.valeurToString();
            case FULL:                  return "Full aux " + rang.valeurToString() + " par les " + secondaire.valeurToString();
            case COULEUR:               return "Couleur : " + rang.couleurToString();
            case QUINTE:                return "Quinte de " + rang.valeurToString();
            case BRELAN:                return "Brelan de " + rang.valeurToString();
            case DEUX_PAIRES:           return "Deux paires, " + rang.valeurToString();
            case PAIRE:                 return "Paire de " + rang.valeurToString();
            case HAUTE:                 return "Carte Haute : " + rang.toString();
            default:                    return "Impossible";
        }
    }
    
}
