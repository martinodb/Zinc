/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SNePSEditor.java
 *
 * Created on Nov 20, 2011, 11:57:59 AM
 */

package edu.buffalo.cse.sneps3.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

/**
 *
 * @author dan
 */
public class SNePSEditor extends javax.swing.JPanel {

    Highlighter h;
    HighlightPainter redPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.red);
    HighlightPainter yellowPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
    ArrayList<Integer> lParens;


    /** Creates new form SNePSEditor */
    public SNePSEditor() {
        initComponents();
        h = editor.getHighlighter();
        lParens = new ArrayList<Integer>();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        editor = new javax.swing.JTextPane();

        editor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                editorKeyPressed(evt);
            }
        });
        jScrollPane2.setViewportView(editor);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void editorKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_editorKeyPressed
        if(evt.getKeyChar() == '('){
            lParens.add(editor.getCaretPosition()-1);
        }
        else if(evt.getKeyChar() == ')'){
            if(lParens.isEmpty()){
                try {
                    h.addHighlight(editor.getCaretPosition() - 1, editor.getCaretPosition(), redPainter);
                } catch (BadLocationException ex) {
                    Logger.getLogger(SNePSEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                //h.addHighlight(editor.getCaretPosition() - 1, editor.getCaretPosition(), yellowPainter);
                //h.addHighlight(lParens. - 1, editor.getCaretPosition(), yellowPainter);
            }
        }
    }//GEN-LAST:event_editorKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane editor;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

}
