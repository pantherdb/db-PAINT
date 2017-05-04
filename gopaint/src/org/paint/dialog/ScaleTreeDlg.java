/* 
 * 
 * Copyright (c) 2010, Regents of the University of California 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Neither the name of the Lawrence Berkeley National Lab nor the names of its contributors may be used to endorse 
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package org.paint.dialog;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class ScaleTreeDlg extends JDialog{
  Frame frame;
  JPanel mainPanel;
  JTextField scale;
  Double scaleValue;

  public ScaleTreeDlg(Frame frame, double scale) {
    super(frame, true);
    setTitle("Scale Tree");
    this.frame = frame;
    scaleValue = new Double(scale);
    initializePanel();
  }

  protected void initializePanel() {
    // Create label and field panels
    JPanel labelPane = new JPanel();
    labelPane.setLayout(new GridLayout(0, 1));
    labelPane.add(new JLabel("Scale:"));

    JPanel fieldPane = new JPanel();
    fieldPane.setLayout(new GridLayout(0, 1));
    scale = new JTextField(5);
    scale.setText(scaleValue.toString());
    fieldPane.add(scale);

    mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(labelPane, BorderLayout.CENTER);
    mainPanel.add(fieldPane, BorderLayout.EAST);

    // Create decision panel
    JPanel decisionPanel = new JPanel();
    decisionPanel.setLayout(new BoxLayout(decisionPanel, BoxLayout.X_AXIS));
    JButton saveChoices = new JButton("OK");
    saveChoices.addActionListener(new OKButtonActionListener());
    JButton cancelChoices = new JButton("Cancel");
    cancelChoices.addActionListener(new CancelButtonActionListener());
    decisionPanel.add(saveChoices);
    decisionPanel.add(cancelChoices);

    mainPanel.add(decisionPanel, BorderLayout.SOUTH);
    getRootPane().setDefaultButton(saveChoices);
    setContentPane(mainPanel);
    Rectangle r = frame.getBounds();
    setBounds(r.x + r.width / 2, r.y + r.height / 2, 300, 100);
  }

  public Double display() {
    setVisible(true);
    return scaleValue;
  }

  protected boolean saveUserInfo() {
   try {
     scaleValue = new Double(scale.getText());
     return true;
   }
   catch(NumberFormatException nfe) {
     scaleValue = null;
     return false;
   }

  }

  private class OKButtonActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (false == ScaleTreeDlg.this.saveUserInfo()) {
        JOptionPane.showMessageDialog(frame, "Please specify a valid scale factor");
        return;
      }
      ScaleTreeDlg.this.setVisible(false);
    }
  }

  private class CancelButtonActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      scaleValue = null;
      ScaleTreeDlg.this.setVisible(false);
    }
  }
}