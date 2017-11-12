package jeu.joueurs;


import cartes.Carte;
import cartes.Paquet;
import logger.Task;
import processing.core.PGraphics;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Ivan CANET & Raphaël BOUCHY
 */
public abstract class Joueur {
    
    private Paquet main;
    private int jetons;
    
    public final String NOM;
    
    /**
     * Créer un Joueur
     * @param nom le nom du joueur
     */
    public Joueur(String nom){
        Task.info("Création du joueur " + nom);
        NOM = nom;
        main = new Paquet(2);
    }
    
    /**
     * Envoie une requête au joueur pour savoir ce qu'il va jouer.<br><br/>
     * Dans la majorité des cas, le joueur ne peut pas jouer immédiatement.
     * Il peut alors renvoyer <code>null</code> ; le jeu utilisera alors la
     * méthode {@link #derniereAction() } pour demander l'action effectuée.
     * @return Ce que le joueur a décidé de jouer, ou <code>null</code>.
     */
    abstract public Action jouer();
    
    /**
     * Permet au jeu de savoir ce que le joueur veut jouer. Si le joueur n'a pas
     * encore répondu, cette méthode doit renvoyer <code>null</code>. Le jeu
     * redemandera alors à la prochaine mise à jour.<br/><br/>
     * <b>Note.</b> Fonctionnement :
     * <pre>
     * derniereAction(); // -> renvoie la dernière action
     * jouer();          // -> renvoie null (le joueur n'a pas répondu)
     * derniereAction(); // -> renvoie null (le joueur n'a toujours pas répondu)
     * derniereAction(); // -> le joueur a répondu.
     * </pre>
     * @return Ce que le joueur a décidé de jouer, ou <code>null</code>.
     */
    abstract public Action derniereAction();
    
    /**
     * Donner de l'argent à ce joueur.
     * @param somme combien d'argent le joueur gagne
     */
    public final void gagner(int somme){
        if(somme < 0) return;
        Task.info(NOM + " gagne " + somme + " jetons.");
        jetons += somme;
    }
    
    /**
     * Enlever de l'argent à ce joueur.
     * @param somme combien d'argent ce joueur perd
     * @return La somme que le joueur a effectivemhumain = new Humain();ent payée (en prenant en compte le risque de manque d'argent).
     */
    public final int payer(int somme){
        Task.begin(NOM + " doit payer " + somme + " jetons, et en a " + jetons);
        jetons -= somme;
        if(jetons <= 0){
            Task.info(NOM + " n'a pas assez de jethumain = new Humain();ons.");
            int dif = -jetons;
            jetons = 0;
            Task.end(NOM + " n'a payé que " + (somme-dif) + " jetons, il lui en reste " + jetons);
            return somme-dif;
        }else{
            Task.end(NOM + " a maintenant " + jetons + " jetons.");
            return somme;
        }
    }
    
    /**
     * Appelle la méthode {@link Paquet#ajouterCarte(cartes.Carte) } de la main de ce joueur.
     * @param c la carte
     */
    public final void ajouterCarte(Carte c){
        main.ajouterCarte(c);
    }
    
    /**
     * Combien le joueur a-t-il d'argent ?
     * @return Le nombre de jetons qu'a ce joueur.
     */
    public final int somme(){
        return jetons;
    }
    
    /**
     * Obtenir une copie du paquet que ce joueur a entre les mains.
     * @return Une copie de la main du joueur (2 cartes).
     */
    public final Paquet paquet(){
        return new Paquet(main);
    }
    
    /**
     * Afficher les cartes de ce joueur.
     * @param g où dessiner
     * @param x coordonnée X
     * @param y coordonnée Y
     */
    public final void draw(PGraphics g, float x, float y){
        main.draw(g, x, y);
    }
    
    /**
     * Ce joueur est-il humain ? Permet de différencier les humains des bots.
     * @return <code>false</code> par défaut.
     */
    public boolean humain(){
        return false;
    }
    
    /**
     * Réinitialise la main du joueur. Ne jamais appeller pendant une partie !
     * @deprecated
     */
    public void reset(){
        main = new Paquet();
    }
}
