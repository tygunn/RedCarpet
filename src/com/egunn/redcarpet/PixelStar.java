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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a pixel star with layers of pixels.
 * @author tyler
 */
public class PixelStar implements Printable {

    public enum HoleFormat {
        OUTLINE, // Holes are drawn as outlines.
        SOLID, // Holes are solid black.
        OUTLINE_TARGET // Holes are drawn as outlines with a drill target in
                        // the middle.
    }
    
    /**
     * @param format Hole format as an int.
     * @return  The {@link HoleFormat} value for the int.
     */
    public static HoleFormat getHoleFormatFromInt(int format) {
        switch (format) {
            case 0:
                return HoleFormat.OUTLINE;
            case 1:
                return HoleFormat.SOLID;
            case 2:
                return HoleFormat.OUTLINE_TARGET;
            default:
                return HoleFormat.OUTLINE;
        }
    }
    
    /**
     * The default size of a hole for a pixel, in inches.
     * This is 12mm.
     */
    public static final double PIXEL_HOLE_IN_INCHES = 0.472441;
        
    /**
     * Desired width of the pixel star.
     */
    private final double mWidth;
    
    /**
     * Size of the hole required for a pixel.
     */
    private final double mHoleDiameter;
    
    /**
     * Diameter of the pixel bodies.
     */
    private final double mPixelBodyDiameter;
    
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
     * {@code true} if pixel bodies should be drawn.
     */
    private final boolean mIsShowingPixelBodies;
    
    /**
     * The spacing between the rows/layers.
     */
    private final double mRowSpacing;
    
    /**
     * The spacing between the layers and the edge.
     */
    private final double mEdgeSpacing;
    
    /**
     * The number of rows in the star.
     */
    private final int mRows;
    
    /**
     * What format the drawn holes will have.
     */
    private final HoleFormat mHoleFormat;
    
    /**
     * Points per inch.
     */
    private final int mResolution;

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
    
    /**
     * {@code true} if the star is being printed.
     */
    private boolean mIsPrinting = false;

    /**
     * Total pages (for printing).
     */
    private int mTotalPages = 0;
    
    /**
     * Page offsets used for printing.
     */
    private List<Double> mPageOffsetsX = new ArrayList<>();
    
    /**
     * Page offsets used for printing.
     */
    private List<Double> mPageOffsetsY = new ArrayList<>();
    
    
    public PixelStar(double width, double ratio, double holeDiameter, 
            double pixelBodyDiameter,
            double holeSpacing, int rows, double rowSpacing, double edgeSpacing,
            boolean isDrawingBorder, boolean isDrawingStarOutlines,
            boolean isLabelingHoles, boolean isShowingPixelBodies,
            HoleFormat holeFormat, int resolution) {
        mStarLayers = new ArrayList<StarLayer>();
        mWidth = width;
        mPentagonRatio = ratio;
        mHoleDiameter = holeDiameter;
        mPixelBodyDiameter = pixelBodyDiameter;
        mHoleSpacing = holeSpacing;
        mRows = rows;
        mRowSpacing = rowSpacing;
        mEdgeSpacing = edgeSpacing;
        mIsDrawingBorder = isDrawingBorder;
        mIsDrawingInnerOutlines = isDrawingStarOutlines;
        mIsLabelingHoles = isLabelingHoles;
        mIsShowingPixelBodies = isShowingPixelBodies;
        mHoleFormat = holeFormat;
        mResolution = resolution;
        
        buildStar();
    }
    
    /**
     * Draws the star to the graphics instance.
     * @param g 
     */
    public void draw(Graphics2D g) {
        System.out.println("Drawing");
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
        //String num = "1 inch";
        //        g.drawString(num, 10.0f, 10.0f);
        //g.drawLine(10, 10, mResolution+ 10, 10);
    }
    
    private void buildStar() {
        AffineTransform trans = null;
        double width = mWidth;
        if (mIsDrawingBorder) {
            mBorder = new Star(mWidth, mPentagonRatio);
            trans = mBorder.getOffsetTransform();
            width -= mEdgeSpacing;
        }
        
        int numHoles = 0;
        int totalPixels = 0;
        for (int layer = 0 ; layer < mRows; layer++) {
            StarLayer sl;
            if (layer == 0) {
                sl = new StarLayer(width - ((double)layer * mRowSpacing), 
                        mPentagonRatio, mHoleSpacing, mHoleDiameter,
                mPixelBodyDiameter, mIsShowingPixelBodies);
                numHoles = sl.getNumHolesPerEdge();
            } else {
                numHoles--;
                sl = new StarLayer(width - ((double)layer * mRowSpacing), 
                        mPentagonRatio, mHoleSpacing, mHoleDiameter, numHoles, 
                        totalPixels, mPixelBodyDiameter, mIsShowingPixelBodies);
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
    
    public Star getBorder() {
        return mBorder;
    }
    
    public double getRatio() {
        return mPentagonRatio;
    }
    
    /**
     * Prints the current star spanning multiple pages.
     * @param graphics
     * @param pageFormat
     * @param pageIndex
     * @return
     * @throws PrinterException 
     */
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) 
            throws PrinterException {
        Graphics2D g2d = (Graphics2D) graphics;
        
        if (!mIsPrinting) {
            mIsPrinting = true;
            int pagesWidth = (int) Math.ceil(getWidth() 
                    / pageFormat.getImageableWidth());
            int pagesHigh = (int) Math.ceil(getHeight()
                    / pageFormat.getImageableHeight());
            mTotalPages = pagesWidth * pagesHigh;
            int curX = 0;
            int curY = 0;
            double pageWidth = pageFormat.getImageableWidth();
            double pageHeight = pageFormat.getImageableHeight();
            for (int ix = 0; ix < mTotalPages; ix++) {
                mPageOffsetsX.add(-1.0 * pageWidth * (double) curX 
                        + pageFormat.getImageableX());
                mPageOffsetsY.add(-1.0 * pageHeight * (double) curY
                        + pageFormat.getImageableY());
                curX = (curX + 1) % pagesWidth;
                if (curX == 0) {
                    curY = (curY + 1) % pagesHigh;
                }
            }
            
            System.out.println("Imageable X: "+ pageFormat.getImageableX());
            System.out.println("Page Width: " + pageFormat.getWidth());
            System.out.println("Imagable Width: "+ pageFormat.getImageableWidth());
            System.out.println("Imageable Y: "+ pageFormat.getImageableY());
            System.out.println("Page Height: " + pageFormat.getHeight());
            System.out.println("Imagable Height: "+ pageFormat.getImageableHeight());

        }
        // We have only one page, and 'page'
        // is zero-based
        if (pageIndex >= mTotalPages) {
            mIsPrinting = false;
            return NO_SUCH_PAGE;
        }
        int imgX = (int)Math.round(pageFormat.getImageableX());
        int imgY = (int)Math.round(pageFormat.getImageableY());
               
        // Translate the star on the page.
        g2d.translate(mPageOffsetsX.get(pageIndex), 
                mPageOffsetsY.get(pageIndex));
        
        //g2d.drawLine(0, 0, 0, 100);
        //g2d.drawLine(0, 0, 100, 0);
        //g2d.drawLine((int)pageFormat.getImageableWidth(), 0, (int)pageFormat.getImageableWidth(), 100);
        //g2d.drawLine((int)pageFormat.getImageableWidth()-100, 0, (int)pageFormat.getImageableWidth(), 0);
        
        draw(g2d);

        return PAGE_EXISTS;
    }
}

