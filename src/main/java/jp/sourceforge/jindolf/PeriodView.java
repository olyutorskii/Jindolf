/*
 * period viewer
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: PeriodView.java 972 2009-12-26 05:05:15Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import jp.sourceforge.jindolf.corelib.TalkType;

/**
 * 発言ブラウザを内包するPeriodビューワ。
 */
@SuppressWarnings("serial")
public class PeriodView extends JPanel implements ItemListener{

    private static final Color COLOR_SELECT = new Color(0xffff80);
    private static final Color COLOR_NORMALBG = Color.BLACK;
    private static final Color COLOR_SIMPLEBG = Color.WHITE;

    private Period period;

    private final Discussion discussion;
    private final JScrollPane scroller = new JScrollPane();
    private final JLabel caption = new JLabel();
    private final JLabel limit = new JLabel();
    private final JComboBox talkSelector = new JComboBox();
    private final DefaultComboBoxModel model = new DefaultComboBoxModel();

    private DialogPref dialogPref = new DialogPref();

    /**
     * 発言ブラウザを内包するPeriodビューワを生成する。
     * @param period 日
     */
    public PeriodView(Period period){
        super();

        this.period = period;

        this.talkSelector.setEditable(false);
        this.talkSelector.setMaximumRowCount(20);
        this.talkSelector.setModel(this.model);
        this.talkSelector.setRenderer(new AnchorRenderer());
        this.talkSelector.addItemListener(this);

        this.discussion = new Discussion();
        Border border =
                BorderFactory.createMatteBorder(15, 15, 15, 15, Color.BLACK);
        this.discussion.setBorder(border);
        this.discussion.setPeriod(this.period);

        JViewport viewPort = this.scroller.getViewport();
        viewPort.setBackground(Color.BLACK);
        viewPort.setView(this.discussion);

        this.scroller.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.scroller.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        design();

        setColorDesign();

        return;
    }

    /**
     * デザインを行う。
     */
    private void design(){
        LayoutManager layout;

        JPanel topPanel = new JPanel();
        layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        topPanel.setLayout(layout);
        constraints.insets = new Insets(1, 3, 1, 3);
        constraints.weightx = 1.0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        topPanel.add(this.caption, constraints);
        constraints.weightx = 0.0;
        constraints.anchor = GridBagConstraints.EAST;
        topPanel.add(this.limit, constraints);
        constraints.weightx = 0.0;
        topPanel.add(this.talkSelector, constraints);

        layout = new BorderLayout();
        setLayout(layout);
        add(topPanel,      BorderLayout.NORTH);
        add(this.scroller, BorderLayout.CENTER);

        return;
    }

    /**
     * 配色を設定する。
     */
    private void setColorDesign(){
        Color bgColor;

        if(this.dialogPref.isSimpleMode()){
            bgColor = COLOR_SIMPLEBG;
        }else{
            bgColor = COLOR_NORMALBG;
        }

        JViewport viewPort = this.scroller.getViewport();
        viewPort.setBackground(bgColor);

        Border border =
                BorderFactory.createMatteBorder(15, 15, 15, 15, bgColor);
        this.discussion.setBorder(border);

        repaint();

        return;
    }

    /**
     * Periodを更新する。
     * 古いPeriodの表示内容は消える。
     * 新しいPeriodの表示内容はまだ反映されない。
     * @param period 新しいPeriod
     */
    public void setPeriod(Period period){
        this.discussion.setPeriod(period);

        this.period = period;

        updateTopPanel();

        return;
    }

    /**
     * 現在のPeriodを返す。
     * @return 現在のPeriod
     */
    public Period getPeriod(){
        return this.discussion.getPeriod();
    }

    /**
     * 上部のGUI(村名、発言一覧)を、Periodの状態に合わせて更新する。
     */
    private void updateTopPanel(){
        if(this.period == null){
            this.caption.setText("");
            this.limit.setText("");
            this.model.removeAllElements();
            return;
        }

        Village village = this.period.getVillage();
        String villageName = village.getVillageName();

        String dayCaption   = this.period.getCaption();
        String limitCaption = this.period.getLimit();
        String account      = this.period.getLoginName();

        String loginout;
        if(this.period.isFullOpen()){
            loginout = "";
        }else if(account != null){
            loginout = " (ログイン中)";
        }else{
            loginout = " (ログアウト中)";
        }

        String info = villageName + "村 " + dayCaption + loginout;
        this.caption.setText(info);
        this.limit.setText("更新時刻 " + limitCaption);

        this.model.removeAllElements();
        this.model.addElement(" ");
        List<Topic> topicList = this.period.getTopicList();
        for(Topic topic : topicList){
            if( ! (topic instanceof Talk) ) continue;
            Talk talk = (Talk) topic;
            if(talk.getTalkCount() <= 0) continue;
            this.model.addElement(talk);
        }

        return;
    }

