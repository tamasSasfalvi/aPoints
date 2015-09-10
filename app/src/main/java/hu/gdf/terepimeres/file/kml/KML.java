package hu.gdf.terepimeres.file.kml;

import hu.gdf.terepimeres.Constants;
import hu.gdf.terepimeres.Context;
import hu.gdf.terepimeres.entity.Gcp;
import hu.gdf.terepimeres.entity.Location;
import hu.gdf.terepimeres.entity.Tracklog;
import hu.gdf.terepimeres.file.FwFile;
import hu.gdf.terepimeres.file.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Environment;

public class KML extends FwFile {
  
  @SuppressWarnings("unused")
  private static final String                 OGC_KML_SCHEMA_LANGUAGE = "http://www.opengis.net/kml/2.2";
  @SuppressWarnings("unused")
  private static final String                 OGC_KML_SCHEMA          = "http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd";
  // private static final String outputEncoding = "UTF-8";
  private static final Charset                utf8                    = Charset.forName("UTF-8");
  private static final String                 PLACEMARKS_FOLDER_NAME  = "placemarks";
  private static final String                 TRACKLOGS_FOLDER_NAME   = "tracklogs";
  private static final String                 DEBUG_FOLDER_NAME       = "debug";
  private static final DocumentBuilderFactory dbf                     = DocumentBuilderFactory.newInstance();
  private static final DocumentBuilder        db;
  private final Document                      doc;
  private final Element                       kml;
  private final Element                       documentTag;
  private final Element                       placemarksFolder;
  private final Element                       tracklogsFolder;
  private Element                             currentTrackLogFolder;
  private Element                             currentDebugFolder;
  
