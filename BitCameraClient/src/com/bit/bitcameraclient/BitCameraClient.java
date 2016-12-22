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
	boolean isPreview = false;        //�Ƿ��������
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
	public int isH264 = 0;//����H264����JPEG
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
	String state_image;//ͼ��ģ��
	String state_distant;//������ģ��
	String state_north;//Ѱ����ģ��
	String state_GPS;//GPS��λģ��
	String state_CPU;//��ؼ����ģ��
	String state_temp;//�¶�
	String state;//״̬����
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
        //�������ص�����
        window = getWindow();  
        WindowManager.LayoutParams params = window.getAttributes();  
        params.systemUiVisibility =View.SYSTEM_UI_FLAG_FULLSCREEN |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;  
        window.setAttributes(params);  
        // ����ȫ��
        requestWindowFeature(Window.FEATURE_NO_TITLE);
     	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);    	
        setContentView(R.layout.main);
        
       
        
		sView = (SurfaceView) findViewById(R.id.sView);                  // ��ȡ������SurfaceView���		
		surfaceHolder = sView.getHolder();                               // ���SurfaceView��SurfaceHolder
		
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
		
//		forward = (ImageButton)findViewById(R.id.forward);//���ð�ť������
//		forward.setOnTouchListener(new forward_OnTouchListener());//�����Ժ���������ж���
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
        // ��ȡ����IP��ַ
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        ipname = data.getString("ipname");
        //textView_hIP.setText(" " + "����IP:" + ipname + " ");		
        textView_hIP.setText(" " + "�������:" + "01" + " ");	
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);  
        //��ص�������
        registerReceiver(new BatteryBroadcastReceiver(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); 
        
        
        
		//����������Ϊ��ĳЩ����ر���UDP	����Ҫʱ����lock.acquire()
        WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		lock= manager.createMulticastLock("test wifi");
		
		// ΪsurfaceHolder���һ���ص�������
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
		// ���ø�SurfaceView�Լ���ά������    
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		
					
    }
    
	public void launch() {
        //��������IP�߳�****************************************************************************
        IpThread IT = new IpThread();
        Thread T6 = new Thread(IT);
        T6.start();
      //����tcp�������߳�****************************************************************************
        TcpReceiveThread TRT = new TcpReceiveThread();
        Thread T8 = new Thread(TRT);
        T8.start();
       
		//Ӳ��������ʼ��***********************************************************
		if (isH264 == 1) {
			mediaCodec = MediaCodec.createDecoderByType("video/avc");
		    MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
		    mediaCodec.configure(mediaFormat, surfaceHolder.getSurface(), null, 0);//ʹ�����APIҪ��С16
		    mediaCodec.start();
		}
		//����ͼ�����ӽ����������߳�**************************************************************************		
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
        //����UDP�������ݽ����߳�**************************************************************************		
//		ReceiveTextThread RTT = new ReceiveTextThread();
//        Thread T2 = new Thread(RTT);     
//        T2.start();
//        //����UDP����������ʾ�߳�**********************************************************************
//        showTextThread STT = new showTextThread();
//        Thread T7 = new Thread(STT);        
//        T7.start();
        //ҡ���߳�����*********************************
        joyStickThread JST = new joyStickThread();
        Thread T10 = new Thread(JST);
        //T10.start();
        //����WIFI�������**********************************************************************
        SWT = new showWifiThread();
        new Thread(SWT).start();
       
	}
