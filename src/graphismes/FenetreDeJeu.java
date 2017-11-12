/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphismes;

import cartes.Carte;
import cartes.Couleur;
import graphismes.actions.BoutonAction;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import jeu.Jeu;
import jeu.joueurs.Action;
import jeu.joueurs.Humain;
import logger.Task;
import objects.MContainer;
import objects.MObject;
import objects.MWindow;
import objects.text.MIntArea;
import poker.Connection;
import poker.Poker;
import jeu.Etat;
import processing.core.PApplet;
import static processing.core.PConstants.BASELINE;
import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.TOP;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * La fenêtre du jeu
 * @author Ivan CANET
 */
public class FenetreDeJeu extends MWindow {

    // Technique
    private Etat etat;
    private final PApplet surface;
    private final Humain humain;
    
    // Notifs
    private Notifs notifs;
    
    // Graphique
    private ArrayList<Integer> pot, jetons;
    private int valeurPot, valeurBet, valeurJoueur;
    
    public FenetreDeJeu(PApplet surface, Etat etat, Humain humain) {
        super(0, 0, surface.width, surface.height);
        Task.begin("Création de la fenêtre de jeu ...");
        Task.info("Initialisation des paramètres ...");
        this.surface = surface;
        this.etat = etat;
        this.humain = humain;
        valeurPot = etat.pot();
        valeurBet = etat.bet();
        valeurJoueur = humain.somme();
        
        Task.info("Mesures ...");
        float offset = surface.width/50f;
        
        float largeurBoutons = surface.width/6f - offset,
              hauteurBoutons = surface.height/6f - offset;
        
        Task.info("Boutons ....");
        this.addObject(new BoutonFold(offset, surface.height - 2 * (hauteurBoutons + offset), largeurBoutons, hauteurBoutons, "Abandonner la partie en cours sans rien payer"));
        this.addObject(new BoutonCall(offset * 2 + largeurBoutons, surface.height - 2 * (hauteurBoutons + offset), largeurBoutons, hauteurBoutons, "Payer le bet"));
        this.addObject(new BoutonRaise(offset, surface.height - (hauteurBoutons + offset), largeurBoutons, hauteurBoutons));
        this.addObject(new BoutonAllIn(offset * 2 + largeurBoutons, surface.height - (hauteurBoutons + offset), largeurBoutons, hauteurBoutons, "Mettre tout votre argent en jeu"));
        
        this.addObject(new BoutonAjout(surface.width - 100, surface.height - 30, 95, 25, "Quitter"){
            @Override
            public void onMousePressed(){
                Connection.quitter();
            }
        });
        
        Task.info("Notifications ...");
        notifs = new Notifs(surface.width - offset*4 - largeurBoutons, surface.height - offset - hauteurBoutons, largeurBoutons + offset*4, hauteurBoutons);
        this.addObject(notifs);
        notif(new Notif("Début de la partie", "Amusez-vous bien !"));
        
        Task.end("La fenêtre de jeu est prête.");
    }
    
    private float alphaCard = 0;
    @Override
    public void draw(PGraphics g){
        g.textFont(policeBoitesGrand);
        g.noStroke();
        g.background(16, 63, 9);
        g.fill(22, 122, 7);
        g.ellipseMode(CENTER);
        g.ellipse(g.width/2, g.height/2, g.width - 100, g.height - 100);
        g.textAlign(CENTER);
        g.fill(255, 50);
        g.textSize(100);
        g.text("Poker !", g.width/2, g.height/2 - 50);
        g.textSize(75);
        g.text("Ivan CANET & Raphaël BOUCHY", g.width/2, g.height/2 + 50);
        g.textSize(50);
        g.fill(255);
        valeurPot += (Poker.ecran.etat().pot() > valeurPot) ? 1 : (Poker.ecran.etat().pot() < valeurPot) ? -1 : 0;
        g.text(valeurPot, Poker.ecran.width/2, Poker.ecran.height/2 - 300);
        g.textSize(30);
        g.fill(206, 185, 181);
        valeurBet += (Poker.ecran.etat().bet() > valeurBet) ? 1 : (Poker.ecran.etat().bet() < valeurBet) ? -1 : 0;
        g.text(valeurBet, Poker.ecran.width/2, Poker.ecran.height/2 - 250);
        
        etat.drawTable(g, (float) (Poker.ecran.width/2 - 135 * 2.5), Poker.ecran.height/2 - 210);
        if(Poker.ecran.mouseY >= Poker.ecran.height - 200 && Poker.ecran.mouseX >= Poker.ecran.width/2 - 135 && Poker.ecran.mouseX < Poker.ecran.width/2 - 135 + 265){
            alphaCard -= 15;
            if(alphaCard < 0) alphaCard = 0;
        }else{
            alphaCard += 15;
            if(alphaCard > 255) alphaCard = 255;
        }
        humain.draw(g, (float) (Poker.ecran.width/2 - 135), Poker.ecran.height - 200);
        g.fill(22, 122, 7, alphaCard);
        g.rect(Poker.ecran.width/2 - 135, Poker.ecran.height - 200, 265, 130*1.414f);
        g.fill(255, alphaCard);
        g.textSize(30);
        g.textAlign(CENTER, CENTER);
        g.text("Survoler pour voir\nles cartes", Poker.ecran.width/2, Poker.ecran.height - 100);
        
        etat.drawListe(g);
        
        g.textSize(50);
        g.fill(255);
        g.textAlign(CENTER, BASELINE);
        valeurJoueur += (humain.somme() > valeurJoueur) ? 1 : (humain.somme() < valeurJoueur) ? -1 : 0;
        g.text(valeurJoueur, Poker.ecran.width/2, Poker.ecran.height/2 + 150);
        
        g.textFont(policeBoitesPetit);
        super.draw(g);
    }
    
