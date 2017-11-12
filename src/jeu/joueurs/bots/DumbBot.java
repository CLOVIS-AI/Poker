/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu.joueurs.bots;

import jeu.joueurs.Action;

/**
 * Un bot pour faire des tests, il joue toujours 'CALL'.
 * @author Ivan CANET
 */
public class DumbBot extends Bot {

    public DumbBot(String nom) {
        super(nom);
    }

    @Override
    Action reflechir() {
        return new Action(Action.Act.CALL);
    }
    
}
