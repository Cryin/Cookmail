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
import org.apache.commons.lang.StringEscapeUtils;
/**
 *
 * @author root
 */
public class HotmailMailRecev extends Mail {
    

    String host="";
    String urls="";
    String cookiesStr="";
    String BoxName="";
    String MailCountTotal="";			//邮件总数mCt
    String sBy="";
    String sAsc="";
    boolean endrecv=false;
    private MailFuncInter m_mailFuncInterhot;
    
    public HotmailMailRecev( MailFuncInter mailFuncInter )
    {
        m_mailFuncInterhot = mailFuncInter;
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
    /**
     * 密码收邮登录
     */
    public void UserLogin()
    {
	int i=0;
	String url="";
	String strHtml="";
	int index;
	url="http://mail.live.com/";
        m_mailFuncInterhot.ShowMessage(m_accounts+" :开始登录...");
	try 
        {
            strHtml=getRequest(url);
            url=getUrl(strHtml);                                                //获取跳转url
            NameValuePair[] InData=getInData(strHtml);                          //获取跳转请求的表单
	    strHtml=postRequest(url,InData);                                //post请求
	
            if((index=strHtml.indexOf("window.location.replace"))>-1)
            {
		url=putstr(strHtml,"\"","\"",index);
                strHtml=getRequest(url);
            }
            else
            {
                m_mailFuncInterhot.ShowMessage(m_accounts+" :登录失败！");
                 return;
            }
		
            url=getLoginMode(strHtml);
            host=putstr(url,"http:","/mail/",0);
            host="http:" + host + "/mail/";
		
            strHtml=getRequest(url);
            url=urls;
            strHtml=getRequest(url);
            if((index=strHtml.indexOf("href=\"ManageFoldersLight"))>0)
            {
		m_mailFuncInterhot.ShowMessage(m_accounts+" :登录成功！");
		String manageFolder=putstr(strHtml,"href=\"","\"",index);
		url=host+manageFolder;
		strHtml=getRequest(url);
		getBoxName(strHtml);
                m_mailFuncInterhot.ShowMessage(m_accounts+" :下载完毕！");
			
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
         * cookie 收邮登录
         */
        public void Cookielogin()
        {
            int i=0;
            String url="";
            String strHtml="";
            int index;
            cookiesStr=m_cookie;
            url="http://mail.live.com/";
            m_mailFuncInterhot.ShowMessage(m_accounts+" :开始登录...");
            try
            {
                strHtml=getRequest(url);
                if((index=strHtml.indexOf("<iframe id=\"UIFrame\""))>0)
                {
                    url=putstr(strHtml,"src=\"","\"",index);
                    url=StringEscapeUtils.unescapeHtml(url);
                    host=putstr(url,"http:","/mail/",0);
                    host="http:" + host + "/mail/";
                }
                strHtml=getRequest(url);
                System.out.println(strHtml);

                if((index=strHtml.indexOf("href=\"ManageFoldersLight"))>0)
		{
			m_mailFuncInterhot.ShowMessage(m_accounts+" :登录成功！");
			String manageFolder=putstr(strHtml,"href=\"","\"",index);
			url=host+manageFolder;
			strHtml=getRequest(url);
			getBoxName(strHtml);
                        m_mailFuncInterhot.ShowMessage(m_accounts+" :下载完毕！");
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
	 * get mail box name
	 * @param strHtml
	 */
	private void getBoxName(String strHtml)
	{
		int boxIndex=0;
		String boxurl="";
		String boxid="";
		
		String[] boxName ={ "收件箱", "垃圾邮件", "草稿", "已发送邮件", "已删除邮件" };
		if((boxIndex=strHtml.indexOf("<tbody>"))>0)
		{
			int i=0;
			while((boxIndex=strHtml.indexOf("?FolderID", boxIndex))>0)
			{
				boxurl=putstr(strHtml,"FolderID=","\"",boxIndex);
				boxid=boxurl.substring(1,boxurl.indexOf("&")-1);
				if(i>4)
				{
					BoxName="自定义文件夹"+(i-4);
				}
				else
				{
					BoxName=boxName[i];
				}
				String mailurl=host+"InboxLight.aspx?FolderID="+boxurl;
				//http://bl125w.blu125.mail.live.com/mail/InboxLight.aspx?FolderID=00000000-0000-0000-0000-000000000001&n=621998721
				
				//分别对各个箱子进行操作
                                m_mailFuncInterhot.ShowMessage(m_accounts+" :"+BoxName+"开始下载！");
				getFirstPage(strHtml,mailurl,boxid,BoxName);
                                if(endrecv)
                                {
                                    return;
                                }
                                m_mailFuncInterhot.ShowMessage(m_accounts+" :"+BoxName+"下载完毕！");
				i++;
				boxIndex++;
			}
		}
		else
		{
			m_mailFuncInterhot.ShowMessage(m_accounts+" :取箱子失败！");
			return;
		}
		
	}
	/**
	 * get FirstPage mail
	 * @param mailurl
	 * @param boxid
	 */
	private void getFirstPage(String boxMessage,String mailurl,String boxid,String boxName)
	{
		try
		{
			int mailIndex;
			int PageIndex=0;
			String mailId="";
			String mailSubject;
			String mailFrom;
			String mailUrl="";
			String PageMessage="";		//关于页数的信息
			String MailMessage="";		//第一页的邮件信息
			String nextPageUrl;
                        int m_sep=0;                    //邮件编号，此编号自定义为递增
			BoxName=boxName;
			NameValuePair[] nextPageIndata;
			
			String strMail=getRequest(mailurl);

			//System.out.println(strMail);
                        GetWholeIndata(strMail);
			MailMessage=strMail;
			//取第一页邮件信息并下载到本地文件夹
			if((mailIndex=MailMessage.indexOf("MessageListItems"))>0)
			{
                           // m_mailFuncInterhot.ShowMessage(m_accounts+" :邮件开始下载！");
			    MailMessage=putstr(MailMessage,"MessageListItems","</tbody>",mailIndex);
			    mailIndex=MailMessage.indexOf("<tr");
                                                                   //保存邮件的序号 递增
                            while((mailIndex=MailMessage.indexOf("<tr", mailIndex))>0)
			    {
				mailId=putstr(MailMessage,"id=\"","\"",mailIndex);
                                if(mailId!=""||mailId!="-1")
                                {
                                   
                                    String selsql="select count(*) from MailRecord where MidValue="+"'"+mailId+"'";
                                    int mailcount=m_mailFuncInterhot.Excutesql(selsql);
                                    if(mailcount==0)
                                    {
                                        mailFrom=putstr(MailMessage,"Frm\"><a>","</a>",mailIndex);
                                        mailSubject=putstr(MailMessage,"Sbj\"><a href=\"#\">","</a>",mailIndex);
                                        mailSubject=StringEscapeUtils.unescapeHtml(mailSubject);
                                        mailUrl=host+"GetMessageSource.aspx?msgid="+mailId;
                                        requestEmail(mailUrl,boxName,m_sep,mailSubject);//i为后面数据预留
                                        //获取当前时间并将本条邮件记录插入到数据库mailrecord中
                                        Date now = new Date(System.currentTimeMillis());
                                        DateFormat date = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
                                        String strdate = date.format(now);
                                        String insql="insert into MailRecord(EmailID,Username,Emailbox,EmailType,EmailDate,MidValue) values (null,'"+m_accounts+"','"
                                                +BoxName+"','"+m_MailType+"','"+strdate+"','"+mailId+"')";
                                        mailcount=m_mailFuncInterhot.Excutemdl(insql);
                                        mailTotalCounts++;
                                        m_sep++;
                                    }
                                    mailIndex++;
                                    if(!m_mailFuncInterhot.GetThreadmark())
                                    {
                                        if(idEndRecv())
                                        {
                                            return;
                                        }
                                    }
                                }
                            }
			}
			else
			{
				m_mailFuncInterhot.ShowMessage(m_accounts+" :取邮件失败！");
			}
                      
			if((PageIndex=strMail.indexOf("nextPageLink"))>0)
			{
				
				nextPageUrl=GetNextPageUrl(strMail);
                                nextPageIndata=GetNextPageIndata(strMail,PageIndex);
				if(nextPageIndata!=null)
				{
					GetNextPageMail(nextPageUrl,nextPageIndata,m_sep);
                                        if(idEndRecv())
                                        {
                                            return;
                                        }

				}
				else
				{
					m_mailFuncInterhot.ShowMessage(m_accounts+" :"+boxName+"邮件下载完毕！");
				}
                               
			}		
			
		} catch (HttpException e)
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
	 * 获取下一页邮件并下载到本地文件夹
	 * @param nextPageUrl
	 * @param nextPageIndata
	 */
	private void GetNextPageMail(String PageUrl,
			NameValuePair[] PageIndata,
                        int m_sep)
	{
		String strHtml="";
		int mailIndex;
		String mailId="";
		String mailSubject;
		String mailFrom;
		String mailUrl="";
		String MailMessage="";		//这一页的邮件信息
                NameValuePair[] nextPageIndata;
		try {
			strHtml=postRequest(PageUrl,PageIndata);
			//System.out.println(strHtml);
			MailMessage=strHtml;
			//取下一页邮件信息并下载到本地文件夹
			if((mailIndex=MailMessage.indexOf("MessageListItems"))>0)
			{
				m_mailFuncInterhot.ShowMessage(m_accounts+" :下一页邮件开始下载！");
			    MailMessage=putstr(MailMessage,"MessageListItems","</tbody>",mailIndex);
			    mailIndex=MailMessage.indexOf("<tr");
                            while((mailIndex=MailMessage.indexOf("<tr", mailIndex))>0)
                            {
				mailId=putstr(MailMessage,"id=\\\"","\\\"",mailIndex);
                                if(mailId!=""||mailId!="-1")
                                {
                                    String selsql="select count(*) from MailRecord where MidValue="+"'"+mailId+"'";
                                    int mailcount=m_mailFuncInterhot.Excutesql(selsql);
                                    if(mailcount==0)
                                    {
                                        mailFrom=putstr(MailMessage,"Frm\\\"><a>","</a>",mailIndex);
                                        mailSubject=putstr(MailMessage,"Sbj\\\"><a href=\\\"#\\\">","</a>",mailIndex);
                                        mailSubject=StringEscapeUtils.unescapeHtml(mailSubject);
                                        mailUrl=host+"GetMessageSource.aspx?msgid="+mailId;
                                        requestEmail(mailUrl,BoxName,m_sep,mailSubject);//i为后面数据预留
                                        
                                        Date now = new Date(System.currentTimeMillis());
                                        DateFormat date = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
                                        String strdate = date.format(now);
                                        String insql="insert into MailRecord(EmailID,Username,Emailbox,EmailType,EmailDate,MidValue) values (null,'"+m_accounts+"','"
                                                +BoxName+"','"+m_MailType+"','"+strdate+"','"+mailId+"')";
                                        mailcount=m_mailFuncInterhot.Excutemdl(insql);
                                        m_sep++;
                                        mailTotalCounts++;
                                    }
                                }
				mailIndex++;
                                if(!m_mailFuncInterhot.GetThreadmark())
                                {
                                     if(idEndRecv())
                                     {
                                         return ;
                                     }
                                }
                            }
                            int PageIndex=0;
                            if((PageIndex=strHtml.indexOf("nextPageLink"))>0)
                            {
				
				//nextPageUrl=GetNextPageUrl(strHtml);
                                nextPageIndata=GetMorePageIndata(strHtml,PageIndex);
				if(nextPageIndata!=null)
				{
					GetNextPageMail(PageUrl,nextPageIndata,m_sep);
                                        if(idEndRecv())
                                        {
                                            return;
                                        }
                                        
				}
				else
				{
					m_mailFuncInterhot.ShowMessage(m_accounts+" :"+BoxName+"邮件下载完毕！");
				}
                               
                            }


			}
			else
			{
				m_mailFuncInterhot.ShowMessage(m_accounts+" :取邮件失败！");
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
         * 
         * @param strMail
         */
        public void GetWholeIndata(String strHtml)
        {
            int ContainerIndex;
		if((ContainerIndex=strHtml.indexOf("messageListContainer"))>0)
		{
			sBy=putstr(strHtml,"sBy=\"","\"",ContainerIndex);
			sAsc=putstr(strHtml,"sAsc=\"","\"",ContainerIndex);
			MailCountTotal=putstr(strHtml,"mCt=\"","\"",ContainerIndex);
		}
		else
		{
			m_mailFuncInterhot.ShowMessage(m_accounts+" :获取messageListContainer失败！");
			return ;
		}

        }
	/**
	 * 获取请求下一页邮件的表单数据
	 * @param strHtml
	 * @return
	 */
	private NameValuePair[] GetNextPageIndata(String strHtml,int PageIndex)
	{
		
		String pnCur;						//表示页数
		String pnAm;
		String pnAd;
		String pnDir;
		String pnMid;
		String pnSkip;
		String d;
		String mt="";
                
		
                    pnCur=putstr(strHtml,"pnCur=\"","\"",PageIndex);
			pnAm=putstr(strHtml,"pnAm=\"","\"",PageIndex);
			pnAd=putstr(strHtml,"pnAd=\"","\"",PageIndex);
			pnAd = pnAd.replace("&#58;", "\\:");
			pnDir=putstr(strHtml,"pnDir=\"","\"",PageIndex);
			pnMid=putstr(strHtml,"pnMid=\"","\"",PageIndex);
			pnSkip=putstr(strHtml,"pnSkip=\"","\"",PageIndex);
			d="true,false,true,{\"00000000-0000-0000-0000-000000000001\","+pnDir+"," +sAsc+
					","+sBy+",false,\""+pnAm+"\"," +
					"\""+pnAd+"\","+pnCur+","+pnMid+",false," +
					"\"\","+MailCountTotal+",-1,Off},false,null";
			mt=putstr(cookiesStr,"mt=",";",0);
			//System.out.print(mt);
                        /*
                        if(pnCur == "-1")
                        {
                            pnCur=putstr(strHtml,"pnCur=\\\"","\\\"",PageIndex);
                            pnAm=putstr(strHtml,"pnAm=\\\"","\\\"",PageIndex);
                            pnAd=putstr(strHtml,"pnAd=\\\"","\\\"",PageIndex);
                            pnAd = pnAd.replace("&#58;", "\\:");
                            pnDir=putstr(strHtml,"pnDir=\\\"","\\\"",PageIndex);
                            pnMid=putstr(strHtml,"pnMid=\\\"","\\\"",PageIndex);
                            pnSkip=putstr(strHtml,"pnSkip=\\\"","\\\"",PageIndex);
                            d="true,false,true,{\"00000000-0000-0000-0000-000000000001\","+pnDir+"," +sAsc+
					","+sBy+",false,\""+pnAm+"\"," +
					"\""+pnAd+"\","+pnCur+","+pnMid+",false," +
					"\"\","+MailCountTotal+",-1,Off},false,null";
                            mt=putstr(cookiesStr,"mt=",";",0);

                        }
                          */
			NameValuePair[] Indata={new NameValuePair("cn","Microsoft.Msn.Hotmail.Ui.Fpp.MailBox"),
					new NameValuePair("mn","GetInboxData"),
					new NameValuePair("d",d),
					new NameValuePair("v","1"),
					new NameValuePair("mt",mt)
			};

			return Indata;
			
	}
	/**
	 * 取其中一个箱子的下一页邮件的url
	 * @param boxMessage
	 * @return
	 */
	private String GetNextPageUrl(String boxMessage) 
	{
		int messIndex;
		String SessionId="";				//请求下一页时url中a的值
		String PartnerID="0";				//请求下一页时url中ptid的值
		String nextPageUrl="";  			//返回下一页的url
		String AuthUser="";					//请求下一页时url中的au的值
		if((messIndex=boxMessage.indexOf("AuthUser:"))>0)
		{
			AuthUser=putstr(boxMessage,"\"","\"",messIndex);
		}
		if((messIndex=boxMessage.indexOf("SessionId:"))>0)
		{
			SessionId=putstr(boxMessage,"\"","\"",messIndex);
		}
		if((messIndex=boxMessage.indexOf("PartnerID:"))>0)
		{
			PartnerID=putstr(boxMessage,"\"","\"",messIndex);
			if(PartnerID=="")
			{
				PartnerID="0";
			}
			PartnerID="0";
		}
		if(SessionId!="-1"||PartnerID!="-1")
		{
			nextPageUrl=host+"mail.fpp?cnmn=Microsoft.Msn.Hotmail.Ui.Fpp.MailBox.GetInboxData&a="+
			SessionId+"&au="+AuthUser+"&ptid="+PartnerID;
			return nextPageUrl;
		}
		else
		{
			m_mailFuncInterhot.ShowMessage(m_accounts+" :取下一页url时出错！");
			return "-1";
		}
		
	}
	/**
	 * 获取不同登陆方式的url
	 * @param strHtml
	 * @return url
	 */
	@SuppressWarnings("deprecation")
	private String getLoginMode(String strHtml)
	{
		String loginUrl="";
		int loginIndex=strHtml.indexOf("self.location.href");
		if(loginIndex>0)
		{
			loginUrl=putstr(strHtml,"'","'",loginIndex);
			loginUrl=loginUrl.replace("\\x", "%");
			loginUrl=java.net.URLDecoder.decode(loginUrl);
			return loginUrl;
		}
		else
		{
			return "-1";
		}
		
	}
	/**
	 * getInData 获取post表单
	 * @param strHtml
	 * @return InData
	 */
	private NameValuePair[] getInData(String strHtml)
	{
		String PPFT="";
		String PPSX="";
		int inDataIndex=strHtml.indexOf("PPFT");
		if(inDataIndex>0)
		{
			PPFT=putstr(strHtml,"value=\"","\"",inDataIndex);
		}
		else
		{
                        m_mailFuncInterhot.ShowMessage(m_accounts+" :PPFT值未取到！");
		}
		inDataIndex=strHtml.indexOf("srf_sRBlob=");
		if(inDataIndex>0)
		{
			PPSX = putstr(strHtml, "srf_sRBlob='", "'", inDataIndex);
		}
		else
		{
			m_mailFuncInterhot.ShowMessage(m_accounts+" :PPSX值未取到！");
		}
		
		NameValuePair[] InData={new NameValuePair("idsbho","1"),
				new NameValuePair("PwdPad","IfYouAreReadingThisYouHaveTooMuc"),
				new NameValuePair("LoginOptions","3"),
				new NameValuePair("PPSX",PPSX),
				new NameValuePair("type","11"),
				new NameValuePair("login",m_accounts+"@"+m_postfix),
				new NameValuePair("passwd",m_password),
				new NameValuePair("NewUser","1"),
				new NameValuePair("PPFT",PPFT),
				new NameValuePair("i1","0"),
				new NameValuePair("i2","2"),
				new NameValuePair("i4","1")
		};
		return InData;
	}
	/**
	 * getUrl
	 * @param strHtml ”http://mail.live.com/“请求得到的网页
	 * @return url
	 */
	private String getUrl(String strHtml)
	{
		String url="";
		int urlIndex=strHtml.indexOf("srf_uPost");
		if(urlIndex>0)
		{
			url=putstr(strHtml,"srf_uPost='","'",urlIndex);
		}
		else
		{
			//System.out.println("url值未取到！");
		}
		return url;
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
				m_mailFuncInterhot.ShowMessage(m_accounts+" :hotmail Request Method failed: "
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
//			System.out
//					.println("---------------------------------->Get Cookies:");
//			for (Cookie cookie : cookiesTemp)
//			{
//
//				System.out.println(cookie.getName());
//				System.out.println(cookie.getValue());
//			}
//			System.out
//					.println("---------------------------------->Get Cookies:");
//			//
			// System.out.println(requestStr);
		}
		catch (HttpException e)
		{
			//
			m_mailFuncInterhot.ShowMessage(m_accounts+" :Please check your provided http address!");
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
						m_mailFuncInterhot.ShowMessage(m_accounts+" :hotmail Request Method failed: "
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
						m_mailFuncInterhot.ShowMessage(m_accounts+" :hotmail Request Method failed: "
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
				m_mailFuncInterhot.ShowMessage(m_accounts+" :Location field value is null.");
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
				m_mailFuncInterhot.ShowMessage(m_accounts+" :Hotmail Request Method failed: "
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
			requestStr=putstr(requestStr,"<pre>","</",0);
			requestStr=StringEscapeUtils.unescapeHtml(requestStr);
			Date now = new Date(System.currentTimeMillis()); 
			DateFormat date = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
			
			String strdate = date.format(now);
			String path="/"+strdate+"/"+m_accounts+"@"+m_postfix+"/"+boxName+"/";
                        m_mailFuncInterhot.ShowMessage(m_accounts+" :"+mailSubject+"下载！");
			saveMailText(m_sep,mailSubject,path,requestStr);
                        
                        
		} 
		catch (HttpException e) 
		{
			// 发生致命的异常，可能是协议不对或者返回的内容有问题
			m_mailFuncInterhot.ShowMessage(m_accounts+" :Please check your provided http address!");
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
                  String Mailpath=m_mailFuncInterhot.GetMailPath();
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
    /**
     * 线程停止是判断是否为用户指定的序号
     * @return
     */
    private boolean idEndRecv() {
        try
        {
            ResultSet result;
            if(m_mailFuncInterhot.GetRecvType())
            {
                if(!m_mailFuncInterhot.GetuserAllThreadMark())
                {
                    m_mailFuncInterhot.ShowMessage("正在停止线程...请耐心等待，不要进行其他操作！");
                    endrecv =true;
                    try {
                        Thread.sleep(3000);
                        m_mailFuncInterhot.ShowMessage("线程停止了！");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(HotmailMailRecev.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return endrecv;
                }
                else
                {
                    String Threadsql="select RevState from emailaccounts where ID='"+m_ID+"'";
                    result=m_mailFuncInterhot.ExcuteResult(Threadsql);

                    if (result.next())
                    {
                        int restate = result.getInt("RevState");
                        if(restate==0)
                        {
                            //m_mailFuncInterhot.SetThreadmark(true);
                            endrecv=true;
                            Thread.sleep(3000);
                            m_mailFuncInterhot.ShowMessage("线程停止了！");
                            
                       
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
                 if(!m_mailFuncInterhot.GetcookieAllThreadMark())
                {
                     m_mailFuncInterhot.ShowMessage("正在停止线程...请耐心等待，不要进行其他操作！");
                     Thread.sleep(3000);
                     m_mailFuncInterhot.ShowMessage("线程停止了！");
                     endrecv =true;
                    
                    return endrecv;
                }
                else
                {
                    String Threadsql="select RevState from emailcookie where ID='"+m_ID+"'";
                    result=m_mailFuncInterhot.ExcuteResult(Threadsql);

                    if (result.next())
                    {
                        int restate = result.getInt("RevState");
                        if(restate==0)
                        {
                           // m_mailFuncInterhot.SetThreadmark(true);
                            
                            endrecv=true;
                            Thread.sleep(3000);
                            m_mailFuncInterhot.ShowMessage("线程停止了！");
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

    private NameValuePair[] GetMorePageIndata(String strHtml, int PageIndex) {

                String pnCur;						//表示页数
		String pnAm;
		String pnAd;
		String pnDir;
		String pnMid;
		String pnSkip;
		String d;
		String mt="";



                pnCur=putstr(strHtml,"pnCur=\\\"","\\\"",PageIndex);
                pnAm=putstr(strHtml,"pnAm=\\\"","\\\"",PageIndex);
                pnAd=putstr(strHtml,"pnAd=\\\"","\\\"",PageIndex);
                pnAd = pnAd.replace("&#58;", "\\:");
                pnDir=putstr(strHtml,"pnDir=\\\"","\\\"",PageIndex);
                pnMid=putstr(strHtml,"pnMid=\\\"","\\\"",PageIndex);
                pnSkip=putstr(strHtml,"pnSkip=\\\"","\\\"",PageIndex);
                d="true,false,true,{\"00000000-0000-0000-0000-000000000001\","+pnDir+"," +sAsc+
					","+sBy+",false,\""+pnAm+"\"," +
					"\""+pnAd+"\","+pnCur+","+pnMid+",false," +
					"\"\","+MailCountTotal+",-1,Off},false,null";
                mt=putstr(cookiesStr,"mt=",";",0);

		NameValuePair[] Indata={new NameValuePair("cn","Microsoft.Msn.Hotmail.Ui.Fpp.MailBox"),
					new NameValuePair("mn","GetInboxData"),
					new NameValuePair("d",d),
					new NameValuePair("v","1"),
					new NameValuePair("mt",mt)
			};

		return Indata;
    }


}
