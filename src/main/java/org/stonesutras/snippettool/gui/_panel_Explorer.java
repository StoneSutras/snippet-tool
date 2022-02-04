package org.stonesutras.snippettool.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stonesutras.snippettool.model.SnippetTool;
import org.stonesutras.snippettool.util.ErrorUtil;
import org.stonesutras.snippettool.util.StringUtil;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XQueryService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.concurrent.ExecutionException;

/**
 * Snippet-tool Explorer Component. Represents database structure in tree view
 * for navigation and selection purposes. Double-click on file forces this file
 * to be loaded into application either as image or as .xml inscript text.
 *
 * @author Alexei Bratuhin
 */
@SuppressWarnings("serial")
public class _panel_Explorer extends JPanel implements TreeSelectionListener {

  private static final Logger logger = LoggerFactory.getLogger(_panel_Explorer.class);
  private final DefaultTreeModel treeModel;
  /**
   * absolute path to selected resource of the database
   **/
  public String selected;
  /**
   * absolute path to selected collection of the database
   **/
  public String selectedCollection;
  /**
   * relative path to selected resource of the database (realtive to
   * selectedCollection)
   **/
  public String selectedResource;
  /**
   * Reference to parent component
   **/
  _frame_SnippetTool root;
  SnippetTool snippettool;
  /**
   * Address of the database containing inscript .xml files and .png or .jpeg
   * images
   **/
  String db_data_uri;
  String db_data_user;
  String db_data_password;
  /**
   * Root node of file tree structure of the database
   **/
  DefaultMutableTreeNode rootnode;
  /**
   *
   **/
  JTree explorer;
  /**
   * Flag indicating, whether files should be loaded on double-click. Inserted
   * because of the extended ImageCutter functionality
   **/
  boolean autoload = false;

  public _panel_Explorer(_frame_SnippetTool r, SnippetTool snippettool, boolean load) {
    super();
    setLayout(new GridLayout(1, 1));
    setBorder(new TitledBorder("explorer"));
    setPreferredSize(new Dimension(200, 400));

    this.root = r;
    this.snippettool = snippettool;
    this.autoload = load;
    db_data_uri = snippettool.prefs2.get("db.data.uri", "");
    db_data_user = snippettool.prefs2.get("db.data.user", "");
    db_data_password = snippettool.prefs2.get("db.data.password", "");

    rootnode = new DefaultMutableTreeNode(db_data_uri);
    treeModel = new DefaultTreeModel(rootnode);
    explorer = new JTree(rootnode);

    explorer.addTreeSelectionListener(this);

    add(new JScrollPane(explorer));

  }

