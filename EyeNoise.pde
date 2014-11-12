import processing.video.*;
import jp.nyatla.nyar4psg.*;

Capture cam_base, cam_crop;
PImage crop;
MultiMarker nya;

//Blur処理の高速化のためにPShaderの導入を検討(2014/11/11)
PShader blur;



// マーカ座標(x, y)
public int marker_upperLeft_x;
public int marker_upperLeft_y;
public int marker_lowerRight_x;
public int marker_lowerRight_y;  

public static final int markerNum = 2;

void setup() {
  size(1024,768,P3D);
  colorMode(RGB, 100);
  println(MultiMarker.VERSION);  

//PShaderによるBlurフィルタファイルのロード
  blur = loadShader("blur1.glsl"); 
  
  String[] cams = Capture.list();
//    /* カメラのキャプチャー */
  // BLUR処理をかけるキャプチャ
  // HD Pro Webcam C920 or FaceTime HD カメラ（内蔵）
  // note: 異なるwebcamを複数使用できない問題あり(2014/11/10) 
  cam_base = new Capture(this, 1024, 768, "HD Pro Webcam C920", 30);
  // フォーカス部分のキャプチャ 
  cam_crop = new Capture(this, width, height, "HD Pro Webcam C920", 30);
  
  // ARマーカのイニシャライズ
  nya=new MultiMarker(this,width,height,"camera_para.dat",NyAR4PsgConfig.CONFIG_PSG);
  nya.addARMarker("Resources/marker16_a50.pat",80);//id=0
  nya.addARMarker("Resources/marker16_b50.pat",80);//id=1

  cam_base.start();
  cam_crop.start();
}

// マーカの中心座標を返す関数
double markerLocation_x(int i){
  double x_0 = nya.getMarkerVertex2D(i)[0].x;
  double x_2 = nya.getMarkerVertex2D(i)[2].x;  
  return (x_0+x_2)/2;
}

double markerLocation_y(int i){
  double y_0 = nya.getMarkerVertex2D(i)[0].y;
  double y_2 = nya.getMarkerVertex2D(i)[2].y;
  return (y_0+y_2)/2;
}

void draw()
{
  if (cam_base.available() !=true) {
    return;
  }
  cam_base.read();
  cam_crop.read();

  nya.detect(cam_base);
  background(0);
  nya.drawBackground(cam_base);//frustumを考慮した背景描画

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

