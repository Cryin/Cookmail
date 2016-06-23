/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package email;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringEscapeUtils;



/**
 *
 * @author root
 */
public class MailYeahMailRecev extends Mail {

    boolean endrecv=false;
    private MailFuncInter m_mailFuncInterYeah;
    public  String urls="";
    public  String cookiesStr="";
    public  String Host="";
    public String BoxName;
    public MailYeahMailRecev( MailFuncInter mailFuncInter )
    {
        m_mailFuncInterYeah = mailFuncInter;
    }
    @Override
    void login() {

        if(m_cookie!=null)                      //以cookie是否为空进行判断是cookie收邮还是密码收邮
        {
            Cookielogin();
        }
        else
        {
            UserLogin();
        }
         m_mailFuncInterYeah.ShowMessage(m_accounts+" :下载完毕");
    }
    
    //密码登陆

    private void UserLogin()
    {
        String url="";
        String strHtml="";
        int index;
        String sid="";
        m_mailFuncInterYeah.ShowMessage(m_accounts+" :开始登录");
        url="http://mail.yeah.net/entry/cgi/ntesdoor?df=webmailyeah&from=web&funcid=loginone&iframe=1&language=-1&passtype=1&verifycookie=1&product=mailyeah&style=-1&uid="+m_accounts+"@yeah.net";
        NameValuePair[] InData={new NameValuePair("savelogin","0"),
			new NameValuePair("url2", "http://mail.yeah.net/errorpage/err_yeah.htm"),  //
			new NameValuePair("username", m_accounts+"@"+m_postfix),
                        new NameValuePair("user", m_accounts),
			new NameValuePair("password", m_password)};

        try {
		strHtml=postRequest(url, InData);		
                //2013-05-01 change post method by Cryin'
                if ((index=strHtml.indexOf("top.location.href")) != -1) {
			url=putstr(strHtml, "\"", "\"", index);
                        
		}
                strHtml=getRequest(url);
		if (strHtml != "") {
                    if (urls.indexOf("js6/main.jsp") != -1)
                    {
			url = urls.replace("main.jsp", "s");
			Host=url;
			url = url + "&func=mbox:getAllFolders";
			strHtml = getRequest(url);
			if (urls.indexOf("mail.yeah.net") != -1) {
                            m_mailFuncInterYeah.ShowMessage(m_accounts+" :登录成功");
                            
                            getMailAddress(strHtml);
                            if(urls.indexOf("js6/main")!=-1)
                            {
                                urls=urls.replace("js6/main.jsp","js6/s");
                            }
                            getBoxName(strHtml);
                        }
                    }
                    else
                        {
                            m_mailFuncInterYeah.ShowMessage(m_accounts+" :登陆失败!");
                        }
			

		}
                else
                {
                    m_mailFuncInterYeah.ShowMessage(m_accounts+" :登陆失败!");
                }
		
            } catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            }

    }

    private void Cookielogin()
    {
        String url = m_url;
        String strHtml="";
        cookiesStr=m_cookie;
        int index;
        if(url.length()==0)
        {
            m_mailFuncInterYeah.ShowMessage(m_accounts+" : url不能为空！");
            return;
        }
        m_mailFuncInterYeah.ShowMessage(m_accounts+" :开始登录");
	try {
		strHtml = getRequest(url);
		if (strHtml.indexOf("errorType=Login_Timeout") != -1) 
                {
                    m_mailFuncInterYeah.ShowMessage(m_accounts + " :Cookie已失效!");
                    return;
                }
                
                if (urls.indexOf("mail.yeah.net") != -1) 
                {
                    m_mailFuncInterYeah.ShowMessage(m_accounts + " :登录成功");
                    if (urls.indexOf("js6/main.jsp") != -1) 
                    {
                        url = urls.replace("main.jsp", "s");
                        Host = url;
                        url = url + "&func=mbox:getAllFolders";
                        strHtml = getRequest(url);
                    }
                    getMailAddress(strHtml);
                    getBoxName(strHtml);

		}
            } catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            }
        
    }
 //取通讯录2014.12.15
    private void getMailAddress(String strHtml)
    {
                String sid="";
                String url="";
                int index;
                                  //下载通讯录代码开始
                try {
                            if((index=urls.indexOf("sid"))!=-1)
                            {
                                sid=putstr(urls,"sid=","&",index);
                            }
                            url="http://mail.yeah.net/contacts/call.do?from=webmail";
                            
                            NameValuePair[] listData={
                                new NameValuePair("sid",sid),
                                new NameValuePair("uid", m_accounts+"@yeah.net"),
                                new NameValuePair("cmd", "newapi.export"),
                                new NameValuePair("encoding","UTF-8"),
                                new NameValuePair("mbtype",""),
                                new NameValuePair("outformat", "8") };
                            
                            m_mailFuncInterYeah.ShowMessage(m_accounts+" :开始下载通讯录!");
                            
                            strHtml=postRequest(url, listData);
                            GetMethod getMethod = new GetMethod(url);
                            byte[] byteContent=getMethod.getResponseBody();
                            Date now = new Date(System.currentTimeMillis());
                            DateFormat date = DateFormat.getDateInstance();
                            String strdate = date.format(now);
                            String path="/"+strdate+"/"+m_accounts+"/"+"通讯录"+"/";
                            saveMailAddress(strHtml,path);
 
                            m_mailFuncInterYeah.ShowMessage(m_accounts+" :通讯录下载完成!");
                            //下载通讯录代码结尾
        } catch (Exception ex) {
			m_mailFuncInterYeah.ShowMessage(m_accounts+" :下载通讯录失败！"+ ex.getMessage());
	}
    }
    /**
    * 取邮箱所有的箱子
    * @param strHtml
    */

    private void getBoxName(String strHtml)
    {
	int i=0;
	int boxIndex=0;
	String boxMess="";
	String [][]boxList=new String[200][3];
	try
	{
	if(urls.indexOf("mail.yeah.net")!=-1)
	{
		//if(urls.indexOf("js6/s")!=-1)
		//{
		//	urls=urls.replace("js6/main.jsp","js6/s");                        
                        urls=urls+"&func=mbox:getAllFolders";
                        
			try
			{
                            strHtml=getRequest(urls);
			} catch (HttpException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			boxMess=strHtml;
			int allIndex=0;
			//allIndex=boxMess.indexOf("var gFolders");
			//boxMess=putstr(boxMess,"var gFolders","</script><link",allIndex);
                        // update 2013-05-11 by Cryin'
			while((boxIndex=boxMess.indexOf("id",boxIndex))!=-1)
			{
				String boxid = putstr(boxMess, "id\">", "<", boxIndex).trim();
                                String boxname = putstr(boxMess, "name\">", "<", boxIndex).trim();
				//String mailcount=putstr(boxMess,"messageCount':",",",boxIndex).trim();
                                String mailcount="9999";
				if(boxid!="-1"&&boxname!="-1"&&mailcount!="-1"&&boxid!=""&&boxname!=""&&mailcount!="")
				{
					boxList[i][0]=boxid;
					boxList[i][1]=boxname;
					boxList[i][2]=mailcount;
					i++;
				}
				boxIndex++;
			}
			for(int j=0;j<i;j++)
			{
				m_mailFuncInterYeah.ShowMessage(m_accounts+boxList[j][1]+"：开始接收");
				getEmailId(urls,boxList[j][1],boxList[j][0],boxList[j][2]);
                                if(endrecv)
                                {
                                       return;
                                }
			}
			
			
		//}
		//else
		//{
		//	m_mailFuncInterYeah.ShowMessage(m_accounts+": 取箱子失败！");
		//}
	}
	}
	catch(Exception err)
	{
		m_mailFuncInterYeah.ShowMessage(m_accounts+" :取箱子失败"+err.getMessage());
	}
	
    }

    /**
    * 取得邮件的唯一标识mid
    * @param urlbox
    * @param boxName
    * @param boxId
    * @param mailCount
    */
    private void getEmailId(String urlbox, String boxName, String boxId,String mailCount)
    {
	int mailIndex=0;
	String mailUrl="";
	String listMailurl="";
	String mailFrom;					//发件人
	String mailSubject="";					//邮件主题
	String mailDate;					//日期
        int m_sep=0;
	String mailMessage;					//邮件列表
	String downUrl="";					//邮件内容
	try
	{
                BoxName=boxName;
		String strindate="<?xml version=\"1.0\"?><object><int name=\"fid\">"+boxId+"</int><boolean name=\"skipLockedFolders\">false</boolean><string name=\"order\">date</string><boolean name=\"desc\">true</boolean><int name=\"start\">0</int><int name=\"limit\">"+mailCount+"</int><boolean name=\"topFirst\">false</boolean><boolean name=\"returnTotal\">true</boolean><boolean name=\"returnTag\">true</boolean></object>";
		mailUrl=urlbox.replace("&func=mbox:getAllFolders", "");
		listMailurl=mailUrl+"&func=mbox:listMessages";
		NameValuePair[] indate={new NameValuePair("var",strindate)};
		mailMessage=postRequest(listMailurl,indate);
		while((mailIndex=mailMessage.indexOf("<object>",mailIndex))!=-1)
		{
			String mailId=putstr(mailMessage,"<string name=\"id\">","</string>",mailIndex);
			if(mailId!="-1"&&mailId!="")
			{
                            String selsql="select count(*) from MailRecord where Midvalue="+"'"+mailId+"'";
                            int mailcount=m_mailFuncInterYeah.Excutesql(selsql);
                            if(mailcount==0)
                            {
                                 mailFrom = putstr(mailMessage, "<string name=\"from\">", "</string>", mailIndex);
                                 mailFrom = StringEscapeUtils.unescapeHtml(mailFrom);
                                 mailSubject = putstr(mailMessage, "<string name=\"subject\">", "</string>", mailIndex);
                                 mailDate = putstr(mailMessage, "<date name=\"receivedDate\">", "</date>", mailIndex);
                                 downUrl=mailUrl
                                     + "&func=mbox:getMessageData&mid=" +mailId
                                     + "&mode=download";
                                 requestEmail(downUrl,boxName,m_sep,mailSubject);
                                 Date now = new Date(System.currentTimeMillis());
                                 DateFormat date = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
                                 String strdate = date.format(now);
                                 String insql="insert into MailRecord(EmailID,Username,Emailbox,EmailType,EmailDate,MidValue) values (null,'"+m_accounts+"','"
                                  +BoxName+"','"+m_MailType+"','"+strdate+"','"+mailId+"')";
                                 mailcount=m_mailFuncInterYeah.Excutemdl(insql);
                                 mailTotalCounts++;
                                 m_sep++;
                            }
                               
                         }
                    mailIndex++;
                    if(!m_mailFuncInterYeah.GetThreadmark())
                    {
                         if(idEndRecv())
                         {
                              return ;
                         }
                    }
                            
		}
			
	}
	catch(Exception err)
	{
		System.out.println("取邮件失败！"+err.getMessage());
	}
		
    }
    /**
      * Get
    * @param url
    * @return
    * @throws HttpException
    * @throws IOException
    */
    private String getRequest(String url) throws HttpException, IOException
    {
		//
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpClient httpClient = new HttpClient(connectionManager);
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		//
		GetMethod getMethod = new GetMethod(url);
		getMethod.getParams().setParameter(
				HttpMethodParams.SINGLE_COOKIE_HEADER, true);
		if (cookiesStr != "")
		{
			getMethod.setRequestHeader("Cookie", cookiesStr);
		}
		getMethod.setRequestHeader("Content-Type",
				"application/x-www-form-urlencoded");
		getMethod.setRequestHeader("user-agent",
				"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
		getMethod.setRequestHeader("Accept-Language", "en-us,ar-SA;q=0.9,de-DE;q=0.8,es-ES;q=0.7,tr-TR;q=0.6,ja-JP;q=0.5,en-GB;q=0.4,fr-FR;q=0.3,zh-CN;q=0.2,zh-TW;q=0.1");
		String requestStr = new String();
		StringBuffer resultBuffer = new StringBuffer();
		try
		{
			//
			int statusCode = httpClient.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK)
			{
				m_mailFuncInterYeah.ShowMessage(m_accounts+" :126 Request Method failed: "
						+ getMethod.getStatusLine());
			}
			//
			BufferedReader in = new BufferedReader(new InputStreamReader(
					getMethod.getResponseBodyAsStream(), getMethod
							.getResponseCharSet()));
			URI uri = getMethod.getURI();
			urls = uri.getURI();
			String inputLine = null;
			while ((inputLine = in.readLine()) != null)
			{
				resultBuffer.append(inputLine);
				resultBuffer.append("\n");
			}
			requestStr = resultBuffer.toString();
			Cookie[] cookiesTemp = httpClient.getState().getCookies();
			if (cookiesTemp != null && cookiesTemp.length > 0)
			{
				for (int i = 0; i < cookiesTemp.length; i++)
				{
					if (cookiesStr == "")
					{
						cookiesStr += cookiesTemp[i].getName() + "="
								+ cookiesTemp[i].getValue() + "; ";
					}
					else
					{
						if (cookiesStr.endsWith("; "))
						{
							cookiesStr += cookiesTemp[i].getName() + "="
									+ cookiesTemp[i].getValue() + "; ";
						}
						else
						{
							cookiesStr += "; " + cookiesTemp[i].getName() + "="
									+ cookiesTemp[i].getValue();
						}
					}
				}
			}
		}
		catch (HttpException e)
		{
			//
			m_mailFuncInterYeah.ShowMessage(m_accounts+" :Please check your provided http address!");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			//
			e.printStackTrace();
		}
		finally
		{
			//
			getMethod.releaseConnection();
		}
		return requestStr;
	}
	/**
	 *
	 * @param url
	 * @param InData
	 * @return Post Response
	 * @throws HttpException
	 * @throws IOException
	 */
	private String postRequest(String url, NameValuePair[] InData) throws HttpException, IOException
	{

		String html = "";
		StringBuffer resultBuffer = new StringBuffer();
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpClient httpClient = new HttpClient(connectionManager);
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		PostMethod postMethod = new PostMethod(url);

		postMethod.setRequestHeader("accept", "*/*");
		postMethod.setRequestHeader("connection", "Keep-Alive");
		postMethod.setRequestHeader("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1)");
		postMethod.setRequestHeader("Accept-Language", "en-us,ar-SA;q=0.9,de-DE;q=0.8,es-ES;q=0.7,tr-TR;q=0.6,ja-JP;q=0.5,en-GB;q=0.4,fr-FR;q=0.3,zh-CN;q=0.2,zh-TW;q=0.1");
		if (cookiesStr != "")
		{
			postMethod.setRequestHeader("Cookie", cookiesStr);
		}
		postMethod.setRequestBody(InData);
		int statusCode = httpClient.executeMethod(postMethod);
		//
		//
		if ((statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
				|| (statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
				|| (statusCode == HttpStatus.SC_SEE_OTHER)
				|| (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT))
		{
			Header locationHeader = postMethod.getResponseHeader("location");
			String location = null;
			if (locationHeader != null)
			{
				location = locationHeader.getValue();
				if (location.startsWith("http"))
				{
					GetMethod getMethod = new GetMethod(url);
					statusCode = httpClient.executeMethod(getMethod);

					if (statusCode != HttpStatus.SC_OK)
					{
						m_mailFuncInterYeah.ShowMessage(m_accounts+" :126 Request Method failed: "
								+ getMethod.getStatusLine());
					}
					//
					BufferedReader in = new BufferedReader(
							new InputStreamReader(getMethod
									.getResponseBodyAsStream(), getMethod
									.getResponseCharSet()));
					URI uri = getMethod.getURI();
					urls = uri.getURI();
					String inputLine = null;
					while ((inputLine = in.readLine()) != null)
					{
						resultBuffer.append(inputLine);
						resultBuffer.append("\n");
					}
					html = resultBuffer.toString();

					//System.out.println("The page was redirected to:" + location);
				}
				else
				{
					url = "http://" + postMethod.getURI().getHost() + location;
					GetMethod getMethod = new GetMethod(url);
					statusCode = httpClient.executeMethod(getMethod);

					if (statusCode != HttpStatus.SC_OK)
					{
						m_mailFuncInterYeah.ShowMessage(m_accounts+" :hotmail Request Method failed: "
								+ getMethod.getStatusLine());
					}
					//
					BufferedReader in = new BufferedReader(
							new InputStreamReader(getMethod
									.getResponseBodyAsStream(), getMethod
									.getResponseCharSet()));
					URI uri = getMethod.getURI();
					urls = uri.getURI();
					String inputLine = null;
					while ((inputLine = in.readLine()) != null)
					{
						resultBuffer.append(inputLine);
						resultBuffer.append("\n");
					}
					html = resultBuffer.toString();
					//System.out.println("The page was error:" + location);
				}

				return html;

			}
                        else
                        {
				m_mailFuncInterYeah.ShowMessage(m_accounts+" :Location field value is null.");
			}
			return html;
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(postMethod
				.getResponseBodyAsStream(), postMethod.getResponseCharSet()));
		StringBuffer strBuffer = new StringBuffer();
		String inputLine = null;
		while ((inputLine = in.readLine()) != null)
		{
			strBuffer.append(inputLine);
			strBuffer.append("\n");
		}
		html = strBuffer.toString();
		in.close();
		Cookie[] cookiesTemp = httpClient.getState().getCookies();
		if (cookiesTemp != null && cookiesTemp.length > 0)
		{
			for (int i = 0; i < cookiesTemp.length; i++)
			{
				if (cookiesStr == "")
				{
					cookiesStr += cookiesTemp[i].getName() + "="
						+ cookiesTemp[i].getValue() + "; ";
				}
				else
				{
					if (cookiesStr.endsWith("; "))
					{
						cookiesStr += cookiesTemp[i].getName() + "="
							+ cookiesTemp[i].getValue() + "; ";
					}
					else
					{
						cookiesStr += "; " + cookiesTemp[i].getName() + "="
							+ cookiesTemp[i].getValue();
					}
				}
			}
		}

//		System.out.println("---------------------------------->Post Cookies:");
//		for (Cookie cookie : cookiesTemp)
//		{
//
//			System.out.println(cookie.getName());
//			System.out.println(cookie.getValue());
//		}
//		System.out.println("---------------------------------->Post Cookies:");


		postMethod.releaseConnection();
		return html;
	}
	/**
	 *
	 * @param message 字符串
	 * @param startStr 起始字符
	 * @param endStr 末尾字符Hotmail
	 * @param startIndex 字符串起始位置
	 * @return 两个字符串之间的数据
	 */
	public String putstr(String message, String startStr, String endStr,
			int startIndex) // 取两个字符串之间的数据
	{
		if (startIndex < 0)
			return "-1";
		if (message.length() < startIndex)
			return "-1";
		String subStr = message.substring(startIndex, message.length());
		int indexStar = subStr.indexOf(startStr);
		if (indexStar < 0)
			return "-1";
		indexStar += startStr.length();
		subStr = subStr.substring(indexStar, subStr.length());
		int indexEnd = subStr.indexOf(endStr);
		if (indexEnd < 0)
			return "-1";
		return subStr.substring(0, indexEnd);
	}
	/**
	 * downland mail by byte
	 * @param url
	 * @param boxName
	 * @param j
	 * @throws HttpException
	 * @throws IOException
	 */
	private void requestEmail(String url,String boxName,int m_sep, String mailSubject) throws HttpException, IOException
	{
		// HttpClient
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpClient httpClient = new HttpClient(connectionManager);
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		// GET method
		GetMethod getMethod = new GetMethod(url);
		getMethod.getParams().setParameter(
				HttpMethodParams.SINGLE_COOKIE_HEADER, true);

		if (cookiesStr != "")
		{
			getMethod.setRequestHeader("Cookie", cookiesStr);
		}
		getMethod.setRequestHeader("Content-Type",
				"application/x-www-form-urlencoded");
		getMethod.setRequestHeader("user-agent",
				"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
		getMethod.setRequestHeader("Accept-Language", "en-us,ar-SA;q=0.9,de-DE;q=0.8,es-ES;q=0.7,tr-TR;q=0.6,ja-JP;q=0.5,en-GB;q=0.4,fr-FR;q=0.3,zh-CN;q=0.2,zh-TW;q=0.1");
		try
		{
			// ִexcute getMethod
			int statusCode = httpClient.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK)
			{
				m_mailFuncInterYeah.ShowMessage(m_accounts+ " :FastMail Request Method failed: "
						+ getMethod.getStatusLine());
			}

			byte[] byteContent=getMethod.getResponseBody();
			Date now = new Date(System.currentTimeMillis());
			DateFormat date = DateFormat.getDateInstance();
			String strdate = date.format(now);
			String path="/"+strdate+"/"+m_accounts+"/"+boxName+"/";
			saveMailText(m_sep,mailSubject,path,byteContent);
                        m_mailFuncInterYeah.ShowMessage(m_accounts+" :"+mailSubject+"下载！");

			Cookie[] cookiesTemp = httpClient.getState().getCookies();
			if (cookiesTemp != null && cookiesTemp.length > 0)
			{
				for (int i = 0; i < cookiesTemp.length; i++)
				{
					if (cookiesStr == "")
					{
						cookiesStr += cookiesTemp[i].getName() + "="
								+ cookiesTemp[i].getValue() + "; ";
					}
					else
					{
						if (cookiesStr.endsWith("; "))
						{
							cookiesStr += cookiesTemp[i].getName() + "="
									+ cookiesTemp[i].getValue() + "; ";
						}
						else
						{
							cookiesStr += "; " + cookiesTemp[i].getName() + "="
									+ cookiesTemp[i].getValue();
						}
					}
				}
			}
		}
		catch (HttpException e)
		{
			//
			m_mailFuncInterYeah.ShowMessage(m_accounts +" :Please check your provided http address!");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			//
			e.printStackTrace();
		}
		finally
		{
			// release
			getMethod.releaseConnection();
		}
		//return requestStr;
	}
	/**
	 * �save mail
	 * @param i mail id
	 * @param path save filepath
	 * @param content mail content
	 */
	public  void saveMailText(int i,String mailSubject,String path, byte[] content)
	{
	      try
	      {
	    	  String Mailpath=m_mailFuncInterYeah.GetMailPath();
	    	  File directory=new File(Mailpath);
	    	  File file = new File(directory.getCanonicalPath()+path);
	    	  if (file.exists())
	    	  {
	    		 // System.out.println("folder exist");
	    	  }
	    	  else
	    	  {
	    		 // System.out.println("creat folder");
	    		  if (file.mkdirs())
	    		  {
	    			  	//System.out.println("folder create done");
	    		  }
	    		  else
	    		  {
	    			 // System.out.println("folder create failed");
	    		  }
	    	  }
	    	  String fileName=Integer.toString(i)+"_"+mailSubject+".eml";
	    	  File newfile=new File(directory.getCanonicalPath()+path+fileName);
	    	  if (!newfile.exists())
	    	  {
	    		  newfile.createNewFile();
	    	  }
	    	  FileOutputStream output = new FileOutputStream(newfile,true);
		  output.write(content);
		      
	    	  output.flush();
                  output.close();
	      }
	      catch (Exception e)
	      {
	    	  e.printStackTrace();
	      }
	}
    /**
	 * �save mail address book
	 * @param i mail id
	 * @param path save filepath
	 * @param content mail content
	 */
	public  void saveMailAddress(String address,String path)
	{
	      try
	      {
	    	  String Mailpath=m_mailFuncInterYeah.GetMailPath();
	    	  File directory=new File(Mailpath);
	    	  File file = new File(directory.getCanonicalPath()+path);
	    	  if (file.exists())
	    	  {
	    		 // System.out.println("folder exist");
	    	  }
	    	  else
	    	  {
	    		 // System.out.println("creat folder");
	    		  if (file.mkdirs())
	    		  {
	    			  	//System.out.println("folder create done");
	    		  }
	    		  else
	    		  {
	    			 // System.out.println("folder create failed");
	    		  }
	    	  }
	    	  String fileName=m_accounts+".csv";
	    	  File newfile=new File(directory.getCanonicalPath()+path+fileName);
	    	  if (!newfile.exists())
	    	  {
	    		  newfile.createNewFile();
	    	  }
	    	  FileOutputStream output = new FileOutputStream(newfile,true);
                  output.write(address.getBytes());
		//  output.write(address);
		      
	    	  output.flush();
                  output.close();
	      }
	      catch (Exception e)
	      {
	    	  e.printStackTrace();
	      }
	}
    /**
     * 线程停止是判断是否为用户指定的序号
     * @return
     */
    private boolean idEndRecv() {
        try
        {
            ResultSet result;
            if(m_mailFuncInterYeah.GetRecvType())
            {
                if(!m_mailFuncInterYeah.GetuserAllThreadMark())
                {
                    m_mailFuncInterYeah.ShowMessage("正在停止线程...请耐心等待，不要进行其他操作！");
                    endrecv =true;
                    try {
                        Thread.sleep(3000);
                        m_mailFuncInterYeah.ShowMessage("线程停止了！");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(HotmailMailRecev.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return endrecv;
                }
                else
                {
                    String Threadsql="select RevState from emailaccounts where ID='"+m_ID+"'";
                    result=m_mailFuncInterYeah.ExcuteResult(Threadsql);

                    if (result.next())
                    {
                        int restate = result.getInt("RevState");
                        if(restate==0)
                        {
                            //m_mailFuncInterhot.SetThreadmark(true);
                            endrecv=true;
                            Thread.sleep(3000);
                            m_mailFuncInterYeah.ShowMessage("线程停止了！");


                        }
                        else
                        {
                            endrecv=false;

                        }
                    }
                }
             }
             else
             {
                 if(!m_mailFuncInterYeah.GetcookieAllThreadMark())
                {
                     m_mailFuncInterYeah.ShowMessage("正在停止线程...请耐心等待，不要进行其他操作！");
                     Thread.sleep(3000);
                     m_mailFuncInterYeah.ShowMessage("线程停止了！");
                     endrecv =true;

                    return endrecv;
                }
                else
                {
                    String Threadsql="select RevState from emailcookie where ID='"+m_ID+"'";
                    result=m_mailFuncInterYeah.ExcuteResult(Threadsql);

                    if (result.next())
                    {
                        int restate = result.getInt("RevState");
                        if(restate==0)
                        {
                           // m_mailFuncInterhot.SetThreadmark(true);

                            endrecv=true;
                            Thread.sleep(3000);
                            m_mailFuncInterYeah.ShowMessage("线程停止了！");
                        }
                        else
                        {
                            endrecv=false;
                        }
                    }
                }
             }

         }
        catch (InterruptedException ex) {
            Logger.getLogger(HotmailMailRecev.class.getName()).log(Level.SEVERE, null, ex);
        }         catch (SQLException ex)
         {
             Logger.getLogger(HotmailMailRecev.class.getName()).log(Level.SEVERE, null, ex);
         }
         return endrecv;
    }


}
