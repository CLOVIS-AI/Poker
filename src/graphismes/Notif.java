/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphismes;

import objects.MObject;
import poker.Connection;
import poker.Poker;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.TOP;
import processing.core.PGraphics;

/**
 * Une notification
 * @author Ivan CANET
 */
public class Notif extends MObject {
    
    private final String TITLE, DESCRIPTION;
    private final Type WEIGHT;
    
    private float time;
    
    public Notif(String title, String description){
        this(title, description, Type.FAIBLE);
    }
    
    public Notif(String title, String description, Type importance){
        super(0, 0, LARGEUR, HAUTEUR);
        TITLE = !title.equals("") ? title : ".";
        DESCRIPTION = !description.equals("") ? description : ".";
        WEIGHT = importance;
        time = 0;
        util.MData m = new util.MData();
        m.put("title", new util.Primitive(TITLE.replace(' ', '_').replace(',', ';')));
        m.put("text", new util.Primitive(DESCRIPTION.replace(' ', '_').replace(',', ';')));
        m.put("type", new util.Primitive(WEIGHT.toString()));
        Connection.envoyerSiServeur("notif", m);
    }

    @Override
    public void draw(PGraphics g) {
        int alpha = (int) ((time > 225) ? Poker.ecran.map(time, 225, 255, 255, 0) : 255);
        switch(WEIGHT){
            case VIDE: time += 1f; return;
            case DEBUG: time += 2f;
            case FAIBLE:  g.stroke( 63,  59,  58, alpha);   g.fill( 96,  90,  89, alpha);   time+=1.5f; break;
            case NORMAL:  g.stroke( 63,  59,  58, alpha);   g.fill(116, 110, 109, alpha);   time+=1; break;
            case ORANGE:  g.stroke(239, 130,  14, alpha);   g.fill(160,  88,  11, alpha);   time+=0.5f; break;
            case ROUGE:   g.stroke(198,  37,   9, alpha);   g.fill(140,  30,  11, alpha);   time+=0.25f; break;
        }
        g.line(getPosX(), getPosY(), getPosX() + getSizeX(), getPosY());
        g.line(getPosX(), getPosY(), getPosX(), getPosY() + getSizeY());
        g.line(getPosX() + getSizeX(), getPosY(), getPosX() + getSizeX(), getPosY() + getSizeY());
        g.rect(getPosX(), getPosY(), getSizeX(), getSizeY());
        g.textAlign(LEFT, TOP);
        g.fill(209, 191, 188, alpha);
        g.textSize(15);
        g.text(TITLE + " › " + DESCRIPTION, getPosX()+3, getPosY()+2, getSizeX()-10, 25);
    }
    
    @Override
    public boolean isAlive(){
        return time < 255;
    }
    
    @Override
    public String toString(){
        return WEIGHT + " > " + TITLE + " : " + DESCRIPTION;
    }
    
    // ************************ STATIC *****************************************
    
    public static final float LARGEUR, HAUTEUR, OFFSET;
    
    static{
        OFFSET = 0;
        
        LARGEUR = Poker.ecran.width/6f + 50;
        HAUTEUR = 17;
    }
    
    public static enum Type{
        /** Un message de très faible importance. */
        DEBUG,
        
        /** Un message d'importance normale. */
        FAIBLE,
        
        /** Un message légèrement plus important. */
        NORMAL,
        
        /** Un message important. (Orange) */
        ORANGE,
        
        /** Un message très important. (Rouge) */
        ROUGE,
        
        /** Pour laisser de la place. */
        VIDE
    }
}
