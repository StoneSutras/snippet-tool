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
 * 'Complementary' Mouse controller. Used for selective manual marking. If
 * selected, starts marking selected character. Current character to mark must
 * be selected in Snippet-Tool Text component. Left button is used for marking.
 *
 * @author Alexei Bratuhin
 */
public class _controller_ManualSelective implements MouseListener,
    MouseMotionListener {

  private static final Logger logger = LoggerFactory
      .getLogger(_controller_ManualSelective.class);

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

  public _controller_ManualSelective(_panel_Mainimage mi) {
    super();
    this.main_image = mi;
    this.snippettool = mi.root.snippettool;
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
      main_image.requestFocusInWindow();
      mouse_pressed = new Point((int) (me.getX() / snippettool.getScale()),
          (int) (me.getY() / snippettool.getScale()));

      // repaint
      logger.trace("Triggering repaint.");
      main_image.root.main.repaint();
    }
  }

  @Override
  @SuppressWarnings("static-access")
  public void mouseReleased(MouseEvent me) {
    if (me.getButton() == me.BUTTON1) {
      mouse_released = new Point((int) (me.getX() / snippettool.getScale()),
          (int) (me.getY() / snippettool.getScale()));
      // int dx = mouse_released.x - mouse_current_new.x;
      // int dy = mouse_released.y - mouse_current_new.y;

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

      // set active
      main_image.root.text.setSelected(activeChar);

      // repaint
      logger.trace("Triggering repaint.");
      main_image.root.main.repaint();
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
      // int dx = mouse_current_new.x - mouse_current_old.x;
      // int dy = mouse_current_new.y - mouse_current_old.y;

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

      // set active
      main_image.root.text.setSelected(activeChar);

      // repaint
      logger.trace("Triggering repaint.");
      main_image.root.main.repaint();
    }
  }

  @Override
  public void mouseMoved(MouseEvent me) {
  }
}
