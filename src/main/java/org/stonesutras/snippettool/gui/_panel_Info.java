package org.stonesutras.snippettool.gui;

import org.stonesutras.snippettool.model.Inscript;
import org.stonesutras.snippettool.model.InscriptCharacter;
import org.stonesutras.snippettool.model.SnippetTool;
import org.stonesutras.snippettool.util.SpringUtilities;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

/**
 * Snippet-tool Charachter Information component. Shows attributes of currently
 * selected character in form: character:
 * {CharacterRepresentation}({OriginalCharacterRepresentation}) x,y:
 * {CharacterMarkingLeftUpperCornerX},{CharacterMarkingLeftUpperCornerY}
 * width,height: {CharacterMarkingWidth},{CharacterMarkingHeight} number, row,
 * column:
 * {CharacterContinuousNumber},{CharacterRowNumber},{CharacterColumnNumber}
 *
 * @author Alexei Bratuhin
 */
@SuppressWarnings("serial")
public class _panel_Info extends JPanel implements Observer, ActionListener {

  /****/
  final JTextArea jta_info = new JTextArea();
  private final Inscript inscript;
  private final JButton clearShape = new JButton("Clear current character shape");

  public _panel_Info(SnippetTool snippettool) {
    // super
    super();
    //
    this.inscript = snippettool.inscript;
    inscript.addObserver(this);
    //
    jta_info.setColumns(20);
    jta_info.setRows(5);
    //
    setVisible(true);
    setLayout(new SpringLayout());
    add(new JScrollPane(jta_info));

    clearShape.setEnabled(false);
    clearShape.addActionListener(this);

    add(clearShape);
    SpringUtilities.makeCompactGrid(this, 2, 1, 0, 0, 0, 0);
  }

  /**
   * Show information to currently selected character
   *
   * @param i character's continuous number
   */
  private String getInfo(InscriptCharacter sn) {
    StringBuilder info = new StringBuilder();
    if (sn != null) {
      info.append("character:\t" + sn.characterStandard + " ("
          + sn.characterOriginal + ")" + "\n");
      info.append("x, y:\t" + sn.getShape().base.x + ", " + sn.getShape().base.y
          + "\n");
      info.append("width, height:\t" + sn.getShape().base.width + ", "
          + sn.getShape().base.height + "\n");
      info.append("angle:\t" + sn.getShape().angle + "\n");
      info.append("number:\t" + sn.getNumber() + " (r:" + sn.getRow() + ", c:"
          + sn.getColumn() + ")");
      return info.toString();
    } else
      return "";
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    InscriptCharacter activeCharacter = inscript.getActiveCharacter();
    if (activeCharacter != null) {
      setBorder(new TitledBorder("info: [" + activeCharacter.getId() + "]"));
      clearShape.setEnabled(!activeCharacter.getShape().isEmpty());
    } else {
      setBorder(new TitledBorder("info: [ ]"));
      clearShape.setEnabled(false);
    }
    jta_info.setText(getInfo(activeCharacter));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(clearShape.getActionCommand())) {
      InscriptCharacter activeCharacter = inscript.getActiveCharacter();
      if (activeCharacter != null) {
        inscript.clearShape(activeCharacter);
      }
    }

  }
}
