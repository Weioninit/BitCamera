package com.bit.bitcameraclient;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Timer;

import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import org.apache.http.conn.util.InetAddressUtils;
import com.topeet.adctest.*;


import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaFormat;
//import javax.imageio.*;
import com.bit.bitcameraclient.R;









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
import android.content.DialogInterface.OnClickListener;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.Image;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateFormat;
import android.text.format.Formatter;


@SuppressLint({ "HandlerLeak", "NewApi" })
public class BitCameraClient extends Activity {
	
	SurfaceView sView;
	SurfaceHolder surfaceHolder;
	private int width = 640;
	private int height = 480;	
	private int FRAME_RATE = 15;
	boolean isPreview = false;        //是否在浏览中
	private String ipname;
	public TextView textView1;
	public TextView textView2;
	public TextView textView_hIP;
	public TextView textView_lbat;
	public TextView textView_lxyz;
	public TextView textView_tote;
	public TextView textView_td;
	public TextView textView_txyz;
	public TextView textView_hbat;
	public TextView textView_rate;
	public TextView textView_wifi;
	private static final int msgKey1 = 1;
	public ImageButton forward;
	public ImageButton backward;
	public ImageButton left;
	public ImageButton right;
	public int portRemoteNum;
	private int portLocalNum;
	public WifiManager.MulticastLock lock= null;
	private DatagramPacket dpCtl;
	private DatagramSocket dsCtl;
	public int isH264 = 0;//设置H264还是JPEG
	private MediaCodec mediaCodec;
	private byte[] h264 = new byte[width*height*3/2];
	private DatagramSocket dsVideo;
	private DatagramPacket dpVideo;
	private DatagramSocket dsText;
	private DatagramPacket dpText;
	Window window;
	private View view;
	String text_Time = null;
	Socket sCtl;
	
	public Button button_lxyz;
	public Button button_tote;
	public Button button_td;
	public Button button_txyz;
	
	public Button button_last;
	public Button button_next;
	public Button button_zoomUp;
	public Button button_zoomDown;
	public int button_flag = 1;
	private ToggleButton button_defog;
	public Button button_state;
	public Button button_set;
	String local_xyz ;
	String target_orientation;
	String target_distant;
	String target_elevation ;
	String target_xyz;
	String host_bat;
	String target_send;
	String state_image;//图像模块
	String state_distant;//激光测距模块
	String state_north;//寻北仪模块
	String state_GPS;//GPS定位模块
	String state_CPU;//测控计算机模块
	String state_temp;//温度
	String state;//状态汇总
	showWifiThread SWT;
	private WifiManager wifiManager;
	boolean realButtonOk_ok;
	boolean realButtonOk_back;
	adc adc = new adc();
	private int[] RfidBuffer=new int[20];
	private int RfidRxCount=0;

	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //getWindow().setFormat(PixelFormat.TRANSLUCENT);
        //设置隐藏导航栏
        window = getWindow();  
        WindowManager.LayoutParams params = window.getAttributes();  
        params.systemUiVisibility =View.SYSTEM_UI_FLAG_FULLSCREEN |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;  
        window.setAttributes(params);  
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
     	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);    	
        setContentView(R.layout.main);
        
       
        
		sView = (SurfaceView) findViewById(R.id.sView);                  // 获取界面中SurfaceView组件		
		surfaceHolder = sView.getHolder();                               // 获得SurfaceView的SurfaceHolder
		
		textView1 = (TextView)findViewById(R.id.textView1);
		textView1 = (TextView)findViewById(R.id.textView2);
		textView_hIP = (TextView)findViewById(R.id.textView_hIP);
		textView_lbat = (TextView)findViewById(R.id.textView_lbat);
		textView_lxyz = (TextView)findViewById(R.id.textView_lxyz);
		textView_tote = (TextView)findViewById(R.id.textView_tote);
		textView_td = (TextView)findViewById(R.id.textView_td);
		textView_txyz = (TextView)findViewById(R.id.textView_txyz);
		textView_hbat = (TextView)findViewById(R.id.textView_hbat);
		textView_rate = (TextView)findViewById(R.id.textView_rate);
		textView_wifi = (TextView)findViewById(R.id.textView_wifi);
		
