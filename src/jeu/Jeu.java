/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu;

import cartes.Main;
import cartes.Paquet;
import graphismes.FenetreDeJeu;
import graphismes.Notif.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import jeu.joueurs.Action;
import jeu.joueurs.Joueur;
import logger.Task;
import poker.Connection;
import poker.Poker;
import static processing.core.PApplet.max;
import static processing.core.PApplet.min;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.RIGHT;
import static processing.core.PConstants.TOP;
import processing.core.PGraphics;
import util.MData;
import util.Primitive;

/**
 * Un objet représentant la partie en cours.
 * @author Ivan CANET & Raphaël BOUCHY
 * @since 7 avr. 2017
 */
public class Jeu {
    
    private final Paquet pioche, poubelle, table;
    private final ArrayList<InfosJoueur> joueurs;
    
    private Joueur bouton;
    private int joueurID, attente;
    
    private int pot, bet;
    
    private boolean enCours = true;
    
    private Etape etape = Etape.EN_ATTENTE;
    private final Mode MODE_DE_JEU;
    private final int SMALL_BLIND, JETONS_MIN;
    
    public Jeu(Mode m, int valeurSmallBlind, int jetonsMin){
        Task.begin("Création d'une nouvelle partie ...");
        
        JETONS_MIN = jetonsMin;
        Task.info("Somme de départ : " + JETONS_MIN);
        
        Task.begin("Initialisation des joueurs ...");
        joueurs = new ArrayList<>();
        Task.end("Terminé.");
        
        Task.begin("Initialisation des paquets ...");
        pioche = new Paquet(true);
        poubelle = new Paquet(3);
        table = new Paquet(5);
        Task.end("Paquets créés.");
        
        MODE_DE_JEU = m;
        Task.info("Initialisation du mode de jeu terminée : " + MODE_DE_JEU);
        
        SMALL_BLIND = valeurSmallBlind < 1 ? 1 : valeurSmallBlind;
        pot = SMALL_BLIND * 2;
        bet = 0;
        etat = new EtatJeu();
        Task.info("Initialisation des sommes terminée.");
        
        Task.end("Création de la partie terminée.");
    }
    
    public void rejoindrePartie(Joueur j) throws IllegalStateException {
        if(etape != Etape.EN_ATTENTE)
            throw new IllegalStateException("Il est interdit de rejoindre la partie en cours de route !");
        if(contientJoueur(joueurs, j)){
            Task.info("Le joueur " + j.NOM + " est déjà un participant.");
        }else{
            if(JETONS_MIN < 1 && j.somme() <= 0)
                return;
            joueurs.add(new InfosJoueur(j));
            j.gagner(JETONS_MIN - j.somme());
            Task.info("Le joueur " + j.NOM + " a rejoint la partie.");
        }
    }
    
