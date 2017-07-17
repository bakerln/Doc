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
	Socket sc;//����Socket������
	ServerThread father;//����ServerThread������
	DataInputStream din = null;//������
	DataOutputStream dout = null;//�����
	private boolean flag=true;//ѭ������ 
	public ServerAgent(Socket sc,ServerThread father){//������
		this.sc=sc;//�õ�Socket
		this.father=father;//�õ�ServerThread������
		try{
			din=new DataInputStream(sc.getInputStream());//������
			dout=new DataOutputStream(sc.getOutputStream());//�������
		}
		catch(Exception e){//�����쳣
			e.printStackTrace();//��ӡ�쳣
		}
	}
	public void run(){
		while(flag){//ѭ��
			try{
				String msg=din.readUTF();//����Ϣ 
				System.out.println("Client msg = " + msg);
				if(msg.startsWith("<#LOGIN#>")){//��¼����
					msg=msg.substring(9);//��ȡ�Ӵ�
					String[] str = msg.split("\\|");//�ָ��ַ���
					if(str.length<=0) return; //���鳤��С��0���򲻼���ִ��
					String u_id = DBUtil.checkUser(str[0], str[1]);//������ݿ����Ƿ��и��û�
					if(u_id == null){//��u_idΪ��ʱ����¼ʧ��
						System.out.println("u_id == null");
						dout.writeUTF("<#LOGINERROR#>");//֪ͨ�ֻ��ͻ��˵�¼ʧ��
					}
					else {//��¼�ɹ�
						System.out.println(u_id+" <#LOGINOK#>");
						dout.writeUTF("<#LOGINOK#>"+u_id);//��¼�ɹ������û�������
					}
					break;
				}
				else if(msg.startsWith("<#REGISTER#>")){//ע�ᶯ��
					msg=msg.substring(12);//��ȡ�Ӵ�
					String[] str = msg.split("\\|");//�ָ��ַ���
					int id = DBUtil.insertUser(str[0], str[2], str[1], str[3], str[4]);
					String uid="1";
					if(id==1) 
						 uid = DBUtil.checkUser(str[0], str[1]);  
					dout.writeUTF("<#REGISTEROK#>"+uid+"|"+str[0]);//��ͻ��˷����û�ID���û���
					System.out.println("uid  "+uid+"   "+str[0]);
					break;
				}else if(msg.startsWith("<#INSERTINFO#>")){
					msg=msg.substring(14);//��ȡ�Ӵ�
					String[] str = msg.split("\\|");//�ָ��ַ���
					int size = din.readInt();//��ȡͼƬ����ĳ���
					byte[] image = new byte[size];//����ͼƬ����
						
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
					msg=msg.substring(10);//��ȡ�Ӵ�
					String[] str = msg.split("\\|");

					List<Object[]> Infos = DBUtil.getInfoForPhone(str[0], str[1], str[2], str[3],str[4],str[5],str[6]);
					int totleNumber=DBUtil.housecount;//�õ��ܵļ�¼��
			        System.out.println("�ܼ�¼��=="+totleNumber);
					dout.writeUTF("<#SEARCHINFO#>"+Infos.size()+"|"+totleNumber);//��ͻ��˷�������������
					for(Object[] mi : Infos){
						dout.writeUTF(mi[0]+"|"+mi[1]+"|"+mi[2]+"|"+mi[3]+"|"+
								mi[4]+"|"+mi[5]+"|"+mi[6]);
						
						    //���ͼ��
							Blob b=(Blob)mi[7];
						
							int size = (int)b.length();
							byte[] bs = b.getBytes(1,size);
							
							dout.writeInt(size);//д���ֽ�����ĳ���
							dout.write(bs);//���ֽ����鷢�͵��ͻ���
							dout.flush();//��ջ�����,��֤֮ǰ�����ݷ��ͳ�ȥ
							
					}					
					
				}	else if(msg.startsWith("<#RECOMMEND#>")){//�����Ƽ�
					ArrayList<HouseInfo> houseInfos = DBUtil.get_recommend();
					dout.writeUTF("<#RECOMMENDINFO#>"+houseInfos.size());//��ͻ��˷�������������
					for(HouseInfo mi : houseInfos){
						dout.writeUTF(mi.getInfo_title()+"|"+mi.getInfo_dis()+"|"+mi.getInfo_lon()+"|"+
							mi.getInfo_lat()+"|"+mi.getInfo_time()+"|"+mi.getUid()+"|"+mi.getMid());
						
					//ͼƬ
					Blob b=mi.getBimg();
					int size = (int)b.length();//�õ�ͼƬ�ĳ���
					byte[] bs = b.getBytes(1, (int)size);//��Blobת���ɳ��ֽ�����
					dout.writeInt(size);//д���ֽ�����ĳ���
					dout.write(bs);//���ֽ����鷢�͵��ͻ���
					dout.flush();//��ջ�����,��֤֮ǰ�����ݷ��ͳ�ȥ
					}	
				
				}	
				else if(msg.startsWith("<#ClientDown#>")){//�ͻ�������
					try{
						din.close();//�ر�������
						dout.close();//�ر������
						sc.close();//�ر�Socket
						flag = false;
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
				
				
			}
			catch(Exception e){//�����쳣
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
	public void setFlag(boolean flag){//ѭ����־λ�����÷���
		this.flag = flag;
	}
}
