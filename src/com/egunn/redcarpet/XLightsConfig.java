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

import java.awt.geom.Point2D;
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
    private static final String MODELS_ATTR = "models";
    private static final String SELECTED_ATTR = "selected";
    private static final String LAYOUT_ATTR = "layout";
    private static final String GRID_SIZE_ATTR = "GridSize";
    
    private final static String MODEL_GROUP_TAG = "modelGroup";
    private final static String MODEL_GROUPS_TAG = "modelGroups";
    private final static String MODEL_TAG = "model";
    private final static String MODELS_TAG = "models";
    private final static String NAME_ATTR = "name";
    private final static String DESCRIPTION_ATTR = "description";
    private final static String DISPLAY_AS_ATTR = "DisplayAs";
    private final static String DISPLAY_AS_POLY_LINE = "Poly Line";
    private final static String DISPLAY_AS_SINGLE_LINE = "Single Line";
    private final static String DISPLAY_AS_STAR = "Star";
    private final static String STRING_TYPE_ATTR = "StringType";
    private final static String RGB_NODES_STRING_TYPE = "RGB Nodes";
    private final static String START_SIDE_ATTR = "StartSide";
    private final static String DIR_ATTR = "Dir";
    private final static String ANTI_ALIAS_ATTR = "Antialias";
    private final static String PIXEL_SIZE_ATTR = "PixelSize";
    private final static String TRANSPARENCY_ATTR = "Transparency";
    private final static String LAYOUT_GROUP_ATTR = "LayoutGroup";
    private final static String INDIV_SEGS_ATTR = "IndivSegs";
    private final static String PARAM_ATTR = "parm";
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
    private final static String START_CHANNEL_ATTR = "StartChannel";
    private final static String X2_ATTR = "X2";
    private final static String Y2_ATTR = "Y2";
    private final static String Z2_ATTR = "Z2";
    /**
     * Name of the xlights config file to read/write to.
     */
    private final String mFileName;
    
    /**
     * XML document which was read from {@link #mFileName}.
     */
    private Document mDocument;
    
    private String mLastSegmentName;
    private List<String> mModelNames = new ArrayList<>();
    
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
            
            configXmlFile.renameTo(new File(file + ".bk"));
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
    public void addStar(PixelStar star, String modelName) {
        int layerIx = 1;
        String previousLayerName = null;
        for (StarLayer layer : star.getLayers()) {
            String layerName = modelName + "L" + layerIx;
            Element layerElement = getStarAsPolyLine(layer, layerName,
                    previousLayerName, layerIx);
            mDocument.getElementsByTagName(MODELS_TAG).item(0)
                    .appendChild(layerElement);
            previousLayerName = layerName;
            layerIx++;
        }
        mDocument.getElementsByTagName(MODEL_GROUPS_TAG).item(0)
                        .appendChild(getModelGroup(modelName, mModelNames));
    }
    
    /**
     * Given a {@link PixelStar}, adds a new model to the current xLights config
     * for the star.
     * @param star The star.
     */
    public void addStarAsSegments(PixelStar star, String modelName) {
        int layerIx = 1;
        List<String> subGroupNames = new ArrayList<>();
        for (StarLayer layer : star.getLayers()) {
            String layerName = modelName + "L" + layerIx;
            List<Element> layerElements = getLayerAsSegments(layer, layerName);
            for (Element element : layerElements) {
                mDocument.getElementsByTagName(MODELS_TAG).item(0)
                        .appendChild(element);
            }
            List<String> layerModelNames = layerElements.stream()
                    .map(e -> e.getAttribute(NAME_ATTR))
                    .collect(Collectors.toList());
            
            String subGroupName = modelName + "L" + layerIx;
            mDocument.getElementsByTagName(MODEL_GROUPS_TAG).item(0)
                        .appendChild(getModelGroup(subGroupName, 
                                layerModelNames));
            subGroupNames.add(subGroupName);
                    
            layerIx++;
        }
        mDocument.getElementsByTagName(MODEL_GROUPS_TAG).item(0)
                        .appendChild(getModelGroup(modelName, subGroupNames));
    }
    
    /**
     * Given a {@link PixelStar}, adds a new model to the current xLights config
     * for the star, as a series of nested xLights star models.
     * The tradeoff for this approach is that it doesn't get the same ratio.
     * @param star The star.
     */
    public void addStarAsStars(PixelStar star, String modelName) {
        int layerIx = 1;
        String previousLayerName = null;
        for (StarLayer layer : star.getLayers()) {
            String layerName = modelName + "L" + layerIx;
            Element layerElement = getLayerAsStar(layer, layerName,
                    previousLayerName);
            mDocument.getElementsByTagName(MODELS_TAG).item(0)
                    .appendChild(layerElement);
            previousLayerName = layerName;
            layerIx++;
        }
        mDocument.getElementsByTagName(MODEL_GROUPS_TAG).item(0)
                        .appendChild(getModelGroup(modelName, mModelNames));
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
    
    private Element getLayerAsStar(StarLayer layer, String modelName, 
            String startChannel) {
        Element modelElement = mDocument.createElement(MODEL_TAG);
        modelElement.setAttribute(DISPLAY_AS_ATTR, DISPLAY_AS_STAR);
        modelElement.setAttribute(STRING_TYPE_ATTR, RGB_NODES_STRING_TYPE);
        modelElement.setAttribute(START_SIDE_ATTR, "B");
        modelElement.setAttribute(DIR_ATTR, "L");
        modelElement.setAttribute(ANTI_ALIAS_ATTR, "1");
        modelElement.setAttribute(PIXEL_SIZE_ATTR, "2");
        modelElement.setAttribute(TRANSPARENCY_ATTR, "0");
        modelElement.setAttribute(LAYOUT_GROUP_ATTR, "Default");
        modelElement.setAttribute(NAME_ATTR, modelName);
        mModelNames.add(modelName);
        if (startChannel != null) {
            modelElement.setAttribute(START_CHANNEL_ATTR, 
                    ">" + startChannel + ":1");
        }
        modelElement.setAttribute(VERSION_NUMBER_ATTR, "3");
        
        modelElement.setAttribute(PARAM_ATTR + "1", "1");
        modelElement.setAttribute(PARAM_ATTR + "2", Integer.toString(
                layer.getPixelCount()));
        modelElement.setAttribute(PARAM_ATTR + "3", "5");
        
        modelElement.setAttribute(WORLD_POS_X_ATTR, "0.0000");
        modelElement.setAttribute(WORLD_POS_Y_ATTR, "0.0000");
        modelElement.setAttribute(WORLD_POS_Z_ATTR, "0.0000");
        modelElement.setAttribute(SCALE_X_ATTR, 
                doubleToString(layer.getWidth()));
        modelElement.setAttribute(SCALE_Y_ATTR, 
                doubleToString(layer.getWidth()));
        modelElement.setAttribute(SCALE_Z_ATTR, 
                doubleToString(layer.getWidth()));
        return modelElement;
    }
    
    /**
     * Generates a series of Single Lines to make up the star.
     *  
     * @param layer
     * @param layerName
     * @return 
     */
    private List<Element> getLayerAsSegments(StarLayer layer, 
            String layerName) {
        List<Element> segments = new ArrayList<>();
        
        int numPixelsPerSide;
        if (layer.getPixelCount() == 10) {
            numPixelsPerSide = 1;
        } else {
            numPixelsPerSide = layer.getPixelCount() / 10;
        }
        System.out.println("Got numPixelsPerSide " + numPixelsPerSide);
        
        List<Ellipse2D> vertices = layer.getHolesList();
        
        int sideNum = 0;
        for (int startIx = 0; startIx < vertices.size(); 
                startIx += numPixelsPerSide) {
            
            Ellipse2D start = vertices.get(startIx);
            Ellipse2D end = vertices.get(startIx + numPixelsPerSide - 1);
            
            String segmentName = layerName + "S" + sideNum;
            segments.add(getStarSegment(start, end, numPixelsPerSide,
                    segmentName, mLastSegmentName));
            mModelNames.add(segmentName);
            mLastSegmentName = segmentName;
            sideNum++;
        }
        return segments;
    }
    
    /**
     * Builds a Single Line element between two points.
     * 
     * This takes into account the fact that a single line element with 3 nodes
     * is represented in xLights as two points (start and end) and that the
     * 3 nodes are spaced out evenly along that line.
     * Start                  End
     *   *   1      2      3   *
     * 
     * This, however means that when we want to make a side of a star between
     * two points (P1 and P2, for example), we need to extend the start and end
     * of the Single Line element out beyond where the actual points are.
     * Start                  End
     *   *   1      2      3   *
     *      P1             P2
     * This ensures the desired pixels are indeed where they should be.
     * 
     * @param start
     * @param end
     * @param numPixels number of pixels between start and end.
     * @return 
     */
    public Element getStarSegment(Ellipse2D start, Ellipse2D end, 
            int numPixels, String modelName, String startChannel) {
        Element modelElement = mDocument.createElement(MODEL_TAG);
        modelElement.setAttribute(DISPLAY_AS_ATTR, DISPLAY_AS_SINGLE_LINE);
        modelElement.setAttribute(STRING_TYPE_ATTR, RGB_NODES_STRING_TYPE);
        modelElement.setAttribute(START_SIDE_ATTR, "B");
        modelElement.setAttribute(DIR_ATTR, "L");
        modelElement.setAttribute(ANTI_ALIAS_ATTR, "1");
        modelElement.setAttribute(PIXEL_SIZE_ATTR, "2");
        modelElement.setAttribute(TRANSPARENCY_ATTR, "0");
        modelElement.setAttribute(LAYOUT_GROUP_ATTR, "Default");
        modelElement.setAttribute(NAME_ATTR, modelName);
        mModelNames.add(modelName);
        if (startChannel != null) {
            modelElement.setAttribute(START_CHANNEL_ATTR, 
                    ">" + startChannel + ":1");
        }
        modelElement.setAttribute(VERSION_NUMBER_ATTR, "3");
        
        modelElement.setAttribute(PARAM_ATTR + "1", "1");
        modelElement.setAttribute(PARAM_ATTR + "2", 
                Integer.toString(numPixels));
        modelElement.setAttribute(PARAM_ATTR + "3", "1");
        
        double distance = Point2D.distance(
                start.getX(),
                start.getY(),
                end.getX(),
                end.getY());
        double distBetweenPix = distance / (double) numPixels;
        double offset1 = 0.5 / numPixels;
        double offset2 = 1.0 + 0.5 / numPixels;
        
        double deltaX = end.getCenterX() - start.getCenterX();
        double deltaY = end.getCenterY() - start.getCenterY();
        
        double startX = start.getCenterX() + deltaX * -offset1;
        double startY = start.getCenterY() + deltaY * -offset1;  
        
        double endX = start.getCenterX() 
                + (end.getCenterX() - start.getCenterX()) * offset2;
        double endY = start.getCenterY() 
                + (end.getCenterY() - start.getCenterY()) * offset2;  
        
        modelElement.setAttribute(WORLD_POS_X_ATTR, doubleToString(startX));
        modelElement.setAttribute(WORLD_POS_Y_ATTR, doubleToString(-startY));
        modelElement.setAttribute(WORLD_POS_Z_ATTR, doubleToString(0.0));
        modelElement.setAttribute(X2_ATTR, doubleToString(endX-startX));
        modelElement.setAttribute(Y2_ATTR, doubleToString(-(endY-startY)));
        modelElement.setAttribute(Z2_ATTR, doubleToString(0.0));
        return modelElement;
    }
    
    /**
     * Creates a model group element for a model name containing the specified
     * models.
     * @param modelName
     * @param models
     * @return 
     */
    public Element getModelGroup(String modelName, List<String> models) {
        Element modelElement = mDocument.createElement(MODEL_GROUP_TAG);
        modelElement.setAttribute(GRID_SIZE_ATTR, "400");
        modelElement.setAttribute(LAYOUT_ATTR, "minimalGrid");
        modelElement.setAttribute(NAME_ATTR, modelName);
        modelElement.setAttribute(SELECTED_ATTR, "0");
        modelElement.setAttribute(LAYOUT_GROUP_ATTR, "Default");
        modelElement.setAttribute(MODELS_ATTR, models.stream()
                .collect(Collectors.joining(",")));
        return modelElement;
    }
    
    /**
     * Returns an xLights Poly Line model definition for a star layer.
     * @param starLayer The star layer.
     * @param layerIx A numerical index for the layer.
     * @return {@link Element} configured for the polyline.
     */
    public Element getStarAsPolyLine(StarLayer starLayer, String modelName,
            String startChannel, int layerIx) {
        Element modelElement = mDocument.createElement(MODEL_TAG);
        modelElement.setAttribute(DISPLAY_AS_ATTR, DISPLAY_AS_POLY_LINE);
        modelElement.setAttribute(STRING_TYPE_ATTR, RGB_NODES_STRING_TYPE);
        modelElement.setAttribute(START_SIDE_ATTR, "B");
        modelElement.setAttribute(DIR_ATTR, "L");
        modelElement.setAttribute(ANTI_ALIAS_ATTR, "1");
        modelElement.setAttribute(PIXEL_SIZE_ATTR, "2");
        modelElement.setAttribute(TRANSPARENCY_ATTR, "0");
        modelElement.setAttribute(LAYOUT_GROUP_ATTR, "Default");
        modelElement.setAttribute(NAME_ATTR, modelName);
        if (startChannel != null) {
            modelElement.setAttribute(START_CHANNEL_ATTR, 
                    ">" + startChannel + ":1");
        }
        modelElement.setAttribute(VERSION_NUMBER_ATTR, "3");
        
        
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
            points.add(-1.0 * start.getCenterY());
            points.add(0.0);
        }
        Ellipse2D end = vertices.get(0);
        points.add(end.getCenterX());
        points.add(-1.0 * end.getCenterY());
        points.add(0.0);
            
        String pointData = points
                .stream()
                .map(p -> doubleToString(p))
                .collect(Collectors.joining(","));
        modelElement.setAttribute(POINT_DATA_ATTR, pointData);
        modelElement.setAttribute(C_POINT_DATA_ATTR, "");
        
        return modelElement;
    }
    
    /**
     * Format a double as a string.
     * @param num double
     * @return string version of double.
     */
    private String doubleToString(double num) {
        return String.format("%6.4f", num);
    }
}