//		forward = (ImageButton)findViewById(R.id.forward);//配置按钮并监听
//		forward.setOnTouchListener(new forward_OnTouchListener());//监听以后就是类似中断了
//        
//        backward = (ImageButton)findViewById(R.id.backward);
//        backward.setOnTouchListener(new backward_OnTouchListener()); 
//        
//        left = (ImageButton)findViewById(R.id.left);
//        left.setOnTouchListener(new left_OnTouchListener());
//        
//        right = (ImageButton)findViewById(R.id.right);
//        right.setOnTouchListener(new right_OnTouchListener()); 
        
        
        button_lxyz = (Button)findViewById(R.id.button1);
        button_lxyz.setOnTouchListener(new button_lxyz_OnTouchListener());       
        button_tote = (Button)findViewById(R.id.button2);
        button_tote.setOnTouchListener(new button_tote_OnTouchListener()); 
        button_td = (Button)findViewById(R.id.button3);
        button_td.setOnTouchListener(new button_td_OnTouchListener()); 
        button_txyz = (Button)findViewById(R.id.button4);
        button_txyz.setOnTouchListener(new button_txyz_OnTouchListener()); 
        
        button_last = (Button)findViewById(R.id.button_last);
        button_last.setOnTouchListener(new button_last_OnTouchListener()); 
        button_next = (Button)findViewById(R.id.button_next);
        button_next.setOnTouchListener(new button_next_OnTouchListener()); 
        
        button_state = (Button)findViewById(R.id.button_state);
        button_state.setOnTouchListener(new button_state_OnTouchListener()); 
        button_set = (Button)findViewById(R.id.button_set);
        button_set.setOnTouchListener(new button_set_OnTouchListener()); 
        
        button_defog = (ToggleButton)findViewById(R.id.button_defog);
        button_defog.setOnCheckedChangeListener(new button_defog_OnClickListener());
        
        button_zoomUp = (Button)findViewById(R.id.button_zoomUp);
        button_zoomUp.setOnTouchListener(new button_zoomUp_OnTouchListener());       
        button_zoomDown = (Button)findViewById(R.id.button_zoomDown);
        button_zoomDown.setOnTouchListener(new button_zoomDown_OnTouchListener()); 
        // 获取主机IP地址
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        ipname = data.getString("ipname");
        //textView_hIP.setText(" " + "主机IP:" + ipname + " ");		
        textView_hIP.setText(" " + "主机编号:" + "01" + " ");	
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);  
        //电池电量声明
        registerReceiver(new BatteryBroadcastReceiver(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); 
        
        
        
		//下面两行是为了某些设配关闭了UDP	，需要时加入lock.acquire()
        WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		lock= manager.createMulticastLock("test wifi");
		
		// 为surfaceHolder添加一个回调监听器
		surfaceHolder.addCallback(new Callback() {
			
			public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {				
			}
			
			public void surfaceCreated(SurfaceHolder holder) {							
				launch();   								 
			}
			
			public void surfaceDestroyed(SurfaceHolder holder) {
				
				System.exit(0);
			}		
		});
		// 设置该SurfaceView自己不维护缓冲    
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		
					
    }
    
	public void launch() {
        //启动发送IP线程****************************************************************************
        IpThread IT = new IpThread();
        Thread T6 = new Thread(IT);
        T6.start();
      //启动tcp收数据线程****************************************************************************
        TcpReceiveThread TRT = new TcpReceiveThread();
        Thread T8 = new Thread(TRT);
        T8.start();
       
		//硬解码器初始化***********************************************************
		if (isH264 == 1) {
			mediaCodec = MediaCodec.createDecoderByType("video/avc");
		    MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
		    mediaCodec.configure(mediaFormat, surfaceHolder.getSurface(), null, 0);//使用这个API要最小16
		    mediaCodec.start();
		}
		//建立图像链接建立并启动线程**************************************************************************		
	    //DecodeH264 = new DecodeH264(width,height,FRAME_RATE,bitrate);
		if (isH264 == 1) {
			try {
				dsVideo = new DatagramSocket(6500);
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
	    ReceiveVideoThread RVT = new ReceiveVideoThread();
        Thread T1 = new Thread(RVT);
        
		//T1.setPriority(Thread.MAX_PRIORITY);
        T1.start();
        //测试UDP建立数据接收线程**************************************************************************		
//		ReceiveTextThread RTT = new ReceiveTextThread();
//        Thread T2 = new Thread(RTT);     
//        T2.start();
//        //测试UDP启动数据显示线程**********************************************************************
//        showTextThread STT = new showTextThread();
//        Thread T7 = new Thread(STT);        
//        T7.start();
        //摇杆线程启动*********************************
        joyStickThread JST = new joyStickThread();
        Thread T10 = new Thread(JST);
        //T10.start();
        //启动WIFI流量监控**********************************************************************
        SWT = new showWifiThread();
        new Thread(SWT).start();
       
	}
//发送本地IP***********************************************************
class IpThread implements Runnable{
	public void run() {	
		try {
			Socket s = new Socket(ipname, 4500);
			//Toast.makeText(getApplicationContext(), "连接成功",Toast.LENGTH_LONG).show();
			Log.i("ReceiveVideoThread", "A Video client connected!");
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			
			WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE); //获取本地ip
		    WifiInfo info = wifiManager.getConnectionInfo();
		    String localIpnameString = Formatter.formatIpAddress(info.getIpAddress());
		    
			//String localIpnameString = s.getLocalAddress().toString();//getLocalIpAddress(); //获取本地ip
			Log.i("LocalIp", "Local ipname = " + localIpnameString);
			dos.writeUTF(localIpnameString);// 写一个UNICODE类型的字符串
			dos.flush();
			dos.close();
			s.close();	      
		} catch (UnknownHostException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}
	}
}	
//TCP发指令数据*********************************************************************
	class CommendSendThread implements Runnable{
		String str;
		public CommendSendThread(String str) {
			this.str = str;
		}

		public void run() {//发送指令new Thread(new CommendSendThread("1")).start();
		
				
			try {
				sCtl = new Socket(ipname, 5000);
				DataOutputStream dos = new DataOutputStream(sCtl.getOutputStream());
				dos.writeUTF(str);// 写一个UNICODE类型的字符串
				dos.flush();
				dos.close();
				sCtl.close();//每次发送完断开，不影响下次发送
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
	
		}
	}
//TCP收数据并启动显示**************************************************************************************	
	class TcpReceiveThread implements Runnable{
		ServerSocket ss;
		int a = -1;
		int b = -1;
		int x;
		public void run() {
			try {
				ss = new ServerSocket(5500);
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			while (true) {
				try {
					Socket s = ss.accept();
					DataInputStream dis = new DataInputStream(s.getInputStream());
					String str= dis.readUTF();
					Log.i("222222222", "touck ok");
					if((a = str.indexOf("@lxyz")) >= 0) {//读取标志
						local_xyz = str.substring(a + "@lxyz".length(), str.length());
						x = 2;
					}
					if((a = str.indexOf("@to")) >= 0) {//读取标志
						int b =str.indexOf("@te");
						target_orientation = str.substring(a + "@to".length(), b);
						target_elevation = str.substring(b + "@te".length(), str.length());
						x = 3;
					}
					if((a = str.indexOf("@td")) >= 0) {//读取标志
						target_distant = str.substring(a + "@td".length(), str.length());
						x = 4;
					}
					if((a = str.indexOf("@txyz")) >= 0) {//读取标志
						target_xyz = str.substring(a + "@txyz".length(), str.length());
						x = 5;
					}
					if((a = str.indexOf("@send")) >= 0) {//读取标志
						target_send = str.substring(a + "@send".length(), str.length());
						x = 6;
					}
					if((a = str.indexOf("@bat")) >= 0) {//读取标志
						host_bat = str.substring(a + "@bat".length(), str.length());
						x = 7;
					}
					if((a = str.indexOf("@si")) >= 0) {//读取标志												
						int b =str.indexOf("@sd");
						int c =str.indexOf("@sn");
						int d =str.indexOf("@sg");
						int e =str.indexOf("@sc");
						int f =str.indexOf("@st");						
						state_image = str.substring(a + "@si".length(), b);
						state_distant = str.substring(b + "@sd".length(), c);
						state_north = str.substring(c + "@sn".length(), d);
						state_GPS = str.substring(d + "@sg".length(), e);
						state_CPU = str.substring(e + "@sc".length(), f);
						state_temp = str.substring(f + "@st".length(), str.length());
						x = 8;						
					}
					Message msg = new Message();
					msg.what = x;//把传送数据包装成message
					mHandler.sendMessage(msg);	
				} catch (IOException e) {
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
				
				case 1://接收状态

					break;
				case 2://接收主机坐标高程
					
					textView_lxyz.setText(" 主机坐标：" + local_xyz + " ");
					Toast.makeText(getApplicationContext(), "成功接收：主机坐标",Toast.LENGTH_LONG).show();
					break;
				case 3://接收目标方位角俯仰角
					textView_tote.setText(" 方位角:" + target_orientation + " 俯仰角：" + target_elevation + " ");
					//Toast.makeText(getApplicationContext(), "成功接收：主机方位角",Toast.LENGTH_LONG).show();
					break;
				case 4://接收目标距离
					textView_td.setText(" 目标距离：" + target_distant + " ");
					//Toast.makeText(getApplicationContext(), "成功接收目标距离",Toast.LENGTH_LONG).show();
					break;
				case 5://接收目标坐标高程
					textView_txyz.setText(" 目标坐标：" + target_xyz + " ");
					Toast.makeText(getApplicationContext(), "成功接收：目标坐标",Toast.LENGTH_LONG).show();//下方弹出提示框
					break;
				case 6://接收目标坐标高程
					Toast.makeText(getApplicationContext(), target_send,Toast.LENGTH_LONG).show();//下方弹出提示框
					break;
				case 7://收到主机电量
					textView_hbat.setText(" 主机电量：" + host_bat + "% ");
					break;
				case 8://收到主机电量
					
					//Toast.makeText(getApplicationContext(), state_image + state_distant + state_north + state_GPS + state_CPU + "@stemp" + state_temp,Toast.LENGTH_LONG).show();
					//Toast.makeText(getApplicationContext(), state + "@stemp" + state_temp,Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
			}
		};
	}
//视频传输操作**************************************************************************	
	class ReceiveVideoThread implements Runnable{
		
		private boolean started = false;
		private Socket s = null;
		DataInputStream dis = null;
		private byte byteBuffer[] = new byte[1472];//原来是1024
		private int mCount = 0;
		private InputStream InputStream;
		Canvas canvas = null;
		Bitmap bm = null;
		
		Paint paint = new Paint(); //准星画笔
		float focusWidth;
		float focusHeight;
		float focusLong;
		public void run() {
			android.os.Process.setThreadPriority(-20);//os最高级
			if (isH264 == 1) {
				dpVideo = new DatagramPacket(h264,h264.length);
				while(true) {
					try {
						dsVideo.receive(dpVideo);
						//decoderbyty(dpVideo);硬解码
						onFrame(h264);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			else { //JPEG				
				try {
					ServerSocket ss = new ServerSocket(6000);
					Paint mPaint = new Paint();	//视频的画笔	
					
					drawFocusPrepare();//准星坐标准备
			        
					while (true) {					
						started = true;
						while (started) {
							boolean bConnected = false;
							s = ss.accept();							
							Log.i("ReceiveVideoThread", "A Video client connected!");
							bConnected = true;				
							
							while (bConnected) {// 循环因为要不停接受							
								try {		
									s = ss.accept();//每次都要接受一次流，因为发送时候一张图是一个流
									InputStream = s.getInputStream();
									canvas = surfaceHolder.lockCanvas();// 获取画布锁定	
									bm = BitmapFactory.decodeStream(InputStream);  //通过openRawResource方法得到工程项目中的图像资源的Raw数据流								
									//canvas.drawBitmap(bm, 0, 0, mPaint);										
									canvas.drawBitmap(bm, null, new Rect(0, 0,  sView.getWidth(),sView.getHeight()), mPaint);//第二和第三个参数分别是截取之前的尺寸，和之后缩放的尺寸
									//canvas.drawBitmap(bm, null, new Rect(0, 0,  800,600), mPaint);//第二和第三个参数分别是截取之前的尺寸，和之后缩放的尺寸
									drawFocus();
									s.close();
									InputStream.close();
										
									
								} catch (Exception e) {
									// TODO 自动生成的 catch 块
									e.printStackTrace();
								}finally {
									surfaceHolder.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
				                 }										
							}
						}
					}
				} catch (EOFException e) {// 这些为了client那边人为或非人为的关闭时，让这边的socket和DataInputStream关闭
					Log.i("ReceiveVideoThread", "A Video client close!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		public void onFrame(byte[] h264) {
	        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            	int inputBufferIndex = mediaCodec.dequeueInputBuffer(0);
	        if (inputBufferIndex >= 0) {
	            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
	            inputBuffer.clear();
	            inputBuffer.put(h264);
        	    mediaCodec.queueInputBuffer(inputBufferIndex, 0, h264.length, mCount * 1000000 / FRAME_RATE, 0);
                mCount ++;
	        }

	        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
	        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
	        while (outputBufferIndex >= 0) {
	            mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
	            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
	        }
		}
		public void drawFocusPrepare() {
			
			//准星画笔设置
	        paint.setAntiAlias(true);  
	        paint.setColor(Color.WHITE);  
	        paint.setAlpha(150);
	        paint.setStyle(Paint.Style.STROKE);  
	        paint.setStrokeWidth(5);  
	        //准星中心位置
	        final int[] location = new int[2];  
	        sView.getLocationInWindow(location); 
	        focusWidth = sView.getPivotX() + (sView.getWidth() / 2);//中心位置
	        focusHeight = sView.getPivotY() + (sView.getHeight() / 2);
	        //瞄准镜线长
	        focusLong = sView.getHeight() / 15;
	        
	        
		}
		public void drawFocus() {
			
	        
	        canvas.drawCircle(focusWidth, focusHeight, 2, paint);//画准星
	        canvas.drawLine(focusWidth, focusHeight + focusLong, focusWidth, focusHeight + 2*focusLong, paint);
	        canvas.drawLine(focusWidth, focusHeight - focusLong, focusWidth, focusHeight - 2*focusLong, paint);
	        canvas.drawLine(focusWidth + focusLong, focusHeight , focusWidth + 2*focusLong, focusHeight, paint);
	        canvas.drawLine(focusWidth - focusLong, focusHeight, focusWidth - 2*focusLong, focusHeight, paint);
		}

//		private void decoderbyty(DatagramPacket packet) {
//		    byte[] outData = new byte[packet.getLength()];
//		    System.arraycopy(packet.getData(), packet.getOffset(), outData, 0,
//		            packet.getLength());
//
//		    ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
//		    ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
//		    int inputBufferIndex = mediaCodec.dequeueInputBuffer(0);
//		    if (inputBufferIndex >= 0) {
//
//		        ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
//		        inputBuffer.clear();
//		        inputBuffer.put(outData);
//		        mediaCodec.queueInputBuffer(inputBufferIndex, 0, outData.length, 0, 0);
//		        // inputBuffer.put(yuv420Byte);
//		        // mediaCodec.queueInputBuffer(inputBufferIndex, 0,
//		        // yuv420Byte.length, 0, 0);
//		    }
//
//		    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//		    int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
//		    while (outputBufferIndex >= 0) {
//		        //ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
//		        /*
//		         * byte[] outData2 = new byte[bufferInfo.size];
//		         * outputBuffer.get(outData2); if (dataListOut != null)
//		         * dataListOut.add(outData2);
//		         */
//
//		    	mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
//		        outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
//		    }
//		}
//		public void onFrame(byte[] buf, int offset, int length, int flag) {
//	        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
//            	int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
//	        if (inputBufferIndex >= 0) {
//	            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
//	            inputBuffer.clear();
//	            inputBuffer.put(buf, offset, length);
//        	    int mCount = 0;
//				mediaCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount * 1000000 / FRAME_RATE, 0);
//                    mCount++;
//	        }
//
//        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
//        while (outputBufferIndex >= 0) {
//            mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
//            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
//        }
//	}

		
	   
	}

//测试UDP数据传输操作**************************************************************************
	class ReceiveTextThread implements Runnable {
		int portLocalNum = 5500;//本机端口
		
		String str = null;
		
		public void run() {
			android.os.Process.setThreadPriority(19);//os最低级
			byte data[] = new byte [1024];		
			try {
				dpText = new DatagramPacket(data,data.length);
				dsText = new DatagramSocket(portLocalNum);//建立本机UDP服务器插口并设置本机端口
			} catch (SocketException e) {
				e.printStackTrace();
			}
			while (true) {
				
				try {
					dsText.receive(dpText);
					str = new String(data,0,dpText.getLength());	//取出传送代码为string
					text_Time = str;
			        //new Thread(new showText(textView1, str)).start();//显示
			       // new showText(textView1, str).show();
			       // new showText(textView2, "主机IP:" + ipname).show();
			        //new Thread(new showText(textView2, "主机IP:" + ipname)).start();
					
					Thread.sleep(1000);	
				} catch (IOException e1) {
					// TODO 自动生成的 catch 块
					e1.printStackTrace();
				} catch (InterruptedException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
				
					
				// ***用thread发送不断更新的数据刷新控件显示或网络收发必须用handle放线程里，因为类似TextView是主线程的控件，而new
				// thread后是次线程，要用handle把次线程数据发给主线程
			
						
					
			
		
			}
		}
		
	}
//测试UDP数据显示类****************************************************************

	class showTextThread implements Runnable {
		String text_Time_old ;
		//String ipname_old ;
		public void run() {
			android.os.Process.setThreadPriority(19);//os最低级
			while(true) {
				Message msg = new Message();
//				if (ipname != null && ipname != ipname_old) {
//					msg.what = 2;
//					mHandler.sendMessage(msg);
//					ipname_old = ipname;
//				}
				
				if (text_Time != null && text_Time_old != text_Time) {
					
					msg.what = 1;
					mHandler.sendMessage(msg);
					text_Time_old = text_Time;
				}
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
				switch (msg.what) {
				case 1:
					textView1.setText(" " + text_Time + " ");
					break;
				case 2:
					//textView2.setText(" " + "主机IP:" + ipname + " ");
					break;
				default:
					break;
				}
			}
		};
	}
//左侧流程按键监听***********************************************************************
	class button_lxyz_OnTouchListener implements OnTouchListener{
		public boolean onTouch(View v, MotionEvent event)
	    {
			if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
			{
				button_lxyz.setTextColor(Color.parseColor("#000000"));
				button_lxyz.setBackgroundColor(Color.parseColor("#FFFFFF"));
				
			}  
			if (event.getAction() == MotionEvent.ACTION_UP)//弹起
	        {
				button_tote.setTextColor(Color.parseColor("#FFFFFF"));
				button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_td.setTextColor(Color.parseColor("#FFFFFF"));
				button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
				button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_next.setText("   确认   ");
				button_flag = 1;
	        }
	       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
	       return true;
	    }
    }
	////////////////////////////////////
	class button_tote_OnTouchListener implements OnTouchListener{
		public boolean onTouch(View v, MotionEvent event)
	    {
			if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
			{
				button_tote.setTextColor(Color.parseColor("#000000"));
				button_tote.setBackgroundColor(Color.parseColor("#FFFFFF"));
				
			}  
			if (event.getAction() == MotionEvent.ACTION_UP)//弹起
	        {
				button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
				button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_td.setTextColor(Color.parseColor("#FFFFFF"));
				button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
				button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));
				button_next.setText("   确认   ");
				button_flag = 2;
	        }
	       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
	       return true;
	    }
    }
	////////////////////////////////////
	class button_td_OnTouchListener implements OnTouchListener{
		public boolean onTouch(View v, MotionEvent event)
	    {
			if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
			{
				button_td.setTextColor(Color.parseColor("#000000"));
				button_td.setBackgroundColor(Color.parseColor("#FFFFFF"));
				
			}  
			if (event.getAction() == MotionEvent.ACTION_UP)//弹起
	        {
				button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
				button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_tote.setTextColor(Color.parseColor("#FFFFFF"));
				button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
				button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_next.setText("   确认   ");
				button_flag = 3;
	        }
	       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
	       return true;
	    }
    }
	////////////////////////////////////
	class button_txyz_OnTouchListener implements OnTouchListener{
		public boolean onTouch(View v, MotionEvent event)
	    {
			if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
			{
				button_txyz.setTextColor(Color.parseColor("#000000"));
				button_txyz.setBackgroundColor(Color.parseColor("#FFFFFF"));
				
			}  
			if (event.getAction() == MotionEvent.ACTION_UP)//弹起
	        {
				button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
				button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_tote.setTextColor(Color.parseColor("#FFFFFF"));
				button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_td.setTextColor(Color.parseColor("#FFFFFF"));
				button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_next.setText("   发送   ");
				button_flag = 4;
	        }
	       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
	       return true;
	    }
    }
//确认与上一步按键监听************************************************************
	class button_last_OnTouchListener implements OnTouchListener{
		boolean buttonOk;
		public boolean onTouch(View v, MotionEvent event)
	    {
			if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
			{	
				buttonOk = false;//为了每次点击只执行一次
			}  
			if ((event.getAction() == MotionEvent.ACTION_UP) && (button_flag == 1)) {	
				if(!buttonOk) {
					buttonOk = true;
		        }
	        }
			else if ((event.getAction() == MotionEvent.ACTION_UP) && (button_flag == 2)) {				
				if(!buttonOk) {
					button_lxyz.setTextColor(Color.parseColor("#000000"));
					button_lxyz.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_tote.setTextColor(Color.parseColor("#FFFFFF"));
					button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_td.setTextColor(Color.parseColor("#FFFFFF"));
					button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));
					buttonOk = true;
					button_flag = 1; 
					
				}
	        }
			else if ((event.getAction() == MotionEvent.ACTION_UP) && (button_flag == 3)) {
				if(!buttonOk) {
					button_tote.setTextColor(Color.parseColor("#000000"));
					button_tote.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_td.setTextColor(Color.parseColor("#FFFFFF"));
					button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					buttonOk = true;
					button_flag = 2; 
				}
	        }
			else if ((event.getAction() == MotionEvent.ACTION_UP) && (button_flag == 4)) {
				if(!buttonOk) {
					button_td.setTextColor(Color.parseColor("#000000"));
					button_td.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_tote.setTextColor(Color.parseColor("#FFFFFF"));
					button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));
					button_next.setText("   确认   ");
					buttonOk = true;
					button_flag = 3; 
				}
	        }
			else if ((event.getAction() == MotionEvent.ACTION_UP) && (button_flag == 5)) {
				if(!buttonOk) {
					button_txyz.setTextColor(Color.parseColor("#000000"));
					button_txyz.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_tote.setTextColor(Color.parseColor("#FFFFFF"));
					button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_td.setTextColor(Color.parseColor("#FFFFFF"));
					button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_next.setText("   发送   ");
					buttonOk = true;
					button_flag = 4; 
				}
	        }
	       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
	       return true;
	    }
    }
	////////////////////////////////////
	class button_next_OnTouchListener implements OnTouchListener{
		boolean buttonOk;
		public boolean onTouch(View v, MotionEvent event)
	    {
			if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
			{	
				buttonOk = false;//为了每次点击只执行一次
			}  
			else if ((event.getAction() == MotionEvent.ACTION_UP) && (button_flag == 1)) {	
				if(!buttonOk) {
					button_tote.setTextColor(Color.parseColor("#000000"));
					button_tote.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_td.setTextColor(Color.parseColor("#FFFFFF"));
					button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));
					new Thread(new CommendSendThread("2")).start();
					Log.i("11111111", "touch OK");
					buttonOk = true;
					button_flag = 2; 
					
		        }
	        }
			else if ((event.getAction() == MotionEvent.ACTION_UP) && (button_flag == 2)) {				
				if(!buttonOk) {
					button_td.setTextColor(Color.parseColor("#000000"));
					button_td.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_tote.setTextColor(Color.parseColor("#FFFFFF"));
					button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					new Thread(new CommendSendThread("3")).start();
					buttonOk = true;
					button_flag = 3; 
				}
	        }
			else if ((event.getAction() == MotionEvent.ACTION_UP) && (button_flag == 3)) {
				if(!buttonOk) {
					button_txyz.setTextColor(Color.parseColor("#000000"));
					button_txyz.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_tote.setTextColor(Color.parseColor("#FFFFFF"));
					button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_td.setTextColor(Color.parseColor("#FFFFFF"));
					button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_next.setText(" 发送 ");
					new Thread(new CommendSendThread("4")).start();
					new Thread(new CommendSendThread("5")).start();
					buttonOk = true;
					button_flag = 4; 
				}
	        }
			else if ((event.getAction() == MotionEvent.ACTION_UP) && (button_flag == 4)) {
				if(!buttonOk) {
					button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_tote.setTextColor(Color.parseColor("#FFFFFF"));
					button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_td.setTextColor(Color.parseColor("#FFFFFF"));
					button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));		
					button_next.setText("   重新瞄准   ");
					new Thread(new CommendSendThread("6")).start();
					buttonOk = true;
					button_flag = 5; 
				}
	        }
			else if ((event.getAction() == MotionEvent.ACTION_UP) && (button_flag == 5)) {
				if(!buttonOk) {
					button_td.setTextColor(Color.parseColor("#000000"));
					button_td.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_tote.setTextColor(Color.parseColor("#FFFFFF"));
					button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_next.setText("   确认   ");
					
					buttonOk = true;
					button_flag = 3; 
				}
	        }
	       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
	       return true;
	    }
    }
	//监听物理按键*********************************************
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      // TODO Auto-generated method stub
    	switch(keyCode) {
    	case KeyEvent.KEYCODE_BACK ://确认键
    		realButtonOk_back = false;//为了每次点击只执行一次
    		break;
    	case KeyEvent.KEYCODE_DPAD_LEFT :
    		realButtonOk_ok = false;//为了每次点击只执行一次
    		break;
    	case  KeyEvent.KEYCODE_HOME :
    		//realButtonOk = false;//为了每次点击只执行一次
    		break;
    	case  KeyEvent.KEYCODE_F1 :
    			
    		break;
    	case  KeyEvent.KEYCODE_F2 :
    		new Thread(new CommendSendThread("1")).start();
    		break;
    	}
      return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
    	  if (button_flag == 1) {	
				if(!realButtonOk_ok) {
					button_tote.setTextColor(Color.parseColor("#000000"));
					button_tote.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_td.setTextColor(Color.parseColor("#FFFFFF"));
					button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));
					new Thread(new CommendSendThread("2")).start();
					realButtonOk_ok = true;
					button_flag = 2; 
		        }
	        }
    	  else if (button_flag == 2) {				
				if(!realButtonOk_ok) {
					button_td.setTextColor(Color.parseColor("#000000"));
					button_td.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_tote.setTextColor(Color.parseColor("#FFFFFF"));
					button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					new Thread(new CommendSendThread("3")).start();
					realButtonOk_ok = true;
					button_flag = 3; 
				}
	        }
    	  else if (button_flag == 3) {
				if(!realButtonOk_ok) {
					button_txyz.setTextColor(Color.parseColor("#000000"));
					button_txyz.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_tote.setTextColor(Color.parseColor("#FFFFFF"));
					button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_td.setTextColor(Color.parseColor("#FFFFFF"));
					button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_next.setText("   发送   ");
					new Thread(new CommendSendThread("4")).start();
					new Thread(new CommendSendThread("5")).start();
					realButtonOk_ok = true;
					button_flag = 4; 
				}
	        }
    	  else if (button_flag == 4) {
				if(!realButtonOk_ok) {
					button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_tote.setTextColor(Color.parseColor("#FFFFFF"));
					button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_td.setTextColor(Color.parseColor("#FFFFFF"));
					button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));		
					button_next.setText("   重新瞄准   ");
					new Thread(new CommendSendThread("6")).start();
					realButtonOk_ok = true;
					button_flag = 5; 
				}
	        }
    	  else if (button_flag == 5) {
				if(!realButtonOk_ok) {
					button_td.setTextColor(Color.parseColor("#000000"));
					button_td.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_tote.setTextColor(Color.parseColor("#FFFFFF"));
					button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_next.setText("   确认   ");
					
					realButtonOk_ok = true;
					button_flag = 3; 
				}
	        }
            return true;         
      } else if (keyCode == KeyEvent.KEYCODE_BACK) {//后端
			if (button_flag == 1) {	
				if(!realButtonOk_back) {
					
					realButtonOk_back = true;
		        }
	        }
			else if (button_flag == 2) {				
				if(!realButtonOk_back) {
					button_lxyz.setTextColor(Color.parseColor("#000000"));
					button_lxyz.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_tote.setTextColor(Color.parseColor("#FFFFFF"));
					button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_td.setTextColor(Color.parseColor("#FFFFFF"));
					button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));
					realButtonOk_back = true;
					button_flag = 1; 
				}
	        }
			else if (button_flag == 3) {
				if(!realButtonOk_back) {
					button_tote.setTextColor(Color.parseColor("#000000"));
					button_tote.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_td.setTextColor(Color.parseColor("#FFFFFF"));
					button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					realButtonOk_back = true;
					button_flag = 2; 
				}
	        }
			else if (button_flag == 4) {
				if(!realButtonOk_back) {
					button_td.setTextColor(Color.parseColor("#000000"));
					button_td.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_tote.setTextColor(Color.parseColor("#FFFFFF"));
					button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));
					button_next.setText("   确认   ");
					realButtonOk_back = true;
					button_flag = 3; 
				}
	        }
			else if (button_flag == 5) {
				if(!realButtonOk_back) {
					button_txyz.setTextColor(Color.parseColor("#000000"));
					button_txyz.setBackgroundColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
					button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_tote.setTextColor(Color.parseColor("#FFFFFF"));
					button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_td.setTextColor(Color.parseColor("#FFFFFF"));
					button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
					button_next.setText("   发送   ");
					realButtonOk_back = true;
					button_flag = 4; 
				}
	        }
        return true;
      } else if (keyCode == KeyEvent.KEYCODE_HOME) {
        
        return false;
      } else if (keyCode == KeyEvent.KEYCODE_F1) {
    	  
        return false;
      } else if (keyCode == KeyEvent.KEYCODE_F2) {
    	  AlertDialog_state ();
        return false;
      } 
      return super.onKeyUp(keyCode, event);
    }
	///监听右侧按键/////////////////////////////////
	//去雾
	class button_defog_OnClickListener implements OnCheckedChangeListener{
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {  
            if(isChecked){  
            	new Thread(new CommendSendThread("7")).start();
            	Toast.makeText(getApplicationContext(), "去雾已开启",Toast.LENGTH_LONG).show();
            	
                
            }else{  
            	new Thread(new CommendSendThread("8")).start(); 
            	Toast.makeText(getApplicationContext(), "去雾已关闭",Toast.LENGTH_LONG).show();
            }  
      
	       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
	       //return true;
	    }
    }
	//状态
	class button_state_OnTouchListener implements OnTouchListener{
		public boolean onTouch(View v, MotionEvent event)
	    {
			if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
			{
				new Thread(new CommendSendThread("1")).start();			
			}  
			if (event.getAction() == MotionEvent.ACTION_UP)//弹起
	        {
				AlertDialog_state ();
	        }
	       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
	       return true;
	    }
    }
	//系统设置
		class button_set_OnTouchListener implements OnTouchListener{
			public boolean onTouch(View v, MotionEvent event)
		    {
				if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
				{
					
					
				}  
				if (event.getAction() == MotionEvent.ACTION_UP)//弹起
		        {
					
		        }
		       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
		       return true;
		    }
	    }
	//焦距拉近
		class button_zoomUp_OnTouchListener implements OnTouchListener{
			public boolean onTouch(View v, MotionEvent event)
		    {
				if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
				{
					new Thread(new CommendSendThread("9")).start();
					
				}  
				if (event.getAction() == MotionEvent.ACTION_UP)//弹起
		        {
					new Thread(new CommendSendThread("33")).start();
		        }
		       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
		       return true;
		    }
	    }
		//焦距拉远
		class button_zoomDown_OnTouchListener implements OnTouchListener{
			public boolean onTouch(View v, MotionEvent event)
		    {
				if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
				{
					
					new Thread(new CommendSendThread("10")).start();
				}  
				if (event.getAction() == MotionEvent.ACTION_UP)//弹起
		        {
					new Thread(new CommendSendThread("33")).start();
		        }
		       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
		       return true;
		    }
	    }
