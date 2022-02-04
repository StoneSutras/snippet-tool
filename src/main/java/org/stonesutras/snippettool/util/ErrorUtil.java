/**
 *
 */
package org.stonesutras.snippettool.util;

import javax.swing.*;
import javax.xml.transform.TransformerException;
import java.awt.*;

/**
 * @author silvestre
 *
 */
public final class ErrorUtil {

  /**
   * The ErrorUtil should not be instantiated.
   */
  private ErrorUtil() {
  }

  /**
   * Shows an error dialog.
   *
   * @param parentComponent
   *            Defines the Component that is to be the parent of this dialog
   *            box. May be null.
   * @param message
   *            The message to be displayed to the user as an explanation for
   *            the error.
   * @param e
   *            The error which occured.
   */
  public static void showError(final Component parentComponent,
                               final String message, final Throwable e) {
    if (!GraphicsEnvironment.isHeadless()) {
      String errorMessage = message + ": " + e.getLocalizedMessage();
      if (e instanceof TransformerException) {
        errorMessage = errorMessage + "\n" + ((TransformerException) e).getLocationAsString();
      }
      JOptionPane.showMessageDialog(parentComponent, errorMessage, "Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Shows a warning dialog.
   *
   * @param parentComponent
   *            Defines the Component that is to be the parent of this dialog
   *            box. May be null.
   * @param message
   *            The warning message to be displayed to the user.
   */
  public static void showWarning(final Component parentComponent,
                                 final String message) {
    if (!GraphicsEnvironment.isHeadless()) {
      JOptionPane.showMessageDialog(parentComponent, message, "Warning",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}
