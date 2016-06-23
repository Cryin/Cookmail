/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package email;

import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author root
 */
public interface MailFuncInter {
    abstract void ShowMessage(String strMess);
    abstract void Connectdb();
    abstract void Disconnectdb();
    abstract int Excutesql(String sql);
    abstract ResultSet ExcuteResult(String sql);
    abstract int Excutemdl(String sql);
    abstract DefaultTableModel initTable_User();
    abstract void RefashTable();
    abstract void RefashCookie();
    abstract boolean GetThreadmark();
    abstract void SetThreadmark(boolean mark);
    abstract boolean GetRecvType();
    abstract void SetRecvType(boolean Rectype);
    abstract void SetuserAllThreadMark(boolean allmark);
    abstract boolean GetuserAllThreadMark();
    abstract void SetcookieAllThreadMark(boolean allmark);
    abstract boolean GetcookieAllThreadMark();
    abstract String GetMailPath();

}
