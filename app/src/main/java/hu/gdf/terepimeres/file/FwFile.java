package hu.gdf.terepimeres.file;

import java.io.File;

public class FwFile {
  
  // encapsulation
  protected final File f;
  
  // szándékosan nem példányosítható csomagon kívül (encapsulation pattern használatára tervezve)
  public FwFile(File f) {
    this.f = f;
  }
  
  /**
   * Returns the path of this file.
   * 
   * @return this file's path.
   */
  public String getPath() {
    return f.getPath();
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((f == null) ? 0 : f.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if ( this == obj )
      return true;
    if ( obj == null )
      return false;
    if ( getClass() != obj.getClass() )
      return false;
    FwFile other = (FwFile) obj;
    if ( f == null ) {
      if ( other.f != null )
        return false;
    } else if ( !f.equals(other.f) )
      return false;
    return true;
  }
  
  /**
   * Returns a new file made from the pathname of the parent of this file. This is the path up to but not including the
   * last name. {@code null} is returned when there is no parent.
   * 
   * @return a new file representing this file's parent or {@code null}.
   */
  public File getParentFile() {
    return f.getParentFile();
  }
  
  /**
   * Indicates if this file represents a <em>file</em> on the underlying file system.
   * 
   * @return {@code true} if this file is a file, {@code false} otherwise.
   */
  public boolean isFile() {
    return f.isFile();
  }
  
  /**
   * Returns a boolean indicating whether this file can be found on the underlying file system.
   * 
   * @return {@code true} if this file exists, {@code false} otherwise.
   */
  public boolean exists() {
    return f.exists();
  }
  
  /**
   * Returns the name of the file or directory represented by this file.
   * 
   * @return this file's name or an empty string if there is no name part in the file's path.
   */
  public String getName() {
    return f.getName();
  }
  
  /**
   * Returns the time when this file was last modified, measured in milliseconds since January 1st, 1970, midnight.
   * Returns 0 if the file does not exist.
   * 
   * @return the time when this file was last modified.
   */
  public long lastModified() {
    return f.lastModified();
  }
}
