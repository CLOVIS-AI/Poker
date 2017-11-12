/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphismes.actions;

import objects.MObject;
import static processing.core.PConstants.BASELINE;
import static processing.core.PConstants.LEFT;
import processing.core.PGraphics;

/**
 * Un modèle de bouton d'actions
 * @author Ivan CANET
 * @since 1 mai 2017
 */
public abstract class BoutonAction extends MObject {

    public String title, description;
    
    public BoutonAction(float pX, float pY, float sX, float sY, String text, String description) {
        super(pX, pY, sX, sY);
        this.title = text.toUpperCase();
        this.description = description;
    }
    
    @Override
    public void draw(PGraphics g){
        if(!isFocus()){g.fill(96, 90, 89);}
        else{          g.fill(116, 110, 109);}
        g.stroke(63, 59, 58);
        g.rect(getPosX(), getPosY(), getSizeX(), getSizeY(), 5);
        g.line(getPosX(), getPosY() + 20, getPosX() + getSizeX(), getPosY() + 20);
        g.textSize(20);
        g.textAlign(LEFT, BASELINE);
        g.fill(209, 191, 188);
        g.text(title, getPosX()+5, getPosY()-1, getSizeX()-10, 25);
        g.textSize(13);
        g.text(description, getPosX()+3, getPosY()+21, getSizeX()-6, getSizeY()-15);
    }
    
}
