package com.bit.bitcameraservice;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.interfaces.DSAKey;
import java.util.Enumeration;
import java.util.List;

import com.topeet.serialtest.serial;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;






import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;




import android.R.integer;
import android.R.string;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.Image;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateFormat;

@SuppressLint("HandlerLeak")
public class BitCameraService extends Activity {	
	SurfaceView mSurfaceView = null;
	SurfaceHolder mSurfaceHolder = null;
	Camera mCamera = null ; 
	int screenWidth, screenHeight;	
	boolean isPreview = false;        //是否在浏览中
	private String ipname ;
	private TextView TextView;
	private String text;
	private ServerSocket ssVideo = null; 
	private ServerSocket ssText = null;
	private static final int msgKey1 = 1;
	
	private SendVideoThread SendVideoThread = null;
    public static  VideoEncodeH264 VideoEncodeH264 = null;//不知为什么加在这里还加static，不加dos.write(VideoEncode.yuv420sp, 0, 28800);错误
    public ImageButton forward;
	public ImageButton backward;
	public ImageButton left;
	public ImageButton right;
	public WifiManager.MulticastLock lock= null;
	public YuvImage image = null;
	public Size size = null;
	private int isH264 = 0;//设置H264还是JPEG
	EncodeH264 EncodeH264;
	private int width = 640;
	private int height = 480;
	private static final int FRAME_RATE = 17;	
	private int bitrate = 2500000;//玩具里面给的125000
	private byte[] h264 = new byte[width*height*3/2];
	private DatagramSocket dsVideo;
	private int ret;
	private InetAddress address = null;
	private byte[] buf = null;
	DatagramPacket packet =null;
	public boolean ipOK = false;
	private int isTCP = 1; //0为udp，1为tcp
	ServerSocket ssIP = null;
	private DatagramSocket dsText;
	private DatagramPacket dpText;
	int powerPercent;
	Thread T5 ;
	boolean clientOut = true;
	private String angleX; 
	private String angleY;
	serial com3 = new serial();
	float Xval;//云台当前X角度
	float Yval;//云台当前Y角度
	private int[] ReceiveStr = new int[7];//云台发回的命令行
	private OnDataReceiveListener onDataReceiveListener = null; 

     boolean uartOK = false;
     boolean isTouch = false;
     
 	boolean isDehazing=false;
 	public int[] array = new int[width*height*3/8];
	//模拟参数***************************************
	String local_xyz = "X:116.3041100919 Y:39.9634188833 Z:45.21";
	String target_orientation = "0";
	String target_distant = "533.32";
	String target_elevation = "0";
	String target_xyz = "X:116.3126221529 Y:39.9254188211 Z:47.32";
	String state_image = "正常";//图像模块
	String state_distant = "正常";//激光测距模块
	String state_north = "正常";//寻北仪模块
	String state_GPS = "出错";//GPS定位模块
	String state_CPU = "正常";//测控计算机模块
	String state_temp = "40";//温度
	//********************************************
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        seeting();	
		launch();
    }
	//初始化设置**************************************************************************
	private void seeting() {
		
		// 设置全屏
        //getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
     	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);       
        
        TextView = (TextView)findViewById(R.id.textView1);
        forward = (ImageButton)findViewById(R.id.forward);//配置按钮并监听
		//forward.setOnTouchListener(new forward_OnTouchListener());//监听以后就是类似中断了
        
        backward = (ImageButton)findViewById(R.id.backward);
        //backward.setOnTouchListener(new backward_OnTouchListener()); 
        
        left = (ImageButton)findViewById(R.id.left);
        //left.setOnTouchListener(new left_OnTouchListener());
        
        right = (ImageButton)findViewById(R.id.right);
        //right.setOnTouchListener(new right_OnTouchListener()); 
        
