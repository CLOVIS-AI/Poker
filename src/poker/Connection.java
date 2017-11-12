/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker;

import cartes.Carte;
import cartes.Couleur;
import cartes.Paquet;
import graphismes.FenetreDeJeu;
import graphismes.Notif.Type;
import java.util.ArrayList;
import jeu.Etat;
import jeu.Jeu;
import jeu.Jeu.Etape;
import jeu.joueurs.Action;
import jeu.joueurs.Humain;
import jeu.joueurs.Joueur;
import jeu.joueurs.ClientConnecte;
import logger.Task;
import objects.Disposer;
import online.Interact;
import online.TCP;
import online.TCPClient;
import online.TCPServer;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.RIGHT;
import static processing.core.PConstants.TOP;
import processing.core.PGraphics;
import util.MData;
import util.Primitive;

/**
 * Une classe gérant les connections avec le serveur/les clients
 * @author Ivan CANET
 */
public class Connection implements Interact {
    
    @Override
    public void receive(String command, MData parameters, String ip) {
        MData m;
        //Poker.ecran.notif(command, parameters.toString(), Notif.Type.DEBUG);
        switch(command){
            
            // ***************** SERVER SIDE ***********************************
            case "request_join":
                Task.begin("Nouvelle requête de connexion ...");
                if(etat != null && etat.etape() != Etape.EN_ATTENTE){
                    Task.info("La partie est déjà commencée, la requête est refusée.");
                    return;
                }
                String nom = ((Primitive)parameters.get("name")).getString();
                for(Joueur j : joueurs)
                    if(j.NOM.equals(nom)){
                        Task.end("Le nom a déjà été pris.");
                        return;
                    }
                Task.info("Le nom est disponible.");
                if(ip == null)
                    throw new IllegalArgumentException(ip + " ne devrait pas être null.");
                joueurs.add(new ClientConnecte(nom, ip));
                m = new MData();
                m.put("null", new Primitive("null"));
                envoyerA("join_accepted", m, ip);
                Task.end("Requête acceptée.");
                break;
            
            // ***************** CLIENT SIDE ***********************************
            case "join_accepted":
                Task.info("Requête acceptée !");
                joueurs.add(humain);
                break;
            case "launch_game":
                Task.info("Le serveur a lancé la partie");
                Poker.ecran.fenetreDeJeu = fenetreDeJeu;
                MData p;
                humain.reset();
                infos = new ArrayList<>();
                for(int i = 0; (p = parameters.get("player_" + i)) != null; i++){
                    Primitive n = (Primitive) p;
                    Task.info("Ajout du joueur : " + n);
                    infos.add(new InfosJoueur(n.getString()));
                }
                Task.info("Connection", "Lancement de la partie");
                etatClient = new EtatClient();
                etat = etatClient;
                fenetreDeJeu = new FenetreDeJeu(Poker.ecran, etat, humain);
                Poker.ecran.fenetreDeJeu = fenetreDeJeu;
                Disposer.put(0, 0, fenetreDeJeu);
                break;
            case "play":
                humain.jouer();
                break;
            case "last":
                Action a = humain.derniereAction();
                if(a != null){
                    m = new MData();
                    switch(a.type()){
                        case FOLD: m.put("action", new Primitive("fold")); for(int i = 0; i < infos.size(); i++) if(infos.get(i).NOM == humain.NOM) infos.remove(0); break;
                        case CALL: m.put("action", new Primitive("call")); break;
                        case RAISE: m.put("action", new Primitive("raise")); m.put("value", new Primitive(a.somme())); break;
                    }
                    link.send("play_" + humain.NOM, m);
                }
                break;
            case "game_update":
                etatClient.bet = Integer.valueOf(((Primitive)parameters.get("bet")).getString());
                etatClient.pot = Integer.valueOf(((Primitive)parameters.get("pot")).getString());
                etatClient.jetonsMin = Integer.valueOf(((Primitive)parameters.get("minimum")).getString());
                etatClient.nombreCarteRestantes = Integer.valueOf(((Primitive)parameters.get("cards_left")).getString());
                etatClient.nombreCartesTable = Integer.valueOf(((Primitive)parameters.get("cards_left_on_table")).getString());
                etatClient.nombreJoueurs = Integer.valueOf(((Primitive)parameters.get("number_players")).getString());
                etatClient.smallBlind = Integer.valueOf(((Primitive)parameters.get("small_blind")).getString());
                etatClient.etape = etape(((Primitive)parameters.get("state")).getString());
                etatClient.modeDeJeu = ((Primitive)parameters.get("mode")).getString().equals("NO_LIMIT") ? Jeu.Mode.NO_LIMIT : Jeu.Mode.LIMIT;
                prochainJoueur = ((Primitive)parameters.get("current_player")).getString();
                break;
            case "table_update":
                etatClient.table = new Paquet();
                for(int i = 0; (m = parameters.get("card_" + i)) != null; i++){
                    etatClient.table.ajouterCarte(
                            new Carte(
                                    Integer.valueOf(((Primitive)m).getString()), 
                                    couleur(((Primitive)parameters.get("color_" + i)).getString())
                            )
                    );
                }
                break;
            case "your_cards":
                for(int i = 0; (m = parameters.get("card_" + i)) != null; i++){
                    humain.ajouterCarte(
                            new Carte(
                                    Integer.valueOf(((Primitive)m).getString()), 
                                    couleur(((Primitive)parameters.get("color_" + i)).getString())
                            )
                    );
                }
                break;
            case "player_update":
                String nomJoueur = ((Primitive)parameters.get("name")).getString();
                Task.info("Mise à jour du joueur : " + nomJoueur);
                for(InfosJoueur info : infos){
                    if(info.NOM.equals(nomJoueur)){
                        Task.info("Joueur trouvé");
                        info.ceTour = Boolean.valueOf(((Primitive)parameters.get("this_turn")).getString());
                        info.main = new Paquet();
                        for(int i = 0; (m = parameters.get("card_" + i)) != null; i++){
                            info.main.ajouterCarte(
                                    new Carte(
                                            Integer.valueOf(((Primitive)m).getString()), 
                                            couleur(((Primitive)parameters.get("color_" + i)).getString())
                                    )
                            );
                        }
                        info.somme = Integer.valueOf(((Primitive)parameters.get("total_money")).getString());
                        info.sommePayee = Integer.valueOf(((Primitive)parameters.get("payed_money")).getString());
                        if(info.NOM.equals(humain.NOM)){
                            humain.gagner(info.somme - humain.somme());
                        }
                        info.tapis = Boolean.valueOf(((Primitive)parameters.get("all_in")).getString());
                        info.valeurMain = Float.valueOf(((Primitive)parameters.get("hand_value")).getString());
                        break;
                    }
                }
                break;
            case "notif":
                Poker.ecran.notif(
                        ((Primitive)parameters.get("title")).getString().replace('_', ' ').replace(';', ','), 
                        ((Primitive)parameters.get("text")).getString().replace('_', ' ').replace(';', ','), 
                        type(((Primitive)parameters.get("type")).getString()));
                break;
            
            default:
                System.err.println("Commande inconnue : " + command);
        }
    }
    
