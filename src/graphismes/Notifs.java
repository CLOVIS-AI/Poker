/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphismes;

import java.util.ArrayList;
import logger.Task;
import objects.MContainer;
import objects.MObject;
import processing.core.PGraphics;

/**
 * Un container qui contient les {@link Notif}.
 * @author Ivan CANET
 */
public class Notifs extends MContainer {

    public Notifs(float pX, float pY, float sX, float sY) {
        super(pX, pY, sX, sY);
    }
    
    @Override
    public void draw(PGraphics g){
        ArrayList<MObject> objects = getContent();
        /*if(getPosY() - Notif.HAUTEUR * objects.size() < 0)
            objects.remove(0);*/
        float suppose = getPosY();
        for(int i = 0; i < objects.size(); i++){
            MObject obj = objects.get(i);
            obj.setPosX(getPosX());
            suppose -= Notif.HAUTEUR;
            float reelle = suppose;
            if(obj.getPosY() == 0){
                obj.setPosY(suppose - Notif.HAUTEUR);
            }
            if(suppose > obj.getPosY()){
                reelle = obj.getPosY() + poker.Poker.ecran.map(suppose - obj.getPosY(), 1, g.height, 1, 10);
            }
            obj.setPosY(reelle);
            obj.draw(g);
            if(!obj.isAlive()){
                objects.remove(i);
                i--;
                Task.info("Suppression de la notif " + obj.toString());
            }
        }
    }
    
}
