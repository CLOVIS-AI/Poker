/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cartes;

import java.util.Objects;

/**
 * 
 * @author Ivan CANET & Raphael BOUCHY
 * @since 27 fev. 2017
 */
public class Carte implements Comparable {
    
    // **************************** CHAMPS *************************************
    private final int valeur;
    private final Couleur couleur;
    
    private final String nom, valStr, coulStr;
    
    // **************************** CONSTRUCTEURS ******************************
    
    /**
     * Crée une carte selon les spécificités données.
     * @param val La valeur de la carte [1..13]
     * @param coul La couleur de la carte
     * @throws IllegalArgumentException Si l'interval n'est pas respecté ou si <b>coul</b> vaut <b>null</b>.
     */
    public Carte(int val, Couleur coul) throws IllegalArgumentException {
        if(val > 13)      throw new IllegalArgumentException("La valeur de la carte doit être inférieure ou égale à 13 : val="+val);
        if(val < 1)       throw new IllegalArgumentException("La valeur de la carte doit être supérieure à 0 : val="+val);
        if(coul == null)  throw new IllegalArgumentException("Vous devez spécifier une couleur : coul="+coul);
        
        valeur = val;
        couleur = coul;
        
        switch(valeur){
            case 1:         valStr = "As";       break;
            case 11:        valStr = "Valet";    break;
            case 12:        valStr = "Dame";     break;
            case 13:        valStr = "Roi";      break;
            default:        valStr = String.valueOf(valeur);
        }
        
        switch(couleur){
            case PIQUE:     coulStr = "Pique";    break;
            case COEUR:     coulStr = "Cour";     break;
            case TREFLE:    coulStr = "Trefle";   break;
            case CARREAU:   coulStr = "Carreau";  break;
            default:        coulStr = "Erreur";
        }
        nom = valStr + " de " + coulStr;
    }
    
    // **************************** METHODES ***********************************
    
    /**
     * Récupérer cette carte sous la forme d'un String
     * @return La carte sous la forme "As de Pique", "10 de Trèfle", "Roi de Cœur", ...
     */
    @Override
    public String toString(){
        return nom;
    }
    
    public String valeurToString(){
        return valStr;
    }
    
    public String couleurToString(){
        return coulStr;
    }
    
    /**
     * Récupère la valeur de la carte.
     * @return La valeur de la carte [1..13]
     * @deprecated
     */
    public int getValeur(){
        return valeur;
    }
    
    /**
     * Récupère la valeur de la carte.
     * @return La valeur de la carte [2..14]
     */
    public int getValeurAs(){
        return (valeur == 1) ? 14 : valeur;
    }
    
    /**
     * Récupère la couleur de la carte.
     * @return La couleur de la carte
     */
    public Couleur getCouleur(){
        return couleur;
    }
    
    public static int valeurDeCouleur(Couleur c){
        switch(c){
            case PIQUE: return 0;
            case COEUR: return 1;
            case TREFLE: return 2;
            case CARREAU: return 3;
            default: //impossible
                return -1;
        }
    }

    @Override
    public int compareTo(Object c1) {
        if(c1 == null || this.getClass() != c1.getClass())
            throw new NullPointerException("");
        Carte c = (Carte)c1;
        int somme = c.getValeurAs() - this.getValeurAs();
        if(somme == 0){
            return valeurDeCouleur(this.getCouleur()) - valeurDeCouleur(c.getCouleur());
        }else{
            return somme;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.valeur;
        hash = 47 * hash + Objects.hashCode(this.couleur);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Carte other = (Carte) obj;
        if (this.valeur != other.valeur) {
            return false;
        }
        if (this.couleur != other.couleur) {
            return false;
        }
        return true;
    }
}
