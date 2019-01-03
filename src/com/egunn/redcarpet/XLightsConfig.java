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

import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class which is capable of reading an 
 * <a href="https://xlights.org/">xLights</a> configuration file and adding new
 * models to represent a star.
 * The format of the xLights configuration files is subject to the 
 * <a href="https://github.com/smeighan/xLights/blob/master/License.txt">
 * xLights license terms</a>
 * @author Tyler Gunn
 */
public class XLightsConfig {
    private final static String MODEL_TAG = "model";
    private final static String MODELS_TAG = "models";
    private final static String NAME_ATTR = "name";
    private final static String DESCRIPTION_ATTR = "description";
    private final static String DISPLAY_AS_ATTR = "DisplayAs";
    private final static String DISPLAY_AS_POLY_LINE = "Poly Line";
    private final static String STRING_TYPE_ATTR = "StringType";
    private final static String RGB_NODES_STRING_TYPE = "RGB Nodes";
    private final static String START_SIDE_ATTR = "StartSide";
    private final static String DIR_ATTR = "Dir";
    private final static String ANTI_ALIAS_ATTR = "Antialias";
    private final static String PIXEL_SIZE_ATTR = "PixelSize";
    private final static String TRANSPARENCY_ATTR = "Transparency";
    private final static String LAYOUT_GROUP_ATTR = "LayoutGroup";
    private final static String INDIV_SEGS_ATTR = "IndivSegs";
    private final static String PARAM_ATTR = "param";
    private final static String SEG_ATTR = "Seg";
    private final static String POINT_DATA_ATTR = "PointData";
    private final static String C_POINT_DATA_ATTR = "cPointData";
    private final static String VERSION_NUMBER_ATTR = "versionNumber";
    private final static String WORLD_POS_X_ATTR = "WorldPosX";
    private final static String WORLD_POS_Y_ATTR = "WorldPosY";
    private final static String WORLD_POS_Z_ATTR = "WorldPosZ";
    private final static String SCALE_X_ATTR = "ScaleX";
    private final static String SCALE_Y_ATTR = "ScaleY";
    private final static String SCALE_Z_ATTR = "ScaleZ";
    private final static String NUM_POINTS_ATTR = "NumPoints";
    
    /**
     * Name of the xlights config file to read/write to.
     */
    private final String mFileName;
    
    /**
     * XML document which was read from {@link #mFileName}.
     */
    private Document mDocument;
    
    /**
     * Loads the xLights config file specified and prepares an XML document for
     * modification.
     * @param fileName The filename. 
     */
    public XLightsConfig(String fileName) {
        mFileName = fileName;
        
        loadConfig(mFileName);
    }
    
    /**
     * Loads the xLights configuration.
     * @param file 
     */
    private void loadConfig(String file) {
        try {
            File configXmlFile = new File(file);
            DocumentBuilderFactory dbFactory = 
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            mDocument = dBuilder.parse(configXmlFile);
            mDocument.getDocumentElement().normalize();            
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(XLightsConfig.class.getName()).log(Level.SEVERE, 
                    null, ex);
        }
    }
    
    /**
     * Retrieves a list of all models in the xLights configuration.
     * @return List of model names.
     */
    public List<String> getModelNames() {
        List<String> modelNames = new ArrayList<>();
        
        NodeList nodes = mDocument.getElementsByTagName(MODEL_TAG);
        for (int ix = 0 ; ix < nodes.getLength(); ix++) {
            Node node = nodes.item(ix);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                modelNames.add(element.getAttribute(NAME_ATTR));
            }
        }
        
