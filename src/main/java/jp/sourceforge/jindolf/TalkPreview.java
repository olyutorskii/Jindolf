/*
 * 発言エディットパネル
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import jp.sourceforge.jindolf.json.JsArray;
import jp.sourceforge.jindolf.json.JsObject;
import jp.sourceforge.jindolf.json.JsString;
import jp.sourceforge.jindolf.json.JsValue;

/**
 * 発言エディットパネル。
 */
@SuppressWarnings("serial")
public class TalkPreview extends JFrame
        implements ActionListener, ChangeListener{

    private static final String FRAMETITLE = "発言エディタ - " + Jindolf.TITLE;
    private static final Color COLOR_EDITORBACK = Color.BLACK;
    private static final String DRAFT_FILE = "draft.json";

    private final JTextComponent freeMemo = new TextEditor();

    private final EditArray editArray = new EditArray();

    private final JButton cutButton      = new JButton("カット");
    private final JButton copyButton     = new JButton("コピー");
    private final JButton clearButton    = new JButton("クリア");
    private final JButton cutAllButton   = new JButton("全カット");
    private final JButton copyAllButton  = new JButton("全コピー");
    private final JButton clearAllButton = new JButton("全クリア");
    private final JButton closeButton    = new JButton("閉じる");
    private final TitledBorder numberBorder =
            BorderFactory.createTitledBorder("");
    private final JComponent singleGroup = buildSingleGroup();
    private final JComponent multiGroup  = buildMultiGroup();
    private final JLabel letsBrowser =
            new JLabel("投稿はWebブラウザからどうぞ");

    private JsObject loadedDraft = null;

    /**
     * コンストラクタ。
     */
    public TalkPreview(){
        super(FRAMETITLE);

        GUIUtils.modifyWindowAttributes(this, true, false, true);

        setDefaultCloseOperation(HIDE_ON_CLOSE);

        this.cutButton      .addActionListener(this);
        this.copyButton     .addActionListener(this);
        this.clearButton    .addActionListener(this);
        this.cutAllButton   .addActionListener(this);
        this.copyAllButton  .addActionListener(this);
        this.clearAllButton .addActionListener(this);
        this.closeButton    .addActionListener(this);

        this.editArray.addChangeListener(this);

        Container content = getContentPane();
        design(content);

        setBorderNumber(1);

        return;
    }

    /**
     * レイアウトを行う。
     * @param content コンテナ
     */
    private void design(Container content){
        JComponent freeNotePanel = buildFreeNotePanel();

        JScrollPane scrollPane  = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        JViewport viewPort = new JViewport();
        viewPort.setBackground(COLOR_EDITORBACK);
        viewPort.setView(this.editArray);
        scrollPane.setViewport(viewPort);

        LayoutManager layout;
        Border border;

        JComponent editPanel = new JPanel();
        layout = new BorderLayout();
        editPanel.setLayout(layout);
        editPanel.add(scrollPane, BorderLayout.CENTER);
        JComponent buttonPanel = buildButtonPanel();
        editPanel.add(buttonPanel, BorderLayout.EAST);
        border = BorderFactory.createTitledBorder("発言編集");
        editPanel.setBorder(border);

        JSplitPane split = new JSplitPane();
        split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        split.setContinuousLayout(false);
        split.setDividerSize(10);
        split.setDividerLocation(200);
        split.setOneTouchExpandable(true);
        split.setLeftComponent(freeNotePanel);
        split.setRightComponent(editPanel);

        Border inside  = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
        Border outside = BorderFactory.createEmptyBorder(2, 5, 2, 2);
        border = BorderFactory.createCompoundBorder(inside, outside);
        this.letsBrowser.setBorder(border);

        layout = new BorderLayout();
        content.setLayout(layout);
        content.add(split, BorderLayout.CENTER);
        content.add(this.letsBrowser, BorderLayout.SOUTH);

        return;
    }

    /**
     * ボタン群を生成する。
     * @return ボタン群
     */
    private JComponent buildButtonPanel(){
        JPanel panel = new JPanel();

        LayoutManager layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        panel.setLayout(layout);

        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 1;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;


        constraints.insets = new Insets(3, 3, 3, 3);
        panel.add(this.singleGroup, constraints);

        constraints.insets = new Insets(10, 3, 3, 3);
        panel.add(this.multiGroup, constraints);

        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.SOUTH;
        constraints.insets = new Insets(3, 3, 10, 3);
        panel.add(this.closeButton, constraints);

        return panel;
    }

    /**
     * アクティブ発言操作ボタン群を生成する。
     * @return ボタン群
     */
    private JComponent buildSingleGroup(){
        JComponent panel = new JPanel();

        LayoutManager layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        panel.setLayout(layout);

        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 3, 3, 3);

        panel.add(this.cutButton,   constraints);
        panel.add(this.copyButton,  constraints);
        panel.add(this.clearButton, constraints);

        panel.setBorder(this.numberBorder);

        return panel;
    }

    /**
     * 全発言操作ボタン群を生成する。
     * @return ボタン群
     */
    private JComponent buildMultiGroup(){
        JComponent panel = new JPanel();

        LayoutManager layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        panel.setLayout(layout);

        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 3, 3, 3);

        panel.add(this.cutAllButton,   constraints);
        panel.add(this.copyAllButton,  constraints);
        panel.add(this.clearAllButton, constraints);

        Border border = BorderFactory.createTitledBorder("全発言を");
        panel.setBorder(border);

        return panel;
    }

    /**
     * フリーノート部を生成する。
     * @return フリーノート部
     */
    private JComponent buildFreeNotePanel(){
        Insets margin = new Insets(3, 3, 3, 3);
        this.freeMemo.setMargin(margin);
        JPopupMenu popup = new TextPopup();
        this.freeMemo.setComponentPopupMenu(popup);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        JViewport viewPort = new JViewport();
        viewPort.setView(this.freeMemo);
        scrollPane.setViewport(viewPort);

        JComponent panel = new JPanel();

        LayoutManager layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        panel.setLayout(layout);

        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(1, 1, 1, 1);
        panel.add(scrollPane, constraints);

        Border border = BorderFactory.createTitledBorder("フリーメモ");
        panel.setBorder(border);

        return panel;
    }

    /**
     * アクティブ発言の通し番号表示を更新。
     * @param num 通し番号
     */
    private void setBorderNumber(int num){
        String title = "発言#"+num+" を";
        this.numberBorder.setTitle(title);
        this.singleGroup.revalidate();
        this.singleGroup.repaint();
        return;
    }

    /**
     * テキスト編集用フォントを指定する。
     * 描画属性は無視される。
     * @param fontInfo フォント設定
     */
    public void setFontInfo(FontInfo fontInfo){
        setTextFont(fontInfo.getFont());
        return;
    }

    /**
     * テキスト編集用フォントを指定する。
     * @param textFont フォント
     */
    public void setTextFont(Font textFont){
        this.freeMemo.setFont(textFont);
        this.editArray.setTextFont(textFont);
        return;
    }

    /**
     * テキスト編集用フォントを取得する。
     * @return フォント
     */
    public Font getTextFont(){
        return this.editArray.getTextFont();
    }

    /**
     * 発言クリア操作の確認ダイアログを表示する。
     * @return OKなら0, Cancelなら2
     */
    private int warnClear(){
        int result = JOptionPane.showConfirmDialog(
                this,
                "本当に発言をクリアしてもよいですか？",
                "発言クリア確認",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE );
        return result;
    }

    /**
     * 原稿のロード。
     */
    public void loadDraft(){
        JsValue value = ConfigFile.loadJson(new File(DRAFT_FILE));
        if(value == null) return;

        if( ! (value instanceof JsObject) ) return;
        JsObject root = (JsObject) value;

        value = root.getValue("freeMemo");
        if(value instanceof JsString){
            JsString memo = (JsString) value;
            this.freeMemo.setText(memo.toRawString());
        }

        value = root.getValue("drafts");
        if( ! (value instanceof JsArray) ) return;
        JsArray array = (JsArray) value;

        StringBuilder draftAll = new StringBuilder();
        for(JsValue elem : array){
            if( ! (elem instanceof JsString) ) continue;
            JsString draft = (JsString) elem;
            draftAll.append(draft.toRawString());
        }
        this.editArray.clearAllEditor();
        this.editArray.setAllText(draftAll);

        this.loadedDraft = root;

        return;
    }

    /**
     * 原稿のセーブ。
     */
    public void saveDraft(){
        AppSetting setting = Jindolf.getAppSetting();
        if( ! setting.useConfigPath() ) return;
        File configPath = setting.getConfigPath();
        if(configPath == null) return;

        JsObject root = new JsObject();
        JsString memo = new JsString(this.freeMemo.getText());
        root.putValue("freeMemo", memo);

        JsArray array = new JsArray();
        JsString text = new JsString(this.editArray.getAllText());
        array.add(text);
        root.putValue("drafts", array);

        if(this.loadedDraft != null){
            if(this.loadedDraft.equals(root)) return;
        }

        ConfigFile.saveJson(new File(DRAFT_FILE), root);

        return;
    }

    /**
     * アクティブな発言をカットしクリップボードへコピーする。
     */
    private void actionCutActive(){
        actionCopyActive();
        actionClearActive(false);
        return;
    }

    /**
     * アクティブな発言をクリップボードにコピーする。
     */
    private void actionCopyActive(){
        TalkEditor activeEditor = this.editArray.getActiveEditor();
        if(activeEditor == null) return;

        CharSequence text = activeEditor.getText();
        ClipboardAction.copyToClipboard(text);

        return;
    }

    /**
     * アクティブな発言をクリアする。
     * @param confirm trueなら確認ダイアログを出す
     */
    private void actionClearActive(boolean confirm){
        if(confirm && warnClear() != 0 ) return;

        TalkEditor activeEditor = this.editArray.getActiveEditor();
        if(activeEditor == null) return;

        activeEditor.clearText();

        return;
    }

    /**
     * 全発言をカットしクリップボードへコピーする。
     */
    private void actionCutAll(){
        actionCopyAll();
        actionClearAll(false);
        return;
    }

    /**
     * 全発言をクリップボードにコピーする。
     */
    private void actionCopyAll(){
        CharSequence text = this.editArray.getAllText();
        ClipboardAction.copyToClipboard(text);
        return;
    }

    /**
     * 全発言をクリアする。
     * @param confirm trueなら確認ダイアログを出す
     */
    private void actionClearAll(boolean confirm){
        if(confirm && warnClear() != 0 ) return;
        this.editArray.clearAllEditor();
        return;
    }

    /**
     * 上位ウィンドウをクローズする。
     */
    private void actionClose(){
        setVisible(false);
        return;
    }

    /**
     * {@inheritDoc}
     * 各種ボタン操作の処理。
     * @param event {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent event){
        Object source = event.getSource();
        if     (source == this.cutButton)      actionCutActive();
        else if(source == this.copyButton)     actionCopyActive();
        else if(source == this.clearButton)    actionClearActive(true);
        else if(source == this.cutAllButton)   actionCutAll();
        else if(source == this.copyAllButton)  actionCopyAll();
        else if(source == this.clearAllButton) actionClearAll(true);
        else if(source == this.closeButton)    actionClose();
        return;
    }

    /**
     * アクティブなエディタが変更された時の処理。
     * @param event イベント情報
     */
    public void stateChanged(ChangeEvent event){
        TalkEditor activeEditor = this.editArray.getActiveEditor();
        int seqNo = activeEditor.getSequenceNumber();
        setBorderNumber(seqNo);
        return;
    }

    // TODO アンドゥ・リドゥ機能
    // TODO バルーンの雰囲気を選択できるようにしたい。(白、赤、青、灰)
    // TODO アンカーの表記揺れの指摘
}