  static {
    try {
      db = dbf.newDocumentBuilder();
      OutputStreamWriter errorWriter = new OutputStreamWriter(System.err, utf8);
      db.setErrorHandler(new MyErrorHandler(new PrintWriter(errorWriter, true)));
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }
  
  // több instance is létezhessen egy fájlból!!!
  private KML(File file) throws SAXException, IOException {
    super(file);
    if ( file.exists() ) {
      doc = db.parse(f);
      kml = doc.getDocumentElement();
      documentTag = (Element) kml.getElementsByTagName(KmlTag.DOCUMENT.val).item(0);
      if ( documentTag == null ) {
        throw new IllegalStateException("Kml is invalid: no <document> tag");
      }
      
      NodeList folders = documentTag.getElementsByTagName(KmlTag.FOLDER.val);
      placemarksFolder = findElement(folders, PLACEMARKS_FOLDER_NAME);
      tracklogsFolder = findElement(folders, TRACKLOGS_FOLDER_NAME);
    } else {
      doc = db.newDocument();
      kml = doc.createElement(KmlTag.KML.val);
      doc.appendChild(kml);
      documentTag = ElementFactory.createDocument(doc, file.getName()); // nem használható a parent, az exportKmz
                                                                        // tempfájlba ment
      kml.appendChild(documentTag);
      
      // INIT placemarkfolder
      placemarksFolder = ElementFactory.createFolder(doc, PLACEMARKS_FOLDER_NAME, true);
      documentTag.appendChild(placemarksFolder);
      
      tracklogsFolder = ElementFactory.createFolder(doc, TRACKLOGS_FOLDER_NAME, false);
      documentTag.appendChild(tracklogsFolder);
      
      // a kml stíluslapja
      documentTag.appendChild(StyleFactory.createStyleElement(doc, Style.REDLINE));
      documentTag.appendChild(StyleFactory.createStyleElement(doc, Style.RED_BOLD_LINE));
      documentTag.appendChild(StyleFactory.createStyleMapElement(doc, StyleMap.TRACKLOG));
    }
  }
  
  public static KML newInstance(File file) {
    try {
      return new KML(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
    
  }
  
  public static KML newInstance(File parent,
                                String name) {
    return newInstance(new File(parent, name));
  }
  
  /**
   * Visszaadja egy NodeList-ből azt az Element-et, amelyik közvetlen gyermekei kötött szereplő első <name> tag tartalma
   * megegyezik egy String-gel
   * 
   * @param list az elemek
   * @param elementName a <name> tag tartalma
   * @return a <name> taget tartalmaző Element, vagy null
   */
  private Element findElement(NodeList list,
                              String elementName) {
    for (int i = 0; i < list.getLength(); i++) {
      Element folder = (Element) list.item(i);
      String name = folder.getElementsByTagName(KmlTag.NAME.val).item(0).getTextContent();
      if ( elementName.equals(name) ) {
        return folder;
      }
    }
    return null;
  }
  
  public void setDescription(String descr,
                             String team) {
    Element e = doc.createElement(KmlTag.DESCRIPTION.val);
    e.setTextContent(descr.concat(hu.gdf.terepimeres.file.kml.Constants.HTML_NEW_LINE.concat(hu.gdf.terepimeres.file.kml.Constants.HTML_NEW_LINE.concat("Készítette: ".concat(team)))));
    documentTag.appendChild(e);
  }
  
  public void addTrackLog(Tracklog tracklog) {
    currentTrackLogFolder = ElementFactory.createFolder(doc, "", true);
    tracklogsFolder.appendChild(currentTrackLogFolder);
    
    Element placemark = ElementFactory.createPlacemark(doc, tracklog.getId());
    currentTrackLogFolder.appendChild(placemark);
    
    Element styleUrl = doc.createElement(KmlTag.STYLEURL.val);
    styleUrl.setTextContent("#" + StyleMap.TRACKLOG.name);
    placemark.appendChild(styleUrl);
    
    Element linestring = doc.createElement(KmlTag.LINESTRING.val);
    placemark.appendChild(linestring);
    
    Element coordinates = doc.createElement(KmlTag.COORDINATES.val);
    linestring.appendChild(coordinates);
    
    currentDebugFolder = ElementFactory.createFolder(doc, DEBUG_FOLDER_NAME, false);
    currentTrackLogFolder.appendChild(currentDebugFolder);
  }
  
  // pillanatnyilag csak az útvonalat updateli, a nevet nem
  public void editTracklog(Tracklog tracklog) {
    List<Location> locations = tracklog.getLocations();
    if ( locations == null || locations.isEmpty() ) {
      return;
    }
    Element coordinate = (Element) currentTrackLogFolder.getElementsByTagName(KmlTag.COORDINATES.val)
                                                        .item(0);
    StringBuilder coordinates = new StringBuilder();
    for (Location location : locations) {
      coordinates.append(Constants.LINE_SEPARATOR).append(location);
    }
    coordinate.setTextContent(coordinates.toString());
  }
  
  public void setTracklogName(String name) {
    Element nameElement = (Element) currentTrackLogFolder.getElementsByTagName(KmlTag.NAME.val)
                                                         .item(0);
    nameElement.setTextContent(name);
  }
  
  /**
   * Törli a Placemarkot a gyerekeivel együtt
   * 
   * @param id az elem azonosítója
   * @param title
   */
  public void removeGcp(long id) {
    NodeList gcps = placemarksFolder.getElementsByTagName(KmlTag.PLACEMARK.val);
    for (int i = 0; i < gcps.getLength(); i++) {
      Node placemark = gcps.item(i);
      long idVal = Long.parseLong(placemark.getAttributes()
                                           .getNamedItem(KmlAttribute.ID.val)
                                           .getNodeValue());
      if ( idVal == id ) {
        placemarksFolder.removeChild(placemark);
        
        return; // mivel csak egy létezhet ezzel az id-val
      }
    }
    throw new RuntimeException("there is no gcp with id: " + id);
  }
  
  public void exportTracklog(File f,
                             long tracklog) throws TransformerFactoryConfigurationError, TransformerException, IOException {
    KML kml = newInstance(f);
    kml.documentTag.appendChild(kml.doc.adoptNode(kml.doc.adoptNode(getTracklogElement(tracklog).cloneNode(true))));
    kml.save();
  }
  
  public File exportKmz(File tempDir) throws TransformerFactoryConfigurationError, TransformerException, IOException {
    File kmz = new File(getParentFile(), Utils.replaceExtension(getName(), ".kmz"));
    
    if ( kmz.exists() && kmz.lastModified() > lastModified() ) {
      return kmz;
    }
    
    Utils.deleteDir(tempDir, false);
    File tempKmlFile = new File(tempDir, Context.kml.getName());
    Utils.copyFile(Context.kml.getPath(), tempKmlFile);
    
    List<File> files = new ArrayList<File>();
    for (Gcp gcp : getGcpData()) {
      for (File img : gcp.getImgs()) {
        if ( img.exists() ) {
          files.add(img);
        }
      }
    }
    
    KML tempKml = newInstance(tempKmlFile);
    if ( tempKml.convertImgUrlForKmz() ) {
      tempKml.save(tempKmlFile);
      files.add(tempKmlFile);
    } else {
      files.add(new File(getParentFile(), getName()));// this file
    }
    
    ZipOutputStream zos = null;
    try {
      FileInputStream fis = null;
      zos = new ZipOutputStream(new FileOutputStream(kmz));
      String entryName;
      ZipEntry entry;
      for (File file : files) {
        try {
          entryName = getParentFile().getName().concat("/").concat(file.getName());
          try {
            long size = file.length();
            if ( size == 0 ) {
              continue;
            }
            entry = new ZipEntry(entryName);
            fis = new FileInputStream(file);
            zos.putNextEntry(entry);
            
            Utils.copyStream(fis, zos);// sajnos az encoding kovertálás nem működik droid 2.3.3-on
            zos.closeEntry();
          } finally {
            fis.close();
          }
        } catch (IOException e) {
          throw new RuntimeException("Could not add kmz entry: " + file.getName());
        }
      }
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if ( zos != null ) {
          zos.close();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return kmz;
  }
  
  public void removeTracklog(long id) {
    NodeList tracklogs = tracklogsFolder.getElementsByTagName(KmlTag.PLACEMARK.val);
    for (int i = 0; i < tracklogs.getLength(); i++) {
      Node placemark = tracklogs.item(i);
      long idVal = Long.parseLong(placemark.getAttributes()
                                           .getNamedItem(KmlAttribute.ID.val)
                                           .getNodeValue());
      if ( idVal == id ) {
        tracklogsFolder.removeChild(placemark.getParentNode());
        return; // mivel csak egy létezhet ezzel az id-val
      }
    }
    throw new RuntimeException("there is no gcp with id: " + id);
  }
  
  /**
   * hozzáad egy placemark elemet a dokumentumhoz. a placemark tartalmazni fog egy name és egy point taget a point tag
   * tartalmazni fog egy coordinates taget. A placemark nevének egyedinek kell lennie. Amennyiben ez nem igaz nem adjuk
   * hozzá.
   * 
   * @param gcp a gcp, amit hozzá szeretnénk adni
   */
  public void addGcp(Gcp gcp) {
    placemarksFolder.appendChild(ElementFactory.createPlacemark(doc, gcp));
  }
  
  public void addDebugPoint(String name,
                            Location loc) {
    Element placemark = ElementFactory.createPlacemark(doc, System.currentTimeMillis(), name, loc);
    currentDebugFolder.appendChild(placemark);
  }
  
  public void editGcp(Gcp newData) {
    // ez így nem igazán frankó (ne töröljük ki a taget, hanem módosítsuk a létező tag adatait!!)
    removeGcp(newData.getId());
    addGcp(newData);
  }
  
  /**
   * a .getElementsByTagName-el ellentétben nem minden leszármazottat, hanem csak a közvetlen gyerekeket adja vissza
   * 
   * @param parent ez alatt keresünk
   * @param name ilyen nevű Elementeket
   * @return az ELementek listája
   */
  private List<Element> getDirectChildren(Element parent,
                                          String name) {
    List<Element> result = new ArrayList<Element>();
    Node child = parent.getFirstChild();
    NodeList a = parent.getChildNodes();
    while ((child = child.getNextSibling()) != null) {
      if ( child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(name) ) {
        result.add((Element) child);
      }
    }
    return result;
  }
  
  public List<Tracklog> getTracklogData() {
    List<Tracklog> result = new ArrayList<Tracklog>();
    if ( tracklogsFolder != null ) {
      
      List<Element> tracks = getDirectChildren(tracklogsFolder, KmlTag.FOLDER.val);
      for (Element e : tracks) { 
        result.add(getTracklog(e));
      }
    }
    return result;
  }
  
  public Element getTracklogElement(long trackId) {
    NodeList placemarks = tracklogsFolder.getElementsByTagName(KmlTag.PLACEMARK.val);
    for (int i = 0; i < placemarks.getLength(); i++) {
      Element placemark = (Element) placemarks.item(i);
      long id = Long.parseLong(placemark.getAttributes()
                                        .getNamedItem(KmlAttribute.ID.val)
                                        .getNodeValue());
      if ( id == trackId ) {
        return (Element) placemark.getParentNode();
      }
    }
    return null;
  }
  
  public List<Gcp> getGcpData() {
    List<Gcp> result = new ArrayList<Gcp>();
    if ( placemarksFolder != null ) {
      NodeList placemarks = placemarksFolder.getElementsByTagName(KmlTag.PLACEMARK.val);
      for (int i = 0; i < placemarks.getLength(); i++) {
        result.add(getGcp((Element) placemarks.item(i)));
      }
    }
    return result;
  }
  
  /**
   * visszaad egy megadott id-jú gcp-t
   * 
   * @param gcpId a keresett placemark id-ja, amit átkonvertálunk GCP-nek
   * @return a gcp objektum, vagy null (ha nincs ilyen GCP a kml-ben)
   */
  public Gcp getGcpData(long gcpId) {
    NodeList placemarks = placemarksFolder.getElementsByTagName(KmlTag.PLACEMARK.val);
    for (int i = 0; i < placemarks.getLength(); i++) {
      Element placemark = (Element) placemarks.item(i);
      long id = Long.parseLong(placemark.getAttributes()
                                        .getNamedItem(KmlAttribute.ID.val)
                                        .getNodeValue());
      if ( id == gcpId ) {
        return getGcp(placemark);
      }
    }
    return null;
  }
  
  private Tracklog getTracklog(Element folder) {
    Element placemark = (Element) folder.getElementsByTagName(KmlTag.PLACEMARK.val).item(0);
    Tracklog tracklog = new Tracklog(Long.parseLong(placemark.getAttributes()
                                                             .getNamedItem(KmlAttribute.ID.val)
                                                             .getNodeValue()));
    tracklog.setName(folder.getElementsByTagName(KmlTag.NAME.val).item(0).getTextContent());
    return tracklog;
  }
  
  private Gcp getGcp(Element placemark) {
    Gcp gcp = new Gcp(Long.parseLong(placemark.getAttributes()
                                              .getNamedItem(KmlAttribute.ID.val)
                                              .getNodeValue()));
    gcp.setName(getDescNode(placemark, KmlTag.NAME.val).getTextContent());
    gcp.setCoords(getDescNode(placemark, KmlTag.COORDINATES.val).getTextContent());
    
    // mivel az accuracy adatok mindig bekerülnek a html-be, biztosak lehetünk abban, hogy van <description> tag
    String html = getDescNode(placemark, KmlTag.DESCRIPTION.val).getTextContent();
    try {
      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
      factory.setNamespaceAware(false);
      XmlPullParser parser = factory.newPullParser();
      parser.setInput(new StringReader(html));
      
      int eventType = parser.getEventType();
      boolean acc = false;
      boolean time = false;
      StringBuilder sb = null;
      while (eventType != XmlPullParser.END_DOCUMENT) {
        switch (eventType) {
          case XmlPullParser.START_TAG:
            if ( "td".equals(parser.getName()) ) {
              String id = parser.getAttributeValue(null, "id");
              if ( hu.gdf.terepimeres.file.kml.Constants.HTML_PLACEMARK_ACCURACY_TAG_ID.equals(id) ) {
                acc = true;
                sb = new StringBuilder();
              } else if ( hu.gdf.terepimeres.file.kml.Constants.HTML_PLACEMARK_RECEIVED_TAG_ID.equals(id) ) {
                time = true;
                sb = new StringBuilder();
              }
            } else if ( "img".equals(parser.getName()) ) {
              File img = new File(parser.getAttributeValue(null, "src"));
              if ( img.exists() ) {
                gcp.addImg(img);
              }
            }
            break;
          case XmlPullParser.END_TAG:
            if ( "td".equals(parser.getName()) ) {
              if ( acc ) {
                gcp.setAccuracy(Double.parseDouble(sb.toString()));
                acc = false;
                sb = null;
              } else if ( time ) {
                gcp.setTime(sb.toString());
                time = false;
                sb = null;
              }
            }
            break;
          case XmlPullParser.TEXT:
            if ( acc || time ) {
              sb.append(parser.getText());
            }
            break;
          default:
            break;
        }
        eventType = parser.next();
      }
    } catch (XmlPullParserException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return gcp;
  }
  
  /**
   * minden elérési utat úgy módosít a kml-ben, hogy azok lokális hivatkozások legyenek
   * 
   * @return true ha volt változtatás a fájlban
   */
  private boolean convertImgUrlForKmz() {
    boolean modified = false;
    
    if ( placemarksFolder != null ) {
      NodeList placemarks = placemarksFolder.getElementsByTagName(KmlTag.PLACEMARK.val);
      Element placemark;
      for (int i = 0; i < placemarks.getLength(); i++) {
        placemark = (Element) placemarks.item(i);
        getDescNode(placemark, KmlTag.DESCRIPTION.val).setTextContent(ElementFactory.generateDescHtml(getGcp(placemark),
                                                                                                      true));
        modified = true;
      }
    }
    return modified;
  }
  
  /**
   * Rekurzívan megkeresi egy szülőtag gyerektagét
   * 
   * @param parent a szülő tag
   * @param nodeName a gyerektag neve
   * @return az első ilyen nevű gyerektag vagy null ha nincs találat
   */
  private Node getDescNode(Node parent,
                           String nodeName) {
    NodeList children = parent.getChildNodes();
    Node result = null;
    Node current;
    for (int i = 0; i < children.getLength(); i++) {
      current = children.item(i);
      if ( current.hasChildNodes() ) {
        result = getDescNode(current, nodeName);
        if ( result != null ) {
          break;
        }
      }
      if ( nodeName.equals(current.getNodeName()) ) {
        return current;
      }
    }
    return result;
  }
  
  public void save() throws TransformerFactoryConfigurationError, TransformerException, IOException {
    save(new File(getPath()));
  }
  
  public void save(File file) throws TransformerFactoryConfigurationError, TransformerException, IOException {
    String state = Environment.getExternalStorageState();
    
    if ( Environment.MEDIA_MOUNTED.equals(state) ) {
      // We can read and write the media
      OutputStream fout = null;
      try {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        if ( doc.getDoctype() != null ) {
          String systemValue = (new File(doc.getDoctype().getSystemId())).getName();
          transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);
        }
        fout = new FileOutputStream(file);
        OutputStream bout = new BufferedOutputStream(fout);
        OutputStreamWriter out = new OutputStreamWriter(bout, utf8);
        StreamResult result = new StreamResult(out);
        transformer.transform(new DOMSource(doc), result);
      } finally {
        fout.close();
      }
    }
  }
  
  /**
   * letöröl egy trackogot a kml-ből
   * 
   * @param name a tracklogneve
   */
  public void deleteTracklog(String name) {
    NodeList folders = documentTag.getElementsByTagName(KmlTag.FOLDER.val);
    Element folder;
    for (int i = 0; i < folders.getLength(); i++) {
      folder = (Element) folders.item(i);
      if ( name.equals(folder.getElementsByTagName(KmlTag.NAME.val).item(0).getTextContent()) ) {
        documentTag.removeChild(folder);
      }
    }
  }
  
  /**
   * letörli az aktuális tracklogot
   */
  public void deleteTracklog() {
    tracklogsFolder.removeChild(currentTrackLogFolder);
  }
}
