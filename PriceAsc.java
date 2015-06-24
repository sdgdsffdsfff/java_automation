/**
 * 验证价格是不是升序排列的
 */
package com.dxb;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.jsoup.Jsoup;

import com.dangdang.util.XMLParser;
public class PriceAsc {
	private static String baseUrl="http://10.255.254.174:8390/?";
	private static final String EQUAL="=";
	private static final String AND="&";
	private static String url=null;
	public static final String CHARSET = "GBK";
	private static int count = 0;
	private static List<String> liTotal=new ArrayList<String>();
	private static final CloseableHttpClient httpClient;
	static {
		RequestConfig config = RequestConfig.custom().setConnectTimeout(60000)
				.setSocketTimeout(15000).build();
		httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config)
				.build();
	}
	public static void main(String[] args) throws Exception {
		HashMap<String,String> hm=new HashMap<String,String>();		
		//get page count
		String pageCount=getPageCount();
		System.out.println("pageCount------->"+pageCount);
		int pc=Integer.parseInt(pageCount);
		String url=null;
		List<String> li=null;
		for(int i=1;i<=pc;i++){
			hm=getUrlMap(hm,i);
			url=getUrl(hm);
			System.out.println(url);
			li=getProductList(url);
		    liTotal.addAll(li);		
		}
		System.out.println(liTotal.toString());
		System.out.println(liTotal.size());
		
		boolean flag=comparePrice(liTotal);
		if(flag){
			System.out.println("成功@@@@@@@@@@@@");
		}
		else{
			System.out.println("失败！！！！！！！！！！！");
		}
	}
	public static boolean comparePrice(List<String> liTotal){
		List<String> liOld=liTotal;
		Collections.sort(liTotal);
		List<String> liNew=liTotal;
		Set<String> s=new HashSet<String>();
		s.addAll(liOld);
		s.addAll(liNew);
		s.removeAll(liOld);
		if (s.size()==0){
			return true;
		}else{
			return false;
		}
	}
	
	public static  HashMap<String, String> getUrlMap(HashMap<String,String> hm,int page) throws Exception{
		String pa=page+"";
		String hanZi="茵曼";
		String hanZiGbk=URLEncoder.encode(URLDecoder.decode(hanZi, "utf-8"), "GBK");
		hm.put("st","full");
		hm.put("q",hanZiGbk);
		hm.put("_new_tpl","1");
		hm.put("ps","60");
		hm.put("pg", pa);
		hm.put("um", "search_ranking");
		hm.put("us", "xprice_asc");
		return hm;
	}
	public static String getUrl(HashMap<String,String> hm){
		StringBuffer sb=new StringBuffer();
		Set<String> s=hm.keySet();
		for(String str:s){
			sb.append(str+EQUAL+hm.get(str)+AND);
		}
		sb.insert(0, baseUrl);
		return sb.toString();
	}
	public static Document getDocument(String url) throws Exception {		
		String resultXml = doGet(url, CHARSET);
		Document doc = DocumentHelper.parseText(resultXml);
		return doc;	
	}
	public static List<String> getProductList(String url) throws Exception{	
        Document doc=getDocument(url);
		List n=doc.selectNodes("//result/Body/Product/dd_sale_price");
		Iterator it=n.iterator();
		String l=null;
		List<String> li=new ArrayList<String>();
		while(it.hasNext()){
			Element elm=(Element) it.next();
			l=elm.getText();
			li.add(l);		
		}	
		return li;
	}
	public static String getPageCount() throws Exception{
		HashMap<String,String> hm=new HashMap<String,String>();
		hm=getUrlMap(hm,1);
		url=getUrl(hm);
		//String url2="http://10.255.254.174:8390/?q=%C4%A7%B7%BD&pg=2&ps=60&ip=192.168.95.162&pid=20150527144324129389391750737658942&domain=search.dangdang.com&_url_token=5&vert=1&cate_type=0&platform=4&is_default_search=0&is_e_default=0&_new_tpl=1&service_type=custom_search,new_sequence&session_id=6c25ab70a7fc4569efb4599bcfbfae50&direct_brand=1&st=full&um=search_ranking&gp=cat_paths,label_id,clothes_size&us=xprice_asc";
		//get document
		Document doc=getDocument(url);
		List n=doc.selectNodes("//result/Header/Summary/Page/PageCount");
		Iterator it=n.iterator();
		String l=null;
		List<String> li=new ArrayList<String>();
		while(it.hasNext()){
			Element elm=(Element) it.next();
			l=elm.getText();
			li.add(l);		
		}	
		return li.get(0);
	}
	public static String doGet(String url, String charset) {
		if (StringUtils.isBlank(url)) {
			return null;
		}
		try {
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				httpGet.abort();
				throw new RuntimeException("HttpClient,error status code :"
						+ statusCode);
			}
			HttpEntity entity = response.getEntity();
			String result = null;
			if (entity != null) {
				result = EntityUtils.toString(entity, "utf-8");
			}
			EntityUtils.consume(entity);
			response.close();
			return result;
		} catch (Exception e) {
			if (e instanceof RequestAbortedException
					|| e instanceof SocketTimeoutException
					|| e instanceof SocketException) {
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return doGet(url, charset);
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			String exception = baos.toString();
			e.printStackTrace();
		}
		return null;
	}
}
