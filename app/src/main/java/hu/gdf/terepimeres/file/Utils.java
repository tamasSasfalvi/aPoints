package hu.gdf.terepimeres.file;

import hu.gdf.terepimeres.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class Utils {
  
  /**
   * Átmásolja a forrásútvonalon található fájl tartalmát a célfájlba. Amennyiben a célfájl nem létezik, létrehozza.
   * Amennyiben létezik, felülírja.
   * 
   * @throws IOException ha a másolás nem sikerült
   */
  public static void copyFile(String source,
                              File destFile) {
    final File sourceFile = new File(source);
    copyFile(sourceFile, destFile);
  }
  
  /**
   * Lemásolja a forráskönyvtárat a tartalmával együt
   * 
   * @param srcDir forráskönyvtár
   * @param destDir célkönyvtár
   * @param exclude kivételek
   */
  public static void copyDir(File srcDir,
                             File destDir,
                             FilenameFilter exclude) {
    if ( srcDir == null ) {
      throw new NullPointerException("srcDir is null");
    }
    if ( srcDir.exists() && srcDir.isDirectory() == false ) {
      throw new IllegalArgumentException(srcDir + "' is not a directory");
    }
    if ( destDir == null ) {
      throw new NullPointerException("destDir is null");
    }
    if ( destDir.exists() && destDir.isDirectory() == false ) {
      throw new IllegalArgumentException(destDir + "' is not a directory");
    }
    final File[] srcFiles = srcDir.listFiles();
    try {
      if ( srcFiles == null ) {
        throw new IOException("Failed to list " + srcDir);
      }
      
      if ( !destDir.mkdirs() && !destDir.isDirectory() ) {
        throw new IOException(destDir + "' could not be created");
      }
      if ( destDir.canWrite() == false ) {
        throw new IOException("No permission for: " + destDir);
      }
      for (final File srcFile : srcFiles) {
        final File destFile = new File(destDir, srcFile.getName());
        if ( exclude == null || exclude.accept(srcDir, srcFile.getName()) ) {
          if ( srcFile.isDirectory() ) {
            copyDir(srcFile, destFile, exclude);
          } else {
            copyFile(srcFile, destFile);
          }
        }
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  public static void copyStream(InputStream input,
                                OutputStream output) {
    byte[] buffer = new byte[1024 * 1024];
    int byteCnt = 0;
    try {
      while (-1 != (byteCnt = input.read(buffer))) {
        output.write(buffer, 0, byteCnt);
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  public static void copyStream(InputStream input,
                                OutputStream out,
                                Charset charset) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, charset));
    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        writer.append(line).append(Constants.LINE_SEPARATOR);
      }
      writer.flush();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  public static void copyFile(File sourceFile,
                              File destFile) {
    final File parentFile = destFile.getParentFile();
    try {
      if ( parentFile != null ) {
        if ( !parentFile.mkdirs() && !parentFile.isDirectory() ) {
          throw new IOException("Error creating " + parentFile);
        }
      }
      if ( destFile.exists() && destFile.canWrite() == false ) {
        throw new IOException(destFile + " is read-only");
      }
      if ( destFile.exists() && destFile.isDirectory() ) {
        throw new IOException(destFile + " is a directory");
      }
      
      FileInputStream fis = new FileInputStream(sourceFile);
      try {
        FileOutputStream output = new FileOutputStream(destFile);
        try {
          copyStream(fis, output);
        } finally {
          if ( output != null ) {
            output.close();
          }
        }
      } finally {
        fis.close();
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  /**
   * Rekurzívan töröl egy könyvtárat
   * 
   * @param directory a könyvtár
   * @param deleteDir maga a könyvtár is törlődjön e
   * @throws IOException
   */
  public static void deleteDir(final File directory,
                               boolean deleteDir) {
    if ( !directory.exists() ) {
      return;
    }
    if ( !isSymlink(directory) ) { // ha symlink akkor elegendő azt törölni
    
      if ( !directory.isDirectory() ) {
        final String message = directory + " is not a directory";
        throw new IllegalArgumentException(message);
      }
      try {
        final File[] files = directory.listFiles();
        if ( files == null ) {
          throw new IOException("Failed to list " + directory);
        }
        
        for (final File file : files) {
          if ( file.isDirectory() ) {
            deleteDir(file, true);
          } else {
            if ( !file.delete() ) {
              if ( !file.exists() ) {
                throw new FileNotFoundException("File does not exist: " + file);
              }
              final String message = "Unable to delete file: " + file;
              throw new IOException(message);
            }
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    
    if ( deleteDir && !directory.delete() ) {
      final String message = "Unable to delete directory " + directory + ".";
      throw new RuntimeException(message);
    }
  }
  
  public static String replaceExtension(String filename,
                                        String newExtension) {
    return removeExtension(filename).concat(newExtension);
  }
  
  public static String removeExtension(String filename) {
    return filename.substring(0, filename.lastIndexOf('.'));
  }
  
  /**
   * @param file a fájl
   * @return true ha a kanonikus és az abszolut elérési út nem egyezik
   * @throws IOException
   */
  public static boolean isSymlink(final File file) {
    File canonicalFile = null;
    try {
      if ( file.getParent() == null ) {
        canonicalFile = file;
      } else {
        canonicalFile = new File(file.getParentFile().getCanonicalFile(), file.getName());
      }
      
      if ( canonicalFile.getCanonicalFile().equals(canonicalFile.getAbsoluteFile()) ) {
        return false;
      } else {
        return true;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
