/**
 * Author:dongxiaobing
 *功能1：查看代表作品是否按月销量排序
 *功能2：查看写过更多版本中的作品是否按月销量排序
 *要求1：只会查看排在第一个写作轨迹中的更多版本
 *要求2：至少有两本书，这样能保证代表作品和写作轨迹中都有书
 */
package com.dxb;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;



public class Author {
	private static String url="http://10.255.254.174:8390/?q=%D1%CF%B8%E8%DC%DF%D7%F7%C6%B7&pg=1&ps=60&ip=111.207.228.104&pid=20150402101944138259700808142260221&domain=search.dangdang.com&_url_token=5&vert=1&cate_type=0&platform=4&is_default_search=1&is_e_default=1&_new_tpl=1&service_type=custom_search,new_sequence,author_page,auothor_page_contents&session_id=66d416d8cc16ed9847232802814fb362&direct_brand=1&st=full&um=search_ranking&gp=cat_paths,label_id,clothes_size";

	public static final String CHARSET = "GBK";
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DBAction db=new DBAction();
		ParseXml px=new ParseXml();
		String xml_result=px.doGet(url,CHARSET);
		System.out.println("###############################################################################");
		representatives(px,xml_result,db);
		System.out.println("###############################################################################");
		System.out.println();
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		moreversions(px, xml_result,db);
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		/*
		//写作轨迹
		Map<String,String> pid_first_publish_date=new LinkedHashMap<String,String>();
		String xml_result2=px.doGet(url,CHARSET);
		pid_first_publish_date=px.get_traces(xml_result2);
		
		Iterator<String> ir3=pid_first_publish_date.keySet().iterator();
		 
		System.out.println("写作轨迹的默认展示:");
		while(ir.hasNext()){
			String pid=ir.next();
			String first_publish_date=pid_first_publish_date.get(pid);
			System.out.println(String.format("产品id:%s------>后台月销量：%s", pid,first_publish_date));
		}
		*/
	}
	public static void representatives(ParseXml px,String xml_result,DBAction db) throws Exception{
		// 代表作品
		Map<String, String> pid_sale_month = new LinkedHashMap<String, String>();
		pid_sale_month = px.get_representatives(xml_result);
		Iterator<String> ir = pid_sale_month.keySet().iterator();

		System.out.println("代表作品的pid和月销量分别是:");
		while (ir.hasNext()) {
			String pid = ir.next();
			String sale_month = pid_sale_month.get(pid);
			System.out.println(String.format("产品id:%s------>后台月销量：%s", pid,
					sale_month));
		}

		Iterator<String> ir2 = pid_sale_month.keySet().iterator();

		String sql_cmd = db.getSql(ir2);
		Map<String, String> sale_month_sql = db.getResult(sql_cmd);

		Iterator<String> ir_psm = pid_sale_month.keySet().iterator();
		while (ir_psm.hasNext()) {
			String product_id = ir_psm.next();
			String pid_sale_month1 = sale_month_sql.get(product_id);
			System.out.println(String.format(
					"数据库作品id:%15s------>月销量：是否有图:是否有库存:是否是独家品%s", product_id,
					pid_sale_month1));
		}
	}
	
	public static void moreversions(ParseXml px,String xml_result,DBAction db) throws Exception{
		// 更多作品
		Map<String, String> more_version = new LinkedHashMap<String, String>();
		more_version = px.get_moreversion(xml_result);
		// 迭代keys
		Iterator<String> i_mv = more_version.keySet().iterator();
		System.out.println("更多作品的作品名称---->作品id，月销量:");
		while (i_mv.hasNext()) {
			String product_name = i_mv.next();
			String pid_sale_month1 = more_version.get(product_name);
			System.out.println(String.format("作品名称:%15s------>作品id和后台月销量：%s",
					product_name, pid_sale_month1));
		}
		Iterator<String> ir_mv = more_version.keySet().iterator();
		String sql_cmd_mv = db.getSql(ir_mv);
		Map<String, String> mysql_result_mv = db.getResult(sql_cmd_mv);

		Iterator<String> ir_mv2 = more_version.keySet().iterator();
		while (ir_mv2.hasNext()) {
			String product_id = ir_mv2.next();
			String pid_sale_month1 = mysql_result_mv.get(product_id);
			System.out.println(String.format(
					"数据库作品id:%15s------>月销量：是否有图:是否有库存:是否是独家品%s", product_id,
					pid_sale_month1));
		}
	}
	
}

