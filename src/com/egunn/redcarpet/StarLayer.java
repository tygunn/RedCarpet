/* 
 * Copyright (C) 2018 Tyler J. Gunn
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

import com.egunn.redcarpet.PixelStar.HoleFormat;
//import com.sun.javafx.binding.StringFormatter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines a single layer in a {@link PixelStar}.
 * 
 * @author tyler
 */
public class StarLayer {
   
    /**
     * Thin wrapper class on {@link Ellipse2D.Double} to provide the ability to
     * store whether an ellipse is a vertex of a star.
     */
    public static class PixelHole extends Ellipse2D.Double {
        /**
         * See {@link Ellipse2D.Double#Double(double, double, double, double).
         * @param x
         * @param y
         * @param w
         * @param h 
         */
        public PixelHole(double x, double y, double w, double h) {
            super(x, y, w, h);
        }
        
        /**
         * Indicates whether this ellipse is a vertex of a star.
         */
        private boolean mIsVertex = false;
        
        /**
         * Sets whether this ellipse is a vertex of a star.
         * @param isVertex {@code true} if it is a vertex, {@code false}
         * otherwise.
         */
        public void setIsVertex(boolean isVertex) {
            mIsVertex = isVertex;
        }
        
        /**
         * @return {@code true} if this ellipse is a vertex of a star,
         * {@code false} otherwise.
         */
        public boolean isVertex() {
            return mIsVertex;
        }
        
        @Override
        public String toString() {
            return String.format("(%.2f, %.2f)]", getCenterX(), getCenterY());
        }
    };
    
    /**
     * The star which makes up this layer.
     */
    private final Star mStar;
    
    /**
     * The desired spacing between each of the holes, along the sides of the
     * stars.
     */
    private final double mHoleSpacing;
    
    /**
     * The diameter of the bodies of pixels.
     */
    private final double mPixelBodySize;
    
    /**
     * {@code true} if pixel bodies are shown.
     */
    private final boolean mIsShowingPixelBodies;
    
    /**
     * The actual spacing between each hole taking into account how many holes
     * we ended up placing on each side.
     */
    private double mActualHoleSpacing;
    
    /**
     * The size of each hole.
     */
    private final double mHoleSize;
    
    /**
     * The number of holes per edge of the star.  This can either be calculated
     * or passed in.
     */
    private Integer mNumHolesPerEdge;
    
    /**
     * The translation which will be applied to the star.
     * The stars are generated using a typical coordinate system centered at
     * (0,0) with +/- X and Y.  The translation is used to move to a typical
     * graphics coordinate system based at 0,0 in the top left of the graphics
     * area.
     */
    private AffineTransform mTranslation;
    
    /**
     * The holes placed on the star.
     * Hole placement begins at the top vertex of the star and continues in a
     * counter-clockwise direction.
     */
    private List<Ellipse2D> mHoles = new ArrayList<Ellipse2D>();
    
    /**
     * Where to start labelling the holes in this layer; used when drawing
     * hole numbers.
     */
    private int mHoleCountStart = 0;
    
    /**
     * Creates a new star layer.
     * @param widthInches
     * @param holeSpacingInches
     * @param holeSizeInches 
     */
    public StarLayer(double widthInches, double ratio, double holeSpacingInches,
            double holeSizeInches, double pixelBodySize, 
            boolean isShowingPixelBodies) {
        mStar = new Star(widthInches, ratio);
        mHoleSpacing = holeSpacingInches;
        mHoleSize = holeSizeInches;
        mNumHolesPerEdge = calculateHolesPerEdge();
        mPixelBodySize = pixelBodySize;
        mIsShowingPixelBodies = isShowingPixelBodies;
        placeHoles();
    }
    
