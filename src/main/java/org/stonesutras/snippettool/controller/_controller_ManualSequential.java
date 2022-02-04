package org.stonesutras.snippettool.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stonesutras.snippettool.gui._panel_Mainimage;
import org.stonesutras.snippettool.model.InscriptCharacter;
import org.stonesutras.snippettool.model.SnippetShape;
import org.stonesutras.snippettool.model.SnippetTool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * 'Complementary' Mouse controller. Used for continuous manual marking. If
 * selected, starts marking from first character. Current character to mark is
 * highlighted in Snippet-Tool Text component. Left button is used for marking.
 *
 * @author Alexei Bratuhin
 */
public class _controller_ManualSequential implements MouseListener,
    MouseMotionListener {

  private static final Logger logger = LoggerFactory
      .getLogger(_controller_ManualSequential.class);
  /**
   * Index of current marking to be made
   **/
  public int currentIndex = 0;
  /**
   * Refernce to parent component
   **/
  _panel_Mainimage main_image;
  SnippetTool snippettool;
  /**
   * Holds the last mousePressed event coordinates
   **/
  Point mouse_pressed = new Point();
  /**
   * Holds the last mouseReleased event coordinates
   **/
  Point mouse_released = new Point();
  /**
   * Holds the prelast mouse event coordinates
   **/
  Point mouse_current_old = new Point();
  /**
   * Holds the last mouseevent coordinates
   **/
  Point mouse_current_new = new Point();

  public _controller_ManualSequential(_panel_Mainimage mi) {
    super();
    this.main_image = mi;
    this.snippettool = mi.root.snippettool;

    this.currentIndex = 0;
  }

  /**
   * Prepare for continuous marking from the beginning
   */
  public void reset() {
    this.currentIndex = 0;
    snippettool.inscript.setActiveCharacter(snippettool.inscript
        .getCharacterNV(0, 0));
    main_image.root.text.setSelected(snippettool.inscript
        .getActiveCharacter());
  }

  @Override
  public void mouseClicked(MouseEvent me) {
  }

  @Override
  public void mouseEntered(MouseEvent me) {
  }

  @Override
  public void mouseExited(MouseEvent me) {
  }

  @Override
  @SuppressWarnings("static-access")
  public void mousePressed(MouseEvent me) {
    if (me.getButton() == me.BUTTON1) {
      //
      main_image.requestFocusInWindow();
      mouse_pressed = new Point((int) (me.getX() / snippettool.getScale()),
          (int) (me.getY() / snippettool.getScale()));

      // repaint
      logger.trace("Triggering repaint.");
      main_image.root.repaint();
    }
  }

  @Override
  @SuppressWarnings("static-access")
  public void mouseReleased(MouseEvent me) {
    if (me.getButton() == me.BUTTON1) {
      mouse_released = new Point((int) (me.getX() / snippettool.getScale()),
          (int) (me.getY() / snippettool.getScale()));

      // compute new dimension of snippet marking
      Dimension dr = new Dimension(Math.abs(mouse_current_new.x
          - mouse_pressed.x), Math.abs(mouse_current_new.y
          - mouse_pressed.y));
      Point pr = (Point) mouse_pressed.clone();
      if (mouse_current_new.x - mouse_pressed.x < 0)
        pr.x -= dr.width;
      if (mouse_current_new.y - mouse_pressed.y < 0)
        pr.y -= dr.height;

      Rectangle r = new Rectangle(pr, dr);

      // check whether new dimension satisfies preferences selected
      // (minimal width and minimal height)
      int min = snippettool.prefs2.getInt("local.snippet.size.min", 50);
      if (r.height < min || r.width < min) {
        JOptionPane.showMessageDialog(main_image.root,
            "Markup rectangle too small!", "Alert!",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      // get active character
      InscriptCharacter activeChar = snippettool.inscript
          .getActiveCharacter();

      // update dimension of snippet marking
      snippettool.inscript.updateSnippet(new SnippetShape(r),
          activeChar.getRow(), activeChar.getColumn());

      //
      currentIndex++;

      // set next active
      snippettool.inscript.setActiveCharacter(snippettool.inscript
          .getCharacterNV(activeChar.getNumber() + 1 - 1, 0)); // +1 for
      // next, -1
      // for
      // indexing
      // started
      // with 1

      // repaint
      logger.trace("Triggering repaint.");
      main_image.root.repaint();
    }
  }

  @Override
  @SuppressWarnings("static-access")
  public void mouseDragged(MouseEvent me) {
    if (me.getModifiers() == me.BUTTON1_MASK) {
      mouse_current_old = new Point(mouse_current_new.x,
          mouse_current_new.y);
      mouse_current_new = new Point(
          (int) (me.getX() / snippettool.getScale()),
          (int) (me.getY() / snippettool.getScale()));

      // compute new dimension of snippet marking
      Dimension dr = new Dimension(Math.abs(mouse_current_new.x
          - mouse_pressed.x), Math.abs(mouse_current_new.y
          - mouse_pressed.y));
      Point pr = (Point) mouse_pressed.clone();
      if (mouse_current_new.x - mouse_pressed.x < 0)
        pr.x -= dr.width;
      if (mouse_current_new.y - mouse_pressed.y < 0)
        pr.y -= dr.height;

      Rectangle r = new Rectangle(pr, dr);

      // get active character
      InscriptCharacter activeChar = snippettool.inscript
          .getActiveCharacter();

      // update dimension of snippet marking
      snippettool.inscript.updateSnippet(new SnippetShape(r),
          activeChar.getRow(), activeChar.getColumn());

      // repaint
      logger.trace("Triggering repaint.");
      main_image.root.repaint();
    }
  }

  @Override
  public void mouseMoved(MouseEvent me) {
  }
}
