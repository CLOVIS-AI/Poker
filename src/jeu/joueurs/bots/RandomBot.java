/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu.joueurs.bots;

import jeu.joueurs.Action;
import logger.Task;
import poker.Poker;

/**
 * Un bot qui joue aléatoirement.
 * @author Ivan CANET
 * @since 13 avr. 2017
 */
public class RandomBot extends Bot {

    private final float chanceRaise;
    private final float chanceCall;
    private final float chanceFold;
    private float augmentationRaise;
    
    /**
     * Crée un bot jouant aléatoirement de manière équilibrée.
     * @param nom son nom
     */
    public RandomBot(String nom) {
        super(nom);
        chanceRaise = 34;
        chanceCall = 34;
        chanceFold = 34;
        augmentationRaise = 0.5f;
    }
    
    /**
     * Crée un bot jouant aléatoirement de manière équilibrée.
     * @param nom son nom
     * @param augm facteur d'ajout lors d'un RAISE [0..1]
     */
    public RandomBot(String nom, float augm){
        this(nom);
        augmentationRaise = augm;
    }
    
    /**
     * Crée un bot jouant aléatoirement, dans des proportions prédéfinies.
     * @param nom son nom
     * @param chR probabilité de jouer RAISE [0..100]
     * @param chC probabilité de jouer CALL [0..100]
     * @param chF probabilité de jouer FOLD [0..100]
     * @param augm facteur d'ajout lors d'un RAISE [0..1]
     * @throws IllegalArgumentException si <code>chR+chC+chF</code> est plus petit que 100, ou si <code>augm</code> n'est pas compris entre 0 et 1.
     */
    public RandomBot(String nom, float chR, float chC, float chF, float augm) throws IllegalArgumentException {
        super(nom);
        if(chR + chC + chF < 100)  throw new IllegalArgumentException("Le total des probabilités devrait valoir au moins 100.");
        chanceRaise = chR;
        chanceCall = chC;
        chanceFold = chF;
        if(augm > 1 || augm < 0) throw new IllegalArgumentException("L'augmentation en cas de Raise doit être comprise entre 0 et 1 : " + augm);
        augmentationRaise = augm;
    }

    @Override
    Action reflechir() {
        int choix = (int)Poker.ecran.random(0, 100);
        Task.begin(NOM + " : Choix d'un nombre aléatoire entre 0 et 100 : " + choix);
        
        // Raise
        if(choix < chanceRaise){
            Task.info("Option choisie : Raise");
            int val = (int) (Poker.ecran.random(0, 5) + Poker.ecran.etat().bet() * augmentationRaise);
            Task.end("Raise de : " + val);
            return new Action(val);
        }
        
        // Call
        else if(choix < chanceCall+chanceRaise){
            Task.end("Option choisie : Call");
            return new Action(Action.Act.CALL);
        }
        
        // Fold
        else{
            Task.end("Option choisie : Fold");
            return new Action(Action.Act.FOLD);
        }
    }
    
}