    // ************************* STATIC ****************************************
    
    private static TCP link;
    private static Connection co;
    
    private static FenetreDeJeu fenetreDeJeu;
    private static Jeu jeu;
    private static Etat etat;
    private static EtatClient etatClient;
    private static final ArrayList<Joueur> joueurs;
    private static Humain humain;
    
    private static boolean serveur = true;
    
    static{
        co = new Connection();
        joueurs = new ArrayList<>();
    }
    
    public static void nouveauServeur(int port, Jeu.Mode mode, int smallBlind, int jetonsMin, Humain joueur){
        Task.begin("Lancement comme Serveur ...");
        serveur = true;
        link = new TCPServer(Poker.ecran, port);
        
        Task.info("Enregistrement des commandes");
        link.registerCmd("request_join", co);
        
        Task.info("Création de la partie");
        jeu = new Jeu(mode, smallBlind, jetonsMin);
        humain = joueur;
        joueurs.add(joueur);
        Task.end("Server prêt.");
    }
    
    public static void nouveauClient(String ip, int port, String nom){
        Task.begin("Lancement comme Client ...");
        serveur = false;
        link = new TCPClient(Poker.ecran, ip, port);
        
        Task.info("Enregistrement des commandes");
        link.registerCmd("join_accepted", co);
        link.registerCmd("launch_game", co);
        link.registerCmd("play", co);
        link.registerCmd("last", co);
        link.registerCmd("game_update", co);
        link.registerCmd("table_update", co);
        link.registerCmd("player_update", co);
        link.registerCmd("your_cards", co);
        link.registerCmd("notif", co);
        
        Task.info("Initialisation des variables");
        humain = new Humain(nom);
        
        Task.info("Demande de connexion");
        MData parametres = new MData();
        parametres.put("name", new Primitive(nom));
        link.send("request_join", parametres);
        Task.end("Client prêt.");
    }
    
