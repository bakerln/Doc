package com.wjp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Qiutiji extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	Button tjButton;
    @Override   
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tjButton=(Button)findViewById(R.id.tjButton);
        //Ϊ��ť���ü���
        tjButton.setOnClickListener(this);

    }

	public void onClick(View v) {
		// TODO Auto-generated method stub
		

		if(v==tjButton){
			EditText length=(EditText)findViewById(R.id.length);
			EditText width=(EditText)findViewById(R.id.width);
			EditText height=(EditText)findViewById(R.id.height);
			
			String chang= length.getText().toString();
			String kuan= width.getText().toString();
			String gao= height.getText().toString();	
			String str=chang+"|"+kuan+"|"+gao;
			//t�ӷ�������Σ�
			Socket s;
			try {
				s = new Socket("10.61.27.209",888);									
				//��ͻ��˷������
				OutputStream os= s.getOutputStream();
				DataOutputStream dout=new DataOutputStream(os);
				
				dout.writeUTF(str);
												
				
				InputStream in= s.getInputStream();
				DataInputStream din=new DataInputStream(in);
				String str_js=din.readUTF();
				TextView textview= (TextView)findViewById(R.id.showTiji);
				textview.setText(str_js);
				
				dout.close();
				os.close();		    	
				din.close();
				s.close();
				
				
				
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}