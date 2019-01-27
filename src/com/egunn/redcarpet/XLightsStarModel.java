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

/**
 * This class is responsible for generating a .xmodel file to represent the
 * star.
 * @author Tyler Gunn
 */
public class XLightsStarModel {
    private static String STAR_MODEL_TAG = "starmodel";
    private static String NAME_ATTR = "name";
    private static String PARM1_ATTR = "parm1";
    private static String PARM2_ATTR = "parm2";
    private static String PARM3_ATTR = "parm3";
    private static String DISPLAY_AS_ATTR = "DisplayAs";
    private static String DISPLAY_AS_STAR = "Star";
    private static String STRING_TYPE_ATTR = "StringType";
    private static String RGB_NODES_TYPE = "RGB Nodes";
    private static String TRANSPARENCY_ATTR = "Transparency";
    private static String PIXEL_SIZE_ATTR = "PixelSize";
    private static String MODEL_BRIGHTNESS_ATTR = "ModelBrightness";
    private static String ANTI_ALIAS_ATTR = "Antialias";
    private static String START_SIDE_ATTR = "StartSide";
    private static String STAR_SIZES_ATTR = "starSizes";
    private static String STAR_RATIO_ATTR = "starRatio";
    private static String DIR_ATTR = "Dir";
    private static String STRAND_NAMES_ATTR = "StrandNames";
    private static String NODE_NAMES_ATTR = "NodeNames";
    private static String SOURCE_VERSION_ATTR = "SourceVersion";
    
    /**
     * XML document which was read from {@link #mFileName}.
     */
    private final Document mDocument;
    
    /**
     * Name of the star model.
     */
    private final String mName;
    
    public XLightsStarModel(PixelStar star, String name) {
        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;        
        try {
          factory = DocumentBuilderFactory.newInstance();
          builder = factory.newDocumentBuilder();  
        } catch (ParserConfigurationException e) {
          e.printStackTrace();
        }
        
        mDocument = builder.newDocument();
        mName = name;
        mDocument.appendChild(getStarModelElement(star));
    }
    
    public void saveFile(String fileName) {
        try {
            TransformerFactory transformerFactory = 
                    TransformerFactory.newInstance();
            
            Transformer transformer = transformerFactory.newTransformer();
            Source domSource = new DOMSource(mDocument);
            StreamResult streamResult = new StreamResult(
                    new File(fileName));
            transformer.transform(domSource, streamResult);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(XLightsConfig.class.getName()).log(Level.SEVERE, 
                    null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(XLightsConfig.class.getName()).log(Level.SEVERE, 
                    null, ex);
        }
    }
    
    private Element getStarModelElement(PixelStar star) {
        Element modelElement = mDocument.createElement(STAR_MODEL_TAG);
        
        modelElement.setAttribute(NAME_ATTR, mName);
        modelElement.setAttribute(PARM1_ATTR, "1"); // 1 string
        modelElement.setAttribute(PARM2_ATTR, 
                Integer.toString(star.getNumberHoles())); // Total holes
        modelElement.setAttribute(PARM3_ATTR, "5"); // always 5 point star
        modelElement.setAttribute(DISPLAY_AS_ATTR, DISPLAY_AS_STAR);
        modelElement.setAttribute(STRING_TYPE_ATTR, RGB_NODES_TYPE);
        modelElement.setAttribute(TRANSPARENCY_ATTR, "0");
        modelElement.setAttribute(PIXEL_SIZE_ATTR, "2");
        modelElement.setAttribute(MODEL_BRIGHTNESS_ATTR, "");
        modelElement.setAttribute(ANTI_ALIAS_ATTR, "1");
        modelElement.setAttribute(START_SIDE_ATTR, "B");
        
        String starSizes = star.getLayers()
                .stream()
                .map(layer -> Integer.toString(layer.getHolesList().size()))
                .collect(Collectors.joining(","));
        modelElement.setAttribute(STAR_SIZES_ATTR, starSizes);
        modelElement.setAttribute(STAR_RATIO_ATTR, 
                doubleToString(star.getRatio()));
        modelElement.setAttribute(DIR_ATTR, "L");
        modelElement.setAttribute(STRAND_NAMES_ATTR, "");
        modelElement.setAttribute(NODE_NAMES_ATTR, "");
        modelElement.setAttribute(SOURCE_VERSION_ATTR, "2019.7");
        
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
