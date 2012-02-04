/*
 * font monodizer
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.util;

import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Map;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

/**
 * Swingコンポーネントのフォント等幅化。
 * L&F変更にも対処。
 */
public final class Monodizer implements PropertyChangeListener{

    /** フォント変更時のプロパティ名。 */
    public static final String PROPNAME_FONT = "font";
    /** L&F変更時のプロパティ名。 */
    public static final String PROPNAME_UI = "UI";
    /** Font.MONOSPACED代替品。 */
    public static final String FAMILY_MONO = "Monospaced";

    private static final Map<TextAttribute, String> TEXTATTR_MONO =
            Collections.singletonMap(TextAttribute.FAMILY, FAMILY_MONO);

    private static final Monodizer CHANGER = new Monodizer();


    /**
     * 隠しコンストラクタ。
     */
    private Monodizer(){
        super();
        return;
    }


    /**
     * 等幅フォントか否か判定する。
     * @param font フォント
     * @return 等幅フォントならtrue
     */
    public static boolean isMonospaced(Font font){
        Map<TextAttribute, ?> attrMap = font.getAttributes();

        Object attr = attrMap.get(TextAttribute.FAMILY);
        if( ! (attr instanceof String) ) return false;

        String family = (String) attr;
        if(family.equals(FAMILY_MONO)) return true;

        return false;
    }

    /**
     * 任意のフォントの等幅化を行う。
     * 等幅以外の属性は可能な限り保持する。
     * @param font 任意のフォント
     * @return 等幅フォント
     */
    public static Font deriveMonoFont(Font font){
        Font monofont = font.deriveFont(TEXTATTR_MONO);
        return monofont;
    }

    /**
     * 任意のコンポーネントをフォント等幅化する。
     * L&F変更に対処するためのリスナ組み込みも行われる。
     * @param comp コンポーネント
     */
    public static void monodize(JComponent comp){
        Font oldFont = comp.getFont();
        Font newFont = deriveMonoFont(oldFont);
        comp.setFont(newFont);

        modifyComponent(comp);

        comp.addPropertyChangeListener(PROPNAME_FONT, CHANGER);
        comp.addPropertyChangeListener(PROPNAME_UI,   CHANGER);

        comp.revalidate();

        return;
    }

    /**
     * コンポーネントに微修正を加える。
     * @param comp コンポーネント
     */
    private static void modifyComponent(JComponent comp){
        if(comp instanceof JTextComponent){
            JTextComponent textComp = (JTextComponent) comp;
            modifyTextComponent(textComp);
        }else if(comp instanceof JComboBox){
            JComboBox combo = (JComboBox) comp;
            modifyComboBox(combo);
        }

        return;
    }

    /**
     * テキストコンポーネントへの微修正を行う。
     * @param textComp テキストコンポーネント
     */
    private static void modifyTextComponent(JTextComponent textComp){
        if(textComp.isEditable()) return;
        if(textComp.getCaret() == null) return;

        textComp.setCaretPosition(0);

        return;
    }

    /**
     * コンボボックスのエディタを等幅化する。
     * @param comboBox コンボボックス
     */
    private static void modifyComboBox(JComboBox comboBox){
        ComboBoxEditor editor = comboBox.getEditor();
        if(editor == null) return;

        Component editComp = editor.getEditorComponent();
        if( ! (editComp instanceof JTextComponent) ) return;
        JTextComponent textEditor = (JTextComponent) editComp;

        Font oldFont = textEditor.getFont();
        Font newFont = deriveMonoFont(oldFont);
        textEditor.setFont(newFont);

        modifyTextComponent(textEditor);

        return;
    }

    /**
     * フォント変更イベントの受信。
     * @param event フォント変更イベント
     */
    public void propertyChange(PropertyChangeEvent event){
        Object source = event.getSource();
        if( ! (source instanceof JComponent) ) return;
        JComponent comp = (JComponent) source;

        String propName = event.getPropertyName();

        Font newFont;
        if(PROPNAME_FONT.equals(propName)){
            Object newValue = event.getNewValue();
            if( ! (newValue instanceof Font) ) return;
            newFont = (Font) newValue;
        }else if(PROPNAME_UI.equals(propName)){
            newFont = comp.getFont();
        }else{
            return;
        }

        Font monoFont = deriveMonoFont(newFont);
        comp.setFont(monoFont);    // 再帰は起きないはず…

        modifyComponent(comp);

        return;
    }

}
