/*
 * Copyright (C) 2019 Tyler Gunn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.egunn.redcarpet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * An XML-based repository which stores star definitions.
 * @author Tyler Gunn
 */
public class StarRepository {
    private final static String STARS_ELEMENT = "Stars";
    private final static String STAR_ELEMENT = "Star";
    
    private final static String ID_ATTR = "id";
    private final static String UNITS_ATTR = "units";
    private final static String WIDTH_ATTR = "width";
    private final static String RATIO_ATTR = "ratio";
    private final static String HOLE_DIAMETER_ATTR = "holeDiameter";
    private final static String PIXEL_BODY_DIAMETER_ATTR = "pixelBodyDiameter";
    private final static String HOLE_SPACING_ATTR = "holeSpacing";
    private final static String ROW_SPACING_ATTR = "rowSpacing";
    private final static String EDGE_SPACING_ATTR = "edgeSpacing";
    private final static String LAYERS_ATTR = "layers";
    private final static String DRAW_OUTER_BORDER_ATTR = "drawOuterBorder";
    private final static String DRAW_INNER_BORDERS_ATTR = "drawInnerBorders";
    private final static String LABEL_HOLES_ATTR = "labelHoles";
    private final static String SHOW_PIXEL_BODIES_ATTR = "showPixelBodies";
    private final static String HOLE_STYLE_ATTR = "holeStyle";
    
    /**
     * Name of the stars definition file to read/write to.
     */
    private final String mFileName = "stars.xml";
    
    /**
     * XML document which was read from {@link #mFileName}.
     */
    private Document mDocument;
    
    public StarRepository() {
        loadStarDocument();
    }
    
    /**
     * Loads the default star repository from stars.xml.
     */
    private void loadStarDocument() {
        try {
            File configXmlFile = new File(mFileName);
            DocumentBuilderFactory dbFactory = 
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            mDocument = dBuilder.parse(configXmlFile);
            mDocument.getDocumentElement().normalize(); 
            
        } catch (FileNotFoundException fne) {
            // No existing star file
            mDocument = createNewStarDocument();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(XLightsConfig.class.getName()).log(Level.SEVERE, 
                    null, ex);
        }
    }
    
    /**
     * Saves the current star repository to disk as an XML file.
     */
    public void saveStarDocument() {
        try {
            TransformerFactory transformerFactory = 
                    TransformerFactory.newInstance(); 
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "2");
            Source domSource = new DOMSource(mDocument);
            StreamResult streamResult = new StreamResult(
                    new File(mFileName));
            transformer.transform(domSource, streamResult);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(XLightsConfig.class.getName()).log(Level.SEVERE, 
                    null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(XLightsConfig.class.getName()).log(Level.SEVERE, 
                    null, ex);
        }
    }
    
