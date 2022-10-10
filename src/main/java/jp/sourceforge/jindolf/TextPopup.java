/*
 * テキストコンポーネント用ポップアップメニュー
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: TextPopup.java 953 2009-12-06 16:42:14Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * テキストコンポーネント用ポップアップメニュー。
 * 各種クリップボード操作(カット、コピー、ペースト、etc.)を備える。
 */
@SuppressWarnings("serial")
public class TextPopup extends JPopupMenu implements PropertyChangeListener{

    /** プロパティ変更イベントキー。 */
    private static final String PROPERTY_UI = "UI";

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
     * ついでにL&F変更監視機構を仕込む。
     * @param invoker {@inheritDoc}
     */
    @Override
    public void setInvoker(Component invoker){
        Component old = getInvoker();
        if(old != null){
            old.removePropertyChangeListener(this);
        }

        super.setInvoker(invoker);

        if(invoker != null){
            invoker.addPropertyChangeListener(PROPERTY_UI, this);
        }

        return;
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

    /**
     * {@inheritDoc}
     * ポップアップ呼び出し元を監視してL&Fを変更する。
     * @param event {@inheritDoc}
     */
    public void propertyChange(PropertyChangeEvent event){
        String propertyName = event.getPropertyName();
        if(PROPERTY_UI.equals(propertyName)) updateUI();
        return;
    }

    // TODO アクセス権チェックによるポップアップメニュー変更
}