    public boolean commencerPartie(){
        Task.begin("Préparation au début de la partie ...");
        
        if(joueurs.size() < 3){
            Task.end("Pas assez de joueurs pour commencer.");
            return false;
        }
        
        for(InfosJoueur i : joueurs)
            i.joueur.gagner(JETONS_MIN - i.joueur.somme());
        
        Task.info("Modification aléatoire de l'ordre de jeu ...");
        Collections.shuffle(joueurs);
        Task.info("Modification terminée. Assignation des rôles :");
        
        bouton = joueurs.get(0).joueur;
        Task.info("Le bouton est le joueur " + bouton.NOM);
        
        Task.begin("Small blind :");
        Task.info("La small blind est le joueur " + joueurs.get(1).joueur.NOM);
        joueurs.get(1).payer(SMALL_BLIND);
        pot += SMALL_BLIND;
        Task.end("Traitement de la small blind terminé.");
        
        Task.begin("Big blind :");
        Task.info("La big blind est le joueur " + joueurs.get(2).joueur.NOM);
        joueurs.get(2).payer(SMALL_BLIND*2);
        pot += SMALL_BLIND * 2;
        bet = SMALL_BLIND * 2;
        Task.end("Traitement de la big blind terminé.");
        
        Task.end("Tout est prêt, début de la partie.");
        
        Task.info("Pre-flop ...");
        Task.begin("Distribution des cartes ...");
        etape = Etape.PRE_FLOP;
        for(int t = 0; t < 2; t++)
            for(InfosJoueur j : joueurs){
                Task.info("Envoi de sa carte n°" + t + " au joueur " + j.joueur.NOM);
                pioche.donnerCarte(j.joueur);
            }
        Task.end("Toutes les cartes ont été distribuées ; il reste " + pioche.taille() + " cartes dans la pioche.");
        
        attente = 0;
        
        joueurID = 3;
        while(joueurID >= joueurs.size())
            joueurID-=joueurs.size();
        joueurs.get(joueurID).joueur.jouer();
        Task.info("Envoyé à " + joueurs.get(joueurID).joueur.NOM + " l'ordre de jouer.");
        
        for(InfosJoueur ij : joueurs)
            notifierServeurJoueur(ij);
        
        return true;
    }
    
