/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu.joueurs;

/**
 * L'action prise par un joueur ...
 * @author Ivan CANET & Raphaël BOUCHY
 */
public class Action {
    
    private final Act ACTION;
    private final int SOMME;
    
    /**
     * Crée un object Action.<br/><br/>
     * <b>NOTE.</b> Ce constructeur ne doit être utilisé que pour un 
     * {@link Act#CALL CALL} ou un {@link Act#FOLD FOLD}, 
     * <code>IllegalArgumentException</code> sera jettée dans le cas contraire. 
     * Pour un {@link Act#RAISE RAISE}, utilisez {@link #Action(int) new Action(int)}.
     * @param act {@link Act#CALL} ou {@link Act#FOLD}
     */
    public Action(Act act){
        if(act == Act.RAISE)
            throw new IllegalArgumentException("Il est interdit d'utiliser ce constructeur pour un Raise, utilisez 'Action(int)' à la place.");
        ACTION = act;
        SOMME = 0;
    }
    
    /**
     * Crée un object Action, dont l'action est un {@link Act#RAISE RAISE}.
     * @param somme la valeur du Raise.
     */
    public Action(int somme){
        if(somme < 0)
            throw new IllegalArgumentException("Vous ne pouvez pas mettre en jeu une somme négative : " + somme);
        ACTION = Act.RAISE;
        SOMME = somme;
    }
    
    /**
     * Renvoie le type d'action choisie par le joueur.
     * @return Le type de l'action choisie par le joueur.
     */
    public Act type(){
        return ACTION;
    }
    
    /**
     * Renvoie la somme d'argent mise en jeu.
     * @return Si le {@link #type() type} est une {@link Act#RAISE relance}, 
     * renvoie la somme mise en jeu. Sinon, renvoie 0.
     */
    public int somme(){
        return SOMME;
    }
    
    /**
     * Quelle action effectue le joueur ?
     */
    public enum Act{
        /** Abandonner la partie. */
        FOLD,
        
        /** Payer la somme du Bet. */
        CALL,
        
        /** Augmenter la somme du Bet, puis ajuster sa somme. */
        RAISE
    }
}
