/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphismes;

import objects.MRect;
import objects.interact.Interact;
import processing.core.PGraphics;
import styles.Color;

/**
 * Un bouton.
 * @author Ivan CANET
 * @since 28 avr. 2017
 */
public class Bouton extends MRect {
    
    private final String text;
    private int alpha;

    public Bouton(float pX, float pY, float sX, float sY, String txt, Interact i) {
        super(pX, pY, sX, sY, new Color(50), i);
        text = txt;
        alpha = 125;
    }
    

    @Override
    public void draw(PGraphics g) {
        if(isFocus() && alpha <= 255){
            alpha+=10;
        }else if(!isFocus() && alpha >= 125){
            alpha-=10;
        }
        g.fill(131, 125, 115, alpha);
        g.rect(getPosX(), getPosY(), getSizeX() + alpha/10f - 10, getSizeY(), 5);
        g.fill(255);
        g.textSize(20);
        g.text(text, getPosX()+10, getPosY()+getSizeY() - 10);
    }
    
    
    
}
