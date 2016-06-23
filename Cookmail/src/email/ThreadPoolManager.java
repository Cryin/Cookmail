/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package email;

import java.util.Vector;
import javax.swing.JOptionPane;

/** 2010－01－19
 *  线程池管理类
 * @author ap.xEmailBox
 */
public class ThreadPoolManager
{
    private int MaxThread;
    public Vector vector;
    MailFuncInter m_mailInter;
    /**
     * 构造函数首先开启最大线程数的线程
     * @param threadCount
     */
    public ThreadPoolManager(int threadCount,MailFuncInter mailInter)
    {
        m_mailInter=mailInter;
        setMaxThread(threadCount);
        vector=new Vector();
        for(int i=1;i<=MaxThread;i++)
        {
            ExecuteRecvThread thread=new ExecuteRecvThread(i,m_mailInter);
            vector.addElement(thread);
            thread.start();                                                     //首先开启最大线程数并进入睡眠状态，等待用户进行收邮操作
        }

    }
    /**
     * 设置最大线程数
     * @param threadCount
     */
    public void setMaxThread(int threadCount)
    {
        MaxThread=threadCount;
    }
    /**
     * 激活线程
     * @param arg
     */
    private ExecuteRecvThread currentThread;
    public void BeginRecThread(Object arg,boolean recvType)
    {
        int i;
        for(i=0;i<vector.size();i++)
        {
            currentThread=(ExecuteRecvThread)vector.elementAt(i);

            if(!currentThread.isRunning())
            {
               
                    //System.out.println("thread"+(i+1)+"is begin:"+arg);
                    String ThreadName = null;
                    ThreadName = String.valueOf(arg);
                    currentThread.setName(ThreadName);
                    currentThread.setArgument(arg, recvType);
                    currentThread.setRunning(true);
                    return;
                
            }
        }
        if(i==vector.size())
        {
            JOptionPane.showMessageDialog(null, "线程数已到最大数，请稍后在试！","提示",JOptionPane.WARNING_MESSAGE);
        }
    }
    /**
     * 停止单个线程
     * @param arg
     * @param recvType
     */
    public void AbortThread(Object arg,boolean recvType)
    {
        try
        {
            int i;
            for(i=0;i<vector.size();i++)
            {
           
                if(currentThread.isRunning())
                {
                    //System.out.println("thread"+(i+1)+"is begin:"+arg);
                    String name=String.valueOf(arg);
                    String Threadname=currentThread.getName();
                    m_mailInter.SetThreadmark(false);
                   // if(name.equals(Threadname))
                   // {
                        currentThread.setAbort(arg,recvType);
                        return;
                   // }

                
                }
            }
        }
        catch(Exception err)
        {
            m_mailInter.ShowMessage("警告： 线程没有在运行或者已经运行完成！");
        }
        

    }
    public void AbortAllThread(Object arg,boolean recvType)
    {
        try
        {
            int i;
            for(i=0;i<vector.size();i++)
            {

                if(currentThread.isRunning())
                {
                    //System.out.println("thread"+(i+1)+"is begin:"+arg);
                    String name=String.valueOf(arg);
                    String Threadname=currentThread.getName();
                    m_mailInter.SetThreadmark(false);
                    if(name.equals(Threadname))
                    {
                        currentThread.setAllAbort(arg,recvType);
                        return;
                    }

                }
            }
        }
        catch(Exception err)
        {
            m_mailInter.ShowMessage("警告： 线程没有在运行或者已经运行完成！");
        }
    }
}
