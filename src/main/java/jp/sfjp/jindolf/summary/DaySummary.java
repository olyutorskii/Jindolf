/*
 * summary of day panel
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.summary;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jp.sfjp.jindolf.data.Avatar;
import jp.sfjp.jindolf.data.Period;
import jp.sfjp.jindolf.data.Talk;
import jp.sfjp.jindolf.data.Topic;
import jp.sfjp.jindolf.data.Village;
import jp.sfjp.jindolf.glyph.TalkDraw;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sfjp.jindolf.view.AvatarPics;
import jp.sourceforge.jindolf.corelib.TalkType;

/**
 * その日ごとの集計。
 */
@SuppressWarnings("serial")
public class DaySummary extends JDialog
        implements WindowListener, ActionListener, ItemListener{

    private static final NumberFormat AVERAGE_FORM;
    private static final String PUBTALK   = "白発言";
    private static final String WOLFTALK  = "赤発言";
    private static final String GRAVETALK = "青発言";
    private static final String PRVTALK   = "灰発言";
    private static final String ALLTALK   = "全発言";
    private static final int HORIZONTAL_GAP = 5;
    private static final int VERTICAL_GAP   = 1;
    private static final Color COLOR_ALL = new Color(0xffff80);

    static{
        AVERAGE_FORM = NumberFormat.getInstance();
        AVERAGE_FORM.setMaximumFractionDigits(1);
        AVERAGE_FORM.setMinimumFractionDigits(1);
    }


    private final DefaultTableModel tableModel;
    private final TableColumn avatarColumn;

    private final JTable tableComp;
    private final JComboBox<String> typeSelector = new JComboBox<>();
    private final JButton closeButton = new JButton("閉じる");
    private final JLabel caption = new JLabel();
    private final JLabel totalSum = new JLabel();

    private TalkType talkFilter;
    private Period period;


    /**
     * コンストラクタ。
     * 集計結果を表示するモーダルダイアログを生成する。
     * @param owner オーナー
     */
    public DaySummary(Frame owner){
        super(owner);
        setModal(true);

        GUIUtils.modifyWindowAttributes(this, true, false, true);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        this.tableModel = createInitModel();
        this.tableComp = new JTable();
        this.tableComp.setModel(this.tableModel);
        this.tableComp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.tableComp.setIntercellSpacing(
                new Dimension(HORIZONTAL_GAP, VERTICAL_GAP) );
        this.tableComp.setDefaultEditor(Object.class, null);
        this.tableComp.setDefaultRenderer(Object.class, new CustomRenderer());
        this.tableComp.setShowGrid(true);

        TableColumnModel tcolModel = this.tableComp.getColumnModel();

        this.avatarColumn = tcolModel.getColumn(0);

        DefaultTableCellRenderer renderer;

        renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tcolModel.getColumn(1).setCellRenderer(renderer);

        renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tcolModel.getColumn(2).setCellRenderer(renderer);

        renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tcolModel.getColumn(3).setCellRenderer(renderer);

        this.typeSelector.addItem(PUBTALK);
        this.typeSelector.addItem(WOLFTALK);
        this.typeSelector.addItem(GRAVETALK);
        this.typeSelector.addItem(PRVTALK);
        this.typeSelector.addItem(ALLTALK);

        this.closeButton.addActionListener(this);
        this.typeSelector.addItemListener(this);

        this.typeSelector.setSelectedItem(null);
        this.typeSelector.setSelectedItem(PUBTALK);

        design();

        clearModel();

        return;
    }


    /**
     * 初期のデータモデルを生成する。
     * @return データモデル
     */
    private static DefaultTableModel createInitModel(){
        DefaultTableModel result;
        result = new DefaultTableModel();

        Object[] rowHeads = {"名前", "発言回数", "平均文字列長", "最終発言"};
        result.setColumnCount(rowHeads.length);
        result.setColumnIdentifiers(rowHeads);

        return result;
    }


    /**
     * テーブルをクリアする。
     */
    private void clearModel(){
        int rows = this.tableModel.getRowCount();
        for(int ct = 1; ct <= rows; ct++){
            this.tableModel.removeRow(0);
        }
    }

    /**
     * 行を追加する。
     * @param avatar アバター
     * @param talkCount 発言回数
     * @param totalChars 発言文字総数
     * @param lastTime 最終発言時刻
     */
    private void appendRow(Avatar avatar,
                            Integer talkCount,
                            Integer totalChars,
                            String lastTime ){
        String talks = talkCount + " 回";

        double average;
        if(talkCount <= 0){
            average = 0.0;
        }else{
            average = (double) totalChars / (double) talkCount;
        }
        String chars = AVERAGE_FORM.format(average) + " 文字";

        Object[] row = {avatar, talks, chars, lastTime};
        int rowIndex = this.tableModel.getRowCount();

        this.tableModel.insertRow(rowIndex, row);

        return;
    }

    /**
     * デザインを行う。
     */
    private void design(){
        Container content = getContentPane();

        LayoutManager layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        content.setLayout(layout);

        constraints.insets = new Insets(5, 5, 5, 5);

        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        content.add(this.caption, constraints);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        content.add(this.typeSelector, constraints);

        JScrollPane scroller = new JScrollPane(this.tableComp);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        content.add(scroller, constraints);

        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        content.add(this.totalSum, constraints);

        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        content.add(new JSeparator(), constraints);

        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        content.add(this.closeButton, constraints);

        return;
    }

    /**
     * 与えられたPeriodで集計を更新する。
     * @param newPeriod 日
     */
    public void summaryPeriod(Period newPeriod){
        this.period = newPeriod;
        summaryPeriod();
    }

    /**
     * 集計を更新する。
     */
    private void summaryPeriod(){
        clearModel();

        if(this.period == null) return;

        SortedSet<Avatar> avatarSet = new TreeSet<>();
        Map<Avatar, Integer> talkCount  = new HashMap<>();
        Map<Avatar, Integer> totalChars = new HashMap<>();
        Map<Avatar, Talk> lastTalk = new HashMap<>();

        List<Topic> topicList = this.period.getTopicList();
        for(Topic topic : topicList){
            if( ! (topic instanceof Talk)) continue;
            Talk talk = (Talk) topic;
            if(talk.getTalkCount() <= 0) continue;
            if(    this.talkFilter != null
                && talk.getTalkType() != this.talkFilter) continue;

            Avatar avatar = talk.getAvatar();

            Integer counts = talkCount.get(avatar);
            if(counts == null) counts = Integer.valueOf(0);
            counts++;
            talkCount.put(avatar, counts);

            Integer total = totalChars.get(avatar);
            if(total == null) total = Integer.valueOf(0);
            total += talk.getTotalChars();
            totalChars.put(avatar, total);

            lastTalk.put(avatar, talk);

            avatarSet.add(avatar);
        }

        int sum = 0;
        for(Avatar avatar : avatarSet){
            Integer counts = talkCount.get(avatar);
            Integer total = totalChars.get(avatar);
            String lastTime = lastTalk.get(avatar).getAnchorNotation();
            appendRow(avatar, counts, total, lastTime);
            sum += counts;
        }

        this.totalSum.setText("合計：" + sum + " 発言");

        Village village = this.period.getVillage();
        String villageName = village.getVillageName();
        String periodCaption = this.period.getCaption();
        this.caption.setText(villageName + "村 " + periodCaption);

        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowActivated(WindowEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowDeactivated(WindowEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowIconified(WindowEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowDeiconified(WindowEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowOpened(WindowEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * ダイアログのクローズボタン押下処理を行う。
     * @param event ウィンドウ変化イベント {@inheritDoc}
     */
    @Override
    public void windowClosing(WindowEvent event){
        close();
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void windowClosed(WindowEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * クローズボタン押下処理。
     * @param event イベント {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent event){
        if(event.getSource() != this.closeButton) return;
        close();
        return;
    }

    /**
     * {@inheritDoc}
     * コンボボックス操作処理。
     * @param event イベント {@inheritDoc}
     */
    @Override
    public void itemStateChanged(ItemEvent event){
        if(event.getStateChange() != ItemEvent.SELECTED) return;

        Object selected = this.typeSelector.getSelectedItem();
        if     (selected == PUBTALK)   this.talkFilter = TalkType.PUBLIC;
        else if(selected == WOLFTALK)  this.talkFilter = TalkType.WOLFONLY;
        else if(selected == GRAVETALK) this.talkFilter = TalkType.GRAVE;
        else if(selected == PRVTALK)   this.talkFilter = TalkType.PRIVATE;
        else if(selected == ALLTALK)   this.talkFilter = null;

        summaryPeriod();

        return;
    }

    /**
     * このパネルを閉じる。
     */
    private void close(){
        clearModel();
        this.period = null;
        setVisible(false);
        return;
    }

    /**
     * Avatar 顔イメージ描画用カスタムセルレンダラ。
     */
    private class CustomRenderer extends DefaultTableCellRenderer{

        /**
         * コンストラクタ。
         */
        public CustomRenderer(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         * セルに{@link Avatar}がきたら顔アイコンと名前を表示する。
         * @param value {@inheritDoc}
         */
        @Override
        public void setValue(Object value){
            if(value instanceof Avatar){
                Avatar avatar = (Avatar) value;

                Village village = DaySummary.this.period.getVillage();
                AvatarPics avatarPics = village.getAvatarPics();
                Image image = avatarPics.getAvatarFaceImage(avatar);
                if(image == null) image = avatarPics.getGraveImage();
                if(image != null){
                    ImageIcon icon = new ImageIcon(image);
                    setIcon(icon);
                }

                setText(avatar.getName());

                Dimension prefSize = getPreferredSize();

                int cellHeight = VERTICAL_GAP * 2 + prefSize.height;
                if(DaySummary.this.tableComp.getRowHeight() < cellHeight){
                    DaySummary.this.tableComp.setRowHeight(cellHeight);
                }

                int cellWidth = HORIZONTAL_GAP * 2 + prefSize.width;
                if(   DaySummary.this.avatarColumn.getPreferredWidth()
                    < cellWidth ){
                    DaySummary.this.avatarColumn.setPreferredWidth(cellWidth);
                }

                return;
            }

            super.setValue(value);

            return;
        }

        /**
         *  {@inheritDoc}
         * 統計種別によってセル色を変える。
         * @param table {@inheritDoc}
         * @param value {@inheritDoc}
         * @param isSelected {@inheritDoc}
         * @param hasFocus {@inheritDoc}
         * @param row {@inheritDoc}
         * @param column {@inheritDoc}
         * @return {@inheritDoc}
         */
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                           Object value,
                                                           boolean isSelected,
                                                           boolean hasFocus,
                                                           int row,
                                                           int column ){
            Component result = super.getTableCellRendererComponent(table,
                                                                   value,
                                                                   isSelected,
                                                                   hasFocus,
                                                                   row,
                                                                   column );

            Object selected = DaySummary.this.typeSelector.getSelectedItem();
            Color bgColor = null;
            if(selected == PUBTALK){
                bgColor = TalkDraw.COLOR_PUBLIC;
            }else if(selected == WOLFTALK){
                bgColor = TalkDraw.COLOR_WOLFONLY;
            }else if(selected == GRAVETALK){
                bgColor = TalkDraw.COLOR_GRAVE;
            }else if(selected == PRVTALK){
                bgColor = TalkDraw.COLOR_PRIVATE;
            }else if(selected == ALLTALK){
                bgColor = COLOR_ALL;
            }else{
                assert false;
                return null;
            }

            result.setForeground(Color.BLACK);
            result.setBackground(bgColor);

            return result;
        }
    }

}
