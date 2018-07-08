/*
 * action manager
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import jp.sfjp.jindolf.ResourceManager;
import jp.sfjp.jindolf.VerInfo;
import jp.sfjp.jindolf.util.GUIUtils;

/**
 * メニュー、ボタン、その他各種Actionを伴うイベントを生成する
 * コンポーネントの一括管理。
 */
public class ActionManager{

    /** アクション{@value}。 */
    public static final String CMD_ACCOUNT    = "ACCOUNT";
    /** アクション{@value}。 */
    public static final String CMD_EXIT       = "EXIT";
    /** アクション{@value}。 */
    public static final String CMD_COPY       = "COPY";
    /** アクション{@value}。 */
    public static final String CMD_SHOWFIND   = "SHOWFIND";
    /** アクション{@value}。 */
    public static final String CMD_SEARCHNEXT = "SEARCHNEXT";
    /** アクション{@value}。 */
    public static final String CMD_SEARCHPREV = "SEARCHPREV";
    /** アクション{@value}。 */
    public static final String CMD_ALLPERIOD  = "ALLPERIOD";
    /** アクション{@value}。 */
    public static final String CMD_SHOWDIGEST = "DIGEST";
    /** アクション{@value}。 */
    public static final String CMD_WEBVILL    = "WEBVILL";
    /** アクション{@value}。 */
    public static final String CMD_WEBCAST    = "WEBCAST";
    /** アクション{@value}。 */
    public static final String CMD_WEBWIKI    = "WEBWIKI";
    /** アクション{@value}。 */
    public static final String CMD_RELOAD     = "RELOAD";
    /** アクション{@value}。 */
    public static final String CMD_DAYSUMMARY = "DAYSUMMARY";
    /** アクション{@value}。 */
    public static final String CMD_DAYEXPCSV  = "DAYEXPCSV";
    /** アクション{@value}。 */
    public static final String CMD_WEBDAY     = "WEBDAY";
    /** アクション{@value}。 */
    public static final String CMD_OPTION     = "OPTION";
    /** アクション{@value}。 */
    public static final String CMD_LANDF      = "LANDF";
    /** アクション{@value}。 */
    public static final String CMD_SHOWFILT   = "SHOWFILT";
    /** アクション{@value}。 */
    public static final String CMD_SHOWEDIT   = "SHOWEDIT";
    /** アクション{@value}。 */
    public static final String CMD_SHOWLOG    = "SHOWLOG";
    /** アクション{@value}。 */
    public static final String CMD_HELPDOC    = "HELPDOC";
    /** アクション{@value}。 */
    public static final String CMD_SHOWPORTAL = "SHOWPORTAL";
    /** アクション{@value}。 */
    public static final String CMD_ABOUT      = "ABOUT";

    /** アクション{@value}。 */
    public static final String CMD_COPYTALK    = "COPYTALK";
    /** アクション{@value}。 */
    public static final String CMD_JUMPANCHOR  = "JUMPANCHOR";
    /** アクション{@value}。 */
    public static final String CMD_WEBTALK     = "WEBTALK";
    /** アクション{@value}。 */
    public static final String CMD_SWITCHORDER = "SWITCHORDER";
    /** アクション{@value}。 */
    public static final String CMD_VILLAGELIST = "VILLAGELIST";
    /** アクション{@value}。 */
    public static final String CMD_FONTSIZESEL = "FONTSIZESEL";

    /** WWWアイコン。 */
    public static final Icon ICON_WWW = GUIUtils.getWWWIcon();
    /** 検索アイコン。 */
    public static final Icon ICON_FIND;
    /** 前検索アイコン。 */
    public static final Icon ICON_SEARCH_PREV;
    /** 次検索アイコン。 */
    public static final Icon ICON_SEARCH_NEXT;
    /** リロードアイコン。 */
    public static final Icon ICON_RELOAD;
    /** フィルタアイコン。 */
    public static final Icon ICON_FILTER;
    /** 発言エディタアイコン。 */
    public static final Icon ICON_EDITOR;

