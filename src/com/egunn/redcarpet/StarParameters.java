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

/**
 * This class encapsulates the parameters requires to generate a
 * {@link PixelStar}.
 * 
 * @author Tyler Gunn
 */
public class StarParameters {
    private String mStarName;
    private String mUnits;
    private double mStarWidth;
    private double mRatio;
    private double mHoleDiameter;
    private double mPixelBodyDiameter;
    private double mHoleSpacing;
    private double mRowSpacing;
    private int mLayers;
    private boolean mIsOuterBorderVisible;
    private boolean mAreInnerBordersVisible;
    private boolean mIsLabellingHoles;
    private boolean mIsShowingPixelBodies;
    private int mHoleType;
    
    public StarParameters(
            String starName, 
            String units, 
            double starWidth,
            double ratio,
            double holeDiameter,
            double pixelBodyDiameter,
            double holeSpacing, 
            double rowSpacing,
            int layers, 
            boolean isOuterBorderVisible,
            boolean areInnerBordersVisible, 
            boolean isLabellingHoles,
            boolean isShowingPixelBodies,
            int holeType) {
        mStarName = starName;
        mUnits = units;
        mStarWidth = starWidth;
        mRatio = ratio;
        mHoleDiameter = holeDiameter;
        mPixelBodyDiameter = pixelBodyDiameter;
        mHoleSpacing = holeSpacing;
        mRowSpacing = rowSpacing;
        mLayers = layers;
        mIsOuterBorderVisible = isOuterBorderVisible;
        mAreInnerBordersVisible = areInnerBordersVisible;
        mIsLabellingHoles = isLabellingHoles;
        mIsShowingPixelBodies = isShowingPixelBodies;
        mHoleType = holeType;
    }
    
     /**
     * @return the mStarName
     */
    public String getStarName() {
        return mStarName;
    }

    /**
     * @param mStarName the mStarName to set
     */
    public void setStarName(String mStarName) {
        this.mStarName = mStarName;
    }
    
    /**
     * @return the mUnits
     */
    public String getUnits() {
        return mUnits;
    }

    /**
     * @param mUnits the mUnits to set
     */
    public void setUnits(String mUnits) {
        this.mUnits = mUnits;
    }

    /**
     * @return the mStarWidth
     */
    public double getStarWidth() {
        return mStarWidth;
    }

    /**
     * @param mStarWidth the mStarWidth to set
     */
    public void setStarWidth(double mStarWidth) {
        this.mStarWidth = mStarWidth;
    }

        /**
     * @return the mRatio
     */
    public double getRatio() {
        return mRatio;
    }

    /**
     * @param mRatio the mRatio to set
     */
    public void setRatio(double mRatio) {
        this.mRatio = mRatio;
    }
   
    /**
     * @return the mHoleDiameter
     */
    public double getHoleDiameter() {
        return mHoleDiameter;
    }

    /**
     * @param mHoleDiameter the mHoleDiameter to set
     */
    public void setHoleDiameter(double mHoleDiameter) {
        this.mHoleDiameter = mHoleDiameter;
    }
    
    /**
     * @return the pixel body diameter.
     */
    public double getPixelBodyDiameter() {
        return mPixelBodyDiameter;
    }
    
    /**
     * @param pixelBodyDiameter the new body diameter.
     */
    public void setPixelBodyDiameter(double pixelBodyDiameter) {
        this.mPixelBodyDiameter = pixelBodyDiameter;
    }

    /**
     * @return the mHoleSpacing
     */
    public double getHoleSpacing() {
        return mHoleSpacing;
    }

    /**
     * @param mHoleSpacing the mHoleSpacing to set
     */
    public void setHoleSpacing(double mHoleSpacing) {
        this.mHoleSpacing = mHoleSpacing;
    }

    /**
     * @return the mRowSpacing
     */
    public double getRowSpacing() {
        return mRowSpacing;
    }

    /**
     * @param mRowSpacing the mRowSpacing to set
     */
    public void setRowSpacing(double mRowSpacing) {
        this.mRowSpacing = mRowSpacing;
    }

    /**
     * @return the mLayers
     */
    public int getLayers() {
        return mLayers;
    }

    /**
     * @param mLayers the mLayers to set
     */
    public void setLayers(int mLayers) {
        this.mLayers = mLayers;
    }

    /**
     * @return the mIsOuterBorderVisible
     */
    public boolean isOuterBorderVisible() {
        return mIsOuterBorderVisible;
    }

    /**
     * @param mIsOuterBorderVisible the mIsOuterBorderVisible to set
     */
    public void setIsOuterBorderVisible(boolean mIsOuterBorderVisible) {
        this.mIsOuterBorderVisible = mIsOuterBorderVisible;
    }

    /**
     * @return the mAreInnerBordersVisible
     */
    public boolean areInnerBordersVisible() {
        return mAreInnerBordersVisible;
    }

    /**
     * @param mAreInnerBordersVisible the mAreInnerBordersVisible to set
     */
    public void setAreInnerBordersVisible(boolean mAreInnerBordersVisible) {
        this.mAreInnerBordersVisible = mAreInnerBordersVisible;
    }

    /**
     * @return the mIsLabellingHoles
     */
    public boolean isLabellingHoles() {
        return mIsLabellingHoles;
    }

    /**
     * @param mIsLabellingHoles the mIsLabellingHoles to set
     */
    public void setIsLabellingHoles(boolean mIsLabellingHoles) {
        this.mIsLabellingHoles = mIsLabellingHoles;
    }
    
    /**
     * @return {@code true} if pixel bodies are shown.
     */
    public boolean isShowingPixelBodies() {
        return mIsShowingPixelBodies;
    }
    
    public void setIsShowingPixelBodies(boolean isShowingPixelBodies) {
        mIsShowingPixelBodies = isShowingPixelBodies;  
    }

    /**
     * @return the mHoleType
     */
    public int getHoleType() {
        return mHoleType;
    }

    /**
     * @param mHoleType the mHoleType to set
     */
    public void setHoleType(int mHoleType) {
        this.mHoleType = mHoleType;
    }
}
