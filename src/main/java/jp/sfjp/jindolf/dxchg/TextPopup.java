/*
 * テキストコンポーネント用ポップアップメニュー
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.dxchg;

import java.awt.Component;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * テキストコンポーネント用ポップアップメニュー。
 * 各種クリップボード操作(カット、コピー、ペースト、etc.)を備える。
 */
@SuppressWarnings("serial")
public class TextPopup extends JPopupMenu {

    private final Action cutAction    = ClipboardAction.cutAction();
    private final Action copyAction   = ClipboardAction.copyAction();
    private final Action pasteAction  = ClipboardAction.pasteAction();
    private final Action selallAction = ClipboardAction.selectallAction();

    /**
     * コンストラクタ。
     */
    public TextPopup(){
        super();

        buildMenu();

        return;
    }

    /**
     * メニューの構成を作る。
     */
    private void buildMenu(){
        add(this.cutAction);
        add(this.copyAction);
        add(this.pasteAction);

        addSeparator();

        add(this.selallAction);

        return;
    }

    /**
     * テキストコンポーネントに選択中文字列があるか判定する。
     * @param textComp テキストコンポーネント
     * @return 選択中文字列があればtrue
     */
    protected boolean hasSelectedContent(JTextComponent textComp){
        int selStart = textComp.getSelectionStart();
        int selEnd   = textComp.getSelectionEnd();

        boolean result;
        if(selStart == selEnd) result = false;
        else                   result = true;

        return result;
    }

    /**
     * テキストコンポーネントに文字列があるか判定する。
     * @param textComp テキストコンポーネント
     * @return 文字列があればtrue
     */
    protected boolean hasContent(JTextComponent textComp){
        Document document = textComp.getDocument();
        int docLength = document.getLength();
        if(docLength <= 0) return false;
        return true;
    }

    /**
     * {@inheritDoc}
     * 文字列選択状況によって一部のポップアップメニューをマスクする。
     * @param invoker {@inheritDoc}
     * @param x {@inheritDoc}
     * @param y {@inheritDoc}
     */
    @Override
    public void show(Component invoker, int x, int y){
        if( ! (invoker instanceof JTextComponent) ) return;
        JTextComponent textComp = (JTextComponent) invoker;

        boolean textSelected = hasSelectedContent(textComp);
        this.cutAction .setEnabled(textSelected);
        this.copyAction.setEnabled(textSelected);

        if( ! textComp.isEditable() ){
            this.cutAction   .setEnabled(false);
            this.pasteAction .setEnabled(false);
        }

        if(hasContent(textComp)){
            this.selallAction.setEnabled(true);
        }else{
            this.selallAction.setEnabled(false);
        }

        super.show(invoker, x, y);

        return;
    }

    // TODO アクセス権チェックによるポップアップメニュー変更
}
