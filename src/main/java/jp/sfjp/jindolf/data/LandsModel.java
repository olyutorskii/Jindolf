/*
 * model of lands for JTree view
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.data;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import jp.sfjp.jindolf.dxchg.XmlUtils;
import jp.sourceforge.jindolf.corelib.LandDef;
import org.xml.sax.SAXException;

/**
 * 国の集合。あらゆるデータモデルの大元。
 * 国一覧と村一覧を管理。
 * JTreeのモデルも兼用。
 */
public class LandsModel implements TreeModel{ // ComboBoxModelも付けるか？

    private static final String ROOT = "ROOT";
    private static final int SECTION_INTERVAL = 100;

    private static final Logger LOGGER = Logger.getAnonymousLogger();


    private final List<Land> landList = new LinkedList<>();
    private final List<Land> unmodList =
            Collections.unmodifiableList(this.landList);
    private final Map<Land, List<VillageSection>> sectionMap =
            new HashMap<>();
    private boolean isLandListLoaded = false;

    private final EventListenerList listeners = new EventListenerList();

    private boolean ascending = false;

    /**
     * コンストラクタ。
     * この時点ではまだ国一覧が読み込まれない。
     */
    public LandsModel(){
        super();
        return;
    }

    /**
     * 指定した国の村一覧を更新しイベントを投げる。
     * @param land 国
     */
    public void updateVillageList(Land land){
        List<VillageSection> sectionList =
                VillageSection.getSectionList(land, SECTION_INTERVAL);
        this.sectionMap.put(land, sectionList);

        int[] childIndices = new int[sectionList.size()];
        for(int ct = 0; ct < childIndices.length; ct++){
            childIndices[ct] = ct;
        }
        Object[] children = sectionList.toArray();

        Object[] path = {ROOT, land};
        TreePath treePath = new TreePath(path);
        TreeModelEvent event = new TreeModelEvent(this,
                                                  treePath,
                                                  childIndices,
                                                  children     );
        fireTreeStructureChanged(event);

        return;
    }

    /**
     * 国一覧を読み込む。
     */
    // TODO static にできない？
    public void loadLandList(){
        if(this.isLandListLoaded) return;

        this.landList.clear();

        List<LandDef> landDefList;
        try{
            DocumentBuilder builder = XmlUtils.createDocumentBuilder();
            landDefList = LandDef.buildLandDefList(builder);
        }catch(IOException e){
            LOGGER.log(Level.SEVERE, "failed to load land list", e);
            return;
        }catch(SAXException e){
            LOGGER.log(Level.SEVERE, "failed to load land list", e);
            return;
        }catch(URISyntaxException e){
            LOGGER.log(Level.SEVERE, "failed to load land list", e);
            return;
        }catch(ParserConfigurationException e){
            LOGGER.log(Level.SEVERE, "failed to load land list", e);
            return;
        }

        for(LandDef landDef : landDefList){
            Land land = new Land(landDef);
            this.landList.add(land);
        }

        this.isLandListLoaded = true;

        fireLandListChanged();

        return;
    }

    /**
     * ツリー内容が更新された事をリスナーに通知する。
     */
    private void fireLandListChanged(){
        int size = this.landList.size();
        int[] childIndices = new int[size];
        for(int ct = 0; ct < size; ct++){
            int index = ct;
            childIndices[ct] = index;
        }

        Object[] children = this.landList.toArray();

        TreePath treePath = new TreePath(ROOT);
        TreeModelEvent event = new TreeModelEvent(this,
                                                  treePath,
                                                  childIndices,
                                                  children     );
        fireTreeStructureChanged(event);

        return;
    }

    /**
     * ツリーの並び順を設定する。
     * 場合によってはTreeModelEventが発生する。
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
     * @param l {@inheritDoc}
     */
    @Override
    public void addTreeModelListener(TreeModelListener l){
        this.listeners.add(TreeModelListener.class, l);
        return;
    }

    /**
     * {@inheritDoc}
     * @param l {@inheritDoc}
     */
    @Override
    public void removeTreeModelListener(TreeModelListener l){
        this.listeners.remove(TreeModelListener.class, l);
        return;
    }

    /**
     * 登録中のリスナーのリストを得る。
     * @return リスナーのリスト
     */
    private TreeModelListener[] getTreeModelListeners(){
        return this.listeners.getListeners(TreeModelListener.class);
    }

    /**
     * 全リスナーにイベントを送出する。
     * @param event ツリーイベント
     */
    protected void fireTreeStructureChanged(TreeModelEvent event){
        for(TreeModelListener listener : getTreeModelListeners()){
            listener.treeStructureChanged(event);
        }
        return;
    }

    /**
     * 国リストを得る。
     * @return 国のリスト
     */
    public List<Land> getLandList(){
        return this.unmodList;
    }