//摇杆**********************************************
	class joyStickThread implements Runnable { 
		String SX;
		String SY;
		int IX=0;
		int IY=0;
		String X = "3";//输出结果
		String Y = "3";//输出结果
		int Xmax = 2950;
		int Xnom = 1720;	
		int Ymax = 2650;
		int Ynom = 1640;
		int s = 200;//第一级
		int n = 800;//第二级
		//int dX = Xmax-Xnom;
		//int dY = Ymax-Ynom;
		int d =1000;
		public void run() {
			//启动ADC
	        adc.Open();
			while (true) {
				SX = readADC(0);//读ADC0口
				SY = readADC(1);//读ADC1口
				IX=Integer.valueOf(SX).intValue();
				IY=Integer.valueOf(SY).intValue();
			
				String Xnew = toStickX();
				String Ynew = toStickY();
				if ((Xnew != X) || (Ynew != Y)){
					new Thread(new CommendSendThread(Y+X)).start();
					if ((Xnew == "3") && (Xnew =="3")) {new Thread(new CommendSendThread("33")).start();}//发送两次停止
					X = Xnew;
					Y = Ynew;
				}
				
				Message msg = new Message();
				msg.what = 1;
				wifiHandler.sendMessage(msg);			
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}
		private Handler wifiHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);				
				switch (msg.what) {
				case 1:					
					textView1.setText(" " + Y + " " + IY + " ");												
					break;
				default:
					break;
				}
			}
		};
		private String readADC (int channel) {
			int i;
			int[] R;
			String str;
			String rxIdCode = "";
			
			adc.Ioctl(channel,0xc000fa01);//读通道0或1    //源码define ADC_SET_CHANNEL		0xc000fa01，所以后面参数是选择设置通道，这个是通道0
			R = adc.Read();			

			if(R != null)
			{
				System.arraycopy(R, 0, RfidBuffer, RfidRxCount, R.length);
			}
			for(i=0; i<5; i++)
			{ 
				str = Integer.toHexString(RfidBuffer[i]);
				
				rxIdCode += str.substring(1);

			}	
			return rxIdCode;			
		}
		private String toStickX () {//计算等级
			String z = "3";
			int q = IX - Xnom;
			
			if ((q >= -s)&(q <= s)) {z = "3";}
			else if ((q > s)&(q <= n)) {z = "4";}
			else if ((q < -s)&(q >= -n)) {z = "2";}
			else if ((q > n)) {z = "5";}
			else if ((q < -n)) {z = "1";}
			return  z;
			//7为正，1为负
		}
		private String toStickY () {//计算等级
			String z = "3";
			int q = IY - Ynom;
		
			if ((q >= -s)&(q <= s)) {z = "3";}
			else if ((q > s)&(q <= n)) {z = "4";}
			else if ((q < -s)&(q >= -n)) {z = "2";}
			else if ((q > n)) {z = "5";}
			else if ((q < -n)) {z = "1";}			
			return  z;
			//7为正，1为负
		}
	}
