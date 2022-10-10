/*
 * period viewer with tab access
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.EventListenerList;
import jp.sfjp.jindolf.data.DialogPref;
import jp.sfjp.jindolf.data.Period;
import jp.sfjp.jindolf.data.Village;
import jp.sfjp.jindolf.glyph.AnchorHitListener;
import jp.sfjp.jindolf.glyph.Discussion;
import jp.sfjp.jindolf.glyph.FontInfo;

/**
 * タブを用いて村情報と各Periodを閲覧するためのコンポーネント。
 */
@SuppressWarnings("serial")
public class TabBrowser extends JTabbedPane{

    private Village village;

    private final VillageInfoPanel villageInfo = new VillageInfoPanel();

    private FontInfo fontInfo;
    private DialogPref dialogPref;

    private final EventListenerList thisListenerList =
            new EventListenerList();

    /**
     * 村が指定されていない状態のタブパネルを生成する。
     */
    public TabBrowser(){
        super();

        setTabPlacement(SwingConstants.TOP);
        // Mac Aqua L&F ignore WRAP_TAB_LAYOUT
        setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

        Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        this.villageInfo.setBorder(border);

        addTab("村情報", new JScrollPane(this.villageInfo));

        setVillage(null);

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
     * 設定された村を返す。
     * @return 設定された村
     */
    public Village getVillage(){
        return this.village;
    }

    /**
     * 新規に村を設定する。
     * @param village 新しい村
     */
    public final void setVillage(Village village){
        if(village == null){
            if(this.village != null){
                this.village.unloadPeriods();
            }
            this.village = null;
            selectVillageInfoTab();
            modifyTabCount(0);
            updateVillageInfo();
            return;
        }else if(village != this.village){
            selectVillageInfoTab();
        }

        if(this.village != null){
            this.village.unloadPeriods();
        }
        this.village = village;

        updateVillageInfo();

        int periodNum = this.village.getPeriodSize();
        modifyTabCount(periodNum);

        for(int periodDays = 0; periodDays < periodNum; periodDays++){
            Period period = this.village.getPeriod(periodDays);
            int tabIndex = periodDaysToTabIndex(periodDays);
            PeriodView periodView = getPeriodView(tabIndex);
            if(periodView == null){
                periodView = new PeriodView(period);
                periodView.setFontInfo(this.fontInfo);
                periodView.setDialogPref(this.dialogPref);
                setComponentAt(tabIndex, periodView);
                Discussion discussion = periodView.getDiscussion();
                for(ActionListener listener : getActionListeners()){
                    discussion.addActionListener(listener);
                }
                for(AnchorHitListener listener : getAnchorHitListeners()){
                    discussion.addAnchorHitListener(listener);
                }
            }
            String caption = period.getCaption();
            setTitleAt(tabIndex, caption);
            if(period == periodView.getPeriod()) continue;
            periodView.setPeriod(period);
        }

        return;
    }

    /**
     * 指定した数のPeriodが収まるよう必要十分なタブ数を用意する。
     * @param periods Periodの数
     */
    private void modifyTabCount(int periods){ // TODO 0でも大丈夫?
        int maxPeriodDays = periods - 1;

        for(;;){   // 短ければタブ追加
            int maxTabIndex = getTabCount() - 1;
            if(tabIndexToPeriodDays(maxTabIndex) >= maxPeriodDays) break;
            String title = "";
            Component component = new JPanel();
            addTab(title, component);
        }

        for(;;){   // 長ければ余分なタブ削除
            int maxTabIndex = getTabCount() - 1;
            if(tabIndexToPeriodDays(maxTabIndex) <= maxPeriodDays) break;
            remove(maxTabIndex);
        }

        return;
    }

    /**
     * Period日付指定からタブインデックス値への変換。
     * @param days Period日付指定
     * @return タブインデックス
     */
    public int periodDaysToTabIndex(int days){
        int tabIndex = days+1;
        if(tabIndex >= getTabCount()) return -1;
        return tabIndex;
    }

    /**
     * タブインデックス値からPeriod日付指定への変換。
     * @param tabIndex タブインデックス
     * @return Period日付指定
     */
    private int tabIndexToPeriodDays(int tabIndex){
        if(tabIndex >= getTabCount()) return - 1;
        int days = tabIndex - 1;
        return days;
    }

    /**
     * PeriodView一覧を得る。
     * @return PeriodView の List
     */
    public List<PeriodView> getPeriodViewList(){
        List<PeriodView> result = new LinkedList<>();

        int tabCount = getTabCount();
        for(int tabIndex = 0; tabIndex <= tabCount - 1; tabIndex++){
            Component component = getComponent(tabIndex);
            if(component == null) continue;
            if( ! (component instanceof PeriodView) ) continue;
            PeriodView periodView = (PeriodView) component;
            result.add(periodView);
        }

        return result;
    }

    /**
     * 現在タブ選択中のDiscussionを返す。
     * Periodに関係ないタブが選択されていたらnullを返す。
     * @return 現在選択中のDiscussion
     */
    public Discussion currentDiscussion(){
        int tabIndex = getSelectedIndex();
        Discussion result = getDiscussion(tabIndex);
        return result;
    }

    /**
     * 現在タブ選択中のPeriodViewを返す。
     * Periodに関係ないタブが選択されていたらnullを返す。
     * @return 現在選択中のPeriodView
     */
    public PeriodView currentPeriodView(){
        int tabIndex = getSelectedIndex();
        PeriodView result = getPeriodView(tabIndex);
        return result;
    }

    /**
     * 指定したタブインデックスに関連付けられたPeriodViewを返す。
     * Periodに関係ないタブが指定されたらnullを返す。
     * @param tabIndex タブインデックス
     * @return 指定されたPeriodView
     */
    public PeriodView getPeriodView(int tabIndex){
        if(tabIndexToPeriodDays(tabIndex) < 0) return null;
        if(tabIndex >= getTabCount()) return null;
        Component component = getComponentAt(tabIndex);
        if(component == null) return null;

        if( ! (component instanceof PeriodView) ) return null;
        PeriodView periodView = (PeriodView) component;

        return periodView;
    }

    /**
     * 指定したタブインデックスに関連付けられたDiscussionを返す。
     * Periodに関係ないタブが指定されたらnullを返す。
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
     * フォント描画設定を変更する。
     * @param fontInfo フォント
     */
    public void setFontInfo(FontInfo fontInfo){
        this.fontInfo = fontInfo;

        for(int tabIndex = 0; tabIndex <= getTabCount() - 1; tabIndex++){
            PeriodView periodView = getPeriodView(tabIndex);
            if(periodView == null) continue;
            periodView.setFontInfo(this.fontInfo);
        }

        return;
    }

    /**
     * 発言表示設定を変更する。
     * @param dialogPref 発言表示設定
     */
    public void setDialogPref(DialogPref dialogPref){
        this.dialogPref = dialogPref;

        for(int tabIndex = 0; tabIndex <= getTabCount() - 1; tabIndex++){
            PeriodView periodView = getPeriodView(tabIndex);
            if(periodView == null) continue;
            periodView.setDialogPref(this.dialogPref);
        }

        return;
    }

    /**
     * ActionListenerを追加する。
     * @param listener リスナー
     */
    public void addActionListener(ActionListener listener){
        this.thisListenerList.add(ActionListener.class, listener);

        if(this.village == null) return;
        int periodNum = this.village.getPeriodSize();
        for(int periodDays = 0; periodDays < periodNum; periodDays++){
            int tabIndex = periodDaysToTabIndex(periodDays);
            Discussion discussion = getDiscussion(tabIndex);
            if(discussion == null) continue;
            discussion.addActionListener(listener);
        }

        return;
    }

    /**
     * ActionListenerを削除する。
     * @param listener リスナー
     */
    public void removeActionListener(ActionListener listener){
        this.thisListenerList.remove(ActionListener.class, listener);

        if(this.village == null) return;
        int periodNum = this.village.getPeriodSize();
        for(int periodDays = 0; periodDays < periodNum; periodDays++){
            int tabIndex = periodDaysToTabIndex(periodDays);
            Discussion discussion = getDiscussion(tabIndex);
            if(discussion == null) continue;
            discussion.removeActionListener(listener);
        }

        return;
    }

    /**
     * ActionListenerを列挙する。
     * @return すべてのActionListener
     */
    public ActionListener[] getActionListeners(){
        return this.thisListenerList.getListeners(ActionListener.class);
    }

    /**
     * AnchorHitListenerを追加する。
     * @param listener リスナー
     */
    public void addAnchorHitListener(AnchorHitListener listener){
        this.thisListenerList.add(AnchorHitListener.class, listener);

        if(this.village == null) return;
        int periodNum = this.village.getPeriodSize();
        for(int periodDays = 0; periodDays < periodNum; periodDays++){
            int tabIndex = periodDaysToTabIndex(periodDays);
            Discussion discussion = getDiscussion(tabIndex);
            if(discussion == null) continue;
            discussion.addAnchorHitListener(listener);
        }

        return;
    }

    /**
     * AnchorHitListenerを削除する。
     * @param listener リスナー
     */
    public void removeAnchorHitListener(AnchorHitListener listener){
        this.thisListenerList.remove(AnchorHitListener.class, listener);

        if(this.village == null) return;
        int periodNum = this.village.getPeriodSize();
        for(int periodDays = 0; periodDays < periodNum; periodDays++){
            int tabIndex = periodDaysToTabIndex(periodDays);
            Discussion discussion = getDiscussion(tabIndex);
            if(discussion == null) continue;
            discussion.removeAnchorHitListener(listener);
        }

        return;
    }

    /**
     * AnchorHitListenerを列挙する。
     * @return すべてのAnchorHitListener
     */
    public AnchorHitListener[] getAnchorHitListeners(){
        return this.thisListenerList.getListeners(AnchorHitListener.class);
    }

    /**
     * {@inheritDoc}
     * @param <T> {@inheritDoc}
     * @param listenerType {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public <T extends EventListener> T[] getListeners(Class<T> listenerType){
        T[] result;
        result = this.thisListenerList.getListeners(listenerType);

        if(result.length <= 0){
            result = super.getListeners(listenerType);
        }

        return result;
    }

}
