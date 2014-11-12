import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.video.*; 
import jp.nyatla.nyar4psg.*; 

import jp.nyatla.nyartoolkit.rpf.reality.nyartk.*; 
import jp.nyatla.nyartoolkit.core.*; 
import jp.nyatla.nyartoolkit.core.labeling.artoolkit.*; 
import jp.nyatla.nyartoolkit.core.raster.*; 
import jp.nyatla.nyartoolkit.core.types.stack.*; 
import jp.nyatla.nyartoolkit.core.utils.*; 
import jp.nyatla.nyartoolkit.core.pickup.*; 
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.*; 
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.*; 
import jp.nyatla.nyartoolkit.psarplaycard.*; 
import jp.nyatla.nyartoolkit.rpf.mklib.*; 
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.*; 
import jp.nyatla.nyartoolkit.markersystem.*; 
import jp.nyatla.nyartoolkit.rpf.sampler.lrlabel.*; 
import jp.nyatla.nyartoolkit.core.match.*; 
import jp.nyatla.nyartoolkit.core.param.*; 
import jp.nyatla.nyartoolkit.core.types.matrix.*; 
import jp.nyatla.nyartoolkit.core.rasterfilter.gs.*; 
import jp.nyatla.nyartoolkit.core.transmat.optimize.*; 
import jp.nyatla.nyartoolkit.core.transmat.rotmatrix.*; 
import jp.nyatla.nyartoolkit.core.pixeldriver.*; 
import jp.nyatla.nyartoolkit.nyidmarker.data.*; 
import jp.nyatla.nyartoolkit.core.squaredetect.*; 
import jp.nyatla.nyartoolkit.detector.*; 
import jp.nyatla.nyartoolkit.rpf.tracker.nyartk.status.*; 
import jp.nyatla.nyartoolkit.core.transmat.solver.*; 
import jp.nyatla.nyartoolkit.core.pca2d.*; 
import jp.nyatla.nyartoolkit.core.raster.rgb.*; 
import jp.nyatla.nyartoolkit.rpf.realitysource.nyartk.*; 
import jp.nyatla.nyartoolkit.nyidmarker.*; 
import jp.nyatla.nyartoolkit.core.labeling.*; 
import jp.nyatla.nyartoolkit.test.*; 
import jp.nyatla.nyartoolkit.core.transmat.optimize.artoolkit.*; 
import jp.nyatla.nyar4psg.*; 
import jp.nyatla.nyartoolkit.core.analyzer.histogram.*; 
import jp.nyatla.nyartoolkit.core.rasterfilter.*; 
import jp.nyatla.nyartoolkit.core.transmat.*; 
import jp.nyatla.nyartoolkit.markersystem.utils.*; 
import jp.nyatla.nyartoolkit.core.rasterdriver.*; 
import jp.nyatla.nyartoolkit.core.types.*; 
import jp.nyatla.nyartoolkit.rpf.utils.*; 
import jp.nyatla.nyartoolkit.processor.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class EN2 extends PApplet {




Capture cam_base, cam_crop;
PImage crop;
MultiMarker nya;

//Blur\u51e6\u7406\u306e\u9ad8\u901f\u5316\u306e\u305f\u3081\u306bPShader\u306e\u5c0e\u5165\u3092\u691c\u8a0e(2014/11/11)
PShader blur;



// \u30de\u30fc\u30ab\u5ea7\u6a19(x, y)
public int marker_upperLeft_x;
public int marker_upperLeft_y;
public int marker_lowerRight_x;
public int marker_lowerRight_y;  

public static final int markerNum = 2;

public void setup() {
  size(1024,768,P3D);
  colorMode(RGB, 100);
  println(MultiMarker.VERSION);  

//PShader\u306b\u3088\u308bBlur\u30d5\u30a3\u30eb\u30bf\u30d5\u30a1\u30a4\u30eb\u306e\u30ed\u30fc\u30c9
  blur = loadShader("blur1.glsl"); 
  
  String[] cams = Capture.list();
//    /* \u30ab\u30e1\u30e9\u306e\u30ad\u30e3\u30d7\u30c1\u30e3\u30fc */
  // BLUR\u51e6\u7406\u3092\u304b\u3051\u308b\u30ad\u30e3\u30d7\u30c1\u30e3
  // HD Pro Webcam C920 or FaceTime HD \u30ab\u30e1\u30e9\uff08\u5185\u8535\uff09
  // note: \u7570\u306a\u308bwebcam\u3092\u8907\u6570\u4f7f\u7528\u3067\u304d\u306a\u3044\u554f\u984c\u3042\u308a(2014/11/10) 
  cam_base = new Capture(this, 1024, 768, "HD Pro Webcam C920", 30);
  // \u30d5\u30a9\u30fc\u30ab\u30b9\u90e8\u5206\u306e\u30ad\u30e3\u30d7\u30c1\u30e3 
  cam_crop = new Capture(this, width, height, "HD Pro Webcam C920", 30);
  
  // AR\u30de\u30fc\u30ab\u306e\u30a4\u30cb\u30b7\u30e3\u30e9\u30a4\u30ba
  nya=new MultiMarker(this,width,height,"camera_para.dat",NyAR4PsgConfig.CONFIG_PSG);
  nya.addARMarker("Resources/marker16_a50.pat",80);//id=0
  nya.addARMarker("Resources/marker16_b50.pat",80);//id=1

  cam_base.start();
  cam_crop.start();
}

// \u30de\u30fc\u30ab\u306e\u4e2d\u5fc3\u5ea7\u6a19\u3092\u8fd4\u3059\u95a2\u6570
public double markerLocation_x(int i){
  double x_0 = nya.getMarkerVertex2D(i)[0].x;
  double x_2 = nya.getMarkerVertex2D(i)[2].x;  
  return (x_0+x_2)/2;
}

public double markerLocation_y(int i){
  double y_0 = nya.getMarkerVertex2D(i)[0].y;
  double y_2 = nya.getMarkerVertex2D(i)[2].y;
  return (y_0+y_2)/2;
}

public void draw()
{
  if (cam_base.available() !=true) {
    return;
  }
  cam_base.read();
  cam_crop.read();

  nya.detect(cam_base);
  background(0);
  nya.drawBackground(cam_base);//frustum\u3092\u8003\u616e\u3057\u305f\u80cc\u666f\u63cf\u753b

  set(0, 0, cam_base);
  filter(blur);

  for(int i=0;i<2;i++){
    if((!nya.isExistMarker(i))){
      continue;
    }
    nya.beginTransform(i);
    fill(0,100*(i%2),100*((i+1)%2));
    translate(0,0,20);
    box(40);
    nya.endTransform();

  marker_upperLeft_x = (int)markerLocation_x(0);
  marker_upperLeft_y = (int)markerLocation_y(0);
  marker_lowerRight_x = (int)markerLocation_x(1);
  marker_lowerRight_y = (int)markerLocation_y(1);


  }
  int crop_x = marker_upperLeft_x;
  int crop_y = marker_upperLeft_y;
  int crop_width = abs(marker_lowerRight_x - marker_upperLeft_x);
  int crop_height = abs(marker_lowerRight_y - marker_upperLeft_y);
  PImage crop = cam_crop.get(crop_x, crop_y, crop_width, crop_height);
  image(crop,crop_x, crop_y, crop_width, crop_height);
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "EN2" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
