package com.dangdang;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;

import com.dangdang.client.DBAction;
import com.dangdang.data.BlackListQuery;
import com.dangdang.data.FuncCatePath;



public class CategoryBookDdSell {	
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(CategoryBookDdSell.class);
	private static List<String> urlList=new ArrayList<String>();
	public static final String CHARSET = "GBK";
	private static int count = 0;
	private static final CloseableHttpClient httpClient;
	static {
		RequestConfig config = RequestConfig.custom().setConnectTimeout(60000)
				.setSocketTimeout(15000).build();
		httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config)
				.build();
	}
	@Test(enabled=true)
	public void f() {
		String subject = "【搜索后台自动化测试】基础功能回归测试结果";
		StringBuffer content = new StringBuffer();
		content.append("<html><head><meta http-equiv=Content-Type content='text/html; charset=utf-8'></head><body><table border=1 cellspacing=0 cellpadding=0><tr><th>功能模块</th><th>通过query</th><th>失败query</th><th>跳过query</th><th>总计</th><th>耗时</th></tr>");
		try {
			long d = System.currentTimeMillis();
			logger.info(Long.toString(d));
			DBAction dba = new DBAction();
			dba.setFuncCondition("category_path like '01.%'");
			//获取所有的图书分类路径
			List<FuncCatePath> fcList =dba.getCatePathFromFC();

			//System.out.println(fcList.get(0).getCat_path());
			urlList=this.getUrl(fcList);
			System.out.println(urlList.get(0));
			
			//判断每一个url是否都包含当当自营checkbox
			for(String s:urlList){
				String value=this.parseHtml(s);
				Assert.assertEquals(value, "当当自营");
				count++;
				System.out.println(count);
			}
			
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			String exception = baos.toString();
			logger.error(" - [LOG_EXCEPTION] - " + exception);
			e.printStackTrace();
		} finally {
		}
	}
	
	//拼接url成完整形式的url格式
	private List<String> getUrl(List<FuncCatePath> fcList ){
		
		for(FuncCatePath cp:fcList){
			String front="http://category.dangdang.com/cp";
			String end=".html";
			String x=front+cp.getCat_path()+end;
			//System.out.println(x);
			urlList.add(x);
		}
		return urlList;
	}
    //返回列表页当当自营 checkbox的内容
	public static String parseHtml(String url) {

		String resultHtml = doGet(url, CHARSET);
		String value = "";
		Document doc = Jsoup.parse(resultHtml);	
		String values = doc.select(".opt>.checkbox+.checkbox+.checkbox>a").text();
		return values;

	}
	//获取html
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
				return doGet(url,charset);
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			String exception = baos.toString();
			logger.error(" - [LOG_EXCEPTION] - " + exception);
			e.printStackTrace();
		}
		return null;
	}
	@BeforeClass
	public void beforeClass() {

	}
	
	@AfterClass
	public void afterClass() {
	}

}
