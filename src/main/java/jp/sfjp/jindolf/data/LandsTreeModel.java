/*
 * model of lands for JTree view
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import jp.sourceforge.jindolf.corelib.LandDef;

/**
 * {@link javax.swing.JTree}のモデルとして国一覧と村一覧を管理。
 *
 * <p>ツリー階層は ROOT - 国 - 範囲セクション - 村 の4階層。
 *
 * <p>昇順/降順の切り替えをサポート。
 */
public class LandsTreeModel implements TreeModel{

    private static final Object ROOT = new Object();
    private static final int SECTION_INTERVAL = 100;


    private final EventListenerList listeners;

    private final List<Land> landList;
    private final Map<Land, List<VillageSection> > sectionMap;

    private boolean ascending = false;


    /**
     * コンストラクタ。
     */
    public LandsTreeModel(){
        super();

        this.listeners = new EventListenerList();

        this.landList = buildLandList();
        this.sectionMap = new HashMap<>();

        return;
    }


    /**
     * ツリーのルートオブジェクトか否か判定する。
     *
     * @param obj オブジェクト
     * @return ルートならtrue
     */
    private static boolean isRoot(Object obj){
        boolean result = Objects.equals(ROOT, obj);
        return result;
    }

    /**
     * 国一覧を読み込む。
     *
     * <p>村一覧はまだ読み込まれない。
     *
     * @return 国リスト
     */
    private static List<Land> buildLandList(){
        List<LandDef> landDefList = CoreData.getLandDefList();
        List<Land> newList = new ArrayList<>(landDefList.size());

        landDefList.stream().map(landDef ->
            new Land(landDef)
        ).forEachOrdered(land -> {
            newList.add(land);
        });

        return Collections.unmodifiableList(newList);
    }

    /**
     * 与えられた国の全ての村を指定されたinterval間隔で格納するために、
     * 範囲セクションのリストを生成する。
     *
     * @param land 国
     * @param interval 範囲セクション間の村ID間隔
     * @return 範囲セクションのリスト
     * @throws java.lang.IllegalArgumentException intervalが正でない
     */
    private static List<VillageSection> getSectionList(Land land,
                                                       int interval )
            throws IllegalArgumentException{
        if(interval <= 0){
            throw new IllegalArgumentException();
        }

        String pfx = land.getLandDef().getLandPrefix();
        List<Village> span = new ArrayList<>(interval);

        List<VillageSection> result = new ArrayList<>(2500 / interval);

        boolean loop1st = true;
        int rangeStart = -1;
        int rangeEnd = -1;

        for(Village village : land.getVillageList()){
            int vid = village.getVillageIDNum();

            if(loop1st){
                rangeStart = vid / interval * interval;
                rangeEnd = rangeStart + interval - 1;
                loop1st = false;
            }

            if(rangeEnd < vid){
                VillageSection section = new VillageSection(
                        pfx, rangeStart, rangeEnd, span);
                span.clear();
                result.add(section);

                rangeStart = vid / interval * interval;
                rangeEnd = rangeStart + interval - 1;
            }

            span.add(village);
        }

        if( ! span.isEmpty()){
            VillageSection section = new VillageSection(
                    pfx, rangeStart, rangeEnd, span);
            span.clear();
            result.add(section);
        }

        return result;
    }


    /**
     * 国リストを得る。
     *
     * @return 国のリスト
     */
    public List<Land> getLandList(){
        return this.landList;
    }

    /**
     * 指定した国の村一覧でツリーリストを更新し、
     * 更新イベントをリスナに投げる。
     *
     * <p>2020-04現在、もはや村一覧が増減することはない。
     *
     * @param land 国
     */
    public void updateVillageList(Land land){
        List<VillageSection> sectionList =
                getSectionList(land, SECTION_INTERVAL);
        this.sectionMap.put(land, sectionList);

        int[] childIndices = new int[sectionList.size()];
        for(int ct = 0; ct < childIndices.length; ct++){
            childIndices[ct] = ct;
        }
        Object[] children = sectionList.toArray();

        TreePath treePath = new TreePath(ROOT);
        treePath = treePath.pathByAddingChild(land);

        TreeModelEvent event = new TreeModelEvent(this,
                                                  treePath,
                                                  childIndices,
                                                  children     );
        fireTreeStructureChanged(event);

        return;
    }

