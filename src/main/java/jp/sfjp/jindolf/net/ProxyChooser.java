/*
 * proxy chooser
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.net;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.InetSocketAddress;
import java.net.Proxy;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import jp.sfjp.jindolf.dxchg.TextPopup;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sfjp.jindolf.util.Monodizer;

/**
 * プロクシサーバ選択画面。
 * @see <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC2616</a>
 * @see <a href="http://www.ietf.org/rfc/rfc1928.txt">RFC1928</a>
 */
@SuppressWarnings("serial")
public class ProxyChooser extends JPanel implements ItemListener{

    private final JRadioButton isDirect =
            new JRadioButton("直接接続");
    private final JRadioButton isHttp =
            new JRadioButton("HTTP-Proxy (RFC2616)");
    private final JRadioButton isSocks =
            new JRadioButton("SOCKS (RFC1928)");
    private final ButtonGroup buttonGroup = new ButtonGroup();

    private final JTextField hostname = new JTextField();
    private final JComboBox port = new JComboBox();
    private final JComponent serverInfo = buildServerPanel();


    /**
     * コンストラクタ。
     */
    public ProxyChooser(){
        this(ProxyInfo.DEFAULT);
        return;
    }

    /**
     * コンストラクタ。
     * @param proxyInfo プロクシ設定
     */
    public ProxyChooser(ProxyInfo proxyInfo){
        super();

        this.buttonGroup.add(this.isDirect);
        this.buttonGroup.add(this.isHttp);
        this.buttonGroup.add(this.isSocks);
        this.isDirect.addItemListener(this);
        this.isHttp  .addItemListener(this);
        this.isSocks .addItemListener(this);

        this.hostname.setComponentPopupMenu(new TextPopup());
        Monodizer.monodize(this.hostname);
        GUIUtils.addMargin(this.hostname, 1, 4, 1, 4);

        this.port.setModel(buildPortRecommender());
        this.port.setEditable(true);
        ComboBoxEditor editor = this.port.getEditor();
        Component comp = editor.getEditorComponent();
        GUIUtils.addMargin(comp, 1, 4, 1, 4);
        if(comp instanceof JComponent){
            ((JComponent)comp).setComponentPopupMenu(new TextPopup());
        }
        Monodizer.monodize(this.port);

        design(this);

        setProxyInfo(proxyInfo);

        return;
    }


    /**
     * ポート番号選択肢を生成する。
     * @return ポート番号選択肢
     */
    private static ComboBoxModel buildPortRecommender(){
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("80");
        model.addElement("1080");
        model.addElement("3128");
        model.addElement("8000");
        model.addElement("8080");
        model.addElement("10080");
        return model;
    }

    /**
     * レイアウトを行う。
     * @param content コンテナ
     */
    private void design(Container content){
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        content.setLayout(layout);

        constraints.insets = new Insets(2, 2, 2, 2);

        constraints.weightx = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        content.add(this.isDirect, constraints);
        content.add(this.isHttp, constraints);
        content.add(this.isSocks, constraints);
        content.add(this.serverInfo, constraints);

        constraints.weighty = 1.0;
        content.add(new JPanel(), constraints);

        return;
    }

    /**
     * サーバ情報パネルを生成する。
     * @return サーバ情報パネル
     */
    private JComponent buildServerPanel(){
        JPanel panel = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        panel.setLayout(layout);

        constraints.insets = new Insets(2, 2, 2, 2);

        constraints.weightx = 0.0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTHEAST;
        panel.add(new JLabel("アドレス:"), constraints);

        constraints.weightx = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        panel.add(this.hostname, constraints);

        constraints.weightx = 0.0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTHEAST;
        panel.add(new JLabel("ポート:"), constraints);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        panel.add(this.port, constraints);

        String warn =
                "<html>※ このプロクシサーバは本当に信頼できますか？<br>"
                + "あなたが人狼BBSにログインしている間、<br>"
                + "あなたのパスワードは平文状態のまま<br>"
                + "このプロクシサーバ上を何度も通過します。</html>";
        panel.add(new JLabel(warn), constraints);

        Border border = BorderFactory.createTitledBorder("プロクシサーバ情報");
        panel.setBorder(border);

        return panel;
    }

    /**
     * プロクシの種別を返す。
     * @return プロクシ種別
     */
    protected Proxy.Type getType(){
        if(this.isDirect.isSelected()) return Proxy.Type.DIRECT;
        if(this.isHttp  .isSelected()) return Proxy.Type.HTTP;
        if(this.isSocks .isSelected()) return Proxy.Type.SOCKS;

        return Proxy.Type.DIRECT;
    }

    /**
     * サーバ名を返す。
     * プロクシのホスト名として妥当なものか否かは検証されない。
     * @return サーバ名
     */
    protected String getHostName(){
        String hostText = this.hostname.getText();
        hostText = hostText.trim();
        return hostText;
    }

    /**
     * ポート番号を返す。
     * 番号の体をなしていなければゼロを返す。
     * @return ポート番号
     */
    protected int getPort(){
        Object portItem = this.port.getEditor().getItem();
        String portText = portItem.toString();
        portText = portText.trim();

        int result;
        try{
            result = Integer.parseInt(portText);
        }catch(NumberFormatException e){
            return 0;
        }

        if(result < 0) result = 0;
        if(65535 < result) result = 65535;

        return result;
    }

    /**
     * サーバへのソケットアドレスを生成する。
     * @return ソケットアドレス
     */
    protected InetSocketAddress getInetSocketAddress(){
        return InetSocketAddress.createUnresolved(getHostName(), getPort());
    }

    /**
     * プロクシ設定を返す。
     * @return プロクシ設定
     */
    public ProxyInfo getProxyInfo(){
        Proxy.Type type = getType();
        return new ProxyInfo(type, getInetSocketAddress());
    }

    /**
     * プロクシ設定を設定する。
     * UIに反映される。
     * @param proxyInfo プロクシ設定。nullなら直接接続と解釈される。
     */
    public final void setProxyInfo(ProxyInfo proxyInfo){
        Proxy.Type type;
        InetSocketAddress addr;
        if(proxyInfo == null){
            type = Proxy.Type.DIRECT;
            addr = ProxyInfo.IP4SOCKET_NOBODY;
        }else{
            type = proxyInfo.getType();
            addr = proxyInfo.address();
        }

        ButtonModel model;
        switch(type){
        case DIRECT: model = this.isDirect.getModel(); break;
        case HTTP:   model = this.isHttp  .getModel(); break;
        case SOCKS:  model = this.isSocks .getModel(); break;
        default:     model = this.isDirect.getModel(); break;
        }
        this.buttonGroup.setSelected(model, true);

        this.hostname.setText(addr.getHostName());
        this.port.getEditor()
                 .setItem(Integer.valueOf(addr.getPort()));

        return;
    }

    /**
     * プロクシ種別ボタン操作の受信。
     * @param event ボタン操作イベント
     */
    @Override
    public void itemStateChanged(ItemEvent event){
        Object source = event.getSource();

        Proxy.Type type;
        if     (source == this.isDirect) type = Proxy.Type.DIRECT;
        else if(source == this.isHttp)   type = Proxy.Type.HTTP;
        else if(source == this.isSocks)  type = Proxy.Type.SOCKS;
        else                             return;

        if(type == Proxy.Type.DIRECT){
            this.hostname.setEnabled(false);
            this.port    .setEnabled(false);
        }else{
            this.hostname.setEnabled(true);
            this.port    .setEnabled(true);
        }

        return;
    }

}