    private static final KeyStroke KEY_F1 = KeyStroke.getKeyStroke("F1");
    private static final KeyStroke KEY_F3 = KeyStroke.getKeyStroke("F3");
    private static final KeyStroke KEY_SHIFT_F3 =
            KeyStroke.getKeyStroke("shift F3");
    private static final KeyStroke KEY_F5 = KeyStroke.getKeyStroke("F5");
    private static final KeyStroke KEY_CTRL_F =
            KeyStroke.getKeyStroke("ctrl F");

    static{
        ICON_FIND =
            ResourceManager.getButtonIcon("resources/image/tb_find.png");

        ICON_SEARCH_PREV =
            ResourceManager.getButtonIcon("resources/image/tb_findprev.png");

        ICON_SEARCH_NEXT =
            ResourceManager.getButtonIcon("resources/image/tb_findnext.png");

        ICON_RELOAD =
            ResourceManager.getButtonIcon("resources/image/tb_reload.png");

        ICON_FILTER =
            ResourceManager.getButtonIcon("resources/image/tb_filter.png");

        ICON_EDITOR =
            ResourceManager.getButtonIcon("resources/image/tb_editor.png");
    }

    private final Set<AbstractButton> actionItems =
            new HashSet<>();
    private final Map<String, JMenuItem> namedMenuItems =
            new HashMap<>();
    private final Map<String, JButton> namedToolButtons =
            new HashMap<>();

    private final JMenuBar menuBar;

    private final JMenu menuFile;
    private final JMenu menuEdit;
    private final JMenu menuVillage;
    private final JMenu menuDay;
    private final JMenu menuPreference;
    private final JMenu menuTool;
    private final JMenu menuHelp;

    private final JMenu menuLook;
    private final ButtonGroup landfGroup = new ButtonGroup();
    private final Map<ButtonModel, String> landfMap =
        new HashMap<>();

    private final JToolBar browseToolBar;

