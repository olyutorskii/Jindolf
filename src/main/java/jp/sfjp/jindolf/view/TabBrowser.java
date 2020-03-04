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
        selectVillageInfoTab();

        repaint();
        revalidate();

        return;
    }

    /**
     * 指定した数のPeriodが収まるよう必要十分なタブ数を用意する。
     *
     * @param periods Periodの数(エピローグを含む)
     */
    private void modifyTabCount(int periods){ // TODO 0でも大丈夫?
        int maxPeriodDays = periods - 1;

        for(;;){   // 短ければタブ追加
            int lastTabIndex = getTabCount() - 1;
            if(tabIndexToPeriodDays(lastTabIndex) >= maxPeriodDays) break;
            String tabTitle = "";
            Component dummy = new JPanel();
            addTab(tabTitle, dummy);
        }

        for(;;){   // 長ければ余分なタブ削除
            int lastTabIndex = getTabCount() - 1;
            if(tabIndexToPeriodDays(lastTabIndex) <= maxPeriodDays) break;
            remove(lastTabIndex);
        }

        return;
    }

    /**
     * Period日付指定からタブインデックス値への変換。
     *
     * <p>エピローグ(0日目)のタブインデックスは1。
     * 1日目Periodのタブインデックスは2。
     *
     * @param days Period日付指定
     * @return タブインデックス。存在しないタブの場合は負の値。
     */
    public int periodDaysToTabIndex(int days){
        if(days < 0) return -1;
        int tabIndex = days+1;
        if(tabIndex >= getTabCount()) return -1;
        return tabIndex;
    }

    /**
     * タブインデックス値からPeriod日付指定への変換。
     *
     * <p>エピローグタブのPeriod日付は0。
     * 1日目PeriodタブのPeriod日付は1。
     *
     * @param tabIndex タブインデックス
     * @return Period日付指定。存在しないタブの場合は負の値。
     */
    private int tabIndexToPeriodDays(int tabIndex){
        if(tabIndex < 0) return -1;
        if(tabIndex >= getTabCount()) return -1;
        int days = tabIndex - 1;
        return days;
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
            selectVillageInfoTab();
        }

        updateVillageInfo();

        int periodNum = this.village.getPeriodSize();
        modifyTabCount(periodNum);

        for(int periodDays = 0; periodDays < periodNum; periodDays++){
            Period period = this.village.getPeriod(periodDays);
            PeriodView periodView = buildPeriodView(period);

            int tabIndex = periodDaysToTabIndex(periodDays);
            setComponentAt(tabIndex, periodView);

            String caption = period.getCaption();
            setTitleAt(tabIndex, caption);
        }

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
     * 村情報表示タブを選択表示する。
     */
    private void selectVillageInfoTab(){
        setSelectedIndex(0);
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
        List<PeriodView> result = new ArrayList<>(tabCount - 1);

        for(int tabIndex = 1; tabIndex < tabCount; tabIndex++){
            Component component = getComponent(tabIndex);
            PeriodView periodView = (PeriodView) component;
            result.add(periodView);
        }

        return result;
    }

    /**
     * 指定したタブインデックスに関連付けられたPeriodViewを返す。
     *
     * <p>Periodに関係ないタブが指定されたらnullを返す。
     *
     * @param tabIndex タブインデックス
     * @return 指定されたPeriodView
     */
    public PeriodView getPeriodView(int tabIndex){
        if(tabIndexToPeriodDays(tabIndex) < 0) return null;
        if(tabIndex >= getTabCount()) return null;

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
        PeriodView result = getPeriodView(tabIndex);
        return result;
    }

    /**
     * 指定したタブインデックスに関連付けられたDiscussionを返す。
     *
     * <p>Periodに関係ないタブが指定されたらnullを返す。
     *
     * @param tabIndex タブインデックス
     * @return 指定されたDiscussion
     */
    private Discussion getDiscussion(int tabIndex){
        PeriodView periodView = getPeriodView(tabIndex);
        if(periodView == null) return null;

        Discussion result = periodView.getDiscussion();
        return result;
    }

    /**
     * 現在タブ選択中のDiscussionを返す。
     *
     * <p>Periodに関係ないタブが選択されていたらnullを返す。
     *
     * @return 現在選択中のDiscussion
     */
    public Discussion currentDiscussion(){
        int tabIndex = getSelectedIndex();
        Discussion result = getDiscussion(tabIndex);
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
