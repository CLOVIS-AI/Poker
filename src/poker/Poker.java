/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker;

import cartes.Carte;
import cartes.Couleur;
import graphismes.FenetreDeJeu;
import graphismes.MenuGeneral;
import graphismes.Notif;
import java.io.File;
import java.net.URISyntaxException;
import jeu.Etat;
import jeu.Jeu;
import keys.Key;
import logger.Task;
import objects.Disposer;
import processing.core.PApplet;

/**
 *
 * @author Ivan CANET & Raphael BOUCHY
 * @since 27 fev. 2017
 */
public class Poker extends PApplet {
    
    public final String VERSION = "Finale";
    
    public final String DATA_PATH = getDataFolderPath();
    public static Poker ecran;
    private String getDataFolderPath(){ try {return new File(new File(Poker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getAbsolutePath() + File.separator + "data").getAbsolutePath();} catch (URISyntaxException ex) {java.util.logging.Logger.getLogger(Poker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);return "";}}
    
    /**
     * Envoi des données à Processing (ne pas utiliser/modifier)
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Task.begin();
        Task.info("Lancement du Poker ...");
        PApplet.main("poker.Poker");
    }
    
    /**
     * Settings d'affichage (taille d'écran)
     */
    @Override
    public void settings(){
        Task.begin("Settings ...");
        //size(800, 600);
        fullScreen();
        ecran = this;
        Task.info("Initialisation de la fenêtre effectuée.");
        
        Task.begin("Vérification de l'existence des dossiers ...");
        File[] dossiers = new File[]{
            new File(DATA_PATH),
            new File(DATA_PATH + "/packs"), 
            new File(DATA_PATH + "/packs/defaut")
        };
        for(File dossier : dossiers){
            Task.begin("Dossier " + dossier.getAbsolutePath());
            if(dossier.isDirectory()){
                Task.info("Ce dossier existe.");
            }else if(dossier.exists()){
                Task.info("Ce fichier existe, mais n'est pas un dossier.");
                Task.info("Suppression du fichier existant ...");
                dossier.delete();
                Task.info("Création du dossier ...");
                dossier.mkdir();
            }else{
                Task.info("Le dossier n'existe pas.");
                Task.info("Création du(/es) dossier(s) manquant(s).");
                dossier.mkdirs();
            }
            Task.end("Ce dossier est vérifié.");
        }
        Task.end("Vérification terminée.");
        
        Task.end("Settings terminés.");
        Task.begin("Lancement de Processing ...");
    }
    
    public Jeu jeu;
    public FenetreDeJeu fenetreDeJeu;
    public MenuGeneral menuGeneral;
    
    private boolean isReady = false;
    public int readynessState = 0;
    
    public Etat etat(){return Connection.etat();}
    
    public void notif(String title, String description){ if(fenetreDeJeu == null) return; fenetreDeJeu.notif(new Notif(title, description)); }
    public void notif(String title, String description, Notif.Type importance){ if(fenetreDeJeu == null) return; fenetreDeJeu.notif(new Notif(title, description, importance)); }
    
    /**
     * Initialisation des données
     */
    @Override
    public void setup(){
        Task.end("Lancement de Processing terminé.");
        Task.begin("Setup ...");
        frameRate(60);
        //frame.setResizable(true);
        
        thread("initialization");
        background(0);
        
        Task.end("Setup terminé !");
    }
    
    public void initialization(){
        Task.begin("Initialisation ...");
        /*Task.info("Initialisation de la partie ...");
        jeu = new Jeu(Jeu.Mode.NO_LIMIT, 10, 200);
        jeu.rejoindrePartie(new DumbBot("DB #1"));
        jeu.rejoindrePartie(new DumbBot("DB #2"));
        jeu.rejoindrePartie(new RandomBot("RB #1", 30, 60, 10, 0.5f));
        jeu.rejoindrePartie(new RandomBot("RB #2", 30, 60, 10, 0.1f));
        jeu.rejoindrePartie(new RandomBot("RB #3", 30, 60, 10, 0.2f));
        jeu.rejoindrePartie(new RandomBot("RB #4", 35, 60, 10, 0.2f));
        jeu.rejoindrePartie(new BotSansBluff("BotSansBluff"));
        jeu.rejoindrePartie(new BotSansBluff("Essayeur"));
        jeu.rejoindrePartie(new BotSansBluff("Raté"));
        Humain humain = new Humain("Humain");
        jeu.rejoindrePartie(humain);
        if(!jeu.commencerPartie())
            exit();
        
        Task.info("Création de la fenêtre de jeu :");
        fenetreDeJeu = new FenetreDeJeu(this, jeu, humain);
        
        Task.info("Ajout des fenêtres au Disposeur.");
        Disposer.put(1, 0, fenetreDeJeu);
        Disposer.put(0, 0, menuGeneral);*/
        
        FenetreDeJeu.carte(new Carte(1, Couleur.PIQUE));
        
        Task.info("Création du menu général :");
        menuGeneral = new MenuGeneral(this);
        Disposer.put(0, 0, menuGeneral);
        
        ellipseMode(CENTER);
        
        Task.end("Prêt.");
        isReady = true;
    }
    
    /**
     * Affichage
     */
    @Override
    public void draw(){
        if(!isReady){ drawLoadingScreen(); return; }
        textAlign(LEFT, BASELINE);
        strokeWeight(1.5f);
        surface.setTitle("Poker "+VERSION+" | Ivan CANET & Raphaël BOUCHY | FPS:" + (int)frameRate);
        thread("update");
        Disposer.draw(this);
        if(true){
            textAlign(LEFT, BASELINE);
            textSize(12);
            fill(255);
            int total = (int)(Runtime.getRuntime().totalMemory() * 0.000_001), maximum = (int)(Runtime.getRuntime().maxMemory() * 0.000_001);
            text("Informations\nFPS : " + (int)frameRate + "\nMemoire : " + total + " / " + maximum + " Mo ("+ (int)((float)total/maximum * 100) + " %)", 20, 30);
        }
    }
    
    private boolean updating = false;
    public void update(){
        if(updating) return;
        updating = true;
        try{
            //Task.info("Mise à jour");
            Connection.mettreAJour();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            updating = false;
        }
    }
    
    private float theta = 0;
    public void drawLoadingScreen(){
        surface.setTitle("Chargement ... \"Wave\" par Jerome Herr, FPS:"+frameRate);
        
        background(20);
        textAlign(LEFT, BASELINE);
        textSize(12);
        fill(255);
        int total = (int)(Runtime.getRuntime().totalMemory() * 0.000_001), maximum = (int)(Runtime.getRuntime().maxMemory() * 0.000_001);
            text("Informations\nFPS : " + (int)frameRate + "\nMemoire : " + total + " / " + maximum + " Mo ("+ (int)((float)total/maximum * 100) + " %)", 20, 30);
            
        // Source : https://www.openprocessing.org/sketch/152169
        int num =20;
        float step = 50, sz = 0, offSet, angle;
        translate(width/2, (float) (height*.75));
        angle=0;
        stroke(255);
        strokeWeight(5);
        for (int i=0; i<num; i++) {
            noFill();
            sz = i*step;
            offSet = TWO_PI/num*i;
            float arcEnd = map(sin(theta+offSet),-1,1, PI, TWO_PI);
            arc(0, 0, sz, sz, PI, arcEnd);
        }
        colorMode(RGB);
        fill(255);
        line(-sz/2, step/2, -sz/2 + map(readynessState, 0, 55, 0, sz), step/2);
        textAlign(CENTER, TOP);
        textSize(50);
        text("Chargement ... " + (int)map(readynessState, 0, 55, 0, 100) + " %", 0, step);
        resetMatrix();
        theta += .0523;
    }
    
    @Override
    public void mousePressed(){
        Disposer.onMousePressed();
    }
    
    @Override
    public void keyPressed(){
        Task.info("Poker", "Pressed key " + new Key(key, keyCode).toString());
        Disposer.onKeyPressed(new Key(key, keyCode));
    }
}
