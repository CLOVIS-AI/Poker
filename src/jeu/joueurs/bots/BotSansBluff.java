/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu.joueurs.bots;

import cartes.Couleur;
import cartes.Paquet;
import jeu.Etat;
import jeu.joueurs.Action;
import logger.Task;
import poker.Poker;
import static processing.core.PApplet.abs;

/**
 * Un bot pour faire des tests
 * @author Raphael Bouchy
 */
public class BotSansBluff extends Bot {

    //variables

    public BotSansBluff(String nom) {
        super(nom); 
        //initialisation
    }

    @Override
    public Action reflechir(){
	Task.info(NOM, "va jouer ...");
        
        //Actions
        Task.info(NOM, "Récupération des données ...");
        Paquet cartes = paquet();
        Etat etat = Poker.ecran.etat();
        Paquet table = etat.table();
        
        Task.info(NOM, "Choix de l'action ...");
        switch(etat.etape()){
            case PRE_FLOP:
                //Pour le préflop
                Task.info(NOM, "Pre-flop :");
                if(cartes.carte(0).getValeurAs() == cartes.carte(1).getValeurAs()){  //Si c'est une paire
                    Task.info(NOM, "Paire :");
                    if(cartes.carte(0).getValeurAs() >= 9){                     //Si c'est plus que 9
                        Task.info(NOM, "Paire plus grande que 9");
                        return new Action(etat.bigBlind() * 4);
                    }else{                                                      //Si c'est moins de 9
                        Task.info(NOM, "Paire plus petite que 9");
                        return new Action(etat.bigBlind() * 2);
                    }
                }else if(cartes.carte(0).getValeurAs() + cartes.carte(1).getValeurAs() >= 20){  //à refaire : si des têtes
                    Task.info(NOM, "Têtes");
                    return new Action((int) (etat.bigBlind() * 1.5f));
                }else if(cartes.carte(0).getCouleur() == cartes.carte(1).getCouleur()){ // si même couleur
                    Task.info(NOM, "Couleur");
                    return new Action(etat.bigBlind());
                }else if(abs(cartes.carte(0).getValeurAs() - cartes.carte(1).getValeurAs()) == 1){
                    Task.info(NOM, "Suite");
                    return new Action(etat.bigBlind());
                }else if(etat.bet() <= etat.bigBlind() * 2){
                    Task.info(NOM, "Jeu pourri, mais le bet est faible");
                    return new Action(Action.Act.CALL);
                }else{
                    Task.info(NOM, "Jeu pourri, le bet est trop élevé");
                    return new Action(Action.Act.FOLD);
                }
            case FLOP: case RIVER: case TURN:
                Task.info("Flop ou après");
                //pour le flop
		
		
		//Compter les cartes
                Task.info("Compter les cartes");
		int[] histo = new int [13];
                for (int i = 0; i<table.taille(); i++){
                    histo[table.carte(i).getValeurAs()-2]++; // création du tableau de stockage
		}
				
		//Préparer
                Task.info("Préparer les variables");
		int brelans = 0; //Initialisation valeurs
                int paires = 0;
                int carres = 0;
                
		
                //Analyse
                Task.info("Analyse de l'histogramme des paires");
                for (int i =0; i<histo.length; i++) {        // tableau pour trier les cartes ==> valeur de carte associé à une case
                    if (histo[i]==2){   paires+=i;}			 // si une case du tableau vaut 2 alors ajouter une paire
                    else if (histo[i]==3) {brelans+=i;}		 // si une case du tableau vaut 3 alors ajouter un brelan
                    else if (histo[i]==4) {carres+=i;}		 // si une case du tableau vaut 4 alors ajouter un carré
                }
                
		//Traitement
                Task.info("Traitement de l'histogramme des paires");
                if (carres >= 1) { return new Action (etat.bet() * 5);} // si c'est  un carré relancer de 5X la mise
                if (brelans >= 1 && paires >= 1) {return new Action (etat.bet() * 5);} // si c'est un brelan ou 2 brelans relancer de 5X la mise
                if (brelans >=1 || paires>=2 ) {return new Action (etat.bet() * 3);} // si c'est deux paires ou plus relancer de X3
                if (paires == 1) {return new Action (Action.Act.CALL);} //si paire go suivre  faudré rajouter une limite pour suivre
                
                //Couleurs ?
                Task.info("Couleurs ?");
                
                //Paquets
                Task.info("Initialisation du paquet total");
                Paquet total = new Paquet(cartes, table);
                
                //Histogramme
                Task.info("Création de l'histogramme des couleurs");
                int[] histc = new int[4];
                for(int i = 0; i < total.taille(); i++)
                    histc[couleur(total.carte(i).getCouleur())]++;
                
                //Analyse
                Task.info("Analyse de l'histogramme des couleurs");
                for(int i = 0; i < histc.length; i++){
                    if(histc[i] >= Poker.ecran.min(total.taille(), 5)){
                        Task.info("Couleur !");
                        return new Action(1_000_000);
                    }
                }
            }
        return new Action(Action.Act.CALL);
    }  
    
    public int couleur (Couleur c) {                        // On associe des couleurs a une valeur numérique
        switch (c) {
            case CARREAU : return 0;
            case COEUR : return 1;
            case PIQUE : return 2;
            case TREFLE : return 3;
        } 
        return -1;
    }

}