//		screenWidth = 640;//这个设置的是照片大小，预览大小下面还有参数
//		screenHeight = 480;		
		mSurfaceView = (SurfaceView) findViewById(R.id.sView);                  // 获取界面中SurfaceView组件		
		mSurfaceHolder = mSurfaceView.getHolder();                               // 获得SurfaceView的SurfaceHolder
		//下面两行是为了某些设配关闭了UDP	，需要时加入lock.acquire()
		WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		lock= manager.createMulticastLock("test wifi");
		
						
		//电池电量声明
        registerReceiver(new BatteryBroadcastReceiver(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); 
        
		// 为surfaceHolder添加一个回调监听器
		mSurfaceHolder.addCallback(new Callback() {
			
			public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {				
			}
			
			public void surfaceCreated(SurfaceHolder holder) {							
				initCamera();							 
			}

			public void surfaceDestroyed(SurfaceHolder holder) {
				int[] stop =  CreateCommandStr(0x00, 0x00, 0x00, 0x00);					
 				com3.Write(stop,stop.length);
				Log.i("mSurfaceHolder",
						"SurfaceHolder.Callback：Surface Destroyed");
				// 如果camera不为null ,释放摄像头
				if (mCamera != null) {
					if (isPreview) {
						mCamera.setPreviewCallback(null); // ！！这个必须在前，不然退出出错
						mCamera.stopPreview();
						mCamera.release();
						mCamera = null;
						if (isH264 == 1) {
							EncodeH264.close();
						}
					}
				}
				System.exit(0);
			}
		});
		// 设置该SurfaceView自己不维护缓冲    
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	} 
	//初始化启动**************************************************************************
	public void launch() {	//小心阻塞
//		//测试UDP显示数据启动线程**************************************************************************
//		ShowTextThread STT = new ShowTextThread();
//        Thread T3 = new Thread(STT);     
//        T3.start();
		
        //启动获取IP线程****************************************************************************
		try {
			ssIP = new ServerSocket(4500);//启动
		} catch (BindException e) {
			AlertDialog("端口使用中","请关掉相关程序并重新运行服务器！",true);
			//System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
        IpThread IT = new IpThread();
        Thread T6 = new Thread(IT);
        T6.start();
        //启动tcp收数据线程****************************************************************************
        CommendReceiveThread CRT = new CommendReceiveThread();
        Thread T7 = new Thread(CRT);
        T7.start();
        //舵机控制UDP启动线程**************************************************************************
        ReceiveControlData RCD = new ReceiveControlData();
        Thread T4 = new Thread(RCD);
        //T4.setPriority(Thread.MIN_PRIORITY);
        //android.os.Process.setThreadPriority(19);//os最低级
        //T4.start();
        //开启串口*********************************************************************************
        com3.Open(3, 9600);
        //开启连续请求查询云台位置线程************************************
        ChkVal CV = new ChkVal();
        Thread T8 = new Thread(CV);
        T8.start();
        //开启接收串口线程并请求查询云台位置*****************************************************
	   	uartOK = false;
	   	new Thread(new ReadUartThread()).start();
        

        //测试启动数据传输线程**************************************************************************
//        SendTextThread ST = new SendTextThread();
//        Thread T2 = new Thread(ST);
//        T2.start();
//		


	}
	//相机启动**************************************************************************
	private void initCamera() {
		int mPreviewHeight;
		int mPreviewWidth;
    	if (!isPreview) {
			mCamera = Camera.open();
		}
		if (mCamera != null && !isPreview) {
			
			try{
				 // 【调试】获取caera支持的PictrueSize，看看能否设置？？
			    List<Size> pictureSizes = mCamera.getParameters().getSupportedPictureSizes();
			    List<Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
			    List<Integer> previewFormats = mCamera.getParameters().getSupportedPreviewFormats();
			    List<Integer> previewFrameRates = mCamera.getParameters().getSupportedPreviewFrameRates();
			    Log.i("parameters"+"initCamera", "cyy support parameters is ");
			    Size psize = null;
			    for (int i = 0; i < pictureSizes.size(); i++)
			    {
			     psize = pictureSizes.get(i);
			     Log.i("parameters"+"initCamera", "PictrueSize,width: " + psize.width + " height" + psize.height);
			    }
			    for (int i = 0; i < previewSizes.size(); i++)
			    {
			     psize = previewSizes.get(i);
			     Log.i("parameters"+"initCamera", "PreviewSize,width: " + psize.width + " height" + psize.height);
			    }
			    Integer pf = null;
			    for (int i = 0; i < previewFormats.size(); i++)
			    {
			     pf = previewFormats.get(i);
			     Log.i("parameters"+"initCamera", "previewformates:" + pf);
			    }
			    // 【调试】设置后的图片大小和预览大小以及帧率
			    Camera.Size csize = mCamera.getParameters().getPreviewSize();
			    mPreviewHeight = csize.height; //
			    mPreviewWidth = csize.width;
			    Log.i("parameters"+"initCamera", "after setting, previewSize:width: " + csize.width + " height: " + csize.height);
			    csize = mCamera.getParameters().getPictureSize();
			    Log.i("parameters"+"initCamera", "after setting, pictruesize:width: " + csize.width + " height: " + csize.height);
			    Log.i("parameters"+"initCamera", "after setting, previewformate is " + mCamera.getParameters().getPreviewFormat());
			    Log.i("parameters"+"initCamera", "after setting, previewframetate is " + mCamera.getParameters().getPreviewFrameRate());	
			    //【调试】结束***************************		
			    Camera.Parameters parameters = mCamera.getParameters();				
				parameters.setPreviewSize(width, height);    // 设置预览照片的大小				
				parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
				parameters.setPreviewFpsRange(20,25);                    // 每秒显示20~30帧，以后可以减小节能				
				if (isH264 == 1) {
					parameters.setPictureFormat(PixelFormat.JPEG);           // 设置图片格式拍照,原来是ImageFormat.NV21				
					parameters.setPreviewFormat(ImageFormat.YV12);//应该改
				}//JPEG就用系统默认
				//parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);	//原来没有，繁体加的										    
				//parameters.setPictureSize(screenWidth, screenHeight);    // 设置照片的大小
				parameters.setPictureSize(width, height);
				parameters.setPreviewSize(width, height); // 指定preview的大小 
				//这两个属性 如果这两个属性设置的和真实手机的不一样时，就会报错
												
				mCamera.setParameters(parameters);						// 有这个才能实现上面的设置
				mCamera.setPreviewDisplay(mSurfaceHolder);                 // 通过SurfaceView显示取景画面						        
		      	//byte[] buf = new byte[screenWidth * screenHeight * 3 / 2]; 
		        buf = new byte[width * height * 3 / 2]; 
		        mCamera.addCallbackBuffer(buf);//增加缓冲区
		        if (isH264 == 1) {
					/* 视频流编码处理 */
					//添加对视频流处理函数
					mCamera.setPreviewCallback(new VideoEncodeH264());         // 设置回调的类	
					//mCamera.setPreviewCallbackWithBuffer(new VideoEncodeH264());//添加对视频的编码处理，setPreviewCallbackWithBuffer是很有必要的，不然每次回调系统都重新分配缓冲区，效率会很低。
				}
		        else {
		        	mCamera.setPreviewCallback(new VideoEncodeJPEG());
		        	//mCamera.setPreviewCallbackWithBuffer(new VideoEncodeJPEG()); //以后试试
		        }
				mCamera.startPreview();                                   // 开始预览				
				mCamera.autoFocus(null);                                  // 自动对焦				
			} catch (Exception e) {
				e.printStackTrace();
			}
			 // 【调试】设置后的图片大小和预览大小以及帧率
		    	  
			isPreview = true;
		}
    }
//获取对方ip连接并发第一次数据**************************************************************************************
	class IpThread implements Runnable{
		public void run() {
			while (true) {
				try {
					Socket s = ssIP.accept();
					clientOut = false;
					//ipname = s.getInetAddress().getHostAddress();//获得客户端IP
					DataInputStream dis = new DataInputStream(s.getInputStream());
					ipname = dis.readUTF();
					//address = InetAddress.getByName(ipname);
					address = InetAddress.getByName(ipname);
					//address = InetAddress.getByName("192.168.31.129");

					ipOK = true;//其他需要这个IP的必须不断获取新的，因为有可能重新连接新IP
					
					Log.i("getipname", "Client ipname = " + ipname);
					
					//客户端连接时需要发送过去的数据**********************因为他首先运行且每次新连接都运行一次，所以发送连接之后所需数据
					new Thread(new TcpSendThread("@bat" + Integer.toString(powerPercent))).start();
					//new Thread(new TcpSendThread(， ，))).start();
					
						//建立图像传输服务器**************************************************************************
				        if (isH264 == 1) {//H264
					        EncodeH264 = new EncodeH264(width,height,FRAME_RATE,bitrate);
					        try {
								dsVideo = new DatagramSocket();
							} catch (SocketException e) {
								e.printStackTrace();
							}
				        }
				        else {//JPEG
							//启用线程将图像数据发送出去
							SendVideoThread RVT = new SendVideoThread();
							T5 = new Thread(RVT);
							
							//T5.setPriority(Thread.MAX_PRIORITY);		
							T5.start();		
				        }
				        
					//******************************************
				} catch (EOFException e) {// 这些为了client那边人为或非人为的关闭时，让这边的socket和DataInputStream关闭
					Log.i("SendTextThread", "A Text client close!");
				} catch (UnknownHostException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				} 
			}
		  
		}
	}
//TCP收指令数据并执行**************************************************************************************	
	class CommendReceiveThread implements Runnable{
		ServerSocket ss;
		int a = -1;

		public void run() {
			try {
				ss = new ServerSocket(5000);
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			while (true) {
				try {
					Socket s = ss.accept();
					DataInputStream dis = new DataInputStream(s.getInputStream());
					String str= dis.readUTF();	
					Log.i("3333333", "touch OK");
					//判断是不是发来的触摸角度
					if((a = str.indexOf("@tX")) >= 0) {//读取标志
						int b =str.indexOf("@tY");
						angleX = str.substring(a + "@tX".length(), b);
						angleY = str.substring(b + "@tY".length(), str.length());							
						Message msg = new Message();
						msg.what = 100;//把传送数据包装成message
						mHandler.sendMessage(msg);	
					}
					else  {
						int x = Integer.parseInt(str);//string转int
						Message msg = new Message();
						msg.what = x;//把传送数据包装成message
						mHandler.sendMessage(msg);	
					}
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}
		private Handler mHandler = new Handler() {
			int[] go;
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);				
				switch (msg.what) {//不能用string类型
				
				case 1://接收状态指令
					new Thread(new TcpSendThread("@si" + state_image + "@sd" + state_distant + "@sn" + state_north + "@sg" + state_GPS + "@sc" + state_CPU + "@st" + state_temp)).start();
					//new Thread(new TcpSendThread("@state" + state_image + state_distant + state_north + state_GPS + state_CPU + "@stemp" + state_temp)).start();
					break;
				case 2://接收主机坐标高程指令
					new Thread(new TcpSendThread("@lxyz" + local_xyz)).start();//“lxyz”是他判断标志
					Log.i("4444444444", "touch OK");
					break;
				case 3://接收目标方位角俯仰角指令
					uartOK =false;
					new Thread(new ReadUartThread()).start();
					new Thread(new TcpSendThread("@to" + target_orientation + "@te" + target_elevation)).start();
					break;
				case 4://接收目标距离指令
					new Thread(new TcpSendThread("@td" + target_distant)).start();
					break;
				case 5://接收目标坐标高程指令
					new Thread(new TcpSendThread("@txyz" + target_xyz)).start();
					break;
				case 6://发送到总指挥指令
					new Thread(new TcpSendThread("@send" + "发送成功")).start();
					break;
				case 7://接收去雾指令
					 isDehazing=true;
					break;
				case 8://接收停止去雾指令
					isDehazing=false;
					break;
				case 9://接收焦距拉近
					go =  CreateCommandStr(0x00, 0x20, 0x00, 0x00);					
	 				com3.Write(go,go.length);
					break;
				case 10://接收焦距远离
					go =  CreateCommandStr(0x00, 0x40, 0x00, 0x00);					
	 				com3.Write(go,go.length);
					break;
				case 11:
					go =  CreateCommandStr(0x00, 0x0C, 0x3F, 0x3F);					
	 				com3.Write(go,go.length);	
					break;	
				case 12:
					go =  CreateCommandStr(0x00, 0x0C, 0x1E, 0x3F);					
	 				com3.Write(go,go.length);	
					break;	
				case 13://上全速
					go =  CreateCommandStr(0x00, 0x08, 0x00, 0x3F);					
	 				com3.Write(go,go.length);	
					break;
				case 14:
					go =  CreateCommandStr(0x00, 0x0A, 0x1E, 0x3F);					
	 				com3.Write(go,go.length);	
					break;	
				case 15:
					go =  CreateCommandStr(0x00, 0x0A, 0x3F, 0x3F);					
	 				com3.Write(go,go.length);	
					break;	
				case 21:
					go =  CreateCommandStr(0x00, 0x0C, 0x3F, 0x1E);					
	 				com3.Write(go,go.length);	
					break;	
				case 22:
					go =  CreateCommandStr(0x00, 0x0C, 0x1E, 0x1E);					
	 				com3.Write(go,go.length);	
					break;
				case 23:
					go =  CreateCommandStr(0x00, 0x08, 0x00, 0x1E);					
	 				com3.Write(go,go.length);	
					break;	
				case 24:
					go =  CreateCommandStr(0x00, 0x0A, 0x1E, 0x1E);					
	 				com3.Write(go,go.length);	
					break;	
				case 25:
					go =  CreateCommandStr(0x00, 0x0A, 0x3F, 0x1E);					
	 				com3.Write(go,go.length);	
					break;	
				case 31:
					go =  CreateCommandStr(0x00, 0x04, 0x3F, 0x00);					
	 				com3.Write(go,go.length);
					break;	
				case 32:
					go =  CreateCommandStr(0x00, 0x04, 0x1E, 0x00);					
	 				com3.Write(go,go.length);
					break;		
				case 33://完全停止
					go =  CreateCommandStr(0x00, 0x00, 0x00, 0x00);					
	 				com3.Write(go,go.length);
	 				new Thread(new ReadUartThread()).start();//请求并发送云台位置
					break;	
				case 34:
					go =  CreateCommandStr(0x00, 0x02, 0x1E, 0x00);					
	 				com3.Write(go,go.length);	
					break;	
				case 35://右全速
					go =  CreateCommandStr(0x00, 0x02, 0x3F, 0x00);					
	 				com3.Write(go,go.length);	
					break;	
				case 41:
					go =  CreateCommandStr(0x00, 0x14, 0x3F, 0x1E);					
	 				com3.Write(go,go.length);	
					break;	
				case 42:
					go =  CreateCommandStr(0x00, 0x14, 0x1E, 0x1E);					
	 				com3.Write(go,go.length);	
					break;
				case 43:
					go =  CreateCommandStr(0x00, 0x10, 0x00, 0x1E);					
	 				com3.Write(go,go.length);
					break;	
				case 44:
					go =  CreateCommandStr(0x00, 0x12, 0x1E, 0x1E);					
	 				com3.Write(go,go.length);	
					break;	
				case 45:
					go =  CreateCommandStr(0x00, 0x04, 0x3F, 0x1E);					
	 				com3.Write(go,go.length);	
					break;	
				case 51:
					go =  CreateCommandStr(0x00, 0x14, 0x3F, 0x3F);					
	 				com3.Write(go,go.length);	
					break;	
				case 52:
					go =  CreateCommandStr(0x00, 0x14, 0x1E, 0x3F);					
	 				com3.Write(go,go.length);	
					break;	
				case 53://下全速
					go =  CreateCommandStr(0x00, 0x10, 0x00, 0x3F);					
	 				com3.Write(go,go.length);
					break;	
				case 54:
					go =  CreateCommandStr(0x00, 0x12, 0x1E, 0x3F);					
	 				com3.Write(go,go.length);	
					break;	
				case 55:
					go =  CreateCommandStr(0x00, 0x12, 0x3F, 0x3F);					
	 				com3.Write(go,go.length);	
					break;	

				case 60:
					go =  CreateCommandStr(0x00, 0x00, 0x00, 0x00);					
	 				com3.Write(go,go.length);		
	 				uartOK = false;
	 				new Thread(new ReadUartThread()).start();//请求并发送云台位置
					forward.setImageResource(R.drawable.forward1);
					backward.setImageResource(R.drawable.backward1);
					left.setImageResource(R.drawable.left1);
					right.setImageResource(R.drawable.right1);
					break;
				case 61:
					go =  CreateCommandStr(0x00, 0x08, 0x00, 0x1E);					
	 				com3.Write(go,go.length);		
	 				//new Thread(new ReadUartThread()).start();
					forward.setImageResource(R.drawable.forward2);
					break;
				case 62:
					go =  CreateCommandStr(0x00, 0x10, 0x00, 0x1E);					
	 				com3.Write(go,go.length);	
					backward.setImageResource(R.drawable.backward2);
					break;
				case 63:
					go =  CreateCommandStr(0x00, 0x04, 0x1E, 0x00);					
	 				com3.Write(go,go.length);	
					left.setImageResource(R.drawable.left2);
					break;
				case 64:
					go =  CreateCommandStr(0x00, 0x02, 0x1E, 0x00);					
	 				com3.Write(go,go.length);	
					right.setImageResource(R.drawable.right2);
					break;
				case 100://接收快速追踪
				   	uartOK = false;
				   	isTouch = true;
				    new Thread(new ReadUartThread()).start();
					
					break;
				default:
					break;
				}
			}
		};

	}
//TCP发数据*********************************************************************
	class TcpSendThread implements Runnable{
		String str;
		int port = 5500;
		long time = 0;
		public TcpSendThread(String str) {
			this.str = str;
		}
		public TcpSendThread(String str, int port) {
			this.str = str;
			this.port = port;
		}
		public TcpSendThread(String str, int port,long time ) {
			this.str = str;
			this.port = port;
			this.time = time;
		}

		public void run() {//发送指令new Thread(new TcpSendThread("1")).start();
		
			while (!ipOK) {/* 等待获得IP */ };
			if (time == 0) {//不带循环的
				try {
					Socket s = new Socket(ipname, port);
					DataOutputStream dos = new DataOutputStream(s.getOutputStream());
					dos.writeUTF(str);// 写一个UNICODE类型的字符串
					dos.flush();
					dos.close();
					s.close();//每次发送完断开，不影响下次发送
					
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
			if(time != 0) {//带循环的
				while(true) {
					try {
						Socket s = new Socket(ipname, port);
						DataOutputStream dos = new DataOutputStream(s.getOutputStream());
						
						dos.writeUTF(str);// 写一个UNICODE类型的字符串
						dos.flush();
						dos.close();
						s.close();//每次发送完断开，不影响下次发送
						Thread.sleep(time);
					} catch (IOException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
				}
			}
		}
	}
//视频编码H264**************************************************************************
	class VideoEncodeH264 implements Camera.PreviewCallback {// 用回调可以自动根据数据变化循环执行
		// public byte[] yuv420sp = null;
		public void onPreviewFrame(byte[] data, Camera camera) {// 视频数据自动放入data中
			// Log.v("h264", "h264 start");
			ret = EncodeH264.offerEncoder(data, h264);
			// mCamera.addCallbackBuffer(buf);
			if (ret > 0)// 检测是否跳过头pps sps，跳过了再传
			{
				while (!ipOK) {/* 等待获得IP */ };//！！！！！！！！这里有问题
				packet = new DatagramPacket(h264, ret, address, 6500);

				SendVideoThread RVT = new SendVideoThread();
				Thread T1 = new Thread(RVT);
				android.os.Process.setThreadPriority(-20);//os最高级
				T1.setPriority(Thread.MAX_PRIORITY);
				T1.start();//***在onPreviewFrame下的是不断执行的，看看这里有没有问题，在这里试试加延时或者检测发送完了在执行，加个缓存更好
//				try {
//					Thread.sleep(5);不能加Thread.sleep(5);这个让所有线程延迟了。
//				} catch (InterruptedException e) {
//					// TODO 自动生成的 catch 块
//					e.printStackTrace();
//				}
			}
			// Log.v("h264", "h264 end"+ ",ret = " + ret);
			// Log.v("h264", "h264 end"+ ", data =" + data);
		}

	}
//视频编码JPEG**************************************************************************
	class VideoEncodeJPEG implements Camera.PreviewCallback {
		
		
	    public void onPreviewFrame(byte[] data, Camera camera) {//视频数据自动放入data中
	    	 size = camera.getParameters().getPreviewSize();      
	    	
	    	 if(!clientOut) {
	        	//调用image.compressToJpeg（）将YUV格式图像数据data转为jpg格式
	            try {
	            	//Log.i("00000", "video");
	            	image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
	            	//Log.i("gaozw", "Welcome dehazing thread");
    	          if (isDehazing==true) {
                	
                	//Log.i("gaozw", "Welcome dehazing thread");
                	array=convert(data);   //将截获的YUV byte型转换为int，等待去雾处理
    	         }
	            }catch(Exception ex){  
	                Log.e("Sys","Error:"+ex.getMessage());  
	            }   
	    	 }	            
	    }
	}
	//去雾的相关函数************************************************************************
		/*
		 * 去雾函数 DehazingImg
		 */
		
		  public Bitmap DehazingImg(Bitmap bitmap,float f){


		        int w = bitmap.getWidth(), h = bitmap.getHeight();
		        int[] pix = new int[w * h];
		        bitmap.getPixels(pix, 0, w, 0, 0, w, h);	       
		        int[] resultInt = Dehazing.OutPut(pix,f, w, h, 0);	  
		        Bitmap result = Bitmap.createBitmap(resultInt,0, w,w,h, Bitmap.Config.ARGB_8888);
		        return result;
		    }

			     /***
			     * 图片的缩放方法
			     *
			     * @param bgimage
			     *            ：源图片资源
			     * @param newWidth
			     *            ：缩放后宽度
			     * @param newHeight
			     *            ：缩放后高度
			     * @return
			     */
			    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
			                                   double newHeight) {
			        // 获取这个图片的宽和高
			        float width = bgimage.getWidth();
			        float height = bgimage.getHeight();
			        // 创建操作图片用的matrix对象
			        Matrix matrix = new Matrix();
			        // 计算宽高缩放率
			        float scaleWidth = ((float) newWidth) / width;
			        float scaleHeight = ((float) newHeight) / height;
			        // 缩放图片动作
			        matrix.postScale(scaleWidth, scaleHeight);
			        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
			                (int) height, matrix, true);
			        return bitmap;
			    }
			    Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {

			        Matrix m = new Matrix();
			        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);

			        try {
			            Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);

			            return bm1;

			        } catch (OutOfMemoryError ex) {
			        }
			        return null;

			    }
			    public int[] convert(byte buf[])
			    {
			        //     int intArr[] = new int[640*480];
			                 int intArr[] = new int[buf.length / 4];
			        int offset=0 ;

			        for(int i = 0; i < intArr.length; i++) {
			            intArr[i] = (buf[0 + offset] & 0xFF) | ((buf[1 + offset] & 0xFF) << 8) |
			                    ((buf[2 + offset] & 0xFF) << 16) | ((buf[3 + offset] & 0xFF) << 24);
			            offset += 4;
			        }
			        return intArr;
			    }
			    public byte[] convertInt2Byte(int buf[])
			    {
			       
			                 byte byteArr[] = new byte[buf.length * 4];
			        int offset=0 ;

			        for(int i = 0; i < buf.length; i++) {
			        	byteArr[0 + offset]=(byte) ((buf[i]& 0xFF000000)>>24);  //取高四位
			        	byteArr[1 + offset]=(byte) ((buf[i]& 0x00FF0000)>>16);
			        	byteArr[2 + offset]=(byte) ((buf[i]& 0x0000FF00)>>8);
			        	byteArr[3 + offset]=(byte) ((buf[i]& 0x000000FF));		        					        			      
			            offset += 4;
			        }
			        return byteArr;
			    }	
//视频传输操作**************************************************************************	
	class SendVideoThread implements Runnable{
		Socket s = null;
		private byte byteBuffer[] = new byte[1472];//原来是1024
		int portRemoteNum=7500;//远程端口
		int portLocalNum=7000;//本机端口
		//DataOutputStream dos = null;				
		public void run() {			
			android.os.Process.setThreadPriority(-20);//os最高级
			if (isH264 == 1) {//H264传送				
				try {
					dsVideo.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
				//Log.v("h264", "h264 send");					
			}
			else {//JPEG传送

				while (true) {//判断是否被停止线程!T5.isInterrupted()
					if(image!=null){
						try {
							//ipname = "192.168.23.1";
							//Socket s = new Socket("192.168.31.129", 6000);
							
							
							while (!ipOK) {/* 等待获得IP */ };	
							if(isTCP == 1) {
								s = new Socket(ipname, 6000);						
								//s = ssVideo.accept();
								//Log.i("SendVideoThread", "A Video client connected!");
			/*gaozw添加去雾处理后发送数据*/
								
								if(isDehazing ==true && array!=null){
						
									
									ByteArrayOutputStream stream = new ByteArrayOutputStream();
								
						
									Log.i("dehazing processing", "dehazing begins");
									int[] buf=Dehazing.OutPut(array, 0.75f, width, height, 1);   //去雾函数运行一段时间，退出
									byte[]  byte_YUV=convertInt2Byte(buf);                    //去雾函数输出byte型，转换为int
									YuvImage  image_dehazed = new YuvImage(byte_YUV, ImageFormat.NV21, width,height, null);
						//			 image.compressToJpeg(new Rect(0, 0, size.width, size.height), 60, stream); 
									Log.i("dehazing processing", "dehazing ends");
			                   
			                        image_dehazed.compressToJpeg(new Rect(0, 0, size.width, size.height), 60, stream);
				              
					                OutputStream outsocketDehazing = s.getOutputStream();
									ByteArrayInputStream inputstreamDehazing = new ByteArrayInputStream(stream.toByteArray());
									
									int amount;
									//Log.v("JPEG", "amount = " + amount);
									//下面循环是不断检测存入byteBuffer的有没有结束，没结束就发送，结束了退出循环，所以byteBuffer大小决定了发送包的大小
									while ((amount = inputstreamDehazing.read(byteBuffer)) != -1) {//read()返回值为-1表示是读到文件的最后了，read出的数是字节数
									    outsocketDehazing.write(byteBuffer, 0, amount);//一张图是一个流
									    //Log.v("JPEG", "amount2 = " + amount);
									}
									//Log.v("JPEG", "byterBuffer" + byteBuffer.length);
									stream.flush();							
									stream.close();
									s.close();
								
								}
								else {
									ByteArrayOutputStream bos = new ByteArrayOutputStream();
									//Log.i("22222", "video");
									image.compressToJpeg(new Rect(0, 0, size.width, size.height), 60, bos); 
									//Log.i("33333", "video");
									//bos.flush(); 																										
									OutputStream outsocket = s.getOutputStream();
									ByteArrayInputStream inputstream = new ByteArrayInputStream(bos.toByteArray());
									int amount;
									//Log.v("JPEG", "amount = " + amount);
									//下面循环是不断检测存入byteBuffer的有没有结束，没结束就发送，结束了退出循环，所以byteBuffer大小决定了发送包的大小
									while ((amount = inputstream.read(byteBuffer)) != -1) {//read()返回值为-1表示是读到文件的最后了，read出的数是字节数
									    outsocket.write(byteBuffer, 0, amount);//一张图是一个流
									    //Log.i("44444", "video");
									    //Log.v("JPEG", "amount2 = " + amount);
									}
									//Log.v("JPEG", "byterBuffer" + byteBuffer.length);
									bos.flush();							
									bos.close();
									s.close();
								}
							}
							if(isTCP == 0) {
//								
//						        DatagramSocket dsJPEG = new DatagramSocket(portLocalNum);//建立本机UDP服务器插口并设置本机端口
//						        		    
//								byte data [] = str.getBytes();
//								ByteArrayOutputStream bos = new ByteArrayOutputStream();
//								image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, bos); 
//								
//								ByteArrayInputStream inputstream = new ByteArrayInputStream(bos.toByteArray());
//								int amount;
//								//Log.v("JPEG", "amount = " + amount);
//								//下面循环是不断检测存入byteBuffer的有没有结束，没结束就发送，结束了退出循环，所以byteBuffer大小决定了发送包的大小
//								while ((amount = inputstream.read(byteBuffer)) != -1) {//read()返回值为-1表示是读到文件的最后了，read出的数是字节数
//								    outsocket.write(byteBuffer, 0, amount);//一张图是一个流
//								    //Log.v("JPEG", "amount2 = " + amount);
//								}
//								//Log.v("JPEG", "byterBuffer" + byteBuffer.length);
//								bos.flush();	
							}
						} catch (IOException e) {
							// TODO 自动生成的 catch 块
							e.printStackTrace();
							Log.i("errrrrrrrrrrrrr", "A client close!");
							clientOut = true;
							//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!加入断开后舵机不动
							int[] stop =  CreateCommandStr(0x00, 0x00, 0x00, 0x00);					
			 				com3.Write(stop,stop.length);
			 				isDehazing = false;
							//T5.interrupt();//停止线程，或用break
							break;
							
						} 			        
					}
				}
			}
		}
	}


//舵机控制信号UDP接收操作**************************************************************************
	class ReceiveControlData implements Runnable {
		int portLocalNum = 9000;//本机端口
		DatagramSocket ds = null;
		DatagramPacket dp = null;
		
		public void run() {
			byte data[] = new byte [1024];		
			try {
				dp = new DatagramPacket(data,data.length);
				ds = new DatagramSocket(portLocalNum);//建立本机UDP服务器插口并设置本机端口
			} catch (SocketException e) {
				e.printStackTrace();
			}
			while(true) {				
				try {
					//lock.acquire();
					ds.receive(dp);		
					//lock.release();
					String str = new String(data,0,dp.getLength());	//取出传送代码为string
					int s = Integer.parseInt(str);//string转int
					Message msg = new Message();
					msg.what = s;//把传送数据包装成message
					mHandler.sendMessage(msg);					
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}				
			}
		}
		private Handler mHandler = new Handler() {
			int[] stop;
			int[] goup;
			int[] godown;
			int[] goleft;
			int[] goright;
			int[] xStop;
			int[] yStop;
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);				
				switch (msg.what) {//不能用string类型				
				case 11://左全速
					goleft =  CreateCommandStr(0x00, 0x04, 0x3F, 0x00);					
	 				com3.Write(goleft,goleft.length);	
					break;	
				case 12:
					goleft =  CreateCommandStr(0x00, 0x04, 0x1E, 0x00);					
	 				com3.Write(goleft,goleft.length);
					break;	
				case 13:
					goleft =  CreateCommandStr(0x00, 0x04, 0x0A, 0x00);					
	 				com3.Write(goleft,goleft.length);	
					break;	
				case 14://水平停止
					xStop =  CreateCommandStr(0x00, 0x04, 0x00, 0x00);					
	 				com3.Write(xStop,xStop.length);	
					break;	
				case 15:
					goright =  CreateCommandStr(0x00, 0x02, 0x0A, 0x00);					
	 				com3.Write(goright,goright.length);	
					break;	
				case 16:
					goright =  CreateCommandStr(0x00, 0x02, 0x1E, 0x00);					
	 				com3.Write(goright,goright.length);	
					break;	
				case 17://右全速
					goright =  CreateCommandStr(0x00, 0x02, 0x3F, 0x00);					
	 				com3.Write(goright,goright.length);	
					break;	
				case 21://下全速
					godown =  CreateCommandStr(0x00, 0x10, 0x00, 0x3F);					
	 				com3.Write(godown,godown.length);
					break;	
				case 22:
					godown =  CreateCommandStr(0x00, 0x10, 0x00, 0x1E);					
	 				com3.Write(godown,godown.length);
					break;	

				case 23:
					godown =  CreateCommandStr(0x00, 0x10, 0x00, 0x0A);					
	 				com3.Write(godown,godown.length);
					break;	
				case 24://垂直停止
					yStop =  CreateCommandStr(0x00, 0x08, 0x00, 0x00);					
	 				com3.Write(yStop,yStop.length);	
					break;	
				case 25:
					goup =  CreateCommandStr(0x00, 0x08, 0x00, 0x0A);					
	 				com3.Write(goup,goup.length);	
					break;	
				case 26:
					goup =  CreateCommandStr(0x00, 0x08, 0x00, 0x1E);					
	 				com3.Write(goup,goup.length);	
					break;	
				case 27://上全速
					goup =  CreateCommandStr(0x00, 0x08, 0x00, 0x3F);					
	 				com3.Write(goup,goup.length);	
					break;	

				case 0:
					stop =  CreateCommandStr(0x00, 0x00, 0x00, 0x00);					
	 				com3.Write(stop,stop.length);		
 				
					forward.setImageResource(R.drawable.forward1);
					backward.setImageResource(R.drawable.backward1);
					left.setImageResource(R.drawable.left1);
					right.setImageResource(R.drawable.right1);
					break;
				case 1:
					goup =  CreateCommandStr(0x00, 0x08, 0x00, 0x1E);					
	 				com3.Write(goup,goup.length);		
					forward.setImageResource(R.drawable.forward2);
					break;
				case 2:
					godown =  CreateCommandStr(0x00, 0x10, 0x00, 0x1E);					
	 				com3.Write(godown,godown.length);	
					backward.setImageResource(R.drawable.backward2);
					break;
				case 3:
					goleft =  CreateCommandStr(0x00, 0x04, 0x1E, 0x00);					
	 				com3.Write(goleft,goleft.length);	
					left.setImageResource(R.drawable.left2);
					break;
				case 4:
					goright =  CreateCommandStr(0x00, 0x02, 0x1E, 0x00);					
	 				com3.Write(goright,goright.length);	
					right.setImageResource(R.drawable.right2);
					break;
				default:
					break;
				}
			}
		};
	}

