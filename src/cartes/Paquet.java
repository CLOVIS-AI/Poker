/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cartes;

import graphismes.FenetreDeJeu;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import jeu.joueurs.Joueur;
import logger.Task;
import poker.Poker;
import processing.core.PGraphics;

/**
 * Implémentation d'un paquet de cartes
 * @author Ivan CANET & Raphael BOUCHY
 * @since 27 fev. 2017
 */
public class Paquet {
    
    // ******************************** CHAMPS *********************************
    private ArrayList<Carte> cartes;
    
    private Main main;
    
    // ******************************** CONSTRUCTEURS **************************
    
    /**
     * Crée un paquet complet de 52 cartes
     * @param mélangé Si true, le paquet est mélangé, sinon il est ordonné.
     */
    public Paquet(boolean mélangé){
        this(52);
        
        Task.begin("Remplissage du paquet ...");
        Couleur[] couleurs = new Couleur[]{Couleur.PIQUE, Couleur.COEUR, Couleur.TREFLE, Couleur.CARREAU};
        Task.info("Tableau de couleurs créé.");
        
        Task.info("Remplissage du paquet ...");
        for(Couleur c : couleurs)
            for(int i = 1; i <= 13; i++)
                cartes.add(new Carte(i, c));
        Task.info("Paquet rempli.");
        
        if(mélangé)     melanger();
        Task.end("Le paquet a bien été créé.");
    }
    
    /**
     * Crée un paquet vide
     * @param taillePrevue Prévision de la taille
     * @see Paquet()
     */
    public Paquet(int taillePrevue){
        Task.begin("Création d'un paquet de taille " + taillePrevue);
        cartes = new ArrayList<>(taillePrevue);
        Task.end("Le paquet a bien été créé.");
    }
    
    /**
     * Crée un paquet vide de capacité initiale de 5 cartes
     */
    public Paquet(){
        this(5);
    }
    
    /**
     * Créé une copie d'un paquet existant.
     * @param p le modèle
     */
    public Paquet(Paquet p){
        Task.begin("Copie d'un paquet existant ...");
        cartes = p.getCartes();
        Task.end("Copie effectuée.");
    }
    
    public Paquet(Paquet p1, Paquet p2){
        Task.begin("Combinaison de deux paquets ...");
        cartes = p1.getCartes();
        for(Carte c : p2.getCartes())
            cartes.add(c);
        Task.end("Combinaison terminée, ce paquet contient " + taille() + " cartes.");
    }
    
    // ******************************** METHODES *******************************
    
    /**
     * Mélange les cartes
     */
    public final void melanger(){
        Task.begin("Mélange des cartes ...");
        ArrayList<Carte> resultat = new ArrayList<>(cartes.size());
        Task.info("Paquet intermédiaire créé.");
        
        Task.info("Remplissage aléatoire du paquet intermédiaire.");
        while(!cartes.isEmpty()){
            int i = (int) Poker.ecran.random(0, cartes.size());
            resultat.add(cartes.remove(i));
        }
        Task.info("Le paquet intermédiaire a bien été rempli aléatoirement, il contient en ce moment " + resultat.size() + " cartes.");
        
        Task.info("Ce paquet devient le paquet intermédiaire.");
        cartes = resultat;
        Task.end("Mélange effectué.");
    }
    
    /**
     * Le paquet, affiché comme String
     * @return Le paquet sous la forme [0] Carte#0.toString() \n ...
     */
    @Override
    public final String toString(){
        String resultat = "";
        
        for (Carte carte : cartes) {
            resultat += carte.toString() + ", ";
        }
        
        return resultat;
    }
    
    /**
     * Récupère une copie de la liste des cartes (modifier cette liste ne modifiera pas le paquet).
     * @return Une copie de la liste des cartes
     */
    public final ArrayList<Carte> getCartes(){
        Task.info("Copie du contenu de ce paquet ...");
        return new ArrayList<>(cartes);
    }
    
