/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu.joueurs;

import logger.Task;

/**
 * Un joueur humain.
 * @author Ivan CANET
 * @since 1 mai 2017
 */
public class Humain extends Joueur {

    private Action derniereAction;
    private boolean doitJouer = false;
        
    public Humain(String nom) {
        super(nom);
    }

    @Override
    public Action jouer() {
        derniereAction = null;
        doitJouer = true;
        return null;
    }

    @Override
    public Action derniereAction() {
        return derniereAction;
    }
    
    public boolean doitJouer(){
        return doitJouer;
    }
    
    public void repondre(Action a){
        Task.info(NOM + " (humain) a choisi de jouer un " + a.type());
        if(a.type() == Action.Act.RAISE)
            Task.info("De valeur " + a.somme());
        derniereAction = a;
        doitJouer = false;
    }
    
    /**
     * Ce joueur est-il humain ?
     * @return Toujours <code>true</code>.
     */
    @Override
    public boolean humain(){
        return true;
    }
}