//串口请求位置+接收位置+计算并发送云台转角线程*****************************************************************
	/* 当有快速瞄准信号isTouch时,为查询接收云台位置并控制云台转动；平时为查询接收并传送云台位置*/
	class ReadUartThread implements Runnable{
		
		int[] RX =null ;
		//int[] RX = null;
		boolean xOK = false;
		boolean yOK = false;
		public void run() {
			android.os.Process.setThreadPriority(-19);
			Message msg = new Message();
			while(uartOK == false) {

				while ((RX = com3.Read()) != null) {
					//RX = RX;//需要利用缓存，因为线程刷的太快，等有反应就刷过去了。	
					if (RX[6]==((RX[1]+RX[2]+RX[3]+RX[4]+RX[5]) & 0xff)) {//RX_buff[6]==((RX_buff[1]+RX_buff[2]+RX_buff[3]+RX_buff[4]+RX_buff[5]) & 0xff
						if(RX[3]==0x59) {//传来水平角度	
							int Xval_buf = 0;
							Xval_buf = ((RX[4] << 8)) + RX[5];
							if ((Xval_buf <=36000) && (Xval_buf >=0 )){
								Xval = (float) Xval_buf/100;
								
								RX = null;
							}
							//Xval = (((float)(RX_buff[4] << 8)) + (float)RX_buff[5])/100;
							target_orientation =  String.valueOf(Xval);//直接把云台角度当成方位角了，实际不是
							xOK = true;
	//							msg.what = 1;
	//							mHandler.sendMessage(msg);							
						}
						else if(RX[3]==0x5B) {//传来垂直角度
							int Yval_buf;
							Yval_buf = ((RX[4] << 8)) + RX[5];
							if ((Yval_buf < 7600)&&(Yval_buf >=0)){
								Yval = (float)(-1 * Yval_buf)/100;
								
								RX = null;
							}
							else if((Yval_buf<36000)&&(Yval_buf>=32000)){
								Yval = (float)(36000 - Yval_buf)/100;
								
								RX = null;
							}
							target_elevation =  String.valueOf(Yval);//直接把云台角度当成方位角了，实际不是
							yOK = true;
	//							msg.what = 2;
	//							mHandler.sendMessage(msg);
						}	
						if ((xOK == true) && (yOK == true)) {
							uartOK =true;
							if(isTouch){
								msg.what = 1;
								mHandler.sendMessage(msg);
								isTouch = false;
							}
							new Thread(new TcpSendThread("@to" + target_orientation + "@te" + target_elevation)).start();
							break;
						}					
						
					}		
					//UartFlag --;
				}
			}
				
			
		}
		private Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);				
				switch (msg.what) {//不能用string类型			
				case 1://接收状态指令	
					float fy_buff = 0;
					float fx_buff = 0;
					float fx = 0;
					float fy = 0;

					
					fx_buff=(Float.parseFloat(angleX) + Xval)*100;//把需要便宜的角度加上云台现在角度，得需要转到的角度
					//x轴云台目标角度转为0到36000之间
					if (fx_buff >=0 && fx_buff <36000) {
						fx = fx_buff;
					}
					else if (fx_buff >=36000) {
						fx = fx_buff - 36000;
					}
					else {
						fx = 36000 + fx_buff;
					}
					
					fy_buff = (Float.parseFloat(angleY) + Yval)*100;
					if (fy_buff <= 0) {
						fy = -1 * fy_buff;
					}
					else {
						fy = 36000 - fy_buff;
					}
					
					int ix = (int)fx;
					int iy = (int)fy;
					byte xhigh,xlow,yhigh,ylow;
					xlow =(byte)(ix & 0xff);
					xhigh = (byte)(ix >> 8);
					ylow =(byte)(iy & 0xff);
					yhigh = (byte)(iy >> 8);
						
					int[] touchAngleX =  CreateCommandStr(0x00, 0x4B, xhigh, xlow);
					int[] touchAngleY =  CreateCommandStr(0x00, 0x4D, yhigh, ylow);
					
	 				com3.Write(touchAngleX,touchAngleX.length);		
	 				com3.Write(touchAngleY,touchAngleY.length);

					Toast.makeText(getApplicationContext(),  " X角："+ angleX + " Y角："+ angleY +" X角to："+ fx%100 + " Y角to："+ fy_buff%100 ,Toast.LENGTH_SHORT).show();
					break;
				case 2:
					
					break;
				default:
					break;
				}
			}
		};
	}
