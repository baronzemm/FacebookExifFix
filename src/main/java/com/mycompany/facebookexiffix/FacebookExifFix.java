/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.facebookexiffix;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

 /**
    * parse folders / subfolders
    * 
    * parse index file
    * edit exif
    * output status / result
    */

/**
 *
 * @author baron
 */
public class FacebookExifFix {
    
    public static void main(String[] args) {
        
        try {
                   
            //Quick and dirty code
            
            String sInput = args[0];
            String sOutput = args[1];
            
            //Parse dirs
            Files.walk(Paths.get(sInput))
                    //.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith("index.htm"))
                    //.forEach(out -> PrintTest(out.toString()));
                    .forEach(out -> ParseIndex(out, sInput, sOutput));
                    //.collect(Collectors.toList());
            
        } catch (IOException ex) {
            Logger.getLogger(FacebookExifFix.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void PrintTest(String input)
    {
        System.out.println(input);
    }
    
    public static void ParseIndex(Path p, String sIn, String sOut) {
        
        //For each get the image source and the meta div
        
        /*
            <div class="block">
            <img src="photos/100604750494/100606835494.jpg">
            <div>
             <div class="meta">
              Sunday, November 16, 2008 at 12:18pm EST
             </div>
            </div>
           </div>
        */
        Document doc;
        try {
            doc = Jsoup.parse(p.toFile(),"UTF-8", "http://example.com/");            

            Elements contents = doc.getElementsByClass("block");
            
            for (Element content : contents) {
                        
                Elements images = content.getElementsByTag("img");
                
                String sImagePath =  images.get(0).attr("src");
                
                String sSourceRoot = sIn;
                String sDestRoot = sOut;
                
                Elements meta = content.getElementsByClass("meta");
                String sDateValue = meta.get(0).ownText();
                
                File fIn = new File(sSourceRoot + sImagePath);
                                
                File fOut = new File(sDestRoot + sImagePath);
                
                File parent = fOut.getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    throw new IllegalStateException("Couldn't create dir: " + parent);
                }
                
                fOut.createNewFile();
                
                changeExifMetadata(fIn, fOut, sDateValue);   
                
                
            }
            
            System.out.println("Success");

        } catch (IOException ex) {
            Logger.getLogger(FacebookExifFix.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception x) {
             Logger.getLogger(FacebookExifFix.class.getName()).log(Level.SEVERE, null, x);
        }

    }
    
    public static void changeExifMetadata(final File jpegImageFile, final File dst, String sValue)
        throws Exception {
        
    OutputStream os = null;
    
    try {
        TiffOutputSet outputSet = null;

        // note that metadata might be null if no metadata is found.
        final ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
        final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        
        if (null != jpegMetadata) {
            // note that exif might be null if no Exif metadata is found.
            final TiffImageMetadata exif = jpegMetadata.getExif();

            if (null != exif) {
                outputSet = exif.getOutputSet();
            }
        }

        // if file does not contain any exif metadata, we create an empty
        // set of exif metadata. Otherwise, we keep all of the other
        // existing tags.
        if (null == outputSet) {
            outputSet = new TiffOutputSet();
        }

                  
            final TiffOutputDirectory exifDirectory = outputSet
                    .getOrCreateExifDirectory();
            // make sure to remove old value if present (this method will
            // not fail if the tag does not exist).
            exifDirectory
                    .removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
            
            //Tuesday, July 3, 2007 at 5:19pm EDT
            //YYYY:MM:DD HH:MM:SS
            
    
            SimpleDateFormat dateParser = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' hh:mmaaa z");            
            Date d = dateParser.parse(sValue);

            String sDateOut = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").format(d);
            
            exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL,
                    sDateOut);
        

        os = new FileOutputStream(dst);
        os = new BufferedOutputStream(os);

        new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
                outputSet);

        os.flush();
        os.close();
    }
    catch(Exception ex) {
        throw(ex);
    }
}
    
}
