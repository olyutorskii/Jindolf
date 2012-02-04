/*
 * クリップボード操作用Action
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.dxchg;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

/**
 * テキストコンポーネント-クリップボード間操作用にカスタム化したAction。
 */
@SuppressWarnings("serial")
public class ClipboardAction extends TextAction{

    /** アクション{@value}。 */
    public static final String ACTION_CUT    = "ACTION_CUT";
    /** アクション{@value}。 */
    public static final String ACTION_COPY   = "ACTION_COPY";
    /** アクション{@value}。 */
    public static final String ACTION_PASTE  = "ACTION_PASTE";
    /** アクション{@value}。 */
    public static final String ACTION_SELALL = "ACTION_SELALL";


    /**
     * コンストラクタ。
     * @param name ポップアップメニュー名
     * @param command アクションコマンド名
     */
    protected ClipboardAction(String name, String command){
        super(name);
        setActionCommand(command);
        return;
    }


    /**
     * 文字列をクリップボードにコピーする。
     * @param data 文字列
     */
    public static void copyToClipboard(CharSequence data){
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        StringSelection selection = new StringSelection(data.toString());
        clipboard.setContents(selection, selection);
        return;
    }

    /**
     * カット用Actionの生成。
     * @return カット用Action
     */
    public static ClipboardAction cutAction(){
        return new ClipboardAction("選択範囲をカット", ACTION_CUT);
    }

    /**
     * コピー用Actionの生成。
     * @return コピー用Action
     */
    public static ClipboardAction copyAction(){
        return new ClipboardAction("選択範囲をコピー", ACTION_COPY);
    }

    /**
     * ペースト用Actionの生成。
     * @return ペースト用Action
     */
    public static ClipboardAction pasteAction(){
        return new ClipboardAction("ペースト", ACTION_PASTE);
    }

    /**
     * 全選択用Actionの生成。
     * @return 全選択用Action
     */
    public static ClipboardAction selectallAction(){
        return new ClipboardAction("すべて選択", ACTION_SELALL);
    }

    /**
     * アクションコマンド名を設定する。
     * @param actionCommand アクションコマンド名
     */
    private void setActionCommand(String actionCommand){
        putValue(Action.ACTION_COMMAND_KEY, actionCommand);
        return;
    }

    /**
     * アクションコマンド名を取得する。
     * @return アクションコマンド名
     */
    protected String getActionCommand(){
        Object value = getValue(Action.ACTION_COMMAND_KEY);
        if( ! (value instanceof String) ) return null;

        String command = (String) value;

        return command;
    }

    /**
     * {@inheritDoc}
     * アクションの受信によってクリップボード操作を行う。
     * @param event {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent event){
        JTextComponent textComp = getTextComponent(event);
        if(textComp == null) return;

        String command = getActionCommand();

        if     (ACTION_CUT   .equals(command)) textComp.cut();
        else if(ACTION_COPY  .equals(command)) textComp.copy();
        else if(ACTION_PASTE .equals(command)) textComp.paste();
        else if(ACTION_SELALL.equals(command)) textComp.selectAll();

        return;
    }

    // TODO 文字列以外の物をペーストしたときに無視したい。
}
