/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu;

import cartes.Paquet;
import jeu.Jeu.Etape;
import jeu.Jeu.Mode;
import processing.core.PGraphics;
import util.MData;
import util.Primitive;

/**
 * Comment obtenir des informations sur l'état du jeu ?
 * @author Ivan CANET
 */
public abstract class Etat {
    
    
    /**
     * Afficher les cartes présentes sur la table.
     * @param g la surface sur laquelle dessiner
     */
    public abstract void drawTable(PGraphics g, float x, float y);
    
    /**
     * Afficher la liste des joueurs.
     * @param g la surface sur laquelle dessiner
     */
    public abstract void drawListe(PGraphics g);
    
    /**
     * Combien de cartes reste-t-il dans la pioche ?
     * @return Un nombre compris entre 0 (la pioche est vide) et 52 (la pioche
     * est pleine).
     */
    public abstract int nombreCarteRestantes();

    /**
     * Combien de joueurs participent à cette manche ?<br/>
     * Les joueurs ayant 'foldé' sont exclus de ce compte.
     * @return Nombre de joueurs restant dans la manche en cours.
     */
    public abstract int nombreJoueurs();

    /**
     * Combien de cartes sont visibles sur la table ?
     * @return Le nombre de cartes visibles sur la table (3 au {@link Etape#FLOP flop},
     * 4 au {@link Etape#TURN turn} et 5 à la {@link Etape#RIVER river}).
     */
    public abstract int nombreCartesTable();

    /**
     * Quelle est la valeur du pot ?
     * @return Le nombre de jetons dans le pot.
     */
    public abstract int pot();

    /**
     * Quelle est la valeur du bet ?
     * @return La somme qu'un joueur paye en faisant un {@link Action.Act#CALL call}.
     */
    public abstract int bet();

    /**
     * La partie est en {@link Mode#LIMIT} ou en {@link Mode#NO_LIMIT} ?
     * @return Le mode de jeu de la partie en cours.
     */
    public abstract Mode modeDeJeu();

    /**
     * Où en es-t-on dans la partie ?
     * @return Un objet Etape contenant l'avancement de la partie.
     */
    public abstract Etape etape();

    /**
     * La valeur de la Small Blind.
     * @return Valeur de la Small Blind en jetons.
     * @see #bigBlind() Valeur de la Big Blind
     */
    public abstract int smallBlind();

    /**
     * La valeur de la Big Blind.
     * @return Valeur de Big Blind en jetons.
     * @see #smallBlind() Valeur de la Small Blind
     */
    public abstract int bigBlind();

    /**
     * Pour savoir quelles sont les cartes face visible sur la table.
     * @return Une copie du paquet sur la table (puisque c'est une copie,
     * il est inutile de le modifier).
     */
    public abstract Paquet table();

    /**
     * Pour savoir le nombre de jetons que les joueurs ont au début de la partie.
     * @return Le nombre de jetons offert aux joueurs au début de la partie.
     */
    public abstract int jetonsMin();
    
    /**
     * Read this object as MDATA
     * @return this object's data
     */
    public MData read(){
        MData m = new MData();
        m.put("nombre_cartes_restantes", new Primitive(nombreCarteRestantes()));
        m.put("nombre_joueurs", new Primitive(nombreJoueurs()));
        m.put("nombre_cartes_table", new Primitive(nombreCartesTable()));
        m.put("pot", new Primitive(pot()));
        m.put("bet", new Primitive(bet()));
        m.put("mode", new Primitive(modeDeJeu().toString()));
        m.put("etape", new Primitive(etape().toString()));
        m.put("small_blind", new Primitive(smallBlind()));
        m.put("big_blind", new Primitive(bigBlind()));
        m.put("table", new Primitive(table().toString()));
        m.put("jetons_minimum", new Primitive(jetonsMin()));
        return m;
    }
}
