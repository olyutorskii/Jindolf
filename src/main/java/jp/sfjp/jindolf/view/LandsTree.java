/*
 * Lands tree container
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.BorderLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import jp.sfjp.jindolf.ResourceManager;
import jp.sfjp.jindolf.data.Land;
import jp.sfjp.jindolf.data.LandsTreeModel;

/**
 * 国一覧Tree周辺コンポーネント群。
 *
 * <p>昇順/降順トグルボタン、村一覧リロードボタン、
 * 人狼BBSサーバ国および村一覧ツリーから構成される。
 */
@SuppressWarnings("serial")
public class LandsTree extends JPanel{

    private static final String TIP_ASCEND =
            "押すと降順に";
    private static final String TIP_DESCEND =
            "押すと昇順に";
    private static final String TIP_ORDER =
            "選択中の国の村一覧を読み込み直す";

    private static final String RES_DIR = "resources/image/";
    private static final String RES_ASCEND  = RES_DIR + "tb_ascend.png";
    private static final String RES_DESCEND = RES_DIR + "tb_descend.png";
    private static final String RES_RELOAD  = RES_DIR + "tb_reload.png";

    private static final Icon ICON_ASCEND;
    private static final Icon ICON_DESCEND;
    private static final Icon ICON_RELOAD;

    static{
        ICON_ASCEND  = ResourceManager.getButtonIcon(RES_ASCEND);
        ICON_DESCEND = ResourceManager.getButtonIcon(RES_DESCEND);
        ICON_RELOAD  = ResourceManager.getButtonIcon(RES_RELOAD);
    }


    private final JButton orderButton;
    private final JButton reloadButton;
    private final JTree treeView;

    private boolean ascending = false;


    /**
     * コンストラクタ。
     */
    public LandsTree(){
        super();

        this.orderButton = new JButton();
        this.reloadButton = new JButton();
        this.treeView = new JTree();

        TreeSelectionModel selModel = this.treeView.getSelectionModel();
        selModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        setupObserver();

        decorateButtons();
        design();

        return;
    }


    /**
     * 各種監視Observerの登録。
     */
    private void setupObserver(){
        this.orderButton.addActionListener((ev) -> {
            toggleTreeOrder();
        });

        this.treeView.addTreeSelectionListener((ev) -> {
            updateReloadButton(ev);
        });

        this.reloadButton.setActionCommand(ActionManager.CMD_VILLAGELIST);

        return;
    }

    /**
     * ボタン群を装飾する。
     */
    private void decorateButtons(){
        this.orderButton.setIcon(ICON_DESCEND);
        this.orderButton.setToolTipText(TIP_DESCEND);
        this.orderButton.setMargin(new Insets(1, 1, 1, 1));

        this.reloadButton.setIcon(ICON_RELOAD);
        this.reloadButton.setToolTipText(TIP_ORDER);
        this.reloadButton.setMargin(new Insets(1, 1, 1, 1));

        return;
    }

    /**
     * GUIパーツのデザインを行う。
     */
    private void design(){
        BorderLayout layout = new BorderLayout();
        setLayout(layout);

        JToolBar toolBar = new JToolBar();
        toolBar.add(this.orderButton);
        toolBar.add(this.reloadButton);

        JComponent landSelector = createLandSelector();

        add(toolBar,      BorderLayout.NORTH);
        add(landSelector, BorderLayout.CENTER);

        return;
    }

    /**
     * 国村選択ツリーコンポーネントを生成する。
     * @return 国村選択ツリーコンポーネント
     */
    private JComponent createLandSelector(){
        this.treeView.setRootVisible(false);
        this.treeView.setCellRenderer(new VillageIconRenderer());

        Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        this.treeView.setBorder(border);

        JScrollPane landSelector = new JScrollPane(this.treeView);

        return landSelector;
    }

    /**
     * リロードボタンを返す。
     * @return リロードボタン
     */
    public JButton getReloadVillageListButton(){
        return this.reloadButton;
    }

    /**
     * 国村選択ツリービューを返す。
     * @return 国村選択ツリービュー
     */
    public JTree getTreeView(){
        return this.treeView;
    }

    /**
     * 指定した国を展開する。
     * @param land 国
     */
    public void expandLand(Land land){
        TreeModel model = this.treeView.getModel();
        Object root = model.getRoot();
        Object[] path = {root, land};
        TreePath treePath = new TreePath(path);
        this.treeView.expandPath(treePath);
        return;
    }

    /**
     * 管理下のLandsTreeModelを返す。
     *
     * @return LandsTreeModel
     */
    private LandsTreeModel getLandsModel(){
        TreeModel model = this.treeView.getModel();
        if(model instanceof LandsTreeModel){
            return (LandsTreeModel) model;
        }
        return null;
    }

    /**
     * Tree表示順を反転させる。
     *
     * <p>昇順/降順ボタンも切り替わる。
     *
     * <p>選択中のツリー要素があれば選択は保持される。
     *
     * @return 反転後が昇順ならtrue
     */
    private boolean toggleTreeOrder(){
        this.ascending = ! this.ascending;

        String newTip;
        Icon newIcon;
        if(this.ascending){
            newTip = TIP_ASCEND;
            newIcon = ICON_ASCEND;
        }else{
            newTip = TIP_DESCEND;
            newIcon = ICON_DESCEND;
        }
        this.orderButton.setToolTipText(newTip);
        this.orderButton.setIcon(newIcon);

        TreePath lastPath = this.treeView.getSelectionPath();

        LandsTreeModel model = getLandsModel();
        if(model != null){
            model.setAscending(this.ascending);
        }

        if(lastPath != null){
            this.treeView.setSelectionPath(lastPath);
            this.treeView.scrollPathToVisible(lastPath);
        }

        return this.ascending;
    }

    /**
     * ツリー選択状況によってリロードボタンの状態を変更する。
     *
     * <p>国がツリー選択された状況でのみリロードボタンは有効になる。
     * その他の状況では無効に。
     *
     * @param event ツリー選択状況
     */
    private void updateReloadButton(TreeSelectionEvent event){
        boolean reloadEnable = false;

        TreePath path = event.getNewLeadSelectionPath();
        if(path != null){
            Object selObj = path.getLastPathComponent();
            if(selObj instanceof Land) reloadEnable = true;
        }

        this.reloadButton.setEnabled(reloadEnable);

        return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUI(){
        super.updateUI();
        if(this.treeView != null){
            this.treeView.setCellRenderer(new VillageIconRenderer());
        }
        return;
    }

}
