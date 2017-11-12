/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu.joueurs;

import cartes.Paquet;
import logger.Task;
import online.Interact;
import poker.Connection;
import util.MData;
import util.Primitive;

/**
 * Un joueur connecté, vu par le serveur.
 * @author Ivan CANET
 */
public class ClientConnecte extends Joueur implements Interact {
    
    private final String ip;
    private Action derniere = null;
    
    public ClientConnecte(String nom, String ip) {
        super(nom);
        this.ip = ip;
        if(!Connection.register("play_" + NOM, this)){
            System.err.println("N'a pas pu enregistrer la commande.");
            poker.Poker.ecran.exit();
        }
    }

    private boolean connaitCartes = false;
    @Override
    public Action jouer() {
        if(!connaitCartes || poker.Poker.ecran.random(0, 100) < 2){
            Paquet p = paquet();
            MData m = new MData();
            for(int i = 0; i < p.taille(); i++){
                m.put("card_" + i, new Primitive(p.carte(i).getValeur()));
                m.put("color_" + i, new Primitive(p.carte(i).couleurToString()));
            }
            Connection.envoyerA("your_cards", m, ip);
            connaitCartes = true;
        }
        derniere = null;
        MData m = new MData();
        m.put("null", new util.Primitive("null"));
        Connection.envoyerA("play", m, ip);
        return null;
    }

    @Override
    public Action derniereAction() {
        MData m = new MData();
        m.put("null", new util.Primitive("null"));
        Connection.envoyerA("last", m, ip);
        return derniere;
    }

    @Override
    public void receive(String command, MData parameters, String ip) {
        if(command.equals("play_" + NOM)){
            Task.begin(NOM + "a reçu un ordre : " + parameters.toString());
            String choix = ((Primitive)parameters.get("action")).getString();
            Task.info("Action : " + choix);
            switch(choix){
                case "fold":
                    Task.info("Fold");
                    derniere = new Action(Action.Act.FOLD);
                    break;
                case "call":
                    Task.info("Call");
                    derniere = new Action(Action.Act.CALL);
                    break;
                case "raise":
                    Task.info("Raise");
                    int valeur = Integer.valueOf(((Primitive)parameters.get("value")).getString());
                    Task.info("Valeur : " + valeur);
                    derniere = new Action(valeur);
                    break;
                default:
                    Task.info("########## Inconnu ##########");
            }
            Task.end("Fin de la réception.");
        }
    }
    
    @Override
    public void reset(){
        connaitCartes = false;
        super.reset();
    }
    
}