//连续查询云台位置*********************************************************************
	class ChkVal implements Runnable {	
		public void run() {
			while (true) {
				int[] text1 =  new int[]{0xFF,0x01,0x00,0x51,0x00,0x00,0x52};//发送查询云台水平角
 				com3.Write(text1, text1.length);
 				int[] text2 =  new int[]{0xFF,0x01,0x00,0x53,0x00,0x00,0x54};//发送查询云台垂直角
 				com3.Write(text2, text2.length);
 				
// 				Message msg = new Message();
// 				msg.what = 1;
//				mHandler.sendMessage(msg);
 				
 				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}			
		}
		private Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);				
				switch (msg.what) {//不能用string类型			
				case 1://接收状态指令	
					uartOK = false;
	 				new Thread(new ReadUartThread()).start();
					break;
				case 2:
					
					break;
				default:
					break;
				}
			}
		};
	}
	//消息提示框**************************************************************************
	public void AlertDialog (String Title,String Message,final boolean isExit) {	
		new android.app.AlertDialog.Builder(BitCameraService.this)  		
		                .setTitle(Title)		
		                .setMessage(Message)		
		                .setPositiveButton("确定",new OnClickListener()
		    			{	    			//点确定后退出	
		    				public void onClick(DialogInterface dialog, int which)
		    				{
		    					if (isExit == true) {
		    					System.exit(1);
		    					}
		    				}
		    			})		
		                .show();
	
	}