class ParseXml{
	private static  CloseableHttpClient httpClient;
	public ParseXml(){
		RequestConfig config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(15000).build();
		this.httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	}
	public String doGet(String url, String charset) {
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
	public  Map<String,String> get_representatives(String xml) throws Exception{
		//代表作品
		Document doc = DocumentHelper.parseText(xml);
		Element root = doc.getRootElement();
        Element Author = root.element("Author");      
        Element representatives = Author.element("representatives");      
        List<Element> list_books=representatives.elements();

        List<String> li=new ArrayList<String>();
        Map<String,String> pid_sale_month=new LinkedHashMap<String,String>();
        for (int i=0;i<list_books.size();i++){
        	String pid=list_books.get(i).element("product_id").getText();
        	String sale_month=list_books.get(i).element("month_sale").getText();
        	pid_sale_month.put(pid, sale_month);
        }
        return pid_sale_month;		
	}	
	public  Map<String,String> get_traces(String xml) throws Exception{
		//写作轨迹
		Document doc = DocumentHelper.parseText(xml);
		Element root = doc.getRootElement();
        Element Author = root.element("Author");      
        Element traces = Author.element("traces");      
        List<Element> list_trace=traces.elements();
        Map<String,String> pid_first_publish_date=new LinkedHashMap<String,String>();
        for (int i=0;i<list_trace.size();i++){
        	String author_name=list_trace.get(i).element("author_name").getText();
      
        	Element pid_e=list_trace.get(i).element("product_id");
        	String pid2;
        	if(pid_e==null){
        		pid2="00001";
        	}
        	else{
        		pid2=list_trace.get(i).element("product_id").getText();
        	}
        	System.out.println(pid_e);
 
        	String first_publish_date=list_trace.get(i).element("first_publish_date").getText();
        	System.out.println(first_publish_date);
        	
        	pid_first_publish_date.put(pid2, first_publish_date);
        }
        return pid_first_publish_date; 
	}	
	public  Map<String,String> get_moreversion(String xml) throws Exception{
		//更多版本
		Document doc = DocumentHelper.parseText(xml);
		Element root = doc.getRootElement();
        Element Author = root.element("Author");      
        Element traces = Author.element("traces"); 
        Element trace = traces.element("trace"); 
        Element other_versions = trace.element("other_versions"); 
        List<Element> list_other_versions=other_versions.elements("version");
        Map<String,String> pid_sale_month=new LinkedHashMap<String,String>();
        for (int i=0;i<list_other_versions.size();i++){
        	StringBuffer sb=new StringBuffer();
        	String product_name=list_other_versions.get(i).element("product_name").getText();
      
        	Element pid_e=list_other_versions.get(i).element("product_id");
        	String pid2;
        	if(pid_e==null){
        		pid2="00001";
        	}
        	else{
        		pid2=list_other_versions.get(i).element("product_id").getText();
        	}
        	String sale_month=list_other_versions.get(i).element("month_sale").getText();
        	sb.append(product_name);
        	sb.append(",");
        	sb.append(sale_month);
        	pid_sale_month.put(pid2,sb.toString());
        }
        return pid_sale_month; 
	}
}

class DBAction{
	private final static String db_url="jdbc:mysql://10.255.254.22:3306/prodviewdb";
	private final static String db_user="writeuser";
	private final static String db_pwd="ddbackend";
	private Connection conn=null;
	private String sql=null;
	private PreparedStatement pstmt=null;
	public DBAction() throws Exception{
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(db_url, db_user,db_pwd);
	}
	public String getSql(Iterator<String> ir){
		StringBuffer sql_cmd=new StringBuffer();
		sql_cmd.append("select product_id,sale_month,num_images,stock_status,icon_flag_sole from  spu_search_v3_view where  product_id in (");	
		while (ir.hasNext()){
			String a=ir.next();
			sql_cmd.append(a);
			if(ir.hasNext()){
				sql_cmd.append(",");
			}
		}
		sql_cmd.append(");");
		return sql_cmd.toString();
	}
	public Map<String,String> getResult(String sql_condition) throws Exception{
		pstmt=conn.prepareStatement(sql_condition);
		//执行查询，获取查询结果
		ResultSet rs=pstmt.executeQuery();
		Map<String,String> m=new LinkedHashMap<String, String>();
		while(rs.next()){
			StringBuffer sale_month_sql=new StringBuffer();
			String product_id=rs.getString(1);
			String sale_month=rs.getString(2);
			String num_images=rs.getString(3);
			String stock_status=rs.getString(4);
			String icon_sale_flag=rs.getString(5);
			String c=sale_month+":"+num_images+":"+stock_status+":"+icon_sale_flag;
			sale_month_sql.append(c);
			m.put(product_id, sale_month_sql.toString());
		}
		rs.close();
		pstmt.close();
		return m;
	}
}