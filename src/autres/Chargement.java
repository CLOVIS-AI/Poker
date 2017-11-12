/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autres;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 *
 * @author Ivan CANET
 */
public class Chargement extends JFrame {
    public JProgressBar[] couches;

    public Chargement(int ncouches, String nom){
        this.setTitle(nom);
        this.setBounds(100, 100, 407, 119+45*ncouches);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setLayout(null);
        
        couches = new JProgressBar[ncouches];
        for(int i = 0; i < ncouches; i++){
            couches[i] = new JProgressBar();
            couches[i].setBounds(10, 45*(ncouches - i), 371, 22);
            getContentPane().add(couches[i]);
        }
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
    
    public void kill(){
        dispose();
    }
}