    /**
     * ツリーの並び順を設定する。
     *
     * <p>場合によってはTreeModelEventが発生する。
     *
     * @param ascending trueなら昇順
     */
    public void setAscending(boolean ascending){
        if(this.ascending == ascending) return;

        this.ascending = ascending;
        fireLandListChanged();

        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param lst {@inheritDoc}
     */
    @Override
    public void addTreeModelListener(TreeModelListener lst){
        this.listeners.add(TreeModelListener.class, lst);
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param lst {@inheritDoc}
     */
    @Override
    public void removeTreeModelListener(TreeModelListener lst){
        this.listeners.remove(TreeModelListener.class, lst);
        return;
    }

    /**
     * 登録中のリスナーのリストを得る。
     *
     * @return リスナーのリスト
     */
    private TreeModelListener[] getTreeModelListeners(){
        return this.listeners.getListeners(TreeModelListener.class);
    }

    /**
     * 全リスナーにイベントを送出する。
     *
     * @param event ツリーイベント
     */
    protected void fireTreeStructureChanged(TreeModelEvent event){
        for(TreeModelListener listener : getTreeModelListeners()){
            listener.treeStructureChanged(event);
        }
        return;
    }

    /**
     * ツリー内容の国一覧が更新された事をリスナーに通知する。
     */
    private void fireLandListChanged(){
        int size = getLandList().size();
        int[] childIndices = new int[size];
        for(int ct = 0; ct < size; ct++){
            int index = ct;
            childIndices[ct] = index;
        }

        Object[] children = getLandList().toArray();

        TreePath treePath = new TreePath(ROOT);
        TreeModelEvent event = new TreeModelEvent(this,
                                                  treePath,
                                                  childIndices,
                                                  children     );
        fireTreeStructureChanged(event);

        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param parent {@inheritDoc}
     * @param index {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Object getChild(Object parent, int index){
        if(index < 0)                      return null;
        if(index >= getChildCount(parent)) return null;

        Object result = null;

        if(isRoot(parent)){
            List<Land> list = getLandList();
            int landIndex = index;
            if( ! this.ascending) landIndex = list.size() - index - 1;
            Land land = list.get(landIndex);
            result = land;
        }else if(parent instanceof Land){
            Land land = (Land) parent;
            List<VillageSection> sectionList = this.sectionMap.get(land);
            int sectIndex = index;
            if( ! this.ascending) sectIndex = sectionList.size() - index - 1;
            VillageSection section = sectionList.get(sectIndex);
            result = section;
        }else if(parent instanceof VillageSection){
            VillageSection section = (VillageSection) parent;
            int vilIndex = index;
            if( ! this.ascending){
                vilIndex = section.getVillageCount() - index - 1;
            }
            Village village = section.getVillage(vilIndex);
            result = village;
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @param parent {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getChildCount(Object parent){
        int result = 0;

        if(isRoot(parent)){
            result = getLandList().size();
        }else if(parent instanceof Land){
            Land land = (Land) parent;
            List<VillageSection> sectionList = this.sectionMap.get(land);
            if(sectionList != null){
                result = sectionList.size();
            }
        }else if(parent instanceof VillageSection){
            VillageSection section = (VillageSection) parent;
            result = section.getVillageCount();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @param parent {@inheritDoc}
     * @param child {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getIndexOfChild(Object parent, Object child){
        if(child == null) return -1;

        int result = -1;

        if(isRoot(parent)){
            List<Land> list = getLandList();
            int index = list.indexOf(child);
            if( ! this.ascending) index = list.size() - index - 1;
            result = index;
        }else if(parent instanceof Land){
            Land land = (Land) parent;
            List<VillageSection> sectionList = this.sectionMap.get(land);
            int index = sectionList.indexOf(child);
            if( ! this.ascending) index = sectionList.size() - index - 1;
            result = index;
        }else if(parent instanceof VillageSection){
            VillageSection section = (VillageSection) parent;
            int index = section.getIndexOfVillage(child);
            if( ! this.ascending){
                index = section.getVillageCount() - index - 1;
            }
            result = index;
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Object getRoot(){
        return ROOT;
    }

    /**
     * {@inheritDoc}
     *
     * @param node {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean isLeaf(Object node){
        if(node instanceof Village)        return true;
        if(node instanceof VillageSection) return false;
        if(node instanceof Land)           return false;
        if(isRoot(node))                   return false;
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>※ たぶん使わないので必ず失敗させている。
     *
     * @param path {@inheritDoc}
     * @param newValue {@inheritDoc}
     */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue){
        throw new UnsupportedOperationException("Not supported yet.");
    }


    /**
     * 村IDで範囲指定した、村のセクション集合。国-村間の中間ツリー。
     *
     * @see javax.swing.tree.TreeModel
     */
    private static final class VillageSection{

        private static final String FORM_NODE =
                "{0}{1,number,#} ～ {0}{2,number,#}";
        private static final String FORM_NODE_G =
                "{0}{1,number,#000} ～ {0}{2,number,#000}";


        private final int startId;
        private final int endId;

        private final String text;

        private final List<Village> villageList;


        /**
         * セクション集合を生成する。
         *
         * @param prefix 国名プレフィクス
         * @param startId 区間開始村ID
         * @param endId 区間終了村ID
         * @param spanList 村の区間リスト
         * @throws java.lang.IndexOutOfBoundsException IDの範囲指定が変
         */
        VillageSection(
                String prefix, int startId, int endId, List<Village> spanList)
                throws IndexOutOfBoundsException{
            super();

            if(startId < 0 || startId > endId){
                throw new IndexOutOfBoundsException();
            }

            this.startId = startId;
            this.endId = endId;

            String format;
            if("G".equals(prefix)) format = FORM_NODE_G;
            else                   format = FORM_NODE;
            this.text = MessageFormat.format(
                    format, prefix, this.startId, this.endId);

            List<Village> newList = new ArrayList<>(spanList);
            this.villageList = Collections.unmodifiableList(newList);

            assert this.endId - this.startId + 1 >= this.villageList.size();

            return;
        }


        /**
         * セクション内に含まれる村の総数を返す。
         *
         * <p>ほとんどの場合はintervalと同じ数。
         *
         * @return 村の総数
         */
        int getVillageCount(){
            return this.villageList.size();
        }

        /**
         * セクション内に含まれるindex番目の村を返す。
         *
         * @param index インデックス
         * @return index番目の村
         */
        Village getVillage(int index){
            return this.villageList.get(index);
        }

        /**
         * セクション内における、指定された子（村）のインデックス位置を返す。
         *
         * @param child 子
         * @return インデックス位置
         */
        int getIndexOfVillage(Object child){
            return this.villageList.indexOf(child);
        }

        /**
         * セクションの文字列表記。
         *
         * <p>JTree描画に反映される。
         *
         * <p>例:「G800 ～ G899」
         *
         * @return 文字列表記
         */
        @Override
        public String toString(){
            return this.text;
        }

    }

}