    /**
     * Récupère une carte du paquet.<br/><br/>
     * Cette méthode utilise un appel à {@link ArrayList#get(int) }, et 
     * n'effectue aucun <code>try..catch</code>. S'en référer à la méthode 
     * mentionnée pour la gestion des exceptions et les détails sur les 
     * performances.
     * @param indice le numéro de la carte demandée (de 0 à {@link #taille() taille}-1).
     * @return La carte dont le numéro est 'Indice'.
     * @see java.util.ArrayList#get(int) Pour plus d'informations sur le fonctionnement de cette méthode
     */
    public final Carte carte(int indice){
        return cartes.get(indice);
    }
    
    /**
     * Ajoute une carte au paquet.
     * @param c la carte à ajouter
     */
    public final void ajouterCarte(Carte c){
        cartes.add(c);
    }
    
    /**
     * Transfert la première carte de ce paquet à un autre paquet.
     * @param p le paquet qui reçoit la carte
     */
    public final void donnerCarte(Paquet p){
        p.ajouterCarte(cartes.remove(0));
    }
    
    /**
     * Transfert la première carte de ce paquet à un joueur.
     * @param j le joueur qui reçoit la carte
     */
    public final void donnerCarte(Joueur j){
        j.ajouterCarte(cartes.remove(0));
    }
    
    /**
     * Taille du paquet.
     * @return La taille du paquet.
     */
    public final int taille(){
        return cartes.size();
    }
    
    public final void draw(PGraphics g, float x, float y){
        g.pushMatrix();
        g.translate(x, y);
        for(int i = 0; i < cartes.size(); i++){
            g.image(FenetreDeJeu.carte(cartes.get(i)), 0, 0);
            g.translate(135, 0);
        }
        g.popMatrix();
    }
    
    public final Main valeur(){
        Task.begin("Calcul de la valeur ...");
        Main m = score(getCartes());
        Task.info(m.toString());
        Task.end("Valeur évaluée : " + m.getValeur());
        return m;
    }
    
