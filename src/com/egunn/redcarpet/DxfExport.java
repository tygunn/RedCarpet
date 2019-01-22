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
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Export a Pixel Star in DXF format.
 * @author Tyler Gunn
 */
public class DxfExport {
    private final static String NEW_LINE = System.lineSeparator();
    
    private final PixelStar mPixelStar;
    
    public DxfExport(PixelStar star) {
        mPixelStar = star;
    }
    
    /**
     * Get a DXF entity for a hole.
     * @param hole The hole.
     * @return String DXF entity.
     */
    private String getEntityForHole(Ellipse2D hole) {
        StringBuilder sb = new StringBuilder();
        sb.append("0");
        sb.append(NEW_LINE);
        sb.append("CIRCLE");
        sb.append(NEW_LINE);
        sb.append("8");
        sb.append(NEW_LINE);
        sb.append("Holes");
        sb.append(NEW_LINE);
        sb.append("10"); // X
        sb.append(NEW_LINE);
        sb.append(doubleToString(hole.getCenterX()));
        sb.append(NEW_LINE);
        sb.append("20"); // Y
        sb.append(NEW_LINE);
        sb.append(doubleToString(hole.getCenterY()));
        sb.append(NEW_LINE);
        sb.append("40"); // Radius
        sb.append(NEW_LINE);
        sb.append(doubleToString(hole.getWidth() / 2.0));
        sb.append(NEW_LINE);
        sb.append("62"); // Color
        sb.append(NEW_LINE);
        sb.append("0"); // black
        sb.append(NEW_LINE);
        return sb.toString();
    }
    
    /**
     * Get a line as a DXF element.
     * @param start start point
     * @param end end point
     * @return line as DXF
     */
    private String getLine(Point2D start, Point2D end) {
        StringBuilder sb = new StringBuilder();
        sb.append("0");
        sb.append(NEW_LINE);
        sb.append("LINE");
        sb.append(NEW_LINE);
        sb.append("8");
        sb.append(NEW_LINE);
        sb.append("Border");
        sb.append(NEW_LINE);
        sb.append("10"); // X
        sb.append(NEW_LINE);
        sb.append(doubleToString(start.getX()));
        sb.append(NEW_LINE);
        sb.append("20"); // Y
        sb.append(NEW_LINE);
        sb.append(doubleToString(start.getY()));
        sb.append(NEW_LINE);
        
        sb.append("11"); // X
        sb.append(NEW_LINE);
        sb.append(doubleToString(end.getX()));
        sb.append(NEW_LINE);
        sb.append("21"); // Y
        sb.append(NEW_LINE);
        sb.append(doubleToString(end.getY()));
        sb.append(NEW_LINE);
        
        sb.append("62"); // Color
        sb.append(NEW_LINE);
        sb.append("0"); // black
        sb.append(NEW_LINE);
        return sb.toString();
    }
    
    /**
     * Get a the DXF entities representing the border of the star.
     * @param star The star
     * @return DXF entities of the border.
     */
    private String getEntityForBorder(PixelStar star) {
        StringBuilder sb = new StringBuilder();
        
        Star border = star.getBorder();
        List<Point2D> vertices = border.getVertices(); 
        for (int ix = 0 ; ix < vertices.size() ; ix++) {
            Point2D start = vertices.get(ix);
            Point2D end = vertices.get((ix + 1) % vertices.size());
            sb.append(getLine(start, end));
        }
        return sb.toString();
    }
    
    /**
     * Get the entities section of the DXF file.
     * @param star The star
     * @return The DXF entities
     */
    private String getEntitiesSection(PixelStar star) {
        StringBuilder sb = new StringBuilder();
        sb.append("0");
        sb.append(NEW_LINE);
        sb.append("SECTION");
        sb.append(NEW_LINE);
        sb.append("2");
        sb.append(NEW_LINE);
        sb.append("ENTITIES");
        sb.append(NEW_LINE);
        
        // Append the border
        sb.append(getEntityForBorder(star));
        
        // Append all the holes
        for (StarLayer layer : star.getLayers()){
            for (Ellipse2D hole : layer.getHolesList()) {
                sb.append(getEntityForHole(hole));
            }
        }
        
        sb.append("0");
        sb.append(NEW_LINE);
        sb.append("ENDSEC");
        sb.append(NEW_LINE);

        return sb.toString();
    }
    
