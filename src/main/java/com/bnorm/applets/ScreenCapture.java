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

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class ScreenCapture extends JApplet {

   @Override
   public void init() {
      final String postURL = getParameter("postURL");

      try {
         SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
               JButton button = new JButton("PtrScn");
               button.addActionListener(new ActionListener() {
                  @Override
                  public void actionPerformed(ActionEvent event) {
                     try {
                        File file = new File("printscreen.jpg");
                        ImageIO.write(prtscn(), "jpg", file);
                        System.out.println("Screen printed to [" + file.getAbsolutePath() + "]");
                        postData(postURL, file);
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

   public static void postData(String urlString, File file) throws IOException {
      CloseableHttpClient client = HttpClients.createDefault();
      try {

         MultipartEntity entity = new MultipartEntity();
         FileBody body = new FileBody(file);
         entity.addPart("image", body);

         HttpPost post = new HttpPost(urlString);
         post.setEntity(entity);

         CloseableHttpResponse response = client.execute(post);
         try {
            HttpEntity resEntity = response.getEntity();

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            if (resEntity != null) {
               System.out.println("Response content length: " + resEntity.getContentLength());
               EntityUtils.consume(resEntity);
            }
            System.out.println("----------------------------------------");
         } finally {
            response.close();
         }
      } finally {
         client.close();
      }
   }
}