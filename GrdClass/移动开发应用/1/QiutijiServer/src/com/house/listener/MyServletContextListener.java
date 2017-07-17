package com.house.listener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.house.server.ServerThread;
public class MyServletContextListener implements ServletContextListener{
	ServerThread st = null;//�����������̵߳�����
	public void contextInitialized(ServletContextEvent sce){//��д��contextInitialized����
		sce.getServletContext().log("[com.house] Context Initialized...");
		System.out.println("[com.house] Context Initialized...");
		st = new ServerThread();//���������߳� 
		st.start();//���������߳�
	}
	public void contextDestroyed(ServletContextEvent sce){//��д��contextDestroyed����
		sce.getServletContext().log("[com.house] Context Destroyed...");
		System.out.println("[com.house] Context Destroyed...");
		st.setFlag(false);//ֹͣ�����߳� 
	}
}