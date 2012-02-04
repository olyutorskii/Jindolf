/*
 * discussion viewer
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.glyph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.io.IOException;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputListener;
import javax.swing.text.DefaultEditorKit;
import jp.sfjp.jindolf.data.Anchor;
import jp.sfjp.jindolf.data.Avatar;
import jp.sfjp.jindolf.data.DialogPref;
import jp.sfjp.jindolf.data.Period;
import jp.sfjp.jindolf.data.RegexPattern;
import jp.sfjp.jindolf.data.SysEvent;
import jp.sfjp.jindolf.data.Talk;
import jp.sfjp.jindolf.data.Topic;
import jp.sfjp.jindolf.dxchg.ClipboardAction;
import jp.sfjp.jindolf.util.GUIUtils;
import jp.sfjp.jindolf.view.ActionManager;
import jp.sfjp.jindolf.view.TopicFilter;

/**
 * 発言表示画面。
 *
 * 表示に影響する要因は、Periodの中身、LayoutManagerによるサイズ変更、
 * フォント属性の指定、フィルタリング操作、ドラッギングによる文字列選択操作、
 * 文字列検索および検索ナビゲーション。
 */
@SuppressWarnings("serial")
public class Discussion extends JComponent
        implements Scrollable, MouseInputListener, ComponentListener{

    private static final Color COLOR_NORMALBG = Color.BLACK;
    private static final Color COLOR_SIMPLEBG = Color.WHITE;

    private static final int MARGINTOP    =  50;
    private static final int MARGINBOTTOM = 100;

    private Period period;
    private final List<TextRow> rowList       = new LinkedList<TextRow>();
    private final List<TalkDraw> talkDrawList = new LinkedList<TalkDraw>();

    private TopicFilter topicFilter;
    private TopicFilter.FilterContext filterContext;
    private RegexPattern regexPattern;

    private Point dragFrom;

    private FontInfo fontInfo;
    private final RenderingHints hints = new RenderingHints(null);

    private DialogPref dialogPref;

    private Dimension idealSize;
    private int lastWidth = -1;

    private final DiscussionPopup popup = new DiscussionPopup();

    private final EventListenerList thisListenerList =
            new EventListenerList();

    private final Action copySelectedAction =
            new ProxyAction(ActionManager.CMD_COPY);

    /**
     * 発言表示画面を作成する。
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public Discussion(){
        super();

        this.fontInfo = FontInfo.DEFAULT_FONTINFO;
        this.dialogPref = new DialogPref();

        this.hints.put(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);
        this.hints.put(RenderingHints.KEY_RENDERING,
                       RenderingHints.VALUE_RENDER_QUALITY);
        updateRenderingHints();

        setPeriod(null);

        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);

        setComponentPopupMenu(this.popup);

        updateInputMap();
        ActionMap actionMap = getActionMap();
        actionMap.put(DefaultEditorKit.copyAction, this.copySelectedAction);

        setColorDesign();

        return;
    }

    /**
     * 描画設定の更新。
     * FontRenderContextが更新された後は必ず呼び出す必要がある。
     */
    private void updateRenderingHints(){
        Object textAliaseValue;
        FontRenderContext context = this.fontInfo.getFontRenderContext();
        if(context.isAntiAliased()){
            textAliaseValue = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
        }else{
            textAliaseValue = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
        }
        this.hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                       textAliaseValue);

        Object textFractionalValue;
        if(context.usesFractionalMetrics()){
            textFractionalValue = RenderingHints.VALUE_FRACTIONALMETRICS_ON;
        }else{
            textFractionalValue = RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
        }
        this.hints.put(RenderingHints.KEY_FRACTIONALMETRICS,
                       textFractionalValue);

        return;
    }

    /**
     * 配色を設定する。
     */
    private void setColorDesign(){
        Color fgColor;
        if(this.dialogPref.isSimpleMode()){
            fgColor = COLOR_SIMPLEBG;
        }else{
            fgColor = COLOR_NORMALBG;
        }

        setForeground(fgColor);
        repaint();

        return;
    }

    /**
     * フォント描画設定を変更する。
     * @param newFontInfo フォント設定
     */
    public void setFontInfo(FontInfo newFontInfo){
        this.fontInfo = newFontInfo;

        updateRenderingHints();

        for(TextRow row : this.rowList){
            row.setFontInfo(this.fontInfo);
        }

        setColorDesign();
        layoutRows();

        revalidate();
        repaint();

        return;
    }

    /**
     * 発言表示設定を変更する。
     * @param newPref 発言表示設定
     */
    public void setDialogPref(DialogPref newPref){
        this.dialogPref = newPref;

        for(TextRow row : this.rowList){
            if(row instanceof TalkDraw){
                TalkDraw talkDraw = (TalkDraw) row;
                talkDraw.setDialogPref(this.dialogPref);
            }else if(row instanceof SysEventDraw){
                SysEventDraw sysDraw = (SysEventDraw) row;
                sysDraw.setDialogPref(this.dialogPref);
            }
        }

        setColorDesign();
        layoutRows();

        revalidate();
        repaint();

        return;
    }

    /**
     * 現在のPeriodを返す。
     * @return 現在のPeriod
     */
    public Period getPeriod(){
        return this.period;
    }

    /**
     * Periodを更新する。
     * 新しいPeriodの表示内容はまだ反映されない。
     * @param period 新しいPeriod
     */
    public final void setPeriod(Period period){
        if(period == null){
            this.period = null;
            this.rowList.clear();
            this.talkDrawList.clear();
            return;
        }

        if(   this.period == period
           && period.getTopics() == this.rowList.size() ){
            filterTopics();
            return;
        }

        this.period = period;

        this.filterContext = null;

        this.rowList.clear();
        this.talkDrawList.clear();
        for(Topic topic : this.period.getTopicList()){
            TextRow row;
            if(topic instanceof Talk){
                Talk talk = (Talk) topic;
                TalkDraw talkDraw = new TalkDraw(talk,
                                                 this.dialogPref,
                                                 this.fontInfo );
                this.talkDrawList.add(talkDraw);
                row = talkDraw;
            }else if(topic instanceof SysEvent){
                SysEvent sysEvent = (SysEvent) topic;
                row = new SysEventDraw(sysEvent,
                                       this.dialogPref,
                                       this.fontInfo );
            }else{
                assert false;
                continue;
            }
            this.rowList.add(row);
        }

        filterTopics();

        clearSizeCache();

        layoutRows();

        return;
    }

    /**
     * 発言フィルタを設定する。
     * @param filter 発言フィルタ
     */
    public void setTopicFilter(TopicFilter filter){
        this.topicFilter = filter;
        filtering();
        return;
    }

    /**
     * 発言フィルタを適用する。
     */
    public void filtering(){
        if(   this.topicFilter != null
           && this.topicFilter.isSame(this.filterContext)){
            return;
        }

        if(this.topicFilter != null){
            this.filterContext = this.topicFilter.getFilterContext();
        }else{
            this.filterContext = null;
        }

        filterTopics();
        layoutVertical();

        clearSelect();

        return;
    }

    /**
     * 検索パターンを取得する。
     * @return 検索パターン
     */
    public RegexPattern getRegexPattern(){
        return this.regexPattern;
    }

    /**
     * 与えられた正規表現にマッチする文字列をハイライト描画する。
     * @param newPattern 検索パターン
     * @return ヒット件数
     */
    public int setRegexPattern(RegexPattern newPattern){
        this.regexPattern = newPattern;

        int total = 0;

        clearHotTarget();

        Pattern pattern = null;
        if(this.regexPattern != null){
            pattern = this.regexPattern.getPattern();
        }

        for(TalkDraw talkDraw : this.talkDrawList){
            total += talkDraw.setRegex(pattern);
        }

        repaint();

        return total;
    }

    /**
     * 検索結果の次候補をハイライト表示する。
     */
    public void nextHotTarget(){
        TalkDraw oldTalk = null;
        int oldIndex = -1;
        TalkDraw newTalk = null;
        int newIndex = -1;
        TalkDraw firstTalk = null;

        boolean findOld = true;
        for(TalkDraw talkDraw : this.talkDrawList){
            int matches = talkDraw.getRegexMatches();
            if(firstTalk == null && matches > 0){
                firstTalk = talkDraw;
            }
            if(findOld){
                int index = talkDraw.getHotTargetIndex();
                if(index < 0) continue;
                oldTalk = talkDraw;
                oldIndex = index;
                scrollRectWithMargin(talkDraw.getHotTargetRectangle());
                if(oldIndex < matches - 1 && ! isFiltered(talkDraw) ){
                    newTalk = talkDraw;
                    newIndex = oldIndex + 1;
                    break;
                }
                findOld = false;
            }else{
                if(isFiltered(talkDraw)) continue;
                if(matches <= 0) continue;
                newTalk = talkDraw;
                newIndex = 0;
                break;
            }
        }

        Rectangle showRect = null;
        if(oldTalk == null && firstTalk != null){
            firstTalk.setHotTargetIndex(0);
            showRect = firstTalk.getHotTargetRectangle();
        }else if(   oldTalk != null
                 && newTalk != null){
            oldTalk.clearHotTarget();
            newTalk.setHotTargetIndex(newIndex);
            showRect = newTalk.getHotTargetRectangle();
        }

        if(showRect != null){
            scrollRectWithMargin(showRect);
        }

        repaint();

        return;
    }

    /**
     * 検索結果の前候補をハイライト表示する。
     */
    public void prevHotTarget(){
        TalkDraw oldTalk = null;
        int oldIndex = -1;
        TalkDraw newTalk = null;
        int newIndex = -1;
        TalkDraw firstTalk = null;

        boolean findOld = true;
        int size = this.talkDrawList.size();
        ListIterator<TalkDraw> iterator =
                this.talkDrawList.listIterator(size);
        while(iterator.hasPrevious()){
            TalkDraw talkDraw = iterator.previous();
            int matches = talkDraw.getRegexMatches();
            if(firstTalk == null && matches > 0){
                firstTalk = talkDraw;
            }
            if(findOld){
                int index = talkDraw.getHotTargetIndex();
                if(index < 0) continue;
                oldTalk = talkDraw;
                oldIndex = index;
                scrollRectWithMargin(talkDraw.getHotTargetRectangle());
                if(oldIndex > 0 && ! isFiltered(talkDraw) ){
                    newTalk = talkDraw;
                    newIndex = oldIndex - 1;
                    break;
                }
                findOld = false;
            }else{
                if(isFiltered(talkDraw)) continue;
                if(matches <= 0) continue;
                newTalk = talkDraw;
                newIndex = matches - 1;
                break;
            }
        }

        Rectangle showRect = null;
        if(oldTalk == null && firstTalk != null){
            int matches = firstTalk.getRegexMatches();
            firstTalk.setHotTargetIndex(matches - 1);
            showRect = firstTalk.getHotTargetRectangle();
        }else if(   oldTalk != null
                 && newTalk != null){
            oldTalk.clearHotTarget();
            newTalk.setHotTargetIndex(newIndex);
            showRect = newTalk.getHotTargetRectangle();
        }

        if(showRect != null){
            scrollRectWithMargin(showRect);
        }

        repaint();

        return;
    }

    /**
     * 検索結果の特殊ハイライト表示を解除。
     */
    public void clearHotTarget(){
        for(TalkDraw talkDraw : this.talkDrawList){
            talkDraw.clearHotTarget();
        }
        repaint();
        return;
    }

    /**
     * 指定した領域に若干の上下マージンを付けて
     * スクロールウィンドウに表示させる。
     * @param rectangle 指定領域
     */
    private void scrollRectWithMargin(Rectangle rectangle){
        Rectangle show = new Rectangle(rectangle);
        show.y      -= MARGINTOP;
        show.height += MARGINTOP + MARGINBOTTOM;

        scrollRectToVisible(show);

        return;
    }

    /**
     * 過去に計算した寸法を破棄する。
     */
    private void clearSizeCache(){
        this.idealSize = null;
        this.lastWidth = -1;
        revalidate();
        return;
    }

    /**
     * 指定した矩形がフィルタリング対象か判定する。
     * @param row 矩形
     * @return フィルタリング対象ならtrue
     */
    private boolean isFiltered(TextRow row){
        if(this.topicFilter == null) return false;

        Topic topic;
        if(row instanceof TalkDraw){
            topic = ((TalkDraw)row).getTalk();
        }else if(row instanceof SysEventDraw){
            topic = ((SysEventDraw)row).getSysEvent();
        }else{
            return false;
        }

        return this.topicFilter.isFiltered(topic);
    }

    /**
     * フィルタリング指定に従いTextRowを表示するか否か設定する。
     */
    private void filterTopics(){
        for(TextRow row : this.rowList){
            if(isFiltered(row)) row.setVisible(false);
            else                row.setVisible(true);
        }
        return;
    }

    /**
     * 幅を設定する。
     * 全子TextRowがリサイズされる。
     * @param width コンポーネント幅
     */
    private void setWidth(int width){
        this.lastWidth = width;
        Insets insets = getInsets();
        int rowWidth = width - (insets.left + insets.right);
        for(TextRow row : this.rowList){
            row.setWidth(rowWidth);
        }

        layoutVertical();

        return;
    }

    /**
     * 子TextRowの縦位置レイアウトを行う。
     * フィルタリングが反映される。
     * TextRowは必要に応じて移動させられるがリサイズされることはない。
     */
    private void layoutVertical(){
        Rectangle unionRect = null;
        Insets insets = getInsets();
        int vertPos = insets.top;

        for(TextRow row : this.rowList){
            if( ! row.isVisible() ) continue;

            row.setPos(insets.left, vertPos);
            Rectangle rowBound = row.getBounds();
            vertPos += rowBound.height;

            if(unionRect == null){
                unionRect = new Rectangle(rowBound);
            }else{
                unionRect.add(rowBound);
            }
        }

        if(unionRect == null){
            unionRect = new Rectangle(insets.left, insets.top, 0, 0);
        }

        if(this.idealSize == null){
            this.idealSize = new Dimension();
        }

        int newWidth  = insets.left + unionRect.width  + insets.right;
        int newHeight = insets.top  + unionRect.height + insets.bottom;

        this.idealSize.setSize(newWidth, newHeight);

        setPreferredSize(this.idealSize);

        revalidate();
        repaint();

        return;
    }

    /**
     * Rowsの縦位置を再レイアウトする。
     */
    public void layoutRows(){
        int width = getWidth();
        setWidth(width);
        return;
    }

    /**
     * {@inheritDoc}
     * @param g {@inheritDoc}
     */
    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(this.hints);

        Rectangle clipRect = g2.getClipBounds();
        g2.fillRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);

        for(TextRow row : this.rowList){
            if( ! row.isVisible() ) continue;

            Rectangle rowRect = row.getBounds();
            if( ! rowRect.intersects(clipRect) ) continue;

            row.paint(g2);
        }

        return;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Dimension getPreferredScrollableViewportSize(){
        return getPreferredSize();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean getScrollableTracksViewportWidth(){
        return true;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean getScrollableTracksViewportHeight(){
        return false;
    }

    /**
     * {@inheritDoc}
     * @param visibleRect {@inheritDoc}
     * @param orientation {@inheritDoc}
     * @param direction {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                               int orientation,
                                               int direction        ){
        if(orientation == SwingConstants.VERTICAL){
            return visibleRect.height;
        }
        return 30; // TODO フォント高 × 1.5 ぐらい？
    }

    /**
     * {@inheritDoc}
     * @param visibleRect {@inheritDoc}
     * @param orientation {@inheritDoc}
     * @param direction {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                              int orientation,
                                              int direction      ){
        return 30;
    }

    /**
     * 任意の発言の表示が占める画面領域を返す。
     * 発言がフィルタリング対象の時はnullを返す。
     * @param talk 発言
     * @return 領域
     */
    public Rectangle getTalkBounds(Talk talk){
        if(   this.topicFilter != null
           && this.topicFilter.isFiltered(talk)) return null;

        for(TalkDraw talkDraw : this.talkDrawList){
            if(talkDraw.getTalk() == talk){
                Rectangle rect = talkDraw.getBounds();
                return rect;
            }
        }

        return null;
    }

    /**
     * ドラッグ処理を行う。
     * @param from ドラッグ開始位置
     * @param to 現在のドラッグ位置
     */
    private void drag(Point from, Point to){
        Rectangle dragRegion = new Rectangle();
        dragRegion.setFrameFromDiagonal(from, to);

        for(TextRow row : this.rowList){
            if(isFiltered(row)) continue;
            if( ! row.getBounds().intersects(dragRegion) ) continue;
            row.drag(from, to);
        }
        repaint();
        return;
    }

    /**
     * 選択範囲の解除。
     */
    private void clearSelect(){
        for(TextRow row : this.rowList){
            row.clearSelect();
        }
        repaint();
        return;
    }

    /**
     * 与えられた点座標を包含する発言を返す。
     * @param pt 点座標（JComponent基準）
     * @return 点座標を含む発言。含む発言がなければnullを返す。
     */
    // TODO 二分探索とかしたい。
    private TalkDraw getHittedTalkDraw(Point pt){
        for(TalkDraw talkDraw : this.talkDrawList){
            if(isFiltered(talkDraw)) continue;
            Rectangle bounds = talkDraw.getBounds();
            if(bounds.contains(pt)) return talkDraw;
        }
        return null;
    }

    /**
     * アンカークリック動作の処理。
     * @param pt クリックポイント
     */
    private void hitAnchor(Point pt){
        TalkDraw talkDraw = getHittedTalkDraw(pt);
        if(talkDraw == null) return;

        Anchor anchor = talkDraw.getAnchor(pt);
        if(anchor == null) return;

        for(AnchorHitListener listener : getAnchorHitListeners()){
            AnchorHitEvent event =
                    new AnchorHitEvent(this, talkDraw, anchor, pt);
            listener.anchorHitted(event);
        }

        return;
    }

    /**
     * 検索マッチ文字列クリック動作の処理。
     * @param pt クリックポイント
     */
    private void hitRegex(Point pt){
        TalkDraw talkDraw = getHittedTalkDraw(pt);
        if(talkDraw == null) return;

        int index = talkDraw.getRegexMatchIndex(pt);
        if(index < 0) return;

        clearHotTarget();
        talkDraw.setHotTargetIndex(index);

        return;
    }

    /**
     * {@inheritDoc}
     * アンカーヒット処理を行う。
     * MouseInputListenerを参照せよ。
     * @param event {@inheritDoc}
     */
    // TODO 距離判定がシビアすぎ
    @Override
    public void mouseClicked(MouseEvent event){
        Point pt = event.getPoint();
        if(event.getButton() == MouseEvent.BUTTON1){
            clearSelect();
            hitAnchor(pt);
            hitRegex(pt);
        }
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void mouseEntered(MouseEvent event){
        // TODO ここでキーボードフォーカス処理が必要？
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void mouseExited(MouseEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * ドラッグ開始処理を行う。
     * @param event {@inheritDoc}
     */
    @Override
    public void mousePressed(MouseEvent event){
        requestFocusInWindow();

        if(event.getButton() == MouseEvent.BUTTON1){
            clearSelect();
            this.dragFrom = event.getPoint();
        }

        return;
    }

    /**
     * {@inheritDoc}
     * ドラッグ終了処理を行う。
     * @param event {@inheritDoc}
     */
    @Override
    public void mouseReleased(MouseEvent event){
        if(event.getButton() == MouseEvent.BUTTON1){
            this.dragFrom = null;
        }
        return;
    }

    /**
     * {@inheritDoc}
     * ドラッグ処理を行う。
     * @param event {@inheritDoc}
     */
    // TODO ドラッグ範囲がビューポートを超えたら自動的にスクロールしてほしい。
    @Override
    public void mouseDragged(MouseEvent event){
        if(this.dragFrom == null) return;
        Point dragTo = event.getPoint();
        drag(this.dragFrom, dragTo);
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void mouseMoved(MouseEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void componentShown(ComponentEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void componentHidden(ComponentEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void componentMoved(ComponentEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     * @param event {@inheritDoc}
     */
    @Override
    public void componentResized(ComponentEvent event){
        int width  = getWidth();
        int height = getHeight();
        if(width != this.lastWidth){
            setWidth(width);
        }
        if(   this.idealSize.width != width
           || this.idealSize.height != height ){
            revalidate();
        }
        return;
    }

    /**
     * 選択文字列を返す。
     * @return 選択文字列
     */
    public CharSequence getSelected(){
        StringBuilder selected = new StringBuilder();

        for(TextRow row : this.rowList){
            if(isFiltered(row)) continue;
            try{
                row.appendSelected(selected);
            }catch(IOException e){
                assert false; // ありえない
                return null;
            }
        }

        if(selected.length() <= 0) return null;

        return selected;
    }

    /**
     * 選択文字列をクリップボードにコピーする。
     * @return 選択文字列
     */
    public CharSequence copySelected(){
        CharSequence selected = getSelected();
        if(selected == null) return null;
        ClipboardAction.copyToClipboard(selected);
        return selected;
    }

    /**
     * 矩形の示す一発言をクリップボードにコピーする。
     * @return コピーした文字列
     */
    public CharSequence copyTalk(){
        TalkDraw talkDraw = this.popup.lastPopupedTalkDraw;
        if(talkDraw == null) return null;
        Talk talk = talkDraw.getTalk();

        StringBuilder selected = new StringBuilder();

        Avatar avatar = talk.getAvatar();
        selected.append(avatar.getName()).append(' ');

        String anchor = talk.getAnchorNotation();
        selected.append(anchor);
        if(talk.hasTalkNo()){
            selected.append(' ').append(talk.getAnchorNotation_G());
        }
        selected.append('\n');

        selected.append(talk.getDialog());
        if(selected.charAt(selected.length() - 1) != '\n'){
            selected.append('\n');
        }

        ClipboardAction.copyToClipboard(selected);

        return selected;
    }

    /**
     * ポップアップメニュートリガ座標に発言があればそれを返す。
     * @return 発言
     */
    public Talk getPopupedTalk(){
        TalkDraw talkDraw = this.popup.lastPopupedTalkDraw;
        if(talkDraw == null) return null;
        Talk talk = talkDraw.getTalk();
        return talk;
    }

    /**
     * ポップアップメニュートリガ座標にアンカーがあればそれを返す。
     * @return アンカー
     */
    public Anchor getPopupedAnchor(){
        return this.popup.lastPopupedAnchor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUI(){
        super.updateUI();
        this.popup.updateUI();

        updateInputMap();

        return;
    }

    /**
     * COPY処理を行うキーの設定をJTextFieldから流用する。
     * おそらくはCtrl-C。MacならCommand-Cかも。
     */
    private void updateInputMap(){
        InputMap thisInputMap = getInputMap();

        InputMap sampleInputMap;
        sampleInputMap = new JTextField().getInputMap();
        KeyStroke[] strokes = sampleInputMap.allKeys();
        for(KeyStroke stroke : strokes){
            Object bind = sampleInputMap.get(stroke);
            if(bind.equals(DefaultEditorKit.copyAction)){
                thisInputMap.put(stroke, DefaultEditorKit.copyAction);
            }
        }

        return;
    }

    /**
     * ActionListenerを追加する。
     * @param listener リスナー
     */
    public void addActionListener(ActionListener listener){
        this.thisListenerList.add(ActionListener.class, listener);

        this.popup.menuCopy       .addActionListener(listener);
        this.popup.menuSelTalk    .addActionListener(listener);
        this.popup.menuJumpAnchor .addActionListener(listener);
        this.popup.menuWebTalk    .addActionListener(listener);
        this.popup.menuSummary    .addActionListener(listener);

        return;
    }

    /**
     * ActionListenerを削除する。
     * @param listener リスナー
     */
    public void removeActionListener(ActionListener listener){
        this.thisListenerList.remove(ActionListener.class, listener);

        this.popup.menuCopy       .removeActionListener(listener);
        this.popup.menuSelTalk    .removeActionListener(listener);
        this.popup.menuJumpAnchor .removeActionListener(listener);
        this.popup.menuWebTalk    .removeActionListener(listener);
        this.popup.menuSummary    .removeActionListener(listener);

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
        return;
    }

    /**
     * AnchorHitListenerを削除する。
     * @param listener リスナー
     */
    public void removeAnchorHitListener(AnchorHitListener listener){
        this.thisListenerList.remove(AnchorHitListener.class, listener);
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

    /**
     * キーボード入力用ダミーAction。
     */
    private class ProxyAction extends AbstractAction{

        private final String command;

        /**
         * コンストラクタ。
         * @param command コマンド
         * @throws NullPointerException 引数がnull
         */
        public ProxyAction(String command) throws NullPointerException{
            super();
            if(command == null) throw new NullPointerException();
            this.command = command;
            return;
        }

        /**
         * {@inheritDoc}
         * @param event {@inheritDoc}
         */
        @Override
        public void actionPerformed(ActionEvent event){
            Object source  = event.getSource();
            int id         = event.getID();
            String actcmd  = this.command;
            long when      = event.getWhen();
            int modifiers  = event.getModifiers();

            for(ActionListener listener : getActionListeners()){
                ActionEvent newEvent = new ActionEvent(source,
                                                       id,
                                                       actcmd,
                                                       when,
                                                       modifiers );
                listener.actionPerformed(newEvent);
            }

            return;
        }
    };

    /**
     * ポップアップメニュー。
     */
    private class DiscussionPopup extends JPopupMenu{

        private final JMenuItem menuCopy =
                new JMenuItem("選択範囲をコピー");
        private final JMenuItem menuSelTalk =
                new JMenuItem("この発言をコピー");
        private final JMenuItem menuJumpAnchor =
                new JMenuItem("アンカーの示す先へジャンプ");
        private final JMenuItem menuWebTalk =
                new JMenuItem("この発言をブラウザで表示...");
        private final JMenuItem menuSummary =
                new JMenuItem("発言を集計...");

        private TalkDraw lastPopupedTalkDraw;
        private Anchor lastPopupedAnchor;

        /**
         * コンストラクタ。
         */
        public DiscussionPopup(){
            super();

            add(this.menuCopy);
            add(this.menuSelTalk);
            addSeparator();
            add(this.menuJumpAnchor);
            add(this.menuWebTalk);
            addSeparator();
            add(this.menuSummary);

            this.menuCopy
                .setActionCommand(ActionManager.CMD_COPY);
            this.menuSelTalk
                .setActionCommand(ActionManager.CMD_COPYTALK);
            this.menuJumpAnchor
                .setActionCommand(ActionManager.CMD_JUMPANCHOR);
            this.menuWebTalk
                .setActionCommand(ActionManager.CMD_WEBTALK);
            this.menuSummary
                .setActionCommand(ActionManager.CMD_DAYSUMMARY);

            this.menuWebTalk.setIcon(GUIUtils.getWWWIcon());

            return;
        }

        /**
         * {@inheritDoc}
         * @param comp {@inheritDoc}
         * @param x {@inheritDoc}
         * @param y {@inheritDoc}
         */
        @Override
        public void show(Component comp, int x, int y){
            Point point = new Point(x, y);

            this.lastPopupedTalkDraw = getHittedTalkDraw(point);
            if(this.lastPopupedTalkDraw != null){
                this.menuSelTalk.setEnabled(true);
                this.menuWebTalk.setEnabled(true);
            }else{
                this.menuSelTalk.setEnabled(false);
                this.menuWebTalk.setEnabled(false);
            }

            if(this.lastPopupedTalkDraw != null){
                this.lastPopupedAnchor =
                        this.lastPopupedTalkDraw.getAnchor(point);
            }else{
                this.lastPopupedAnchor = null;
            }

            if(this.lastPopupedAnchor != null){
                this.menuJumpAnchor.setEnabled(true);
            }else{
                this.menuJumpAnchor.setEnabled(false);
            }

            if(getSelected() != null){
                this.menuCopy.setEnabled(true);
            }else{
                this.menuCopy.setEnabled(false);
            }

            super.show(comp, x, y);

            return;
        }
    }

    // TODO シンプルモードの追加
    // Period変更を追跡するリスナ化
}
