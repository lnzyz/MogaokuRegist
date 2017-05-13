package com.hehaoyisheng.mogaokuregist.utls;

import java.net.HttpURLConnection;
import java.net.URL;

import com.hehaoyisheng.crawler.utils.Print;

/**
 * 对接易码平台获取手机验证码
 * @author Administrator
 *
 */
public class Message {
	
	//易码用户名
	private static String username = "lnzyz1212";
	//易码密码
	private static String password = "940817";
	//身份token
	private static String token = "";
	//项目编号
	private static String itemid = "3081";
	
	/**
	 * 对外提供修改信息方法
	 * @param user
	 * @param pass
	 * @param item
	 */
	public static void setInfo(String user, String pass, String item){
		username = user;
		password = pass;
		itemid = item;
	}
	
//	@Test
//	public void test(){
//		login();
//		getPhone("13190153381");
//	}
	
	public static void main(String[] args){
		login();
		getInfo();
	}
	
	
	/**
	 * 登录
	 * @return
	 */
	public static boolean login(){
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL("http://api.51ym.me/UserInterface.aspx?action=login&username=" + username + "&password=" + password).openConnection();
			String s = Print.getBody(conn, true, "utf-8");
			if(s.startsWith("success")){
				token = s.substring(8).replace("\r", "").replace("\n", "");
				System.out.println(token);
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 获取随机手机号码
	 * @return
	 */
	public static String getRandPhone(){
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL("http://api.51ym.me/UserInterface.aspx?action=getmobile&itemid=" + itemid + "&token=" + token + "&country=1").openConnection();
			String s = Print.getBody(conn, true, "utf-8");
			if(s.startsWith("success")){
				return s.substring(8).replace("\r", "").replace("\n", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取指定手机号码
	 * @param phone
	 * @return
	 */
	public static boolean getPhone(String phone){
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL("http://api.51ym.me/UserInterface.aspx?action=getmobile&itemid=" + itemid + "&token=" + token + "&country=1&mobile=" + phone).openConnection();
			String res = Print.getBody(conn, true, "utf-8");
			if(res.startsWith("success")){
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 获取验证码短信
	 * @return
	 */
	public static String getMessage(String phone){
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL("http://api.51ym.me/UserInterface.aspx?action=getsms&mobile=" + phone + "&itemid=" + itemid + "&token=" + token).openConnection();
			String res = Print.getBody(conn, true, "utf-8");
			if(res.startsWith("success")){
				return res.substring(8).replace("\r", "").replace("\n", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 释放号码
	 */
	public static boolean release(String phone){
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL("http://api.51ym.me/UserInterface.aspx?action=release&mobile=" + phone + "&itemid=" + itemid + "&token=" + token).openConnection();
			String res = Print.getBody(conn, true, "utf-8");
			if(res.startsWith("success")){
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return false;
	}
	
	public static String getInfo(){
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL("http://api.51ym.me/UserInterface.aspx?action=getaccountinfo&format=1&token=" + token).openConnection();
			String res = Print.getBody(conn, true, "utf-8");
			if(res.startsWith("success")){
				return res.substring(8).replace("\r", "").replace("\n", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
