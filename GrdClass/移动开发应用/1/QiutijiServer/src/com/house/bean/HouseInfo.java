package com.house.bean;

import java.sql.Blob;
import java.sql.Date;

public class HouseInfo {
	String info_title;
	String info_dis;
	String info_lon;
	String info_lat;
	Date info_time;
	String uid;
	String mid;
	Blob bimg;
	
	public Blob getBimg() {
		return bimg;
	}
	public void setBimg(Blob bimg) {
		this.bimg = bimg;
	}
	public String getInfo_title() {
		return info_title;
	}
	public void setInfo_title(String infoTitle) {
		info_title = infoTitle;
	}
	public String getInfo_dis() {
		return info_dis;
	}
	public void setInfo_dis(String infoDis) {
		info_dis = infoDis;
	}
	public String getInfo_lon() {
		return info_lon;
	}
	public void setInfo_lon(String infoLon) {
		info_lon = infoLon;
	}
	public String getInfo_lat() {
		return info_lat;
	}
	public void setInfo_lat(String infoLat) {
		info_lat = infoLat;
	}
	public Date getInfo_time() {
		return info_time;
	}
	public void setInfo_time(Date infoTime) {
		info_time = infoTime;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getMid(){
		return mid;
	}
	
	public void setMid(String mid){
		this.mid = mid;
	}
	public HouseInfo(String infoTitle, String infoDis, String infoLon,
			String infoLat, Date infoTime, String uid,String mid,Blob bimg) {
		super();
		info_title = infoTitle;
		info_dis = infoDis;
		info_lon = infoLon;
		info_lat = infoLat;
		info_time = infoTime;
		this.uid = uid;
		this.mid = mid;
		this.bimg=bimg;
	}
	
}
