/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package email;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author root
 */
public class MySqlCon {

    public Statement stmt;
    public ResultSet result;
    public Connection con;
    public void connectdb()
    {
	//int totalCount=0;
        String userName="root";                                                 //mysql数据库所在主机用户名
	String passWord="kevin";                                                //密码
	String url="jdbc:mysql://localhost/emailplatform?";                             //连接到mysql数据库
	try
	{
		Class.forName("com.mysql.jdbc.Driver");                         //.newInstance();	//创建类
		con=DriverManager.getConnection(url,userName,passWord)  ;       //创建连接
		//stmt=con.createStatement();
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
    public void disconnectdb()
    {
        if(con != null)
	{
            try
	{
		con.close();
		//stmt.close();
		//result.close();
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
		con = null;
	}
    }
    public int ExcuteSql(String sql)
    {
        int count=0;
        try
	{
		result=stmt.executeQuery(sql);
                count=result.getRow();
	}
	catch (SQLException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

        return count;
    }
}
