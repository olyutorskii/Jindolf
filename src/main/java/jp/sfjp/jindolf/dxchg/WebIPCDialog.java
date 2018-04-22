/*
 * Dialog for Desktop
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.dxchg;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import jp.sfjp.jindolf.VerInfo;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sfjp.jindolf.util.Monodizer;

/**
 * Webブラウザ起動用の専用ダイアログ。
 */
@SuppressWarnings("serial")
public class WebIPCDialog extends JDialog {

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private static final String CMD_BROWSE   = "browse";
    private static final String CMD_CLIPCOPY = "clipcopy";
    private static final String CMD_CANCEL   = "cancel";

    private static final String TITLE_WWW =
            VerInfo.getFrameTitle("URLへのアクセス確認");


    private final String warnMessage;

    private final JLabel info =
            new JLabel("以下のURLへのアクセスが指示されました。");
    private final JTextArea urltext =
            new JTextArea("");
    private final JButton browse =
            new JButton("デフォルトのWebブラウザで表示");
    private final JButton clipcopy =
            new JButton("URLをクリップボードにコピー");
    private final JLabel dndLabel =
            new JLabel("…またはブラウザにDrag&Drop →");
    private final JButton cancel =
            new JButton("閉じる");

    private final Desktop desktop;

    private URI uri;


