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
import java.text.MessageFormat;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
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

    /** logger. */
    protected static final Logger LOGGER = Logger.getAnonymousLogger();

    /** browse command. */
    protected static final String CMD_BROWSE   = "browse";
    /** clipboard copy command. */
    protected static final String CMD_CLIPCOPY = "clipcopy";
    /** cancel command. */
    protected static final String CMD_CANCEL   = "cancel";

    private static final String MSG_NODESKTOP =
            "何らかの理由でこの機能は利用不可になっています";
    private static final String MSG_DND =
            "URL {0} がどこかへドラッグ&ドロップされました";
    private static final String MSG_CLIPCOPY =
            "文字列「{0}」をクリップボードにコピーしました";
    private static final String MSG_BROWSE =
            "URL {0} へのアクセスをWebブラウザに指示しました";

    private static final String TITLE_WWW =
            VerInfo.getFrameTitle("URLへのアクセス確認");

    private static final DragIgniter DNDIGNITER = new DragIgniter();

    private static final URI URI_EMPTY;

    static{
        try{
            URI_EMPTY = new URI("");
        }catch(URISyntaxException e){
            throw new ExceptionInInitializerError(e);
        }
    }


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
     *
     * @param owner オーナーフレーム
     */
    protected WebIPCDialog(Frame owner){
        super(owner);

        this.desktop = getBrowserCntl();
        if(this.desktop == null){
            this.browse.setEnabled(false);
        }

        buildUrlLabel();
        buildEventCatcher();
        buildDnDLabel();

        Container container = getContentPane();
        design(container);

        return;
    }


    /**
     * ブラウザ制御オブジェクトを返す。
     *
     * @return ブラウザ制御オブジェクト。サポートされなければnull。
     */
    private static Desktop getBrowserCntl(){
        if( ! Desktop.isDesktopSupported()) return null;

        Desktop result = Desktop.getDesktop();
        assert result != null;

        if( ! result.isSupported(Desktop.Action.BROWSE) ) return null;

        return result;
    }

    /**
     * Webブラウザ起動用のモーダルダイアログを表示する。
     *
     * @param owner オーナーフレーム
     * @param url URL文字列
     */
    public static void showDialog(Frame owner, String url){
        WebIPCDialog dialog = createDialog(owner);

        dialog.setUrlText(url);

        dialog.pack();
        dialog.setVisible(true);

        return;
    }

    /**
     * ダイアログを生成する。
     *
     * @param owner オーナーフレーム
     * @return ダイアログ
     */
    protected static WebIPCDialog createDialog(Frame owner){
        WebIPCDialog dialog = new WebIPCDialog(owner);

        decorateDialog(dialog);
        dialog.setLocationRelativeTo(owner);

        return dialog;
    }

    /**
     * ダイアログを装飾する。
     *
     * @param dialog ダイアログ
     */
    protected static void decorateDialog(WebIPCDialog dialog){
        dialog.setResizable(true);
        dialog.setLocationByPlatform(true);

        dialog.setTitle(TITLE_WWW);

        dialog.setModalityType(ModalityType.APPLICATION_MODAL);

        return;
    }

    /**
     * 有効なURIか判定する。
     *
     * @param uri URI
     * @return 有効ならtrue
     */
    private static boolean isValidURI(URI uri){
        if(uri == null) return false;

        if( ! uri.isAbsolute() ) return false;

        String scheme = uri.getScheme();
        if(    ! "http".equalsIgnoreCase(scheme)
            && ! "https".equalsIgnoreCase(scheme) ) return false;

        String host = uri.getHost();
        if(host == null) return false;

        return true;
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
    private void buildEventCatcher(){
        this.browse  .setActionCommand(CMD_BROWSE);
        this.clipcopy.setActionCommand(CMD_CLIPCOPY);
        this.cancel  .setActionCommand(CMD_CANCEL);

        EventCatcher catcher = new EventCatcher();
        this.browse  .addActionListener(catcher);
        this.clipcopy.addActionListener(catcher);
        this.cancel  .addActionListener(catcher);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(catcher);

        JRootPane pane = getRootPane();
        pane.setDefaultButton(this.browse);
        this.browse.requestFocusInWindow();

        if(this.desktop == null){
            this.browse.setToolTipText(MSG_NODESKTOP);
        }

        return;
    }

    /**
     * DragAndDropラベルを構成する。
     */
    private void buildDnDLabel(){
        this.dndLabel.setIcon(GUIUtils.getWWWIcon());
        this.dndLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        this.dndLabel.setTransferHandler(new DnDHandler());
        this.dndLabel.addMouseListener(DNDIGNITER);
        return;
    }

    /**
     * レイアウトを行う。
     *
     * @param container レイアウトコンテナ
     */
    private void design(Container container){
        GridBagLayout layout = new GridBagLayout();
        container.setLayout(layout);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill      = GridBagConstraints.HORIZONTAL;
        constraints.insets    = new Insets(5, 5, 5, 5);

        JComponent buttonPanel = buildButtonPanel();

        container.add(this.info,    constraints);
        container.add(this.urltext, constraints);
        container.add(buttonPanel,  constraints);
        container.add(this.cancel,  constraints);

        return;
    }

    /**
     * ボタンパネルを生成する。
     *
     * @return ボタンパネル
     */
    private JComponent buildButtonPanel(){
        JPanel buttonPanel = new JPanel();

        Border border = BorderFactory.createTitledBorder(
                            "アクセスする方法を選択してください。"
                        );
        buttonPanel.setBorder(border);

        GridBagLayout layout = new GridBagLayout();
        buttonPanel.setLayout(layout);

        GridBagConstraints constraints = new GridBagConstraints();

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
     *
     * @param url URL文字列
     */
    public void setUrlText(String url){
        URI argUri;
        try{
            argUri = new URI(url);
        }catch(URISyntaxException e){
            argUri = null;
        }

        setUri(argUri);

        return;
    }

    /**
     * URIを設定する。
     *
     * @param uriArg URI
     */
    public void setUri(URI uriArg){
        if(isValidURI(uriArg)){
            this.uri = uriArg;
        }else{
            this.uri = URI_EMPTY;
        }

        String uriText = this.uri.toASCIIString();
        this.urltext.setText(uriText);

        this.urltext.revalidate();
        pack();

        return;
    }

    /**
     * URIを返す。
     *
     * @return URI
     */
    public URI getURI(){
        return this.uri;
    }

    /**
     * WebブラウザでURLを表示。
     *
     * <p>ダイアログは必ず閉じられる。
     */
    protected void actionBrowse(){
        try{
            actionBrowseImpl();
        }finally{
            closeDialog();
        }
    }

    /**
     * WebブラウザでURLを表示。
     */
    private void actionBrowseImpl(){
        if( ! isValidURI(this.uri) ){
            return;
        }

        if(this.desktop == null){
            String title = "報告";
            JOptionPane.showMessageDialog(
                    this,
                    MSG_NODESKTOP, title,
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try{
            this.desktop.browse(this.uri);
        }catch(IOException e){
            return;
        }

        String uriText = this.uri.toASCIIString();
        String logMessage =
                MessageFormat.format(MSG_BROWSE, uriText);
        LOGGER.info(logMessage);

        return;
    }

    /**
     * URLをクリップボードにコピーする。
     */
    protected void actionClipboardCopy(){
        if( ! isValidURI(this.uri) ){
            closeDialog();
            return;
        }

        String uriText = this.uri.toASCIIString();

        try{
            ClipboardAction.copyToClipboard(uriText);
            String logMessage =
                    MessageFormat.format(MSG_CLIPCOPY, uriText);
            LOGGER.info(logMessage);
        }finally{
            closeDialog();
        }

        return;
    }

    /**
     * 何もせずダイアログを閉じる。
     */
    protected void actionCancel(){
        closeDialog();
        return;
    }

    /**
     * ダイアログを閉じる。
     */
    protected void closeDialog(){
        setVisible(false);
        dispose();
        return;
    }


    /**
     * イベント受信リスナ。
     */
    private class EventCatcher
            extends WindowAdapter
            implements ActionListener {

        /**
         * コンストラクタ。
         */
        EventCatcher(){
            super();
            return;
        }

        /**
         * ボタン押下受信。
         *
         * <p>{@inheritDoc}
         *
         * @param event {@inheritDoc}
         */
        @Override
        public void actionPerformed(ActionEvent event){
            String cmd = event.getActionCommand();
            if(cmd == null) return;

            switch(cmd){
            case CMD_BROWSE:
                actionBrowse();
                break;
            case CMD_CLIPCOPY:
                actionClipboardCopy();
                break;
            case CMD_CANCEL:
                actionCancel();
                break;
            default:
                break;
            }

            return;
        }

        /**
         * ウィンドウクローズ受信。
         *
         * <p>{@inheritDoc}
         *
         * @param event {@inheritDoc}
         */
        @Override
        public void windowClosing(WindowEvent event){
            actionCancel();
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
        DnDHandler(){
            super();
            return;
        }

        /**
         * コピー動作のみをサポートすることを伝える。
         *
         * <p>{@inheritDoc}
         *
         * @param comp {@inheritDoc}
         * @return {@inheritDoc}
         */
        @Override
        public int getSourceActions(JComponent comp){
            return 0 | COPY;
        }

        /**
         * URIエクスポータを生成する。
         *
         * <p>URIも指定される。
         *
         * <p>{@inheritDoc}
         *
         * @param comp {@inheritDoc}
         * @return {@inheritDoc}
         */
        @Override
        protected Transferable createTransferable(JComponent comp){
            UriExporter result = new UriExporter(getURI());
            return result;
        }

        /**
         * D&amp;Dに成功したらダイアログを閉じる。
         *
         * <p>{@inheritDoc}
         *
         * @param source {@inheritDoc}
         * @param data {@inheritDoc}
         * @param action {@inheritDoc}
         */
        @Override
        protected void exportDone(JComponent source,
                                  Transferable data,
                                  int action ){
            if(action == NONE) return;

            String uriAscii = getURI().toASCIIString();
            String logmsg = MessageFormat.format(MSG_DND, uriAscii);
            LOGGER.info(logmsg);

            closeDialog();

            return;
        }

    }


    /**
     * ドラッグ開始イベント処理。
     */
    private static class DragIgniter extends MouseAdapter{

        /**
         * コンストラクタ。
         */
        DragIgniter(){
            super();
            return;
        }

        /**
         * ドラッグ開始イベント受信。
         *
         * <p>{@inheritDoc}
         *
         * @param event {@inheritDoc}
         */
        @Override
        public void mousePressed(MouseEvent event){
            Object source = event.getSource();
            assert source instanceof JComponent;
            JComponent comp = (JComponent) source;

            TransferHandler handler = comp.getTransferHandler();
            handler.exportAsDrag(comp, event, TransferHandler.COPY);

            return;
        }

    }

}
