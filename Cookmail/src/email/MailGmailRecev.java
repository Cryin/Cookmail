/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class MailGmailRecev extends Mail {

    boolean endrecv=false;
    private MailFuncInter m_mailFuncIntergmail;
    public  String urls="";
    public  String cookiesStr="";
    public  String Host="";
    public String BoxName;
    MailGmailRecev(MailFuncInter m_mailFuncInter)
    {
        m_mailFuncIntergmail=m_mailFuncInter;
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
         m_mailFuncIntergmail.ShowMessage(m_accounts+" :下载完毕");
        
    }

    private void Cookielogin()
    {

    }

    private void UserLogin()
    {
        String url="";
		String strHtml="";
		int index;
		String galx="";
		String dsh="";
		url="https://mail.google.com/mail/";
                m_mailFuncIntergmail.ShowMessage(m_accounts+" :开始登录");
		try
		{
			strHtml=getRequest(url);
			
			if((index=strHtml.indexOf("dsh"))!=-1)
			{
				dsh=putstr(strHtml,"value=\"","\"",index);
			}
			if((index=strHtml.indexOf("GALX"))!=-1)
			{
				galx=putstr(strHtml,"value=\"","\"",index);
			}
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		url="https://www.google.com/accounts/ServiceLoginAuth?service=mail";
		NameValuePair[] inData={new NameValuePair("ltmpl","default"),
				new NameValuePair("ltmplcache","2"),
				new NameValuePair("continue","http%3A%2F%2Fmail.google.com%2Fmail%2F%3F"),
				new NameValuePair("service","mail"),
				new NameValuePair("rm","false"),
				new NameValuePair("dsh",dsh),
				new NameValuePair("hl","zh-CN"),
				new NameValuePair("scc","1"),
				new NameValuePair("ss","1"),
				new NameValuePair("GALX",galx),
				new NameValuePair("Email","twd0302"),
				new NameValuePair("Passwd","1q2w3e4r"),
				new NameValuePair("rmShown","1"),
				new NameValuePair("signIn","%E7%99%BB%E5%BD%95"),
				new NameValuePair("asts","")
		};
		try {
			strHtml=postRequest(url,inData);
                        m_mailFuncIntergmail.ShowMessage(m_accounts+" :登录成功");
			
		} catch (HttpException e) {
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
					m_mailFuncIntergmail.ShowMessage("ru Request Method failed: "
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
				m_mailFuncIntergmail.ShowMessage("Please check your provided http address!");
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
							m_mailFuncIntergmail.ShowMessage("ru Request Method failed: "
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
							m_mailFuncIntergmail.ShowMessage("ru Request Method failed: "
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
					m_mailFuncIntergmail.ShowMessage("Location field value is null.");
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

}

