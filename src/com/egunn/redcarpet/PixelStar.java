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

import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_BUTT;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a pixel star with layers of pixels.
 * @author tyler
 */
public class PixelStar {
    
    public enum HoleFormat {
        OUTLINE, // Holes are drawn as outlines.
        SOLID, // Holes are solid black.
        OUTLINE_TARGET // Holes are drawn as outlines with a drill target in
                        // the middle.
    }
    
    /**
     * The default size of a hole for a pixel, in inches.
     * This is 12mm.
     */
    public static final double PIXEL_HOLE_IN_INCHES = 0.472441;
    
    /**
     * The typical number of Java points per inch.
     */
    public static final double PTS_PER_INCH = 72.0;
    
    /**
     * Desired width of the pixel star.
     */
    private final double mWidth;
    
    /**
     * Size of the hole required for a pixel.
     */
    private final double mHoleDiameter;
    
    /**
     * The spacing between the holes.
     */
    private final double mHoleSpacing;
    
    /**
     * The ratio of the outer to inner pentagon of the star; determines how
     * "beefy" or "gaunt" the star will be.
     */
    private final double mPentagonRatio;
    
    /**
     * {@code true} if a border should be drawn around the star.
     */
    private boolean mIsDrawingBorder = true;
    
    /**
     * {@code true} if the inner star outlines should be drawn.
     */
    private boolean mIsDrawingInnerOutlines = true;
    
    /**
     * {@code true} if the holes should be numbered.
     */
    private boolean mIsLabelingHoles = true;
    
    /**
     * The spacing between the rows/layers.
     */
    private final double mRowSpacing;
    
    /**
     * The number of rows in the star.
     */
    private final int mRows;
    
    /**
     * What format the drawn holes will have.
     */
    private final HoleFormat mHoleFormat;
    
    /**
     * The border of the star.
     */
    private Star mBorder;
    
    /**
     * The layers/rows of the star.
     */
    private List<StarLayer> mStarLayers;
    
    /**
     * How many holes are there?
     */
    private int mNumHoles;
    
    public PixelStar(double width, double ratio, double holeDiameter, 
            double holeSpacing, int rows, double rowSpacing,
            boolean isDrawingBorder, boolean isDrawingStarOutlines,
            boolean isLabelingHoles, HoleFormat holeFormat) {
        mStarLayers = new ArrayList<StarLayer>();
        mWidth = width;
        mPentagonRatio = ratio;
        mHoleDiameter = holeDiameter;
        mHoleSpacing = holeSpacing;
        mRows = rows;
        mRowSpacing = rowSpacing;
        mIsDrawingBorder = isDrawingBorder;
        mIsDrawingInnerOutlines = isDrawingStarOutlines;
        mIsLabelingHoles = isLabelingHoles;
        mHoleFormat = holeFormat;
        
        buildStar();
    }
    
    /**
     * Draws the star to the graphics instance.
     * @param g 
     */
    public void draw(Graphics2D g) {
        g.setColor(Color.black);
        g.setStroke(new BasicStroke(1.0f, CAP_BUTT, BasicStroke.JOIN_MITER));
        
        // Draw the outline of the star.
        if (mIsDrawingBorder) {
            Path2D starPath = mBorder.getPath();
            starPath.transform(mBorder.getOffsetTransform());
            g.draw(starPath);
        }
        
        for (StarLayer layer : mStarLayers) {
            layer.draw(g, mIsLabelingHoles, mIsDrawingInnerOutlines, 
                    mHoleFormat);
        }
        
        // Draw a scale ruler.
        /*String num = "1 inch";
                g.drawString(num, 10.0f, 10.0f);
        g.drawLine(10, 10, 
                (int) java.awt.Toolkit.getDefaultToolkit().getScreenResolution() 
                        + 10, 10);*/
    }
    
    private void buildStar() {
        AffineTransform trans = null;
        double width = mWidth;
        if (mIsDrawingBorder) {
            mBorder = new Star(mWidth, mPentagonRatio);
            trans = mBorder.getOffsetTransform();
            width -= mRowSpacing;
        }
        
        int numHoles = 0;
        int totalPixels = 0;
        for (int layer = 0 ; layer < mRows; layer++) {
            StarLayer sl;
            if (layer == 0) {
                sl = new StarLayer(width - ((double)layer * mRowSpacing), 
                        mPentagonRatio, mHoleSpacing, mHoleDiameter);
                numHoles = sl.getNumHolesPerEdge();
            } else {
                numHoles--;
                sl = new StarLayer(width - ((double)layer * mRowSpacing), 
                        mPentagonRatio, mHoleSpacing, mHoleDiameter, numHoles, 
                        totalPixels);
            }
            if (trans == null) {
                trans = sl.getOffsetTransform();
            } else {
                sl.setOffsetTransform(trans);
            }
            totalPixels += sl.getPixelCount();
            mStarLayers.add(sl);
            System.out.println("Star layer: " + sl);
        }
        System.out.println("Total holes: " + totalPixels);
        mNumHoles = totalPixels;
    }
    
    public int getWidth() {
        if (mIsDrawingBorder) {
            return (int) Math.ceil(mBorder.getWidth());
        } else {
            return (int) Math.ceil(mStarLayers.get(0).getWidth());
        }
    }
    
    public int getHeight() {
        if (mIsDrawingBorder) {
            return (int) Math.ceil(mBorder.getHeight());
        } else {
            return (int) Math.ceil(mStarLayers.get(0).getHeight());
        }
    }
    
    public int getNumberHoles() {
        return mNumHoles;
    }
    
    public List<StarLayer> getLayers() {
        return mStarLayers;
    }
}

