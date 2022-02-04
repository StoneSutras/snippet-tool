package org.stonesutras.snippettool.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stonesutras.snippettool.model.Inscript;
import org.stonesutras.snippettool.model.SnippetTool;
import org.stonesutras.snippettool.util.ErrorUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class _panel_Main extends JPanel implements ActionListener, ChangeListener, Observer {

  private static final Logger logger = LoggerFactory.getLogger(_panel_Main.class);
  /**
   * Sub JPanel containing image, where the marking is done
   **/
  public _panel_Mainimage main_image;
  /**
   * as reference
   **/
  _frame_SnippetTool root;
  SnippetTool snippettool;
  Inscript inscript;
  /**
   * JPanel containing Zoom functionality
   **/
  JPanel main_navigation = new JPanel();

  /**
   * JPanel containing CLEAR ad SUBMIT Buttons
   **/
  JPanel main_buttons = new JPanel();

  /**
   * JPanel containing 2 further buttons for scaling image to one of
   * predefined sizes
   **/
  JPanel main_workspace = new JPanel();

  JButton zoom_in = new JButton("+");
  JButton zoom_out = new JButton("-");
  _slider_JZoomSlider zoomer = new _slider_JZoomSlider(_slider_JZoomSlider.HORIZONTAL, -10, 10, 0);

  /**
   * Fit whole image in Sub JPanel
   **/
  JButton fit_image_max = new JButton("Full");
  /**
   * Fit width image in Sub JPanel
   **/
  JButton fit_image_min = new JButton("Fit");

  /**
   * Clear marking
   **/
  JButton clear = new JButton("Clear");
  /**
   * Submit marking
   **/
  JButton submit = new JButton("Submit");
  /**
   * Submit all markings
   **/
  JButton resubmit = new JButton("Resubmit all");


  /**
   * @param jf    reference to parent JFrame
   * @param sutra refernce to parent's inscript
   */
  public _panel_Main(_frame_SnippetTool jf, SnippetTool snippettool) {
    super();

    this.root = jf;
    this.snippettool = snippettool;
    this.inscript = snippettool.inscript;
    inscript.addObserver(this);
    this.main_image = new _panel_Mainimage(jf, snippettool);

    setLayout(new BorderLayout());

    /* creating Zoom JPanel */
    main_navigation.add(zoom_out);
    main_navigation.add(zoomer);
    main_navigation.add(zoom_in);
    zoom_in.addActionListener(this);
    zoomer.addChangeListener(this);
    zoom_out.addActionListener(this);

    /* creating extra Zoom options JPanel */
    main_workspace.setLayout(new BoxLayout(main_workspace, BoxLayout.Y_AXIS));
    Box box1 = new Box(BoxLayout.Y_AXIS);
    box1.add(fit_image_max);
    box1.add(fit_image_min);
    main_workspace.add(box1);

    fit_image_max.addActionListener(this);
    fit_image_min.addActionListener(this);

    /* creating CLEAR/SUBMIT JPanel */
    main_buttons.add(clear);
    main_buttons.add(resubmit);
    main_buttons.add(submit);

    clear.addActionListener(this);
    submit.addActionListener(this);
    resubmit.addActionListener(this);

    /* creating overall layout */
    add(main_navigation, BorderLayout.NORTH);
    add(main_image, BorderLayout.CENTER);
    add(main_buttons, BorderLayout.SOUTH);
    add(main_workspace, BorderLayout.EAST);
  }

  @Override
  public void actionPerformed(ActionEvent ae) {
    String cmd = ae.getActionCommand();
    if (cmd.equals(zoom_in.getActionCommand())) {
      // compute new scale factor
      snippettool.setScale(snippettool.getScale() * snippettool.scaleFactor);
      // set zoomer jslider
      zoomer.setZoom(snippettool.getScale());
    }
    if (cmd.equals(zoom_out.getActionCommand())) {
      // compute new scale factor
      snippettool.setScale(snippettool.getScale() * (1 / snippettool.scaleFactor));
      // set zoomer jslider
      zoomer.setZoom(snippettool.getScale());
    }
    if (cmd.equals(fit_image_max.getActionCommand()) || cmd.equals(fit_image_min.getActionCommand())) {
      fitImage(cmd);
    }

    if (cmd.equals(clear.getActionCommand())) {

      inscript.clearShapes();

      root.options.rb_auto.doClick();

      logger.trace("Triggering repaint.");
      root.repaint();
    }
    if (cmd.equals(submit.getActionCommand()) || cmd.equals(resubmit.getActionCommand())) {

      if (cmd.equals(resubmit.getActionCommand())) {
        inscript.setAllCharactersToModified();
      }

      final String submissionTaskDescription = root.status.addTask("Submitting snippets");

      new SwingWorker<Object, Object>() {

        @Override
        protected Object doInBackground() throws Exception {
          snippettool.submitInscript();
          return null;
        }

        @Override
        protected void done() {
          super.done();
          root.status("Finished Submit");
          root.status.removeTask(submissionTaskDescription);
        }

      }.execute();
    }
  }

  /**
   * @param cmd
   */
  private void fitImage(String cmd) {
    // compute scale ratio
    try {
      Dimension viewportDimension = main_image.scroll_image.getViewport().getExtentSize();
      Dimension imageDimension = inscript.getPyramidalImage().getDimension();
      double horizontalRatio = viewportDimension.getWidth() / imageDimension.getWidth();
      double verticalRatio = viewportDimension.getHeight() / imageDimension.getHeight();
      double scale = 1.0;

      if (cmd.equals(fit_image_max.getActionCommand())) {
        scale = Math.max(horizontalRatio, verticalRatio);
      } else {
        scale = Math.min(horizontalRatio, verticalRatio);
      }

      // set zoomer jslider
      zoomer.setZoom(scale);
      // scale
      snippettool.setScale(scale);
    } catch (Exception e) {
      logger.error("Error in actionPerformed:", e);
      ErrorUtil.showError(this, "Error", e);
    }
  }

  public void fitImageMin() {
    fitImage(fit_image_min.getActionCommand());
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    // scale
    snippettool.setScale(zoomer.getZoom());
  }

  @Override
  public void update(Observable o, Object arg) {
    if (inscript != null) {
      setBorder(new TitledBorder("main: [ " + inscript.getAbsoluteRubbingPath() + " ]"));
    } else {
      setBorder(new TitledBorder("main: "));
    }
  }
}