    /**
     * Where no star repository exists, make a new one.
     * @return new document.
     */
    private Document createNewStarDocument() {
        DocumentBuilderFactory dbFactory = 
                    DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            document = dbFactory.newDocumentBuilder().newDocument();
            
            Element stars = document.createElement(STARS_ELEMENT);
            document.appendChild(stars);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(StarRepository.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        return document;
    }
    
    /**
     * Load an existing star identified by "name".
     * @param name
     * @return
     * @throws XPathExpressionException 
     */
    private Element getStar(final String name) throws XPathExpressionException {
        if (mDocument == null) {
            return null;
        }
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        xPath.setXPathVariableResolver((QName variableName) -> {
            if (variableName.getLocalPart().equals("idVar")) {
                return name;
            } else {
                return "";
            }
        });
        XPathExpression expression = xPath.compile("/Stars/Star[@id=$idVar]");
        Element element = (Element) expression.evaluate(mDocument, 
                XPathConstants.NODE);
        return element;
    }
    
    /**
     * Create a new element for a star.
     * @return 
     */
    private Element createStarElement() {
        if (mDocument == null) {
            return null;
        }
        return mDocument.createElement(STAR_ELEMENT);
    }
    
    /**
     * Update or add a star given the passed parameters.
     * @param starParams 
     */
    public void updateStarElement(StarParameters starParams) {
        Element existingStar = null;
        try {
            existingStar = getStar(starParams.getStarName());
        } catch (XPathExpressionException ex) {
            System.out.println("Ooops");
            Logger.getLogger(StarRepository.class.getName()).log(Level.SEVERE, 
                    null, ex);
        }
        if (existingStar == null) {
            existingStar = createStarElement();
            existingStar.setAttribute(ID_ATTR, starParams.getStarName());
            existingStar.setIdAttribute(ID_ATTR, true);
            mDocument.getElementsByTagName(STARS_ELEMENT).item(0)
                        .appendChild(existingStar);
            System.out.println("existng" + existingStar);
        }
        
        existingStar.setAttribute(UNITS_ATTR, starParams.getUnits());
        existingStar.setAttribute(WIDTH_ATTR, 
                doubleToString(starParams.getStarWidth()));
        existingStar.setAttribute(RATIO_ATTR, 
                doubleToString(starParams.getRatio()));
        existingStar.setAttribute(HOLE_DIAMETER_ATTR, 
                doubleToString(starParams.getHoleDiameter()));
        existingStar.setAttribute(PIXEL_BODY_DIAMETER_ATTR, 
                doubleToString(starParams.getPixelBodyDiameter()));
        existingStar.setAttribute(HOLE_SPACING_ATTR, 
                doubleToString(starParams.getHoleSpacing()));
        existingStar.setAttribute(ROW_SPACING_ATTR, 
                doubleToString(starParams.getRowSpacing()));
        existingStar.setAttribute(EDGE_SPACING_ATTR, 
                doubleToString(starParams.getEdgeSpacing()));
        existingStar.setAttribute(LAYERS_ATTR, ("" + starParams.getLayers()));
        existingStar.setAttribute(DRAW_OUTER_BORDER_ATTR,
                Boolean.toString(starParams.isOuterBorderVisible()));
        existingStar.setAttribute(DRAW_INNER_BORDERS_ATTR,
                Boolean.toString(starParams.areInnerBordersVisible()));
        existingStar.setAttribute(LABEL_HOLES_ATTR,
                Boolean.toString(starParams.isLabellingHoles()));
        existingStar.setAttribute(SHOW_PIXEL_BODIES_ATTR,
                Boolean.toString(starParams.isShowingPixelBodies()));
        existingStar.setAttribute(HOLE_STYLE_ATTR,
                Integer.toString(starParams.getHoleType()));
        
        saveStarDocument();
    }
    
    /** 
     * Returns all of the stars which are in the star repository.  
     * @return 
     */
    public List<StarParameters> getStars() {
        NodeList nodes = mDocument.getElementsByTagName(STAR_ELEMENT);
        List<StarParameters> stars = new ArrayList<>();
        for (int ix = 0 ; ix < nodes.getLength() ; ix++) {
            Node node = nodes.item(ix);
            Element element = (Element) node;
            stars.add(loadStarModel(element));
        }    
        return stars;
    }
    
    /**
     * Load a {@link StarParameters} instance from an XML element.
     * @param starElement The loaded params.
     * @return 
     */
    private StarParameters loadStarModel(Element starElement) {
        String starName = starElement.getAttribute(ID_ATTR);
        String starUnits = starElement.getAttribute(UNITS_ATTR);
        double starWidth = Double.parseDouble(starElement.getAttribute(
                WIDTH_ATTR));
        double ratio = Double.parseDouble(starElement.getAttribute(
                RATIO_ATTR));
        double holeDiameter = Double.parseDouble(starElement.getAttribute(
                HOLE_DIAMETER_ATTR));
        double pixelBodyDiameter;
        try {
            pixelBodyDiameter = Double.parseDouble(starElement.getAttribute(
                PIXEL_BODY_DIAMETER_ATTR));
        } catch (NumberFormatException nfe) {
            pixelBodyDiameter = 0.0;
        }
        
        double holeSpacing = Double.parseDouble(starElement.getAttribute(
                HOLE_SPACING_ATTR));
        double rowSpacing = Double.parseDouble(starElement.getAttribute(
                ROW_SPACING_ATTR));
        String edgeAttrStr = starElement.getAttribute(EDGE_SPACING_ATTR);
        if (edgeAttrStr == null || edgeAttrStr.isEmpty()) {
            edgeAttrStr = starElement.getAttribute(ROW_SPACING_ATTR);
        }
        double edgeSpacing = Double.parseDouble(edgeAttrStr);
        int layers = Integer.parseInt(starElement.getAttribute(LAYERS_ATTR));
        boolean isOuterBorderVisible = Boolean.parseBoolean(
                starElement.getAttribute(DRAW_OUTER_BORDER_ATTR));
        boolean isInnerBorderVisible = Boolean.parseBoolean(
                starElement.getAttribute(DRAW_INNER_BORDERS_ATTR));
        boolean isLabellingHoles = Boolean.parseBoolean(
                starElement.getAttribute(LABEL_HOLES_ATTR));
        boolean isShowingPixelBodies = Boolean.parseBoolean(
                starElement.getAttribute(SHOW_PIXEL_BODIES_ATTR));
        int holeType = Integer.parseInt(
                starElement.getAttribute(HOLE_STYLE_ATTR));
                
        StarParameters star = new StarParameters(starName, 
                starUnits, 
                starWidth,
                ratio, 
                holeDiameter, 
                pixelBodyDiameter,
                holeSpacing, 
                rowSpacing, 
                edgeSpacing,
                layers, 
                isOuterBorderVisible, 
                isInnerBorderVisible, 
                isLabellingHoles,
                isShowingPixelBodies,
                holeType);
        return star;
    }
    
    /**
     * Format a double as a string.
     * @param num double
     * @return string version of double.
     */
    public static String doubleToString(double num) {
        return String.format("%6.6f", num);
    }
}
