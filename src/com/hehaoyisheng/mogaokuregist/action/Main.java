package com.hehaoyisheng.mogaokuregist.action;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.hehaoyisheng.crawler.utils.Print;
import com.hehaoyisheng.crawler.utils.Submit;
import com.hehaoyisheng.mogaokuregist.entity.Member;
import com.hehaoyisheng.mogaokuregist.utls.Dama2Web;
import com.hehaoyisheng.mogaokuregist.utls.Dama2Web.DecodeResult;
import com.hehaoyisheng.mogaokuregist.utls.Message;

public class Main {

	public void main(Member mem) throws Exception {
		HttpURLConnection conn = (HttpURLConnection) new URL("http://www.mgk.org.cn/toReg.htm").openConnection();
		String cookie = "";
		for(int i = 0; ; i++){
			String s = conn.getHeaderField(i);
			if(s == null){
				break;
			}
			if(i == 12){
				cookie += s.split(";")[0] + ";";
			}
			if(i == 14){
				cookie += s.split(";")[0] + ";";
			}
			//System.out.println(cookie);
		}
		
		//确定是否号码冲突
		HttpURLConnection conn2 = (HttpURLConnection) new URL("http://www.mgk.org.cn/validateLoginName.htm?accountLogin.loginName=" + mem.getUsername() + "&_=" + System.currentTimeMillis()).openConnection();
		String check = Print.getBody(conn2, false, "utf-8");
		if(check.indexOf("true") != -1){
			System.out.println("yes");
		}else{
			System.out.println("no");
			return;
		}
		/**
		 * 发送验证码
		 */
		String result = "";
		while(true){
			Dama2Web dm = new Dama2Web();
			DecodeResult res = dm.decodeUrlAndGetResult("http://www.mgk.org.cn/checkCode.htm?rand=", cookie, "http://www.mgk.org.cn/toReg.htm", 62, 3000);
			System.out.println(res.result);
			HttpURLConnection send = (HttpURLConnection) new URL("http://www.mgk.org.cn/login!sendCheckCodeRegister.htm").openConnection();
			send.setRequestProperty("Cookie", cookie);
			String msg = "loginName=" + mem.getUsername() + "&checkCode=" + URLEncoder.encode(res.result, "utf-8");
			Submit.post(send, msg, "utf-8");
			String sk = Print.getBody(send, true, "utf-8");
			if(sk.indexOf("输入的验证码有误") != -1){
				mem.setState("验证码错误!");
			}else{
				mem.setState("已发送短信");
				result = res.result;
				break;
			}
		}
		
		String sss = null;
		while(sss == null){
			sss = Message.getMessage(mem.getUsername());
			Thread.sleep(3000);
		}
		mem.setState("已收到短信");
		sss = sss.substring(33,37);
		System.out.println(sss);
		HttpURLConnection conn3 = (HttpURLConnection) new URL("http://www.mgk.org.cn/doReg.htm").openConnection();
		conn3.setRequestProperty("Cookie", cookie);
		String data = "accountLogin.loginName=" + mem.getUsername() + "&accountLogin.realname=" + URLEncoder.encode(mem.getName(), "utf-8") + "&accountLogin.areaCode=001015001001&accountLogin.idCard=" + mem.getIdcard() + "&accountLogin.email=" + mem.getEmail() +"&accountLogin.sex=1&accountLogin.loginPass=" + mem.getPassword() + "&password=" + mem.getPassword() + "&checkFlag=1&checkCode=" + URLEncoder.encode(result, "utf-8") + "&mobileCode=" + sss;
		Submit.post(conn3, data, "utf-8");
		String s = Print.getBody(conn3, true, "utf-8");
		if(s.indexOf("注册成功") != -1){
			mem.setState("注册成功");
		}
		Message.release(mem.getUsername());
	}

}
