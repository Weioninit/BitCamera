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
	boolean isPreview = false;        //�Ƿ��������
	private String ipname ;
	private TextView TextView;
	private String text;
	private ServerSocket ssVideo = null; 
	private ServerSocket ssText = null;
	private static final int msgKey1 = 1;
	
	private SendVideoThread SendVideoThread = null;
    public static  VideoEncodeH264 VideoEncodeH264 = null;//��֪Ϊʲô�������ﻹ��static������dos.write(VideoEncode.yuv420sp, 0, 28800);����
    public ImageButton forward;
	public ImageButton backward;
	public ImageButton left;
	public ImageButton right;
	public WifiManager.MulticastLock lock= null;
	public YuvImage image = null;
	public Size size = null;
	private int isH264 = 0;//����H264����JPEG
	EncodeH264 EncodeH264;
	private int width = 640;
	private int height = 480;
	private static final int FRAME_RATE = 17;	
	private int bitrate = 2500000;//����������125000
	private byte[] h264 = new byte[width*height*3/2];
	private DatagramSocket dsVideo;
	private int ret;
	private InetAddress address = null;
	private byte[] buf = null;
	DatagramPacket packet =null;
	public boolean ipOK = false;
	private int isTCP = 1; //0Ϊudp��1Ϊtcp
	ServerSocket ssIP = null;
	private DatagramSocket dsText;
	private DatagramPacket dpText;
	int powerPercent;
	Thread T5 ;
	boolean clientOut = true;
	private String angleX; 
	private String angleY;
	serial com3 = new serial();
	float Xval;//��̨��ǰX�Ƕ�
	float Yval;//��̨��ǰY�Ƕ�
	private int[] ReceiveStr = new int[7];//��̨���ص�������
	private OnDataReceiveListener onDataReceiveListener = null; 

     boolean uartOK = false;
     boolean isTouch = false;
     
 	boolean isDehazing=false;
 	public int[] array = new int[width*height*3/8];
	//ģ�����***************************************
	String local_xyz = "X:116.3041100919 Y:39.9634188833 Z:45.21";
	String target_orientation = "0";
	String target_distant = "533.32";
	String target_elevation = "0";
	String target_xyz = "X:116.3126221529 Y:39.9254188211 Z:47.32";
	String state_image = "����";//ͼ��ģ��
	String state_distant = "����";//������ģ��
	String state_north = "����";//Ѱ����ģ��
	String state_GPS = "����";//GPS��λģ��
	String state_CPU = "����";//��ؼ����ģ��
	String state_temp = "40";//�¶�
	//********************************************
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        seeting();	
		launch();
    }
	//��ʼ������**************************************************************************
	private void seeting() {
		
		// ����ȫ��
        //getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
     	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);       
        
        TextView = (TextView)findViewById(R.id.textView1);
        forward = (ImageButton)findViewById(R.id.forward);//���ð�ť������
		//forward.setOnTouchListener(new forward_OnTouchListener());//�����Ժ���������ж���
        
        backward = (ImageButton)findViewById(R.id.backward);
        //backward.setOnTouchListener(new backward_OnTouchListener()); 
        
        left = (ImageButton)findViewById(R.id.left);
        //left.setOnTouchListener(new left_OnTouchListener());
        
        right = (ImageButton)findViewById(R.id.right);
        //right.setOnTouchListener(new right_OnTouchListener()); 
        
