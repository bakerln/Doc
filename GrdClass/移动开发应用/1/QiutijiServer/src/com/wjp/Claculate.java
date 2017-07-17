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
			//创建服务器端socket对象（发送）
			ServerSocket ss=new ServerSocket(888);
			while(true){				
			
				//等待客户端连接		
				
				Socket s1=ss.accept();
				//接受客户端
				InputStream in= s1.getInputStream();
				DataInputStream din=new DataInputStream(in);
				String str= din.readUTF();
				
				
				
				String[] arr= str.split("\\|");
				
				Double chang=Double.valueOf(arr[0]);
				Double kuan=Double.valueOf(arr[1]);
				Double gao=Double.valueOf(arr[2]);
				Double tiji=chang*kuan*gao;
				String str_tiji=String.valueOf(tiji);
				//向客户端发送数据
				OutputStream os= s1.getOutputStream();
				DataOutputStream dout=new DataOutputStream(os);
				dout.writeUTF(str_tiji);
				
				//os.write("hello".getBytes());
				//关闭（防止内存泄漏）
				//os.close();
				//s1.close();
				
				
				
			}
		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
