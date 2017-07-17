package com.house.server;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

import com.house.bean.HouseInfo;
import com.house.dao.DBUtil;
public class ServerAgent extends Thread{
	Socket sc;//声明Socket的引用
	ServerThread father;//声明ServerThread的引用
	DataInputStream din = null;//输入流
	DataOutputStream dout = null;//输出流
	private boolean flag=true;//循环变量 
	public ServerAgent(Socket sc,ServerThread father){//构造器
		this.sc=sc;//得到Socket
		this.father=father;//得到ServerThread的引用
		try{
			din=new DataInputStream(sc.getInputStream());//输入流
			dout=new DataOutputStream(sc.getOutputStream());//输入出流
		}
		catch(Exception e){//捕获异常
			e.printStackTrace();//打印异常
		}
	}
	public void run(){
		while(flag){//循环
			try{
				String msg=din.readUTF();//收消息 
				System.out.println("Client msg = " + msg);
				if(msg.startsWith("<#LOGIN#>")){//登录动作
					msg=msg.substring(9);//截取子串
					String[] str = msg.split("\\|");//分割字符串
					if(str.length<=0) return; //数组长度小于0，则不继续执行
					String u_id = DBUtil.checkUser(str[0], str[1]);//检查数据库中是否含有该用户
					if(u_id == null){//当u_id为空时，登录失败
						System.out.println("u_id == null");
						dout.writeUTF("<#LOGINERROR#>");//通知手机客户端登录失败
					}
					else {//登录成功
						System.out.println(u_id+" <#LOGINOK#>");
						dout.writeUTF("<#LOGINOK#>"+u_id);//登录成功，将用户名返回
					}
					break;
				}
				else if(msg.startsWith("<#REGISTER#>")){//注册动作
					msg=msg.substring(12);//截取子串
					String[] str = msg.split("\\|");//分割字符串
					int id = DBUtil.insertUser(str[0], str[2], str[1], str[3], str[4]);
					String uid="1";
					if(id==1) 
						 uid = DBUtil.checkUser(str[0], str[1]);  
					dout.writeUTF("<#REGISTEROK#>"+uid+"|"+str[0]);//向客户端发送用户ID和用户名
					System.out.println("uid  "+uid+"   "+str[0]);
					break;
				}else if(msg.startsWith("<#INSERTINFO#>")){
					msg=msg.substring(14);//截取子串
					String[] str = msg.split("\\|");//分割字符串
					int size = din.readInt();//读取图片数组的长度
					byte[] image = new byte[size];//创建图片数组
						
					byte[] mm=new byte[1024];
					
					int len=0;
					int k=0;
					while ((len=din.read(mm))>0) {
					   	 for(int i=0;i<len;i++){
				    		 image[k+i]=mm[i];
				    	}
				    	 k=k+len; 
				    	 if(k>=size) break;
					}
					
					
					int mid = DBUtil.insert_info(str[0], str[1], str[2], str[3], str[4],str[5],str[6],str[7],image);
					System.out.println("i=="+mid);
					if(mid>0){
						dout.writeUTF("<#INSERTOK#>");
						System.out.println("<#INSERTOK#>");
					}
					else{
						dout.writeUTF("<#INSERTERROR#>");
						System.out.println("<#INSERTERROR#>");
					}
				}
				else if(msg.startsWith("<#SEARCH#>")){
					msg=msg.substring(10);//截取子串
					String[] str = msg.split("\\|");

					List<Object[]> Infos = DBUtil.getInfoForPhone(str[0], str[1], str[2], str[3],str[4],str[5],str[6]);
					int totleNumber=DBUtil.housecount;//得到总的记录数
			        System.out.println("总记录数=="+totleNumber);
					dout.writeUTF("<#SEARCHINFO#>"+Infos.size()+"|"+totleNumber);//向客户端发送搜索的数据
					for(Object[] mi : Infos){
						dout.writeUTF(mi[0]+"|"+mi[1]+"|"+mi[2]+"|"+mi[3]+"|"+
								mi[4]+"|"+mi[5]+"|"+mi[6]);
						
						    //输出图像
							Blob b=(Blob)mi[7];
						
							int size = (int)b.length();
							byte[] bs = b.getBytes(1,size);
							
							dout.writeInt(size);//写入字节数组的长度
							dout.write(bs);//将字节数组发送到客户端
							dout.flush();//清空缓冲区,保证之前的数据发送出去
							
					}					
					
				}	else if(msg.startsWith("<#RECOMMEND#>")){//搜索推荐
					ArrayList<HouseInfo> houseInfos = DBUtil.get_recommend();
					dout.writeUTF("<#RECOMMENDINFO#>"+houseInfos.size());//向客户端发送搜索的数据
					for(HouseInfo mi : houseInfos){
						dout.writeUTF(mi.getInfo_title()+"|"+mi.getInfo_dis()+"|"+mi.getInfo_lon()+"|"+
							mi.getInfo_lat()+"|"+mi.getInfo_time()+"|"+mi.getUid()+"|"+mi.getMid());
						
					//图片
					Blob b=mi.getBimg();
					int size = (int)b.length();//得到图片的长度
					byte[] bs = b.getBytes(1, (int)size);//将Blob转换成成字节数组
					dout.writeInt(size);//写入字节数组的长度
					dout.write(bs);//将字节数组发送到客户端
					dout.flush();//清空缓冲区,保证之前的数据发送出去
					}	
				
				}	
				else if(msg.startsWith("<#ClientDown#>")){//客户端下线
					try{
						din.close();//关闭输入流
						dout.close();//关闭输出流
						sc.close();//关闭Socket
						flag = false;
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
				
				
			}
			catch(Exception e){//捕获异常
				System.out.println("Client Down");
				flag = false;
				try{
					if(din != null){
						din.close();
						din = null;
					}
				}
				catch(Exception el){
					el.printStackTrace();
				}
				try{
					if(dout != null){
						dout.close();
						dout = null;
					}
				}
				catch(Exception el){
					el.printStackTrace();
				}
				try{
					if(sc != null){
						sc.close();
						sc = null;
					}
				}
				catch(Exception el){
					el.printStackTrace();
				}		
			}
			
		}
	}
	public void setFlag(boolean flag){//循环标志位的设置方法
		this.flag = flag;
	}
}
