/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu.joueurs.bots;

import jeu.joueurs.Action;
import jeu.joueurs.Joueur;

/**
 * Classe symbolisant un bot jouant immédiatement.
 * @author Ivan CANET & Raphaël BOUCHY
 * @since 13 avr. 2017
 */
public abstract class Bot extends Joueur {

    private Action derniereAction;
    
    /**
     * Crée un bot.
     * @param nom son nom de joueur
     */
    public Bot(String nom) {
        super(nom);
        derniereAction = null;
    }
    
    @Override
    public final Action jouer(){
        derniereAction = reflechir();
        if(derniereAction == null) 
            derniereAction = new Action(Action.Act.FOLD);
        return derniereAction;
    }
    
    @Override
    public final Action derniereAction(){
        return derniereAction;
    }
    
    /**
     * Le bot doit définir cette méthode, c'est ici qu'il décidera quoi jouer,
     * sous la forme d'un objet Action.
     * @return Un objet Action déterminant l'action choisie par le bot.<br/>
     * Il est interdit de renvoyer <code>null</code>, sinon le jeu éliminera votre
     * bot.
     */
    abstract Action reflechir();
    
}
