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
import java.net.URLEncoder;
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
import sun.misc.BASE64Encoder;

/**
 *
 * @author root
 */
public class YahooMailRecev extends Mail{

    boolean endrecv=false;
    private MailFuncInter m_mailFuncInteryahoo;
    public  String urls="";
    public  String cookiesStr="";
    public  String Host="";
    public String BoxName;
    public YahooMailRecev( MailFuncInter mailFuncInter )
    {
        m_mailFuncInteryahoo = mailFuncInter;
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
         m_mailFuncInteryahoo.ShowMessage(m_accounts+" :下载完毕");
    }

    private void Cookielogin()
    {
        String url="";
	String strHtml="";
	int index;
        cookiesStr=m_cookie;
        if(cookiesStr.indexOf("intl=jp")>0)
        {
            Cookieloginjp();
        }
	url="http://mail.yahoo.com/";
        m_mailFuncInteryahoo.ShowMessage(m_accounts+": 开始登录");
	try
	{
            strHtml=getRequest(url);
            if((index=urls.indexOf("/mc/welcome?"))>0)
            {
                m_mailFuncInteryahoo.ShowMessage(m_accounts+": 登录成功");
		Host=urls.substring(0, urls.indexOf("welcome?"));
		GetBoxName(strHtml);
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

    private void UserLogin()
    {
        if(m_postfix.indexOf("jp")>0)
        {
            loginjp();
            return;
        }
        String url="";
	String strHtml="";
	int index;
	url="http://mail.yahoo.com/";
        m_mailFuncInteryahoo.ShowMessage(m_accounts+": 开始登录");
	try
	{
            strHtml=getRequest(url);
            NameValuePair[] Indata=GetLoginIndata(strHtml);
            url="https://login.yahoo.com/config/login?";
            strHtml=postRequest(url,Indata);
            if((index=strHtml.indexOf("window.location.replace"))>0)
            {
                m_mailFuncInteryahoo.ShowMessage(m_accounts+": 登录成功");
		url=putstr(strHtml,"(\"","\")",index);
		Host=url.substring(0, url.indexOf("welcome?"));
		strHtml=getRequest(url);
		GetBoxName(strHtml);
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
    *
    * @param strHtml
    */
    private void GetBoxName(String strHtml)
    {

	try
	{
            int i=0;
            String strFolder;
            String [][]boxList=new String[200][3];
            int boxIndex=strHtml.indexOf("defaultfolders");
            strFolder=putstr(strHtml,"defaultfolders","</ol> </div>",boxIndex);
            boxIndex=0;
            while((boxIndex=strFolder.indexOf("<li",boxIndex))>0)
            {
		String folderinfo=putstr(strFolder,"<li","/li>",boxIndex);
		if(folderinfo!="-1")
		{
			String boxname=putstr(folderinfo,"id=\"","\"",0);
			String boxid=putstr(folderinfo,"href=\"","\"",0);
			if(boxid!="-1"&&boxname!="-1"&&boxid!=""&&boxname!="")
			{
				boxList[i][0]=boxid;
				boxList[i][1]=boxname;
				i++;
			}
		}
		boxIndex++;
            }
            if((boxIndex=strHtml.indexOf("listings custom"))>0)
            {
		strFolder=putstr(strHtml,"listings custom","</ol></div>",boxIndex);
		boxIndex=0;
		while((boxIndex=strFolder.indexOf("<li",boxIndex))>0)
		{
			String folderinfo=putstr(strFolder,"<li","/li>",boxIndex);
			if(folderinfo!="-1")
			{
				String boxname=putstr(folderinfo,"id=\"","\"",0);
				String boxid=putstr(folderinfo,"href=\"","\"",0);
				if(boxid!="-1"&&boxname!="-1"&&boxid!=""&&boxname!="")
				{
					boxList[i][0]=boxid;
					boxList[i][1]=boxname;
					i++;
				}
			}
                boxIndex++;
                }
            }
            for(int j=0;j<i;j++)
            {
		m_mailFuncInteryahoo.ShowMessage(m_accounts+": "+boxList[j][1]+"开始下载 ");
		GetMailId(boxList[j][0],boxList[j][1],0);
                if(endrecv)
                {
                     return;
                }
            }
        }
        catch(Exception err)
        {
            m_mailFuncInteryahoo.ShowMessage(m_accounts+" :get box failed!"+err.getMessage());
        }
    }
    private void GetMailId(String boxid, String boxname,int m_sep)
    {
	// TODO Auto-generated method stub
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
	String strMail;
	String nextUrl;
	boolean unRead;						//read or not
	int messageList = 0;                       //消息列表
	String mCrumb = "";                        //消息  mCrumb
	String spamCleanupPeriod = "";             //消息 spamCleanupPeriod
	String prefNumOfMid = "";                  //消息 prefNumOfMid
	String pSize = "";                         //消息 pSize
	String totalCount = "";                    //消息 totalCount
	String tt = "";                            //消息  tt
	String aCrumb = "";                        //信息  aCrumb
        boolean Attachments;


	listMailurl=Host+boxid;
        BoxName=boxname;
	try
	{
            strFolders=getRequest(listMailurl);
            messageList = strFolders.indexOf("aCrumb =", 0);
            if (messageList > 0)
	    {
	        aCrumb = putstr(strFolders, "\"", "\"", messageList);
	    }
	    messageList = strFolders.indexOf("name=\"mcrumb\"", 0);
	    if (messageList > 0)
	    {
	        mCrumb = putstr(strFolders, "value=\"", "\">", messageList);
	    }
	    messageList = strFolders.indexOf("spamCleanupPeriod", 0);
	    if (messageList > 0)
	    {
	        spamCleanupPeriod = putstr(strFolders, "value=\"", "\">", messageList);
	    }

	    messageList = strFolders.indexOf("prefNumOfMid", 0);
	    if (messageList > 0)
	    {
	        prefNumOfMid = putstr(strFolders, "value=\"", "\">", messageList);
	    }

	    messageList = strFolders.indexOf("name=\"pSize\"", 0);
	    if (messageList > 0)
	    {
	        pSize = putstr(strFolders, "value=\"", "\">", messageList);
	    }

	    messageList = strFolders.indexOf("totalCount", 0);
	    if (messageList > 0)
	    {
	        totalCount = putstr(strFolders, "value=\"", "\">", messageList);
	    }
	    messageList = strFolders.indexOf("name=\"tt\"", 0);
	    if (messageList > 0)
	    {
	        tt = putstr(strFolders, "value=\"", "\">", messageList);
	    }
            mailIndex=strFolders.indexOf("<tbody>");
            while((mailIndex=strFolders.indexOf("<tr",mailIndex))>0)
            {
                mailMessage=putstr(strFolders,"<tr","</tr>",mailIndex);

		mailId=putstr(mailMessage,"\"mid\" value=\"","\">",0);
		if(mailId!="-1"&&mailId!="")
		{
                    String selsql="select count(*) from MailRecord where Midvalue="+"'"+mailId+"'";
                    int count =m_mailFuncInteryahoo.Excutesql(selsql);
                    if(0==count)
                    {
                        String []array=mailMessage.split("</td>");
                        mailFrom=putstr(array[4],"&lt;","&gt;",0);
                        Attachments=false;
                        if(array[5].indexOf("Attachments")>0)
                        {
                            Attachments=true;
                        }//array[5]判断是否含有附件
			mailSubject=putstr(array[6],"title=\"","\">",0);
			mailSubject=mailSubject.trim();
			array[7]=array[7]+"end";
			//mailDate=putstr(array[7]+" ","\">","end",0);
			//mailDate=mailDate.trim();
			mailUrl=putstr(array[6],"<a href=\"","\"",0);
			if(array[0].indexOf("Unread")>0)
			{
                            unRead=true;
			}
                        else
			{
                            unRead=false;
			}
			downUrl=Host+mailUrl+"&head=f&pView=1&view=print";
			strMail=requestEmail(downUrl);
                        SaveMailText(strMail,Attachments,downUrl,mailId,mailSubject,mailFrom,m_sep);
                        m_mailFuncInteryahoo.ShowMessage(m_accounts+" :"+mailSubject+"下载成功");
                        if(unRead==true)                                        //置未读
			{
                            String fid = putstr(mailUrl, "fid=", "&", 0);
                            String rand = putstr(mailUrl, ".rand=", "&", 0);
                            String YY = "&fid=" + fid + "&.rand=" + rand;
                            String HostYY = Host + "showFolder?" + YY + "&needG&acrumb=" + aCrumb + "&op=data";
                            NameValuePair[] indata ={new NameValuePair("startMid","0"),
                    		new NameValuePair("sort","date"),
                    		new NameValuePair("order","down"),
                    		new NameValuePair("mcrumb",mCrumb),
                    		new NameValuePair("spamCleanupPeriod",spamCleanupPeriod),
                    		new NameValuePair("prefNumOfMid",prefNumOfMid),
                    		new NameValuePair("pSize",pSize),
                    		new NameValuePair("totalCount",totalCount),
                    		new NameValuePair("tt",tt),
                    		new NameValuePair("cmd","mask"),
                    		new NameValuePair("top_mark_select","msg.markunread"),
                    		new NameValuePair("top_move_select",""),
                    		new NameValuePair("mid",mailId),
                    		new NameValuePair("bottom_mark_select","msg.markunread"),
                    		new NameValuePair("bottom_move_select",""),
                    		new NameValuePair("self_action_msg_topmark","Mark")
                                        } ;
                                String strMark=postRequest(HostYY,indata);
                                //m_mailFuncInteryahoo.ShowMessage(strMark);
                                unRead = false;
                         }
                         Date now = new Date(System.currentTimeMillis());
                         DateFormat date = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
                         String strdate = date.format(now);
                         String insql="insert into MailRecord(EmailID,Username,Emailbox,EmailType,EmailDate,MidValue) values (null,'"+m_accounts+"','"
                             +BoxName+"','"+m_MailType+"','"+strdate+"','"+mailId+"')";
                         count=m_mailFuncInteryahoo.Excutemdl(insql);
                         mailTotalCounts++;
                          m_sep++;
                    }
                }
                mailIndex++;
                if(!m_mailFuncInteryahoo.GetThreadmark())
                 {
                    if(idEndRecv())
                    {
                            return ;
                    }
                }
             }
             String nextMessage=putstr(strFolders,"Previous","Go to",0);
             if(nextMessage.indexOf("<a href=\"showFolder")>0)
             {
                 String nexturl=putstr(nextMessage,"<a href=\"","\"",0);
                 GetMailId(nexturl, boxname,m_sep);
                 m_mailFuncInteryahoo.ShowMessage(m_accounts+": 下一页开始下载");
                 if(endrecv)
                 {
                      return;
                 }
                 nextMessage="";
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
     * 邮件头
     * @return
     */
    private String headerFile()
    {
	// TODO Auto-generated method stub
	String s = "MIME-Version: 1.0" + "\r\n"
        + "Content-Type: multipart/mixed;" + "\r\n"

        + "        " + "boundary=\"----=_NextPart_000_0010_01C8B66B.9F041560\"" + "\r\n"
        + "X-Priority: 3" + "\r\n"
        + "X-MSMail-Priority: Normal" + "\r\n"
        + "X-Unsent: 1" + "\r\n"
         + "X-MimeOLE: Produced By Microsoft MimeOLE V6.00.2900.3138" + "\r\n" + "\r\n"
         + "This is a multi-part message in MIME format." + "\r\n" + "\r\n"
        + "------=_NextPart_000_0010_01C8B66B.9F041560" + "\r\n"
        + "Content-Type: multipart/alternative;" + "\r\n"
        + "        " + "boundary=\"----=_NextPart_001_0011_01C8B66B.9F041560\"" + "\r\n" + "\r\n" + " " + "\r\n"
        + "------=_NextPart_001_0011_01C8B66B.9F041560" + "\r\n"
        + "Content-Type: text/plain;" + "\r\n"
        + "        " + "charset=\"utf-8\"" + "\r\n"
        + "Content-Transfer-Encoding: base64" + "\r\n" + "\r\n"
        + "WWFob28h55S16YKuIC0gaHVpeWk0NTg1QHlhaG9vLmNvbS5jbg0KICAgICAgICDmiZPljbAgLSDl" + "\r\n"
        + "hbPpl63nqpflj6MgIA0KICAgICAgWC1BcHBhcmVudGx5LVRvOiBodWl5aTQ1ODVAeWFob28uY29t" + "\r\n"
        + "LmNuIHZpYSAyMDIuMTY1LjEwMy43NzsgV2VkLCAzMSBPY3QgMjAwNyAxMjoxMDowNiArMDgwMCAN" + "\r\n"
        + "CiAgICAgIFgtT3JpZ2luYXRpbmctSVA6IFsyMjAuMTgxLjEyLjExXSANCiAgICAgIFJldHVybi1Q" + "\r\n"
        + "YXRoOiA8Y2hpbHk2MTFAMTYzLmNvbT4gDQogICAgICBBdXRoZW50aWNhdGlvbi1SZXN1bHRzOiBt" + "\r\n"
        + "dGExMTUubWFpbC5jbmIueWFob28uY29tIGZyb209MTYzLmNvbTsgZG9tYWlua2V5cz1uZXV0cmFs" + "\r\n"
        + "IChubyBzaWcpIA0KICAgICAgUmVjZWl2ZWQ6IGZyb20gMjIwLjE4MS4xMi4xMSAoSEVMTyBtMTIt" + "\r\n"
        + "MTEuMTYzLmNvbSkgKDIyMC4xODEuMTIuMTEpIGJ5IG10YTExNS5tYWlsLmNuYi55YWhvby5jb20g" + "\r\n"
        + "d2l0aCBTTVRQOyBXZWQsIDMxIE9jdCAyMDA3IDEyOjEwOjA2ICswODAwIA0KICAgICAgUmVjZWl2" + "\r\n"
        + "ZWQ6IGZyb20gVVNFUi0yN0Y3MUMwNDI4ICh1bmtub3duIFsyMjIuODIuODEuMTNdKSBieSBzbXRw" + "\r\n"
        + "NyAoQ29yZW1haWwpIHdpdGggU01UUCBpZCB3S2pBQzdDN0hBUVhBQ2hIcVU3R2N3PT0uMzU4NjNT" + "\r\n"
        + "MzsgV2VkLCAzMSBPY3QgMjAwNyAxMjoxMDowMCArMDgwMCAoQ1NUKSANCiAgICAgIOWPkeS7tuS6" + "\r\n"
        + "ujogImFhIiA8Y2hpbHk2MTFAMTYzLmNvbT4gDQogICAgICDkuLvpopg6IGJiIA0KICAgICAg5pS2" + "\r\n"
        + "5Lu25Lq6OiBodWl5aTQ1ODVAeWFob28uY29tLmNuIA0KICAgICAgQ29udGVudC1UeXBlOiB0ZXh0" + "\r\n"
        + "L2h0bWwgDQogICAgICBDb250ZW50LVRyYW5zZmVyLUVuY29kaW5nOiBxdW90ZWQtcHJpbnRhYmxl" + "\r\n"
        + "IA0KICAgICAg5pel5pyfOiBXZWQsIDMxIE9jdCAyMDA3IDEyOjEwOjA2ICswODAwIA0KICAgICAg" + "\r\n"
        + "WC1Qcmlvcml0eTogMyANCiAgICAgIFgtTGlicmFyeTogSW5keSA5LjAwLjEwIA0KICAgICAgTUlN" + "\r\n"
        + "RS1WZXJzaW9uOiAxLjAgDQogICAgICBYLUNvcmVtYWlsLUFudGlzcGFtOiAxVTNZeG4wV2ZBU3It" + "\r\n"
        + "VkZBVURJY1NzR3ZmSlRSVVVVanlrRnhWQ0Y3N3hDNkl4S280IGtFVjR5bFl4MEV4NEEyanNJRTE0" + "\r\n"
        + "djI2cjRqNkY0VU1JSUYweHZFeDRBMmpzSUUxNHYyNnIxajZyNFVNMnZZejRJRTA0azIgNFZBdndW" + "\r\n"
        + "QUtJNElyTUk4RTY3QUY2N2tGMVZBRndJMF9KcjBfSnJ5bDdJMFk2NGtfTTR4dkYySUU1SThDclZB" + "\r\n"
        + "RXc0MGtNcyAwRXJjeFljN0lZYXdDSTQySVk2eElJanh2MjB4dkUxNHYyNnIxajZyMXhNeENqblZB" + "\r\n"
        + "cW43eHZyd0FLelZBQzB4Q0ZqMkFJIDZjeDdNSUlGMHh2RTJJeDBjSThJY1ZDWTF4MDI2N0FLeFZX" + "\r\n"
        + "VUpWVzhKd0FGeFZDYVl4dkk0VkNJd2NBS3pJQXRNeGtJN0kgSTJqSTh2ejR2RXdJeEdyd0NJNDJJ" + "\r\n"
        + "WTZJOEU4N0l2NnhrRjdJMEUxNHYyNnIxajZyNFVNeGtJZWN4RXdWQUZ3Vlc4WHdBRiBGMjBFMTR2" + "\r\n"
        + "MjZyMWo2cjRVTWNJajZ4SUlqeHYyMHh2RTE0djI2cjEwNnIxNU0ya0s2NHhJeDdJRTg3STIweENq" + "\r\n"
        + "ajRJRTU0IDhtNnIxZkdyMThLd0FZRlZDamp4Q3JNN0NJY1ZBRno0a0s2cjFqNnIxOE1jMDJGNDBF" + "\r\n"
        + "NDJJMjZ4QzJhNDh4TXhrRnMyMEUgWTR2RThzeEtqNHh2MXdBWWpzeEk0Vld4SmprYUxhQUZMU1Vy" + "\r\n"
        + "VVVVVVVqdmptM0FhTGFKM1VqSUZ5VHVZdmp4VUJYZGJVVSBVVVVVQWFMYUozVSANCiAgICAgIE1l" + "\r\n"
        + "c3NhZ2UtSWQ6IDw0NzI4MDAxOC4wNDJCRTguMjAzMTlAbTEyLTExLjE2My5jb20+IA0KICAgICAg" + "\r\n"
        + "Q29udGVudC1MZW5ndGg6IDc2NyANCg0KaGVsbG8gc2FsYW0gaGkg5L2g5aW9IA0KDQoNCg==" + "\r\n" + "\r\n"
        + "------=_NextPart_001_0011_01C8B66B.9F041560" + "\r\n"
        + "Content-Type: text/html;" + "\r\n"
        + "        " + "charset=\"utf-8\""+ "\r\n"
        + "Content-Transfer-Encoding: base64" + "\r\n";

		return s;
	}
    /**
    * get post indata
    * @param strHtml
    * @return
     */
    private NameValuePair[] GetLoginIndata(String loginMessage)
    {
	// TODO Auto-generated method stub
        int messageList = 0;
        String tries = "";                        //消息".tries"
        String src = "";                          //消息".src"
        String intl = "";                         //消息".intl"
        String u = "";                            //消息".u"
        String v = "";                            //消息".v"
        String challenge = "";                    //消息".challenge"
        String chkP = "";                         //消息".chkP"
        String hasMsgr = "";                      //消息".hasMsgr"


        messageList = loginMessage.indexOf("post");
        if (messageList > 0)
        {
            messageList = loginMessage.indexOf("\".tries\"", messageList);
            if (messageList > 0)
            {
                tries = putstr(loginMessage, "value=\"", "\">", messageList);

                messageList = loginMessage.indexOf("\".src\"", messageList);
                if (messageList > 0)
                {
                    src = putstr(loginMessage, "value=\"", "\">", messageList);

                    messageList = loginMessage.indexOf("\".intl\"", messageList);
                    if (messageList > 0)
                    {
                        intl = putstr(loginMessage, "value=\"", "\">", messageList);

                        messageList = loginMessage.indexOf("\".u\"", messageList);
                        if (messageList > 0)
                        {
                            u = putstr(loginMessage, "value=\"", "\">", messageList);

                            messageList = loginMessage.indexOf("\".v\"", messageList);
                            if (messageList > 0)
                            {
                                v = putstr(loginMessage, "value=\"", "\">", messageList);
                            }
                            messageList = loginMessage.indexOf("\".challenge\"", messageList);
                            if (messageList > 0)
                            {
                                challenge = putstr(loginMessage, "value=\"", "\"", messageList);
                            }
                            messageList = loginMessage.indexOf("\"hasMsgr\"", messageList);
                            if (messageList > 0)
                            {
                                hasMsgr = putstr(loginMessage, "value=\"", "\">", messageList);
                            }
                            messageList = loginMessage.indexOf("\".chkP\"", messageList);
                            if (messageList > 0)
                            {
                                chkP = putstr(loginMessage, "value=\"", "\">", messageList);
                            }

                            if (tries == "-1" || src == "-1" || intl == "-1" || u == "-1" || v == "-1"
                            	|| challenge == "-1" || chkP == "-1" || hasMsgr == "-1")
                            {
                                m_mailFuncInteryahoo.ShowMessage(m_accounts+": 取消息失败！");
                            }
                        }
                    }
                }
            }

        }
        
        
        NameValuePair[] Indata={new NameValuePair(".tries",tries),
        		new NameValuePair(".src",src),
        		new NameValuePair(".md5",""),
        		new NameValuePair(".hash",""),
        		new NameValuePair(".js",""),
        		new NameValuePair(".last",""),
        		new NameValuePair("promo",""),
        		new NameValuePair(".bypass",""),
        		new NameValuePair(".intl",intl),
        		new NameValuePair(".bypass",""),
        		new NameValuePair(".partner",""),
        		new NameValuePair(".u",u),
        		new NameValuePair(".v",v),
        		new NameValuePair(".challenge",challenge),
        		new NameValuePair(".yplus",""),
        		new NameValuePair(".emailCode",""),
        		new NameValuePair("pkg",""),
        		new NameValuePair("stepid",""),
        		new NameValuePair(".ev",""),
        		new NameValuePair("hasMsgr",hasMsgr),
        		new NameValuePair(".chkP",chkP),
        		new NameValuePair(".done","http://mail.yahoo.com"),
        		new NameValuePair(".pd","ym_ver=0&c="),
        		new NameValuePair("login",m_accounts),
        		new NameValuePair("passwd",m_password),
        		new NameValuePair(".save","Sign+In")

            };
            return Indata;
		
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

	MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	HttpClient httpClient = new HttpClient(connectionManager);
	httpClient.getParams().setCookiePolicy(
			CookiePolicy.BROWSER_COMPATIBILITY);
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
		int statusCode = httpClient.executeMethod(getMethod);
		if (statusCode != HttpStatus.SC_OK)
		{
			m_mailFuncInteryahoo.ShowMessage(m_accounts+": yahoo Request Method failed: "
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
		System.out.println("Please check your provided http address!");
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
						System.err.println("FastMail Request Method failed: "
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

					System.out.println("The page was redirected to:" + location);
				}
				else
				{
					url = "http://" + postMethod.getURI().getHost() + location;
					GetMethod getMethod = new GetMethod(url);
					statusCode = httpClient.executeMethod(getMethod);

					if (statusCode != HttpStatus.SC_OK)
					{
						System.err.println("FastMail Request Method failed: "
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
					System.out.println("The page was error:" + location);
				}

				return html;

			} else {
				System.err.println("Location field value is null.");
			}
			return html;
		}
		//System.out.println(postMethod.getResponseCharSet());
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
     * 保存邮件，自己组邮件eml格式文件，如果有附件则添加附件
     * @param strMail
     * @param Attachments
     * @param downUrl
     * @param mailId
     * @param mailSubject
     * @param mailFrom
     * @param j
     */
    private void SaveMailText(String strMail, boolean Attachments,String downUrl,String mailId,String mailSubject, String mailFrom, int j)
    {
        byte[] strAttachment=null;
        String mailContent;
        int attIndex=0;
        int attNumber=1;
        mailContent=strMail;
	if(strMail!=null)
        {
            try
            {
                int index=0;
                String mailDate =" Wed Dec 30 02:24:28 2010";
                if((index=strMail.indexOf("Date:"))>0)
                {
                    mailDate=putstr(strMail,"<td>","</td>",index);
                }
		String Mailpath=m_mailFuncInteryahoo.GetMailPath();
                BASE64Encoder encode=new BASE64Encoder();
                strMail=encode.encode(strMail.getBytes());

	    	File directory=new File(Mailpath);
                Date now = new Date(System.currentTimeMillis());
                DateFormat date = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
                String strdate = date.format(now);
                String path="/"+strdate+"/"+m_accounts+"@"+m_postfix+"/"+BoxName+"/";
                File file = new File(directory.getCanonicalPath()+path);

                if (file.exists())
                {
                   //System.out.println("folder exist");
                }
		else
		{
                    if (file.mkdirs())
                    {
                       // System.out.println("folder create done");
                    }
                    else
                    {
                        //System.out.println("folder create failed");
                    }
                }
                String fileName=Integer.toString(j)+"_"+mailSubject+".eml";
                File newfile=new File(directory.getCanonicalPath()+path+fileName);
		if (!newfile.exists())
                {
                    newfile.createNewFile();
		}
		BufferedWriter output = new BufferedWriter(new FileWriter(newfile));
                String header="\r\n"+
                        "Date:"+mailDate+"\r\n"+
                        "From:"+mailFrom+"\r\n"+
                        "To:"+m_accounts+"@"+m_postfix+"\r\n"+
                        "Subject:"+mailSubject+"\r\n";

                output.write(header);
		output.write(headerFile());
		output.newLine();
		output.write(strMail+"\r\n");
		output.write(mailend());
                output.flush();
                if(Attachments)
                {
                     String hostAtt;
                     hostAtt=Host.replace("/mc", "/ya");
                     hostAtt=hostAtt.replace("mc", "f");
                     String fid=putstr(downUrl,"fid=","&",0);
                     mailId=URLEncoder.encode(mailId, "UTF-8");
                     String atturl=hostAtt+"download?clean=0&fid="+fid+"&mid="+mailId;
                     while((attIndex=mailContent.indexOf("<span class=\"imgname\">",attIndex))>0)
                    {
                        attNumber++;
                        String pid=Integer.toString(attNumber);
                        String AttName=putstr(mailContent,"/>","</span>",attIndex);
                        if(AttName.indexOf(".")>0)
                        {
                            try
                            {
                                atturl = atturl + "&pid=" + pid + "&tnef=&prefFilename=" + AttName;
                                strAttachment = requestAttachment(atturl);
                                
                                output.write("\r\n"+nextPart());
                                output.write("\r\n"+ConType());
                                output.write("\r\n"+attmentName(AttName));
                                output.write("\r\n"+Getencode());
                                output.write("\r\n"+getfile(AttName));
                                output.write(encode.encode(strAttachment)+"\r\n");
                                output.write("\r\n"+nextPart());

                            } catch (HttpException ex) {
                                Logger.getLogger(YahooMailRecev.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                 Logger.getLogger(YahooMailRecev.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        attIndex++;

                     }
                     Attachments=false;
                     
                 }

	    	output.flush();
                output.close();
           }
           catch (Exception e)
           {
		e.printStackTrace();
           }

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
    private byte[] requestAttachment(String url) throws HttpException, IOException
    {
        byte[] byteContent = null;
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
			m_mailFuncInteryahoo.ShowMessage(m_accounts+ " :FastMail Request Method failed: "
					+ getMethod.getStatusLine());
		}

		byteContent=getMethod.getResponseBody();

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
		m_mailFuncInteryahoo.ShowMessage(m_accounts +" :Please check your provided http address!");
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
	return byteContent;
    }

    private String mailend() {

        String end="------=_NextPart_001_0011_01C8B66B.9F041560--" + "\r\n" + "\r\n" + "------=_NextPart_000_0010_01C8B66B.9F041560";
        return end;
    }

    private String nextPart() {

        String nextPart="------=_NextPart_000_0010_01C8B66B.9F041560";
        return nextPart;
    }

    private String ConType() {

        String contype="Content-Type: application/octet-stream;";
        return contype;
    }

    private String attmentName(String name) {

        String attmentName="        "+"name=\""+name+"\"";
        return attmentName;
    }

    private String Getencode() {

        String encode="Content-Transfer-Encoding: base64"+"\r\n"+"Content-Disposition: attachment;";
        return encode;
    }

    private String  getfile(String name) {

        String filename="        "+"filename=\""+name+"\"\r\n";
        return filename;
    }

    private void loginjp()
    {
        String url="";
	String strHtml="";
	int index;
        cookiesStr="";
	url="http://mail.yahoo.co.jp/";
        m_mailFuncInteryahoo.ShowMessage(m_accounts+": 开始登录");
        try
        {
            strHtml = getRequest(url);
            NameValuePair[] Indata=GetLoginIndatajp(strHtml);
            url="https://login.yahoo.co.jp/config/login?";
            strHtml=postRequest(url, Indata);
            if((index=strHtml.indexOf("window.location.replace"))>0)
            {
                m_mailFuncInteryahoo.ShowMessage(m_accounts+": 登录成功");
		url=putstr(strHtml,"(\"","\")",index);
		Host=url.substring(0, url.indexOf("/ym/login?"));
		strHtml=getRequest(url);
                if((index=strHtml.indexOf("<a href=\"/ym/Folders?YY"))>0)
                {
                    String folderurl=putstr(strHtml,"<a href=\"","\"",index);
                    url=Host+folderurl;
                }
                else
                {
                    m_mailFuncInteryahoo.ShowMessage(m_accounts+": 请求文件夹失败");
                }
                strHtml=getRequest(url);
		GetBoxNamejp(strHtml);
            }


        } catch (HttpException ex) {
            Logger.getLogger(YahooMailRecev.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(YahooMailRecev.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void GetBoxNamejp(String strHtml) {

        try
	{
            int i=0;
            String folderUrl;
            String [][]boxList=new String[200][3];
            int boxIndex=0;
            while((boxIndex=strHtml.indexOf("<a href=\"/ym/ShowFolder?box=",boxIndex))>0)
            {
		String folderinfo=putstr(strHtml,"<a","</tr>",boxIndex);
		if(folderinfo!="-1")
		{
			String boxid=putstr(folderinfo,"href=\"","\"",0);
			String boxname=putstr(folderinfo,"<b>","</b>",0);
			if(boxid!="-1"&&boxname!="-1"&&boxid!=""&&boxname!="")
			{
				boxList[i][0]=boxid;
				boxList[i][1]=boxname;
				i++;
			}
		}
		boxIndex++;
            }
            for(int j=0;j<i;j++)
            {
		m_mailFuncInteryahoo.ShowMessage(m_accounts+": "+boxList[j][1]+" 开始下载");
		GetMailIdjp(boxList[j][0],boxList[j][1],0);
                if(endrecv)
                {
                     return;
                }
            }
        }
        catch(Exception err)
        {
            m_mailFuncInteryahoo.ShowMessage(m_accounts+" :get box failed!"+err.getMessage());
        }
    }

    private void GetMailIdjp(String boxid, String boxname,int m_sep) {
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
	String strMail;
	String nextUrl;
	boolean unRead;						//read or not
	int messageList = 0;                       //消息列表
	String folderscrumb = "";                  //消息 prefNumOfMid
	String ymuri = "";                         //消息 pSize
	String newfoldermessage = "";                    //消息 totalCount
	String warnondelete = "";                            //消息  tt
	String crumb = "";                        //信息  aCrumb
        boolean Attachments;


	listMailurl=Host+boxid;
        BoxName=boxname;
	try
	{
            strFolders=getRequest(listMailurl);

            messageList = strFolders.indexOf("messageList", 0);
            if (messageList > 0)
	    {
                messageList=strFolders.indexOf(".crumb",messageList);
                if(messageList>0)
                {
                    crumb = putstr(strFolders, "value=\"", "\"", messageList);
                }
                messageList=strFolders.indexOf("warnondelete",messageList);
	        if(messageList>0)
                {
                    warnondelete = putstr(strFolders, "value=\"", "\">", messageList);
                }
                messageList=strFolders.indexOf("newfoldermessage",messageList);
                if(messageList>0)
                {
                    newfoldermessage = putstr(strFolders, "value=\"", "\">", messageList);
                }
                messageList=strFolders.indexOf("ymuri",messageList);
                if(messageList>0)
                {
                    ymuri = putstr(strFolders, "value=\"", "\">", messageList);
                }
                messageList=strFolders.indexOf("folderscrumb",messageList);
                if(messageList>0)
                {
                    folderscrumb = putstr(strFolders, "value=\"", "\">", messageList);
                }
	    }
            mailIndex=strFolders.indexOf("<tbody>");
            while((mailIndex=strFolders.indexOf("<tr",mailIndex))>0)
            {
                mailMessage=putstr(strFolders,"<tr","</tr>",mailIndex);

		mailId=putstr(mailMessage,"\"Mid\" value=\"","\">",0);
		if(mailId!="-1"&&mailId!="")
		{
                    String selsql="select count(*) from MailRecord where Midvalue="+"'"+mailId+"'";

                    int count =m_mailFuncInteryahoo.Excutesql(selsql);
                    if(0==count)
                    {
                        String []array=mailMessage.split("</td>");
                        if(array.length<8)
                        {
                            array[1]=array[1]+"end";
                            mailFrom=putstr(array[1],"<td>","end",0);
                            Attachments=false;
                            if(array[2].indexOf("attachments")>0)
                            {
                                Attachments=true;
                            }//array[2]判断是否含有附件
                            mailSubject=putstr(array[2],"\">","</a>",0);
                            mailSubject=mailSubject.trim();
                            mailUrl=putstr(array[2],"href=\"","\"",0);
                        }
                        else
                        {
                            array[2]=array[2]+"end";
                            mailFrom=putstr(array[2],"<td>","end",0);
                            Attachments=false;
                            if(array[4].indexOf("attachments")>0)
                            {
                                Attachments=true;
                            }//array[5]判断是否含有附件
                            mailSubject=putstr(array[6],"\">","</a>",0);
                            mailSubject=mailSubject.trim();
                            mailUrl=putstr(array[6],"href=\"","\"",0);
                        }
			if(array[0].indexOf("msgnew")>0)
			{
                            unRead=true;
			}
                        else
			{
                            unRead=false;
			}
			downUrl=Host+mailUrl+"&Nhead=f&PRINT=1";
			strMail=requestEmail(downUrl);
                        SaveMailTextjp(strMail,Attachments,downUrl,mailId,mailSubject,mailFrom,m_sep);
                        m_mailFuncInteryahoo.ShowMessage(m_accounts+" :"+mailSubject+"下载成功");
                        if(unRead==true)                                        //置未读
			{
                            mailId=URLEncoder.encode(mailId, "UTF-8");
                            String YY = putstr(downUrl,"YY=","&Nhead",0);
                            YY="YY="+YY;
                            YY=URLEncoder.encode(YY, "UTF-8");
                            String HostYY = Host + "/ym/ShowFolder?" + YY;
                            NameValuePair[] indata ={new NameValuePair(".crumb",crumb),
                    		new NameValuePair("DEL",""),
                    		new NameValuePair("FLG","1"),
                    		new NameValuePair("MOV",""),
                    		new NameValuePair("NewFol",""),
                    		new NameValuePair("destBox",""),
                    		new NameValuePair("flags","unread"),
                    		new NameValuePair("warnondelete",warnondelete),
                    		new NameValuePair("newfoldermessage",newfoldermessage),
                    		new NameValuePair("ymuri",ymuri),
                                new NameValuePair("folderscrumb",folderscrumb),
                                new NameValuePair("urlextras",YY),
                                new NameValuePair("delete","%BA%EF%BD%FC"),
                                new NameValuePair("spam","%CC%C2%CF%C7%A5%E1%A1%BC%A5%EB%CA%F3%B9%F0"),
                                new NameValuePair("mark","%A5%D5%A5%E9%A5%B0"),
                                new NameValuePair("Mid",mailId),
                                new NameValuePair("delete","%BA%EF%BD%FC"),
                                new NameValuePair("spam","%CC%C2%CF%C7%A5%E1%A1%BC%A5%EB%CA%F3%B9%F0"),
                                new NameValuePair("mark","%A5%D5%A5%E9%A5%B0"),
                                new NameValuePair("move","%B0%DC%C6%B0")
                                        } ;
                                String strMark=postRequest(HostYY,indata);
                                //m_mailFuncInteryahoo.ShowMessage(strMark);
                                
                         }
                         Date now = new Date(System.currentTimeMillis());
                         DateFormat date = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
                         String strdate = date.format(now);
                         String insql="insert into MailRecord(EmailID,Username,Emailbox,EmailType,EmailDate,MidValue) values (null,'"+m_accounts+"','"
                                  +BoxName+"','"+m_MailType+"','"+strdate+"','"+mailId+"')";
                         count=m_mailFuncInteryahoo.Excutemdl(insql);
                         mailTotalCounts++;
                         m_sep++;
                    }
                }
                mailIndex++;
                if(!m_mailFuncInteryahoo.GetThreadmark())
                 {
                    if(idEndRecv())
                    {
                            return ;
                    }
                }
             }
            String nextMessage=putstr(strFolders,"前","次",0);
             if(nextMessage.indexOf("<a href=\"/ym/ShowFolder")>0)
             {
                 String nexturl=putstr(nextMessage,"<a href=\"","\"",0);
                 m_mailFuncInteryahoo.ShowMessage(m_accounts+": 下一页开始下载");
                 GetMailIdjp(nexturl, boxname,m_sep);
                 if(endrecv)
                {
                     return;
                }
                 nextMessage="";
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
     private void SaveMailTextjp(String strMail, boolean Attachments,String downUrl,String mailId,String mailSubject, String mailFrom, int j)
    {
        byte[] strAttachment=null;
        String mailContent;
        int attIndex=0;
        int attNumber=1;
        mailContent=strMail;
	if(strMail!=null)
        {
            try
            {
                int index=0;
                String mailDate =" Wed Dec 30 02:24:28 2010";
                if((index=strMail.indexOf("Date:"))>0)
                {
                    mailDate=putstr(strMail,"<td>","</td>",index);
                }
		String Mailpath=m_mailFuncInteryahoo.GetMailPath();
                BASE64Encoder encode=new BASE64Encoder();
                strMail=encode.encode(strMail.getBytes());

	    	File directory=new File(Mailpath);
                Date now = new Date(System.currentTimeMillis());
                DateFormat date = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
                String strdate = date.format(now);
                String path="/"+strdate+"/"+m_accounts+"@"+m_postfix+"/"+BoxName+"/";
                File file = new File(directory.getCanonicalPath()+path);

                if (file.exists())
                {
                   //System.out.println("folder exist");
                }
		else
		{
                    if (file.mkdirs())
                    {
                       // System.out.println("folder create done");
                    }
                    else
                    {
                        //System.out.println("folder create failed");
                    }
                }
                String fileName=Integer.toString(j)+"_"+mailSubject+".eml";
                File newfile=new File(directory.getCanonicalPath()+path+fileName);
		if (!newfile.exists())
                {
                    newfile.createNewFile();
		}
		BufferedWriter output = new BufferedWriter(new FileWriter(newfile));
                String header="\r\n"+
                        "Date:"+mailDate+"\r\n"+
                        "From:"+mailFrom+"\r\n"+
                        "To:"+m_accounts+"@"+m_postfix+"\r\n"+
                        "Subject:"+mailSubject+"\r\n";

                output.write(header);
		output.write(headerFile());
		output.newLine();
		output.write(strMail+"\r\n");
		output.write(mailend());
                output.flush();
                if(Attachments)
                {
                     
                     mailId=URLEncoder.encode(mailId, "UTF-8");
                     String atturl;
                     attIndex=mailContent.indexOf("tabfoldercontent");
                     while((attIndex=mailContent.indexOf("<b><a href=\"",attIndex))>0)
                    {
                        attNumber++;
                        atturl=putstr(mailContent,"<a href=\"","\"",attIndex);
                        String AttName=putstr(mailContent,"\">","</a>",attIndex);
                        if(AttName.indexOf(".")>0)
                        {
                            try
                            {
                                atturl = Host+atturl + "&download=1&filename="+AttName;
                                strAttachment = requestAttachment(atturl);

                                output.write("\r\n"+nextPart());
                                output.write("\r\n"+ConType());
                                output.write("\r\n"+attmentName(AttName));
                                output.write("\r\n"+Getencode());
                                output.write("\r\n"+getfile(AttName));
                                output.write(encode.encode(strAttachment)+"\r\n");
                                output.write("\r\n"+nextPart());

                            } catch (HttpException ex) {
                                Logger.getLogger(YahooMailRecev.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                 Logger.getLogger(YahooMailRecev.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        else
                        {
                            m_mailFuncInteryahoo.ShowMessage(m_accounts+": 取附件失败！");
                        }
                        attIndex++;

                     }
                     Attachments=false;

                 }

	    	output.flush();
                output.close();
           }
           catch (Exception e)
           {
		e.printStackTrace();
           }

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
            if(m_mailFuncInteryahoo.GetRecvType())
            {
                if(!m_mailFuncInteryahoo.GetuserAllThreadMark())
                {
                    m_mailFuncInteryahoo.ShowMessage("正在停止线程...请耐心等待，不要进行其他操作！");
                    endrecv =true;
                    try {
                        Thread.sleep(3000);
                        m_mailFuncInteryahoo.ShowMessage("线程停止了！");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(HotmailMailRecev.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return endrecv;
                }
                else
                {
                    String Threadsql="select RevState from emailaccounts where ID='"+m_ID+"'";
                    result=m_mailFuncInteryahoo.ExcuteResult(Threadsql);

                    if (result.next())
                    {
                        int restate = result.getInt("RevState");
                        if(restate==0)
                        {
                            //m_mailFuncInterhot.SetThreadmark(true);
                            endrecv=true;
                            Thread.sleep(3000);
                            m_mailFuncInteryahoo.ShowMessage("线程停止了！");


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
                 if(!m_mailFuncInteryahoo.GetcookieAllThreadMark())
                {
                     m_mailFuncInteryahoo.ShowMessage("正在停止线程...请耐心等待，不要进行其他操作！");
                     Thread.sleep(3000);
                     m_mailFuncInteryahoo.ShowMessage("线程停止了！");
                     endrecv =true;

                    return endrecv;
                }
                else
                {
                    String Threadsql="select RevState from emailcookie where ID='"+m_ID+"'";
                    result=m_mailFuncInteryahoo.ExcuteResult(Threadsql);

                    if (result.next())
                    {
                        int restate = result.getInt("RevState");
                        if(restate==0)
                        {
                           // m_mailFuncInterhot.SetThreadmark(true);

                            endrecv=true;
                            Thread.sleep(3000);
                            m_mailFuncInteryahoo.ShowMessage("线程停止了！");
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
    * get post indata
    * @param strHtml
    * @return
     */
    private NameValuePair[] GetLoginIndatajp(String loginMessage)
    {
	// TODO Auto-generated method stub
        int messageList = 0;
        String tries = "";                        //消息".tries"
        String src = "";                          //消息".src"
        String intl = "";                         //消息".intl"
        String u = "";                            //消息".u"
        String v = "";                            //消息".v"
        String challenge = "";                    //消息".challenge"
        String chkP = "";                         //消息".chkP"
        String hasMsgr = "";                      //消息".hasMsgr"


        messageList = loginMessage.indexOf("post");
        if (messageList > 0)
        {
            messageList = loginMessage.indexOf("\".tries\"", messageList);
            if (messageList > 0)
            {
                tries = putstr(loginMessage, "value=\"", "\">", messageList);

                messageList = loginMessage.indexOf("\".src\"", messageList);
                if (messageList > 0)
                {
                    src = putstr(loginMessage, "value=\"", "\">", messageList);

                    messageList = loginMessage.indexOf("\".intl\"", messageList);
                    if (messageList > 0)
                    {
                        intl = putstr(loginMessage, "value=\"", "\">", messageList);

                        messageList = loginMessage.indexOf("\".u\"", messageList);
                        if (messageList > 0)
                        {
                            u = putstr(loginMessage, "value=\"", "\">", messageList);

                            messageList = loginMessage.indexOf("\".v\"", messageList);
                            if (messageList > 0)
                            {
                                v = putstr(loginMessage, "value=\"", "\">", messageList);
                            }
                            messageList = loginMessage.indexOf("\".challenge\"", messageList);
                            if (messageList > 0)
                            {
                                challenge = putstr(loginMessage, "value=\"", "\"", messageList);
                            }
                            messageList = loginMessage.indexOf("\"hasMsgr\"", messageList);
                            if (messageList > 0)
                            {
                                hasMsgr = putstr(loginMessage, "value=\"", "\">", messageList);
                            }
                            messageList = loginMessage.indexOf("\".chkP\"", messageList);
                            if (messageList > 0)
                            {
                                chkP = putstr(loginMessage, "value=\"", "\">", messageList);
                            }

                            if (tries == "-1" || src == "-1" || intl == "-1" || u == "-1" || v == "-1"
                            	|| challenge == "-1" || chkP == "-1" || hasMsgr == "-1")
                            {
                                m_mailFuncInteryahoo.ShowMessage(m_accounts+": 取消息失败！");
                            }
                        }
                    }
                }
            }

        }

       NameValuePair[] Indata={
                        new NameValuePair(".tries",tries),
                        new NameValuePair(".src",src),
        		new NameValuePair(".md5",""),
        		new NameValuePair(".hash",""),
        		new NameValuePair(".js",""),
                        new NameValuePair(".last",""),
                        new NameValuePair("promo",""),
                        new NameValuePair(".intl",intl),
                        new NameValuePair(".bypass",""),
                        new NameValuePair(".partner",""),
                        new NameValuePair(".u",u),
                        new NameValuePair(".v",v),
                        new NameValuePair(".challenge",challenge),
                        new NameValuePair(".yplus",""),
                        new NameValuePair(".emailCode",""),
                        new NameValuePair("pkg",""),
                        new NameValuePair("stepid",""),
                        new NameValuePair(".ev",""),
                        new NameValuePair("hasMsgr",hasMsgr),
                        new NameValuePair(".chkP",chkP),
                        new NameValuePair(".done","http://mail.yahoo.co.jp"),
                        new NameValuePair(".pd",""),
                        new NameValuePair(".protoctl",""),
                        new NameValuePair("login",m_accounts),
                        new NameValuePair("passwd",m_password)
                        
                        };
        return Indata;


	}
/**
    * Get
    * @param url
    * @return
    * @throws HttpException
    * @throws IOException
    */
    private String requestEmail(String url) throws HttpException, IOException
    {

	MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	HttpClient httpClient = new HttpClient(connectionManager);
	httpClient.getParams().setCookiePolicy(
			CookiePolicy.BROWSER_COMPATIBILITY);
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
		int statusCode = httpClient.executeMethod(getMethod);
		if (statusCode != HttpStatus.SC_OK)
		{
			System.err.println("ru Request Method failed: "
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
		System.out.println("Please check your provided http address!");
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

    private void Cookieloginjp() {
        String url="";
	String strHtml="";
	int index;
        cookiesStr=m_cookie;

	url="http://mail.yahoo.co.jp/";
        m_mailFuncInteryahoo.ShowMessage(m_accounts+": 开始登录");
	try
	{
            strHtml=getRequest(url);
            if((index=urls.indexOf("/ym/login?"))>0)
            {
                m_mailFuncInteryahoo.ShowMessage(m_accounts+": 登录成功");
		Host=urls.substring(0, urls.indexOf("/ym/login?"));
		strHtml=getRequest(urls);
                if((index=strHtml.indexOf("<a href=\"/ym/Folders?YY"))>0)
                {
                    String folderurl=putstr(strHtml,"<a href=\"","\"",index);
                    url=Host+folderurl;
                }
                else
                {
                    m_mailFuncInteryahoo.ShowMessage(m_accounts+": 请求文件夹失败");
                }
                strHtml=getRequest(url);
		GetBoxNamejp(strHtml);
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
}