    /**
     * コンストラクタ。
     */
    public ActionManager(){
        this.menuFile       = buildMenu("Jindolf",  KeyEvent.VK_F);
        this.menuEdit       = buildMenu("編集",     KeyEvent.VK_E);
        this.menuVillage    = buildMenu("村",       KeyEvent.VK_V);
        this.menuDay        = buildMenu("日",       KeyEvent.VK_D);
        this.menuPreference = buildMenu("設定",     KeyEvent.VK_P);
        this.menuTool       = buildMenu("ツール",   KeyEvent.VK_T);
        this.menuHelp       = buildMenu("ヘルプ",   KeyEvent.VK_H);

        this.menuLook = buildLookAndFeelMenu("ルック&フィール", KeyEvent.VK_L);

        buildMenuItem(CMD_ACCOUNT, "アカウント管理", KeyEvent.VK_M);
        buildMenuItem(CMD_EXIT, "終了", KeyEvent.VK_X);
        buildMenuItem(CMD_COPY, "選択範囲をコピー", KeyEvent.VK_C);
        buildMenuItem(CMD_SHOWFIND, "検索...", KeyEvent.VK_F);
        buildMenuItem(CMD_SEARCHNEXT, "次候補", KeyEvent.VK_N);
        buildMenuItem(CMD_SEARCHPREV, "前候補", KeyEvent.VK_P);
        buildMenuItem(CMD_ALLPERIOD, "全日程の一括読み込み", KeyEvent.VK_R);
        buildMenuItem(CMD_SHOWDIGEST, "村のダイジェストを表示...",
                KeyEvent.VK_D);
        buildMenuItem(CMD_WEBVILL, "この村をブラウザで表示...", KeyEvent.VK_N);
        buildMenuItem(CMD_WEBWIKI,
                      "まとめサイトの村ページを表示...", KeyEvent.VK_M);
        buildMenuItem(CMD_RELOAD, "この日を強制リロード", KeyEvent.VK_R);
        buildMenuItem(CMD_DAYSUMMARY, "この日の発言を集計...", KeyEvent.VK_D);
        buildMenuItem(CMD_DAYEXPCSV, "CSVへエクスポート...", KeyEvent.VK_C);
        buildMenuItem(CMD_WEBDAY, "この日をブラウザで表示...", KeyEvent.VK_B);
        buildMenuItem(CMD_OPTION, "オプション...", KeyEvent.VK_O);
        buildMenuItem(CMD_SHOWFILT, "発言フィルタ", KeyEvent.VK_F);
        buildMenuItem(CMD_SHOWEDIT, "発言エディタ", KeyEvent.VK_E);
        buildMenuItem(CMD_SHOWLOG, "ログ表示", KeyEvent.VK_S);
        buildMenuItem(CMD_HELPDOC, "ヘルプ表示", KeyEvent.VK_H);
        buildMenuItem(CMD_SHOWPORTAL, "ポータルサイト...", KeyEvent.VK_P);
        buildMenuItem(CMD_ABOUT, VerInfo.TITLE + "について...", KeyEvent.VK_A);

        buildToolButton(CMD_RELOAD, "選択中の日を強制リロード", ICON_RELOAD);
        buildToolButton(CMD_SHOWFIND,   "検索",     ICON_FIND);
        buildToolButton(CMD_SEARCHPREV, "↑前候補", ICON_SEARCH_PREV);
        buildToolButton(CMD_SEARCHNEXT, "↓次候補", ICON_SEARCH_NEXT);
        buildToolButton(CMD_SHOWFILT, "発言フィルタ", ICON_FILTER);
        buildToolButton(CMD_SHOWEDIT, "発言エディタ", ICON_EDITOR);

        getMenuItem(CMD_SHOWPORTAL).setIcon(ICON_WWW);
        getMenuItem(CMD_WEBVILL)   .setIcon(ICON_WWW);
        getMenuItem(CMD_WEBWIKI)   .setIcon(ICON_WWW);
        getMenuItem(CMD_WEBDAY)    .setIcon(ICON_WWW);
        getMenuItem(CMD_SHOWFIND)  .setIcon(ICON_FIND);
        getMenuItem(CMD_SEARCHPREV).setIcon(ICON_SEARCH_PREV);
        getMenuItem(CMD_SEARCHNEXT).setIcon(ICON_SEARCH_NEXT);
        getMenuItem(CMD_SHOWFILT)  .setIcon(ICON_FILTER);
        getMenuItem(CMD_SHOWEDIT)  .setIcon(ICON_EDITOR);

        registKeyAccelerator();

        this.menuBar       = buildMenuBar();
        this.browseToolBar = buildBrowseToolBar();

        appearVillageImpl(false);
        appearPeriodImpl(false);

        return;
    }

    /**
     * メニューを生成する。
     * @param label メニューラベル
     * @param nemonic ニモニックキー
     * @return メニュー
     */
    private JMenu buildMenu(String label, int nemonic){
        JMenu result = new JMenu();

        String keyText = label + "(" + KeyEvent.getKeyText(nemonic) + ")";

        result.setText(keyText);
        result.setMnemonic(nemonic);

        return result;
    }

    /**
     * メニューアイテムを生成する。
     * @param command アクションコマンド名
     * @param label メニューラベル
     * @param nemonic ニモニックキー
     * @return メニューアイテム
     */
    private JMenuItem buildMenuItem(String command,
                                      String label,
                                      int nemonic ){
        JMenuItem result = new JMenuItem();

        String keyText = label + "(" + KeyEvent.getKeyText(nemonic) + ")";

        result.setActionCommand(command);
        result.setText(keyText);
        result.setMnemonic(nemonic);

        this.actionItems.add(result);
        this.namedMenuItems.put(command, result);

        return result;
    }

