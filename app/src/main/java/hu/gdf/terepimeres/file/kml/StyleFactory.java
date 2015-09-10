package hu.gdf.terepimeres.file.kml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

final class StyleFactory {
  
  static Element createStyleElement(Document doc,
                                    Style style) {
    Element styleTag = doc.createElement(KmlTag.STYLE.val);
    styleTag.setAttribute(KmlAttribute.ID.val, style.name);
    
    Element lineStyle = doc.createElement(KmlTag.LINESTYLE.val);
    styleTag.appendChild(lineStyle);
    
    Element color = doc.createElement(KmlTag.COLOR.val);
    color.setTextContent(style.color);
    lineStyle.appendChild(color);
    
    Element width = doc.createElement(KmlTag.WIDTH.val);
    width.setTextContent(style.width + "");
    lineStyle.appendChild(width);
    
    return styleTag;
  }
  
  static Element createStyleMapElement(Document doc,
                                       StyleMap styleMap) {
    Element styleMapTag = doc.createElement(KmlTag.STYLEMAP.val);
    styleMapTag.setAttribute(KmlAttribute.ID.val, styleMap.name);
    
    // normal
    Element pair = doc.createElement(KmlTag.PAIR.val);
    styleMapTag.appendChild(pair);
    
    Element key = doc.createElement(KmlTag.KEY.val);
    key.setTextContent("normal");
    pair.appendChild(key);
    
    Element styleUrl = doc.createElement(KmlTag.STYLEURL.val);
    styleUrl.setTextContent("#" + styleMap.normal.name);
    pair.appendChild(styleUrl);
    
    // highlight
    pair = doc.createElement(KmlTag.PAIR.val);
    styleMapTag.appendChild(pair);
    
    key = doc.createElement(KmlTag.KEY.val);
    key.setTextContent("highlight");
    pair.appendChild(key);
    
    styleUrl = doc.createElement(KmlTag.STYLEURL.val);
    styleUrl.setTextContent("#" + styleMap.highlite.name);
    pair.appendChild(styleUrl);
    
    return styleMapTag;
  }
}

enum Style {
  REDLINE("redLine", "ff0000ff", 2),
  RED_BOLD_LINE("redBoldLine", "ff0000ff", 3);
  
  public final String name;
  public final String color;
  public final int    width;
  
  private Style(String name, String color, int width) {
    this.name = name;
    this.color = color;
    this.width = width;
  }
}

enum StyleMap {
  TRACKLOG("tracklog", Style.REDLINE, Style.RED_BOLD_LINE);
  
  public final String name;
  public final Style  normal;
  public final Style  highlite;
  
  private StyleMap(String name, Style normal, Style highlite) {
    this.name = name;
    this.normal = normal;
    this.highlite = highlite;
  }
}
