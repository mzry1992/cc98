package android.application.cc98.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class SendHttpRequest {
   
	//����ģ��get����
	public static HttpResult sendGet(String url,Map<String,String> headers,Map<String,String>  params,String encoding,boolean duan) throws ClientProtocolException, IOException{
	//ʵ����һ��Httpclient��
		DefaultHttpClient client = new DefaultHttpClient();
		//����в����ľ�ƴװ����
		url = url+(null==params?"":assemblyParameter(params));
		//����ʵ����һ��get����
		HttpGet hp = new HttpGet(url);
		//�����Ҫͷ������װ����
		if(null!=headers)hp.setHeaders(assemblyHeader(headers));
		//ִ������󷵻�һ��HttpResponse
		HttpResponse response = client.execute(hp);
		//���Ϊtrue��ϵ����get����
		if(duan) hp.abort();
		//����һ��HttpEntity
		HttpEntity  entity = response.getEntity();
		//��װ���صĲ���
		HttpResult result= new HttpResult();
		//���÷��ص�cookie
		result.setCookie(assemblyCookie(client.getCookieStore().getCookies()));
		//���÷��ص�״̬
		result.setStatusCode(response.getStatusLine().getStatusCode());
		//���÷��ص�ͷ������
		result.setHeaders(response.getAllHeaders());
		//���÷��ص���Ϣ
		result.setHttpEntity(entity);
		return result;
	}
	public static HttpResult sendGet(String url,Map<String,String> headers,Map<String,String>  params,String encoding) throws ClientProtocolException, IOException{
		return sendGet(url, headers, params, encoding,false);
	}
	
	//����ģ��post����
	public static HttpResult sendPost(String url,Map<String,String> headers,Map<String,String>  params,String encoding) throws ClientProtocolException, IOException{
		//ʵ����һ��Httpclient��
		DefaultHttpClient client = new DefaultHttpClient();
		//ʵ����һ��post����
		HttpPost post = new HttpPost(url);
		
		//������Ҫ�ύ�Ĳ���
		List<NameValuePair> list  = new ArrayList<NameValuePair>();
		for (String temp : params.keySet()) {
			list.add(new BasicNameValuePair(temp,params.get(temp)));
		}
		post.setEntity(new UrlEncodedFormEntity(list,encoding));
		System.out.println("Post Data:" + EntityUtils.toString(post.getEntity()));
		
		//����ͷ��
		if(null!=headers)post.setHeaders(assemblyHeader(headers));
		//post.setRequestHeader("Content-Type","application/x-www-form-urlencoded;charset=gbk"); 

		//ʵ�����󲢷���
		HttpResponse response = client.execute(post);
		HttpEntity  entity = response.getEntity();
		
		//��װ���صĲ���
		HttpResult result = new HttpResult();
        //���÷���״̬����
        result.setStatusCode(response.getStatusLine().getStatusCode());
        //���÷��ص�ͷ����Ϣ
        result.setHeaders(response.getAllHeaders());
        //���÷��ص�cookie����
		result.setCookie(assemblyCookie(client.getCookieStore().getCookies()));
		//���÷��ص���Ϣ
		result.setHttpEntity(entity);
		return result ;
	}

	//������װͷ��
	public static Header[] assemblyHeader(Map<String,String> headers){
		Header[] allHeader= new BasicHeader[headers.size()];
		int i  = 0;
		for (String str :headers.keySet()) {
			allHeader[i] = new BasicHeader(str,headers.get(str));
			i++;
		}
		return allHeader;
	}
	
	//������װcookie
	public static String assemblyCookie(List<Cookie> cookies){
		StringBuffer sbu = new StringBuffer();
		for (Cookie cookie : cookies) {
			sbu.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
		}
		if(sbu.length()>0)sbu.deleteCharAt(sbu.length()-1);
		return sbu.toString();
	}
	//������װ����
	public static String assemblyParameter(Map<String,String> parameters){
		String para = "?";
		for (String str :parameters.keySet()) {
			para+=str+"="+parameters.get(str)+"&";
		}
		return para.substring(0,para.length()-1);
	}
}