//舵机控制并用tcp发送**************************************************************************
	 ///////////////////////////////////////////////////
	class forward_OnTouchListener implements OnTouchListener//用这个配合上面地配置就可以监听按下这个按键的触发
    {
      public boolean onTouch(View v, MotionEvent event)
      {
        if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
           {
        	forward.setImageResource(R.drawable.forward2);//按下后变按下效果图片
        	String str = "61";	
     	    //sendControlData(str);	 
        	new Thread(new CommendSendThread(str)).start();
     	  
            }  
       if (event.getAction() == MotionEvent.ACTION_UP)//弹起
          {
    	   forward.setImageResource(R.drawable.forward1);
    	   String str = "60";	
    	   //sendControlData(str);	
    	   new Thread(new CommendSendThread(str)).start();
    	   
          }
       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
       return true;
      }
    }
 
    ////////////////////////////////////
	class backward_OnTouchListener implements OnTouchListener
    {
      public boolean onTouch(View v, MotionEvent event)
      {
        if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
           {
        	backward.setImageResource(R.drawable.backward2);
        	String str = "62";	
     	    //sendControlData(str);	
        	new Thread(new CommendSendThread(str)).start();
            }  
       if (event.getAction() == MotionEvent.ACTION_UP)//弹起
          {
    	   backward.setImageResource(R.drawable.slide_one);
    	   String str = "60";	
    	   //sendControlData(str);	
    	   new Thread(new CommendSendThread(str)).start();
          }
       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
       return true;
      }
    }
 
    ////////////////////////////////////
	class left_OnTouchListener implements OnTouchListener
    {
    
      public boolean onTouch(View v, MotionEvent event)
      {
        if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
           {
        	left.setImageResource(R.drawable.left2);
        	String str = "63";	
     	    //sendControlData(str);	
        	new Thread(new CommendSendThread(str)).start();
            }  
       if (event.getAction() == MotionEvent.ACTION_UP)//弹起
          {
    	   left.setImageResource(R.drawable.left1);
    	   String str = "60";	
    	   //sendControlData(str);	
    	   new Thread(new CommendSendThread(str)).start();
          }
       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
       return true;
      }
    }
 
    ////////////////////////////////////
	class right_OnTouchListener implements OnTouchListener
    {
   
      public boolean onTouch(View v, MotionEvent event)
      {
        if (event.getAction() == MotionEvent.ACTION_DOWN)//按下
           {
        	right.setImageResource(R.drawable.right2);
        	String str = "64";	
     	    //sendControlData(str);	
        	new Thread(new CommendSendThread(str)).start();
            }  
       if (event.getAction() == MotionEvent.ACTION_UP)//弹起
          {
    	   right.setImageResource(R.drawable.right1);
    	   String str = "60";	
    	   //sendControlData(str);	
    	   new Thread(new CommendSendThread(str)).start();
          }
       //返回true  表示事件处理完毕，会中断系统对该事件的处理。false 系统会同时处理对应的事件
       return true;
      }
    }