    /**
     * ツールボタンを生成する。
     * @param command アクションコマンド名
     * @param tooltip ツールチップ文字列
     * @param icon アイコン画像
     * @return ツールボタン
     */
    private JButton buildToolButton(String command,
                                      String tooltip,
                                      Icon icon){
        JButton result = new JButton();

        result.setIcon(icon);
        result.setToolTipText(tooltip);
        result.setMargin(new Insets(1, 1, 1, 1));
        result.setActionCommand(command);

        this.actionItems.add(result);
        this.namedToolButtons.put(command, result);

        return result;
    }

    /**
     * L&F 一覧メニューを作成する。
     * @param label メニューラベル
     * @param nemonic ニモニックキー
     * @return L&F 一覧メニュー
     */
    private JMenu buildLookAndFeelMenu(String label, int nemonic){
        JMenu result = buildMenu(label, nemonic);

        LookAndFeel currentLookAndFeel = UIManager.getLookAndFeel();
        String currentName = currentLookAndFeel.getClass().getName();
        JMenuItem matchedButton = null;

        UIManager.LookAndFeelInfo[] landfs =
                UIManager.getInstalledLookAndFeels();
        for(UIManager.LookAndFeelInfo lafInfo : landfs){
            String name      = lafInfo.getName();
            String className = lafInfo.getClassName();

            JRadioButtonMenuItem button = new JRadioButtonMenuItem(name);
            button.setActionCommand(CMD_LANDF);

            if(className.equals(currentName)) matchedButton = button;

            this.actionItems.add(button);
            this.landfGroup.add(button);
            this.landfMap.put(button.getModel(), className);

            result.add(button);
        }

        if(matchedButton != null) matchedButton.setSelected(true);

        return result;
    }

    /**
     * アクセラレータの設定。
     */
    private void registKeyAccelerator(){
        getMenuItem(CMD_HELPDOC)    .setAccelerator(KEY_F1);
        getMenuItem(CMD_SHOWFIND)   .setAccelerator(KEY_CTRL_F);
        getMenuItem(CMD_SEARCHNEXT) .setAccelerator(KEY_F3);
        getMenuItem(CMD_SEARCHPREV) .setAccelerator(KEY_SHIFT_F3);
        getMenuItem(CMD_RELOAD)     .setAccelerator(KEY_F5);
        return;
    }

    /**
     * アクションコマンド名からメニューアイテムを探す。
     * @param command アクションコマンド名
     * @return メニューアイテム
     */
    private JMenuItem getMenuItem(String command){
        JMenuItem result = this.namedMenuItems.get(command);
        return result;
    }

    /**
     * アクションコマンド名からツールボタンを探す。
     * @param command アクションコマンド名
     * @return ツールボタン
     */
    private JButton getToolButton(String command){
        JButton result = this.namedToolButtons.get(command);
        return result;
    }

    /**
     * 現在メニューで選択中のL&amp;Fのクラス名を返す。
     * @return L&amp;F クラス名
     */
    public String getSelectedLookAndFeel(){
        ButtonModel selected = this.landfGroup.getSelection();
        if(selected == null) return null;
        String className = this.landfMap.get(selected);
        return className;
    }

    /**
     * 全てのボタンにアクションリスナーを登録する。
     * @param listener アクションリスナー
     */
    public void addActionListener(ActionListener listener){
        for(AbstractButton button : this.actionItems){
            button.addActionListener(listener);
        }
        return;
    }

