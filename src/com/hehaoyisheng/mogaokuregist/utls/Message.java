package com.hehaoyisheng.mogaokuregist.utls;

import java.net.HttpURLConnection;
import java.net.URL;

import com.hehaoyisheng.crawler.utils.Print;

/**
 * �Խ�����ƽ̨��ȡ�ֻ���֤��
 * @author Administrator
 *
 */
public class Message {
	
	//�����û���
	private static String username = "lnzyz1212";
	//��������
	private static String password = "940817";
	//���token
	private static String token = "";
	//��Ŀ���
	private static String itemid = "3081";
	
	/**
	 * �����ṩ�޸���Ϣ����
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
	 * ��¼
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
	 * ��ȡ����ֻ�����
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
	 * ��ȡָ���ֻ�����
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
	 * ��ȡ��֤�����
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
	 * �ͷź���
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
