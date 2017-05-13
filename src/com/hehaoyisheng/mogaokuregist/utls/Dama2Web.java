package com.hehaoyisheng.mogaokuregist.utls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class Dama2Web {
	//�����뱦��
	public final static int errCalcEncInfo		= -1000001;		//���������Ϣʧ��
	public final static int errBalanceNotNumber	= -1000002;		//���ص���������
	public final static int errIdNotNumber		= -1000003;		//���ص�ID��������
	public final static int errEncodingError	= -1000004;		//�������
	public final static int errRespError		= -1000005;		//HTTP��Ӧ���������а���HTTP����Ӧ��
	public final static int errJsonParseError	= -1000006;		//JSON��������
	public final static int errJsonMapError		= -1000007;		//JSONӳ�����
	public final static int errIOException		= -1000008;		//IO�쳣�����������쳣��
	public final static int errGetResultTimeout	= -1000009;		//�õ������ʱ
	
	private final static String _urlPrex = "http://api.dama2.com:7777/app/";
	
	//һ������Ľ��
	public class RequestResult {
		public int ret;			//������
		public String desc;		//����������
		
		RequestResult(int ret, String desc) {
			this.ret = ret;
			this.desc = desc;
		}
	}

	//��ѯ�����
	public class ReadBalanceResult extends RequestResult {
		public int balance;				//���
		
		ReadBalanceResult(int ret, String desc, int balance) {
			super(ret, desc);
			this.balance = balance;
		}
	}

	//���û���Ϣ���
	public class ReadInfoResult extends RequestResult {
		public String name;
		public String qq;		//�û�QQ�ţ�������ʱΪnull
		public String email;	//�û����䣬������ʱΪnull
		public String tel;		//�û��绰���룬������ʱΪnull
		
		ReadInfoResult(int ret, String desc, String name, String qq, String email, String tel) {
			super(ret, desc);
			this.name = name;
			this.qq = qq;
			this.email = email;
			this.tel = tel;
		}
	}

	//������(getResult����)
	public class DecodeResult extends RequestResult{
		public String result;		//����ִ�
		public String cookie;
		
		DecodeResult(int ret, String desc, String result, String cookie) {
			super(ret, desc);
			this.result = result;
			this.cookie = cookie;
		}
	}
	
	//���캯��
	//appID��Ӧ��ID�����ID��
	//softKey:���KEY
	//uname���û���
	//upwd���û�����
	public Dama2Web(int appID, String softKey, String uname, String upwd) {
		_appID = appID;
		_softKey = softKey;
		_uname = uname;
		_upwd = upwd;
	}
	public Dama2Web(){
		_appID = 42152;
		_softKey = "b9d476038725af57dbfc6a9f5056c01f";
		_uname = "kpq001";
		_upwd = "qwe123";
	}
	//ע�����û����û���������ͨ�����캯������
	public RequestResult register(String qq, String email, String tel) {
		RequestResult result = preAuth();
		if (result.ret < 0)
			return result;
		
		String url = _urlPrex + "register";
		String encInfo = calcEncInfo(result.desc, _softKey, _uname, _upwd);
		if (encInfo == null) {
			return new RequestResult(errCalcEncInfo, "");
		}
		
		url = url + "?appID=" + _appID + "&qq=" + qq + "&email=" + email + "&tel=" + tel + "&encinfo=" + encInfo;
		
		return doRequest(url, new IResultHandler() {
			@Override
			public RequestResult handleSucc(Map<String, String> m) {
				return genRequestResult(0);
			}
		}, true, false);
	}
	
	//��ȡ�û����
	public ReadBalanceResult getBalance() {
		RequestResult result = handleRequest(new IActualHandler() 
		{
			@Override
			public RequestResult process() 
			{
				String url = _urlPrex + "getBalance";
				return doRequest(url, new IResultHandler() 
				{
					@Override
					public RequestResult handleSucc(Map<String, String> m) 
					{
						int [] balance = new int[1];
						try { balance[0] = Integer.parseInt(m.get("balance")); } catch (Exception e) { return genRequestResult(errBalanceNotNumber); }
						return new ReadBalanceResult(0, "", balance[0]);
					}
				});
			}
		});
		
		if (result instanceof ReadBalanceResult) {
			return (ReadBalanceResult)result;
		} else {
			return new ReadBalanceResult(result.ret, result.desc, 0);
		}
	}
	
	//��ȡ�û���Ϣ
	public ReadInfoResult readInfo() {
		RequestResult result = handleRequest(new IActualHandler() 
		{
			@Override
			public RequestResult process() 
			{
				String url = _urlPrex + "readInfo";
				
				return doRequest(url, new IResultHandler() 
				{
					@Override
					public RequestResult handleSucc(Map<String, String> m) 
					{
						return new ReadInfoResult(0, "", m.get("name"), m.get("qq"), m.get("email"), m.get("tel"));

					}
				});
			}
			
		});
		
		if (result instanceof ReadInfoResult) {
			return (ReadInfoResult)result;
		} else {
			return new ReadInfoResult(result.ret, result.desc, null, null, null, null);
		}
	}
	
	//ͨ��URL��ַ�������,������COOKIE��REFERER��Ϣ
	//urlPic��ͼƬ��URL��ַ���ڲ������URL����
	//type����֤������ID
	//timeout����ʱ����
	public RequestResult decodeUrl(final String urlPic, final int type, final int timeout) {
		return decodeUrl(urlPic, null, null, type, timeout);
	}
	
	//ͨ��URL��ַ�������
	//urlPic��ͼƬ��URL��ַ���ڲ������URL����
	//type����֤������ID
	//timeout����ʱ����
	//RequestResult.ret < 0 failed, ret is error code;  > 0, ret is id
	public RequestResult decodeUrl(final String urlPic, final String cookie, final String referer, final int type, final int timeout) {
		return handleRequest(new IActualHandler() 
		{

			@Override
			public RequestResult process() 
			{
				String url = _urlPrex + "decodeURL";
				
				return doRequest(url, new IResultHandler() 
				{
					@Override
					public RequestResult handleSucc(Map<String, String> m) 
					{
						int id = 0;
						try {id = Integer.parseInt(m.get("id")); } catch (Exception e) { return genRequestResult(errIdNotNumber); }
						return new RequestResult(id, "");
					}
				}, new IPostDataGettor() {

					@Override
					public byte[] getPostData() {
						return null;
					}

					@Override
					public Map<String, String> getProperties() {
						HashMap<String,String> m = new HashMap<String,String>();
						m.put("type", String.valueOf(type));
						m.put("timeout", String.valueOf(timeout));
						try {
							m.put("url", URLEncoder.encode(urlPic, "utf-8"));
						} catch (Exception e) {
							e.printStackTrace();
						}
						String temp;
						try {
							if (cookie != null && (temp = URLEncoder.encode(cookie, "utf-8")) != null) {
								m.put("cookie", temp);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						try {
							if (referer != null && (temp = (URLEncoder.encode(referer, "utf-8"))) != null) {
								m.put("referer", temp);
							}
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						
						return m;
					}
					
				}, true, true);
			}
		});
	}

	//ͨ��URL��ַ������룬���Ҷ�ȡ���
	//urlPic��ͼƬ��URL��ַ���ڲ������URL����
	//type����֤������ID
	//timeout����ʱ����
	//RequestResult.ret < 0 failed, ret is error code;  > 0, ret is id
	public DecodeResult decodeUrlAndGetResult(String urlPic, String cookie, String referer, int type, int timeout) {
		RequestResult result = this.decodeUrl(urlPic, cookie, referer, type, timeout);
		if (result.ret < 0) {
			return new DecodeResult(result.ret, result.desc, null, null);
		}
		
		return this.getResultUntilDone(result.ret, timeout * 1000);
	}

	//ͨ��URL��ַ������룬���Ҷ�ȡ���
	//urlPic��ͼƬ��URL��ַ���ڲ������URL����
	//type����֤������ID
	//timeout����ʱ����
	//RequestResult.ret < 0 failed, ret is error code;  > 0, ret is id
	public DecodeResult decodeUrlAndGetResult(String urlPic, int type, int timeout) {
		return decodeUrlAndGetResult(urlPic, null, null, type, timeout);
	}
	
	//ͨ��ͼƬ�����������
	//type����֤������ID
	//timeout����ʱ����
	//data��ͼƬ����
	//�ɹ�ʱ���ض����ret > 0����ʾ��֤��ID�����ڵ���getResult��getResultUtilDone�Ⱥ���
	private RequestResult decode(final int type, final int timeout, final byte [] data, final String txt) {
		return handleRequest(new IActualHandler() {
			@Override
			public RequestResult process() {
				String url = _urlPrex + "decode";
				
				return doRequest(url, new IResultHandler() {
					@Override
					public RequestResult handleSucc(Map<String, String> m) {
						int id;
						try {id = Integer.parseInt(m.get("id")); } catch (Exception e) { return genRequestResult(errIdNotNumber); }
						return new RequestResult(id, "");
					}
				}, new IPostDataGettor() {
					@Override
					public byte[] getPostData() {
						return data;
					}

					@Override
					public Map<String, String> getProperties() {
						Map<String, String> p = new HashMap<String,String>();
						p.put("type", Integer.toString(type));
						p.put("timeout", Integer.toString(timeout));
						if (txt != null && txt.length() > 0) {
							try {
								String temp;
								temp = URLEncoder.encode(txt, "utf-8");
								p.put("text", temp);
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}
						return p;
					}
				}, true, true);
			}
		});
	}

	//ͨ��ͼƬ�����������
	//type����֤������ID
	//timeout����ʱ����
	//data��ͼƬ����
	//�ɹ�ʱ���ض����ret > 0����ʾ��֤��ID�����ڵ���getResult��getResultUtilDone�Ⱥ���
	public RequestResult decode(final int type, final int timeout, final byte [] data) {
		return decode(type, timeout, data, null);
	}
	
	//ͨ��ͼƬ�����������
	//type����֤������ID
	//timeout����ʱ����
	//txt���ı�����
	//�ɹ�ʱ���ض����ret > 0����ʾ��֤��ID�����ڵ���getResult��getResultUtilDone�Ⱥ���
	public RequestResult decode(final int type, final int timeout, final String txt) {
		byte [] data = null;
		try {
			data = txt.getBytes("gbk");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return decode(type, timeout, data, txt);
	}

	//��ѯָ��ID�Ľ�������ܳɹ�ʧ�ܣ���������
	//id����֤��ID����decode��decodeUrl����
	//����ֵ��DecodeResult.ret >=0ʱ, �õ�����ɹ�������ID�� <0ʧ��
	public DecodeResult getResult(final int id) {
		RequestResult res = handleRequest(new IActualHandler() {
			@Override
			public RequestResult process() {
				String url = _urlPrex + "getResult?id=" + id;
				
				return doRequest(url, new IResultHandler() {
					@Override
					public RequestResult handleSucc(Map<String, String> m) {
						String cookie;
						try {cookie = URLDecoder.decode(m.get("cookie"), "utf-8"); } catch(Exception e){ cookie = ""; }
						return new DecodeResult(id, "", m.get("result"), cookie);
					}
				});
			}
		});
		
		if (res instanceof DecodeResult) {
			return (DecodeResult)res;
		} else {
			return new DecodeResult(res.ret, res.desc, null, null);
		}
	}
	
	//�õ�ָ��ID������Ľ����ֱ��������ɡ���ʱδȡ�ý�����߷��������ش���
	//id����֤��ID����decode��decodeUrl����
	//timeout: ��λ������
	//����ֵ��DecodeResult.ret >=0ʱ, �õ�����ɹ�������ID�� <0ʧ��
	public DecodeResult getResultUntilDone(int id, int timeout) {
		long startTime = System.currentTimeMillis();
		while (true) {
			DecodeResult decodeResult = getResult(id);
			if (decodeResult.ret >= 0) {
				return decodeResult;
			}
			if (decodeResult.ret != -303) //��֤����δ�������
				return decodeResult;
			
			if (System.currentTimeMillis() - startTime > timeout) {
				return new DecodeResult(errGetResultTimeout, getErrorDesc(errGetResultTimeout), null, null);
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	//������벢��ȡ��������ֱ��������ػ��ߴ���
	//type����֤������ID
	//timeout����ʱ����
	//data��ͼƬ����
	//����ֵ��DecodeResult.ret >=0ʱ, �õ�����ɹ�������ID��������reportError�� <0ʧ��
	public DecodeResult decodeAndGetResult(int type, int timeout, byte [] data) {
		RequestResult result = decode(type, timeout, data);
		if (result.ret < 0) {
			return new DecodeResult(result.ret, result.desc, null, null);
		}
		
		return getResultUntilDone(result.ret, timeout * 1000);
	}

	//������벢��ȡ��������ֱ��������ػ��ߴ���
	//type����֤������ID
	//timeout����ʱ����
	//txt���ı�����
	//����ֵ��DecodeResult.ret >=0ʱ, �õ�����ɹ�������ID��������reportError�� <0ʧ��
	public DecodeResult decodeAndGetResult(int type, int timeout, String txt) {
		RequestResult result = decode(type, timeout, txt);
		if (result.ret < 0) {
			return new DecodeResult(result.ret, result.desc, null, null);
		}
		
		return getResultUntilDone(result.ret, timeout * 1000);
	}
	
	//��ȡ�û����
	public RequestResult reportError(final int id) {
		RequestResult result = handleRequest(new IActualHandler() 
		{
			@Override
			public RequestResult process() 
			{
				String url = _urlPrex + "reportError?id=" + id;
				return doRequest(url, new IResultHandler() 
				{
					@Override
					public RequestResult handleSucc(Map<String, String> m) 
					{
						return genRequestResult(0);
					}
				});
			}
		});
		
		return result;
	}
	
	//ִ��HTTP���󲢽������ؽ��
	private RequestResult doRequest(String url, IResultHandler handler) {
		return doRequest(url, handler, true, true);
	}
	
	//ִ��HTTP���󲢽������ؽ����֧�������Ƿ񱣴���Ȩ��Ϣ���������м�����Ȩ��Ϣ
	private RequestResult doRequest(String url, IResultHandler handler, boolean saveAuth, boolean loadAuth) {
		return doRequest(url, handler, null, saveAuth, loadAuth);
	}
	
	//ִ��HTTP����֧��POST���������ݻ��
	@SuppressWarnings("unchecked")
	private RequestResult doRequest(String url, IResultHandler handler, IPostDataGettor postDataGettor, boolean saveAuth, boolean loadAuth) {
		try {
			//��URL��ַ�и�����Ȩ��Ϣ
			String auth = null;
			if (loadAuth) {
				synchronized(this) {
					auth = _auth;
				}
				if (postDataGettor == null) {
					if (url.indexOf('?') > 0) {
						url += "&auth=" + auth;
					} else {
						url += "?auth=" + auth;
					}
				}
			}
			
			//�����ӣ�������������
			HttpURLConnection conn = (HttpURLConnection)(new URL(url)).openConnection();
			conn.setUseCaches(false);
			conn.setConnectTimeout(15 * 1000);
			conn.setReadTimeout(40 * 1000);
			
			//����POST�����Ĵ���
			 if (postDataGettor != null) {
				//��������
				Map<String,String> properties = postDataGettor.getProperties();
				Map<String,String> newProp = null;
				if (properties != null && auth != null) {
					newProp = new HashMap<String,String>(properties);
					newProp.put("auth", auth);
				} else if (properties == null) {
					newProp = new HashMap<String,String>();
					newProp.put("auth", auth);
				} else {
					newProp = properties;
				}
				
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Charsert", "UTF-8");
				//��������ļ����ݵ����
				if (postDataGettor.getPostData() != null) {
					 String BOUNDARY = "------WebKitFormBoundary"; //���ݷָ���
					 conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
					 
					 //д����
					 OutputStream os = conn.getOutputStream();
					 if (newProp != null) {
						 Iterator<String> it = newProp.keySet().iterator();
						 while (it.hasNext()) {
							 String key = it.next();
							 String value = newProp.get(key);
							 StringBuilder sb = new StringBuilder();
							 sb.append("\r\n--").append(BOUNDARY)
							 	.append("\r\nContent-Disposition: form-data;name=\"").append(key).append("\";")
							 	.append("\r\nContent-Type:plain/text\r\n\r\n").append(value);
							 os.write(sb.toString().getBytes());
						 }
					 }
					 
					 //д�ļ�����
					 StringBuilder sb = new StringBuilder();
					 sb.append("\r\n--").append(BOUNDARY).append("\r\nContent-Disposition: form-data;name=\"data\";filename=\"pic.jpg\"\r\nContent-Type:image/jpg\r\n\r\n");
					 os.write(sb.toString().getBytes());
					 os.write(postDataGettor.getPostData());
					 
					 //д������
					 StringBuilder sbEnd = new StringBuilder();
					 sbEnd.append("\r\n--").append(BOUNDARY).append("--\r\n");
					 os.write(sbEnd.toString().getBytes());
					 os.flush();
				} else if (newProp != null && newProp.size() > 0) {	//����û���ļ����ݵ������Ե����
					StringBuilder sb = new StringBuilder();
					Iterator<String> it = newProp.keySet().iterator();
					while (it.hasNext()) {
						String key = it.next();
						String value = newProp.get(key);
						if (sb.length() > 0)
							sb.append("&");
						sb.append(key).append("=").append(value);
					}
					
					byte [] data = sb.toString().getBytes();
					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					conn.setRequestProperty("Content-Length", String.valueOf(data.length));
					
					OutputStream os = conn.getOutputStream();
					os.write(data);
					os.flush();
				}
			} 
			 
			//���ӷ��������������ͨѶ��������Ӧ��
			conn.connect();
			int ret = conn.getResponseCode();
			if (ret != 200) {
				return new RequestResult(errRespError, conn.getResponseMessage() + "(" + ret + ")");
			}
			
			//��ȡ�������ݶ���
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			ObjectMapper om = new ObjectMapper();
			Map<String,String> m = om.readValue(in, Map.class);
			ret = Integer.parseInt(m.get("ret"));
			String desc = m.get("desc");
			if (ret < 0) {
				return new RequestResult(ret, desc);
			}
			
			//������Ȩ��Ϣ
			if (saveAuth) {
				synchronized(this) {
					_auth = m.get("auth");
				}
			}
			
			//�ɹ�����ĺ�������
			return handler.handleSucc(m);
		} catch (JsonParseException e) {
			e.printStackTrace();
			return genRequestResult(errJsonParseError);
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return genRequestResult(errJsonMapError);
		} catch (IOException e) {
			e.printStackTrace();
			return genRequestResult(errIOException);
		}
	}
	
	//���������Ϣ
	private String calcEncInfo(String preauth, String softKey, String uname, String upwd) {
		//ѹ�����KEYΪ8�ֽڣ�����DES���ܵ�KEY
		byte [] key16 = hexString2ByteArray(softKey); 
		byte [] key8 = new byte[8];
		for (int i = 0; i < 8; i++) {
			key8[i] = (byte)((key16[i] ^ key16[i + 8]) & 0xff);
		}

		try {
			byte [] pwd_data = upwd.getBytes("utf-8");
			java.security.MessageDigest md5 = java.security.MessageDigest.getInstance("MD5");
			md5.update(pwd_data, 0, pwd_data.length);
			String pwd_md5_str = byteArray2HexString(md5.digest()); //תΪ16�����ַ���

			String enc_data_str = preauth + "\n" + uname + "\n" + pwd_md5_str;

			SecureRandom sr = new SecureRandom();
			DESKeySpec dks = new DESKeySpec(key8);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding"); 
			cipher.init(Cipher.ENCRYPT_MODE , securekey, sr);
			byte [] resultData = cipher.doFinal(enc_data_str.getBytes("utf-8"));
			return byteArray2HexString(resultData);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} 
		
		return null;
	}
	
	//���õĴ����������������Ҫ��½��������Ԥ��Ȩ�����½����󷢳�ʵ������
	private RequestResult handleRequest(IActualHandler handler) {
		int tryTime = 0;
		RequestResult res = null;
		while (tryTime < 2) {
			tryTime++;
			
			boolean bNeedLogin = false;
			synchronized(this) {
				if (_auth == null) {
					bNeedLogin = true;
				}
			}
			
			if (bNeedLogin) {
				do
				{
					res = preAuth();
				}
				while (processBusy(res.ret));
				if (res.ret < 0) {
					return res;
				}
				
				do
				{
					res = login(res.desc, _appID, _softKey, _uname, _upwd);
				}
				while (processBusy(res.ret));
				if (res.ret < 0) {
					return res;
				}
			}
			
			do 
			{
				res = handler.process();

			} while(processBusy(res.ret));
			
			if (res.ret != -10003 && 	// encinfo timeout
				res.ret != -10004 &&	//ip inconsistent
				res.ret != -10001) 		//invalid auth
			{
				return res;
			}
			
			synchronized(this) {
				_auth = null;
			}
		}
		
		return res;
	}
	
	//����ϵͳæ�����룬�����������ϵͳæ��˯��500ms������true�����򣬷���false
	private static boolean processBusy(int ret) {
		if (ret == -10000) { //busy
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}
		
		return false;
	}
	
	//Ԥ��Ȩ
	//�ɹ�ʱԤ��Ȩ��Ϣ��RequestResult.desc����
	private RequestResult preAuth() {	
		String url = _urlPrex + "preauth";
		return doRequest(url, new IResultHandler() {
			@Override
			public RequestResult handleSucc(Map<String, String> m) {
				return new RequestResult(0, m.get("auth"));
			}
		}, false, false);
	}
	
	//��½
	private RequestResult login(String preAuthInfo, int appID, String softKey, String uname, String upwd) {
		String url = _urlPrex + "login";
		String encInfo = calcEncInfo(preAuthInfo, softKey, uname, upwd);
		if (encInfo == null) {
			return genRequestResult(errCalcEncInfo);
		}
		
		url = url + "?appID=" + appID + "&encinfo=" + encInfo;
		
		return doRequest(url, new IResultHandler() {
			@Override
			public RequestResult handleSucc(Map<String, String> m) {
				return genRequestResult(0);
			}
		}, true, false);
	}
	
	//16�����ַ���תΪBYTE����
	private byte[] hexString2ByteArray(String hexStr) throws NumberFormatException {
		int len = hexStr.length();
		if (len % 2 != 0)
			throw new NumberFormatException();
		
		byte [] result = new byte[len / 2];
		
		for (int i = 0; i < len; i += 2) {
			String s = hexStr.substring(i, i + 2);
			int n = Integer.parseInt(s, 16);
			result[i / 2] = (byte)(n & 0xff);
		}
		
		return result;
	}
	
	//ת��BYTE����Ϊ16�����ַ���
	private String byteArray2HexString(byte [] data) {
		StringBuilder sb = new StringBuilder();
		for (byte b : data) {
			String s = Integer.toHexString(b & 0xff);
			if (s.length() == 1) {
				sb.append("0" + s);
			} else {
				sb.append(s);
			}
		}
		return sb.toString();
	}
	
	//���ݴ�����õ��õ���������
	private String getErrorDesc(int ret) {
		switch (ret) {
		case 0: return "�ɹ�";
		case errCalcEncInfo: return "calc encinfo error";
		case errBalanceNotNumber: return "balance is not number";
		case errIdNotNumber: return "id is not number";
		case errEncodingError: return "encoding error";
		case errRespError: return "response error";
		case errJsonParseError: return "Json parse error";
		case errJsonMapError: return "Json map error";
		case errIOException: return "IO Exception";
		case errGetResultTimeout: return "Get Result timeout";
		}
		return "" + ret;
	}
	
	//���ݷ���������������
	private RequestResult genRequestResult(int ret) {
		return new RequestResult(ret, getErrorDesc(ret));
	}

	
	//////////////////////////////////
	//          �ڲ��ӿڶ���
	//////////////////////////////////
	private interface IActualHandler { //ʵ�ʴ�����
		RequestResult process();
	}
	
	private interface IResultHandler	//������� ��
	{
		RequestResult handleSucc(Map<String,String> m);
	}
	
	private interface IPostDataGettor	//POST�����ṩ��
	{
		byte [] getPostData();
		Map<String,String> getProperties();
	}
	
	public static void main(String [] argvs) {
		String key = "9503ce045ad14d83ea876ab578bd3184";
		String uname = "user";
		String upwd = "test";
		int idApp = 205;
		Dama2Web web = new Dama2Web(idApp, "", uname, upwd);
		String preAuth = "1234567890abcdef1234567890abcdef";
		String s = web.calcEncInfo(preAuth, key, uname, upwd);
		System.out.println(s);
	}
	
	
	//��Ա��������
	private String _auth;			//���ص���Ȩ��Ϣ
	
	private final int _appID;		//Ӧ��ID
	private final String _softKey;	//���KEY
	private final String _uname;	//�û���
	private final String _upwd;		//�û�����
}
