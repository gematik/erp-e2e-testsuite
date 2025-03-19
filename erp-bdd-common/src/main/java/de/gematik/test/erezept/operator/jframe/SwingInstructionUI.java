/*
 * Copyright 2025 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.operator.jframe;

import de.gematik.test.erezept.operator.InteractionListener;
import de.gematik.test.erezept.operator.InteractionView;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

@SuppressWarnings({"java:S110"})
public class SwingInstructionUI extends JDialog implements InteractionView<Void> {
  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JTextPane textPane1;

  private InteractionListener<Void> listener;

  public SwingInstructionUI(String instruction) {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);

    buttonOK.addActionListener(e -> onOK());

    buttonCancel.addActionListener(e -> onCancel());

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            onCancel();
          }
        });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(
        e -> onCancel(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    this.textPane1.setText(instruction);
  }

  @Override
  public String getInstruction() {
    return this.textPane1.getText();
  }

  @Override
  public void register(InteractionListener<Void> listener) {
    this.listener = listener;
  }

  @Override
  public void start() {
    this.pack();
    this.setVisible(true);
  }

  private void onOK() {
    listener.receiveInteractionEvent(null);
    dispose();
  }

  private void onCancel() {
    listener.receiveCancelEvent();
    dispose();
  }
}