  @Override
  public void valueChanged(TreeSelectionEvent tse) {
    TreePath tp = tse.getPath();
    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tp.getLastPathComponent();
    Object[] path = tp.getPath();
    String selectedDir = rootnode.toString();
    for (int i = 1; i < path.length; i++) { // starting with index=1 to
      // avoid adding pcname to path
      selectedDir += (String) ((DefaultMutableTreeNode) path[i]).getUserObject();
      selectedDir += "/";
    }
    selectedDir = selectedDir.substring(0, selectedDir.length() - 1);
    try {

      Collection col = DatabaseManager.getCollection(selectedDir, db_data_user, db_data_password);

      if (col != null) {
        // get child collections
        if (col.getChildCollectionCount() > 0) {
          String[] children = col.listChildCollections();
          StringUtil.sortArrayofString(children);
          for (String element : children) {
            selectedNode.add(new DefaultMutableTreeNode(element));
          }
        }

        // get child resources
        if (col.getResourceCount() > 0) {
          String[] resources = col.listResources();
          StringUtil.sortArrayofString(resources);
          for (String resource : resources) {
            selectedNode.add(new DefaultMutableTreeNode(resource));
          }
        }

        treeModel.reload();
      }

      // set selected collection and resource
      if (col == null) {
        // set selected fields
        selected = selectedDir;
        selectedCollection = selectedDir.substring(0, selectedDir.lastIndexOf("/") + 1);
        selectedResource = selectedDir.substring(selectedDir.lastIndexOf("/") + 1);

        // perform loading of selected resource
        // if resource is an image
        if (autoload
            && (selectedResource.endsWith(".png") || selectedResource.endsWith(".jpeg")
            || selectedResource.endsWith(".jpg") || selectedResource.endsWith(".tiff") || selectedResource
            .endsWith(".tif"))) {

          final String loadTaskDescription = root.status.addTask("Loading " + selected);
          new SwingWorker<Object, Object>() {

            @Override
            protected Object doInBackground() throws Exception {
              snippettool.setInscriptImageToRemoteRessource(selected);
              return null;
            }

            @Override
            protected void done() {
              super.done();

              root.main.fitImageMin();
              root.status("Image " + selected + " loaded.");
              root.status.removeTask(loadTaskDescription);
            }

          }.execute();

        } else if (autoload && selectedResource.endsWith(".xml")) {

          if (!resourceContainsChineseText(selectedCollection, selectedResource)) {
            JOptionPane.showMessageDialog(this,
                "<html>The selected document does not contain Chinese text marked as such in the body.<br/>"
                    + "Check if an <em><code>xml:lang=\"zh\"</code> attribute is missing</em> in "
                    + selectedResource + "</html>",
                "No Chinese Text in " + selectedResource, JOptionPane.ERROR_MESSAGE);
          } else {

            final String loadDocumentTaskDescription = root.status.addTask("Loading " + selectedResource);
            new SwingWorker<Object, Object>() {

              @Override
              protected Object doInBackground() throws Exception {
                snippettool.loadInscriptTextFromRemoteResource(selectedCollection, selectedResource);
                snippettool.updateInscriptImagePathFromAppearances();
                return null;
              }

              @Override
              protected void done() {
                try {
                  try {
                    get();
                  } catch (ExecutionException ee) {
                    Throwable e = ee.getCause();
                    logger.error("Error loading inscript " + selectedResource, e);
                    ErrorUtil.showError(root, "Error loading inscript " + selectedResource, e);
                    return;
                  }
                  root.status("Inscript " + selectedResource + " loaded.");
                  root.status.removeTask(loadDocumentTaskDescription);

                  final String rubbingPath = snippettool.inscript.getAbsoluteRubbingPath();
                  if (rubbingPath != null && rubbingPath != "" && rubbingPath.contains("/")) {

                    final String loadImageTaskDescription = root.status.addTask("Loading "
                        + snippettool.inscript.getRelativeRubbingPath());
                    new SwingWorker<String, Object>() {

                      @Override
                      protected String doInBackground() throws Exception {

                        snippettool.setInscriptImageToRemoteRessource(rubbingPath);
                        return rubbingPath;
                      }

                      @Override
                      protected void done() {
                        try {
                          root.status("Image " + get() + " loaded.");
                        } catch (Exception e) {
                          logger.error("Error loading image", e);
                          root.status("Loading image failed: " + e.getLocalizedMessage());
                        }
                        root.status.removeTask(loadImageTaskDescription);
                        root.main.fitImageMin();
                      }

                    }.execute();

                    final String loadCoordinatesTaskDescription = root.status
                        .addTask("Loading coordinates for "
                            + snippettool.inscript.getRelativeRubbingPath());
                    new SwingWorker<Object, Object>() {

                      @Override
                      protected Object doInBackground() throws Exception {
                        snippettool.updateInscriptCoordinates();
                        return null;
                      }

                      @Override
                      protected void done() {
                        root.status.removeTask(loadCoordinatesTaskDescription);
                        root.status("Coordinates loaded.");
                      }

                    }.execute();
                  }
                } catch (Exception e1) {
                  logger.error("Error loading inscript " + selectedResource, e1);
                  root.status.setError("Error loading inscript " + selectedResource, e1);
                }
              }

            }.execute();
          }
        }
      }
    } catch (XMLDBException e) {
      logger.error("XMLDBException in valueChanged(): ", e);
      JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private boolean resourceContainsChineseText(String selectedCollection2, String selectedResource2)
      throws XMLDBException {
    String chineseTextQuery = "declare namespace tei=\"http://www.tei-c.org/ns/1.0\";" + "fn:count(doc(\""
        + selectedResource2 + "\")//tei:TEI/tei:text/tei:body/tei:div[@xml:lang = 'zh'])";

    Collection col = DatabaseManager.getCollection(selectedCollection2, db_data_user, db_data_password);
    XQueryService service = (XQueryService) col.getService("XQueryService", "1.0");

    XMLResource result = (XMLResource) service.query(chineseTextQuery).getResource(0);

    return Integer.parseInt((String) result.getContent()) > 0;

  }
}
