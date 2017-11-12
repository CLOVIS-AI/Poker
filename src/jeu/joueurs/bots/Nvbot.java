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
public class Nvbot extends Bot {
    
    private int bluff;
    private int brelans = 0; //Initialisation valeurs
    private int paires = 0;
    private int carres = 0;

    public Nvbot(String nom) {
        super(nom);
        this.bluff = 0;
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
                bluff = (int) Poker.ecran.random(0,10);
                if (bluff == 9) {return new Action (etat.bigBlind() * 100);}    // Pour faire tapis
                if (bluff >= 7) {return new Action (etat.bigBlind()*3);}       // en gros, cela simule le bluff, on relance  deux fois le blind pour forcer les joueurs a fold           
            
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
            case FLOP: 
                Task.info("Flop ou après");
                //pour le flop
                if (bluff == 9) {return new Action (etat.bigBlind() * 100);}   // j'e
		if (bluff >= 7) {return new Action (etat.bigBlind()*2);} // VOILA ON GALR DONC ON REMET MAIS SA GENE PAS
		
		//Compter les cartes
                Task.info("Compter les cartes");
		int[] histo = new int [13];
                for (int i = 0; i<table.taille(); i++){
                    histo[table.carte(i).getValeurAs()-2]++; // création du tableau de stockage
		}
				
		//Préparer
                Task.info("Préparer les variables");
		brelans = 0; //Initialisation valeurs
                paires = 0;
                carres = 0;
                
			
                //Analyse
                Task.info("Analyse de l'histogramme des paires");
                for (int i =0; i<histo.length; i++) {        // tableau pour trier les cartes ==> valeur de carte associé à une case
                    if (histo[i]==2){   paires+=i;}			 // si une case du tableau vaut 2 alors ajouter une paire
                    else if (histo[i]==3) {brelans+=i;}		 // si une case du tableau vaut 3 alors ajouter un brelan
                    else if (histo[i]==4) {carres+=i;}		 // si une case du tableau vaut 4 alors ajouter un carré
                }
                
		//Traitement
                Task.info("Traitement de l'histogramme des paires");    
                if (carres >= 1) { return new Action (Action.Act.CALL);}                                // CES MAINS SONT CONSIDEREES COMME FORTES LE BUT DE LA MANOEUVRE C'EST DE FAIRE CROIRE QUE T'AS RIEN AVEC UN CALL OU CHECK PUIS RAISE A LA RIVER TU PIEGE TON PSYCHOLOGIQUEMENT TON ADVERSAIRE
                
                if (brelans >= 1 && paires >= 1) {return new Action (Action.Act.CALL);} 
                if (brelans >=1 || paires>=2 ) {return new Action (Action.Act.CALL);} 
                if (paires == 1 && etat.bet() < 300) {return new Action (Action.Act.CALL);} // MODIF ICI SUR LE CALL MAX jsuis pas sur
                
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
            case RIVER:
            case TURN: 
                if (brelans >= 1 && paires >=1){return new Action (etat.bet () * 5);}  // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT // VOILA LE SHCEMA DU CHECK RAISE EXPLIQUE PRECEDEMMENT
                if (paires >= 1 || brelans >=1) {return new Action (etat.bet() * 5);}
                if (carres >= 0){return new Action (etat.bet() * 5);}
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