        return modelNames;
    }
    
    /**
     * Given a {@link PixelStar}, adds a new model to the current xLights config
     * for the star.
     * @param star The star.
     */
    public void addStar(PixelStar star) {
        int layerIx = 1;
        for (StarLayer layer : star.getLayers()) {
            Element layerElement = getStarAsPolyLine(layer, layerIx);
            mDocument.getElementsByTagName(MODELS_TAG).item(0)
                    .appendChild(layerElement);
            layerIx++;
        }
    }
    
    /**
     * Saves the current xLights configuration.
     */
    public void saveXml() {
        try {
            TransformerFactory transformerFactory = 
                    TransformerFactory.newInstance();
            
            Transformer transformer = transformerFactory.newTransformer();
            Source domSource = new DOMSource(mDocument);
            StreamResult streamResult = new StreamResult(
                    new File(mFileName+".out"));
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
     * Returns an xLights Poly Line model definition for a star layer.
     * @param starLayer The star layer.
     * @param layerIx A numerical index for the layer.
     * @return {@link Element} configured for the polyline.
     */
    public Element getStarAsPolyLine(StarLayer starLayer, int layerIx) {
        Element modelElement = mDocument.createElement(MODEL_TAG);
        modelElement.setAttribute(DISPLAY_AS_ATTR, DISPLAY_AS_POLY_LINE);
        modelElement.setAttribute(STRING_TYPE_ATTR, RGB_NODES_STRING_TYPE);
        modelElement.setAttribute(START_SIDE_ATTR, "B");
        modelElement.setAttribute(DIR_ATTR, "L");
        modelElement.setAttribute(ANTI_ALIAS_ATTR, "1");
        modelElement.setAttribute(PIXEL_SIZE_ATTR, "2");
        modelElement.setAttribute(TRANSPARENCY_ATTR, "0");
        modelElement.setAttribute(LAYOUT_GROUP_ATTR, "Default");
        modelElement.setAttribute(NAME_ATTR, "Star_layer_" + layerIx);
        modelElement.setAttribute(DESCRIPTION_ATTR, "RedCarpet star");
        modelElement.setAttribute(INDIV_SEGS_ATTR, "1");
        for (int side = 1 ; side <= 10; side++) {
            modelElement.setAttribute((SEG_ATTR + side), 
                    "" + (starLayer.getNumHolesPerEdge() + 1));
        }
        
        modelElement.setAttribute(WORLD_POS_X_ATTR, doubleToString(0.0));
        modelElement.setAttribute(WORLD_POS_Y_ATTR, doubleToString(0.0));
        modelElement.setAttribute(WORLD_POS_Z_ATTR, doubleToString(0.0));
        modelElement.setAttribute(SCALE_X_ATTR, doubleToString(1.0));
        modelElement.setAttribute(SCALE_Y_ATTR, doubleToString(1.0));
        modelElement.setAttribute(SCALE_Z_ATTR, doubleToString(1.0));
        modelElement.setAttribute("parm1", "1");
        modelElement.setAttribute("parm3", "1");
        modelElement.setAttribute("parm2", 
                "" + ((starLayer.getNumHolesPerEdge() + 1) * 10));
        modelElement.setAttribute(NUM_POINTS_ATTR, "11");
        
        List<Double> points = new ArrayList<>();
        List<Ellipse2D> vertices = starLayer.getVertices();
        for (int ix = 0 ; ix < vertices.size() ; ix++) {
            Ellipse2D start = vertices.get(ix);
            points.add(start.getCenterX());
            points.add(start.getCenterY());
            points.add(0.0);
        }
        Ellipse2D end = vertices.get(0);
        points.add(end.getCenterX());
        points.add(end.getCenterY());
        points.add(0.0);
            
        String pointData = points
                .stream()
                .map(p -> doubleToString(p))
                .collect(Collectors.joining(","));
        modelElement.setAttribute(POINT_DATA_ATTR, pointData);
        modelElement.setAttribute(C_POINT_DATA_ATTR, "");
        modelElement.setAttribute(VERSION_NUMBER_ATTR, "3");
        return modelElement;
    }
    
    /**
     * Format a double as a string.
     * @param num double
     * @return string version of double.
     */
    private String doubleToString(double num) {
        return String.format("%.6f", num);
    }
}

