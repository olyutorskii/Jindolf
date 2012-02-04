/*
 * Dialog for WebIPC
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.dxchg;

import java.awt.Container;
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
import jp.sfjp.jindolf.JreChecker;
import jp.sfjp.jindolf.VerInfo;
import jp.sfjp.jindolf.log.LogWrapper;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sfjp.jindolf.util.Monodizer;

/**
 * Webブラウザ起動用の専用ダイアログ。
 */
@SuppressWarnings("serial")
public class WebIPCDialog
        extends JDialog
        implements ActionListener {

    private static final String TITLE_WWW =
            VerInfo.getFrameTitle("URLへのアクセス確認");


    private static final LogWrapper LOGGER = new LogWrapper();


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

    private final WebIPC ipc;

    private URI uri;


    /**
     * コンストラクタ。
     * @param owner オーナーフレーム
     */
    public WebIPCDialog(Frame owner){
        super(owner);
        setModal(true);

        GUIUtils.modifyWindowAttributes(this, true, false, true);

        WebIPC webipc = null;
        if(WebIPC.isDesktopSupported()){
            webipc = WebIPC.getWebIPC();
            if( ! webipc.isSupported(WebIPC.Action.BROWSE) ){
                webipc = null;
            }
        }
        this.ipc = webipc;

        if(this.ipc == null){
            if( ! JreChecker.has16Runtime() ){
                this.warnMessage =
                        "この機能を利用するには、JRE1.6以上が必要です";
            }else{
                this.warnMessage =
                        "何らかの理由でこの機能は利用不可になっています";
            }
        }else{
            this.warnMessage = "";
        }

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

        this.dndLabel.setIcon(GUIUtils.getWWWIcon());
        this.dndLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        this.dndLabel.setTransferHandler(new DnDHandler());
        this.dndLabel.addMouseListener(new DragIgniter());

        Container container = getContentPane();
        design(container);

        this.browse  .addActionListener(this);
        this.clipcopy.addActionListener(this);
        this.cancel  .addActionListener(this);

        getRootPane().setDefaultButton(this.browse);
        this.browse.requestFocusInWindow();

        if(this.ipc == null){
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
        if(   ! scheme.equalsIgnoreCase("http")
           && ! scheme.equalsIgnoreCase("https") ) return false;

        String host = uri.getHost();
        if(host == null) return false;

        return true;
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
     * ボタン押下リスナ。
     * @param event ボタン押下イベント
     */
    public void actionPerformed(ActionEvent event){
        Object source = event.getSource();
        if(source == this.browse){
            actionBrowse();
        }else if(source == this.clipcopy){
            actionClipboardCopy();
        }else if(source == this.cancel){
            actionCancel();
        }
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

        if(this.ipc == null){
            String title;
            if( ! JreChecker.has16Runtime() ){
                title = "新しいJavaを入手しましょう";
            }else{
                title = "報告";
            }
            JOptionPane.showMessageDialog(
                    this,
                    this.warnMessage, title,
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try{
            try{
                this.ipc.browse(this.uri);
            }catch(NullPointerException e){
                assert false;
            }catch(UnsupportedOperationException e){
                // NOTHING
            }catch(IOException e){
                // NOTHING
            }catch(SecurityException e){
                // NOTHING
            }catch(IllegalArgumentException e){
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
     * Drag&Dropの転送処理を管理。
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
            JComponent comp = (JComponent)event.getSource();
            TransferHandler handler = comp.getTransferHandler();
            handler.exportAsDrag(comp, event, TransferHandler.COPY);
            return;
        }

    }

}
