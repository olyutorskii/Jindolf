/*
 * Lands tree container
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import jp.sfjp.jindolf.ResourceManager;
import jp.sfjp.jindolf.data.Land;
import jp.sfjp.jindolf.data.LandsModel;

/**
 * 国一覧Tree周辺コンポーネント群。
 */
@SuppressWarnings("serial")
public class LandsTree
        extends JPanel
        implements ActionListener, TreeSelectionListener{

    private static final String TIP_ASCEND = "押すと降順に";
    private static final String TIP_DESCEND = "押すと昇順に";
    private static final String TIP_ORDER = "選択中の国の村一覧を読み込み直す";
    private static final ImageIcon ICON_ASCEND;
    private static final ImageIcon ICON_DESCEND;

    static{
        ICON_ASCEND =
            ResourceManager.getImageIcon("resources/image/tb_ascend.png");
        ICON_DESCEND =
            ResourceManager.getImageIcon("resources/image/tb_descend.png");
    }

    private final JButton orderButton = new JButton();
    private final JButton reloadButton = new JButton();
    private final JTree treeView = new JTree();

    private boolean ascending = false;

    /**
     * コンストラクタ。
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public LandsTree(){
        super();

        design();

        this.orderButton.setIcon(ICON_DESCEND);
        this.orderButton.setToolTipText(TIP_DESCEND);
        this.orderButton.setMargin(new Insets(1, 1, 1, 1));
        this.orderButton.setActionCommand(ActionManager.CMD_SWITCHORDER);
        this.orderButton.addActionListener(this);

        ImageIcon icon =
                ResourceManager.getImageIcon("resources/image/tb_reload.png");
        this.reloadButton.setIcon(icon);
        this.reloadButton.setToolTipText(TIP_ORDER);
        this.reloadButton.setMargin(new Insets(1, 1, 1, 1));
        this.reloadButton.setActionCommand(ActionManager.CMD_VILLAGELIST);

        TreeSelectionModel selModel = this.treeView.getSelectionModel();
        selModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.treeView.addTreeSelectionListener(this);

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
     * 管理下のLandsModelを返す。
     * @return LandsModel
     */
    private LandsModel getLandsModel(){
        TreeModel model = this.treeView.getModel();
        if(model instanceof LandsModel){
            return (LandsModel) model;
        }
        return null;
    }

    /**
     * Tree表示順を反転させる。
     * @return 反転後が昇順ならtrue
     */
    private boolean toggleTreeOrder(){
        this.ascending = ! this.ascending;

        if(this.ascending){
            this.orderButton.setToolTipText(TIP_ASCEND);
            this.orderButton.setIcon(ICON_ASCEND);
        }else{
            this.orderButton.setToolTipText(TIP_DESCEND);
            this.orderButton.setIcon(ICON_DESCEND);
        }

        final TreePath lastPath = this.treeView.getSelectionPath();

        LandsModel model = getLandsModel();
        if(model != null){
            model.setAscending(this.ascending);
        }

        EventQueue.invokeLater(new Runnable(){
            @Override
            public void run(){
                if(lastPath != null){
                    LandsTree.this.treeView.setSelectionPath(lastPath);
                    LandsTree.this.treeView.scrollPathToVisible(lastPath);
                }
                return;
            }
        });

        return this.ascending;
    }

    /**
     * {@inheritDoc}
     * ボタン押下処理。
     * @param event ボタン押下イベント {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent event){
        String cmd = event.getActionCommand();
        if(ActionManager.CMD_SWITCHORDER.equals(cmd)){
            toggleTreeOrder();
        }
        return;
    }

    /**
     * {@inheritDoc}
     * ツリーリストで何らかの要素（国、村）がクリックされたときの処理。
     * @param event イベント {@inheritDoc}
     */
    @Override
    public void valueChanged(TreeSelectionEvent event){
        TreePath path = event.getNewLeadSelectionPath();
        if(path == null){
            this.reloadButton.setEnabled(false);
            return;
        }

        Object selObj = path.getLastPathComponent();

        if( selObj instanceof Land ){
            this.reloadButton.setEnabled(true);
        }else{
            this.reloadButton.setEnabled(false);
        }

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
