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
public class FastmailMailRecev extends Mail{

    public  String urls="";
    public  String cookiesStr="";
    public  String Host="";
    public static String UDm;
    public static String Ust;
    public String BoxName;
    boolean endrecv=false;
    private MailFuncInter m_mailFuncInterfast;

    public FastmailMailRecev( MailFuncInter mailFuncInter )
    {
        m_mailFuncInterfast = mailFuncInter;
    }

    @Override
    void login()
    {
        if(m_cookie!=null)                      //以cookie是否为空进行判断是cookie收邮还是密码收邮
        {
            Cookielogin();
        }
        else
        {
            UserLogin();
        }

    }

    private void Cookielogin() {
        String url=m_url;
	String strHtml="";
        cookiesStr=m_cookie;
	int index;
	if(url.length()==0)
        {
            m_mailFuncInterfast.ShowMessage(m_accounts+" : url不能为空！");
            return;
        }
	try {
                m_mailFuncInterfast.ShowMessage(m_accounts+" : 开始登录");
		strHtml=getRequest(url);

		if(strHtml.indexOf(m_accounts+"@fastmail.fm")>0)
		{
                    m_mailFuncInterfast.ShowMessage(m_accounts+" : 登录成功!");
                    UDm=putstr(url,"UDm=","&",0);
		    Ust=putstr(url,"Ust=","&",0);
                    GetBoxName();
                    m_mailFuncInterfast.ShowMessage(m_accounts+" :下载完毕！");
		}
		else
		{
			m_mailFuncInterfast.ShowMessage(m_accounts+" : load failed!");
		}

	} catch (HttpException e)
	{
		e.printStackTrace();
	} catch (IOException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    }

    private void UserLogin() {

        String url="";
	String strHtml="";
	int index;
	url="https://www.fastmail.fm/";
	try {
                m_mailFuncInterfast.ShowMessage(m_accounts+" : 开始登录");
		strHtml=getRequest(url);
		if((index=strHtml.indexOf("id=\"memail\""))>0)
		{
			url=putstr(strHtml,"action=\"", "\"", index);
			if(url!="-1"&&url!="")
			{
				NameValuePair[] InData={new NameValuePair("MLS","LN-*"),
					new NameValuePair("FLN-LoginMode","0"),
					new NameValuePair("FLN-UserName",m_accounts),
					new NameValuePair("FLN-Password",m_password),
					new NameValuePair("MSignal_LN-AU*","Login"),
					new NameValuePair("MSignal_LN-AU*","Secure+Login"),
					new NameValuePair("FLN-Security","0"),
					new NameValuePair("FLN-CssMode","0"),
					new NameValuePair("FLN-SessionTime","7200")
				};
                                strHtml=postRequest(url,InData);

				if((index=strHtml.indexOf("Click"))>0)
				{
					url=putstr(strHtml,"href=\"","\"",index);

					strHtml=getRequest(url);
					if(strHtml.indexOf(m_accounts+"@fastmail.fm")>0)
					{
                                                m_mailFuncInterfast.ShowMessage(m_accounts+" : 登录成功!");
						UDm=putstr(url,"UDm=","&",0);
						Ust=putstr(url,"Ust=","&",0);
						GetBoxName();
                                                m_mailFuncInterfast.ShowMessage(m_accounts+" :下载完毕！");
					}
					else
					{
						m_mailFuncInterfast.ShowMessage(m_accounts+" : load failed!");
					}
				}

			}
		}
	} catch (HttpException e)
	{
		e.printStackTrace();
	} catch (IOException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
    public void GetBoxName()
	{
		try
		{
			int i=0;
			String folderUrl;
			String strFolder;
			String [][]boxList=new String[200][3];
			folderUrl="https://www.fastmail.fm/mail/?MLS=MB-*;MSS=;UDm=" + UDm + ";Ust=" + Ust + ";MSignal=FL-*U-1";
			strFolder=getRequest(folderUrl);
			strFolder=putstr(strFolder,"FFL-FPS-CreateSubFolderId","</select>",0);
			if(strFolder!="-1"&&strFolder!="");
			{
				int boxIndex=0;
				while((boxIndex=strFolder.indexOf("<option", boxIndex))>0)
				{
					String boxid=putstr(strFolder,"value=\"","\"",boxIndex);
					String boxname=putstr(strFolder,"\">","</option>",boxIndex);
					if(!boxid.trim().equals("-1")&&!boxname.equals("-1"))
					{
						boxList[i][0]=boxid;
						boxList[i][1]=boxname;
						i++;
					}
					boxIndex++;
				}
				for(int j=0;j<i;j++)
				{
					m_mailFuncInterfast.ShowMessage(m_accounts+": "+boxList[j][1]+"开始下载");
					GetMailId(boxList[j][0],boxList[j][1]);

                                        if(endrecv)
                                        {
                                             return;
                                        }
                                        m_mailFuncInterfast.ShowMessage(m_accounts+": "+boxList[j][1]+"下载完毕");
                                }
			}
		}
		catch(Exception err)
		{
                    m_mailFuncInterfast.ShowMessage(m_accounts+" : get boxname failed!");
		}

	}
	/**
	 * get mail id and download
	 * @param boxid
	 * @param boxname
	 */
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
                int m_sep=0;                    //邮件编号，此编号自定义为递增
		String downUrl="";					//downland url
		String strMail;					//the box conent
		String nextUrl;
                BoxName=boxname;
		try
		{
			mailUrl="https://www.fastmail.fm/mail/?MLS=MB-*;MSS=;UDm=" + UDm + ";Ust=" + Ust + ";MSignal=MB-GF**" + boxid;
			strMail=getRequest(mailUrl);
			if((mailIndex=strMail.indexOf("<tbody >"))!=-1)
			{
				while((mailIndex=strMail.indexOf("<tr id=", mailIndex))!=-1)
				{
					mailId=putstr(strMail,"id=\"","\"",mailIndex);

					if(mailId!="-1"&&mailId!="")
					{
						String selsql="select count(*) from MailRecord where MidValue="+"'"+mailId+"'";
                                                int mailcount=m_mailFuncInterfast.Excutesql(selsql);
                                                if(mailcount==0)
						{
							int fromIndex=strMail.indexOf("<td class=\"from\">",mailIndex);
							mailFrom=putstr(strMail,"&lt;","&gt;",fromIndex);
							fromIndex=strMail.indexOf("<td class=\"subject\">",mailIndex);
							mailSubject=putstr(strMail,"Click to read message","\">",mailIndex).trim();
							fromIndex=strMail.indexOf("<td class=\"date\">",mailIndex);
							mailDate=putstr(strMail,"title=\"","(",fromIndex);
							downUrl="https://www.fastmail.fm/mail/.txt?MLS=MR-**"+mailId+"*;MSS=;SMB-CF=14549827;SMR-PT=;SMR-UM=f14549827u148;UDm="+UDm+";Ust="+Ust+";MSignal=MR-RV*";
							requestEmail(downUrl,boxname,m_sep,mailSubject);
                                                        
                                                        Date now = new Date(System.currentTimeMillis());
                                                        DateFormat date = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
                                                        String strdate = date.format(now);
                                                        String insql="insert into MailRecord(EmailID,Username,Emailbox,EmailType,EmailDate,MidValue) values (null,'"+m_accounts+"','"
                                                        +BoxName+"','"+m_MailType+"','"+strdate+"','"+mailId+"')";
                                                        mailcount=m_mailFuncInterfast.Excutemdl(insql);
                                                        mailTotalCounts++;
							m_sep++;

						}
					}
					mailIndex++;
                                        if(!m_mailFuncInterfast.GetThreadmark())
                                        {
                                            if(idEndRecv())
                                            {
                                                return;
                                            }
                                        }
					
				}
				while(strMail.indexOf("View next page")!=-1)
				{
					int nextindex=0;
					if((nextindex=strMail.indexOf("Show</button>"))!=-1)
					{
						nextUrl=putstr(strMail,"<a href=\"","\"",nextindex);
						strMail=GetNextPageMailId(nextUrl,boxname,m_sep);
                                                if(strMail=="-1")
                                                {
                                                    return;
                                                }
						m_sep=m_sep+20;
					}
				}

			}
		}
		catch (HttpException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * down next page mail
	 * @param nextUrl
	 */
	public String GetNextPageMailId(String nextUrl,String boxname,int m_sep)
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
		String strMail = "";					//the box conent

		try
		{
			String nextPageUrl="https://www.fastmail.fm"+nextUrl;
			strMail=getRequest(nextPageUrl);
			if((mailIndex=strMail.indexOf("<tbody >"))!=-1)
			{
				while((mailIndex=strMail.indexOf("<tr id=", mailIndex))!=-1)
				{
					mailId=putstr(strMail,"id=\"","\"",mailIndex);
					if(mailId!="-1"&&mailId!="")
					{
						String selsql="select count(*) from MailRecord where MidValue="+"'"+mailId+"'";
                                                int mailcount=m_mailFuncInterfast.Excutesql(selsql);
                                                if(mailcount==0)
						{
							int fromIndex=strMail.indexOf("<td class=\"from\">",mailIndex);
							mailFrom=putstr(strMail,"&lt;","&gt;",fromIndex);
							fromIndex=strMail.indexOf("<td class=\"subject\">",mailIndex);
							mailSubject=putstr(strMail,"Click to read message","\">",mailIndex).trim();
							fromIndex=strMail.indexOf("<td class=\"date\">",mailIndex);
							mailDate=putstr(strMail,"title=\"","(",fromIndex);
							downUrl="https://www.fastmail.fm/mail/.txt?MLS=MR-**"+mailId+"*;MSS=;SMB-CF=14549827;SMR-PT=;SMR-UM=f14549827u148;UDm="+UDm+";Ust="+Ust+";MSignal=MR-RV*";
							requestEmail(downUrl,boxname,m_sep,mailSubject);

                                                        Date now = new Date(System.currentTimeMillis());
                                                        DateFormat date = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
                                                        String strdate = date.format(now);
                                                        String insql="insert into MailRecord(EmailID,Username,Emailbox,EmailType,EmailDate,MidValue) values (null,'"+m_accounts+"','"
                                                        +BoxName+"','"+m_MailType+"','"+strdate+"','"+mailId+"')";
                                                        mailcount=m_mailFuncInterfast.Excutemdl(insql);
                                                        mailTotalCounts++;
							m_sep++;

						}
					}
					mailIndex++;
                                        if(!m_mailFuncInterfast.GetThreadmark())
                                        {
                                        if(idEndRecv())
                                        {
                                            return "-1";
                                        }
                                        }
				}
			}

		}
		catch (HttpException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strMail;
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
				m_mailFuncInterfast.ShowMessage(m_accounts+ " :FastMail Request Method failed: "
						+ getMethod.getStatusLine());
			}

			byte[] byteContent=getMethod.getResponseBody();
			Date now = new Date(System.currentTimeMillis());
			DateFormat date = DateFormat.getDateInstance();
			String strdate = date.format(now);
			String path="/"+strdate+"/"+m_accounts+"@"+m_postfix+"/"+boxName+"/";
			saveMailText(m_sep,mailSubject,path,byteContent);
                        m_mailFuncInterfast.ShowMessage(m_accounts+" :"+mailSubject+"下载！");

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
			m_mailFuncInterfast.ShowMessage(m_accounts +" :Please check your provided http address!");
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
	    	  String Mailpath=m_mailFuncInterfast.GetMailPath();
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
				m_mailFuncInterfast.ShowMessage(m_accounts+" :fastmail Request Method failed: "
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
			m_mailFuncInterfast.ShowMessage(m_accounts+" :Please check your provided http address!");
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
						m_mailFuncInterfast.ShowMessage(m_accounts+" :fastmail Request Method failed: "
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
						m_mailFuncInterfast.ShowMessage(m_accounts+" :fastmail Request Method failed: "
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
				m_mailFuncInterfast.ShowMessage(m_accounts+" :Location field value is null.");
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
            if(m_mailFuncInterfast.GetRecvType())
            {
                if(!m_mailFuncInterfast.GetuserAllThreadMark())
                {
                    m_mailFuncInterfast.ShowMessage("正在停止线程...请耐心等待，不要进行其他操作！");
                    endrecv =true;
                    try {
                        Thread.sleep(3000);
                        m_mailFuncInterfast.ShowMessage("线程停止了！");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(HotmailMailRecev.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return endrecv;
                }
                else
                {
                    String Threadsql="select RevState from emailaccounts where ID='"+m_ID+"'";
                    result=m_mailFuncInterfast.ExcuteResult(Threadsql);

                    if (result.next())
                    {
                        int restate = result.getInt("RevState");
                        if(restate==0)
                        {
                            //m_mailFuncInterhot.SetThreadmark(true);
                            endrecv=true;
                            Thread.sleep(3000);
                            m_mailFuncInterfast.ShowMessage("线程停止了！");


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
                 if(!m_mailFuncInterfast.GetcookieAllThreadMark())
                {
                     m_mailFuncInterfast.ShowMessage("正在停止线程...请耐心等待，不要进行其他操作！");
                     Thread.sleep(3000);
                     m_mailFuncInterfast.ShowMessage("线程停止了！");
                     endrecv =true;

                    return endrecv;
                }
                else
                {
                    String Threadsql="select RevState from emailcookie where ID='"+m_ID+"'";
                    result=m_mailFuncInterfast.ExcuteResult(Threadsql);

                    if (result.next())
                    {
                        int restate = result.getInt("RevState");
                        if(restate==0)
                        {
                           // m_mailFuncInterhot.SetThreadmark(true);

                            endrecv=true;
                            Thread.sleep(3000);
                            m_mailFuncInterfast.ShowMessage("线程停止了！");
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