    /**
     * {@inheritDoc}
     * @param parent {@inheritDoc}
     * @param index {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Object getChild(Object parent, int index){
        if(index < 0)                      return null;
        if(index >= getChildCount(parent)) return null;

        if(parent == ROOT){
            List<Land> list = getLandList();
            int landIndex = index;
            if( ! this.ascending) landIndex = list.size() - index - 1;
            Land land = list.get(landIndex);
            return land;
        }
        if(parent instanceof Land){
            Land land = (Land)parent;
            List<VillageSection> sectionList = this.sectionMap.get(land);
            int sectIndex = index;
            if( ! this.ascending) sectIndex = sectionList.size() - index - 1;
            VillageSection section = sectionList.get(sectIndex);
            return section;
        }
        if(parent instanceof VillageSection){
            VillageSection section = (VillageSection)parent;
            int vilIndex = index;
            if( ! this.ascending){
                vilIndex = section.getVillageCount() - index - 1;
            }
            Village village = section.getVillage(vilIndex);
            return village;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * @param parent {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getChildCount(Object parent){
        if(parent == ROOT){
            return getLandList().size();
        }
        if(parent instanceof Land){
            Land land = (Land)parent;
            List<VillageSection> sectionList = this.sectionMap.get(land);
            if(sectionList == null) return 0;
            return sectionList.size();
        }
        if(parent instanceof VillageSection){
            VillageSection section = (VillageSection)parent;
            return section.getVillageCount();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     * @param parent {@inheritDoc}
     * @param child {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getIndexOfChild(Object parent, Object child){
        if(child == null) return -1;
        if(parent == ROOT){
            List<Land> list = getLandList();
            int index = list.indexOf(child);
            if( ! this.ascending) index = list.size() - index - 1;
            return index;
        }
        if(parent instanceof Land){
            Land land = (Land)parent;
            List<VillageSection> sectionList = this.sectionMap.get(land);
            int index = sectionList.indexOf(child);
            if( ! this.ascending) index = sectionList.size() - index - 1;
            return index;
        }
        if(parent instanceof VillageSection){
            VillageSection section = (VillageSection)parent;
            int index = section.getIndexOfVillage(child);
            if( ! this.ascending){
                index = section.getVillageCount() - index - 1;
            }
            return index;
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Object getRoot(){
        return ROOT;
    }

    /**
     * {@inheritDoc}
     * @param node {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean isLeaf(Object node){
        if(node == ROOT)                   return false;
        if(node instanceof Land)           return false;
        if(node instanceof VillageSection) return false;
        if(node instanceof Village)        return true;
        return true;
    }

    /**
     * {@inheritDoc}
     * ※ たぶん使わないので必ず失敗させている。
     * @param path {@inheritDoc}
     * @param newValue {@inheritDoc}
     */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 村IDで範囲指定した、村のセクション集合。国-村間の中間ツリー。
     * @see javax.swing.tree.TreeModel
     */
    private static final class VillageSection{

        private final int startID;
        private final int endID;
        private final String prefix;

        private final List<Village> villageList = new LinkedList<>();


        /**
         * セクション集合を生成する。
         * @param land 国
         * @param startID 開始村ID
         * @param endID 終了村ID
         * @throws java.lang.IndexOutOfBoundsException IDの範囲指定が変
         */
        private VillageSection(Land land, int startID, int endID)
                throws IndexOutOfBoundsException{
            super();

            if(startID < 0 || startID > endID){
                throw new IndexOutOfBoundsException();
            }

            this.startID = startID;
            this.endID = endID;
            this.prefix = land.getLandDef().getLandPrefix();

            for(Village village : land.getVillageList()){
                int id = village.getVillageIDNum();
                if(startID <= id && id <= endID){
                    this.villageList.add(village);
                }
            }

            return;
        }


        /**
         * 与えられた国の全ての村を、指定されたinterval間隔でセクション化する。
         * @param land 国
         * @param interval セクションの間隔
         * @return セクションのリスト
         * @throws java.lang.IllegalArgumentException intervalが正でない
         */
        private static List<VillageSection> getSectionList(Land land,
                                                             int interval )
                throws IllegalArgumentException{
            if(interval <= 0){
                throw new IllegalArgumentException();
            }

            List<Village> villageList = land.getVillageList();
            Village village1st = villageList.get(0);
            Village villageLast = villageList.get(villageList.size() - 1);

            int startID = village1st.getVillageIDNum();
            int endID = villageLast.getVillageIDNum();

            List<VillageSection> result = new LinkedList<>();

            int fixedStart = startID / interval * interval;
            for(int ct = fixedStart; ct <= endID; ct += interval){
                VillageSection section =
                        new VillageSection(land, ct, ct + interval - 1);
                result.add(section);
            }

            return Collections.unmodifiableList(result);
        }

        /**
         * セクションに含まれる村の総数を返す。
         * @return 村の総数
         */
        private int getVillageCount(){
            return this.villageList.size();
        }

        /**
         * セクションに含まれるindex番目の村を返す。
         * @param index インデックス
         * @return index番目の村
         */
        private Village getVillage(int index){
            return this.villageList.get(index);
        }

        /**
         * セクションにおける、指定された子（村）のインデックス位置を返す。
         * @param child 子
         * @return インデックス位置
         */
        private int getIndexOfVillage(Object child){
            return this.villageList.indexOf(child);
        }

        /**
         * セクションの文字列表記。
         * JTree描画に反映される。
         * @return 文字列表記
         */
        @Override
        public String toString(){
            StringBuilder result = new StringBuilder();
            result.append(this.prefix).append(this.startID);
            result.append(" ～ ");
            result.append(this.prefix).append(this.endID);
            return result.toString();
        }
    }

}
