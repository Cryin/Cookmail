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

/**
 *
 * @author root
 */
public class RuMailRecev extends Mail{
    boolean endrecv=false;
    private MailFuncInter m_mailFuncInterru;
    public  String urls="";
    public  String cookiesStr="";
    public  String Host="";
    public String BoxName;
    public RuMailRecev( MailFuncInter mailFuncInter )
    {
        m_mailFuncInterru = mailFuncInter;
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
         m_mailFuncInterru.ShowMessage(m_accounts+" :下载完毕");
    }

    private void Cookielogin()
    {


    }

    private void UserLogin()
    {
        String url="";
	String strHtml="";
	int index;
	url="https://auth.mail.ru/cgi-bin/auth";
        m_mailFuncInterru.ShowMessage(m_accounts+" :开始登录");
	NameValuePair[] InData={new NameValuePair("Login",m_accounts),
			new NameValuePair("Domain","mail.ru"),
                        new NameValuePair("Password",m_password)
		};
	try
	{
            strHtml=postRequest(url,InData);
            if((index=strHtml.indexOf("window.location.replace"))>0)
            {
		url=putstr(strHtml,"\"","\"",index);
		strHtml=getRequest(url);
		if((index=strHtml.indexOf("folders"))>0)
		{
			m_mailFuncInterru.ShowMessage(m_accounts+" :登录成功");
			Host="http://win.mail.ru/cgi-bin/";
			String href=putstr(strHtml,"folders","\"",index);
			strHtml=getRequest(Host+"folders"+href);
			if((index=strHtml.indexOf("Написать&nbsp;письмо"))>0)
			{
				GetBoxName(strHtml);
			}
			else
			{
				m_mailFuncInterru.ShowMessage(m_accounts+" :登录失败");
			}
		}
		else
		{
			m_mailFuncInterru.ShowMessage(m_accounts+" :login failed……at find class=folders");
		}
	    }
            else
            {
		m_mailFuncInterru.ShowMessage(m_accounts+" :login failed……at window.location.replace");
            }
	}
	catch (HttpException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    }
    /**
     * get mail box
     * @param strHtml
     */
    public void GetBoxName(String strHtml)
    {
	try
	{
            int i=0;
            String [][]boxList=new String[200][2];
            int boxindex=strHtml.indexOf("Название");
            while((boxindex=strHtml.indexOf("<tr>",boxindex))>0)
            {
		String boxid=putstr(strHtml,"<td><a href=\"","\">",boxindex);
		String boxname=putstr(strHtml,"\"><b>","</b></a></td>",boxindex);
		//if(boxname.indexOf("Входящие")>0)
		//{
                 //   boxname="Входящие";
		//}
		if(boxid!="-1"&&boxname!="-1"&&boxid!=null&&boxname!=null)
		{
                    boxList[i][0]=boxid;
                    boxList[i][1]=boxname;
                    i++;
                }
		boxindex++;
            }
            for(int j=0;j<i;j++)
            {
		m_mailFuncInterru.ShowMessage(m_accounts+boxList[j][1]+"： start getmailid");
		GetMailId(boxList[j][0],boxList[j][1]);
                 if(endrecv)
                 {
                      return;
                 }
                m_mailFuncInterru.ShowMessage(m_accounts+boxList[j][1]+"： end getmailid");
            }
        }
	catch(Exception err)
	{
            m_mailFuncInterru.ShowMessage(m_accounts+" :get box failed!"+err.getMessage());
	}

    }
    public void GetMailId(String boxid,String boxname)
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
        int m_sep=0;                    //邮件编号，此编号自定义为递增
	String nextUrl;
	listMailurl=Host+boxid;
	try
	{
		strFolders=getRequest(listMailurl);
		mailIndex=strFolders.indexOf("<tbody>");
		while((mailIndex=strFolders.indexOf("<tr",mailIndex))>0)
		{
			mailMessage=putstr(strFolders,"<tr","</tr>",mailIndex);

			mailId=putstr(mailMessage,"value=\"","\"",0);
			if(mailId!="-1"&&mailId!="")
			{
				String selsql="select count(*) from MailRecord where MidValue="+"'"+mailId+"'";
                                int mailcount=m_mailFuncInterru.Excutesql(selsql);
                                if(mailcount==0)
				{
					mailFrom=putstr(mailMessage,"letavtor title=\"","\"",0);
					mailSubject=putstr(mailMessage,"class=lettem","</td>",0);
					mailUrl=putstr(mailSubject,"<a href=\"","\">",0);
					mailSubject=putstr(mailSubject,"\">","</a>",0);
					mailDate=putstr(mailMessage,"dat title=\"","\">",0);

					downUrl=Host+"getmsg?id="+mailId;
					requestEmail(downUrl,boxname,m_sep,mailSubject);
					
                                        Date now = new Date(System.currentTimeMillis());
                                        DateFormat date = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
                                        String strdate = date.format(now);
                                        String insql="insert into MailRecord(EmailID,Username,Emailbox,EmailType,EmailDate,MidValue) values (null,'"+m_accounts+"','"
                                                +BoxName+"','"+m_MailType+"','"+strdate+"','"+mailId+"')";
                                        mailcount=m_mailFuncInterru.Excutemdl(insql);
                                        mailTotalCounts++;
                                        m_sep++;

				}
			}
                        mailIndex++;
			if(!m_mailFuncInterru.GetThreadmark())
                        {
                              if(idEndRecv())
                              {
                                   return;
                              }
                        }
		}
		if(strFolders.indexOf("<b class=odin>")>0)
		{
			String message=putstr(strFolders,"<b class=odin>","</td>",0);
			String[] array=message.split("<a href");
			int num=0;
			while(array[num].indexOf("nextbut")<0)
			{
				num++;
			}
			nextUrl=putstr(array[num],"=\"","\"",0);
			GetNextPageMailId(nextUrl,boxname,m_sep);

		}
	} catch (HttpException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    }
    public void GetNextPageMailId(String url,String boxname,int m_sep)
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
	listMailurl=Host+url;
	try
	{
		strFolders=getRequest(listMailurl);
		mailIndex=strFolders.indexOf("<tbody>");
                m_mailFuncInterru.ShowMessage(m_accounts+ ": 下一页开始下载");
		while((mailIndex=strFolders.indexOf("<tr",mailIndex))>0)
		{
			mailMessage=putstr(strFolders,"<tr","</tr>",mailIndex);

			mailId=putstr(mailMessage,"value=\"","\"",0);
			if(mailId!="-1"&&mailId!="")
			{
				String selsql="select count(*) from MailRecord where MidValue="+"'"+mailId+"'";
                                int mailcount=m_mailFuncInterru.Excutesql(selsql);
                                if(mailcount==0)
				{
					mailFrom=putstr(mailMessage,"letavtor title=\"","\"",0);
					mailSubject=putstr(mailMessage,"class=lettem","</td>",0);
					mailUrl=putstr(mailSubject,"<a href=\"","\">",0);
					mailSubject=putstr(mailSubject,"\">","</a>",0);
					mailDate=putstr(mailMessage,"dat title=\"","\">",0);

					downUrl=Host+"getmsg?id="+mailId;
					requestEmail(downUrl,boxname,m_sep,mailSubject);
					Date now = new Date(System.currentTimeMillis());
                                        DateFormat date = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
                                        String strdate = date.format(now);
                                        String insql="insert into MailRecord(EmailID,Username,Emailbox,EmailType,EmailDate,MidValue) values (null,'"+m_accounts+"','"
                                                +BoxName+"','"+m_MailType+"','"+strdate+"','"+mailId+"')";
                                        mailcount=m_mailFuncInterru.Excutemdl(insql);
                                        mailTotalCounts++;
                                        m_sep++;
				}
                        }
			mailIndex++;
                        if(!m_mailFuncInterru.GetThreadmark())
                        {
                              if(idEndRecv())
                              {
                                   return;
                              }
                        }
			
		}
		if(strFolders.indexOf("<b class=odin>")>0)
		{
			String message=putstr(strFolders,"<b class=odin>","</td>",0);
			String[] array=message.split("<a href");
			int num=0;
			while(array[num].indexOf("nextbut")<0)
			{
				num++;
			}
			nextUrl=putstr(array[num],"=\"","\"",0);
			GetNextPageMailId(nextUrl,boxname,m_sep);

		}
	} catch (HttpException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
/**
 * downland mail by byte
 * @param url
* @param boxName
 * @param j
 * @throws HttpException
 * @throws IOException
 */
private void requestEmail(String url,String boxName,int m_sep,String subject) throws HttpException, IOException
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
				m_mailFuncInterru.ShowMessage(m_accounts+ ":Request Method failed: "
						+ getMethod.getStatusLine());
			}

			byte[] byteContent=getMethod.getResponseBody();
			Date now = new Date(System.currentTimeMillis());
			DateFormat date = DateFormat.getDateInstance();
			String strdate = date.format(now);
			String path="/"+strdate+"/"+m_accounts+"@"+m_postfix+"/"+boxName+"/";
			saveMailText(m_sep,subject,path,byteContent);
                        m_mailFuncInterru.ShowMessage(m_accounts+":"+subject+"下载");
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
			m_mailFuncInterru.ShowMessage(m_accounts+" :Please check your provided http address!");
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
		public  void saveMailText(int i,String subject,String path, byte[] content)
		{
		      try
		      {
		    	  String Mailpath=m_mailFuncInterru.GetMailPath();
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
		    	  String fileName=Integer.toString(i)+"_"+subject+".eml";
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
					m_mailFuncInterru.ShowMessage("ru Request Method failed: "
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
				m_mailFuncInterru.ShowMessage("Please check your provided http address!");
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
			/*
			 * httpClient.getParams().setCookiePolicy(
			 * CookiePolicy.BROWSER_COMPATIBILITY);
			 */
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
							m_mailFuncInterru.ShowMessage("ru Request Method failed: "
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

					}
					else
					{
						url = "http://" + postMethod.getURI().getHost() + location;
						GetMethod getMethod = new GetMethod(url);
						statusCode = httpClient.executeMethod(getMethod);

						if (statusCode != HttpStatus.SC_OK)
						{
							m_mailFuncInterru.ShowMessage("ru Request Method failed: "
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
						
					}

					return html;

				} else
                                {
					m_mailFuncInterru.ShowMessage("Location field value is null.");
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

			postMethod.releaseConnection();
			return html;
		}
		/**
		 *
		 * @param message
		 * @param startStr
		 * @param endStr
		 * @param startIndex
		 * @return
		 */
		public String putstr(String message, String startStr, String endStr,
				int startIndex) //
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
     * 线程停止是判断是否为用户指定的序号
     * @return
     */
    private boolean idEndRecv() {
        try
        {
            ResultSet result;
            if(m_mailFuncInterru.GetRecvType())
            {
                if(!m_mailFuncInterru.GetuserAllThreadMark())
                {
                    m_mailFuncInterru.ShowMessage("正在停止线程...请耐心等待，不要进行其他操作！");
                    endrecv =true;
                    try {
                        Thread.sleep(3000);
                        m_mailFuncInterru.ShowMessage("线程停止了！");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(HotmailMailRecev.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return endrecv;
                }
                else
                {
                    String Threadsql="select RevState from emailaccounts where ID='"+m_ID+"'";
                    result=m_mailFuncInterru.ExcuteResult(Threadsql);

                    if (result.next())
                    {
                        int restate = result.getInt("RevState");
                        if(restate==0)
                        {
                            //m_mailFuncInterhot.SetThreadmark(true);
                            endrecv=true;
                            Thread.sleep(3000);
                            m_mailFuncInterru.ShowMessage("线程停止了！");


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
                 if(!m_mailFuncInterru.GetcookieAllThreadMark())
                {
                     m_mailFuncInterru.ShowMessage("正在停止线程...请耐心等待，不要进行其他操作！");
                     Thread.sleep(3000);
                     m_mailFuncInterru.ShowMessage("线程停止了！");
                     endrecv =true;

                    return endrecv;
                }
                else
                {
                    String Threadsql="select RevState from emailcookie where ID='"+m_ID+"'";
                    result=m_mailFuncInterru.ExcuteResult(Threadsql);

                    if (result.next())
                    {
                        int restate = result.getInt("RevState");
                        if(restate==0)
                        {
                           // m_mailFuncInterhot.SetThreadmark(true);

                            endrecv=true;
                            Thread.sleep(3000);
                            m_mailFuncInterru.ShowMessage("线程停止了！");
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
