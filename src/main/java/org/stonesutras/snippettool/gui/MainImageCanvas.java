/**
 * @author silvestre
 */
package org.stonesutras.snippettool.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stonesutras.snippettool.model.Inscript;
import org.stonesutras.snippettool.model.InscriptCharacter;
import org.stonesutras.snippettool.model.PyramidalImage;
import org.stonesutras.snippettool.model.SnippetTool;
import org.stonesutras.snippettool.util.ErrorUtil;
import org.stonesutras.snippettool.util.PrefUtil;

import java.awt.*;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.prefs.Preferences;

/**
 * @author silvestre
 *
 */
@SuppressWarnings("serial")
public class MainImageCanvas extends JComponentE implements Observer {

  /** The logger. */
  private static final Logger logger = LoggerFactory.getLogger(MainImageCanvas.class);
  /** The inscript to draw. */
  private final Inscript inscript;
  /** The preferences to use when drawing. */
  private final Preferences preferences;

  /** The snippetTool dictating the scale factor to use. */
  private final SnippetTool snippetTool;

  /** The image to display. */
  private PyramidalImage image;

  /**
   * Creates an MainImageCanvas displaying the image and text of the supplied inscript using the given preferences.
   *
   * @param snippetTool
   *            the snippet tool containg the scale factor and the inscript to display
   * @param preferences2
   *            the preferences to apply when drawing the image
   */
  public MainImageCanvas(final SnippetTool snippetTool, final Preferences preferences2) {
    super();
    this.snippetTool = snippetTool;
    this.inscript = snippetTool.inscript;
    this.preferences = preferences2;
    snippetTool.addObserver(this);
    inscript.addObserver(this);
    updateSize();
  }

  /**
   * Recalculates the preferred size of the component based on the scale
   * factor and the base image size.
   */
  private void updateSize() {
    image = inscript.getPyramidalImage();
    if (image != null) {
      try {
        setPreferredSize(image.getDimension(snippetTool.getScale()));
        revalidate();
        repaint();
      } catch (IOException e) {
        logger.error("IOException occurred in update", e);
        ErrorUtil.showError(this, "Error concerning image:", e);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  @Override
  public void update(final Observable o, final Object arg) {
    updateSize();
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);
    Graphics2D gg = (Graphics2D) g;

    // load paint properties
    Color rubbingColor = PrefUtil.String2Color(preferences.get("local.color.rubbing", "00ffff"));
    Float rubbingAlpha = preferences.getFloat("local.alpha.rubbing", 0.7f);

    // draw background
    gg.setBackground(rubbingColor);
    gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rubbingAlpha));

    if (image != null) {
      try {
        image.drawImage(snippetTool.getScale(), g);
      } catch (IOException e) {
        logger.error("IOException occurred in paintComponent", e);
        ErrorUtil.showError(this, "Error painting image:", e);
        image = null; // Stop drawing the image if an error occurred.
      }
    }
    // draw marking
    gg.scale(snippetTool.getScale(), snippetTool.getScale());
    for (InscriptCharacter sign : inscript.getPreferredReadingText()) {
      if (!sign.getShape().isEmpty()
          && gg.hitClip((int) Math.round(sign.getShape().base.x), (int) Math.round(sign.getShape().base.y),
          (int) Math.round(sign.getShape().base.width), (int) Math.round(sign.getShape().base.height))) {
        sign.drawCharacter(gg, preferences);
      }
    }

  }
}
