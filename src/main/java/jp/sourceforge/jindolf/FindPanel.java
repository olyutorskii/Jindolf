/*
 * Find panel
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.JTextComponent;
import jp.sourceforge.jindolf.json.JsArray;
import jp.sourceforge.jindolf.json.JsObject;
import jp.sourceforge.jindolf.json.JsValue;

/**
 * 検索パネルGUI。
 */
@SuppressWarnings("serial")
public class FindPanel extends JDialog
        implements ActionListener,
                   ItemListener,
                   ChangeListener,
                   PropertyChangeListener {

    private static final String HIST_FILE = "searchHistory.json";
    private static final String FRAMETITLE = "発言検索 - " + Jindolf.TITLE;
    private static final String LABEL_REENTER = "再入力";
    private static final String LABEL_IGNORE = "無視して検索をキャンセル";

    private final JComboBox findBox = new JComboBox();
    private final JButton searchButton = new JButton("検索");
    private final JButton clearButton = new JButton("クリア");
    private final JCheckBox capitalSwitch =
            new JCheckBox("大文字/小文字を区別する");
    private final JCheckBox regexSwitch =
            new JCheckBox("正規表現");
    private final JCheckBox dotallSwitch =
            new JCheckBox("正規表現 \".\" を行末記号にもマッチさせる");
    private final JCheckBox multilineSwitch =
            new JCheckBox("正規表現 \"^\" や \"$\" を"
                         +"行末記号の前後に反応させる");
    private final JCheckBox bulkSearchSwitch =
            new JCheckBox("全日程を一括検索");
    private final JButton closeButton = new JButton("キャンセル");

    private final CustomModel model = new CustomModel();

    private JsObject loadedHistory = null;

    private boolean canceled = false;
    private RegexPattern regexPattern = null;

    /**
     * 検索パネルを生成する。
     * @param owner 親フレーム。
     */
    public FindPanel(Frame owner){
        super(owner, FRAMETITLE, true);

        GUIUtils.modifyWindowAttributes(this, true, false, true);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent event){
                actionCancel();
                return;
            }
        });

        design();

        this.findBox.setModel(this.model);
        this.findBox.setToolTipText("検索文字列を入力してください");
        this.findBox.setEditable(true);
        this.findBox.setRenderer(new CustomRenderer());
        this.findBox.setMaximumRowCount(15);

        ComboBoxEditor editor = this.findBox.getEditor();
        modifyComboBoxEditor(editor);
        this.findBox.addPropertyChangeListener("UI", this);

        this.searchButton.setToolTipText("発言内容を検索する");
        this.clearButton.setToolTipText("入力をクリアする");

        this.findBox.addItemListener(this);
        this.searchButton.addActionListener(this);
        this.clearButton.addActionListener(this);
        this.regexSwitch.addChangeListener(this);
        this.closeButton.addActionListener(this);

        setRegexPattern(null);

        return;
    }

    /**
     * デザイン・レイアウトを行う。
     */
    private void design(){
        Container content = getContentPane();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        content.setLayout(layout);

        constraints.insets = new Insets(2, 2, 2, 2);

        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 2;
        Border border =
                BorderFactory
                .createTitledBorder("検索文字列を入力してください");
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(this.findBox, BorderLayout.CENTER);
        panel.setBorder(border);
        content.add(panel, constraints);

        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.SOUTH;
        content.add(this.searchButton, constraints);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        content.add(this.clearButton, constraints);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        content.add(this.capitalSwitch, constraints);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        content.add(this.regexSwitch, constraints);

        JPanel regexPanel = new JPanel();
        regexPanel.setBorder(BorderFactory.createTitledBorder(""));
        regexPanel.setLayout(new GridLayout(2, 1));
        regexPanel.add(this.dotallSwitch);
        regexPanel.add(this.multilineSwitch);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        content.add(regexPanel, constraints);

        constraints.weightx = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        content.add(new JSeparator(), constraints);

        constraints.weightx = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        content.add(this.bulkSearchSwitch, constraints);

        constraints.weightx = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        content.add(new JSeparator(), constraints);

        constraints.weightx = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        content.add(this.closeButton, constraints);

        return;
    }

    /**
     * {@inheritDoc}
     * 検索ダイアログを表示・非表示する。
     * @param show 表示フラグ。真なら表示。{@inheritDoc}
     */
    @Override
    public void setVisible(boolean show){
        super.setVisible(show);
        getRootPane().setDefaultButton(this.searchButton);
        this.findBox.requestFocusInWindow();
        return;
    }

    /**
     * ダイアログが閉じられた原因を判定する。
     * @return キャンセルもしくはクローズボタンでダイアログが閉じられたらtrue
     */
    public boolean isCanceled(){
        return this.canceled;
    }

    /**
     * 一括検索が指定されたか否か返す。
     * @return 一括検索が指定されたらtrue
     */
    public boolean isBulkSearch(){
        return this.bulkSearchSwitch.isSelected();
    }

    /**
     * キャンセルボタン押下処理。
     * このモーダルダイアログを閉じる。
     */
    private void actionCancel(){
        this.canceled = true;
        setVisible(false);
        dispose();
        return;
    }

    /**
     * 検索ボタン押下処理。
     * このモーダルダイアログを閉じる。
     */
    private void actionSubmit(){
        Object selected = this.findBox.getSelectedItem();
        if(selected == null){
            this.regexPattern = null;
            return;
        }
        String edit = selected.toString();

        boolean isRegex = this.regexSwitch.isSelected();

        int flag = 0x00000000;
        if( ! this.capitalSwitch.isSelected() ){
            flag |= RegexPattern.IGNORECASEFLAG;
        }
        if(this.dotallSwitch.isSelected())    flag |= Pattern.DOTALL;
        if(this.multilineSwitch.isSelected()) flag |= Pattern.MULTILINE;

        try{
            this.regexPattern = new RegexPattern(edit, isRegex, flag);
        }catch(PatternSyntaxException e){
            this.regexPattern = null;
            if(showRegexError(e)){
                return;
            }
            actionCancel();
            return;
        }

        this.model.addHistory(this.regexPattern);

        this.canceled = false;
        setVisible(false);
        dispose();

        return;
    }

    /**
     * 正規表現パターン異常系のダイアログ表示。
     * @param e 正規表現構文エラー
     * @return 再入力が押されたらtrue。それ以外はfalse。
     */
    private boolean showRegexError(PatternSyntaxException e){
        String pattern = e.getPattern();

        String position = "";
        int index = e.getIndex();
        if(0 <= index && index <= pattern.length() - 1){
            char errChar = pattern.charAt(index);
            position = "エラーの発生箇所は、おおよそ"
                      + (index+1) + "文字目 [ " + errChar + " ] "
                      +"かその前後と推測されます。\n";
        }

        String message =
                "入力された検索文字列 [ " + pattern + " ] は"
                +"正しい正規表現として認識されませんでした。\n"
                +position
                +"正規表現の書き方は"
                +" [ http://java.sun.com/j2se/1.5.0/ja/docs/ja/api/"
                +"java/util/regex/Pattern.html#sum ] "
                +"を参照してください。\n"
                +"ただの文字列を検索したい場合は"
                +"「正規表現」のチェックボックスを外しましょう。\n"
                ;

        Object[] buttons = new Object[2];
        buttons[0] = LABEL_REENTER;
        buttons[1] = LABEL_IGNORE;
        Icon icon = null;

        int optionNo = JOptionPane.showOptionDialog(this,
                                                    message,
                                                    "不正な正規表現",
                                                    JOptionPane.YES_NO_OPTION,
                                                    JOptionPane.ERROR_MESSAGE,
                                                    icon,
                                                    buttons,
                                                    LABEL_REENTER);

        if(optionNo == JOptionPane.CLOSED_OPTION) return false;
        if(buttons[optionNo].equals(LABEL_REENTER)) return true;
        if(buttons[optionNo].equals(LABEL_IGNORE) ) return false;

        return true;
    }

    /**
     * 現時点での検索パターンを得る。
     * @return 検索パターン
     */
    public RegexPattern getRegexPattern(){
        return this.regexPattern;
    }

    /**
     * 検索パターンを設定する。
     * @param pattern 検索パターン
     */
    public final void setRegexPattern(RegexPattern pattern){
        if(pattern == null) this.regexPattern = CustomModel.INITITEM;
        else                this.regexPattern = pattern;

        String edit = this.regexPattern.getEditSource();
        this.findBox.getEditor().setItem(edit);

        this.regexSwitch.setSelected(this.regexPattern.isRegex());

        int initflag = this.regexPattern.getRegexFlag();
        this.capitalSwitch.setSelected(
                (initflag & RegexPattern.IGNORECASEFLAG) == 0);
        this.dotallSwitch.setSelected((initflag & Pattern.DOTALL) != 0);
        this.multilineSwitch.setSelected((initflag & Pattern.MULTILINE) != 0);

        maskRegexUI();

        return;
    }

    /**
     * {@inheritDoc}
     * ボタン操作時にリスナとして呼ばれる。
     * @param event イベント {@inheritDoc}
     */
    public void actionPerformed(ActionEvent event){
        Object source = event.getSource();
        if(source == this.closeButton){
            actionCancel();
        }else if(source == this.searchButton){
            actionSubmit();
        }else if(source == this.clearButton){
            this.findBox.getEditor().setItem("");
            this.findBox.requestFocusInWindow();
        }
        return;
    }

    /**
     * {@inheritDoc}
     * コンボボックスのアイテム選択リスナ。
     * @param event アイテム選択イベント {@inheritDoc}
     */
    public void itemStateChanged(ItemEvent event){
        int stateChange = event.getStateChange();
        if(stateChange != ItemEvent.SELECTED) return;

        Object item = event.getItem();
        if( ! (item instanceof RegexPattern) ) return;
        RegexPattern regex = (RegexPattern) item;

        setRegexPattern(regex);

        return;
    }

    /**
     * {@inheritDoc}
     * チェックボックス操作のリスナ。
     * @param event チェックボックス操作イベント {@inheritDoc}
     */
    public void stateChanged(ChangeEvent event){
        if(event.getSource() != this.regexSwitch) return;
        maskRegexUI();
        return;
    }

    /**
     * 正規表現でしか使わないUIのマスク処理。
     */
    private void maskRegexUI(){
        boolean isRegex = this.regexSwitch.isSelected();
        this.dotallSwitch   .setEnabled(isRegex);
        this.multilineSwitch.setEnabled(isRegex);
        return;
    }

    /**
     * {@inheritDoc}
     * コンボボックスのUI変更通知を受け取るリスナ。
     * @param event UI差し替えイベント {@inheritDoc}
     */
    public void propertyChange(PropertyChangeEvent event){
        if( ! event.getPropertyName().equals("UI") ) return;
        if(event.getSource() != this.findBox) return;

        ComboBoxEditor editor = this.findBox.getEditor();
        modifyComboBoxEditor(editor);

        return;
    }

    /**
     * コンボボックスエディタを修飾する。
     * マージン修飾と等幅フォントをいじる。
     * @param editor エディタ
     */
    private void modifyComboBoxEditor(ComboBoxEditor editor){
        if(editor == null) return;

        Component editComp = editor.getEditorComponent();
        if(editComp == null) return;

        if(editComp instanceof JTextComponent){
            JTextComponent textEditor = (JTextComponent) editComp;
            textEditor.setComponentPopupMenu(new TextPopup());
        }

        GUIUtils.addMargin(editComp, 1, 4, 1, 4);

        return;
    }

    /**
     * 検索履歴をロードする。
     */
    public void loadHistory(){
        JsValue value = ConfigFile.loadJson(new File(HIST_FILE));
        if(value == null) return;

        if( ! (value instanceof JsObject) ) return;
        JsObject root = (JsObject) value;

        value = root.getValue("history");
        if( ! (value instanceof JsArray) ) return;
        JsArray array = (JsArray) value;

        for(JsValue elem : array){
            if( ! (elem instanceof JsObject) ) continue;
            JsObject regObj = (JsObject) elem;
            RegexPattern regex = RegexPattern.decodeJson(regObj);
            if(regex == null) continue;
            this.model.addHistory(regex);
        }

        this.loadedHistory = root;

        return;
    }

    /**
     * 検索履歴をセーブする。
     */
    public void saveHistory(){
        AppSetting setting = Jindolf.getAppSetting();
        if( ! setting.useConfigPath() ) return;
        File configPath = setting.getConfigPath();
        if(configPath == null) return;

        JsObject root = new JsObject();
        JsArray array = new JsArray();
        root.putValue("history", array);

        List<RegexPattern> history = this.model.getOriginalHistoryList();
        history = new ArrayList<RegexPattern>(history);
        Collections.reverse(history);
        for(RegexPattern regex : history){
            JsObject obj = RegexPattern.encodeJson(regex);
            array.add(obj);
        }

        if(this.loadedHistory != null){
            if(this.loadedHistory.equals(root)) return;
        }

        ConfigFile.saveJson(new File(HIST_FILE), root);

        return;
    }

    /**
     * コンボボックスの独自レンダラ。
     */
    private static class CustomRenderer extends DefaultListCellRenderer{

        /**
         * コンストラクタ。
         */
        public CustomRenderer(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
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
            if(value instanceof JSeparator){
                return (JSeparator) value;
            }

            JLabel superLabel =
                    (JLabel) super.getListCellRendererComponent(list,
                                                                value,
                                                                index,
                                                                isSelected,
                                                                cellHasFocus);

            if(value instanceof RegexPattern){
                RegexPattern regexPattern = (RegexPattern) value;
                String text;
                if(regexPattern.isRegex()){
                    text = "[R]　" + regexPattern.getEditSource();
                }else{
                    text = regexPattern.getEditSource();
                }
                text += regexPattern.getComment();

                superLabel.setText(text);
            }

            GUIUtils.addMargin(superLabel, 1, 4, 1, 4);

            return superLabel;
        }
    }

    /**
     * コンボボックスの独自データモデル。
     */
    private static class CustomModel implements ComboBoxModel{

        private static final int HISTORY_MAX = 7;
        private static final RegexPattern INITITEM =
            new RegexPattern(
                "", false, RegexPattern.IGNORECASEFLAG | Pattern.DOTALL);
        private static final List<RegexPattern> PREDEF_PATTERN_LIST =
                new LinkedList<RegexPattern>();

        static{
            PREDEF_PATTERN_LIST.add(
                    new RegexPattern("【[^】]*】",
                                     true,
                                     Pattern.DOTALL,
                                     "     ※ 重要事項") );
            PREDEF_PATTERN_LIST.add(
                    new RegexPattern("[■●▼★□○▽☆〇◯∇]",
                                     true,
                                     Pattern.DOTALL,
                                     "     ※ 議題") );
            PREDEF_PATTERN_LIST.add(
                    new RegexPattern("Jindolf",
                                     false,
                                     RegexPattern.IGNORECASEFLAG,
                                     "     ※ 宣伝") );
        }

        private final List<RegexPattern> history =
                new LinkedList<RegexPattern>();
        private final JSeparator separator1st = new JSeparator();
        private final JSeparator separator2nd = new JSeparator();
        private Object selected;
        private final EventListenerList listenerList =
                new EventListenerList();

        /**
         * コンストラクタ。
         */
        public CustomModel(){
            super();
            return;
        }

        /**
         * {@inheritDoc}
         * @return {@inheritDoc}
         */
        public Object getSelectedItem(){
            return this.selected;
        }

        /**
         * {@inheritDoc}
         * @param item {@inheritDoc}
         */
        public void setSelectedItem(Object item){
            if(item instanceof JSeparator) return;
            this.selected = item;
            return;
        }

        /**
         * {@inheritDoc}
         * @param index {@inheritDoc}
         * @return {@inheritDoc}
         */
        public Object getElementAt(int index){
            int historySize = this.history.size();

            if(index == 0){
                return INITITEM;
            }
            if(index == 1){
                return this.separator1st;
            }
            if(2 <= index && index <= 1 + historySize){
                return this.history.get(index - 2);
            }
            if(index == historySize + 2){
                return this.separator2nd;
            }
            if(historySize + 3 <= index){
                return PREDEF_PATTERN_LIST.get(index - 1
                                                     - 1
                                                     - historySize
                                                     - 1 );
            }

            return null;
        }

        /**
         * {@inheritDoc}
         * @return {@inheritDoc}
         */
        public int getSize(){
            int size = 1;
            size += 1;         // first separator
            size += this.history.size();
            size += 1;         // second separator
            size += PREDEF_PATTERN_LIST.size();
            return size;
        }

        /**
         * {@inheritDoc}
         * @param listener {@inheritDoc}
         */
        public void addListDataListener(ListDataListener listener){
            this.listenerList.add(ListDataListener.class, listener);
            return;
        }

        /**
         * {@inheritDoc}
         * @param listener {@inheritDoc}
         */
        public void removeListDataListener(ListDataListener listener){
            this.listenerList.remove(ListDataListener.class, listener);
            return;
        }

        /**
         * 検索履歴ヒストリ追加。
         * @param regexPattern 検索履歴
         */
        public void addHistory(RegexPattern regexPattern){
            if(regexPattern == null) return;
            if(regexPattern.equals(INITITEM)) return;
            if(PREDEF_PATTERN_LIST.contains(regexPattern)) return;
            if(this.history.contains(regexPattern)){
                this.history.remove(regexPattern);
            }

            this.history.add(0, regexPattern);

            while(this.history.size() > HISTORY_MAX){
                this.history.remove(HISTORY_MAX);
            }

            fire();

            return;
        }

        /**
         * プリセットでない検索ヒストリリストを返す。
         * @return 検索ヒストリリスト
         */
        public List<RegexPattern> getOriginalHistoryList(){
            return Collections.unmodifiableList(this.history);
        }

        /**
         * ヒストリ追加イベント発火。
         */
        private void fire(){
            ListDataEvent event =
                    new ListDataEvent(this,
                                      ListDataEvent.CONTENTS_CHANGED,
                                      0, getSize() - 1 );
            ListDataListener[] listeners =
                    this.listenerList.getListeners(ListDataListener.class);
            for(ListDataListener listener : listeners){
                listener.contentsChanged(event);
            }
            return;
        }
    }

    // TODO ブックマーク機能との統合
}
