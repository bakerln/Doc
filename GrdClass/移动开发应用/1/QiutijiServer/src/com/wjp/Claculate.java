package com.wjp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Claculate {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
			//������������socket���󣨷��ͣ�
			ServerSocket ss=new ServerSocket(888);
			while(true){				
			
				//�ȴ��ͻ�������		
				
				Socket s1=ss.accept();
				//���ܿͻ���
				InputStream in= s1.getInputStream();
				DataInputStream din=new DataInputStream(in);
				String str= din.readUTF();
				
				
				
				String[] arr= str.split("\\|");
				
				Double chang=Double.valueOf(arr[0]);
				Double kuan=Double.valueOf(arr[1]);
				Double gao=Double.valueOf(arr[2]);
				Double tiji=chang*kuan*gao;
				String str_tiji=String.valueOf(tiji);
				//��ͻ��˷�������
				OutputStream os= s1.getOutputStream();
				DataOutputStream dout=new DataOutputStream(os);
				dout.writeUTF(str_tiji);
				
				//os.write("hello".getBytes());
				//�رգ���ֹ�ڴ�й©��
				//os.close();
				//s1.close();
				
				
				
			}
		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
