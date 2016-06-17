/*
 * font select list
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.glyph;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * フォント選択リスト。
 * <p>フォント一覧の遅延読み込みに対応。
 */
@SuppressWarnings("serial")
public class FontSelectList extends JList<String>
        implements ListDataListener {

    private String selectedFamily = null;


    /**
     * コンストラクタ。
     */
    public FontSelectList(){
        super();

        ListModel<String> fontListModel = new FontListModel();
        setModelImpl(fontListModel);
        setVisibleRowCount(-1);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        return;
    }

    /**
     * {@link setModel(ListModel)} の下請けメソッド。
     * 与えられたモデルの更新は監視対象となる。
     * @param model リストモデル
     */
    private void setModelImpl(ListModel<String> model){
        ListModel<String> oldModel = getModel();
        if(oldModel != null){
            oldModel.removeListDataListener(this);
        }

        model.addListDataListener(this);

        super.setModel(model);

        return;
    }

    /**
     * {@inheritDoc}
     * 与えられたモデルの更新は監視対象となる。
     * @param model {@inheritDoc}
     */
    @Override
    public void setModel(ListModel<String> model){
        setModelImpl(model);
        return;
    }

    /**
     * 指定したフォントファミリ名が選択された状態にする。
     * @param family フォントファミリ名
     */
    public void setSelectedFamily(String family){
        this.selectedFamily = family;
        reSelectFamily();
        return;
    }

    /**
     * 選択されたファミリ名を返す。
     * @return 選択されたファミリ名。何も選択されていなければnull
     */
    public String getSelectedFamily(){
        Object selected = getSelectedValue();
        if(selected == null) return null;
        String result = selected.toString();
        return result;
    }

    /**
     * 過去に指示された選択ファミリを用いて再選択操作を試みる。
     */
    private void reSelectFamily(){
        boolean shouldScroll = true;
        setSelectedValue(this.selectedFamily, shouldScroll);
        return;
    }

    /**
     * {@inheritDoc}
     * データモデル変更に伴い再選択処理を行う。
     * @param event {@inheritDoc}
     */
    @Override
    public void contentsChanged(ListDataEvent event){
        reSelectFamily();
        return;
    }

    /**
     * {@inheritDoc}
     * データモデル変更に伴い再選択処理を行う。
     * @param event {@inheritDoc}
     */
    @Override
    public void intervalAdded(ListDataEvent event){
        reSelectFamily();
        return;
    }

    /**
     * {@inheritDoc}
     * データモデル変更に伴い再選択処理を行う。
     * @param event {@inheritDoc}
     */
    @Override
    public void intervalRemoved(ListDataEvent event){
        reSelectFamily();
        return;
    }

}
