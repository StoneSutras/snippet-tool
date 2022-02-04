package org.stonesutras.snippettool.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stonesutras.snippettool.model.SnippetTool;
import org.stonesutras.snippettool.util.PrefUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.prefs.Preferences;

/**
 * Snippet-tool application window. Contains all other components and a
 * reference to Inscript Object Reference to HiWi_GUI is contained in every
 * sub-component to provide the possibility to influence one component from the
 * other.
 *
 * @author Alexei Bratuhin
 */
@SuppressWarnings("serial")
public class _frame_SnippetTool extends JFrame implements WindowListener {

  private static final Logger logger = LoggerFactory.getLogger(_frame_SnippetTool.class);

  public SnippetTool snippettool;
  public Preferences preferences;

  /**
   * Menubar
   **/
  public _menubar_SnippetTool menubar;

  /**
   * Main panel, marking is done here
   **/
  public _panel_Main main;

  /**
   * Text panel, inscript text is shown here
   **/
  public _panel_Text text;

  /**
   * Log panel, all log messages are shown here (except for exceptions and
   * errors that occure)
   **/
  public _panel_Status status;

  /**
   * Explorer panel, tree-like explorer of eXist database for selecting and
   * loading resources
   **/
  public _panel_Explorer explorer;

  /**
   * Options panel, all adjustments for automatic marking, opacities, colors,
   * text direction and displayed information are done here
   **/
  public _panel_Options options;

  /**
   * Info panel, information about currently selected character is shown here
   **/
  public _panel_Info info;

  public _frame_SnippetTool(SnippetTool snippettool) {
    super();
    this.snippettool = snippettool;
    preferences = snippettool.prefs2;
  }

  public static void main(String[] args) {
    StartGUI.main(args);
  }

  public void createAndShowGUI() {

    // initialize subparts
    menubar = new _menubar_SnippetTool(this, snippettool);
    main = new _panel_Main(this, snippettool);
    text = new _panel_Text(this, snippettool);
    status = new _panel_Status(this);
    explorer = new _panel_Explorer(this, snippettool, true);
    options = new _panel_Options(this, snippettool);
    info = new _panel_Info(snippettool);

    // construct GUI as consisting of JSplitPanes
    setJMenuBar(menubar);

    JSplitPane mainoption = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, main, options);
    mainoption.setResizeWeight(1);
    JSplitPane up = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, explorer, mainoption);
    JSplitPane down = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, text, info);
    down.setResizeWeight(1);
    JSplitPane all = new JSplitPane(JSplitPane.VERTICAL_SPLIT, up, down);
    all.setResizeWeight(1);

    mainoption.setBorder(null);
    up.setBorder(null);
    down.setBorder(null);
    down.setBorder(null);
    all.setBorder(null);

    final int dividerSize = preferences.getInt("local.window.divider.width", 5);

    mainoption.setDividerSize(dividerSize);
    up.setDividerSize(dividerSize);
    down.setDividerSize(dividerSize);
    all.setDividerSize(dividerSize);

    // construct jframe
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(all, BorderLayout.CENTER);
    getContentPane().add(status, BorderLayout.PAGE_END);

    setTitle("Snippet-Tool");

    // load user set layout
    loadLayout();

    // load font
    text.text_in.setFont(snippettool.inscript.getFont());
    info.jta_info.setFont(snippettool.inscript.getFont());

    // add own windowlistener
    addWindowListener(this);

    setVisible(true);
    pack();
  }

  public void status(String status) {
    this.status.setStatus(status);
  }

  /**
   * Save Layout of application window. Saved are the sizes of each of JPanel
   * parts of GUI
   */
  public void saveLayout() {
    String point_window = PrefUtil.point2string(this.getLocation());
    String dim_window = PrefUtil.dimension2string(this.getSize());
    String dim_main = PrefUtil.dimension2string(main.getSize());
    String dim_explorer = PrefUtil.dimension2string(explorer.getSize());
    String dim_options = PrefUtil.dimension2string(options.getSize());
    String dim_text = PrefUtil.dimension2string(text.getSize());
    String dim_info = PrefUtil.dimension2string(info.getSize());
    String dim_status = PrefUtil.dimension2string(status.getSize());

    preferences.put("local.window.position", point_window);
    preferences.put("local.window.size", dim_window);
    preferences.put("local.window.main.size", dim_main);
    preferences.put("local.window.explorer.size", dim_explorer);
    preferences.put("local.window.options.size", dim_options);
    preferences.put("local.window.text.size", dim_text);
    preferences.put("local.window.info.size", dim_info);
    preferences.put("local.window.status.size", dim_status);

  }

  /**
   * Load Layout of application window. Size of each JPanel part of GUI is
   * restores from previous session
   */
  public void loadLayout() {
    setLocation(PrefUtil.string2point(preferences.get("local.window.position", "20,20")));
    setPreferredSize(PrefUtil.string2dimesion(preferences.get("local.window.size", "800x800")));
    main.setPreferredSize(PrefUtil.string2dimesion(preferences.get("local.window.main.size", "800x800")));
    explorer.setPreferredSize(PrefUtil.string2dimesion(preferences.get("local.window.explorer.size", "800x800")));
    options.setPreferredSize(PrefUtil.string2dimesion(preferences.get("local.window.options.size", "800x800")));
    text.setPreferredSize(PrefUtil.string2dimesion(preferences.get("local.window.text.size", "800x800")));
    info.setPreferredSize(PrefUtil.string2dimesion(preferences.get("local.window.info.size", "800x800")));
    status.setPreferredSize(PrefUtil.string2dimesion(preferences.get("local.window.status.size", "800x800")));
  }

  public void exit() {
    saveLayout();
    snippettool.exit();
  }

  @Override
  public void windowActivated(WindowEvent e) {
  }

  @Override
  public void windowClosed(WindowEvent e) {
  }

  @Override
  public void windowClosing(WindowEvent e) {
    exit();
  }

  @Override
  public void windowDeactivated(WindowEvent e) {
  }

  @Override
  public void windowDeiconified(WindowEvent e) {
  }

  @Override
  public void windowIconified(WindowEvent e) {
  }

  @Override
  public void windowOpened(WindowEvent e) {
  }

}
