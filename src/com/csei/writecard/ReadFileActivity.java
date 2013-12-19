package com.csei.writecard;
import java.util.ArrayList;
import java.util.HashMap;
import com.example.service.RFIDService;
import com.example.writecard.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
public class ReadFileActivity extends Activity implements OnClickListener {
   public static final int FILE_RESULT_CODE=1;
   private Button selectfile;
   private Button writecard;
   private Button backbutton;
   private ListView showTextContent;
   ArrayList<HashMap<String, Object>> listItem=new ArrayList<HashMap<String,Object>>();	  
   String writedata="";
   private String activity = "com.csei.writecard.ReadFileActivity";
   private MyBroadcast myBroadcast;				//�㲥������
	public static int cmd_flag = 0;				//����״̬  0Ϊ��������������1ΪѰ����2Ϊ��֤��3Ϊ�����ݣ�4Ϊд����
	public static int authentication_flag = 0;		//��֤״̬  0Ϊ��֤ʧ�ܺ�δ��֤  1Ϊ��֤�ɹ�
	public static String TAG= "M1card";
	String ctype="";
	int canWriteCard;
	private ProgressDialog shibieDialog; //ʶ��������
	int cur_pos;
	String[] s1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.readfile);
		selectfile=(Button) this.findViewById(R.id.selectfile);
		writecard=(Button) this.findViewById(R.id.writecard);
		showTextContent=(ListView) this.findViewById(R.id.showfilecontent);
		backbutton=(Button) this.findViewById(R.id.backbutton);
		selectfile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(ReadFileActivity.this,MyFileManager.class);
				startActivityForResult(intent,FILE_RESULT_CODE);
				canWriteCard=1;
			}
		});
		backbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				backbutton.setBackgroundResource(R.drawable.btn_back_active);
				finish();
			}
		});
		writecard.setOnClickListener(this);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(FILE_RESULT_CODE==requestCode){
			Bundle  bundle=null;
			if(data!=null&&(bundle=data.getExtras())!=null){             
				String ss=bundle.getString("filecontent");
				ctype=bundle.getString("ctype");
				if(ss!=null){
				s1=ss.split(" ");
				for(int i=0;i<s1.length;i++){
					Log.e("yyyy",s1[i]);
				final String[] s=s1[i].split(",");
			    	 HashMap<String, Object> map=new HashMap<String,Object>();
			    	 map.put("ItemImage",R.drawable.item);
			    	 map.put("ItemText", s[3]+":"+s[1]);
			    	 listItem.add(map);
			    	 SimpleAdapter listItemAdapter=new SimpleAdapter(this,listItem,R.layout.rolestable,new String[]{"ItemImage","ItemText"},new int[]{R.id.ItemImage,R.id.ItemText});
					 showTextContent.setAdapter(listItemAdapter);
					 showTextContent.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View v,
								int position, long id) {
							// TODO Auto-generated method stub
						     //������ʾ��Ȼ����Ӧ����Ϣ����ȫ�ֱ�����֮����button.onclick�²���
							cur_pos=position;
		                    v.setSelected(true);           
						    Log.e("poi",s1[cur_pos]);
						    writedata=s1[cur_pos];
						}
					 }); 
				}	   
		}else{
			Toast.makeText(ReadFileActivity.this, "�Բ���û��ѡ����ȷ���ļ�!", Toast.LENGTH_SHORT).show(); 
		}
		}	
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
			shibieDialog = new ProgressDialog(ReadFileActivity.this, R.style.mProgressDialog);
			shibieDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			shibieDialog.setMessage("д����...");
			/*shibieDialog.setCancelable(false);*/
			shibieDialog.show();
		  if(canWriteCard==1){
		  sendCmd(writedata);
		  }else{
				Toast.makeText(ReadFileActivity.this, "��ѡ��Ҫд����ļ�", Toast.LENGTH_SHORT).show(); 
			  shibieDialog.cancel();
		  }
	}
	private void sendCmd(String writedata) {
		Intent sendToservice = new Intent(ReadFileActivity.this,RFIDService.class);
			if(ctype.equals("00")){
		    sendToservice.putExtra("cardType", "0x01");
			}else if(ctype.equals("01")){
			sendToservice.putExtra("cardType", "0x02");	
			}
			sendToservice.putExtra("data", writedata);
			sendToservice.putExtra("activity", activity);
			startService(sendToservice);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		myBroadcast = new MyBroadcast();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.csei.writecard.ReadFileActivity");
		registerReceiver(myBroadcast, filter); 		//ע��㲥������
		super.onResume();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		cmd_flag = 0;  				  //д״̬�ָ���ʼ״̬
		authentication_flag = 0;
		unregisterReceiver(myBroadcast);  //ж�ع㲥������
		super.onPause();
		Log.e("M1CARDPAUSE", "PAUSE");  	
	}	
	@Override
	protected void onDestroy() {
		Intent stopService = new Intent();
		stopService.setAction("com.example.service.RFIDService");
		stopService.putExtra("stopflag", true);
		sendBroadcast(stopService);  //�������͹㲥,�����ֹͣ
		Log.e(TAG, "send stop");
		super.onDestroy();
	}
	private class MyBroadcast extends BroadcastReceiver {
		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context context, Intent intent) {
			String receivedata = intent.getStringExtra("result");
			if(receivedata!=null){
			shibieDialog.cancel();	
			Toast.makeText(ReadFileActivity.this, receivedata, Toast.LENGTH_SHORT).show();
			/*showTextContent.getChildAt(cur_pos).setBackgroundColor(Color.GRAY);*/
			/*	if(showTextContent.getChildAt(cur_pos)!=null){
			showTextContent.getChildAt(cur_pos).setSelected(false);
			++cur_pos;
			Log.e("cur_pos",cur_pos+"");
			if(cur_pos==8){
				showTextContent.setSelection(cur_pos+1);
				cur_pos=0;
			}
			showTextContent.getChildAt(cur_pos).setSelected(true);
			writedata=s1[cur_pos];
			sendCmd(writedata);
			}else{
				showalert.setVisibility(View.VISIBLE);
				showalert.setText("��ѡ������!");
			}*/
		}
		}
	}
    
	
}