    /**
     * フィルタを適用してPeriodの内容を出力する。
     */
    public void showTopics(){
        Period newPeriod = this.discussion.getPeriod();
        setPeriod(newPeriod);
        return;
    }

    /**
     * フォント描画設定を変更する。
     * @param fontInfo フォント設定
     */
    // TODO スクロール位置の復元
    public void setFontInfo(FontInfo fontInfo){
        this.discussion.setFontInfo(fontInfo);

        revalidate();
        repaint();

        return;
    }

    /**
     * 発言表示設定を更新する。
     * @param pref 表示設定
     */
    public void setDialogPref(DialogPref pref){
        this.dialogPref = pref;
        this.discussion.setDialogPref(this.dialogPref);

        setColorDesign();
        revalidate();
        repaint();

        return;
    }

    /**
     * ビューポート内の発言ブラウザを返す。
     * @return 内部ブラウザ
     */
    public Discussion getDiscussion(){
        return this.discussion;
    }

    /**
     * スクロール位置を返す。
     * @return スクロール位置
     */
    public int getVerticalPosition(){
        JScrollBar vt = this.scroller.getVerticalScrollBar();
        int pos = vt.getValue();
        return pos;
    }

    /**
     * スクロール位置を設定する。
     * @param pos スクロール位置
     */
    public void setVerticalPosition(int pos){
        JScrollBar vt = this.scroller.getVerticalScrollBar();
        vt.setValue(pos);
        return;
    }

    /**
     * {@inheritDoc}
     * コンボボックス操作のリスナ。
     * @param event コンボボックス操作イベント {@inheritDoc}
     */
    public void itemStateChanged(ItemEvent event){
        if(event.getStateChange() != ItemEvent.SELECTED) return;

        Object selected = this.talkSelector.getSelectedItem();
        if( ! (selected instanceof Talk) ) return;
        Talk talk = (Talk) selected;

        scrollToTalk(talk);

        return;
    }

    /**
     * 任意の発言が表示されるようスクロールする。
     * @param talk 発言
     */
    public void scrollToTalk(Talk talk){
        if(talk == null) return;
        if(talk.getPeriod() != this.period) return;

        Rectangle rect = this.discussion.getTalkBounds(talk);
        if(rect == null) return;

        Rectangle showRect = new Rectangle(rect);
        showRect.y -= 15;
        showRect.height = this.scroller.getHeight();
        this.discussion.scrollRectToVisible(showRect);

        return;
    }

    /**
     * Talkをアイテムに持つコンボボックス用のセル描画。
     */
    private static class AnchorRenderer extends DefaultListCellRenderer{

        /**
         * コンストラクタ。
         */
        public AnchorRenderer(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         * Talkのアンカー表記と発言者名を描画する。
         * @param list {@inheritDoc}
         * @param value {@inheritDoc}
         * @param index {@inheritDoc}
         * @param isSelected {@inheritDoc}
         * @param cellHasFocus {@inheritDoc}
         * @return {@inheritDoc}
         */
        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus ){
            Talk talk = null;
            Object newValue = value;
            if(value instanceof Talk){
                talk = (Talk) value;
                newValue = new StringBuilder()
                          .append(talk.getAnchorNotation())
                          .append(' ')
                          .append(talk.getAvatar().getName())
                          .toString();
            }

            Component superResult =
                    super.getListCellRendererComponent(list,
                                                       newValue,
                                                       index,
                                                       isSelected,
                                                       cellHasFocus);

            if(talk != null){
                Color bgColor = null;
                if(isSelected){
                    bgColor = COLOR_SELECT;
                }else{
                    TalkType type = talk.getTalkType();
                    bgColor = TalkDraw.getTypedColor(type);
                }
                superResult.setForeground(Color.BLACK);
                superResult.setBackground(bgColor);
            }

            return superResult;
        }
    }

    // TODO フィルタ中の発言をプルダウン選択したらどうあるべきか？
}