    /**
     * コンストラクタ。
     * @param owner オーナーフレーム
     */
    public WebIPCDialog(Frame owner){
        super(owner);
        setModal(true);

        GUIUtils.modifyWindowAttributes(this, true, false, true);

        Desktop webipc = null;
        if(Desktop.isDesktopSupported()){
            webipc = Desktop.getDesktop();
            if( ! webipc.isSupported(Desktop.Action.BROWSE) ){
                webipc = null;
            }
        }
        this.desktop = webipc;

        if(this.desktop == null){
            this.warnMessage =
                    "何らかの理由でこの機能は利用不可になっています";
        }else{
            this.warnMessage = "";
        }

        buildDnDIcon();
        buildUrlLabel();
        buildButton();

        Container container = getContentPane();
        design(container);

        if(this.desktop == null){
            this.browse.setToolTipText(this.warnMessage);
        }

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent event){
                actionCancel();
                return;
            }
        });

        return;
    }


    /**
     * Webブラウザ起動用のモーダルダイアログを表示する。
     * @param owner オーナーフレーム
     * @param url URL文字列
     */
    public static void showDialog(Frame owner, String url){
        WebIPCDialog dialog = new WebIPCDialog(owner);

        dialog.setTitle(TITLE_WWW);
        dialog.setUrlText(url);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);

        return;
    }

    /**
     * 有効なURIか判定する。
     * @param uri URI
     * @return 有効ならtrue
     */
    private static boolean isValidURI(URI uri){
        if(uri == null) return false;

        if( ! uri.isAbsolute() ) return false;

        String scheme = uri.getScheme();
        if(scheme == null) return false;
        if(    ! scheme.equalsIgnoreCase("http")
            && ! scheme.equalsIgnoreCase("https") ) return false;

        String host = uri.getHost();
        if(host == null) return false;

        return true;
    }


    /**
     * DragAndDropアイコンを構成する。
     */
    private void buildDnDIcon(){
        this.dndLabel.setIcon(GUIUtils.getWWWIcon());
        this.dndLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        this.dndLabel.setTransferHandler(new DnDHandler());
        this.dndLabel.addMouseListener(new DragIgniter());
        return;
    }

    /**
     * URL表示部を構成する。
     */
    private void buildUrlLabel(){
        Border inside =
                BorderFactory.createEmptyBorder(1, 4, 1, 4);
        Border outside =
                BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        Border border =
                BorderFactory.createCompoundBorder(outside, inside);

        this.urltext.setBorder(border);

        this.urltext.setEditable(false);
        this.urltext.setLineWrap(true);
        this.urltext.setComponentPopupMenu(new TextPopup());

        Monodizer.monodize(this.urltext);

        return;
    }

    /**
     * ボタンを構成する。
     */
    private void buildButton(){
        this.browse  .setActionCommand(CMD_BROWSE);
        this.clipcopy.setActionCommand(CMD_CLIPCOPY);
        this.cancel  .setActionCommand(CMD_CANCEL);

        ActionListener btnListsner = new ButtonListener();
        this.browse  .addActionListener(btnListsner);
        this.clipcopy.addActionListener(btnListsner);
        this.cancel  .addActionListener(btnListsner);

        getRootPane().setDefaultButton(this.browse);
        this.browse.requestFocusInWindow();

        return;
    }

    /**
     * レイアウトを行う。
     * @param container レイアウトコンテナ
     */
    private void design(Container container){
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        container.setLayout(layout);

        JComponent buttonPanel = buildButtonPanel();

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill      = GridBagConstraints.HORIZONTAL;
        constraints.insets    = new Insets(5, 5, 5, 5);

        container.add(this.info,   constraints);
        container.add(this.urltext, constraints);
        container.add(buttonPanel,    constraints);
        container.add(this.cancel,   constraints);

        return;
    }

    /**
     * ボタンパネルを生成する。
     * @return ボタンパネル
     */
    private JComponent buildButtonPanel(){
        JPanel buttonPanel = new JPanel();

        Border border = BorderFactory.createTitledBorder(
                            "アクセスする方法を選択してください。"
                        );
        buttonPanel.setBorder(border);

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        buttonPanel.setLayout(layout);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill      = GridBagConstraints.HORIZONTAL;
        constraints.insets    = new Insets(3, 3, 3, 3);
        buttonPanel.add(this.browse,   constraints);
        buttonPanel.add(this.clipcopy, constraints);

        constraints.fill   = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets    = new Insets(10, 3, 10, 3);
        buttonPanel.add(this.dndLabel, constraints);

        return buttonPanel;
    }

    /**
     * URL文字列を設定する。
     * @param url URL文字列
     */
    public void setUrlText(String url){
        URI uriarg = null;
        try{
            uriarg = new URI(url);
        }catch(URISyntaxException e){
            // NOTHING
        }

        this.uri = uriarg;
        if(this.uri == null) return;

        if( ! isValidURI(this.uri) ) return;

        String uriText = this.uri.toASCIIString();
        this.urltext.setText(uriText);

        this.urltext.revalidate();
        pack();

        return;
    }

    /**
     * WebブラウザでURLを表示。
     */
    private void actionBrowse(){
        if(this.uri == null){
            close();
            return;
        }

        if(this.desktop == null){
            String title = "報告";
            JOptionPane.showMessageDialog(
                    this,
                    this.warnMessage, title,
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try{
            try{
                this.desktop.browse(this.uri);
            }catch(NullPointerException e){
                assert false;
            }catch(   UnsupportedOperationException
                    | IOException
                    | SecurityException
                    | IllegalArgumentException e
                    ){
                // NOTHING
            }
            String logmsg =   "URL "
                            + this.uri.toASCIIString()
                            + " へのアクセスをWebブラウザに指示しました";
            LOGGER.info(logmsg);
        }finally{
            close();
        }

        return;
    }

    /**
     * URLをクリップボードにコピーする。
     */
    private void actionClipboardCopy(){
        if(this.uri == null){
            close();
            return;
        }

        String uristring = this.uri.toASCIIString();

        try{
            ClipboardAction.copyToClipboard(uristring);
            String logmsg =  "文字列「"
                           + uristring
                           + "」をクリップボードにコピーしました";
            LOGGER.info(logmsg);
        }finally{
            close();
        }

        return;
    }

    /**
     * 何もせずダイアログを閉じる。
     */
    private void actionCancel(){
        close();
        return;
    }

    /**
     * ダイアログを閉じる。
     */
    private void close(){
        setVisible(false);
        return;
    }


    /**
     * ボタンリスナ。
     */
    private class ButtonListener implements ActionListener{

        /**
         * ボタン押下リスナ。
         * @param event ボタン押下イベント
         */
        @Override
        public void actionPerformed(ActionEvent event){
            String cmd = event.getActionCommand();
            if(CMD_BROWSE.equals(cmd)){
                actionBrowse();
            }else if(CMD_CLIPCOPY.equals(cmd)){
                actionClipboardCopy();
            }else if(CMD_CANCEL.equals(cmd)){
                actionCancel();
            }
            return;
        }

    }


    /**
     * Drag&amp;Dropの転送処理を管理。
     */
    private class DnDHandler extends TransferHandler{

        /**
         * コンストラクタ。
         */
        public DnDHandler(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         * コピー動作のみをサポートすることを伝える。
         * @param comp {@inheritDoc}
         * @return {@inheritDoc}
         */
        @Override
        public int getSourceActions(JComponent comp){
            return 0 | COPY;
        }

        /**
         * {@inheritDoc}
         * URIエクスポータを生成する。
         * URIも指定される。
         * @param comp {@inheritDoc}
         * @return {@inheritDoc}
         */
        @Override
        protected Transferable createTransferable(JComponent comp){
            UriExporter result = new UriExporter(WebIPCDialog.this.uri);
            return result;
        }

        /**
         * {@inheritDoc}
         * D&Dに成功したらダイアログを閉じる。
         * @param source {@inheritDoc}
         * @param data {@inheritDoc}
         * @param action {@inheritDoc}
         */
        @Override
        protected void exportDone(JComponent source,
                                   Transferable data,
                                   int action ){
            if(action == NONE) return;

            String logmsg =   "URL "
                            + WebIPCDialog.this.uri.toASCIIString()
                            + " がどこかへドラッグ&ドロップされました";
            LOGGER.info(logmsg);

            close();

            return;
        }

        /**
         * {@inheritDoc}
         * ※ SunのJRE1.6.0_11前後では、BugID 4816922のため決して呼ばれない。
         * @param tx {@inheritDoc}
         * @return {@inheritDoc}
         */
        @Override
        public Icon getVisualRepresentation(Transferable tx){
            return GUIUtils.getWWWIcon();
        }

    }


    /**
     * ドラッグ開始イベント処理。
     */
    private static class DragIgniter extends MouseAdapter{

        /**
         * コンストラクタ。
         */
        public DragIgniter(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         * ドラッグ開始イベント受信。
         * @param event {@inheritDoc}
         */
        @Override
        public void mousePressed(MouseEvent event){
            JComponent comp = (JComponent) event.getSource();
            TransferHandler handler = comp.getTransferHandler();
            handler.exportAsDrag(comp, event, TransferHandler.COPY);
            return;
        }

    }

}
