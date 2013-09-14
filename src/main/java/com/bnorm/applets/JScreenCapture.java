package com.bnorm.applets;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class JScreenCapture extends JApplet {

   public static final String PARAMETER_POST_URL = "postURL";

   public static final String IMAGE_TYPE = "jpg";

   @Override
   public void init() {
      final String postURL = getParameter(PARAMETER_POST_URL);
      if (postURL == null) {
         System.err.println("No POST URL provided!");
         return;
      }

      try {
         SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
               JButton button = new JButton("PtrScn");
               button.addActionListener(new ActionListener() {
                  @Override
                  public void actionPerformed(ActionEvent event) {
                     post(postURL, prtscn());
                  }
               });
               add(button);
            }
         });
      } catch (Exception e) {
         System.err.println(e);
      }
   }

   public static BufferedImage prtscn() {
      System.out.println("ENTER prtscn()");
      Rectangle screenRect = new Rectangle(0, 0, 0, 0);
      for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
         screenRect = screenRect.union(gd.getDefaultConfiguration().getBounds());
      }

      BufferedImage image = null;
      try {
         image = new Robot().createScreenCapture(screenRect);
      } catch (AWTException e) {
         System.err.println(e);
      }
      System.out.println("EXIT prtscn() => " + image);
      return image;
   }

   public static boolean post(String postURL, BufferedImage image) {
      System.out.println("ENTER post(" + postURL + ", " + image + ")");
      boolean successful = false;

      if (postURL == null) {
         System.err.println("POST URL parameter is null!");
         System.out.println("EXIT post(null, " + image + ") => false");
         return false;
      } else if (image == null) {
         System.err.println("Image parameter is null!");
         System.out.println("EXIT post(" + postURL + ", null) => false");
         return false;
      }

      byte[] array = convert(image);
      if (array == null) {
         System.err.println("Unable to convert image to byte array!");
         System.out.println("EXIT post(" + postURL + ", " + image + ") => false");
         return false;
      }

      try {
         CloseableHttpClient client = HttpClients.createDefault();
         System.out.println("    Opening client connection => " + client);
         try {
            HttpPost post = new HttpPost(postURL);
            System.out.println("    Building POST => " + post);
            MultipartEntity entity = new MultipartEntity();
            ByteArrayBody body = new ByteArrayBody(array, null);
            entity.addPart("image", body);
            post.setEntity(entity);

            System.out.println("    Executing POST => " + post);
            CloseableHttpResponse response = client.execute(post);
            System.out.println("    Received response => " + response);
            try {
               HttpEntity resEntity = response.getEntity();
               System.out.println("    Consuming response entity => " + resEntity);
               EntityUtils.consume(resEntity);
               successful = true;
            } finally {
               response.close();
            }
         } finally {
            client.close();
         }
      } catch (IOException e) {
         System.out.println("There was an exception POSTing image!");
         System.err.println(e);
      }

      System.out.println("EXIT post(" + postURL + ", " + image + ") => " + successful);
      return successful;
   }

   public static byte[] convert(BufferedImage image) {
      System.out.println("ENTER convert(" + image + ")");
      byte[] bytes = null;
      try {
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         try {
            ImageIO.write(image, IMAGE_TYPE, stream);
            stream.flush();
            bytes = stream.toByteArray();
         } finally {
            stream.close();
         }
      } catch (IOException e) {
         System.out.println("There was an exception converting image!");
         System.err.println(e);
      }
      System.out.println("EXIT convert(" + image + ") => " + (bytes == null ? null : "Byte[" + bytes.length + "]"));
      return bytes;
   }
}
