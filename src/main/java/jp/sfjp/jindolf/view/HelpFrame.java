/*
 * help frame
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import jp.sfjp.jindolf.ResourceManager;
import jp.sfjp.jindolf.config.ConfigStore;
import jp.sfjp.jindolf.config.EnvInfo;
import jp.sfjp.jindolf.config.OptionInfo;
import jp.sfjp.jindolf.dxchg.TextPopup;
import jp.sfjp.jindolf.util.GUIUtils;

/**
 * ヘルプ画面。
 */
@SuppressWarnings("serial")
public class HelpFrame extends JFrame
        implements ActionListener, HyperlinkListener{

    private static final String HELP_HTML = "resources/html/help.html";

    private static final Logger LOGGER = Logger.getAnonymousLogger();


    private final JTabbedPane tabPanel = new JTabbedPane();
    private final JEditorPane htmlView = new JEditorPane();
    private final JTextArea vmInfo = new JTextArea();
    private final JButton closeButton = new JButton("閉じる");

    /**
     * コンストラクタ。
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public HelpFrame(){
        super();

        GUIUtils.modifyWindowAttributes(this, true, false, true);

        this.htmlView.setEditable(false);
        this.htmlView.setContentType("text/html");
        this.htmlView.putClientProperty(JEditorPane.W3C_LENGTH_UNITS,
                                        Boolean.TRUE);
        this.htmlView.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,
                                        Boolean.TRUE);
        Border border = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        this.htmlView.setBorder(border);
        this.htmlView.addHyperlinkListener(this);
        this.htmlView.setComponentPopupMenu(new TextPopup());

        this.vmInfo.setEditable(false);
        this.vmInfo.setLineWrap(true);
        this.vmInfo.setComponentPopupMenu(new TextPopup());

        this.closeButton.addActionListener(this);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent event){
                close();
            }
        });

        URL topUrl = ResourceManager.getResource(HELP_HTML);
        loadURL(topUrl);

        design();

        return;
    }

    /**
     * デザインを行う。
     */
    private void design(){
        Container content = this.getContentPane();
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        content.setLayout(layout);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;

        JScrollPane sc = new JScrollPane(this.htmlView);
        this.tabPanel.add("ヘルプ", sc);
        sc = new JScrollPane(this.vmInfo);
        this.tabPanel.add("実行環境", sc);
        content.add(this.tabPanel, constraints);

        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        content.add(new JSeparator(), constraints);

        constraints.weightx = 0.0;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        content.add(this.closeButton, constraints);

        return;
    }

    /**
     * ウィンドウを閉じる。
     */
    private void close(){
        setVisible(false);
        return;
    }

    /**
     * URLの示すHTML文書を表示する。
     * @param url URL
     */
    private void loadURL(URL url){
        if(url == null) return;

        try{
            this.htmlView.setPage(url);
        }catch(IOException e){
            LOGGER.log(Level.WARNING, "ヘルプファイルが読み込めません", e);
            assert false;
        }

        return;
    }

    /**
     * 実行環境に関する情報を更新する。
     * @param optinfo コマンドライン引数情報
     * @param configStore 設定ファイル情報
     */
    public void updateVmInfo(OptionInfo optinfo, ConfigStore configStore){
        StringBuilder info = new StringBuilder();

        info.append("起動時引数:\n");
        for(String arg : optinfo.getInvokeArgList()){
            info.append("\u0020\u0020").append(arg).append('\n');
        }
        info.append('\n');

        info.append(EnvInfo.getVMInfo());

        if(configStore.useStoreFile()){
            info.append("設定格納ディレクトリ : ")
                .append(configStore.getConfigDir().getPath());
        }else{
            info.append("※ 設定格納ディレクトリは使っていません。");
        }

        this.vmInfo.setText(info.toString());

        return;
    }

    /**
     * {@inheritDoc}
     * 閉じるボタン押下処理。
     * @param event ボタン押下イベント {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent event){
        if(event.getSource() != this.closeButton) return;
        close();
        return;
    }

    /**
     * {@inheritDoc}
     * リンククリック処理。
     * @param event リンククリックイベント {@inheritDoc}
     */
    @Override
    public void hyperlinkUpdate(HyperlinkEvent event){
        if(event.getEventType() != HyperlinkEvent.EventType.ACTIVATED){
            return;
        }

        URL url = event.getURL();
        loadURL(url);

        return;
    }

}