    public final void notif(Notif n){
        notifs.addObject(n);
    }
    
    private class BoutonFold extends BoutonAction {
        public BoutonFold(float pX, float pY, float sX, float sY, String description) {super(pX, pY, sX, sY, "Fold", description);}
        @Override
        public void onMousePressed(){ if(humain.doitJouer()){ humain.repondre(new Action(Action.Act.FOLD));} }
        @Override
        public void draw(PGraphics g){ if(humain.doitJouer()) super.draw(g); }
    }
    
    private class BoutonCall extends BoutonAction {
        public BoutonCall(float pX, float pY, float sX, float sY, String description) {super(pX, pY, sX, sY, "Call", description);}
        @Override
        public void onMousePressed(){ if(humain.doitJouer() && humain.somme() >= Poker.ecran.etat().bet()){ humain.repondre(new Action(Action.Act.CALL));} }
        @Override
        public void draw(PGraphics g){ if(Poker.ecran.etat().bet() == 0){ title = "Check"; description = "Continuer sans payer"; }else{ title = "Call"; description = "Egaliser " + Poker.ecran.etat().bet() + " jetons";} if(humain.doitJouer() && humain.somme() >= Poker.ecran.etat().bet()) super.draw(g); }
    }
    
    private class BoutonRaise extends MContainer {
        
        MIntArea raise;
        
        public BoutonRaise(float pX, float pY, float sX, float sY) {
            super(pX, pY, sX, sY);
            Task.begin("Création du bouton RAISE");
            Task.info("Etat vaut 'null' ? " + (etat == null ? "Oui" : "Non"));
            Task.info("Ajout du bouton augmentant le RAISE");
            addObject(new BoutonAjout(pX + sX/8, pY + sY/2, sX/8, sX/8, "+"){
                @Override
                public void onMousePressed(){
                    Task.info("Augmentation de la valeur du RAISE");
                    if(etat.modeDeJeu() == Jeu.Mode.LIMIT){ raise.setContent(etat.bet()); }
                    else                                  { raise.setContent(etat.bet() + raise.getContent().getInt()); }
                }
            });
            Task.info("Ajout de la zone de texte");
            raise = new Ecrire(pX + 2*sX/8, pY + sY/2, 3*sX/8, sX/8);
            raise.setContent(etat.bigBlind());
            addObject(raise);
            Task.info("Ajout du bouton diminuant le RAISE");
            addObject(new BoutonAjout(pX + 5*sX/8, pY + sY/2, sX/8, sX/8, "-"){
                @Override
                public void onMousePressed(){
                    Task.info("Diminution de la valeur du RAISE");
                    if(etat.modeDeJeu() == Jeu.Mode.LIMIT){ raise.setContent(etat.bet()); }
                    else                                  { int valeurRaise = raise.getContent().getInt() - etat.bigBlind(); if(valeurRaise < 0) valeurRaise = 0; raise.setContent(valeurRaise); }
                }
            });
            Task.info("Ajout du bouton pour effectuer le RAISE");
            addObject(new BoutonAjout(pX + 6*sX/8, pY + sY/2, sX/8, sX/8, "›"){
                @Override
                public void onMousePressed(){
                    raise.onMousePressed();
                }
            });
            Task.end("Bouton créé.");
        }
        
        @Override
        public void draw(PGraphics g){
            if(!humain.doitJouer()) return;
            if(!isFocus()){g.fill(96, 90, 89);}
            else{          g.fill(116, 110, 109);}
            g.stroke(63, 59, 58);
            g.rect(getPosX(), getPosY(), getSizeX(), getSizeY(), 5);
            g.line(getPosX(), getPosY() + 20, getPosX() + getSizeX(), getPosY() + 20);
            g.textSize(20);
            g.textAlign(LEFT, BASELINE);
            g.fill(209, 191, 188);
            g.text("RAISE", getPosX()+5, getPosY()-1, getSizeX()-10, 25);
            g.textSize(13);
            g.text("Augmenter la somme du bet", getPosX()+3, getPosY()+21, getSizeX()-6, getSizeY()-15);
            super.draw(g);
        }
        