//		screenWidth = 640;//������õ�����Ƭ��С��Ԥ����С���滹�в���
//		screenHeight = 480;		
		mSurfaceView = (SurfaceView) findViewById(R.id.sView);                  // ��ȡ������SurfaceView���		
		mSurfaceHolder = mSurfaceView.getHolder();                               // ���SurfaceView��SurfaceHolder
		//����������Ϊ��ĳЩ����ر���UDP	����Ҫʱ����lock.acquire()
		WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		lock= manager.createMulticastLock("test wifi");
		
						
		//��ص�������
        registerReceiver(new BatteryBroadcastReceiver(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); 
        
		// ΪsurfaceHolder���һ���ص�������
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
						"SurfaceHolder.Callback��Surface Destroyed");
				// ���camera��Ϊnull ,�ͷ�����ͷ
				if (mCamera != null) {
					if (isPreview) {
						mCamera.setPreviewCallback(null); // �������������ǰ����Ȼ�˳�����
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
		// ���ø�SurfaceView�Լ���ά������    
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	} 
	//��ʼ������**************************************************************************
	public void launch() {	//С������
//		//����UDP��ʾ���������߳�**************************************************************************
//		ShowTextThread STT = new ShowTextThread();
//        Thread T3 = new Thread(STT);     
//        T3.start();
		
        //������ȡIP�߳�****************************************************************************
		try {
			ssIP = new ServerSocket(4500);//����
		} catch (BindException e) {
			AlertDialog("�˿�ʹ����","��ص���س����������з�������",true);
			//System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
        IpThread IT = new IpThread();
        Thread T6 = new Thread(IT);
        T6.start();
        //����tcp�������߳�****************************************************************************
        CommendReceiveThread CRT = new CommendReceiveThread();
        Thread T7 = new Thread(CRT);
        T7.start();
        //�������UDP�����߳�**************************************************************************
        ReceiveControlData RCD = new ReceiveControlData();
        Thread T4 = new Thread(RCD);
        //T4.setPriority(Thread.MIN_PRIORITY);
        //android.os.Process.setThreadPriority(19);//os��ͼ�
        //T4.start();
        //��������*********************************************************************************
        com3.Open(3, 9600);
        //�������������ѯ��̨λ���߳�************************************
        ChkVal CV = new ChkVal();
        Thread T8 = new Thread(CV);
        T8.start();
        //�������մ����̲߳������ѯ��̨λ��*****************************************************
	   	uartOK = false;
	   	new Thread(new ReadUartThread()).start();
        

        //�����������ݴ����߳�**************************************************************************
//        SendTextThread ST = new SendTextThread();
//        Thread T2 = new Thread(ST);
//        T2.start();
//		


	}
	//�������**************************************************************************
	private void initCamera() {
		int mPreviewHeight;
		int mPreviewWidth;
    	if (!isPreview) {
			mCamera = Camera.open();
		}
		if (mCamera != null && !isPreview) {
			
			try{
				 // �����ԡ���ȡcaera֧�ֵ�PictrueSize�������ܷ����ã���
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
			    // �����ԡ����ú��ͼƬ��С��Ԥ����С�Լ�֡��
			    Camera.Size csize = mCamera.getParameters().getPreviewSize();
			    mPreviewHeight = csize.height; //
			    mPreviewWidth = csize.width;
			    Log.i("parameters"+"initCamera", "after setting, previewSize:width: " + csize.width + " height: " + csize.height);
			    csize = mCamera.getParameters().getPictureSize();
			    Log.i("parameters"+"initCamera", "after setting, pictruesize:width: " + csize.width + " height: " + csize.height);
			    Log.i("parameters"+"initCamera", "after setting, previewformate is " + mCamera.getParameters().getPreviewFormat());
			    Log.i("parameters"+"initCamera", "after setting, previewframetate is " + mCamera.getParameters().getPreviewFrameRate());	
			    //�����ԡ�����***************************		
			    Camera.Parameters parameters = mCamera.getParameters();				
				parameters.setPreviewSize(width, height);    // ����Ԥ����Ƭ�Ĵ�С				
				parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
				parameters.setPreviewFpsRange(20,25);                    // ÿ����ʾ20~30֡���Ժ���Լ�С����				
				if (isH264 == 1) {
					parameters.setPictureFormat(PixelFormat.JPEG);           // ����ͼƬ��ʽ����,ԭ����ImageFormat.NV21				
					parameters.setPreviewFormat(ImageFormat.YV12);//Ӧ�ø�
				}//JPEG����ϵͳĬ��
				//parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);	//ԭ��û�У�����ӵ�										    
				//parameters.setPictureSize(screenWidth, screenHeight);    // ������Ƭ�Ĵ�С
				parameters.setPictureSize(width, height);
				parameters.setPreviewSize(width, height); // ָ��preview�Ĵ�С 
				//���������� ����������������õĺ���ʵ�ֻ��Ĳ�һ��ʱ���ͻᱨ��
												
				mCamera.setParameters(parameters);						// ���������ʵ�����������
				mCamera.setPreviewDisplay(mSurfaceHolder);                 // ͨ��SurfaceView��ʾȡ������						        
		      	//byte[] buf = new byte[screenWidth * screenHeight * 3 / 2]; 
		        buf = new byte[width * height * 3 / 2]; 
		        mCamera.addCallbackBuffer(buf);//���ӻ�����
		        if (isH264 == 1) {
					/* ��Ƶ�����봦�� */
					//��Ӷ���Ƶ��������
					mCamera.setPreviewCallback(new VideoEncodeH264());         // ���ûص�����	
					//mCamera.setPreviewCallbackWithBuffer(new VideoEncodeH264());//��Ӷ���Ƶ�ı��봦��setPreviewCallbackWithBuffer�Ǻ��б�Ҫ�ģ���Ȼÿ�λص�ϵͳ�����·��仺������Ч�ʻ�ܵ͡�
				}
		        else {
		        	mCamera.setPreviewCallback(new VideoEncodeJPEG());
		        	//mCamera.setPreviewCallbackWithBuffer(new VideoEncodeJPEG()); //�Ժ�����
		        }
				mCamera.startPreview();                                   // ��ʼԤ��				
				mCamera.autoFocus(null);                                  // �Զ��Խ�				
			} catch (Exception e) {
				e.printStackTrace();
			}
			 // �����ԡ����ú��ͼƬ��С��Ԥ����С�Լ�֡��
		    	  
			isPreview = true;
		}
    }
//��ȡ�Է�ip���Ӳ�����һ������**************************************************************************************
	class IpThread implements Runnable{
		public void run() {
			while (true) {
				try {
					Socket s = ssIP.accept();
					clientOut = false;
					//ipname = s.getInetAddress().getHostAddress();//��ÿͻ���IP
					DataInputStream dis = new DataInputStream(s.getInputStream());
					ipname = dis.readUTF();
					//address = InetAddress.getByName(ipname);
					address = InetAddress.getByName(ipname);
					//address = InetAddress.getByName("192.168.31.129");

					ipOK = true;//������Ҫ���IP�ı��벻�ϻ�ȡ�µģ���Ϊ�п�������������IP
					
					Log.i("getipname", "Client ipname = " + ipname);
					
					//�ͻ�������ʱ��Ҫ���͹�ȥ������**********************��Ϊ������������ÿ�������Ӷ�����һ�Σ����Է�������֮����������
					new Thread(new TcpSendThread("@bat" + Integer.toString(powerPercent))).start();
					//new Thread(new TcpSendThread(�� ��))).start();
					
						//����ͼ���������**************************************************************************
				        if (isH264 == 1) {//H264
					        EncodeH264 = new EncodeH264(width,height,FRAME_RATE,bitrate);
					        try {
								dsVideo = new DatagramSocket();
							} catch (SocketException e) {
								e.printStackTrace();
							}
				        }
				        else {//JPEG
							//�����߳̽�ͼ�����ݷ��ͳ�ȥ
							SendVideoThread RVT = new SendVideoThread();
							T5 = new Thread(RVT);
							
							//T5.setPriority(Thread.MAX_PRIORITY);		
							T5.start();		
				        }
				        
					//******************************************
				} catch (EOFException e) {// ��ЩΪ��client�Ǳ���Ϊ�����Ϊ�Ĺر�ʱ������ߵ�socket��DataInputStream�ر�
					Log.i("SendTextThread", "A Text client close!");
				} catch (UnknownHostException e) {
					// TODO �Զ����ɵ� catch ��
					e.printStackTrace();
				} catch (IOException e) {
					// TODO �Զ����ɵ� catch ��
					e.printStackTrace();
				} 
			}
		  
		}
	}
//TCP��ָ�����ݲ�ִ��**************************************************************************************	
	class CommendReceiveThread implements Runnable{
		ServerSocket ss;
		int a = -1;

		public void run() {
			try {
				ss = new ServerSocket(5000);
			} catch (IOException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
			while (true) {
				try {
					Socket s = ss.accept();
					DataInputStream dis = new DataInputStream(s.getInputStream());
					String str= dis.readUTF();	
					Log.i("3333333", "touch OK");
					//�ж��ǲ��Ƿ����Ĵ����Ƕ�
					if((a = str.indexOf("@tX")) >= 0) {//��ȡ��־
						int b =str.indexOf("@tY");
						angleX = str.substring(a + "@tX".length(), b);
						angleY = str.substring(b + "@tY".length(), str.length());							
						Message msg = new Message();
						msg.what = 100;//�Ѵ������ݰ�װ��message
						mHandler.sendMessage(msg);	
					}
					else  {
						int x = Integer.parseInt(str);//stringתint
						Message msg = new Message();
						msg.what = x;//�Ѵ������ݰ�װ��message
						mHandler.sendMessage(msg);	
					}
				} catch (IOException e) {
					// TODO �Զ����ɵ� catch ��
					e.printStackTrace();
				}
			}
		}
		private Handler mHandler = new Handler() {
			int[] go;
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);				
				switch (msg.what) {//������string����
				
				case 1://����״ָ̬��
					new Thread(new TcpSendThread("@si" + state_image + "@sd" + state_distant + "@sn" + state_north + "@sg" + state_GPS + "@sc" + state_CPU + "@st" + state_temp)).start();
					//new Thread(new TcpSendThread("@state" + state_image + state_distant + state_north + state_GPS + state_CPU + "@stemp" + state_temp)).start();
					break;
				case 2://������������߳�ָ��
					new Thread(new TcpSendThread("@lxyz" + local_xyz)).start();//��lxyz�������жϱ�־
					Log.i("4444444444", "touch OK");
					break;
				case 3://����Ŀ�귽λ�Ǹ�����ָ��
					uartOK =false;
					new Thread(new ReadUartThread()).start();
					new Thread(new TcpSendThread("@to" + target_orientation + "@te" + target_elevation)).start();
					break;
				case 4://����Ŀ�����ָ��
					new Thread(new TcpSendThread("@td" + target_distant)).start();
					break;
				case 5://����Ŀ������߳�ָ��
					new Thread(new TcpSendThread("@txyz" + target_xyz)).start();
					break;
				case 6://���͵���ָ��ָ��
					new Thread(new TcpSendThread("@send" + "���ͳɹ�")).start();
					break;
				case 7://����ȥ��ָ��
					 isDehazing=true;
					break;
				case 8://����ֹͣȥ��ָ��
					isDehazing=false;
					break;
				case 9://���ս�������
					go =  CreateCommandStr(0x00, 0x20, 0x00, 0x00);					
	 				com3.Write(go,go.length);
					break;
				case 10://���ս���Զ��
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
				case 13://��ȫ��
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
				case 33://��ȫֹͣ
					go =  CreateCommandStr(0x00, 0x00, 0x00, 0x00);					
	 				com3.Write(go,go.length);
	 				new Thread(new ReadUartThread()).start();//���󲢷�����̨λ��
					break;	
				case 34:
					go =  CreateCommandStr(0x00, 0x02, 0x1E, 0x00);					
	 				com3.Write(go,go.length);	
					break;	
				case 35://��ȫ��
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
				case 53://��ȫ��
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
	 				new Thread(new ReadUartThread()).start();//���󲢷�����̨λ��
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
				case 100://���տ���׷��
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
//TCP������*********************************************************************
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

		public void run() {//����ָ��new Thread(new TcpSendThread("1")).start();
		
			while (!ipOK) {/* �ȴ����IP */ };
			if (time == 0) {//����ѭ����
				try {
					Socket s = new Socket(ipname, port);
					DataOutputStream dos = new DataOutputStream(s.getOutputStream());
					dos.writeUTF(str);// дһ��UNICODE���͵��ַ���
					dos.flush();
					dos.close();
					s.close();//ÿ�η�����Ͽ�����Ӱ���´η���
					
				} catch (IOException e) {
					// TODO �Զ����ɵ� catch ��
					e.printStackTrace();
				}
			}
			if(time != 0) {//��ѭ����
				while(true) {
					try {
						Socket s = new Socket(ipname, port);
						DataOutputStream dos = new DataOutputStream(s.getOutputStream());
						
						dos.writeUTF(str);// дһ��UNICODE���͵��ַ���
						dos.flush();
						dos.close();
						s.close();//ÿ�η�����Ͽ�����Ӱ���´η���
						Thread.sleep(time);
					} catch (IOException e) {
						// TODO �Զ����ɵ� catch ��
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO �Զ����ɵ� catch ��
						e.printStackTrace();
					}
				}
			}
		}
	}
//��Ƶ����H264**************************************************************************
	class VideoEncodeH264 implements Camera.PreviewCallback {// �ûص������Զ��������ݱ仯ѭ��ִ��
		// public byte[] yuv420sp = null;
		public void onPreviewFrame(byte[] data, Camera camera) {// ��Ƶ�����Զ�����data��
			// Log.v("h264", "h264 start");
			ret = EncodeH264.offerEncoder(data, h264);
			// mCamera.addCallbackBuffer(buf);
			if (ret > 0)// ����Ƿ�����ͷpps sps���������ٴ�
			{
				while (!ipOK) {/* �ȴ����IP */ };//��������������������������
				packet = new DatagramPacket(h264, ret, address, 6500);

				SendVideoThread RVT = new SendVideoThread();
				Thread T1 = new Thread(RVT);
				android.os.Process.setThreadPriority(-20);//os��߼�
				T1.setPriority(Thread.MAX_PRIORITY);
				T1.start();//***��onPreviewFrame�µ��ǲ���ִ�еģ�����������û�����⣬���������Լ���ʱ���߼�ⷢ��������ִ�У��Ӹ��������
//				try {
//					Thread.sleep(5);���ܼ�Thread.sleep(5);����������߳��ӳ��ˡ�
//				} catch (InterruptedException e) {
//					// TODO �Զ����ɵ� catch ��
//					e.printStackTrace();
//				}
			}
			// Log.v("h264", "h264 end"+ ",ret = " + ret);
			// Log.v("h264", "h264 end"+ ", data =" + data);
		}

	}
//��Ƶ����JPEG**************************************************************************
	class VideoEncodeJPEG implements Camera.PreviewCallback {
		
		
	    public void onPreviewFrame(byte[] data, Camera camera) {//��Ƶ�����Զ�����data��
	    	 size = camera.getParameters().getPreviewSize();      
	    	
	    	 if(!clientOut) {
	        	//����image.compressToJpeg������YUV��ʽͼ������dataתΪjpg��ʽ
	            try {
	            	//Log.i("00000", "video");
	            	image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
	            	//Log.i("gaozw", "Welcome dehazing thread");
    	          if (isDehazing==true) {
                	
                	//Log.i("gaozw", "Welcome dehazing thread");
                	array=convert(data);   //���ػ��YUV byte��ת��Ϊint���ȴ�ȥ����
    	         }
	            }catch(Exception ex){  
	                Log.e("Sys","Error:"+ex.getMessage());  
	            }   
	    	 }	            
	    }
	}
	//ȥ�����غ���************************************************************************
		/*
		 * ȥ���� DehazingImg
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
			     * ͼƬ�����ŷ���
			     *
			     * @param bgimage
			     *            ��ԴͼƬ��Դ
			     * @param newWidth
			     *            �����ź���
			     * @param newHeight
			     *            �����ź�߶�
			     * @return
			     */
			    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
			                                   double newHeight) {
			        // ��ȡ���ͼƬ�Ŀ�͸�
			        float width = bgimage.getWidth();
			        float height = bgimage.getHeight();
			        // ��������ͼƬ�õ�matrix����
			        Matrix matrix = new Matrix();
			        // ������������
			        float scaleWidth = ((float) newWidth) / width;
			        float scaleHeight = ((float) newHeight) / height;
			        // ����ͼƬ����
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
			        	byteArr[0 + offset]=(byte) ((buf[i]& 0xFF000000)>>24);  //ȡ����λ
			        	byteArr[1 + offset]=(byte) ((buf[i]& 0x00FF0000)>>16);
			        	byteArr[2 + offset]=(byte) ((buf[i]& 0x0000FF00)>>8);
			        	byteArr[3 + offset]=(byte) ((buf[i]& 0x000000FF));		        					        			      
			            offset += 4;
			        }
			        return byteArr;
			    }	
//��Ƶ�������**************************************************************************	
	class SendVideoThread implements Runnable{
		Socket s = null;
		private byte byteBuffer[] = new byte[1472];//ԭ����1024
		int portRemoteNum=7500;//Զ�̶˿�
		int portLocalNum=7000;//�����˿�
		//DataOutputStream dos = null;				
		public void run() {			
			android.os.Process.setThreadPriority(-20);//os��߼�
			if (isH264 == 1) {//H264����				
				try {
					dsVideo.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
				//Log.v("h264", "h264 send");					
			}
			else {//JPEG����

				while (true) {//�ж��Ƿ�ֹͣ�߳�!T5.isInterrupted()
					if(image!=null){
						try {
							//ipname = "192.168.23.1";
							//Socket s = new Socket("192.168.31.129", 6000);
							
							
							while (!ipOK) {/* �ȴ����IP */ };	
							if(isTCP == 1) {
								s = new Socket(ipname, 6000);						
								//s = ssVideo.accept();
								//Log.i("SendVideoThread", "A Video client connected!");
			/*gaozw���ȥ�����������*/
								
								if(isDehazing ==true && array!=null){
						
									
									ByteArrayOutputStream stream = new ByteArrayOutputStream();
								
						
									Log.i("dehazing processing", "dehazing begins");
									int[] buf=Dehazing.OutPut(array, 0.75f, width, height, 1);   //ȥ��������һ��ʱ�䣬�˳�
									byte[]  byte_YUV=convertInt2Byte(buf);                    //ȥ�������byte�ͣ�ת��Ϊint
									YuvImage  image_dehazed = new YuvImage(byte_YUV, ImageFormat.NV21, width,height, null);
						//			 image.compressToJpeg(new Rect(0, 0, size.width, size.height), 60, stream); 
									Log.i("dehazing processing", "dehazing ends");
			                   
			                        image_dehazed.compressToJpeg(new Rect(0, 0, size.width, size.height), 60, stream);
				              
					                OutputStream outsocketDehazing = s.getOutputStream();
									ByteArrayInputStream inputstreamDehazing = new ByteArrayInputStream(stream.toByteArray());
									
									int amount;
									//Log.v("JPEG", "amount = " + amount);
									//����ѭ���ǲ��ϼ�����byteBuffer����û�н�����û�����ͷ��ͣ��������˳�ѭ��������byteBuffer��С�����˷��Ͱ��Ĵ�С
									while ((amount = inputstreamDehazing.read(byteBuffer)) != -1) {//read()����ֵΪ-1��ʾ�Ƕ����ļ�������ˣ�read���������ֽ���
									    outsocketDehazing.write(byteBuffer, 0, amount);//һ��ͼ��һ����
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
									//����ѭ���ǲ��ϼ�����byteBuffer����û�н�����û�����ͷ��ͣ��������˳�ѭ��������byteBuffer��С�����˷��Ͱ��Ĵ�С
									while ((amount = inputstream.read(byteBuffer)) != -1) {//read()����ֵΪ-1��ʾ�Ƕ����ļ�������ˣ�read���������ֽ���
									    outsocket.write(byteBuffer, 0, amount);//һ��ͼ��һ����
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
//						        DatagramSocket dsJPEG = new DatagramSocket(portLocalNum);//��������UDP��������ڲ����ñ����˿�
//						        		    
//								byte data [] = str.getBytes();
//								ByteArrayOutputStream bos = new ByteArrayOutputStream();
//								image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, bos); 
//								
//								ByteArrayInputStream inputstream = new ByteArrayInputStream(bos.toByteArray());
//								int amount;
//								//Log.v("JPEG", "amount = " + amount);
//								//����ѭ���ǲ��ϼ�����byteBuffer����û�н�����û�����ͷ��ͣ��������˳�ѭ��������byteBuffer��С�����˷��Ͱ��Ĵ�С
//								while ((amount = inputstream.read(byteBuffer)) != -1) {//read()����ֵΪ-1��ʾ�Ƕ����ļ�������ˣ�read���������ֽ���
//								    outsocket.write(byteBuffer, 0, amount);//һ��ͼ��һ����
//								    //Log.v("JPEG", "amount2 = " + amount);
//								}
//								//Log.v("JPEG", "byterBuffer" + byteBuffer.length);
//								bos.flush();	
							}
						} catch (IOException e) {
							// TODO �Զ����ɵ� catch ��
							e.printStackTrace();
							Log.i("errrrrrrrrrrrrr", "A client close!");
							clientOut = true;
							//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!����Ͽ���������
							int[] stop =  CreateCommandStr(0x00, 0x00, 0x00, 0x00);					
			 				com3.Write(stop,stop.length);
			 				isDehazing = false;
							//T5.interrupt();//ֹͣ�̣߳�����break
							break;
							
						} 			        
					}
				}
			}
		}
	}


//��������ź�UDP���ղ���**************************************************************************
	class ReceiveControlData implements Runnable {
		int portLocalNum = 9000;//�����˿�
		DatagramSocket ds = null;
		DatagramPacket dp = null;
		
		public void run() {
			byte data[] = new byte [1024];		
			try {
				dp = new DatagramPacket(data,data.length);
				ds = new DatagramSocket(portLocalNum);//��������UDP��������ڲ����ñ����˿�
			} catch (SocketException e) {
				e.printStackTrace();
			}
			while(true) {				
				try {
					//lock.acquire();
					ds.receive(dp);		
					//lock.release();
					String str = new String(data,0,dp.getLength());	//ȡ�����ʹ���Ϊstring
					int s = Integer.parseInt(str);//stringתint
					Message msg = new Message();
					msg.what = s;//�Ѵ������ݰ�װ��message
					mHandler.sendMessage(msg);					
				} catch (IOException e) {
					// TODO �Զ����ɵ� catch ��
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
				switch (msg.what) {//������string����				
				case 11://��ȫ��
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
				case 14://ˮƽֹͣ
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
				case 17://��ȫ��
					goright =  CreateCommandStr(0x00, 0x02, 0x3F, 0x00);					
	 				com3.Write(goright,goright.length);	
					break;	
				case 21://��ȫ��
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
				case 24://��ֱֹͣ
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
				case 27://��ȫ��
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

//��������λ��+����λ��+���㲢������̨ת���߳�*****************************************************************
	/* ���п�����׼�ź�isTouchʱ,Ϊ��ѯ������̨λ�ò�������̨ת����ƽʱΪ��ѯ���ղ�������̨λ��*/
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
					//RX = RX;//��Ҫ���û��棬��Ϊ�߳�ˢ��̫�죬���з�Ӧ��ˢ��ȥ�ˡ�	
					if (RX[6]==((RX[1]+RX[2]+RX[3]+RX[4]+RX[5]) & 0xff)) {//RX_buff[6]==((RX_buff[1]+RX_buff[2]+RX_buff[3]+RX_buff[4]+RX_buff[5]) & 0xff
						if(RX[3]==0x59) {//����ˮƽ�Ƕ�	
							int Xval_buf = 0;
							Xval_buf = ((RX[4] << 8)) + RX[5];
							if ((Xval_buf <=36000) && (Xval_buf >=0 )){
								Xval = (float) Xval_buf/100;
								
								RX = null;
							}
							//Xval = (((float)(RX_buff[4] << 8)) + (float)RX_buff[5])/100;
							target_orientation =  String.valueOf(Xval);//ֱ�Ӱ���̨�Ƕȵ��ɷ�λ���ˣ�ʵ�ʲ���
							xOK = true;
	//							msg.what = 1;
	//							mHandler.sendMessage(msg);							
						}
						else if(RX[3]==0x5B) {//������ֱ�Ƕ�
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
							target_elevation =  String.valueOf(Yval);//ֱ�Ӱ���̨�Ƕȵ��ɷ�λ���ˣ�ʵ�ʲ���
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
				switch (msg.what) {//������string����			
				case 1://����״ָ̬��	
					float fy_buff = 0;
					float fx_buff = 0;
					float fx = 0;
					float fy = 0;

					
					fx_buff=(Float.parseFloat(angleX) + Xval)*100;//����Ҫ���˵ĽǶȼ�����̨���ڽǶȣ�����Ҫת���ĽǶ�
					//x����̨Ŀ��Ƕ�תΪ0��36000֮��
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

					Toast.makeText(getApplicationContext(),  " X�ǣ�"+ angleX + " Y�ǣ�"+ angleY +" X��to��"+ fx%100 + " Y��to��"+ fy_buff%100 ,Toast.LENGTH_SHORT).show();
					break;
				case 2:
					
					break;
				default:
					break;
				}
			}
		};
	}
//������ѯ��̨λ��*********************************************************************
	class ChkVal implements Runnable {	
		public void run() {
			while (true) {
				int[] text1 =  new int[]{0xFF,0x01,0x00,0x51,0x00,0x00,0x52};//���Ͳ�ѯ��̨ˮƽ��
 				com3.Write(text1, text1.length);
 				int[] text2 =  new int[]{0xFF,0x01,0x00,0x53,0x00,0x00,0x54};//���Ͳ�ѯ��̨��ֱ��
 				com3.Write(text2, text2.length);
 				
// 				Message msg = new Message();
// 				msg.what = 1;
//				mHandler.sendMessage(msg);
 				
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
				switch (msg.what) {//������string����			
				case 1://����״ָ̬��	
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
	//��Ϣ��ʾ��**************************************************************************
	public void AlertDialog (String Title,String Message,final boolean isExit) {	
		new android.app.AlertDialog.Builder(BitCameraService.this)  		
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
//��ȡ������ص���****************************************************
	/**���ܵ����ı�㲥*/  
    class BatteryBroadcastReceiver extends BroadcastReceiver{  
          
        @Override  
        public void onReceive(Context context, Intent intent) {  
              
            if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){  //�б仯��ִ��
                  
                int level = intent.getIntExtra("level", 0);  
                int scale = intent.getIntExtra("scale", 100);  
                powerPercent = level*100 /scale;
                
                new Thread(new TcpSendThread("@bat" + Integer.toString(powerPercent))).start();//����
                //textView3.setText(" ��������" + powerPercent + "% ");
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
   

	    
  //����UDP���ݴ������**************************************************************************
class SendTextThread implements Runnable {
	int portRemoteNum = 5500;//Զ�̶˿�
	int portLocalNum = 5000;//�����˿�
	
	//Socket s = null;
	//DataOutputStream dos = null;
	//boolean started = false;
	//String str = null;
	//boolean bConnected = false;

	public void run() {
		while (true) {
			android.os.Process.setThreadPriority(19);//os��ͼ�
			while (!ipOK) {/* �ȴ����IP */ };				
			try {
				dsText  = new DatagramSocket(portLocalNum);//��������UDP��������ڲ����ñ����˿�
				byte buf [] = text.getBytes(); 		    
				dpText = new DatagramPacket(buf,buf.length,address,portRemoteNum);	//��Ҫ�������ݴ�����������IP�Ͷ˿�
				dsText.send(dpText);
				dsText.close();
				Thread.sleep(1000);
				
			} catch (SocketException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			
			} catch (IOException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}catch (InterruptedException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
	}

}
//����UDP������ʾ**************************************************************************
class ShowTextThread implements Runnable {

	public void run() {
		android.os.Process.setThreadPriority(19);//os��ͼ�
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

//��̨����ָ��ת��******************************************
public int[] CreateCommandStr(int comand1,int comand2,int data1,int data2) {
	int[] str = new int[] {0xFF,0x01,comand1,comand2,data1,data2,((0x01+comand1+comand2+data1+data2)& 0xff)};
	Log.i("CreateCommandStr", "SendCommandStr = " + str);
	return str;
}
//�������ÿ�*************************************
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