    public static void mettreAJour(){
        //Task.info("Mise à jour");
        if(link != null)
            link.update();
        if(jeu != null)
            jeu.mettreAJour();
    }
    
    public static void quitter(){
        Task.begin("Quitter la partie");
        link.stop();
        fenetreDeJeu = null;
        jeu = null;
        joueurs.clear();
        Disposer.put(0, 0, Poker.ecran.menuGeneral);
        Task.end("Retour au menu général");
    }
    
    public static void envoyer(String commande, MData parametres){
        if(link != null)
            link.send(commande, parametres);
    }
    
    public static void envoyerSiServeur(String commande, MData parametres){
        if(serveur)
            envoyer(commande, parametres);
    }
    
    /**
     * Envoie un message à un client déterminé, ou au serveur si je suis un client.
     * @param commande la commande à envoyer
     * @param parametres les paramètres de cette commande
     * @param ip IP du client désigné
     */
    public static void envoyerA(String commande, MData parametres, String ip){
        if(link != null && serveur){
            ((TCPServer)(link)).sendTo(commande, parametres, ip);
        }else if(link != null){
            link.send(commande, parametres);
        }
    }
    
    public static boolean register(String commande, Interact objet){
        if(link != null){
            link.registerCmd(commande, objet);
            return true;
        }
        return false;
    }
    
    public static int nJoueurs(){
        return joueurs.size();
    }
    
    /**
     * Sommes-nous un serveur ?
     * @return <code>true</code> si cet objet est un serveur.
     */
    public static boolean serveur(){ return serveur; }
    
    public static void lancer(){
        if(serveur){
            MData m = new MData();
            etat = jeu.etat();
            fenetreDeJeu = new FenetreDeJeu(Poker.ecran, etat, humain);
            Poker.ecran.fenetreDeJeu = fenetreDeJeu;
            Poker.ecran.jeu = jeu;
            for(int i = 0; i < joueurs.size(); i++){
                jeu.rejoindrePartie(joueurs.get(i));
                m.put("player_" + i, new Primitive(joueurs.get(i).NOM));
            }
            Disposer.put(0, 0, fenetreDeJeu);
            Task.info("Lancement de la partie ; envoi des infos aux joueurs : " + m.toString());
            m.put("small_blind", new Primitive(jeu.etat().smallBlind()));
            m.put("minimum", new Primitive(jeu.etat().jetonsMin()));
            m.put("mode", new Primitive(jeu.etat().modeDeJeu().toString()));
            link.send("launch_game", m);
            jeu.commencerPartie(); //lancer la partie après avoir prévenu tout le monde
            System.gc(); // Nettoyage
        }
    }
    
    public static void relancer(){
        jeu = new Jeu(jeu.etat().modeDeJeu(), jeu.etat().smallBlind(), 0);
        for(Joueur j : joueurs)
            j.reset();
        lancer();
    }
    
    public static void ajouterJoueur(Joueur j){
        joueurs.add(j);
    }
    
    public static Etat etat(){
        return etat;
    }
    
    private static ArrayList<InfosJoueur> infos;
    private String prochainJoueur;
    
    private class EtatClient extends Etat {