//获取本机电池电量****************************************************
	/**接受电量改变广播*/  
    class BatteryBroadcastReceiver extends BroadcastReceiver{  
          
        @Override  
        public void onReceive(Context context, Intent intent) {  
              
            if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){  //有变化则执行
                  
                int level = intent.getIntExtra("level", 0);  
                int scale = intent.getIntExtra("scale", 100);  
                powerPercent = level*100 /scale;
                
                new Thread(new TcpSendThread("@bat" + Integer.toString(powerPercent))).start();//发送
                //textView3.setText(" 本机电量" + powerPercent + "% ");
//	                int curPower = (level * 100 / scale)/25;  
//	                switch (curPower) {  
//	                case 0:  
//	                    tvBatteryChanged.setImageBitmap(BitmapFactory.decodeResource(Main.this.getResources(), R.drawable.power0));  
//	                    break;  
//	                case 1:  
//	                    tvBatteryChanged.setImageBitmap(BitmapFactory.decodeResource(Main.this.getResources(), R.drawable.power1));  
//	                    break;  
//	                case 2:  
//	                    tvBatteryChanged.setImageBitmap(BitmapFactory.decodeResource(Main.this.getResources(), R.drawable.power2));  
//	                    break;  
//	                case 3:  
//	                    tvBatteryChanged.setImageBitmap(BitmapFactory.decodeResource(Main.this.getResources(), R.drawable.power3));  
//	                    break;  
//	                case 4:  
//	                    tvBatteryChanged.setImageBitmap(BitmapFactory.decodeResource(Main.this.getResources(), R.drawable.power4));  
//	                    break;  
//	                }  
            }  
        }  
    }  
   

	    
  //测试UDP数据传输操作**************************************************************************