    public void mettreAJour(){
        if(!enCours || etape == Etape.EN_ATTENTE){ return; }
        
        Joueur j = joueurs.get(joueurID).joueur;
        Action a = j.derniereAction();
        if(attente > 10000 || j.somme() == 0)
            a = new Action(Action.Act.FOLD);
        if(joueurs.get(joueurID).ceTour){
            phaseSuivante();
            if(etape == Etape.SHOWDOWN)
                return;
        }
        if(a != null){
            
            joueurs.get(joueurID).ceTour = true;
            switch(a.type()){
                case FOLD:
                    Task.info("Le joueur " + j.NOM + " a joué un FOLD.");
                    Poker.ecran.notif(j.NOM, "Abandonne la partie");
                    abandon(j);
                    if(!enCours)
                        return;
                    joueurID--;   //on enlève 1 au n° du joueurID pour éviter de sauter le joueurID suivant
                    break;
                case RAISE:
                    Task.info("Le joueur " + j.NOM + " a joué un RAISE, le bet vaut en ce moment " + bet);
                    resetCeTour(joueurs);
                    Poker.ecran.notif(j.NOM, "Relance !");
                    if(MODE_DE_JEU == Mode.NO_LIMIT){
                        InfosJoueur joueur = joueurs.get(joueurID);
                        if(a.somme() < SMALL_BLIND*2)
                            a = new Action(SMALL_BLIND*2);
                        if(joueur.somme() > a.somme()){ // Si le joueur a assez d'argent pour payer
                            Task.info(joueur.joueur.NOM + " a suffisament d'argent.");
                            bet += a.somme();
                        }else{ // Si le joueur n'a pas suffisament d'argent
                            Task.info(joueur.joueur.NOM + " n'a pas suffisament d'argent : il devrait payer " + a.somme() + " mais n'a que " + joueur.somme());
                            joueur.tapis = true;
                            Task.info("Le joueur " + joueur.joueur.NOM + " fait tapis.");
                            bet = max(bet, joueur.somme() + joueur.sommePayee);
                            Task.info("Le bet est devenu : " + bet);
                        }
                    }else{                             bet *= 2;         }
                    // Effectuer ensuite un 'call'
                case CALL:
                    Task.info("Le joueur " + j.NOM + " paye la mise.");
                    if(a.type() == Action.Act.CALL)  Poker.ecran.notif(j.NOM, "Paie la mise");
                    int demande = bet - joueurs.get(joueurID).sommePayee;
                    int paie = j.payer(demande);
                    if(paie != demande){
                        joueurs.get(joueurID).tapis = true;
                        Task.info("Le joueur " + joueurs.get(joueurID).joueur.NOM + " fait tapis.");
                    }
                    pot += paie;
                    joueurs.get(joueurID).sommePayee += paie;
                    notifierServeurJoueur(joueurs.get(joueurID));
                    break;
            }
            notifierServeur();
            attente = 0;
            
            int nombreDeTapis = 0;
            // Sélectionner le joueur suivant, sauf s'il a fait tapis
            do{
                joueurID++;
                nombreDeTapis++;
                if(joueurID >= joueurs.size())
                    joueurID -= joueurs.size();
                if(nombreDeTapis >= joueurs.size()-1){
                    // Si tous les joueurs ont fait tapis, tour suivant
                    phaseSuivante();
                    if(!enCours)
                        return;
                }
            }while(joueurs.get(joueurID).tapis);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
            }
            Task.info("C'est au tour de " + joueurs.get(joueurID).joueur.NOM + " de jouer.");
            if(!joueurs.get(joueurID).tapis)
                joueurs.get(joueurID).joueur().jouer();
        }else{
            // Task.info("En attente du choix de jeu de " + j.NOM);
            attente++;
        }
    }
    
    private void phaseSuivante(){
        if(!enCours)    return;
        resetJoueurs(joueurs);
        resetCeTour(joueurs);
        System.out.println();
        bet = 0;
        switch(etape){
            case PRE_FLOP:
                Task.info("Fin du pre-flop.");
                etape = Etape.FLOP;
                for(int i = 0; i < 3; i++){
                    pioche.donnerCarte(poubelle);
                    pioche.donnerCarte(table);
                }
                Task.info("Début du flop.");
                Poker.ecran.notif("Flop", "Début du flop !", Type.ORANGE);
                break;
            case FLOP:
            case TURN:
                Task.info("Fin du tour de mise ...");
                etape = etape == Etape.FLOP ? Etape.TURN : Etape.RIVER;
                pioche.donnerCarte(poubelle);
                pioche.donnerCarte(table);
                Task.info("Prêt pour le tour suivant.");
                if(etape == Etape.TURN) Poker.ecran.notif("Turn", "Début de la Turn !", Type.ORANGE);
                if(etape == Etape.RIVER) Poker.ecran.notif("River", "Début de la River !", Type.ORANGE);
                break;
            case RIVER:
                Task.info("Fin du tour de mise ...");
                etape = Etape.SHOWDOWN;
                Poker.ecran.notif("Showdown", "Fin de la partie ...", Type.ROUGE);
                Task.info("Prêt pour le tour suivant.");
            case SHOWDOWN:
                Task.begin("Début du showdown.");
                Task.info("Il reste " + joueurs.size() + " joueurs.");
                for(int i = 0; i < joueurs.size(); i++){
                    InfosJoueur infos = joueurs.get(i);
                    System.out.println("\n");
                    Main m = new Paquet(table, infos.joueur().paquet()).valeur();
                    infos.valeurMain = m.getValeur();
                    Poker.ecran.notif(infos.joueur.NOM, m.toString(), Type.ORANGE);
                    Task.info("C'était la valeur du joueur " + infos.joueur.NOM);
                    infos.main = infos.joueur.paquet();
                }
                System.out.println("\n");
                Task.info("Ordre des joueurs ...");
                ArrayList<InfosJoueur> resultats = new ArrayList<>(2);
                resultats.add(joueurs.get(0));
                Task.info("La table était [" + table.toString() + "]");
                Task.info(joueurs.get(0).joueur.NOM + " a un score de " + joueurs.get(0).valeurMain + ", avec comme cartes : " + joueurs.get(0).joueur().paquet().toString());
                for(int i = 1; i < joueurs.size(); i++){ //on saute le premier
                    Task.info(joueurs.get(i).joueur.NOM + " a un score de " + joueurs.get(i).valeurMain + ", avec comme cartes : " + joueurs.get(i).joueur().paquet().toString());
                    if(joueurs.get(i).valeurMain > resultats.get(0).valeurMain){
                        resultats.clear();
                        resultats.add(joueurs.get(i));
                    }else if(joueurs.get(i).valeurMain == resultats.get(0).valeurMain){
                        resultats.add(joueurs.get(i));
                    }
                }
                Task.end("Fin du showdown.");
                System.gc();
                ArrayList<InfosJoueur> classementGeneral = new ArrayList<>(joueurs);
                Collections.sort(classementGeneral);
                
                //Déclaration des vainqueurs
                Task.begin("Proclamation des vainqueurs !");
                for(int i = 0; i < 5; i++)
                    Poker.ecran.notif("", "", Type.VIDE);
                
                Task.info("Grands gagnants (le pot vaut " + pot + ") :");
                for(InfosJoueur ij : resultats){
                    victoireJoueur(ij, (int) (pot / resultats.size()));
                    classementGeneral.remove(ij);
                }
                
                if(pot > 0){
                    Poker.ecran.notif("", "", Type.VIDE);
                    Task.info("Petits gagnants :");
                    for(InfosJoueur ij : classementGeneral)
                        victoireJoueur(ij, pot);
                }
                
                enCours = false;
                Task.end("Fin de la proclamation.");
                Poker.ecran.notif("Poker", "Fin de la partie !", Type.ROUGE);
                finDePartie();
                return;
        }
        notifierServeur();
        for(InfosJoueur ij : joueurs)
            notifierServeurJoueur(ij);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void finDePartie(){
        enCours = false;
        for(int i = 0; i < 5; i++)
            Poker.ecran.notif(".", ".", Type.VIDE);
        try{
            for(int i = 10; i >= 0; i--){
                Thread.sleep(1000);
                Poker.ecran.notif("Prochaine partie", "Dans " + i, Type.NORMAL);
            }
        }catch(InterruptedException ex){
            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
        }
        Connection.relancer();
    }
    
    /**
     * Cette partie est-elle en cours ?
     * @return <code>true</code> si oui.
     */
    public boolean enCours(){
        return enCours;
    }
    
    private void abandon(Joueur j){
        supprimerJoueur(joueurs, j);
        Task.info("Le joueur " + j.NOM + " a quitté la partie.");
        if(joueurs.size() <= 1){
            // Le joueur restant gagne
            victoire(joueurs.get(0));
            finDePartie();
        }
    }
    
    private void victoire(InfosJoueur infos){
        Task.begin("Déclaration du vainqueur :");
        Poker.ecran.notif(infos.joueur.NOM, "Gagne " + pot + " jetons !", Type.ROUGE);
        infos.joueur.gagner(pot);
        enCours = false;
        pot = 0;
        bet = 0;
        Task.end("Fin de la partie.");
    }
    
    private void victoireJoueur(InfosJoueur infos, int gagnerMax){
        if(infos.tapis){
            Task.info(infos.joueur.NOM, "A fait tapis ... il pourrait gagner jusqu'à " + gagnerMax + " jetons.");
            int paye = min(infos.sommePayee * joueurs.size(), gagnerMax);
            if(paye > 0) Poker.ecran.notif(infos.joueur().NOM, "Gagne " + paye + " jetons !", gagnerMax != pot ? Type.ROUGE : Type.ORANGE);
            infos.joueur.gagner(paye);
            pot -= paye;
        }else{
            Task.info(infos.joueur.NOM, pot == gagnerMax ? "Raffle le pot !" : "Remporte sa part complète !");
            int paye = min(pot, gagnerMax);
            if(paye > 0){ infos.joueur.gagner(paye); Poker.ecran.notif(infos.joueur().NOM, "Gagne " + paye + " jetons !", gagnerMax != pot ? Type.ROUGE : Type.ORANGE);}
            pot -= paye;
        }
        Task.info("Il reste " + pot + " jetons à partager.");
    }
    
    public void drawTable(PGraphics g, float x, float y){
        table.draw(g, x, y);
    }
    
    public void drawListe(PGraphics g){
        etat.drawListe(g);
    }
    
    /**
     * Envoyer les commandes "table_update" et "game_update" aux clients.
     */
    private void notifierServeur(){
        MData p = new MData();
        p.put("bet", new Primitive(bet));
        p.put("pot", new Primitive(pot));
        p.put("minimum", new Primitive(JETONS_MIN));
        p.put("cards_left", new Primitive(etat.nombreCarteRestantes()));
        p.put("cards_left_on_table", new Primitive(etat.nombreCartesTable()));
        p.put("number_players", new Primitive(etat.nombreJoueurs()));
        p.put("small_blind", new Primitive(etat.smallBlind()));
        p.put("state", new Primitive(etat.etape().toString()));
        p.put("mode", new Primitive(etat.modeDeJeu().toString()));
        String current = joueurs.get(joueurID <= -1 ? 0 : joueurID).joueur.NOM;
        p.put("current_player", new Primitive(current));
        Connection.envoyer("game_update", p);
        MData p2 = new MData();
        for(int i = 0; i < table.taille(); i++){
            p2.put("card_" + i, new Primitive(table.carte(i).getValeur()));
            p2.put("color_" + i, new Primitive(table.carte(i).couleurToString()));
        }
        Connection.envoyer("table_update", p2);
        System.gc();
    }
    
    /**
     * Envoyer la commande "player_update" aux clients.
     * @param joueur 
     */
    private void notifierServeurJoueur(InfosJoueur joueur){
        MData m = new MData();
        m.put("name", new Primitive(joueur.joueur.NOM));
        if(joueur.main != null)
            for(int i = 0; i < joueur.main.taille(); i++){
                m.put("card_" + i, new Primitive(joueur.main.carte(i).getValeur()));
                m.put("color_" + i, new Primitive(joueur.main.carte(i).couleurToString()));
            }
        m.put("total_money", new Primitive(joueur.somme()));
        m.put("payed_money", new Primitive(joueur.sommePayee));
        m.put("all_in", new Primitive(joueur.tapis));
        m.put("hand_value", new Primitive(joueur.valeurMain));
        m.put("this_turn", new Primitive(joueur.ceTour));
        Connection.envoyer("player_update", m);
        System.gc();
    }
    
    public static enum Etape{
        /** Quand la partie n'est pas encore commencée ; les nouveaux joueurs sont acceptés. */
        EN_ATTENTE,
        
        /** Chaque joueur connaît ses cartes mais aucune n'est encore au centre. */
        PRE_FLOP,
        
        /** Il y a trois cartes au centre. */
        FLOP,
        
        /** Il y a quatre cartes au centre. */
        TURN,
        
        /** Il y a cinq cartes au centre. */
        RIVER,
        
        /** Chaque joueurID montre ses cartes, la meilleur combinaison gagne. */
        SHOWDOWN
    }
    
    /**
     * Pour savoir si le jeu est en mode {@link #NO_LIMIT} ou en mode {@link #LIMIT}.
     */
    public static enum Mode{
        /** Les joueurs relancent de la somme qu'ils veulent. */
        NO_LIMIT,
        /** Les joueurs ne choisissent pas de combien la relance est. */
        LIMIT
    }
    
    public class InfosJoueur implements Comparable<InfosJoueur> {
        private int sommePayee;
        private boolean ceTour = false;
        private Joueur joueur;
        private float valeurMain = 0;
        private boolean tapis = false;
        private Paquet main = null;
        
        public InfosJoueur(Joueur j){
            sommePayee = 0;
            joueur = j;
        }
        
        //private void 
        
        public Joueur joueur(){return joueur;}
        
        public int sommePayee(){return sommePayee;}
        
        public int somme(){return joueur.somme();}
        
        public void reset(){ if(!tapis) sommePayee = 0; ceTour = false;}
        
        public boolean aJoueCeTour(){
            return ceTour;
        }
        
        public int payer(int somme){
            sommePayee += joueur.payer(somme);
            return sommePayee;
        }

        @Override
        public int compareTo(InfosJoueur o) {
            if(etape == Etape.SHOWDOWN){
                return (int) (valeurMain - o.valeurMain);
            }else{
                return 0;
            }
        }
    }
    
    private static boolean contientJoueur(ArrayList<InfosJoueur> list, Joueur j){
        for(InfosJoueur infos : list)
            if(infos.joueur == j)
                return true;
        return false;
    }
    
    private static void supprimerJoueur(ArrayList<InfosJoueur> list, Joueur j){
        for(int i = 0; i < list.size(); i++)
            if(list.get(i).joueur == j){
                list.remove(i);
                i--;
            }
    }
    
    private static void resetJoueurs(ArrayList<InfosJoueur> list){
        Task.info("Reset des sommes déjà payées...");
        for(InfosJoueur infos : list)
            infos.reset();
        Task.info("Reset terminé.");
    }
    
    private static void resetCeTour(ArrayList<InfosJoueur> list){
        Task.info("Reset des tours payés");
        for(InfosJoueur infos : list)
            infos.ceTour = false;
        Task.info("Terminé.");
    }
    
    private final EtatJeu etat;
    public EtatJeu etat(){
        return etat;
    }
    
    /**
     * Permet d'obtenir des informations sur le jeu. Cette classe ne contient que
     * des accesseurs, elle ne permet pas de modifier la partie en cours d'une
     * quelconque manière.
     */
    public class EtatJeu extends Etat {
        
        @Override
        public int nombreCarteRestantes(){
            return pioche.taille();
        }
        
        @Override
        public int nombreJoueurs(){
            return joueurs.size();
        }
        
        @Override
        public int nombreCartesTable(){
            return table.taille();
        }
        
        @Override
        public int pot(){
            return pot;
        }
        
        @Override
        public int bet(){
            return bet;
        }
        
        @Override
        public Mode modeDeJeu(){
            return MODE_DE_JEU;
        }
        
        @Override
        public Etape etape(){
            return etape;
        }
        
        @Override
        public int smallBlind(){
            return SMALL_BLIND;
        }
        
        @Override
        public int bigBlind(){
            return SMALL_BLIND * 2;
        }
        
        @Override
        public Paquet table(){
            return new Paquet(table);
        }
        
        @Override
        public int jetonsMin(){
            return JETONS_MIN;
        }

        @Override
        public void drawListe(PGraphics g) {
            g.pushMatrix();
            g.translate(50, 50);
            for(int i = 0; i < joueurs.size(); i++){
                InfosJoueur infos = joueurs.get(i);
                g.fill(infos.joueur.humain() ? 45 : 25);
                g.translate(0, 50);
                float width = 200 + (i == joueurID ? 20 : 0);
                g.stroke(50);
                g.strokeWeight(2);
                g.rect(0, 0, width, 50);
                g.strokeWeight(1);
                g.line(0, 15, width, 15);
                g.textSize(13);

                g.textAlign(LEFT, TOP);
                g.fill(255);
                g.text(infos.joueur().NOM, 5, 1, width, 17);
                g.textAlign(RIGHT, TOP);
                g.text("" + infos.somme(), width-5, 1);
                g.fill(125);
                g.textSize(25);
                g.text((infos.tapis ? "Tapis : " : "") + infos.sommePayee, width-5, 16);

                if(etape == Etape.SHOWDOWN && infos.main != null){
                    // Afficher les cartes du joueur
                    g.image(FenetreDeJeu.carte(infos.main.carte(0)), 3, 18, 20, 29);
                    g.image(FenetreDeJeu.carte(infos.main.carte(1)), 26, 18, 20, 29);
                }
            }
            g.popMatrix();
        }

        @Override
        public void drawTable(PGraphics g, float x, float y) {
            table.draw(g, x, y);
        }
    }
    
}