    /**
     * Generates the DXF header.
     * @return The header.
     */
    private String generateHeader() {
        StringBuffer sb = new StringBuffer();
        sb.append("999" + NEW_LINE);
        sb.append("DXF created by Red Carpet" + NEW_LINE);
        sb.append("0" + NEW_LINE);
        sb.append("SECTION" + NEW_LINE);
        sb.append("2" + NEW_LINE);
        sb.append("HEADER" + NEW_LINE);
        sb.append("9" + NEW_LINE);
        sb.append("$ACADVER" + NEW_LINE);
        sb.append("1" + NEW_LINE);
        sb.append("AC1006" + NEW_LINE);
        sb.append("9" + NEW_LINE);
        sb.append("$INSUNITS" + NEW_LINE);
        sb.append("70" + NEW_LINE);
        sb.append("1" + NEW_LINE); // Inches
        sb.append("9" + NEW_LINE);
        sb.append("$INSBASE" + NEW_LINE);
        sb.append("10" + NEW_LINE);
        sb.append("0.0" + NEW_LINE);
        sb.append("20" + NEW_LINE);
        sb.append("0.0" + NEW_LINE);
        sb.append("30" + NEW_LINE);
        sb.append("0.0" + NEW_LINE);
        sb.append("9" + NEW_LINE);
        sb.append("$EXTMIN" + NEW_LINE);
        sb.append("10" + NEW_LINE);
        sb.append("0.0" + NEW_LINE);
        sb.append("20" + NEW_LINE);
        sb.append("0.0" + NEW_LINE);
        sb.append("9" + NEW_LINE);
        sb.append("$EXTMAX" + NEW_LINE);
        sb.append("10" + NEW_LINE);
        sb.append("1000.0" + NEW_LINE);
        sb.append("20" + NEW_LINE);
        sb.append("1000.0" + NEW_LINE);
        sb.append("0" + NEW_LINE);
        sb.append("ENDSEC").append(NEW_LINE);
        
        
        sb.append("0" + NEW_LINE);
        sb.append("SECTION" + NEW_LINE);
        sb.append("2" + NEW_LINE);
        sb.append("TABLES" + NEW_LINE);
        sb.append("0" + NEW_LINE);
        sb.append("TABLE" + NEW_LINE);
        sb.append("2" + NEW_LINE);
        sb.append("LTYPE" + NEW_LINE);
        sb.append("70" + NEW_LINE);
        sb.append("1" + NEW_LINE);
        sb.append("0" + NEW_LINE);
        sb.append("LTYPE" + NEW_LINE);
        sb.append("2" + NEW_LINE);
        sb.append("CONTINUOUS" + NEW_LINE);
        sb.append("70" + NEW_LINE);
        sb.append("64" + NEW_LINE);
        sb.append("3" + NEW_LINE);
        sb.append("Solid line" + NEW_LINE);
        sb.append("72" + NEW_LINE);
        sb.append("65" + NEW_LINE);
        sb.append("73" + NEW_LINE);
        sb.append("0" + NEW_LINE);
        sb.append("40" + NEW_LINE);
        sb.append("0.000000" + NEW_LINE);
        sb.append("0" + NEW_LINE);
        sb.append("ENDTAB" + NEW_LINE);
        
        
        sb.append("0" + NEW_LINE);
        sb.append("TABLE" + NEW_LINE);
        sb.append("2" + NEW_LINE);
        sb.append("LAYER" + NEW_LINE);
        sb.append("70" + NEW_LINE);
        sb.append("6" + NEW_LINE);
        sb.append("0" + NEW_LINE);
        sb.append("LAYER" + NEW_LINE);
        sb.append("2" + NEW_LINE);
        sb.append("Holes" + NEW_LINE);
        sb.append("70" + NEW_LINE);
        sb.append("64" + NEW_LINE);
        sb.append("62" + NEW_LINE);
        sb.append("7" + NEW_LINE);
        sb.append("6" + NEW_LINE);
        sb.append("CONTINUOUS" + NEW_LINE);
        sb.append("0" + NEW_LINE);
        sb.append("LAYER" + NEW_LINE);
        sb.append("2" + NEW_LINE);
        sb.append("Border" + NEW_LINE);
        sb.append("70" + NEW_LINE);
        sb.append("64" + NEW_LINE);
        sb.append("62" + NEW_LINE);
        sb.append("7" + NEW_LINE);
        sb.append("6" + NEW_LINE);
        sb.append("CONTINUOUS" + NEW_LINE);
        sb.append("0" + NEW_LINE);
        sb.append("ENDTAB" + NEW_LINE);
        sb.append("0" + NEW_LINE);
        sb.append("TABLE" + NEW_LINE);
        sb.append("2" + NEW_LINE);
        sb.append("STYLE" + NEW_LINE);
        sb.append("70" + NEW_LINE);
        sb.append("0" + NEW_LINE);
        sb.append("0" + NEW_LINE);
        sb.append("ENDTAB" + NEW_LINE);
        sb.append("0" + NEW_LINE);
        sb.append("ENDSEC" + NEW_LINE);

        return sb.toString();
    }
    
    private String generateDxf(PixelStar star) {
        StringBuilder sb = new StringBuilder();
        sb.append(generateHeader());
        sb.append(getEntitiesSection(star));
        sb.append("0");
        sb.append(NEW_LINE);
        sb.append("EOF");
        sb.append(NEW_LINE);
        return sb.toString();
    }
    
    /**
     * Format a double as a string.
     * @param num double
     * @return string version of double.
     */
    private String doubleToString(double num) {
        return String.format("%6.4f", num);
    }
    
    /**
     * Writes a star to a file.
     * @param fileName File to write to.
     */
    public void writeToFile(String fileName) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(fileName, "UTF-8");
            writer.print(generateDxf(mPixelStar));
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DxfExport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DxfExport.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            writer.close();
        }
    }
}
