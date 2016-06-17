/*
 * font list model
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.glyph;

import java.awt.EventQueue;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 * フォントファミリ名一覧表示用リストのデータモデル。
 * <p>環境によってはフォントリストを完成させるのに
 * 数千msかかる場合があるので、その対策として非同期に一覧を読み込む。
 * <p>実際のリスト作成はEDTにより行われ、
 * リスト完成の暁にはEDTによりリスナに通知される。
 * 一般的なリストモデルと同様、
 * 基本的にスレッド間競合の問題はEDTで解決すること。
 */
@SuppressWarnings("serial")
public class FontListModel extends AbstractListModel<String> {

    private static final FontEnv DEFAULT_FONTENV = FontEnv.DEFAULT;

    private final List<String> familyList = new LinkedList<>();

    /**
     * コンストラクタ。
     * <p>リスト埋めタスクがEDTで走り始める。
     */
    public FontListModel(){
        super();

        // スレッド間競合を避けるため、ここより先の処理はEDT任せ。
        EventQueue.invokeLater(new Runnable(){
            /** {@inheritDoc} */
            @Override
            public void run(){
                List<String> fontList = DEFAULT_FONTENV.getFontFamilyList();
                FontListModel model = FontListModel.this;
                model.familyList.addAll(fontList);
                int size = model.familyList.size();
                if(size <= 0) return;

                int begin = 0;
                int end   = size - 1;
                fireContentsChanged(this, begin, end);

                return;
            }
        });

        return;
    }

    /**
     * {@inheritDoc}
     * @param index {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String getElementAt(int index){
        String result = this.familyList.get(index);
        return result;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getSize(){
        int result = this.familyList.size();
        return result;
    }

}
