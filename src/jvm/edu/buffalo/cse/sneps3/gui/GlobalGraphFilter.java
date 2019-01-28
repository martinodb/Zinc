/*
 * GlobalGraphFilter.java
 *
 * Created on Dec 2, 2011, 6:16:43 PM
 */

package edu.buffalo.cse.sneps3.gui;

import edu.buffalo.cse.sneps3.gui.business.Caseframe;
import edu.buffalo.cse.sneps3.gui.business.Context;
import edu.buffalo.cse.sneps3.gui.business.Slot;
import edu.buffalo.cse.sneps3.gui.business.SemanticType;
import edu.buffalo.cse.sneps3.gui.business.Term;

import java.awt.Component;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;

import edu.buffalo.cse.sneps3.gui.business.IView;

/**
 *
 * @author dan
 */
public class GlobalGraphFilter extends javax.swing.JPanel implements IView {

	private static final long serialVersionUID = -3882603345607674164L;

	static GlobalGraphFilter instance;

    DefaultListModel<Caseframe> listModel;

    /** Creates new form GlobalGraphFilter */
    private GlobalGraphFilter() {
        initComponents();
        listModel = new DefaultListModel<Caseframe>();
        jList1.setSelectionModel(new GGFListSelectionModel());
        populateList();
    }

    public static void showFilterDialog(Component parent){
        if(instance == null){
            instance = new GlobalGraphFilter();
            GUI2.model.registerView(instance);
        }
        GlobalGraphFilterDialog ggfd = new GlobalGraphFilterDialog((Frame)parent, instance);
        ggfd.setVisible(true);
    }

    private void populateList(){
        for(Caseframe c : Caseframe.getCaseframes()){
            listModel.addElement(c);
        }
        jList1.setModel(listModel);
    }

    protected JButton getOKButton(){
        return jButton_ok;
    }

    protected JButton getCancelButton(){
        return jButton_cancel;
    }

    protected void perfomOK(){
        //ArrayList<Caseframe> added = new ArrayList<Caseframe>();
    }

    private void initComponents() {

        jButton_cancel = new javax.swing.JButton();
        jButton_ok = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<Caseframe>();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        jButton_cancel.setText("Cancel");

        jButton_ok.setText("OK");

        jScrollPane1.setViewportView(jList1);

        jTextArea1.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(2);
        jTextArea1.setText("  Select the Caseframes you do    not wish to show in the graph.");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane2.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton_ok)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_cancel))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_cancel)
                    .addComponent(jButton_ok))
                .addContainerGap())
        );
    }
    
    private javax.swing.JButton jButton_cancel;
    private javax.swing.JButton jButton_ok;
    private javax.swing.JList<Caseframe> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;

    public void ctUpdate(ArrayList<Context> c, Boolean clear) { }

    public void ctCurrent(Context c) { }

    public void stUpdate(Collection<SemanticType> v, Boolean clear) { }

    public void cfUpdate(Collection<Caseframe> cf, boolean clear) {  
        for(Caseframe ncf : cf){
            if(!listModel.contains(ncf)) listModel.addElement(ncf);
        }
    }

    public void slotUpdate(Collection<Slot> slot, Boolean clear) { }

	public void termUpdate(Collection<Term> term, Boolean clear) { }

}

class GGFListSelectionModel extends DefaultListSelectionModel {

	private static final long serialVersionUID = 864317399339711536L;

	public GGFListSelectionModel(){
        super();
    }

    @Override
    public void setSelectionInterval(int index0, int index1) {
        if(super.isSelectedIndex(index0)) {
            super.removeSelectionInterval(index0, index1);
        }
        else {
            super.addSelectionInterval(index0, index1);
        }
    }

    @Override
    public void addSelectionInterval(int index0, int index1) {
        if (index0 == index1) {
            if (isSelectedIndex(index0)) {
                removeSelectionInterval(index0, index0);
                return;
            }
            super.addSelectionInterval(index0, index1);
        }
    }
}
