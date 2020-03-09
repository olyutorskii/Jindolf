/*
 * period viewer with tab access
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import jp.sfjp.jindolf.data.DialogPref;
import jp.sfjp.jindolf.data.Period;
import jp.sfjp.jindolf.data.Village;
import jp.sfjp.jindolf.glyph.AnchorHitListener;
import jp.sfjp.jindolf.glyph.Discussion;
import jp.sfjp.jindolf.glyph.FontInfo;

/**
 * タブを用いて村情報と各Periodを切り替え表示するためのコンポーネント。
 *
 * <p>村情報タブのビューはVillageInfoPanel、
 * PeriodタブのビューはPeriodViewが担当する。
 *
 * <p>PeriodViewの描画下請Discussionへのアクセス、
 * およびフォント管理、会話描画設定を提供する。
 */
@SuppressWarnings("serial")
public final class TabBrowser extends JTabbedPane{

    private static final int PERIODTAB_OFFSET = 1;


    private Village village;

    private final VillageInfoPanel villageInfo = new VillageInfoPanel();

    private FontInfo fontInfo;
    private DialogPref dialogPref;


    /**
     * コンストラクタ。
     *
     * <p>村が指定されていない状態のタブパネルを生成する。
     */
    public TabBrowser(){
        super();

        setTabPlacement(SwingConstants.TOP);
        // Mac Aqua L&F ignore WRAP_TAB_LAYOUT
        setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

        JComponent infoPane = decorateVillageInfo();
        addTab("村情報", infoPane);

        initTab();

        return;
    }


    /**
     * 村情報表示コンポーネントを装飾する。
     *
     * @return 装飾済みコンポーネント
     */
    private JComponent decorateVillageInfo(){
        Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        this.villageInfo.setBorder(border);
        JScrollPane result = new JScrollPane(this.villageInfo);
        return result;
    }

    /**
     * タブ初期化。
     *
     * <p>Periodタブは全て消え村情報タブのみになる。
     */
    private void initTab(){
        modifyTabCount(0);

        updateVillageInfo();
        showVillageInfoTab();

        repaint();
        revalidate();

        return;
    }

    /**
     * 指定した数のPeriodが収まるよう必要十分なタブ数を用意する。
     *
     * @param periods Periodの数(エピローグを含む)
     */
    private void modifyTabCount(int periods){
        for(;;){   // 短ければタブ追加
            int periodTabs = getTabCount() - PERIODTAB_OFFSET;
            if(periods <= periodTabs) break;
            String tabTitle = "";
            Component dummy = new JPanel();
            addTab(tabTitle, dummy);
        }

        for(;;){   // 長ければ余分なタブ削除
            int periodTabs = getTabCount() - PERIODTAB_OFFSET;
            if(periods >= periodTabs) break;
            int lastTabIndex = getTabCount() - 1;
            remove(lastTabIndex);
        }

        return;
    }

    /**
     * Period表示するタブ全てのコンポーネント本体とタイトルを埋める。
     */
    private void fillPeriodTab(){
        this.village.getPeriodList().stream().forEachOrdered(period ->{
            PeriodView periodView = buildPeriodView(period);
            String caption = period.getCaption();

            int tabIndex = period.getDay() + PERIODTAB_OFFSET;

            setComponentAt(tabIndex, periodView);
            setTitleAt(tabIndex, caption);
        });

        return;
    }

    /**
     * 設定された村を返す。
     *
     * @return 設定された村
     */
    public Village getVillage(){
        return this.village;
    }

    /**
     * 新規に村を設定する。
     *
     * <p>村のPeriod数に応じてタブの数は変化する。
     *
     * @param village 新しい村
     */
    public final void setVillage(Village village){
        Village oldVillage = this.village;
        if(oldVillage != null && village != oldVillage){
            oldVillage.unloadPeriods();
        }

        this.village = village;
        if(this.village == null){
            initTab();
            return;
        }

        if(this.village != oldVillage){
            showVillageInfoTab();
        }

        updateVillageInfo();

        int periodNum = this.village.getPeriodSize();
        modifyTabCount(periodNum);
        fillPeriodTab();

        repaint();
        revalidate();

        return;
    }

    /**
     * 村情報閲覧用のコンポーネントを更新する。
     */
    private void updateVillageInfo(){
        Village target = getVillage();
        this.villageInfo.updateVillage(target);
        return;
    }

    /**
     * PeriodViewインスタンスを生成する。
     *
     * <p>フォント設定、会話表示設定、各種リスナの設定が行われる。
     *
     * @param period Period
     * @return PeriodViewインスタンス
     */
    private PeriodView buildPeriodView(Period period){
        Objects.nonNull(period);

        PeriodView result;

        result = new PeriodView(period);
        result.setFontInfo(this.fontInfo);
        result.setDialogPref(this.dialogPref);

        Discussion discussion = result.getDiscussion();
        for(ActionListener listener : getActionListeners()){
            discussion.addActionListener(listener);
        }
        for(AnchorHitListener listener : getAnchorHitListeners()){
            discussion.addAnchorHitListener(listener);
        }

        return result;
    }

