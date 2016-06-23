/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package email;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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


/**
 *
 * @author root
 */
public class HiNetMailRecev extends Mail{
    boolean endrecv=false;
    private MailFuncInter m_mailFuncInter163;
    public  String urls="";
    public  String cookiesStr="";
    public  String Host="";
    public String BoxName;
    public HiNetMailRecev( MailFuncInter mailFuncInter )
    {
        m_mailFuncInter163 = mailFuncInter;
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
         m_mailFuncInter163.ShowMessage(m_accounts+" :下载完毕");
    }

    private void Cookielogin() {
                String url=m_url;
                cookiesStr=m_cookie;
		String strHtml="";
		int index;
                if(url.length()==0)
                {
                    m_mailFuncInter163.ShowMessage(m_accounts+" : url不能为空！");
                    return;
                }
		try
		{
                        m_mailFuncInter163.ShowMessage(m_accounts+" : 开始登录");
			strHtml=getRequest(url);
			if(strHtml.indexOf("/mailService/mail/")>0)
			{
                            m_mailFuncInter163.ShowMessage(m_accounts+" : 登录成功");
				Host=urls.substring(0, urls.indexOf("mailService/mail/"));
				url=Host+"mailService/mail/M_main_8.jsp";
				strHtml=getRequest(url);
				GetBoxName(strHtml);
			}
                        else
                        {
                            if(strHtml.indexOf("01604")>0)
                            {
                                m_mailFuncInter163.ShowMessage(m_accounts+" : 登录失败");
                                m_mailFuncInter163.ShowMessage(m_accounts+" : 由於網頁郵件服務不提供同一帳號、同時間在不同視窗登入，若有重複登入、不正常登出(直接關閉視窗或是因系統錯誤而離開)之情形，請重新登入 (01604)");
                            }
                            else
                            {
                                if(strHtml.indexOf("/mailService")>0)
                                {
                                    m_mailFuncInter163.ShowMessage(m_accounts+" : 登录成功");
                                    Host=urls.substring(0, urls.indexOf("mailService"));
                                    url=Host+"mailService/mail/M_main_8.jsp";
                                    strHtml=getRequest(url);
                                    GetBoxName(strHtml);
                                }
                                else
                                {
                                     m_mailFuncInter163.ShowMessage(m_accounts+" : 登录失败");
                                }
                            }
                        }

		}
		catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }

    }

    private void UserLogin() {

                String url="";
		String strHtml="";
		int index;
		try
		{
                        m_mailFuncInter163.ShowMessage(m_accounts+" : 开始登录");
			url="http://www.webmail.hinet.net/login.do?usertype=1&mailid="+m_accounts+"&password="+m_password+"&OK.x=15&OK.y=12";
                        /*
                        NameValuePair[] indata={new NameValuePair("usertype","1"),
                                         new NameValuePair("mailid",m_accounts),
                                         new NameValuePair("password",m_password),
                                         new NameValuePair("OK.x","19"),
                                         new NameValuePair("OK.y","9")
                        };
                         * *
                         */
                        //usertype=1&mailid="+m_accounts+"&password="+m_password+"&OK.x=15&OK.y=12";
			strHtml=getRequest(url);
			if(strHtml.indexOf("/mailService/mail/")>0)
			{
                            m_mailFuncInter163.ShowMessage(m_accounts+" : 登录成功");
				Host=urls.substring(0, urls.indexOf("mailService/mail/"));
				url=Host+"mailService/mail/M_main_8.jsp";
				strHtml=getRequest(url);
				GetBoxName(strHtml);
			}
                        else
                        {
                            if(strHtml.indexOf("01604")>0)
                            {
                                m_mailFuncInter163.ShowMessage(m_accounts+" : 登录失败");
                                m_mailFuncInter163.ShowMessage(m_accounts+" : 由於網頁郵件服務不提供同一帳號、同時間在不同視窗登入，若有重複登入、不正常登出(直接關閉視窗或是因系統錯誤而離開)之情形，請重新登入 (01604)");
                            }
                            else
                            {
                                if(strHtml.indexOf("/mailService")>0)
                                {
                                    m_mailFuncInter163.ShowMessage(m_accounts+" : 登录成功");
                                    Host=urls.substring(0, urls.indexOf("mailService"));
                                    url=Host+"mailService/mail/M_main_8.jsp";
                                    strHtml=getRequest(url);
                                    GetBoxName(strHtml);
                                }
                                else
                                {
                                     m_mailFuncInter163.ShowMessage(m_accounts+" : 登录失败");
                                }
                            }
                        }

		}
		catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }
    }
        /**
	 *
	 * @param strHtml
	 */
	public void GetBoxName(String strHtml)
	{
		try
		{
			int i=0;
			String [][]boxList=new String[200][3];
			int boxIndex=strHtml.indexOf("/mailService/mail/M_wrt_1.jsp");
			while((boxIndex=strHtml.indexOf("<tr>",boxIndex))>0)
			{
				String folderinfo=putstr(strHtml,"<tr>","</tr>",boxIndex);
				int urlIndex;
				if((urlIndex=folderinfo.indexOf("valign="))>0)
				{
					String boxinfo=putstr(folderinfo,"<a href=","/a>",urlIndex);
					String boxid=putstr(boxinfo,"\"","\"",0);
					String boxname=putstr(boxinfo,">","<",0);
					boxid=boxid.trim();
					boxname=boxname.trim();
					if(boxid!="-1"&&boxname!="-1"&&boxid!=""&&boxname!="")
					{
						boxList[i][0]=boxid;
						boxList[i][1]=boxname;
						i++;
					}
					else
					{
						break;				//jump out loop
					}
				}
				boxIndex++;
			}
			for(int j=0;j<i;j++)
			{
				m_mailFuncInter163.ShowMessage(m_accounts+" :"+boxList[j][1]+" 开始下载");
				GetMailId(boxList[j][0],boxList[j][1]);
                                if(endrecv)
                                {
                                      return;
                                }
			}
		}
		catch(Exception err)
		{
			m_mailFuncInter163.ShowMessage(m_accounts+" :取箱子失败!"+err.getMessage());
		}

	}
	/**
	 *
	 * @param boxid
	 * @param boxname
	 */
	private void GetMailId(String boxid, String boxname)
	{
		int mailIndex=0;
		String mailUrl="";
		String listMailurl="";
		String mailId;
		String mailFrom;					//mail from
		String mailSubject;					//mail subject
		String mailMessage;
		String mailDate;					//mail date
		String downUrl="";					//downland url
		String strFolders;					//the box conent
		String nextUrl;
		listMailurl=Host+boxid;
                int m_sep=0;

		try
		{
			strFolders=getRequest(listMailurl);
			mailIndex=strFolders.indexOf("listr");
			while((mailIndex=strFolders.indexOf("<tr",mailIndex))>0)
			{
				mailMessage=putstr(strFolders,"<tr","</tr>",mailIndex);

				mailId=putstr(mailMessage,"\"mailid\" value=\"","\"",0);
				if(mailId!="-1"&&mailId!="")
				{
					String selsql="select count(*) from MailRecord where Midvalue="+"'"+mailId+"'";
                                         int count=m_mailFuncInter163.Excutesql(selsql);
					if(0==count)
					{
						mailFrom=putstr(mailMessage,"\"blacklist\" value=\"","\"",0);
						int SubjectIndex=mailMessage.indexOf("mail_read");
						mailSubject=putstr(mailMessage,"\">","</a>",SubjectIndex);
                                                int subjectindex=0;
                                                if((subjectindex=mailSubject.indexOf("[X-Spam]"))>0)
                                                {
                                                    mailSubject=mailSubject+"guanggao";
                                                    mailSubject=putstr(mailSubject,"[X-Spam]","guanggao",0);
                                                }
						mailSubject=mailSubject.trim();
						mailDate=putstr(mailMessage,"nowrap >","</td>",SubjectIndex);
						mailDate=mailDate.trim();
						//http://sg1003.webmail.hinet.net/mailService/sendAttach.do?pid=-1&msg=52EBDCC24E223950DD948D3577F19D8F
						downUrl=Host+"mailService/sendAttach.do?pid=-1&msg="+mailId;
						requestEmail(downUrl,boxname,m_sep,mailSubject);
						Date now = new Date(System.currentTimeMillis());
                                                 DateFormat date = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
                                                 String strdate = date.format(now);
                                                String insql="insert into MailRecord(EmailID,Username,Emailbox,EmailType,EmailDate,MidValue) values (null,'"+m_accounts+"','"
                                                        +BoxName+"','"+m_MailType+"','"+strdate+"','"+mailId+"')";
                                                count=m_mailFuncInter163.Excutemdl(insql);
                                                mailTotalCounts++;
                                                m_sep++;
					}
				}
				mailIndex++;
                                if(!m_mailFuncInter163.GetThreadmark())
                                {
                                    if(idEndRecv())
                                    {
                                        return ;
                                    }
                                }
			}
                        if(strFolders.indexOf("arrow_next")>0)
                        {
                            String pageNo=putstr(strFolders,"最末頁","arrow_next",0);
                            String go_page=putstr(pageNo,"go_page(",")",0);
                            int page=Integer.parseInt(go_page);
                            page=page-1;
                            nextUrl="mailService/mail/M_main_1.do?mail_type=&msg=&start="+page+"&query_form=&query_string=&folder_id=170&sort_method=1&sort_subject=2&function_name=show&next_page=menu_page";
                            GetMailId(nextUrl, boxname);
                             if(endrecv)
                             {
                                   return;
                             }
                        }
		} catch (HttpException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				m_mailFuncInter163.ShowMessage(m_accounts+" :hinet Request Method failed: "
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
			m_mailFuncInter163.ShowMessage(m_accounts+" :Please check your provided http address!");
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
						m_mailFuncInter163.ShowMessage(m_accounts+" :126 Request Method failed: "
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
						m_mailFuncInter163.ShowMessage(m_accounts+" :hotmail Request Method failed: "
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
				m_mailFuncInter163.ShowMessage(m_accounts+" :Location field value is null.");
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
        /*
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
				m_mailFuncInter163.ShowMessage(m_accounts+ " :FastMail Request Method failed: "
						+ getMethod.getStatusLine());
			}

			byte[] byteContent=getMethod.getResponseBody();
			Date now = new Date(System.currentTimeMillis());
			DateFormat date = DateFormat.getDateInstance();
			String strdate = date.format(now);
			String path="/"+strdate+"/"+m_accounts+"@"+m_postfix+"/"+boxName+"/";
			saveMailText(m_sep,mailSubject,path,byteContent);
                        m_mailFuncInter163.ShowMessage(m_accounts+" :"+mailSubject+"下载！");

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
			m_mailFuncInter163.ShowMessage(m_accounts +" :Please check your provided http address!");
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
        /*
	public  void saveMailText(int i,String mailSubject,String path, byte[] content)
	{
	      try
	      {
	    	  String Mailpath=m_mailFuncInter163.GetMailPath();
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
        */
    /**
     * 线程停止是判断是否为用户指定的序号
     * @return
     */
    private boolean idEndRecv() {
        try
        {
            ResultSet result;
            if(m_mailFuncInter163.GetRecvType())
            {
                if(!m_mailFuncInter163.GetuserAllThreadMark())
                {
                    m_mailFuncInter163.ShowMessage("正在停止线程...请耐心等待，不要进行其他操作！");
                    endrecv =true;
                    try {
                        Thread.sleep(3000);
                        m_mailFuncInter163.ShowMessage("线程停止了！");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(HotmailMailRecev.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return endrecv;
                }
                else
                {
                    String Threadsql="select RevState from emailaccounts where ID='"+m_ID+"'";
                    result=m_mailFuncInter163.ExcuteResult(Threadsql);

                    if (result.next())
                    {
                        int restate = result.getInt("RevState");
                        if(restate==0)
                        {
                            //m_mailFuncInterhot.SetThreadmark(true);
                            endrecv=true;
                            Thread.sleep(3000);
                            m_mailFuncInter163.ShowMessage("线程停止了！");


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
                 if(!m_mailFuncInter163.GetcookieAllThreadMark())
                {
                     m_mailFuncInter163.ShowMessage("正在停止线程...请耐心等待，不要进行其他操作！");
                     Thread.sleep(3000);
                     m_mailFuncInter163.ShowMessage("线程停止了！");
                     endrecv =true;

                    return endrecv;
                }
                else
                {
                    String Threadsql="select RevState from emailcookie where ID='"+m_ID+"'";
                    result=m_mailFuncInter163.ExcuteResult(Threadsql);

                    if (result.next())
                    {
                        int restate = result.getInt("RevState");
                        if(restate==0)
                        {
                           // m_mailFuncInterhot.SetThreadmark(true);

                            endrecv=true;
                            Thread.sleep(3000);
                            m_mailFuncInter163.ShowMessage("线程停止了！");
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



    /**
	 * 按字符下载邮件
	 * @param url
	 * @param boxName
	 * @param j
	 * @throws HttpException
	 * @throws IOException
	 */
	private void requestEmail(String url,String boxName,int m_sep, String mailSubject) throws HttpException, IOException
	{
		// 构造HttpClient的实例
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpClient httpClient = new HttpClient(connectionManager);
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		// 创建GET方法的实例
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
			// 执行getMethod
			String requestStr = new String();
			StringBuffer resultBuffer = new StringBuffer();
			int statusCode = httpClient.executeMethod(getMethod);

			if (statusCode != HttpStatus.SC_OK)
			{
				m_mailFuncInter163.ShowMessage(m_accounts+" :Hotmail Request Method failed: "
						+ getMethod.getStatusLine());
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(
					getMethod.getResponseBodyAsStream(), getMethod
							.getResponseCharSet()));
			String inputLine = null;
			while ((inputLine = in.readLine()) != null)
			{
				resultBuffer.append(inputLine);
				resultBuffer.append("\n");
			}
                        in.close();
			requestStr = resultBuffer.toString();

			Date now = new Date(System.currentTimeMillis());
			DateFormat date = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）

			String strdate = date.format(now);
			String path="/"+strdate+"/"+m_accounts+"@"+m_postfix+"/"+boxName+"/";
                        m_mailFuncInter163.ShowMessage(m_accounts+" :"+mailSubject+"下载！");
			saveMailText(m_sep,mailSubject,path,requestStr);


		}
		catch (HttpException e)
		{
			// 发生致命的异常，可能是协议不对或者返回的内容有问题
			m_mailFuncInter163.ShowMessage(m_accounts+" :Please check your provided http address!");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// 发生网络异常
			e.printStackTrace();
		}
		finally
		{
			// 释放连接
			getMethod.releaseConnection();
		}
		//return requestStr;
	}
	/**
	 * 保存邮件
	 * @param i 邮件名称编号
	 * @param path 保存路径
	 * @param content 邮件内容
	 */
	public  void saveMailText(int m_sep,String mailSubject,String path, String content)
	{
	      try
	      {
                  String Mailpath=m_mailFuncInter163.GetMailPath();
	    	  File directory=new File(Mailpath);
	    	  File file = new File(directory.getCanonicalPath()+path);
	    	  if (file.exists())
	    	  {
	    		 // System.out.println("文件夹存在");
	    	  }
	    	  else
	    	  {
	    		 // System.out.println("文件夹不存在，正在创建...");
	    		  if (file.mkdirs())
	    		  {
	    			  	//System.out.println("文件夹创建成功！");
	    		  }
	    		  else
	    		  {
	    			 // System.out.println("文件夹创建失败！");
	    		  }
	    	  }
	    	  String fileName=Integer.toString(m_sep)+"_"+mailSubject+".eml";
	    	  File newfile=new File(directory.getCanonicalPath()+path+fileName);
	    	  if (!newfile.exists())
	    	  {
	    		  newfile.createNewFile();
	    	  }
	    	  BufferedWriter output = new BufferedWriter(new FileWriter(newfile));
		      output.write(content);
		      output.flush();
		      output.close();
	      }
	      catch (Exception e)
	      {
	    	  //e.printStackTrace();
	      }




    }

}
