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
import de.erichseifert.vectorgraphics2d.Document;
import de.erichseifert.vectorgraphics2d.Processor;
import de.erichseifert.vectorgraphics2d.Processors;
import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import de.erichseifert.vectorgraphics2d.intermediate.CommandSequence;
import de.erichseifert.vectorgraphics2d.pdf.PDFProcessor;
import de.erichseifert.vectorgraphics2d.util.PageSize;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Junk class for playing around with stuff and things; ignore.
 * @author tyler
 */
public class RedCarpet {

    /**
     * @param args the command line arguments
     */
    public static void nMain(String[] args) {
        // TODO code application logic here
        Graphics2D vg2d = new VectorGraphics2D();

        CommandSequence commands = ((VectorGraphics2D) vg2d).getCommands();
        PDFProcessor pdfProcessor = new PDFProcessor(true);
        Document doc = pdfProcessor.getDocument(commands, PageSize.LETTER);
        try {
            doc.writeTo(new FileOutputStream("test.pdf"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(
                    RedCarpet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(
                    RedCarpet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        BufferedImage bImg = new BufferedImage(72*72, 72*72, 
                BufferedImage.TYPE_INT_RGB);
        
        PixelStar ps = new PixelStar(72.0f * PixelStar.PTS_PER_INCH, 
                Star.DEFAULT_OUTER_TO_INNER_RATIO,
                PixelStar.PIXEL_HOLE_IN_INCHES * PixelStar.PTS_PER_INCH,
                1.5f * PixelStar.PTS_PER_INCH /* holeSpacing */,
                12 /* numRows */,
                5.0f * PixelStar.PTS_PER_INCH, true, true, true, 
                HoleFormat.OUTLINE);
        Graphics2D cg = bImg.createGraphics();
        
        cg.setBackground(Color.white);
        cg.setColor(Color.white);
        cg.fillRect(0, 0, bImg.getWidth(), bImg.getHeight());
        cg.setColor(Color.black);
        ps.draw(cg);
        try {
            if (ImageIO.write(bImg, "png", new File("output_image.png")))
            {
                System.out.println("-- saved");
            }
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        System.out.println(cg.toString());
    }

}
