/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphismes;

import jeu.Jeu.Mode;
import jeu.joueurs.Humain;
import jeu.joueurs.bots.BotSansBluff;
import jeu.joueurs.bots.DumbBot;
import jeu.joueurs.bots.Nvbot;
import jeu.joueurs.bots.RandomBot;
import logger.Task;
import objects.MObject;
import objects.MWindow;
import objects.interact.Interact;
import objects.interact.Reason;
import objects.text.MIntArea;
import objects.text.MStringArea;
import objects.text.MWritable;
import poker.Connection;
import poker.Poker;
import processing.core.PApplet;
import static processing.core.PApplet.min;
import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.TOP;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Le menu général
 * @author Ivan CANET
 * @since 27 avr. 2017
 */
public class MenuGeneral extends MWindow {
	
    private Humain humain;
    
    //variables
    PImage photo;
    
    private String boutonSelectionne;
	
    public MenuGeneral(final PApplet surface) {
        super(0, 0, surface.width, surface.height);
        boutonSelectionne = "";
        Task.info("Initialisation du menu général en " + surface.width + "x" + surface.height);
        final float x = 50, 
              y = surface.height/2f, 
              increment = surface.height/8f, 
              width = surface.width/5f + x,
              height = min(increment - 10, 50);
        
        this.addObject(new Bouton(x, y, width, height, "Créer partie", new Interact(){
            @Override
            public void actOn(Reason r) {
                Task.info("Menu général", "Créer partie");
                boutonSelectionne = "Creer";
                //Disposer.put(0, 0, Poker.ecran.fenetreDeJeu);
                final MIntArea p = new BoutonInt(2*x + width, y, 100, height, 6666, "Port :"){
                    @Override public boolean isAlive(){ return boutonSelectionne.equals("Creer"); }
                };
                addObject(p);
                
                final MIntArea sb = new BoutonInt(2*x + width + 120, y, 100, height, 10, "Small Blind :"){
                    @Override public boolean isAlive(){ return boutonSelectionne.equals("Creer"); }
                };
                addObject(sb);
                
                final MIntArea s = new BoutonInt(2*x + width + 240, y, 100, height, 200, "Somme de départ :"){
                    @Override public boolean isAlive(){ return boutonSelectionne.equals("Creer"); }
                };
                addObject(s);
                
                final MStringArea n = new BoutonString(2*x + width, y + increment, 200, height, "", "Nom :"){
                    @Override public boolean isAlive(){ return boutonSelectionne.equals("Creer"); }
                };
                addObject(n);
                
                final BoutonMenu m = new BoutonMenu(2*x + width + 450, y, height*2, height, ""){
                    @Override public boolean isAlive(){ return boutonSelectionne.equals("Creer"); }
                    private Mode mode = Mode.LIMIT;
                    @Override
                    public void onMousePressed(){
                        if(mode == Mode.LIMIT){ mode = Mode.NO_LIMIT; }else{ mode = Mode.LIMIT; }
                    }
                    
                    @Override
                    public void draw(PGraphics g){
                        super.text = mode.toString();
                        super.draw(g);
                    }
                };
                addObject(m);
                
                addObject(new BoutonMenu(2*x + width + 570, y, height, height, "Go !"){
                    MIntArea port = p, smallBlind = sb, somme = s;
                    BoutonMenu mode = m;
                    MWritable nom = n;
                    @Override public boolean isAlive(){ return boutonSelectionne.equals("Creer"); }
                    @Override
                    public void onMousePressed(){
                        humain = new Humain(nom.getContent().getString());
                        Connection.nouveauServeur(port.getContent().getInt(), (m.text.equals("NO_LIMIT") ? Mode.NO_LIMIT : Mode.LIMIT), smallBlind.getContent().getInt(), somme.getContent().getInt(), humain);
                        addObject(new BoutonString(2*x + width + 580 + height, y, height*2.5f, height, "C'est parti !", "Joueurs : 1"){
                            @Override public boolean isAlive(){ return boutonSelectionne.equals("Creer"); }
                            @Override
                            public void draw(PGraphics g){
                                if(super.isWritable()) super.disallowWriting();
                                text = "Joueurs : " + Connection.nJoueurs();
                                super.draw(g);
                            }
                            @Override
                            public void onMousePressed(){
                                Task.begin("Début de la partie");
                                Connection.lancer();
                                Task.end("Partie initialisée");
                            }
                        });
                        
                        final float bx = surface.width * 3/4,
                                    by = 50;
                        
                        final BoutonString n; addObject(n = new BoutonString(bx, by, 150, height, "", "Nom"){@Override public boolean isAlive(){ return boutonSelectionne.equals("Creer"); }});
                        final BoutonMenu m; addObject(m = new BoutonMenu(bx, by + height + 5, 150, height, "DumbBot"){
                            @Override public boolean isAlive(){ return boutonSelectionne.equals("Creer"); }
                            @Override
                            public void onMousePressed(){
                                switch(text){
                                    case "DumbBot": text = "RandomBot"; break;
                                    case "RandomBot": text = "BotSansBluff"; break;
                                    case "BotSansBluff": text = "NvBot"; break;
                                    case "NvBot": text = "DumbBot"; break;
                                }
                            }
                        });
                        addObject(new BoutonMenu(bx, by + 2*height + 10, 150, height, "Ajouter le bot"){
                            private final BoutonString nom = n;
                            private final BoutonMenu type = m;
                            @Override public boolean isAlive(){ return boutonSelectionne.equals("Creer"); }
                            @Override
                            public void onMousePressed(){
                                String NOM = nom.getContent().getString();
                                switch(type.text){
                                    case "DumbBot": Connection.ajouterJoueur(new DumbBot(NOM)); break;
                                    case "RandomBot": Connection.ajouterJoueur(new RandomBot(NOM, Poker.ecran.random(10, 40), Poker.ecran.random(10, 60), 100, Poker.ecran.random(0.1f, 1))); break;
                                    case "BotSansBluff": Connection.ajouterJoueur(new BotSansBluff(NOM)); break;
                                    case "NvBot": Connection.ajouterJoueur(new Nvbot(NOM)); break;
                                }
                            }
                        });
                    }
                });
            }}));
        
        this.addObject(new Bouton(x, y+increment, width, height, "Rejoindre partie", new Interact(){
            @Override
            public void actOn(Reason r) {
                boutonSelectionne = "Rejoindre";
                Task.info("Menu général", "Rejoindre partie");
                
                final MIntArea ip1 = new BoutonInt(2*x + width, y, 50, height, 127, "IP :"){
                    @Override public boolean isAlive(){ return boutonSelectionne.equals("Rejoindre"); }
                };
                addObject(ip1);
                
                final MIntArea ip2 = new BoutonInt(2*x + width + 55, y, 50, height, 0, ""){
                    @Override public boolean isAlive(){ return boutonSelectionne.equals("Rejoindre"); }
                };
                addObject(ip2);
                
                final MIntArea ip3 = new BoutonInt(2*x + width + 110, y, 50, height, 0, ""){
                    @Override public boolean isAlive(){ return boutonSelectionne.equals("Rejoindre"); }
                };
                addObject(ip3);
                
                final MIntArea ip4 = new BoutonInt(2*x + width + 165, y, 50, height, 1, ""){
                    @Override public boolean isAlive(){ return boutonSelectionne.equals("Rejoindre"); }
                };
                addObject(ip4);
                
                final MIntArea p = new BoutonInt(2*x + width + 230, y, 100, height, 6666, "Port :"){
                    @Override public boolean isAlive(){ return boutonSelectionne.equals("Rejoindre"); }
                };
                addObject(p);
                
                final MStringArea n = new BoutonString(2*x + width, y + increment, 200, height, "", "Nom :"){
                    @Override public boolean isAlive(){ return boutonSelectionne.equals("Rejoindre"); }
                };
                addObject(n);
                
                addObject(new BoutonMenu(2*x + width + 230, y + increment, height, height, "Go !"){
                    MWritable port = p, i1 = ip1, i2 = ip2, i3 = ip3, i4 = ip4, nom = n;
                    @Override public boolean isAlive(){ return boutonSelectionne.equals("Rejoindre"); }
                    @Override
                    public void onMousePressed(){
                        Connection.nouveauClient(i1.getContent().getInt() + "." + i2.getContent().getInt() + "." + i3.getContent().getInt() + "." + i4.getContent().getInt(), port.getContent().getInt(), nom.getContent().getString());
                    }
                });
            }}));
        
        addObject(new Bouton(x, y + 2*increment, width, height, "Quitter", new Interact() {
            @Override
            public void actOn(Reason r) {
                Task.info("Menu général", "Quitter ...");
                Poker.ecran.exit();
            }}));
		
	//initialisation
	photo = surface.loadImage(Poker.ecran.DATA_PATH + "/carteenmain.png");
	photo.resize(250, 0);
    }
    