    private Main score(ArrayList<Carte> cartes){
        if(cartes.size() < 5 || cartes.size() >= 11)
            throw new IllegalArgumentException("Il faut au minimum 5 cartes et il est interdit de dépasser 11 cartes.");
        if(cartes.size() == 5){
            // Code d'évalutation : il n'y a que 5 cartes ici
            Task.info("Paquet de 5 cartes trouvé : " + cartes.toString());
            
            // ============================ I ==================================
            //                            PAIRES
            // =================================================================
            Task.begin("Calcul des paires ...");
            ArrayList<ArrayList<Carte>> histogramme = new ArrayList<>(14);
            for(int i = 0; i <= 14; i++)
                histogramme.add(new ArrayList<Carte>(4));
            for(int i = 0; i < cartes.size(); i++){
                histogramme.get(cartes.get(i).getValeurAs()).add(cartes.get(i));
            }
            Collections.sort(histogramme, new ComparatorImpl());
            Collections.sort(histogramme.get(0));
            Collections.sort(histogramme.get(1));
            Task.end("Histogramme : " + histogramme.toString());
            
            // ************************** Carré ********************************
            if(histogramme.get(0).size() == 4){
                return new Main(Combinaison.CARRE, histogramme.get(0).get(0), null);
            }
            
            // **************************** Full *******************************
            else if(histogramme.get(0).size() == 3 && histogramme.get(1).size() == 2){
                return new Main(Combinaison.FULL, histogramme.get(0).get(0), histogramme.get(1).get(0));
            }
            
            // ************************** Brelan *******************************
            else if(histogramme.get(0).size() == 3){
                return new Main(Combinaison.BRELAN, histogramme.get(0).get(0), null);
            }
            
            // ************************ Deux Paires ****************************
            else if(histogramme.get(0).size() == 2 && histogramme.get(1).size() == 2){
                return new Main(Combinaison.DEUX_PAIRES, histogramme.get(0).get(0), null);
            }
            
            // *************************** Paire *******************************
            else if(histogramme.get(0).size() == 2){
                return new Main(Combinaison.PAIRE, histogramme.get(0).get(0), null);
            }
            
            // ============================ II =================================
            //                           COULEUR ?
            // =================================================================
            
            Task.begin("Couleur ?");
            boolean isCouleur = true;
            Couleur last = cartes.get(0).getCouleur();
            for(int i = 1; i < cartes.size(); i++){
                if(cartes.get(i).getCouleur() != last)
                    isCouleur = false;
            }
            Task.end(isCouleur ? "Oui" : "Non");
            
            // ============================ III ================================
            //                            SUITE ?
            // =================================================================
            
            Task.begin("Suite ?");
            Task.info("Cartes : " + cartes.toString());
            Collections.sort(cartes);
            Task.info("Triées : " + cartes.toString());
            Carte suite = cartes.get(0).getValeurAs() - cartes.get(4).getValeurAs() == 4 ?
                    cartes.get(0) : null;
            Task.info("Soustraction : " + cartes.get(0).getValeurAs() + "-" + cartes.get(4).getValeurAs() + " = " + (cartes.get(0).getValeurAs() - cartes.get(4).getValeurAs()));
            if(suite == null && 
                    cartes.get(0).getValeurAs() == 14 && 
                    cartes.get(1).getValeurAs() == 5)
                suite = cartes.get(1);
            Task.info("Deuxième étape : première valeur : " + cartes.get(0).getValeurAs() + ", deuxième valeur : " + cartes.get(1).getValeurAs());
            Task.end(suite == null ? "Pas de suite" : "Suite trouvée");
            
            // ============================ IV =================================
            //                          RESOLUTION
            // =================================================================
            
            // *********************** Quinte Flush ****************************
            if(suite != null && isCouleur){
                if(suite.getValeurAs() != 14){
                    return new Main(Combinaison.QUINTE_FLUSH, suite, null);
                }
                
                // **************** Quinte Flush Royale ************************
                else{
                    return new Main(Combinaison.QUINTE_FLUSH_ROYALE, suite, null);
                }
            }
            
            // ************************** Quinte *******************************
            else if(suite != null){
                return new Main(Combinaison.QUINTE, suite, null);
            }
            
            // ************************** Couleur ******************************
            else if(isCouleur){
                return new Main(Combinaison.COULEUR, cartes.get(0), null);
            }
            
            // ************************* Carte Haute ***************************
            else{
                return new Main(Combinaison.HAUTE, cartes.get(0), null);
            }
            
        }else{
            Task.begin("Récursion ...");
            Main meilleure = new Main(Combinaison.HAUTE, new Carte(2, Couleur.PIQUE), null);
            for(int i = 0; i < cartes.size(); i++){
                Task.begin("Essai " + (i+1) + "/" + cartes.size());
                ArrayList<Carte> interm = new ArrayList<>(cartes);
                interm.remove(cartes.get(i));
                Main m = score(interm);
                Task.info("Combinaison trouvée : " + m.toString());
                if(meilleure.compareTo(m) <= 0){
                    Task.info("Cette combinaison est meilleure !");
                    meilleure = m;
                }
                Task.end("Essai terminé.");
            }
            //Poker.ecran.notif("Évaluation", meilleure.toString(), Notif.Type.DEBUG);
            Task.end("Fin de la récursion.");
            return meilleure;
        }
    }

    private static class ComparatorImpl implements Comparator<ArrayList<Carte>> {

        public ComparatorImpl() {
            super();
        }

        @Override
        public int compare(ArrayList<Carte> arg0, ArrayList<Carte> arg1) {
            if(arg1.size() != 0 && arg1.size() == arg0.size()){
                // Si les deux paquets ont la même taille et qu'elle n'est pas 0
                return arg1.get(0).getValeurAs() - arg0.get(0).getValeurAs();
            }else{
                // Si les deux paquets ont la même taille
                return arg1.size() - arg0.size();
            }
        }
    }
}