//���ͱ���IP***********************************************************
class IpThread implements Runnable{
	public void run() {	
		try {
			Socket s = new Socket(ipname, 4500);
			//Toast.makeText(getApplicationContext(), "���ӳɹ�",Toast.LENGTH_LONG).show();
			Log.i("ReceiveVideoThread", "A Video client connected!");
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			
			WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE); //��ȡ����ip
		    WifiInfo info = wifiManager.getConnectionInfo();
		    String localIpnameString = Formatter.formatIpAddress(info.getIpAddress());
		    
			//String localIpnameString = s.getLocalAddress().toString();//getLocalIpAddress(); //��ȡ����ip
			Log.i("LocalIp", "Local ipname = " + localIpnameString);
			dos.writeUTF(localIpnameString);// дһ��UNICODE���͵��ַ���
			dos.flush();
			dos.close();
			s.close();	      
		} catch (UnknownHostException e1) {
			// TODO �Զ����ɵ� catch ��
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO �Զ����ɵ� catch ��
			e1.printStackTrace();
		}
	}
}	
//TCP��ָ������*********************************************************************
	class CommendSendThread implements Runnable{
		String str;
		public CommendSendThread(String str) {
			this.str = str;
		}

		public void run() {//����ָ��new Thread(new CommendSendThread("1")).start();
		
				
			try {
				sCtl = new Socket(ipname, 5000);
				DataOutputStream dos = new DataOutputStream(sCtl.getOutputStream());
				dos.writeUTF(str);// дһ��UNICODE���͵��ַ���
				dos.flush();
				dos.close();
				sCtl.close();//ÿ�η�����Ͽ�����Ӱ���´η���
			} catch (IOException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
	
		}
	}
//TCP�����ݲ�������ʾ**************************************************************************************	
	class TcpReceiveThread implements Runnable{
		ServerSocket ss;
		int a = -1;
		int b = -1;
		int x;
		public void run() {
			try {
				ss = new ServerSocket(5500);
			} catch (IOException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
			while (true) {
				try {
					Socket s = ss.accept();
					DataInputStream dis = new DataInputStream(s.getInputStream());
					String str= dis.readUTF();
					Log.i("222222222", "touck ok");
					if((a = str.indexOf("@lxyz")) >= 0) {//��ȡ��־
						local_xyz = str.substring(a + "@lxyz".length(), str.length());
						x = 2;
					}
					if((a = str.indexOf("@to")) >= 0) {//��ȡ��־
						int b =str.indexOf("@te");
						target_orientation = str.substring(a + "@to".length(), b);
						target_elevation = str.substring(b + "@te".length(), str.length());
						x = 3;
					}
					if((a = str.indexOf("@td")) >= 0) {//��ȡ��־
						target_distant = str.substring(a + "@td".length(), str.length());
						x = 4;
					}
					if((a = str.indexOf("@txyz")) >= 0) {//��ȡ��־
						target_xyz = str.substring(a + "@txyz".length(), str.length());
						x = 5;
					}
					if((a = str.indexOf("@send")) >= 0) {//��ȡ��־
						target_send = str.substring(a + "@send".length(), str.length());
						x = 6;
					}
					if((a = str.indexOf("@bat")) >= 0) {//��ȡ��־
						host_bat = str.substring(a + "@bat".length(), str.length());
						x = 7;
					}
					if((a = str.indexOf("@si")) >= 0) {//��ȡ��־												
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
					msg.what = x;//�Ѵ������ݰ�װ��message
					mHandler.sendMessage(msg);	
				} catch (IOException e) {
					// TODO �Զ����ɵ� catch ��
					e.printStackTrace();
				}
			}
		}
		private Handler mHandler = new Handler() {
			
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);				
				switch (msg.what) {//������string����
				
				case 1://����״̬

					break;
				case 2://������������߳�
					
					textView_lxyz.setText(" �������꣺" + local_xyz + " ");
					Toast.makeText(getApplicationContext(), "�ɹ����գ���������",Toast.LENGTH_LONG).show();
					break;
				case 3://����Ŀ�귽λ�Ǹ�����
					textView_tote.setText(" ��λ��:" + target_orientation + " �����ǣ�" + target_elevation + " ");
					//Toast.makeText(getApplicationContext(), "�ɹ����գ�������λ��",Toast.LENGTH_LONG).show();
					break;
				case 4://����Ŀ�����
					textView_td.setText(" Ŀ����룺" + target_distant + " ");
					//Toast.makeText(getApplicationContext(), "�ɹ�����Ŀ�����",Toast.LENGTH_LONG).show();
					break;
				case 5://����Ŀ������߳�
					textView_txyz.setText(" Ŀ�����꣺" + target_xyz + " ");
					Toast.makeText(getApplicationContext(), "�ɹ����գ�Ŀ������",Toast.LENGTH_LONG).show();//�·�������ʾ��
					break;
				case 6://����Ŀ������߳�
					Toast.makeText(getApplicationContext(), target_send,Toast.LENGTH_LONG).show();//�·�������ʾ��
					break;
				case 7://�յ���������
					textView_hbat.setText(" ����������" + host_bat + "% ");
					break;
				case 8://�յ���������
					
					//Toast.makeText(getApplicationContext(), state_image + state_distant + state_north + state_GPS + state_CPU + "@stemp" + state_temp,Toast.LENGTH_LONG).show();
					//Toast.makeText(getApplicationContext(), state + "@stemp" + state_temp,Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
			}
		};
	}
//��Ƶ�������**************************************************************************	
	class ReceiveVideoThread implements Runnable{
		
		private boolean started = false;
		private Socket s = null;
		DataInputStream dis = null;
		private byte byteBuffer[] = new byte[1472];//ԭ����1024
		private int mCount = 0;
		private InputStream InputStream;
		Canvas canvas = null;
		Bitmap bm = null;
		
		Paint paint = new Paint(); //׼�ǻ���
		float focusWidth;
		float focusHeight;
		float focusLong;
		public void run() {
			android.os.Process.setThreadPriority(-20);//os��߼�
			if (isH264 == 1) {
				dpVideo = new DatagramPacket(h264,h264.length);
				while(true) {
					try {
						dsVideo.receive(dpVideo);
						//decoderbyty(dpVideo);Ӳ����
						onFrame(h264);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			else { //JPEG				
				try {
					ServerSocket ss = new ServerSocket(6000);
					Paint mPaint = new Paint();	//��Ƶ�Ļ���	
					
					drawFocusPrepare();//׼������׼��
			        
					while (true) {					
						started = true;
						while (started) {
							boolean bConnected = false;
							s = ss.accept();							
							Log.i("ReceiveVideoThread", "A Video client connected!");
							bConnected = true;				
							
							while (bConnected) {// ѭ����ΪҪ��ͣ����							
								try {		
									s = ss.accept();//ÿ�ζ�Ҫ����һ��������Ϊ����ʱ��һ��ͼ��һ����
									InputStream = s.getInputStream();
									canvas = surfaceHolder.lockCanvas();// ��ȡ��������	
									bm = BitmapFactory.decodeStream(InputStream);  //ͨ��openRawResource�����õ�������Ŀ�е�ͼ����Դ��Raw������								
									//canvas.drawBitmap(bm, 0, 0, mPaint);										
									canvas.drawBitmap(bm, null, new Rect(0, 0,  sView.getWidth(),sView.getHeight()), mPaint);//�ڶ��͵����������ֱ��ǽ�ȡ֮ǰ�ĳߴ磬��֮�����ŵĳߴ�
									//canvas.drawBitmap(bm, null, new Rect(0, 0,  800,600), mPaint);//�ڶ��͵����������ֱ��ǽ�ȡ֮ǰ�ĳߴ磬��֮�����ŵĳߴ�
									drawFocus();
									s.close();
									InputStream.close();
										
									
								} catch (Exception e) {
									// TODO �Զ����ɵ� catch ��
									e.printStackTrace();
								}finally {
									surfaceHolder.unlockCanvasAndPost(canvas);// �����������ύ���õ�ͼ��
				                 }										
							}
						}
					}
				} catch (EOFException e) {// ��ЩΪ��client�Ǳ���Ϊ�����Ϊ�Ĺر�ʱ������ߵ�socket��DataInputStream�ر�
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
			
			//׼�ǻ�������
	        paint.setAntiAlias(true);  
	        paint.setColor(Color.WHITE);  
	        paint.setAlpha(150);
	        paint.setStyle(Paint.Style.STROKE);  
	        paint.setStrokeWidth(5);  
	        //׼������λ��
	        final int[] location = new int[2];  
	        sView.getLocationInWindow(location); 
	        focusWidth = sView.getPivotX() + (sView.getWidth() / 2);//����λ��
	        focusHeight = sView.getPivotY() + (sView.getHeight() / 2);
	        //��׼���߳�
	        focusLong = sView.getHeight() / 15;
	        
	        
		}
		public void drawFocus() {
			
	        
	        canvas.drawCircle(focusWidth, focusHeight, 2, paint);//��׼��
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

//����UDP���ݴ������**************************************************************************
	class ReceiveTextThread implements Runnable {
		int portLocalNum = 5500;//�����˿�
		
		String str = null;
		
		public void run() {
			android.os.Process.setThreadPriority(19);//os��ͼ�
			byte data[] = new byte [1024];		
			try {
				dpText = new DatagramPacket(data,data.length);
				dsText = new DatagramSocket(portLocalNum);//��������UDP��������ڲ����ñ����˿�
			} catch (SocketException e) {
				e.printStackTrace();
			}
			while (true) {
				
				try {
					dsText.receive(dpText);
					str = new String(data,0,dpText.getLength());	//ȡ�����ʹ���Ϊstring
					text_Time = str;
			        //new Thread(new showText(textView1, str)).start();//��ʾ
			       // new showText(textView1, str).show();
			       // new showText(textView2, "����IP:" + ipname).show();
			        //new Thread(new showText(textView2, "����IP:" + ipname)).start();
					
					Thread.sleep(1000);	
				} catch (IOException e1) {
					// TODO �Զ����ɵ� catch ��
					e1.printStackTrace();
				} catch (InterruptedException e) {
					// TODO �Զ����ɵ� catch ��
					e.printStackTrace();
				}
				
					
				// ***��thread���Ͳ��ϸ��µ�����ˢ�¿ؼ���ʾ�������շ�������handle���߳����Ϊ����TextView�����̵߳Ŀؼ�����new
				// thread���Ǵ��̣߳�Ҫ��handle�Ѵ��߳����ݷ������߳�
			
						
					
			
		
			}
		}
		
	}
//����UDP������ʾ��****************************************************************

	class showTextThread implements Runnable {
		String text_Time_old ;
		//String ipname_old ;
		public void run() {
			android.os.Process.setThreadPriority(19);//os��ͼ�
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
					// TODO �Զ����ɵ� catch ��
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
					//textView2.setText(" " + "����IP:" + ipname + " ");
					break;
				default:
					break;
				}
			}
		};
	}
//������̰�������***********************************************************************
	class button_lxyz_OnTouchListener implements OnTouchListener{
		public boolean onTouch(View v, MotionEvent event)
	    {
			if (event.getAction() == MotionEvent.ACTION_DOWN)//����
			{
				button_lxyz.setTextColor(Color.parseColor("#000000"));
				button_lxyz.setBackgroundColor(Color.parseColor("#FFFFFF"));
				
			}  
			if (event.getAction() == MotionEvent.ACTION_UP)//����
	        {
				button_tote.setTextColor(Color.parseColor("#FFFFFF"));
				button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_td.setTextColor(Color.parseColor("#FFFFFF"));
				button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
				button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_next.setText("   ȷ��   ");
				button_flag = 1;
	        }
	       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
	       return true;
	    }
    }
	////////////////////////////////////
	class button_tote_OnTouchListener implements OnTouchListener{
		public boolean onTouch(View v, MotionEvent event)
	    {
			if (event.getAction() == MotionEvent.ACTION_DOWN)//����
			{
				button_tote.setTextColor(Color.parseColor("#000000"));
				button_tote.setBackgroundColor(Color.parseColor("#FFFFFF"));
				
			}  
			if (event.getAction() == MotionEvent.ACTION_UP)//����
	        {
				button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
				button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_td.setTextColor(Color.parseColor("#FFFFFF"));
				button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
				button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));
				button_next.setText("   ȷ��   ");
				button_flag = 2;
	        }
	       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
	       return true;
	    }
    }
	////////////////////////////////////
	class button_td_OnTouchListener implements OnTouchListener{
		public boolean onTouch(View v, MotionEvent event)
	    {
			if (event.getAction() == MotionEvent.ACTION_DOWN)//����
			{
				button_td.setTextColor(Color.parseColor("#000000"));
				button_td.setBackgroundColor(Color.parseColor("#FFFFFF"));
				
			}  
			if (event.getAction() == MotionEvent.ACTION_UP)//����
	        {
				button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
				button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_tote.setTextColor(Color.parseColor("#FFFFFF"));
				button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_txyz.setTextColor(Color.parseColor("#FFFFFF"));
				button_txyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_next.setText("   ȷ��   ");
				button_flag = 3;
	        }
	       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
	       return true;
	    }
    }
	////////////////////////////////////
	class button_txyz_OnTouchListener implements OnTouchListener{
		public boolean onTouch(View v, MotionEvent event)
	    {
			if (event.getAction() == MotionEvent.ACTION_DOWN)//����
			{
				button_txyz.setTextColor(Color.parseColor("#000000"));
				button_txyz.setBackgroundColor(Color.parseColor("#FFFFFF"));
				
			}  
			if (event.getAction() == MotionEvent.ACTION_UP)//����
	        {
				button_lxyz.setTextColor(Color.parseColor("#FFFFFF"));
				button_lxyz.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_tote.setTextColor(Color.parseColor("#FFFFFF"));
				button_tote.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_td.setTextColor(Color.parseColor("#FFFFFF"));
				button_td.setBackgroundColor(Color.parseColor("#d2aa3e"));	
				button_next.setText("   ����   ");
				button_flag = 4;
	        }
	       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
	       return true;
	    }
    }
//ȷ������һ����������************************************************************
	class button_last_OnTouchListener implements OnTouchListener{
		boolean buttonOk;
		public boolean onTouch(View v, MotionEvent event)
	    {
			if (event.getAction() == MotionEvent.ACTION_DOWN)//����
			{	
				buttonOk = false;//Ϊ��ÿ�ε��ִֻ��һ��
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
					button_next.setText("   ȷ��   ");
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
					button_next.setText("   ����   ");
					buttonOk = true;
					button_flag = 4; 
				}
	        }
	       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
	       return true;
	    }
    }
	////////////////////////////////////
	class button_next_OnTouchListener implements OnTouchListener{
		boolean buttonOk;
		public boolean onTouch(View v, MotionEvent event)
	    {
			if (event.getAction() == MotionEvent.ACTION_DOWN)//����
			{	
				buttonOk = false;//Ϊ��ÿ�ε��ִֻ��һ��
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
					button_next.setText(" ���� ");
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
					button_next.setText("   ������׼   ");
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
					button_next.setText("   ȷ��   ");
					
					buttonOk = true;
					button_flag = 3; 
				}
	        }
	       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
	       return true;
	    }
    }
	//����������*********************************************
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      // TODO Auto-generated method stub
    	switch(keyCode) {
    	case KeyEvent.KEYCODE_BACK ://ȷ�ϼ�
    		realButtonOk_back = false;//Ϊ��ÿ�ε��ִֻ��һ��
    		break;
    	case KeyEvent.KEYCODE_DPAD_LEFT :
    		realButtonOk_ok = false;//Ϊ��ÿ�ε��ִֻ��һ��
    		break;
    	case  KeyEvent.KEYCODE_HOME :
    		//realButtonOk = false;//Ϊ��ÿ�ε��ִֻ��һ��
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
					button_next.setText("   ����   ");
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
					button_next.setText("   ������׼   ");
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
					button_next.setText("   ȷ��   ");
					
					realButtonOk_ok = true;
					button_flag = 3; 
				}
	        }
            return true;         
      } else if (keyCode == KeyEvent.KEYCODE_BACK) {//���
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
					button_next.setText("   ȷ��   ");
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
					button_next.setText("   ����   ");
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
	///�����Ҳఴ��/////////////////////////////////
	//ȥ��
	class button_defog_OnClickListener implements OnCheckedChangeListener{
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {  
            if(isChecked){  
            	new Thread(new CommendSendThread("7")).start();
            	Toast.makeText(getApplicationContext(), "ȥ���ѿ���",Toast.LENGTH_LONG).show();
            	
                
            }else{  
            	new Thread(new CommendSendThread("8")).start(); 
            	Toast.makeText(getApplicationContext(), "ȥ���ѹر�",Toast.LENGTH_LONG).show();
            }  
      
	       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
	       //return true;
	    }
    }
	//״̬
	class button_state_OnTouchListener implements OnTouchListener{
		public boolean onTouch(View v, MotionEvent event)
	    {
			if (event.getAction() == MotionEvent.ACTION_DOWN)//����
			{
				new Thread(new CommendSendThread("1")).start();			
			}  
			if (event.getAction() == MotionEvent.ACTION_UP)//����
	        {
				AlertDialog_state ();
	        }
	       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
	       return true;
	    }
    }
	//ϵͳ����
		class button_set_OnTouchListener implements OnTouchListener{
			public boolean onTouch(View v, MotionEvent event)
		    {
				if (event.getAction() == MotionEvent.ACTION_DOWN)//����
				{
					
					
				}  
				if (event.getAction() == MotionEvent.ACTION_UP)//����
		        {
					
		        }
		       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
		       return true;
		    }
	    }
	//��������
		class button_zoomUp_OnTouchListener implements OnTouchListener{
			public boolean onTouch(View v, MotionEvent event)
		    {
				if (event.getAction() == MotionEvent.ACTION_DOWN)//����
				{
					new Thread(new CommendSendThread("9")).start();
					
				}  
				if (event.getAction() == MotionEvent.ACTION_UP)//����
		        {
					new Thread(new CommendSendThread("33")).start();
		        }
		       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
		       return true;
		    }
	    }
		//������Զ
		class button_zoomDown_OnTouchListener implements OnTouchListener{
			public boolean onTouch(View v, MotionEvent event)
		    {
				if (event.getAction() == MotionEvent.ACTION_DOWN)//����
				{
					
					new Thread(new CommendSendThread("10")).start();
				}  
				if (event.getAction() == MotionEvent.ACTION_UP)//����
		        {
					new Thread(new CommendSendThread("33")).start();
		        }
		       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
		       return true;
		    }
	    }
//ҡ��**********************************************
	class joyStickThread implements Runnable { 
		String SX;
		String SY;
		int IX=0;
		int IY=0;
		String X = "3";//������
		String Y = "3";//������
		int Xmax = 2950;
		int Xnom = 1720;	
		int Ymax = 2650;
		int Ynom = 1640;
		int s = 200;//��һ��
		int n = 800;//�ڶ���
		//int dX = Xmax-Xnom;
		//int dY = Ymax-Ynom;
		int d =1000;
		public void run() {
			//����ADC
	        adc.Open();
			while (true) {
				SX = readADC(0);//��ADC0��
				SY = readADC(1);//��ADC1��
				IX=Integer.valueOf(SX).intValue();
				IY=Integer.valueOf(SY).intValue();
			
				String Xnew = toStickX();
				String Ynew = toStickY();
				if ((Xnew != X) || (Ynew != Y)){
					new Thread(new CommendSendThread(Y+X)).start();
					if ((Xnew == "3") && (Xnew =="3")) {new Thread(new CommendSendThread("33")).start();}//��������ֹͣ
					X = Xnew;
					Y = Ynew;
				}
				
				Message msg = new Message();
				msg.what = 1;
				wifiHandler.sendMessage(msg);			
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO �Զ����ɵ� catch ��
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
			
			adc.Ioctl(channel,0xc000fa01);//��ͨ��0��1    //Դ��define ADC_SET_CHANNEL		0xc000fa01�����Ժ��������ѡ������ͨ���������ͨ��0
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
		private String toStickX () {//����ȼ�
			String z = "3";
			int q = IX - Xnom;
			
			if ((q >= -s)&(q <= s)) {z = "3";}
			else if ((q > s)&(q <= n)) {z = "4";}
			else if ((q < -s)&(q >= -n)) {z = "2";}
			else if ((q > n)) {z = "5";}
			else if ((q < -n)) {z = "1";}
			return  z;
			//7Ϊ����1Ϊ��
		}
		private String toStickY () {//����ȼ�
			String z = "3";
			int q = IY - Ynom;
		
			if ((q >= -s)&(q <= s)) {z = "3";}
			else if ((q > s)&(q <= n)) {z = "4";}
			else if ((q < -s)&(q >= -n)) {z = "2";}
			else if ((q > n)) {z = "5";}
			else if ((q < -n)) {z = "1";}			
			return  z;
			//7Ϊ����1Ϊ��
		}
	}
//������Ʋ���tcp����**************************************************************************
	 ///////////////////////////////////////////////////
	class forward_OnTouchListener implements OnTouchListener//����������������þͿ��Լ���������������Ĵ���
    {
      public boolean onTouch(View v, MotionEvent event)
      {
        if (event.getAction() == MotionEvent.ACTION_DOWN)//����
           {
        	forward.setImageResource(R.drawable.forward2);//���º�䰴��Ч��ͼƬ
        	String str = "61";	
     	    //sendControlData(str);	 
        	new Thread(new CommendSendThread(str)).start();
     	  
            }  
       if (event.getAction() == MotionEvent.ACTION_UP)//����
          {
    	   forward.setImageResource(R.drawable.forward1);
    	   String str = "60";	
    	   //sendControlData(str);	
    	   new Thread(new CommendSendThread(str)).start();
    	   
          }
       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
       return true;
      }
    }
 
    ////////////////////////////////////
	class backward_OnTouchListener implements OnTouchListener
    {
      public boolean onTouch(View v, MotionEvent event)
      {
        if (event.getAction() == MotionEvent.ACTION_DOWN)//����
           {
        	backward.setImageResource(R.drawable.backward2);
        	String str = "62";	
     	    //sendControlData(str);	
        	new Thread(new CommendSendThread(str)).start();
            }  
       if (event.getAction() == MotionEvent.ACTION_UP)//����
          {
    	   backward.setImageResource(R.drawable.slide_one);
    	   String str = "60";	
    	   //sendControlData(str);	
    	   new Thread(new CommendSendThread(str)).start();
          }
       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
       return true;
      }
    }
 
    ////////////////////////////////////
	class left_OnTouchListener implements OnTouchListener
    {
    
      public boolean onTouch(View v, MotionEvent event)
      {
        if (event.getAction() == MotionEvent.ACTION_DOWN)//����
           {
        	left.setImageResource(R.drawable.left2);
        	String str = "63";	
     	    //sendControlData(str);	
        	new Thread(new CommendSendThread(str)).start();
            }  
       if (event.getAction() == MotionEvent.ACTION_UP)//����
          {
    	   left.setImageResource(R.drawable.left1);
    	   String str = "60";	
    	   //sendControlData(str);	
    	   new Thread(new CommendSendThread(str)).start();
          }
       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
       return true;
      }
    }
 
    ////////////////////////////////////
	class right_OnTouchListener implements OnTouchListener
    {
   
      public boolean onTouch(View v, MotionEvent event)
      {
        if (event.getAction() == MotionEvent.ACTION_DOWN)//����
           {
        	right.setImageResource(R.drawable.right2);
        	String str = "64";	
     	    //sendControlData(str);	
        	new Thread(new CommendSendThread(str)).start();
            }  
       if (event.getAction() == MotionEvent.ACTION_UP)//����
          {
    	   right.setImageResource(R.drawable.right1);
    	   String str = "60";	
    	   //sendControlData(str);	
    	   new Thread(new CommendSendThread(str)).start();
          }
       //����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ϵͳ��ͬʱ�����Ӧ���¼�
       return true;
      }
    }
//��������ź�UDP�������**************************************************************************
	void sendControlData(String str)
	{
		try{
			 boolean flag = true;
			 portRemoteNum=9000;//Զ�̶˿�
			 portLocalNum=9500;//�����˿�
	         dsCtl  = new DatagramSocket(portLocalNum);//��������UDP��������ڲ����ñ����˿�
             } catch (Exception e) {
       e.printStackTrace();}			
		try {			      		    
		    InetAddress serverAddress = InetAddress.getByName(ipname);	 //cb?  		    
		    byte data [] = str.getBytes(); 		    
		    dpCtl = new DatagramPacket(data,data.length,serverAddress,portRemoteNum);	//��Ҫ�������ݴ�����������IP�Ͷ˿�
		    //�ӱ��ض˿ڸ�ָ��IP��Զ�̶˿ڷ����ݰ�		    
		    //lock.acquire();
		    //ֻҪ���õ����紫���Ҫ�õ��̣߳����������߳�
		    SendControlDataThread SCDT = new SendControlDataThread();
	        Thread TSCDT = new Thread(SCDT);
	        TSCDT.start();		   
		    //lock.release();		    
		    } catch (Exception e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }		
	}
	//��������ź�UDP�����̲߳���**************************************************************************	
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
	//��Ϣ��ʾ��**************************************************************************
	public void AlertDialog (String Title,String Message,final boolean isExit) {	
		new android.app.AlertDialog.Builder(BitCameraClient.this)  		
		                .setTitle(Title)		
		                .setMessage(Message)		
		                .setPositiveButton("ȷ��",new OnClickListener()
		    			{	    			//��ȷ�����˳�	
		    				public void onClick(DialogInterface dialog, int which)
		    				{
		    					if (isExit == true) {
		    					System.exit(1);
		    					}
		    				}
		    			})		
		                .show();
 
	}
	//״̬��Ϣ��ʾ��********************************************************	
	public void AlertDialog_state () {
		boolean[] x = {true,true,true,true,true};

		AlertDialog alertDialog = new android.app.AlertDialog.Builder(BitCameraClient.this)  							
		 //.setMultiChoiceItems(new String[] {"����ģ��","���ģ��","Ѱ��ģ��","��λģ��","������ģ��" },x,null)
		 .setItems(new  String[] {"����ģ��        "+state_image,"���ģ��        "+state_distant,"Ѱ��ģ��        "+state_north,"��λģ��        "+state_GPS,"������ģ��        "+state_CPU,"����¶�        "+state_temp+"��C"},  null )
		 .setPositiveButton("ȷ��", null)
		 .create();   
	    Window window = alertDialog.getWindow();   
	    WindowManager.LayoutParams params = window.getAttributes();   	     	   
        //�������ص�����               
        params.systemUiVisibility =View.SYSTEM_UI_FLAG_FULLSCREEN |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;  
        // ����͸����Ϊ0.3   
        params.alpha = 0.8f;
        //���ÿ��
        //params.width = 300 ;
        //params.width = LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);  
        //alertDialog.getWindow().setAttributes(params); 
		alertDialog.show(); 
		//alertDialog.getWindow().setLayout(300, 200); 
	}
	//��ȡ������wifiǿ��*******************************************************
	class showWifiThread implements Runnable {
		private long total_data_down = TrafficStats.getTotalRxBytes();
		private long total_data_up = TrafficStats.getTotalTxBytes();
	    private final int count = 1; //����ˢ��һ��
	    private int level;                      //�ź�ǿ��ֵ  
 
		public void run() {
		
			while(true){
			android.os.Process.setThreadPriority(19);//os��ͼ�	
			//wifiHandler.postDelayed(SWT, count * 1000);//��ʱ�� ��������ѭ���߳������ʱ whileҲ����Ҫ�� �ƺ�����sleep
			//Log.i("222222222222222222222", "2222222222222222222222222");
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();  
            //����ź�ǿ��ֵ  
            level = wifiInfo.getRssi();  
            
			Message msg = new Message();
			msg.what = 1;
			//����ÿ����������
			long traffic_data_down = TrafficStats.getTotalRxBytes() - total_data_down;
			total_data_down = TrafficStats.getTotalRxBytes();
			//����ÿ���ϴ�����
			long traffic_data_up = TrafficStats.getTotalTxBytes() - total_data_up;
			total_data_up = TrafficStats.getTotalTxBytes();
			
			msg.arg1 = (int) (traffic_data_down /count) ;
			msg.arg2 = (int) (traffic_data_up /count) ;
			wifiHandler.sendMessage(msg);	
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}}
      
		private Handler wifiHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);				
				switch (msg.what) {
				case 1:
					
					textView_wifi.setText(" " + "WIFIǿ��:" + level + "dbm" + " ");
					//Log.i("1111111111111111", "11111111111111111111111111111111");
					if((msg.arg1 > 1024) && (msg.arg2 > 1024)) {
						textView_rate.setText(" " + "����:" + msg.arg1 / 1024 + "kb/s" + " "+ "����:" + msg.arg2 / 1024 + "kb/s" + " ");					
					}
					if((msg.arg1 > 1024) && (msg.arg2 < 1024)) {
						textView_rate.setText(" " + "����:" + msg.arg1 / 1024 + "kb/s" + " "+ "����:" + msg.arg2 + "b/s" + " ");					
					}
					if((msg.arg1 < 1024) && (msg.arg2 > 1024)) {
						textView_rate.setText(" " + "����:" + msg.arg1 + "b/s" + " " + "����:" + msg.arg2 / 1024 + "kb/s" + " ");					
					}
					if((msg.arg1 < 1024) && (msg.arg2 < 1024)) {
						textView_rate.setText(" " + "����:" + msg.arg1 + "b/s" + " "+ "����:" + msg.arg2 + "b/s" + " ");
					}
					break;
				default:
					break;
				}
			}
		};
	}
	//��ȡ��ص���****************************************************
	/**���ܵ����ı�㲥*/  
    class BatteryBroadcastReceiver extends BroadcastReceiver{  
          
        @Override  
        public void onReceive(Context context, Intent intent) {  
              
            if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){  
                  
                int level = intent.getIntExtra("level", 0);  
                int scale = intent.getIntExtra("scale", 100);  
                int powerPercent = level*100 /scale;
                textView_lbat.setText(" ����������" + powerPercent + "% ");
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
    
	//���Ի�ȡ����IP**********************************************
 public String getLocalIpAddress() {
    
        String ipaddress = "";
        try
        {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // �������õ�����ӿ�
            while (en.hasMoreElements())
            {
                NetworkInterface nif = en.nextElement();// �õ�ÿһ������ӿڰ󶨵�����ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // ����ÿһ���ӿڰ󶨵�����ip
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
            Log.e("feige", "��ȡ����ip��ַʧ��");
            e.printStackTrace();
        }
        return ipaddress;

    }

    // �õ�����Mac��ַ
    public String getLocalMac()
    {
        String mac = "";
        // ��ȡwifi������
        WifiManager wifiMng = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfor = wifiMng.getConnectionInfo();
        mac = wifiInfor.getMacAddress();
        return mac;
    }
//adc���ÿ�**************************************
	static {
        System.loadLibrary("adc");
	}
	
//�Զ�������������**********************************************************
	public boolean onTouchEvent(MotionEvent event) {
	// �������ж�һ������ǰ��²����ͻ�ȡ����Ȼ��ִ�з���
	if (event.getAction() == MotionEvent.ACTION_DOWN) {
		touchTrace(event.getX(), event.getY());
		
	}
	return super.onTouchEvent(event);
	}
//����㰴���ٽǶȲ�����****************************************	
	public float touchTrace(float x, float y ) {
		//x,y�ֵ㰴λ��
		int xMax = 1024;//��ĻX������
		int yMax = 768;//��Ļy������ ��4.4.4Ϊ768,4.0.3Ϊ716
		int xMid = 512;//��Ļ���ĵ�
		int yMid = 384;//��Ļ���ĵ�		4.4.4Ϊ384,4.0.3Ϊ358
		float angleXmax = 24;//����ͷX�ӳ��ǵ�һ��
		float angleYmax = 18;//����ͷY�ӳ��ǵ�һ��
		float angleX = 0;
		float angleY = 0;
	
		if (true) {//x > 80 && y > 300 && x <200 && y < 600
			double cX = (Math.tan(Math.toRadians(angleXmax)))/(xMax-xMid);//���㳣�� ���ⷴ������  ����Ҳ���õĻ���,����Ҫת����
			double cY = (Math.tan(Math.toRadians(angleYmax)))/(yMax-yMid);//���㳣�� ���ⷴ������
			if (x != xMid) {
				angleX = (float)Math.toDegrees(Math.atan(cX*(x-xMid)));//�����еõ����ǻ���,����ת�Ƕ�
			}
			if (y != yMid) {
				angleY =  (float)Math.toDegrees(Math.atan(cY*(-(y-yMid))));//ȡ����Ϊ��Ļy�ᷴ��
			}	
			new Thread(new CommendSendThread("@tX" + String.valueOf(angleX) + "@tY" + String.valueOf(angleY))).start();
			Toast.makeText(getApplicationContext(), " X��" + x + " Y:" + y + " X�ǣ�"+ angleX + " Y�ǣ�"+ angleY + " ",Toast.LENGTH_SHORT).show();

		}
		
		return angleX + angleY;
		


		
	}

		
}