    /**
     * Creates a new star layer.
     * @param widthInches
     * @param ratio
     * @param holeSpacingInches
     * @param holeSizeInches
     * @param numHoles
     * @param holeCountStart 
     */
    public StarLayer(double widthInches, double ratio, double holeSpacingInches,
            double holeSizeInches, int numHoles, int holeCountStart,
            double pixelBodySize, boolean isShowingPixelBodies) {
        mStar = new Star(widthInches, ratio);
        mHoleSpacing = holeSpacingInches;
        mHoleSize = holeSizeInches;
        mNumHolesPerEdge = numHoles;
        mHoleCountStart = holeCountStart;
        mPixelBodySize = pixelBodySize;
        mIsShowingPixelBodies = isShowingPixelBodies;
        placeHoles();
    }
    
    /**
     * Calculates the number of holes which will be present on an edge of the
     * star.
     * @return The number of holes per edge.
     */
    private int calculateHolesPerEdge() {
        return (int) (getEdgeLength() / mHoleSpacing);
    }
    
    /**
     * Gets the number of holes which will be present per edge of the star.
     * When {@link #StarLayer(double, double, double) is used to construct
     * the layer, this will be {@link #calculateHolesPerEdge()}.
     * When {@link #StarLayer(double, double, double, int, int)  is used to
     * construct the layer, then the supplied value will be used.
     * @return 
     */
    public int getNumHolesPerEdge() {
        return mNumHolesPerEdge;
    }
    
    /**
     * Sets the number of holes which will be present on each edge of the star.
     * @param holes 
     */
    public void setNumHolesPerEdge(int holes) {
        mNumHolesPerEdge = holes;
    }
    
    /**
     * Place the holes along the edges of the star.
     */
    private void placeHoles() {
        double sideLength = getEdgeLength();
        
        int pixelsToAdd = mNumHolesPerEdge;
        // The start vertex is not included in the calculations below..
        pixelsToAdd--;
        
        List<Point2D> vertices = mStar.getVertices();
        for (int ix = 0 ; ix < vertices.size(); ix++) {
            // Pick two vertices on the star to place holes.
            Point2D start = vertices.get(ix);
            Point2D end = vertices.get((ix + 1)
                    % vertices.size());
            double distance = Point2D.distance(start.getX(), start.getY(), 
                    end.getX(), end.getY());
            
            mActualHoleSpacing = distance / (double)(pixelsToAdd - 1);
            
            // Add the hole at the start of the segment on the star vertex
            PixelHole startHole = new PixelHole(
                    /** top left coordinate */
                    start.getX() - (mHoleSize / 2.0), 
                    start.getY() - (mHoleSize / 2.0),
                    /** x and y width */
                    mHoleSize, 
                    mHoleSize);
            startHole.setIsVertex(true);
            mHoles.add(startHole);
            
            // Divide up the space and add the pixels
            List<Ellipse2D> localHoles = new ArrayList<Ellipse2D>();
            for (int addIx = 1 ; addIx < pixelsToAdd+1; addIx++) {
                // Determine where between the start and end point we are
                // placing the hole.
                double offset = (double) ((pixelsToAdd + 1) - addIx) 
                        / (double) (pixelsToAdd + 1);
                
                PixelHole newHole = new PixelHole(
                    start.getX() + (end.getX() - start.getX()) * offset 
                            - (mHoleSize / 2.0),
                    start.getY() + (end.getY() - start.getY()) * offset 
                            - (mHoleSize / 2.0),
                    /** x and y width */
                    mHoleSize, 
                    mHoleSize);
                newHole.setIsVertex(false);
                localHoles.add(newHole);
            }
            // This seems strange, but we generate the holes backwards from the
            // end.  We place them into the list of holes in reverse order so
            // that when we're draing the numbers they are sequential.
            Collections.reverse(localHoles);
            mHoles.addAll(localHoles);
        }
    }
    
    /**
     * Returns a mutable list of the holes in this star layer.
     * @return List of all holes, including vertices.
     */
    public List<Ellipse2D> getHolesList() {
        return mHoles;
    }
    
