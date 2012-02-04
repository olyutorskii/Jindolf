/*
 * Log frame
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.log;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.logging.Handler;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import jp.sfjp.jindolf.dxchg.TextPopup;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sfjp.jindolf.util.Monodizer;

/**
 * ログ表示パネル。
 */
@SuppressWarnings("serial")
public class LogFrame extends JDialog
        implements WindowListener, ActionListener, DocumentListener{

    private static final Document DOC_EMPTY = new PlainDocument();


    private final JTextArea textarea;
    private final Document document = new PlainDocument();
    private final JScrollPane scrollPane;
    private final JScrollBar vertical;
    private final Handler handler;

    private final JButton clearButton = new JButton("クリア");
    private final JButton closeButton = new JButton("閉じる");

    /**
     * ログ表示パネルの生成。
     * @param owner フレームオーナー
     */
    public LogFrame(Frame owner){
        super(owner);
        setModal(false);

        if(LogUtils.hasLoggingPermission()){
            this.handler = new SwingDocHandler(this.document);
        }else{
            this.handler = null;
        }

        GUIUtils.modifyWindowAttributes(this, true, false, true);

        this.textarea = new JTextArea();
        this.textarea.setEditable(false);
        this.textarea.setLineWrap(true);
        this.textarea.setDocument(DOC_EMPTY);
        Border border = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        this.textarea.setBorder(border);
        Monodizer.monodize(this.textarea);
        JPopupMenu popup = new TextPopup();
        this.textarea.setComponentPopupMenu(popup);

        this.scrollPane = new JScrollPane(this.textarea);
        this.vertical = this.scrollPane.getVerticalScrollBar();

        design();

        this.clearButton.addActionListener(this);
        this.closeButton.addActionListener(this);

        this.document.addDocumentListener(this);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        return;
    }

    /**
     * デザインを行う。
     */
    private void design(){
        Container content = getContentPane();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        content.setLayout(layout);

        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = GridBagConstraints.REMAINDER;

        content.add(this.scrollPane, constraints);

        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        content.add(new JSeparator(), constraints);

        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = 1;
        content.add(this.clearButton, constraints);

        constraints.weightx = 0.0;
        constraints.anchor = GridBagConstraints.EAST;
        content.add(this.closeButton, constraints);

        return;
    }

    /**
     * ロギングハンドラを取得する。
     * @return ロギングハンドラ
     */
    public Handler getHandler(){
        return this.handler;
    }

    /**
     * 垂直スクロールバーを末端に設定する。
     */
    private void showLastPos(){
        if( ! isVisible() ) return;
        if(this.textarea.getDocument() != this.document) return;

        EventQueue.invokeLater(new Runnable(){
            public void run(){
                LogFrame.this.vertical.setValue(Integer.MAX_VALUE);
                return;
            }
        });

        return;
    }

    /**
     * ウィンドウクローズ処理。
     */
    private void close(){
        setVisible(false);
        return;
    }

    /**
     * ログクリア処理。
     */
    private void clear(){
        try{
            this.document.remove(0, this.document.getLength());
        }catch(BadLocationException e){
            assert false;
        }
        return;
    }

    /**
     * {@inheritDoc}
     * ウィンドウの表示・非表示を設定する。
     * @param visible trueなら表示 {@inheritDoc}
     */
    @Override
    public void setVisible(boolean visible){
        super.setVisible(visible);

        if(visible){
            this.textarea.setDocument(this.document);
            showLastPos();
        }else{
            this.textarea.setDocument(DOC_EMPTY);
        }

        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowActivated(WindowEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowDeactivated(WindowEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowIconified(WindowEvent event){
        this.textarea.setDocument(DOC_EMPTY);
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowDeiconified(WindowEvent event){
        this.textarea.setDocument(this.document);
        showLastPos();
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowOpened(WindowEvent event){
        this.textarea.setDocument(this.document);
        showLastPos();
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowClosed(WindowEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowClosing(WindowEvent event){
        close();
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent event){
        Object source = event.getSource();
        if(source == this.clearButton){
            clear();
        }else if(source == this.closeButton){
            close();
        }

        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void changedUpdate(DocumentEvent event){
        showLastPos();
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void insertUpdate(DocumentEvent event){
        showLastPos();
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void removeUpdate(DocumentEvent event){
        showLastPos();
        return;
    }

}