        @Override
        public void drawListe(PGraphics g) {
            g.pushMatrix();
            g.translate(50, 50);
            for(int i = 0; i < infos.size(); i++){
                InfosJoueur info = infos.get(i);
                g.fill((info.NOM == null ? humain.NOM == null : info.NOM.equals(humain.NOM)) ? 45 : 25);
                g.translate(0, 50);
                float width = 200 + ((info.NOM == null ? prochainJoueur == null : info.NOM.equals(prochainJoueur)) ? 20 : 0);
                g.stroke(50);
                g.strokeWeight(2);
                g.rect(0, 0, width, 50);
                g.strokeWeight(1);
                g.line(0, 15, width, 15);
                g.textSize(13);

                g.textAlign(LEFT, TOP);
                g.fill(255);
                g.text(info.NOM, 5, 1, width, 17);
                g.textAlign(RIGHT, TOP);
                g.text("" + info.somme, width-5, 1);
                g.fill(125);
                g.textSize(25);
                g.text((info.tapis ? "Tapis : " : "") + info.sommePayee, width-5, 16);

                if(etat.etape() == Jeu.Etape.SHOWDOWN && info.main != null){
                    // Afficher les cartes du joueur
                    g.image(FenetreDeJeu.carte(info.main.carte(0)), 3, 18, 20, 29);
                    g.image(FenetreDeJeu.carte(info.main.carte(1)), 26, 18, 20, 29);
                }
            }
            g.popMatrix();
        }

        private int nombreCarteRestantes;
        @Override
        public int nombreCarteRestantes() {
            return nombreCarteRestantes;
        }

        private int nombreJoueurs;
        @Override
        public int nombreJoueurs() {
            return nombreJoueurs;
        }

        private int nombreCartesTable;
        @Override
        public int nombreCartesTable() {
            return nombreCartesTable;
        }

        private int pot;
        @Override
        public int pot() {
            return pot;
        }

        private int bet;
        @Override
        public int bet() {
            return bet;
        }

        private Jeu.Mode modeDeJeu = Jeu.Mode.NO_LIMIT;
        @Override
        public Jeu.Mode modeDeJeu() {
            return modeDeJeu;
        }

        private Jeu.Etape etape = Jeu.Etape.EN_ATTENTE;
        @Override
        public Jeu.Etape etape() {
            return etape;
        }

        private int smallBlind;
        @Override
        public int smallBlind() {
            return smallBlind;
        }

        @Override
        public int bigBlind() {
            return smallBlind * 2;
        }

        private Paquet table = new Paquet(0);
        @Override
        public Paquet table() {
            return table;
        }

        private int jetonsMin;
        @Override
        public int jetonsMin() {
            return jetonsMin;
        }

        @Override
        public void drawTable(PGraphics g, float x, float y) {
            table.draw(g, x, y);
        }
        
    }
    
    private class InfosJoueur{
        private int sommePayee, somme;
        private boolean ceTour = false;
        private final String NOM;
        private float valeurMain = 0;
        private boolean tapis = false;
        private Paquet main = null;
        
        public InfosJoueur(String nom){
            sommePayee = 0;
            this.NOM = nom;
        }
    }
    
    private static Etape etape(String etape){
        switch(etape){
            case "PRE_FLOP": return Etape.PRE_FLOP;
            case "FLOP": return Etape.FLOP;
            case "RIVER": return Etape.RIVER;
            case "TURN": return Etape.TURN;
            case "SHOWDOWN": return Etape.SHOWDOWN;
            default: return Etape.EN_ATTENTE;
        }
    }
    
    private static Couleur couleur(String couleur){
        switch(couleur){
            case "Pique": return Couleur.PIQUE;
            case "Cour": return Couleur.COEUR;
            case "Trefle": return Couleur.TREFLE;
            case "Carreau": return Couleur.CARREAU;
            default:
                System.err.println("Connection.couleur() : Couleur non reconnue !");
                return null;
        }
    }
    
    private static Type type(String type){
        switch(type){
            case "DEBUG": return Type.DEBUG;
            case "FAIBLE": return Type.FAIBLE;
            case "NORMAL": return Type.NORMAL;
            case "ORANGE": return Type.ORANGE;
            case "ROUGE": return Type.ROUGE;
            case "VIDE": return Type.VIDE;
            default:
                System.err.println("Connection.couleur() : Couleur non reconnue !");
                return null;
        }
    }
}
