package com.hehaoyisheng.mogaokuregist.utls;

import java.util.Random;

public class RandomString {
	public static String base = "abcdefghijklmnopqrstuvwxyz0123456789";

	 public static int getNum(int start,int end) {  
	        return (int)(Math.random()*(end-start+1)+start);  
	    }  
	public static String getEmail(int lMin, int lMax) {
		
		int length = getNum(lMin, lMax);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = (int) (Math.random() * base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}
	public static String getString(){
		String yzm = "";
		Random r = new Random();
		for (int i = 0; i < 8; i++) {
			yzm += base.charAt(r.nextInt(36));
		}
		//System.out.println(yzm);
		return yzm;
	}
	 private static String[] telFirst="134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153".split(",");  
	   public static String getTel() {  
	        int index=getNum(0,telFirst.length-1);  
	        String first=telFirst[index];  
	        String second=String.valueOf(getNum(1,888)+10000).substring(1);  
	        String thrid=String.valueOf(getNum(1,9100)+10000).substring(1);  
	        return first+second+thrid;  
	    }  
}
