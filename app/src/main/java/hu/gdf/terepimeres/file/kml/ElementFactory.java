package hu.gdf.terepimeres.file.kml;

import hu.gdf.terepimeres.Constants;
import hu.gdf.terepimeres.Context;
import hu.gdf.terepimeres.entity.Gcp;
import hu.gdf.terepimeres.entity.Location;

import java.io.File;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

final class ElementFactory {
  
  static Element createDocument(Document doc,
                                String name) {
    Element document = doc.createElement(KmlTag.DOCUMENT.val);
    
    Element e = doc.createElement(KmlTag.NAME.val);
    e.setTextContent(name);
    document.appendChild(e);
    
    e = doc.createElement(KmlTag.OPEN.val);
    e.setTextContent("1");
    document.appendChild(e);
    
    return document;
  }
  
  static Element createFolder(Document doc,
                              String name,
                              boolean open) {
    Element folder = doc.createElement(KmlTag.FOLDER.val);
    
    Element e = doc.createElement(KmlTag.NAME.val);
    e.setTextContent(name);
    folder.appendChild(e);
    
    e = doc.createElement(KmlTag.OPEN.val);
    e.setTextContent(open ? "1" : "0");
    folder.appendChild(e);
    
    return folder;
  }
  
  static Element createPlacemark(Document doc,
                                 long id) {
    Element placemark = doc.createElement(KmlTag.PLACEMARK.val);
    placemark.setAttribute(KmlAttribute.ID.val, Long.toString(id));
    return placemark;
  }
  
  static Element createPlacemark(Document doc,
                                 long id,
                                 String name,
                                 Location loc) {
    Element placemark = createPlacemark(doc, id);
    
    Element nameElement = doc.createElement(KmlTag.NAME.val);
    nameElement.setTextContent(name);
    placemark.appendChild(nameElement);
    
    Element point = doc.createElement(KmlTag.POINT.val);
    placemark.appendChild(point);
    
    Element coordinates = doc.createElement(KmlTag.COORDINATES.val);
    coordinates.setTextContent(loc.getLon() + Constants.KML_COORDS_DELIMITER + loc.getLat()
        + Constants.KML_COORDS_DELIMITER + loc.getAlt());
    point.appendChild(coordinates);
    
    return placemark;
  }
  
  static String generateDescHtml(Gcp gcp,
                                 boolean convertUrlToLocal) {
    StringBuilder sb = new StringBuilder("<html><head>").append(hu.gdf.terepimeres.file.kml.Constants.HTML_PLACEMARK_DESCR_STYLE)
                                                        .append("<title>")
                                                        .append(gcp.getName())
                                                        .append("</title>")
                                                        .append(hu.gdf.terepimeres.file.kml.Constants.HML_PLACEMARK_DESCR_SCRIPT)
                                                        .append("</head><body");
    if ( gcp.getImgs().size() > 0 ) {
      File firstBigImg = gcp.getImgs().iterator().next();
      sb.append(" onload=\"myFunction('")
        .append(convertUrlToLocal
            ? Context.projectName.concat("/").concat(firstBigImg.getName())
            : firstBigImg.getAbsolutePath())
        .append("')\">");
    } else {
      sb.append(">");
    }
    sb.append("<table><tr><td>accuracy: </td><td id=\"")
      .append(hu.gdf.terepimeres.file.kml.Constants.HTML_PLACEMARK_ACCURACY_TAG_ID)
      .append("\">")
      .append(gcp.getAccuracy())
      .append("</td></tr><tr><td>received: </td><td id=\"")
      .append(hu.gdf.terepimeres.file.kml.Constants.HTML_PLACEMARK_RECEIVED_TAG_ID)
      .append("\">")
      .append(gcp.getTime())
      .append("</td></tr></table>");
    if ( gcp.getImgs().size() > 0 ) {
      sb.append("<div id=\"bigImg\"></div><ul class=\"images\">");
      String imgPath;
      for (File img : gcp.getImgs()) {
        imgPath = convertUrlToLocal
            ? Context.projectName.concat("/").concat(img.getName())
            : img.getAbsolutePath();
        sb.append("<li><img onclick=\"myFunction('")
          .append(imgPath)
          .append("')\" src=\"")
          .append(imgPath)
          .append("\" alt=\"img\"/></li>");
      }
      sb.append("</ul>");
    }
    sb.append("</body></html>");
    return sb.toString();
  }
  
  static Element createPlacemark(Document doc,
                                 Gcp gcp) {
    Element placemark = createPlacemark(doc, gcp.getId(), gcp.getName(), gcp.getLocation());
    
    Element descr = doc.createElement(KmlTag.DESCRIPTION.val);
    
    CDATASection html = doc.createCDATASection(generateDescHtml(gcp, false));
    descr.appendChild(html);
    
    placemark.appendChild(descr);
    return placemark;
  }
}