    /**
     * PeriodView一覧を得る。
     *
     * @return PeriodView の List
     */
    public List<PeriodView> getPeriodViewList(){
        int tabCount = getTabCount();
        int periodCount = tabCount - PERIODTAB_OFFSET;
        List<PeriodView> result = new ArrayList<>(periodCount);

        for(int tabIndex = PERIODTAB_OFFSET; tabIndex < tabCount; tabIndex++){
            Component component = getComponent(tabIndex);
            PeriodView periodView = (PeriodView) component;
            result.add(periodView);
        }

        return result;
    }

    /**
     * 指定したPeriod日付に関連付けられたPeriodViewを返す。
     *
     * <p>Periodに関係ないタブが指定されたらnullを返す。
     *
     * @param periodIndex Period日付インデックス
     * @return 指定されたPeriodView
     */
    public PeriodView getPeriodView(int periodIndex){
        int tabIndex = periodIndex + PERIODTAB_OFFSET;
        if(tabIndex < PERIODTAB_OFFSET || getTabCount() <= tabIndex){
            return null;
        }

        Component component = getComponentAt(tabIndex);
        if( ! (component instanceof PeriodView) ) return null;
        PeriodView periodView = (PeriodView) component;

        return periodView;
    }

    /**
     * 現在タブ選択中のPeriodViewを返す。
     *
     * <p>Periodに関係ないタブが選択されていたらnullを返す。
     *
     * @return 現在選択中のPeriodView
     */
    public PeriodView currentPeriodView(){
        int tabIndex = getSelectedIndex();
        int periodIndex = tabIndex - PERIODTAB_OFFSET;
        PeriodView result = getPeriodView(periodIndex);
        return result;
    }

    /**
     * フォント描画設定を変更する。
     *
     * <p>設定は各PeriodViewに委譲される。
     *
     * @param fontInfo フォント
     */
    public void setFontInfo(FontInfo fontInfo){
        Objects.nonNull(fontInfo);
        this.fontInfo = fontInfo;

        getPeriodViewList().forEach(periodView -> {
            periodView.setFontInfo(this.fontInfo);
        });

        return;
    }

    /**
     * 発言表示設定を変更する。
     *
     * <p>設定は各PeriodViewに委譲される。
     *
     * @param dialogPref 発言表示設定
     */
    public void setDialogPref(DialogPref dialogPref){
        Objects.nonNull(dialogPref);
        this.dialogPref = dialogPref;

        getPeriodViewList().forEach(periodView -> {
            periodView.setDialogPref(this.dialogPref);
        });

        return;
    }

    /**
     * 村情報表示タブを選択表示する。
     */
    private void showVillageInfoTab(){
        setSelectedIndex(0);
        return;
    }

    /**
     * 指定した日付インデックスのPeriodのタブを表示する。
     *
     * @param periodIndex 日付インデックス
     */
    public void showPeriodTab(int periodIndex){
        int tabIndex = periodIndex + PERIODTAB_OFFSET;
        setSelectedIndex(tabIndex);
        return;
    }

    /**
     * ActionListenerを追加する。
     *
     * <p>配下のDiscussionへもリスナは登録される。
     *
     * @param listener リスナー
     */
    public void addActionListener(ActionListener listener){
        this.listenerList.add(ActionListener.class, listener);

        getPeriodViewList().stream()
                .map(PeriodView::getDiscussion)
                .forEach(discussion ->{
                    discussion.addActionListener(listener);
                });

        return;
    }

    /**
     * ActionListenerを削除する。
     *
     * @param listener リスナー
     */
    public void removeActionListener(ActionListener listener){
        this.listenerList.remove(ActionListener.class, listener);

        getPeriodViewList().stream()
                .map(PeriodView::getDiscussion)
                .forEach(discussion ->{
                    discussion.removeActionListener(listener);
                });

        return;
    }

    /**
     * ActionListenerを列挙する。
     *
     * @return すべてのActionListener
     */
    public ActionListener[] getActionListeners(){
        return getListeners(ActionListener.class);
    }

    /**
     * AnchorHitListenerを追加する。
     *
     * <p>配下のDiscussionへもリスナは登録される。
     *
     * @param listener リスナー
     */
    public void addAnchorHitListener(AnchorHitListener listener){
        this.listenerList.add(AnchorHitListener.class, listener);

        getPeriodViewList().stream()
                .map(PeriodView::getDiscussion)
                .forEach(discussion -> {
                    discussion.addAnchorHitListener(listener);
                });

        return;
    }

    /**
     * AnchorHitListenerを削除する。
     *
     * @param listener リスナー
     */
    public void removeAnchorHitListener(AnchorHitListener listener){
        this.listenerList.remove(AnchorHitListener.class, listener);

        getPeriodViewList().stream()
                .map(PeriodView::getDiscussion)
                .forEach(discussion -> {
                    discussion.removeAnchorHitListener(listener);
                });

        return;
    }

    /**
     * AnchorHitListenerを列挙する。
     *
     * @return すべてのAnchorHitListener
     */
    public AnchorHitListener[] getAnchorHitListeners(){
        return getListeners(AnchorHitListener.class);
    }

}
