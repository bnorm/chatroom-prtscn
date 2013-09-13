package com.bnorm.applets;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

public class ScreenCapture extends JApplet {

    // upload to : http://<redacted>/FILES/upload.php
    @Override
    public void init() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    System.out.println(getDocumentBase());
                    JButton button = new JButton("PtrScn");
                    button.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            try {
                                File file = new File("printscreen.jpg");
                                ImageIO.write(prtscn(), "jpg", file);
                                System.out.println("Screen printed to [" + file.getAbsolutePath() + "]");
                            } catch (IOException e) {
                                System.err.println(e);
                            } catch (AWTException e) {
                                System.err.println(e);
                            }
                        }
                    });
                    add(button);
                }
            });
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    @Override
    public void destroy() {
        File file = new File("printscreen.jpg");
        boolean success = file.delete();
        System.out.println("File [" + file + "] was" + (success ? "" : " not") + " deleted.");
    }

    public static BufferedImage prtscn() throws AWTException {
        Rectangle screenRect = new Rectangle(0, 0, 0, 0);
        for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            screenRect = screenRect.union(gd.getDefaultConfiguration().getBounds());
        }

        return new Robot().createScreenCapture(screenRect);
    }
}