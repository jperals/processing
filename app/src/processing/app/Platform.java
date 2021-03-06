/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2008 Ben Fry and Casey Reas

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package processing.app;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.UIManager;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.FileUtils;


/**
 * Used by Base for platform-specific tweaking, for instance finding the
 * sketchbook location using the Windows registry, or OS X event handling.
 *
 * The methods in this implementation are used by default, and can be
 * overridden by a subclass, if loaded by Base.main().
 *
 * These methods throw vanilla-flavored Exceptions, so that error handling
 * occurs inside Base.
 *
 * There is currently no mechanism for adding new platforms, as the setup is
 * not automated. We could use getProperty("os.arch") perhaps, but that's
 * debatable (could be upper/lowercase, have spaces, etc.. basically we don't
 * know if name is proper Java package syntax.)
 */
public class Platform {
  Base base;


  /**
   * Set the default L & F. While I enjoy the bounty of the sixteen possible
   * exception types that this UIManager method might throw, I feel that in
   * just this one particular case, I'm being spoiled by those engineers
   * at Sun, those Masters of the Abstractionverse. It leaves me feeling sad
   * and overweight. So instead, I'll pretend that I'm not offered eleven dozen
   * ways to report to the user exactly what went wrong, and I'll bundle them
   * all into a single catch-all "Exception". Because in the end, all I really
   * care about is whether things worked or not. And even then, I don't care.
   *
   * @throws Exception Just like I said.
   */
  public void setLookAndFeel() throws Exception {
    String laf = Preferences.get("editor.laf");
    if (laf == null || laf.length() == 0) {  // normal situation
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } else {
      UIManager.setLookAndFeel(laf);
    }
  }


  public void init(Base base) {
    this.base = base;
  }


  /** 
   * This function should throw an exception or return a value. 
   * Do not return null. 
   */  
  public File getSettingsFolder() throws Exception {
    // otherwise make a .processing directory int the user's home dir
    File home = new File(System.getProperty("user.home"));
    return new File(home, ".processing");

    /*
    try {
      Class clazz = Class.forName("processing.app.macosx.ThinkDifferent");
      Method m = clazz.getMethod("getLibraryFolder", new Class[] { });
      String libraryPath = (String) m.invoke(null, new Object[] { });
      //String libraryPath = BaseMacOS.getLibraryFolder();
      File libraryFolder = new File(libraryPath);
      dataFolder = new File(libraryFolder, "Processing");

    } catch (Exception e) {
      showError("Problem getting data folder",
                "Error getting the Processing data folder.", e);
    }
    */
  }


  /**
   * @return if not overridden, a folder named "sketchbook" in user.home.
   * @throws Exception so that subclasses can throw a fit
   */
  public File getDefaultSketchbookFolder() throws Exception {
    return new File(System.getProperty("user.home"), "sketchbook");
  }


  public void openURL(String url) throws Exception {
    Desktop.getDesktop().browse(new URI(url));
    /*
    String launcher = Preferences.get("launcher");
    if (launcher != null) {
      Runtime.getRuntime().exec(new String[] { launcher, url });
    } else {
      showLauncherWarning();
    }
    */
  }


  public boolean openFolderAvailable() {
    return Desktop.isDesktopSupported();
    /*
    return Preferences.get("launcher") != null;
    */
  }


  public void openFolder(File file) throws Exception {
    Desktop.getDesktop().open(file);
    /*
    String launcher = Preferences.get("launcher");
    if (launcher != null) {
      String folder = file.getAbsolutePath();
      Runtime.getRuntime().exec(new String[] { launcher, folder });
    } else {
      showLauncherWarning();
    }
    */
  }
  

  /**
   * Attempts to move to the Trash on OS X, or the Recycle Bin on Windows. 
   * Also tries to find a suitable Trash location on Linux. 
   * If not possible, just deletes the file or folder instead.  
   * @param file the folder or file to be removed/deleted
   * @return true if the folder was successfully removed
   * @throws IOException 
   */
  final public boolean deleteFile(File file) throws IOException {
    FileUtils fu = FileUtils.getInstance();
    if (fu.hasTrash()) {
      fu.moveToTrash(new File[] { file });
      return true;
      
    } else if (file.isDirectory()) {
      Base.removeDir(file);
      return true;
      
    } else {
      return file.delete();
    }
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  public interface CLibrary extends Library {
    CLibrary INSTANCE = (CLibrary)Native.loadLibrary("c", CLibrary.class);
    int setenv(String name, String value, int overwrite);
    String getenv(String name);
    int unsetenv(String name);
    int putenv(String string);
  }


  public void setenv(String variable, String value) {
    CLibrary clib = CLibrary.INSTANCE;
    clib.setenv(variable, value, 1);
  }


  public String getenv(String variable) {
    CLibrary clib = CLibrary.INSTANCE;
    return clib.getenv(variable);
  }


  public int unsetenv(String variable) {
    CLibrary clib = CLibrary.INSTANCE;
    return clib.unsetenv(variable);
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  /*
  protected void showLauncherWarning() {
    Base.showWarning("No launcher available",
                     "Unspecified platform, no launcher available.\n" +
                     "To enable opening URLs or folders, add a \n" +
                     "\"launcher=/path/to/app\" line to preferences.txt",
                     null);
  }
  */
}