//舵机控制信号UDP传输操作**************************************************************************
	void sendControlData(String str)
	{
		try{
			 boolean flag = true;
			 portRemoteNum=9000;//远程端口
			 portLocalNum=9500;//本机端口
	         dsCtl  = new DatagramSocket(portLocalNum);//建立本机UDP服务器插口并设置本机端口
             } catch (Exception e) {
       e.printStackTrace();}			
		try {			      		    
		    InetAddress serverAddress = InetAddress.getByName(ipname);	 //cb?  		    
		    byte data [] = str.getBytes(); 		    
		    dpCtl = new DatagramPacket(data,data.length,serverAddress,portRemoteNum);	//把要传的数据打包并配置输出IP和端口
		    //从本地端口给指定IP的远程端口发数据包		    
		    //lock.acquire();
		    //只要是用到网络传输就要用到线程，不能用主线程
		    SendControlDataThread SCDT = new SendControlDataThread();
	        Thread TSCDT = new Thread(SCDT);
	        TSCDT.start();		   
		    //lock.release();		    
		    } catch (Exception e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }		
	}
	//舵机控制信号UDP传输线程操作**************************************************************************	
		class SendControlDataThread implements Runnable{				
			public void run() {
				 try {
					dsCtl.send(dpCtl);
				} catch (IOException e) {
					e.printStackTrace();
				}
				 dsCtl.close(); 
			}
		}
	//消息提示框**************************************************************************
	public void AlertDialog (String Title,String Message,final boolean isExit) {	
		new android.app.AlertDialog.Builder(BitCameraClient.this)  		
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
	//状态消息提示框********************************************************	
	public void AlertDialog_state () {
		boolean[] x = {true,true,true,true,true};

		AlertDialog alertDialog = new android.app.AlertDialog.Builder(BitCameraClient.this)  							
		 //.setMultiChoiceItems(new String[] {"摄像模块","测距模块","寻北模块","定位模块","处理器模块" },x,null)
		 .setItems(new  String[] {"摄像模块        "+state_image,"测距模块        "+state_distant,"寻北模块        "+state_north,"定位模块        "+state_GPS,"处理器模块        "+state_CPU,"电池温度        "+state_temp+"°C"},  null )
		 .setPositiveButton("确定", null)
		 .create();   
	    Window window = alertDialog.getWindow();   
	    WindowManager.LayoutParams params = window.getAttributes();   	     	   
        //设置隐藏导航栏               
        params.systemUiVisibility =View.SYSTEM_UI_FLAG_FULLSCREEN |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;  
        // 设置透明度为0.3   
        params.alpha = 0.8f;
        //设置宽度
        //params.width = 300 ;
        //params.width = LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);  
        //alertDialog.getWindow().setAttributes(params); 
		alertDialog.show(); 
		//alertDialog.getWindow().setLayout(300, 200); 
	}
	//获取流量和wifi强度*******************************************************
	class showWifiThread implements Runnable {
		private long total_data_down = TrafficStats.getTotalRxBytes();
		private long total_data_up = TrafficStats.getTotalTxBytes();
	    private final int count = 1; //几秒刷新一次
	    private int level;                      //信号强度值  
 
		public void run() {
		
			while(true){
			android.os.Process.setThreadPriority(19);//os最低级	
			//wifiHandler.postDelayed(SWT, count * 1000);//定时器 用这个替代循环线程里的延时 while也不需要了 似乎不如sleep
			//Log.i("222222222222222222222", "2222222222222222222222222");
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();  
            //获得信号强度值  
            level = wifiInfo.getRssi();  
            
			Message msg = new Message();
			msg.what = 1;
			//计算每秒下载流量
			long traffic_data_down = TrafficStats.getTotalRxBytes() - total_data_down;
			total_data_down = TrafficStats.getTotalRxBytes();
			//计算每秒上传流量
			long traffic_data_up = TrafficStats.getTotalTxBytes() - total_data_up;
			total_data_up = TrafficStats.getTotalTxBytes();
			
			msg.arg1 = (int) (traffic_data_down /count) ;
			msg.arg2 = (int) (traffic_data_up /count) ;
			wifiHandler.sendMessage(msg);	
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}}
      
		private Handler wifiHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);				
				switch (msg.what) {
				case 1:
					
					textView_wifi.setText(" " + "WIFI强度:" + level + "dbm" + " ");
					//Log.i("1111111111111111", "11111111111111111111111111111111");
					if((msg.arg1 > 1024) && (msg.arg2 > 1024)) {
						textView_rate.setText(" " + "下行:" + msg.arg1 / 1024 + "kb/s" + " "+ "上行:" + msg.arg2 / 1024 + "kb/s" + " ");					
					}
					if((msg.arg1 > 1024) && (msg.arg2 < 1024)) {
						textView_rate.setText(" " + "下行:" + msg.arg1 / 1024 + "kb/s" + " "+ "上行:" + msg.arg2 + "b/s" + " ");					
					}
					if((msg.arg1 < 1024) && (msg.arg2 > 1024)) {
						textView_rate.setText(" " + "下行:" + msg.arg1 + "b/s" + " " + "上行:" + msg.arg2 / 1024 + "kb/s" + " ");					
					}
					if((msg.arg1 < 1024) && (msg.arg2 < 1024)) {
						textView_rate.setText(" " + "下行:" + msg.arg1 + "b/s" + " "+ "上行:" + msg.arg2 + "b/s" + " ");
					}
					break;
				default:
					break;
				}
			}
		};
	}
	//获取电池电量****************************************************
	/**接受电量改变广播*/  
    class BatteryBroadcastReceiver extends BroadcastReceiver{  
          
        @Override  
        public void onReceive(Context context, Intent intent) {  
              
            if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){  
                  
                int level = intent.getIntExtra("level", 0);  
                int scale = intent.getIntExtra("scale", 100);  
                int powerPercent = level*100 /scale;
                textView_lbat.setText(" 本机电量：" + powerPercent + "% ");
//                int curPower = (level * 100 / scale)/25;  
//                switch (curPower) {  
//                case 0:  
//                    tvBatteryChanged.setImageBitmap(BitmapFactory.decodeResource(Main.this.getResources(), R.drawable.power0));  
//                    break;  
//                case 1:  
//                    tvBatteryChanged.setImageBitmap(BitmapFactory.decodeResource(Main.this.getResources(), R.drawable.power1));  
//                    break;  
//                case 2:  
//                    tvBatteryChanged.setImageBitmap(BitmapFactory.decodeResource(Main.this.getResources(), R.drawable.power2));  
//                    break;  
//                case 3:  
//                    tvBatteryChanged.setImageBitmap(BitmapFactory.decodeResource(Main.this.getResources(), R.drawable.power3));  
//                    break;  
//                case 4:  
//                    tvBatteryChanged.setImageBitmap(BitmapFactory.decodeResource(Main.this.getResources(), R.drawable.power4));  
//                    break;  
//                }  
            }  
        }  
    }  
    
	//测试获取本地IP**********************************************
 public String getLocalIpAddress() {
    
        String ipaddress = "";
        try
        {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements())
            {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements())
                {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(ip
                                    .getHostAddress()))
                    {
                        return ipaddress = ip.getHostAddress();
                    }
                }

            }
        }
        catch (SocketException e)
        {
            Log.e("feige", "获取本地ip地址失败");
            e.printStackTrace();
        }
        return ipaddress;

    }

    // 得到本机Mac地址
    public String getLocalMac()
    {
        String mac = "";
        // 获取wifi管理器
        WifiManager wifiMng = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfor = wifiMng.getConnectionInfo();
        mac = wifiInfor.getMacAddress();
        return mac;
    }
