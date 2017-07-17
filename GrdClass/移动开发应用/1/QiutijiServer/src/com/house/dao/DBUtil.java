package com.house.dao;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.house.bean.HouseInfo;
public class DBUtil {
	public static Connection getConnection(){//得到数据库连接
		Connection conn = null;
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn=DriverManager.getConnection("jdbc:mysql://localhost/housedb?useUnicode=true&characterEncoding=utf8","root","admin");			
		}
		catch(Exception e){
			e.printStackTrace();
		}

		return conn;
	}
	
	//房屋信息记录数--android
	public static int housecount=0;
	
	//检查用户名是否存在
	public static String checkUser(String u_name,String u_pwd){
		String result = null;
		Connection con = getConnection();
		Statement st = null;
		ResultSet rs = null;
		try{
			st = con.createStatement();//创建语句
			rs = st.executeQuery("select uid from house_user where u_name = '"+
					u_name+"' and u_pwd = '"+u_pwd+"';");
			
			if(rs.next()){
				result = new String(rs.getString(1).getBytes("ISO8859_1"), "GBK");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		} finally{
			try{
				if(rs != null){
					rs.close();
					rs = null;
				}
			if(st != null){
					st.close();
					st = null;
				}
			if(con != null){
					con.close();
					con = null;
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}			
		}
		return result;
	}	
	
	//添加用户
	public static int insertUser(String u_name,String u_qq,String u_pwd,String u_Email,String u_dis){
		Connection con = getConnection();
		Statement st = null;
		int i=0;
		try {
			u_name = new String(u_name.getBytes("GBK"), "ISO8859_1");
			u_dis = new String(u_dis.getBytes("GBK"), "ISO8859_1");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		try {
			st = con.createStatement();
			String sql = "insert into house_user" +
				"(u_name,u_qq,u_pwd,u_Email,u_dis) " +
				"values('"+u_name+"','"+((u_qq.equals(""))?0:u_qq)+"','"+u_pwd+"','"+u_Email+"','"+u_dis+"');";
			 i=st.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try{
				if(st != null){
					st.close();
					st = null;
				}
				if(con != null){
					con.close();
					con = null;
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}			
		}
		return i;
	}
	
	//插入出租出售信息--android
	public static int insert_info(String info_title,String info_dis,String info_lon,String info_lat,String uid,String info_sort,String info_price,String mshousehx,byte[] image){//插入美食信息
		Connection con = getConnection();
		PreparedStatement pstmt = null;
		int i=0;
		try {
			pstmt = con.prepareStatement("insert into house_info(info_title,info_dis,info_lon,info_lat,uid,info_sort,info_price,house_hx,image) values(?,?,?,?,?,?,?,?,?)");
			
				pstmt.setString(1, info_title);
				pstmt.setString(2, info_dis);
			    pstmt.setDouble(3, Double.parseDouble(info_lon));
				pstmt.setDouble(4, Double.parseDouble(info_lat));
				pstmt.setInt(5, Integer.parseInt(uid));
				//pstmt.setString(6, new String(info_sort.getBytes("iso-8859-1"),"utf-8"));
				pstmt.setString(6, info_sort);
				pstmt.setInt(7, Integer.parseInt(info_price));
				//pstmt.setString(8, new String(mshousehx.getBytes("GBK"),"ISO-8859-1"));
				pstmt.setString(8, mshousehx);
				pstmt.setBytes(9, image);
				
				i=pstmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{
				if(pstmt != null){
					pstmt.close();
					pstmt = null;
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			try{
				if(con != null){
					con.close();
					con = null;
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}	
		}
		return i;
	}
	
	//房屋信息搜索--android端
	public static List<Object[]> getInfoForPhone(String infoValues, String searchSort,String startPrice,String endPrice,String span,String currentPageNo,String househx){
		List result=new ArrayList();
		Connection con = getConnection();
		Statement st = null;
		ResultSet rs = null;
		String sql_count="";
		String sql_info="";
				
		int start=Integer.parseInt(span)*(Integer.parseInt(currentPageNo)-1);//计算出起始记录编号
		
		String sql_s="select * from house_info where 1=1 ";
		String sql_s1="select count(*) from house_info where 1=1 ";
		String where_sql="";
		if(infoValues!=null && !infoValues.trim().equals(""))  where_sql=where_sql+" and info_dis like '%"+infoValues+"%'";
		if(searchSort!=null && !searchSort.trim().equals("")) where_sql=where_sql+" and info_sort='"+searchSort+"'";
		if(startPrice!=null && !startPrice.trim().equals("") && endPrice!=null && !endPrice.trim().equals(""))
			where_sql=where_sql + " and info_price between "+Integer.parseInt(startPrice)+" and "+Integer.parseInt(endPrice);
		if(househx!=null && !househx.trim().equals("")) where_sql=where_sql+" and house_hx='"+househx+"'";
		//查询记录数sql
		sql_count=sql_s1+where_sql;
		
		where_sql=where_sql+"  limit "+start+","+span+";";
		sql_info=sql_s+where_sql;
	
		try{
			con=getConnection();
			st=con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);

			//执行检索		
			rs=st.executeQuery(sql_info);
            //System.out.println(sql_info);
			while(rs.next()){
				Object[] str = new Object[9];
				str[0] = new String(rs.getString("house_hx").getBytes("ISO8859_1"), "GBK");//house_hx
				str[1] = new String(rs.getString("info_title").getBytes("ISO8859_1"), "GBK");//info_title
				str[2] = new String(rs.getString("info_dis").getBytes("ISO8859_1"), "GBK");//info_dis
				str[3] = new String(rs.getString("info_lon").getBytes("ISO8859_1"), "GBK");//info_lon
				str[4] = new String(rs.getString("info_lat").getBytes("ISO8859_1"), "GBK");//info_lat
				str[5] = String.valueOf(rs.getDate("info_time"));
				str[6] = new String(rs.getString("uid").getBytes("ISO8859_1"), "GBK");//mid
				str[7] = rs.getBlob("image");

				result.add(str);
			}
			//获取总的记录数
			rs=st.executeQuery(sql_count);
			if(rs.next())
				housecount=rs.getInt(1);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{
				if(rs != null){
					rs.close();
					rs = null;}
				if(st != null){
					st.close();
					st = null;
				}
				if(con != null){
					con.close();
					con = null;
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}			
		}		
		return result;
	}
	
	//得到所有的房屋推荐
	public static ArrayList<HouseInfo> get_recommend(){
		ArrayList<HouseInfo> result = new ArrayList<HouseInfo>();
		Connection con = getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			pstmt = con.prepareStatement("select info_title,info_dis,info_lon,info_lat,a.info_time,a.uid,a.mid,a.image  from house_info as a, house_recommend as b where a.mid = b.mid");
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				String info_title = new String(rs.getString(1).getBytes("ISO-8859-1"),"GBK");
				String info_dis = new String(rs.getString(2).getBytes("ISO-8859-1"),"GBK");
				String info_lon = new String(rs.getString(3).getBytes("ISO-8859-1"),"GBK");
				String info_lat = new String(rs.getString(4).getBytes("ISO-8859-1"),"GBK");
				Date info_time = rs.getDate(5);
				String uid = new String(rs.getString(6).getBytes("ISO-8859-1"),"GBK");
				String mid = new String(rs.getString(7).getBytes("ISO-8859-1"),"GBK");
				
				Blob bimage=rs.getBlob("image");
				
				HouseInfo mi = new HouseInfo(info_title, info_dis, info_lon, info_lat, info_time, uid, mid,bimage);
				result.add(mi);
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}		finally{
			try{
				if(rs != null){
					rs.close();
					rs = null;}
				if(pstmt != null){
					pstmt.close();
					pstmt = null;
				}
				if(con != null){
					con.close();
					con = null;
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}			
		}		
		return result;
	}
	
	
	public static ArrayList<HouseInfo> getHouseUid(String uid){//得到我的收藏所有美食
		ArrayList<HouseInfo> result = new ArrayList<HouseInfo>();
		String sql = "select info_title,info_dis,info_lon,info_lat,info_time,a.uid,a.mid,a.image " +
				"from house_info as a where a.uid = "+uid+";";
		Connection con = getConnection();
		Statement st = null;
		ResultSet rs = null;
		try{
			st = con.createStatement();//创建语句
			rs = st.executeQuery(sql);
			while(rs.next()){
				String info_title = new String(rs.getString("info_title").getBytes("ISO8859_1"), "GBK");
				String info_dis = new String(rs.getString("info_dis").getBytes("ISO8859_1"), "GBK");
				String info_lon = new String(rs.getString("info_lon").getBytes("ISO8859_1"), "GBK");
				String info_lat = new String(rs.getString("info_lat").getBytes("ISO8859_1"), "GBK");
				String mid = new String(rs.getString("mid").getBytes("ISO8859_1"), "GBK");
				Date info_time = rs.getDate("info_time");
				Blob bimage=rs.getBlob("image");
				
				HouseInfo mi = new HouseInfo(info_title, info_dis, info_lon, info_lat, info_time, uid,mid,bimage);
				result.add(mi);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(rs != null){
					rs.close();
					rs = null;
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			try{
				if(st != null){
					st.close();
					st = null;
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			try{
				if(con != null){
					con.close();
					con = null;
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}			
		}
		return result;
	}
	
	

	
	
}