        @Override
        public boolean isWritable(){
            return true;
        }
        private class Ecrire extends MIntArea {
            public Ecrire(float pX, float pY, float sX, float sY) {
                super(pX, pY, sX, sY, true, etat.bigBlind(), null, null, false);
            }
            @Override
            public void draw(PGraphics g) {
                if(!isFocus()){g.fill(96, 90, 89);}
                else{          g.fill(131, 125, 115);}
                g.stroke(63, 59, 58);
                g.rect(getPosX(), getPosY(), getSizeX(), getSizeY(), 5);
                g.fill(255);
                g.textAlign(LEFT, TOP);
                g.textSize(20);
                g.text(getContent().getInt(), getPosX() + 5, getPosY() + 3);
            }
            @Override
            public void onMousePressed(){
                int valeurRaise = getContent().getInt();
                Task.info("Humain : RAISE de " + valeurRaise);
                humain.repondre(new Action(valeurRaise));
            }
        }
    }
    
    private class BoutonAjout extends MObject {    
        private final String text;
        public BoutonAjout(float pX, float pY, float sX, float sY, String t) { super(pX, pY, sX, sY); text = t;}
        @Override
        public void draw(PGraphics g) {
            if(!isFocus()){g.fill(96, 90, 89);}
            else{          g.fill(131, 125, 115);}
            g.stroke(63, 59, 58);
            g.rect(getPosX(), getPosY(), getSizeX(), getSizeY(), 5);
            g.fill(255);
            g.textAlign(CENTER, TOP);
            g.textSize(20);
            g.text(text, getPosX() + getSizeX()/2, getPosY() + 3);
        }
    }
    
    private class BoutonAllIn extends BoutonAction {
        public BoutonAllIn(float pX, float pY, float sX, float sY, String description) {super(pX, pY, sX, sY, "All-in", description);}
        @Override
        public void onMousePressed(){ if(humain.doitJouer()){ humain.repondre(new Action(1_000_000));} }
        @Override
        public void draw(PGraphics g){ if(humain.doitJouer()) super.draw(g); }
    }
    
    public Etat etat(){
        return etat;
    }
    
    // ***************************** STATIC ************************************
    
    private static PImage fond;
    
    private static HashMap<Carte, PImage> cartes;
    private static PImage dos;
    
    private static PFont policeBoitesPetit, policeBoitesGrand;
    
    /** Liste des extensions autorisées. (voir la méthode {@link processing.core.PApplet#loadImage(java.lang.String) } pour plus d'informations.) */
    public static final String[] extensions;
    
    private static final String PACK_DEFAUT = Poker.ecran.DATA_PATH + "/packs/defaut";
    
    static{
        extensions = new String[]{".png", ".jpg", ".tga", ".gif", ".jpeg"};
        charger(PACK_DEFAUT);
    }
    
    public static void charger(String pack){
        Task.begin("Chargement des images ...");
        
        Task.info("Fond ...");
        fond = chargerImage(pack, "fond");
        Poker.ecran.readynessState++;
        
        Task.info("Police de caractères ...");
        policeBoitesPetit = Poker.ecran.loadFont(pack + "/" + "font_petit.vlw");
        Poker.ecran.readynessState++;
        policeBoitesGrand = Poker.ecran.loadFont(pack + "/" + "font_grand.vlw");
        Poker.ecran.readynessState++;
        
        Task.info("Chargement des cartes ...");
        cartes = new HashMap<>(52);
        for(int i = 1; i <= 13; i++){
            cartes.put(new Carte(i, Couleur.PIQUE),   chargerImage(pack, i + "_pique"));  Poker.ecran.readynessState++;
            cartes.put(new Carte(i, Couleur.COEUR),   chargerImage(pack, i + "_coeur"));  Poker.ecran.readynessState++;
            cartes.put(new Carte(i, Couleur.TREFLE),  chargerImage(pack, i + "_trefle")); Poker.ecran.readynessState++;
            cartes.put(new Carte(i, Couleur.CARREAU), chargerImage(pack, i + "_carreau"));Poker.ecran.readynessState++;
        }
        
        Task.begin("Cleaning ...");
        System.gc();
        Task.end("Finished cleaning.");
        Task.end("Les images sont prêtes.");
    }
    
    private static PImage chargerImage(String pack, String nom){
        File t;
        String fichier = pack + "/" + nom;
        for(String extension : extensions)
            if(new File(fichier + extension).exists()){
                Task.info("Chargement de " + fichier + extension + " réussi.");
                PImage ret = Poker.ecran.loadImage(fichier + extension);
                ret.resize(130, 0);
                return ret;
            }
        System.err.println("Impossible de trouver l'image " + fichier + ".");
        if(!pack.equals(PACK_DEFAUT)){
            Task.info("Recherche dans 'defaut'");
            return chargerImage(PACK_DEFAUT, nom);
        }
        System.err.println("L'image n'est pas présente dans 'defaut', il est impossible de continuer.");
        System.exit(5);
        return null;
    }
    
    public static PImage carte(Carte c){
        return cartes.get(c);
    }
}
