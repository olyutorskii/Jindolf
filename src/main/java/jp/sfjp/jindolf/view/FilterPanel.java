/*
 * Filter panel
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import jp.sfjp.jindolf.data.Avatar;
import jp.sfjp.jindolf.data.SysEvent;
import jp.sfjp.jindolf.data.Talk;
import jp.sfjp.jindolf.data.Topic;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sourceforge.jindolf.corelib.EventFamily;
import jp.sourceforge.jindolf.corelib.TalkType;

/**
 * 発言フィルタ GUI。
 */
@SuppressWarnings("serial")
public final class FilterPanel extends JDialog
        implements ActionListener, TopicFilter{

    private static final int COLS = 4;


    private final JCheckBox checkPublic = new JCheckBox("公開", true);
    private final JCheckBox checkWolf = new JCheckBox("狼", true);
    private final JCheckBox checkPrivate = new JCheckBox("独り言", true);
    private final JCheckBox checkGrave = new JCheckBox("墓下", true);
    private final JCheckBox checkExtra = new JCheckBox("Extra", true);

    private final JButton selAllButton = new JButton("全選択");
    private final JButton selNoneButton = new JButton("全解除");
    private final JButton negateButton = new JButton("反転");

    private final JCheckBox checkRealtime =
            new JCheckBox("リアルタイム更新", true);
    private final JButton applyButton = new JButton("フィルタ適用");

    private final Map<Avatar, JCheckBox> cbMap =
            new HashMap<>();
    private final List<JCheckBox> cbList = new LinkedList<>();

    private final EventListenerList listeners = new EventListenerList();

    /**
     * 発言フィルタを生成する。
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public FilterPanel(){
        super((Dialog)null);
        // We need unowned dialog

        setModal(false);

        GUIUtils.modifyWindowAttributes(this, true, false, true);

        JComponent topicPanel = createTopicPanel();
        JComponent avatarPanel = createAvatarPanel();
        JComponent buttonPanel = createButtonPanel();
        JComponent bottomPanel = createBottomPanel();
        design(topicPanel, avatarPanel, buttonPanel, bottomPanel);

        this.checkPublic.addActionListener(this);
        this.checkWolf.addActionListener(this);
        this.checkPrivate.addActionListener(this);
        this.checkGrave.addActionListener(this);
        this.checkExtra.addActionListener(this);

        for(JCheckBox avatarCheckBox : this.cbList){
            avatarCheckBox.addActionListener(this);
        }

        this.selAllButton.addActionListener(this);
        this.selNoneButton.addActionListener(this);
        this.negateButton.addActionListener(this);

        this.checkRealtime.addActionListener(this);
        this.applyButton.addActionListener(this);
        this.applyButton.setEnabled(false);

        return;
    }

    /**
     * レイアウトデザインを行う。
     *
     * @param topicPanel システムイベント選択
     * @param avatarPanel キャラ一覧
     * @param buttonPanel ボタン群
     * @param bottomPanel 下段パネル
     */
    private void design(JComponent topicPanel,
                         JComponent avatarPanel,
                         JComponent buttonPanel,
                         JComponent bottomPanel ){
        Container content = getContentPane();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        content.setLayout(layout);

        constraints.weightx = 1.0 / 5;
        constraints.weighty = 1.0;
        constraints.gridheight = 3;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        content.add(topicPanel, constraints);

        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.insets = new Insets(3, 0, 3, 0);
        content.add(new JSeparator(SwingConstants.VERTICAL), constraints);

        constraints.weightx = 4.0 / 5;
        constraints.weighty = 0.0;
        constraints.gridheight = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        content.add(buttonPanel, constraints);

        constraints.insets = new Insets(0, 3, 0, 3);
        content.add(new JSeparator(), constraints);

        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 0, 0);
        content.add(avatarPanel, constraints);

        constraints.weightx = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        content.add(new JSeparator(SwingConstants.HORIZONTAL), constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTHEAST;
        content.add(bottomPanel, constraints);

        return;
    }

    /**
     * システムイベントチェックボックス群パネルを作成。
     *
     * @return システムイベントチェックボックス群パネル
     */
    private JComponent createTopicPanel(){
        this.checkPublic.setToolTipText("誰にでも見える発言");
        this.checkWolf.setToolTipText("人狼同士にしか見えない発言");
        this.checkPrivate.setToolTipText("本人にしか見えない発言");
        this.checkGrave.setToolTipText("死者同士にしか見えない発言");
        this.checkExtra.setToolTipText("占い先や護衛先などのシステムメッセージ");

        JPanel topicPanel = new JPanel();

        Border border = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        topicPanel.setBorder(border);

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        topicPanel.setLayout(layout);

        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        topicPanel.add(this.checkPublic, constraints);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        topicPanel.add(this.checkWolf, constraints);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        topicPanel.add(this.checkPrivate, constraints);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        topicPanel.add(this.checkGrave, constraints);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        topicPanel.add(this.checkExtra, constraints);

        return topicPanel;
    }

    /**
     * キャラ一覧チェックボックス群パネルを作成。
     *
     * @return キャラ一覧チェックボックス群パネル
     */
    private JComponent createAvatarPanel(){
        JPanel avatarPanel = new JPanel();

        Border border = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        avatarPanel.setBorder(border);

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        avatarPanel.setLayout(layout);

        constraints.weightx = 1.0 / COLS;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.WEST;

        int xPos = 0;
        for(Avatar avatar : Avatar.getPredefinedAvatarList()){
            JCheckBox checkBox = new JCheckBox(avatar.getName(), true);
            checkBox.setToolTipText(avatar.getJobTitle());
            this.cbList.add(checkBox);
            if(xPos >= COLS - 1){
                constraints.gridwidth = GridBagConstraints.REMAINDER;
                xPos = 0;
            }else{
                constraints.gridwidth = 1;
                xPos++;
            }
            avatarPanel.add(checkBox, constraints);
            this.cbMap.put(avatar, checkBox);
        }

        return avatarPanel;
    }

    /**
     * ボタン群パネルを生成。
     *
     * @return ボタン群パネル
     */
    private JComponent createButtonPanel(){
        this.selAllButton.setToolTipText(
                "全キャラクタの発言を表示する");
        this.selNoneButton.setToolTipText(
                "全キャラクタの発言をフィルタリングする");
        this.negateButton.setToolTipText(
                "(表示⇔フィルタリング)の設定を反転させる");

        JPanel buttonPanel = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        buttonPanel.setLayout(layout);

        constraints.weightx = 1.0 / 3;
        constraints.insets = new Insets(3, 3, 3, 3);
        buttonPanel.add(this.selAllButton, constraints);
        buttonPanel.add(this.selNoneButton, constraints);
        buttonPanel.add(this.negateButton, constraints);

        return buttonPanel;
    }

    /**
     * 下段パネルを生成する。
     *
     * @return 下段パネル
     */
    private JComponent createBottomPanel(){
        JPanel panel = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        panel.setLayout(layout);

        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(3, 3, 3, 3);
        panel.add(this.checkRealtime, constraints);
        panel.add(this.applyButton, constraints);

        return panel;
    }

    /**
     * リスナを登録する。
     *
     * @param listener リスナ
     */
    public void addChangeListener(ChangeListener listener){
        this.listeners.add(ChangeListener.class, listener);
    }

    /**
     * リスナを削除する。
     *
     * @param listener リスナ
     */
    public void removeChangeListener(ChangeListener listener){
        this.listeners.remove(ChangeListener.class, listener);
    }

    /**
     * 全リスナを取得する。
     *
     * @return リスナの配列
     */
    public ChangeListener[] getChangeListeners(){
        return this.listeners.getListeners(ChangeListener.class);
    }

    /**
     * 全リスナへフィルタ操作を通知する。
     */
    protected void fireCheckChanged(){
        ChangeEvent changeEvent = new ChangeEvent(this);
        for(ChangeListener listener : getChangeListeners()){
            listener.stateChanged(changeEvent);
        }
    }

    /**
     * ボタン状態の初期化。
     */
    public void initButtons(){
        this.checkPublic.setSelected(true);
        this.checkWolf.setSelected(true);
        this.checkPrivate.setSelected(true);
        this.checkGrave.setSelected(true);
        this.checkExtra.setSelected(true);

        this.selAllButton.doClick();

        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>チェックボックスまたはボタン操作時にリスナとして呼ばれる。
     *
     * @param event イベント
     */
    @Override
    public void actionPerformed(ActionEvent event){
        Object source = event.getSource();

        boolean isRealtime = this.checkRealtime.isSelected();

        if(source == this.selAllButton){
            boolean hasChanged = false;
            for(JCheckBox avatarCBox : this.cbList){
                if( ! avatarCBox.isSelected()){
                    avatarCBox.setSelected(true);
                    hasChanged = true;
                }
            }
            if(isRealtime && hasChanged){
                fireCheckChanged();
            }
        }else if(source == this.selNoneButton){
            boolean hasChanged = false;
            for(JCheckBox avatarCBox : this.cbList){
                if(avatarCBox.isSelected()){
                    avatarCBox.setSelected(false);
                    hasChanged = true;
                }
            }
            if(isRealtime && hasChanged){
                fireCheckChanged();
            }
        }else if(source == this.negateButton){
            for(JCheckBox avatarCBox : this.cbList){
                if(avatarCBox.isSelected()){
                    avatarCBox.setSelected(false);
                }else{
                    avatarCBox.setSelected(true);
                }
            }
            if(isRealtime){
                fireCheckChanged();
            }
        }else if(source == this.checkRealtime){
            if(isRealtime){
                this.applyButton.setEnabled(false);
                fireCheckChanged();
            }else{
                this.applyButton.setEnabled(true);
            }
        }else if(source == this.applyButton){
            fireCheckChanged();
        }else if(source instanceof JCheckBox){
            if(isRealtime){
                fireCheckChanged();
            }
        }

        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param topic {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean isFiltered(Topic topic){
        Talk talk;
        if(topic instanceof Talk){
            talk = (Talk) topic;
        }else if(topic instanceof SysEvent){
            SysEvent sysEvent = (SysEvent) topic;
            if(sysEvent.getEventFamily() == EventFamily.EXTRA){
                if( ! this.checkExtra.isSelected() ){
                    return true;
                }
            }
            return false;
        }else{
            return false;
        }

        JCheckBox cbox;

        TalkType type = talk.getTalkType();
        switch(type){
        case PUBLIC:
            cbox = this.checkPublic;
            break;
        case WOLFONLY:
            cbox = this.checkWolf;
            break;
        case PRIVATE:
            cbox = this.checkPrivate;
            break;
        case GRAVE:
            cbox = this.checkGrave;
            break;
        default:
            assert false;
            return true;
        }
        if( ! cbox.isSelected()){
            return true;
        }

        Avatar avatar = talk.getAvatar();
        cbox = this.cbMap.get(avatar);
        if( cbox != null && ! cbox.isSelected()){
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public FilterContext getFilterContext(){
        return new FilterPanelContext();
    }

    /**
     * {@inheritDoc}
     *
     * @param context {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean isSame(FilterContext context){
        if(context == null) return false;
        if( ! (context instanceof FilterPanelContext) ) return false;
        FilterPanelContext argContext = (FilterPanelContext) context;
        FilterPanelContext thisContext =
                (FilterPanelContext) getFilterContext();

        return thisContext.context.equals(argContext.context);
    }

    /**
     * カスタム化されたフィルタ状態。
     */
    private final class FilterPanelContext implements FilterContext{

        private final BitSet context = new BitSet();

        /**
         * コンストラクタ。
         */
        public FilterPanelContext(){
            super();

            int index = 0;
            this.context.set(index++,
                    FilterPanel.this.checkPublic.isSelected());
            this.context.set(index++,
                    FilterPanel.this.checkWolf.isSelected());
            this.context.set(index++,
                    FilterPanel.this.checkPrivate.isSelected());
            this.context.set(index++,
                    FilterPanel.this.checkGrave.isSelected());
            this.context.set(index++,
                    FilterPanel.this.checkExtra.isSelected());

            for(Avatar avatar : Avatar.getPredefinedAvatarList()){
                JCheckBox checkBox = FilterPanel.this.cbMap.get(avatar);
                this.context.set(index++, checkBox.isSelected());
            }

            return;
        }

        /**
         * {@inheritDoc}
         *
         * @return {@inheritDoc}
         */
        @Override
        public String toString(){
            return this.context.toString();
        }

    }

}