//adc引用库**************************************
	static {
        System.loadLibrary("adc");
	}
	
//自动监听触摸坐标**********************************************************
	public boolean onTouchEvent(MotionEvent event) {
	// 在这里判断一下如果是按下操作就获取坐标然后执行方法
	if (event.getAction() == MotionEvent.ACTION_DOWN) {
		touchTrace(event.getX(), event.getY());
		
	}
	return super.onTouchEvent(event);
	}
//计算点按跟踪角度并发送****************************************	
	public float touchTrace(float x, float y ) {
		//x,y手点按位置
		int xMax = 1024;//屏幕X轴最大点
		int yMax = 768;//屏幕y轴最大点 ，4.4.4为768,4.0.3为716
		int xMid = 512;//屏幕中心点
		int yMid = 384;//屏幕中心点		4.4.4为384,4.0.3为358
		float angleXmax = 24;//摄像头X视场角的一半
		float angleYmax = 18;//摄像头Y视场角的一半
		float angleX = 0;
		float angleY = 0;
	
		if (true) {//x > 80 && y > 300 && x <200 && y < 600
			double cX = (Math.tan(Math.toRadians(angleXmax)))/(xMax-xMid);//计算常数 避免反复运算  正切也是用的弧度,所以要转弧度
			double cY = (Math.tan(Math.toRadians(angleYmax)))/(yMax-yMid);//计算常数 避免反复运算
			if (x != xMid) {
				angleX = (float)Math.toDegrees(Math.atan(cX*(x-xMid)));//反正切得到的是弧度,所以转角度
			}
			if (y != yMid) {
				angleY =  (float)Math.toDegrees(Math.atan(cY*(-(y-yMid))));//取负因为屏幕y轴反的
			}	
			new Thread(new CommendSendThread("@tX" + String.valueOf(angleX) + "@tY" + String.valueOf(angleY))).start();
			Toast.makeText(getApplicationContext(), " X：" + x + " Y:" + y + " X角："+ angleX + " Y角："+ angleY + " ",Toast.LENGTH_SHORT).show();

		}
		
		return angleX + angleY;
		


		
	}

		
}