    /**
     * Returns a list containing only the vertices of the star for this layer.
     * @return List of the holes corresponding to the vertices of the star.
     */
    public List<Ellipse2D> getVertices() {
        return mHoles.stream()
                .filter(h -> h instanceof StarLayer.PixelHole
                        && ((StarLayer.PixelHole)(h)).isVertex())
                .collect(Collectors.toList());
    }
    
    /**
     * @return returns a copy of the holes used in the star layer.
     */
    public List<Ellipse2D> getHoles() {
        List<Ellipse2D> list = new ArrayList<Ellipse2D>(mHoles.size());
        for (Ellipse2D hole : mHoles) {
            list.add(new Ellipse2D.Double(
                    hole.getX(), 
                    hole.getY(), 
                    hole.getWidth(), 
                    hole.getHeight()));
        }
        return list;
    }
    
    /**
     * Draws this star layer on the specified graphics canvas.
     * @param g The {@link Graphics2D} to draw to.
     * @param isLabellingHoles
     * @param isDrawingOutline
     */
    public void draw(Graphics2D g, boolean isLabellingHoles, 
            boolean isDrawingOutline, HoleFormat holeFormat) {
        AffineTransform at = getOffsetTransform();
        
        if (isDrawingOutline) {
            // Draw the outline of the star.
            Path2D starPath = mStar.getPath();
            starPath.transform(getOffsetTransform());
            g.draw(starPath);
        }
        
        // Draw the holes.
        int holeCount = 1;
        for (Ellipse2D e : getHolesList()) {
            
            // Draw the hole
            Ellipse2D eTran = new Ellipse2D.Double(
                    e.getX() + at.getTranslateX(),
                    e.getY() + at.getTranslateY(),
                    e.getWidth(),
                    e.getHeight());
            
            if (holeFormat == HoleFormat.SOLID) {
                g.setColor(Color.black);
                g.fill(eTran);
                g.draw(eTran);
            } else {
                g.setColor(Color.white);
                g.fill(eTran);
                g.setColor(Color.black);
                g.draw(eTran);
                if (holeFormat == HoleFormat.OUTLINE_TARGET) {
                    // Draw a crosshair in the hole.
                    g.drawLine(
                            (int) (e.getX() + at.getTranslateX() 
                                    + (e.getWidth() / 2.0)),
                            (int) (e.getY() + at.getTranslateY()),
                            (int) (e.getX() + at.getTranslateX() 
                                    + (e.getWidth() / 2.0)),
                            (int) (e.getY() + at.getTranslateY() 
                                    + e.getHeight())
                    );

                    g.drawLine(
                            (int) (e.getX() + at.getTranslateX()),
                            (int) (e.getY() + at.getTranslateY()
                                    + (e.getHeight() / 2)),
                            (int) (e.getX() + at.getTranslateX() 
                                    + e.getWidth()),
                            (int) (e.getY() + at.getTranslateY() 
                                    + (e.getHeight() / 2))
                    );
                }
            }
            
            if (mIsShowingPixelBodies) {
                // Draw the pixelBody
                double bodyRadius = mPixelBodySize / 2.0;
                Stroke startStroke = g.getStroke();
                
                Stroke dashed = new BasicStroke(0.0f, 
                        BasicStroke.CAP_BUTT, 
                        BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
                g.setStroke(dashed);
                Ellipse2D pixelBody = new Ellipse2D.Double(
                    e.getCenterX() - bodyRadius + at.getTranslateX(),
                    e.getCenterY() - bodyRadius + at.getTranslateY(),
                    mPixelBodySize,
                    mPixelBodySize);
                g.draw(pixelBody);
                
                g.setStroke(startStroke);
            }
            
            if (isLabellingHoles) {
                // Label each hole with a hole number.
                String num = "" + (mHoleCountStart + holeCount);
                g.drawString(num, 
                        (float)(e.getX() + at.getTranslateX()), 
                        (float) ( e.getY() + at.getTranslateY()));
            }
            holeCount++;
        }
    }
    
    /**
     * @return Determine the length of an edge of the star.
     */
    public double getEdgeLength() {
        return Point2D.distance(
                mStar.getVertices().get(0).getX(), 
                mStar.getVertices().get(0).getY(), 
                mStar.getVertices().get(1).getX(),
                mStar.getVertices().get(1).getY());
    }
    
    /**
     * @return the width of this star layer as determined by the min and max X
     * values contained in it.
     */
    public double getWidth() {
        return getMaxX() - getMinX();
    }
    
    /**
     * @return the height of this star layer as determined by the min and max Y
     * values contained in it.
     */
    public double getHeight() {
        return getMaxY() - getMinY();
    }
    
    /**
     * @return The minimum X value bounding the star layer.
     */
    public double getMinX() {
        double starMinX = mStar.getMinX();
        double holeMinX = 0.0;
        for (Ellipse2D e : getHoles()) {
            holeMinX = Math.min(holeMinX, e.getX());
        }
        return Math.min(starMinX, holeMinX);
    }
    
    /**
     * @return The maximum X value bounding the star layer.
     */
    public double getMaxX() {
        double starMaxX = mStar.getMaxX();
        double holeMaxX = 0.0;
        for (Ellipse2D e : getHoles()) {
            holeMaxX = Math.max(holeMaxX, e.getX() + e.getWidth());
        }
        return Math.max(starMaxX, holeMaxX);
    }
    
    /**
     * @return The minimum Y value bounding the star layer.
     */
    public double getMinY() {
        double starMinY= mStar.getMinY();
        double holeMinY = 0.0;
        for (Ellipse2D e : getHoles()) {
            holeMinY = Math.min(holeMinY, e.getY());
        }
        return Math.min(starMinY, holeMinY);
    }

    /**
     * @return The maximum Y value bounding the star layer.
     */
    public double getMaxY() {
        double starMaxY = mStar.getMaxY();
        double holeMaxY = 0.0;
        for (Ellipse2D e : getHoles()) {
            holeMaxY = Math.max(holeMaxY, e.getY() + e.getHeight());
        }
        return Math.max(starMaxY, holeMaxY);
    }
    
    /**
     * Get the translation applied to move this star layer into alignment with
     * the other star layers.
     * @return 
     */
    public AffineTransform getOffsetTransform() {
        if (mTranslation != null ) {
            return mTranslation;
        }
        return AffineTransform.getTranslateInstance(
                -1.0 * getMinX(), 
                -1.0 * getMinY());
    }
    
    /**
     * @param trans Sets the transform used to align this layer with the others.
     */
    public void setOffsetTransform(AffineTransform trans) {
        mTranslation = trans;
    }
    
    /**
     * @return The number of pixels in this layer.
     */
    public int getPixelCount() {
        return mHoles.size();
    }
    
    /**
     * @return the actual hole spacing; this takes into account the fact that
     * the first layer is going to have {@link #mHoleSpacing} between each pixel
     * but that subsequent layers simply get one less pixel, so the spacing
     * changes.
     */
    public double getActualHoleSpacing() {
        return mActualHoleSpacing;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[StarLayer ");
        sb.append("pixPerSide=");
        sb.append(mHoles.size() / 10);
        
        sb.append(", topLeft=(");
        sb.append(getMinX());
        sb.append(",");
        sb.append(getMinY());
        sb.append(")");
         
        sb.append(", bottomRight=(");
        sb.append(getMaxX());
        sb.append(",");
        sb.append(getMaxY());
        sb.append(")");
        
        sb.append(", width=");
        sb.append(getWidth());
        sb.append(", height=");
        sb.append(getHeight());
        
        sb.append(", sideLength=");
               
        mStar.getVertices().get(1);
        
        sb.append("]");
      
        return sb.toString();
    }
}