class SendTextThread implements Runnable {
	int portRemoteNum = 5500;//远程端口
	int portLocalNum = 5000;//本机端口
	
	//Socket s = null;
	//DataOutputStream dos = null;
	//boolean started = false;
	//String str = null;
	//boolean bConnected = false;

	public void run() {
		while (true) {
			android.os.Process.setThreadPriority(19);//os最低级
			while (!ipOK) {/* 等待获得IP */ };				
			try {
				dsText  = new DatagramSocket(portLocalNum);//建立本机UDP服务器插口并设置本机端口
				byte buf [] = text.getBytes(); 		    
				dpText = new DatagramPacket(buf,buf.length,address,portRemoteNum);	//把要传的数据打包并配置输出IP和端口
				dsText.send(dpText);
				dsText.close();
				Thread.sleep(1000);
				
			} catch (SocketException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}catch (InterruptedException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	}

}
//测试UDP数据显示**************************************************************************
class ShowTextThread implements Runnable {

	public void run() {
		android.os.Process.setThreadPriority(19);//os最低级
		do {
			try {				
				Message msg = new Message();
				msg.what = msgKey1;
				mHandler.sendMessage(msg);
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (true);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);				
			switch (msg.what) {
			case msgKey1:
				long sysTime = System.currentTimeMillis();
				CharSequence sysTimeStr = DateFormat.format("hh:mm:ss", sysTime);
				TextView.setText("Time:" + sysTimeStr);
				text = TextView.getText().toString();
				break;

			default:
				break;
			}
		}
	};
}	 

//云台控制指令转换******************************************
public int[] CreateCommandStr(int comand1,int comand2,int data1,int data2) {
	int[] str = new int[] {0xFF,0x01,comand1,comand2,data1,data2,((0x01+comand1+comand2+data1+data2)& 0xff)};
	Log.i("CreateCommandStr", "SendCommandStr = " + str);
	return str;
}
//串口引用库*************************************
static {
    System.loadLibrary("serialtest");
}
public interface OnDataReceiveListener {  
    public void onDataReceive(byte[] buffer, int size);  
}  

public void setOnDataReceiveListener(  
        OnDataReceiveListener dataReceiveListener) {  
    onDataReceiveListener = dataReceiveListener;  
}  
}