    /**
     * メニューバーを生成する。
     * @return メニューバー
     */
    private JMenuBar buildMenuBar(){
        this.menuFile.add(getMenuItem(CMD_ACCOUNT));
        this.menuFile.addSeparator();
        this.menuFile.add(getMenuItem(CMD_EXIT));

        this.menuEdit.add(getMenuItem(CMD_COPY));
        this.menuEdit.addSeparator();
        this.menuEdit.add(getMenuItem(CMD_SHOWFIND));
        this.menuEdit.add(getMenuItem(CMD_SEARCHPREV));
        this.menuEdit.add(getMenuItem(CMD_SEARCHNEXT));

        this.menuVillage.add(getMenuItem(CMD_ALLPERIOD));
        this.menuVillage.add(getMenuItem(CMD_SHOWDIGEST));
        this.menuVillage.addSeparator();
        this.menuVillage.add(getMenuItem(CMD_WEBVILL));
        this.menuVillage.add(getMenuItem(CMD_WEBWIKI));

        this.menuDay.add(getMenuItem(CMD_RELOAD));
        this.menuDay.add(getMenuItem(CMD_DAYSUMMARY));
        this.menuDay.add(getMenuItem(CMD_DAYEXPCSV));
        this.menuDay.addSeparator();
        this.menuDay.add(getMenuItem(CMD_WEBDAY));

        this.menuPreference.add(getMenuItem(CMD_OPTION));
        this.menuPreference.addSeparator();
        this.menuPreference.add(this.menuLook);

        this.menuTool.add(getMenuItem(CMD_SHOWFILT));
        this.menuTool.add(getMenuItem(CMD_SHOWEDIT));
        this.menuTool.add(getMenuItem(CMD_SHOWLOG));

        this.menuHelp.add(getMenuItem(CMD_HELPDOC));
        this.menuHelp.addSeparator();
        this.menuHelp.add(getMenuItem(CMD_SHOWPORTAL));
        this.menuHelp.add(getMenuItem(CMD_ABOUT));

        JMenuBar bar = new JMenuBar();

        bar.add(this.menuFile);
        bar.add(this.menuEdit);
        bar.add(this.menuVillage);
        bar.add(this.menuDay);
        bar.add(this.menuPreference);
        bar.add(this.menuTool);
        bar.add(this.menuHelp);

        return bar;
    }

    /**
     * メニューバーを取得する。
     * @return メニューバー
     */
    public JMenuBar getMenuBar(){
        return this.menuBar;
    }

    /**
     * ブラウザ用ツールバーの生成を行う。
     * @return ツールバー
     */
    private JToolBar buildBrowseToolBar(){
        JToolBar toolBar = new JToolBar();

        toolBar.add(getToolButton(CMD_RELOAD));
        toolBar.addSeparator();
        toolBar.add(getToolButton(CMD_SHOWFIND));
        toolBar.add(getToolButton(CMD_SEARCHNEXT));
        toolBar.add(getToolButton(CMD_SEARCHPREV));
        toolBar.addSeparator();
        toolBar.add(getToolButton(CMD_SHOWFILT));
        toolBar.add(getToolButton(CMD_SHOWEDIT));

        return toolBar;
    }

    /**
     * ブラウザ用ツールバーを取得する。
     * @return ツールバー
     */
    public JToolBar getBrowseToolBar(){
        return this.browseToolBar;
    }

    /**
     * Periodが表示されているか通知を受ける。
     * @param appear 表示されているときはtrue
     */
    private void appearPeriodImpl(boolean appear){
        if(appear) appearVillageImpl(appear);

        this.menuEdit.setEnabled(appear);
        this.menuDay .setEnabled(appear);

        getToolButton(CMD_RELOAD)     .setEnabled(appear);
        getToolButton(CMD_SHOWFIND)   .setEnabled(appear);
        getToolButton(CMD_SEARCHNEXT) .setEnabled(appear);
        getToolButton(CMD_SEARCHPREV) .setEnabled(appear);

        return;
    }

    /**
     * Periodが表示されているか通知を受ける。
     * @param appear 表示されているときはtrue
     */
    public void appearPeriod(boolean appear){
        appearPeriodImpl(appear);
        return;
    }

    /**
     * 村が表示されているか通知を受ける。
     * @param appear 表示されているときはtrue
     */
    private void appearVillageImpl(boolean appear){
        if( ! appear) appearPeriodImpl(appear);

        this.menuVillage.setEnabled(appear);

        return;
    }

    /**
     * 村が表示されているか通知を受ける。
     * @param appear 表示されているときはtrue
     */
    public void appearVillage(boolean appear){
        appearVillageImpl(appear);
        return;
    }

}