    @Override
    public void draw(PGraphics g){
	//affichage
        g.background(16, 63, 9);
        g.fill(255);
        g.textAlign(CENTER);
        g.textSize(150);
        g.text("Poker !", g.width/2, g.height/3);
        //g.imageMode(CENTER);
	g.image(photo, (int)(g.width/1.6), (int)(g.height/1.2));
        
        g.textAlign(LEFT);
        super.draw(g);
    }
    
    private class BoutonMenu extends MObject {    
            public String text;
            public BoutonMenu(float pX, float pY, float sX, float sY, String t) { super(pX, pY, sX, sY); text = t;}
            @Override
            public void draw(PGraphics g) {
                if(!isFocus()){g.fill(96, 90, 89);}
                else{          g.fill(131, 125, 115);}
                g.stroke(63, 59, 58);
                g.rect(getPosX(), getPosY(), getSizeX(), getSizeY(), 5);
                g.fill(255);
                g.textAlign(CENTER, TOP);
                g.textSize(20);
                g.text(text, getPosX() + getSizeX()/2, getPosY() + 15);
            }
        }
        private class BoutonInt extends MIntArea {
            private final String text;
            public BoutonInt(float pX, float pY, float sX, float sY, int defaultValue, String text) {
                super(pX, pY, sX, sY, true, defaultValue, null, null, false);
                this.text = text;
            }
            @Override
            public void draw(PGraphics g) {
                g.fill(255);
                g.textSize(15);
                g.textAlign(LEFT, TOP);
                g.text(text, getPosX(), getPosY());
                if(!isFocus()){g.fill(96, 90, 89);}
                else{          g.fill(131, 125, 115);}
                g.stroke(63, 59, 58);
                g.rect(getPosX(), getPosY() + 17, getSizeX(), getSizeY() - 17, 5);
                g.fill(255);
                g.textSize(20);
                g.text(getContent().getInt(), getPosX() + 5, getPosY() + 20);
            }
        }
        private class BoutonString extends MStringArea {
            String text;
            public BoutonString(float pX, float pY, float sX, float sY, String defaultValue, String text) {
                super(pX, pY, sX, sY, true, defaultValue, null, null, 16);
                this.text = text;
            }
            @Override
            public void draw(PGraphics g) {
                g.fill(255);
                g.textSize(15);
                g.textAlign(LEFT, TOP);
                g.text(text, getPosX(), getPosY());
                if(!isFocus()){g.fill(96, 90, 89);}
                else{          g.fill(131, 125, 115);}
                g.stroke(63, 59, 58);
                if(getContent().getString().equals(""))
                    g.stroke(255, 0, 0);
                g.rect(getPosX(), getPosY() + 17, getSizeX(), getSizeY() - 17, 5);
                g.fill(255);
                g.textSize(20);
                g.text(getContent().getString(), getPosX() + 5, getPosY() + 20);
            }
        }
}
