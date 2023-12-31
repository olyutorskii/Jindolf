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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
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
 * 会話表示画面。
 *
 * <p>担当責務は
 * <ul>
 * <li>会話表示</li>
 * <li>フィルタによる表示絞り込み</li>
 * <li>検索結果ハイライト</li>
 * <li>テキストのドラッグ選択</li>
 * <li>アンカー選択処理</li>
 * <li>テキストのCopyAndPaste</li>
 * <li>ポップアップメニュー</li>
 * </ul>
 * など
 */
@SuppressWarnings("serial")
public final class Discussion extends JComponent
        implements Scrollable, MouseInputListener, ComponentListener{

    private static final Color COLOR_NORMALBG = Color.BLACK;
    private static final Color COLOR_SIMPLEBG = Color.WHITE;

    private static final int MARGINTOP    =  50;
    private static final int MARGINBOTTOM = 100;


    private Period period;
    private final List<TextRow> rowList       = new LinkedList<>();
    private final List<TalkDraw> talkDrawList = new LinkedList<>();

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
    private Talk activeTalk;
    private Anchor activeAnchor;


    /**
     * コンストラクタ。
     *
     * <p>会話表示画面を作成する。
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

        modifyInputMap();
        modifyActionMap();

        setColorDesign();

        return;
    }

    /**
     * 描画設定の更新。
     *
     * <p>FontRenderContextが更新された後は必ず呼び出す必要がある。
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
     *
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
     * 会話表示設定を変更する。
     *
     * @param newPref 会話表示設定
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
     *
     * @return 現在のPeriod
     */
    public Period getPeriod(){
        return this.period;
    }

    /**
     * Periodを更新する。
     *
     * <p>新しいPeriodの表示内容はまだ反映されない。
     *
     * @param period 新しいPeriod
     */
    public final void setPeriod(Period period){
        if(period == null){
            this.period = null;
            this.rowList.clear();
            this.talkDrawList.clear();
            return;
        }

        if(    this.period == period
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
     * 会話フィルタを設定する。
     *
     * @param filter 会話フィルタ
     */
    public void setTopicFilter(TopicFilter filter){
        this.topicFilter = filter;
        filtering();
        return;
    }

    /**
     * 会話フィルタを適用する。
     */
    public void filtering(){
        if(    this.topicFilter != null
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
     *
     * @return 検索パターン
     */
    public RegexPattern getRegexPattern(){
        return this.regexPattern;
    }

    /**
     * 与えられた正規表現にマッチする文字列をハイライト描画する。
     *
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
     *
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
     *
     * @param row 矩形
     * @return フィルタリング対象ならtrue
     */
    private boolean isFiltered(TextRow row){
        if(this.topicFilter == null) return false;

        Topic topic;
        if(row instanceof TalkDraw){
            topic = ((TalkDraw) row).getTalk();
        }else if(row instanceof SysEventDraw){
            topic = ((SysEventDraw) row).getSysEvent();
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
     *
     * <p>全子TextRowがリサイズされる。
     *
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
     *
     * <p>フィルタリングが反映される。
     *
     * <p>TextRowは必要に応じて移動させられるがリサイズされることはない。
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
     *
     * <p>描画処理
     *
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
     *
     * @return {@inheritDoc}
     */
    @Override
    public Dimension getPreferredScrollableViewportSize(){
        return getPreferredSize();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean getScrollableTracksViewportWidth(){
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean getScrollableTracksViewportHeight(){
        return false;
    }

    /**
     * {@inheritDoc}
     *
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
     *
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
     * 任意の会話の表示が占める画面領域を返す。
     *
     * <p>会話がフィルタリング対象の時はnullを返す。
     *
     * @param talk 会話
     * @return 領域
     */
    public Rectangle getTalkBounds(Talk talk){
        if(    this.topicFilter != null
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
     *
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
     * 与えられた点座標を包含する会話を返す。
     *
     * @param pt 点座標（JComponent基準）
     * @return 点座標を含む会話。含む会話がなければnullを返す。
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
     *
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
     *
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
     *
     * <p>アンカーヒット処理を行う。
     *
     * <p>MouseInputListenerを参照せよ。
     *
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
     *
     * @param event {@inheritDoc}
     */
    @Override
    public void mouseEntered(MouseEvent event){
        // TODO ここでキーボードフォーカス処理が必要？
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param event {@inheritDoc}
     */
    @Override
    public void mouseExited(MouseEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>ドラッグ開始処理を行う。
     *
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
     *
     * <p>ドラッグ終了処理を行う。
     *
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
     *
     * <p>ドラッグ処理を行う。
     *
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
     *
     * @param event {@inheritDoc}
     */
    @Override
    public void mouseMoved(MouseEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param event {@inheritDoc}
     */
    @Override
    public void componentShown(ComponentEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param event {@inheritDoc}
     */
    @Override
    public void componentHidden(ComponentEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param event {@inheritDoc}
     */
    @Override
    public void componentMoved(ComponentEvent event){
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param event {@inheritDoc}
     */
    @Override
    public void componentResized(ComponentEvent event){
        int width  = getWidth();
        int height = getHeight();
        if(width != this.lastWidth){
            setWidth(width);
        }
        if(    this.idealSize.width != width
            || this.idealSize.height != height ){
            revalidate();
        }
        return;
    }

    /**
     * 選択文字列を返す。
     *
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
     *
     * @return 選択文字列。なければnull
     */
    public CharSequence copySelected(){
        CharSequence selected = getSelected();
        if(selected == null) return null;
        ClipboardAction.copyToClipboard(selected);
        return selected;
    }

    /**
     * 一会話全体の文字列内容をクリップボードにコピーする。
     *
     * @return コピーした文字列
     */
    public CharSequence copyTalk(){
        Talk talk = getActiveTalk();
        if(talk == null) return null;

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
     * ポップアップメニュートリガなどの要因による
     * 特定の会話への指示があればそれを返す。
     *
     * @return 会話
     */
    public Talk getActiveTalk(){
        return this.activeTalk;
    }

    /**
     * ポップアップメニュートリガなどの要因による
     * 特定の会話への指示があればそれを設定する。
     *
     * @param talk 会話
     */
    public void setActiveTalk(Talk talk){
        this.activeTalk = talk;
        return;
    }

    /**
     * ポップアップメニュートリガなどの要因による
     * 特定のアンカーへの指示があればそれを返す。
     *
     * @return アンカー
     */
    public Anchor getActiveAnchor(){
        return this.activeAnchor;
    }

    /**
     * ポップアップメニュートリガなどの要因による
     * 特定のアンカーへの指示があればそれを設定する。
     *
     * @param anchor アンカー
     */
    public void setActiveAnchor(Anchor anchor){
        this.activeAnchor = anchor;
        return;
    }

    /**
     * {@inheritDoc}
     *
     * <p>キーバインディング設定が変わる可能性あり。
     */
    @Override
    public void updateUI(){
        super.updateUI();
        modifyInputMap();
        return;
    }

    /**
     * COPY処理を行うキーの設定をJTextFieldから流用する。
     *
     * <p>おそらくはCtrl-C。MacならCommand-Cかも。
     *
     * <p>キー設定はLookAndFeelにより異なる可能性がある。
     */
    private void modifyInputMap(){
        InputMap thisInputMap = getInputMap();

        JComponent sampleComp = new JTextField();
        InputMap sampleInputMap;
        sampleInputMap = sampleComp.getInputMap();

        KeyStroke[] strokes = sampleInputMap.allKeys();
        Arrays.stream(strokes).filter((stroke) -> {
            Object binding = sampleInputMap.get(stroke);
            return DefaultEditorKit.copyAction.equals(binding);
        }).forEach((stroke) ->
            thisInputMap.put(stroke, DefaultEditorKit.copyAction)
        );

        return;
    }

    /**
     * COPY処理を行うActionを割り当てる。
     */
    private void modifyActionMap(){
        ActionMap actionMap = getActionMap();
        Action copyAction = new CopySelAction();
        actionMap.put(DefaultEditorKit.copyAction, copyAction);
        return;
    }

    /**
     * ActionListenerを追加する。
     *
     * <p>通知対象となるActionは、
     * COPYキー操作によって選択文字列からクリップボードへの
     * コピーが指示される場合とポップアップメニューの押下。
     *
     * @param listener リスナー
     */
    public void addActionListener(ActionListener listener){
        this.listenerList.add(ActionListener.class, listener);
        this.popup.addActionListener(listener);
        return;
    }

    /**
     * ActionListenerを削除する。
     *
     * @param listener リスナー
     */
    public void removeActionListener(ActionListener listener){
        this.listenerList.remove(ActionListener.class, listener);
        this.popup.removeActionListener(listener);
        return;
    }

    /**
     * ActionListenerを列挙する。
     *
     * @return すべてのActionListener
     */
    public ActionListener[] getActionListeners(){
        return this.listenerList.getListeners(ActionListener.class);
    }

    /**
     * 登録済みリスナに対しActionEventを発火する。
     *
     * <p>対象となるActionは、
     * COPYキー操作によって選択文字列からクリップボードへの
     * コピーが指示される場合のみ。
     *
     * @param event イベント
     */
    protected void fireActionPerformed(ActionEvent event) {
        Object source  = event.getSource();
        int id         = event.getID();
        String actcmd  = event.getActionCommand();
        long when      = event.getWhen();
        int modifiers  = event.getModifiers();

        ActionEvent newEvent =
                new ActionEvent(source, id, actcmd, when, modifiers);

        for(ActionListener listener : getActionListeners()){
            listener.actionPerformed(newEvent);
        }

        return;
    }

    /**
     * AnchorHitListenerを追加する。
     *
     * @param listener リスナー
     */
    public void addAnchorHitListener(AnchorHitListener listener){
        this.listenerList.add(AnchorHitListener.class, listener);
        return;
    }

    /**
     * AnchorHitListenerを削除する。
     *
     * @param listener リスナー
     */
    public void removeAnchorHitListener(AnchorHitListener listener){
        this.listenerList.remove(AnchorHitListener.class, listener);
        return;
    }

    /**
     * AnchorHitListenerを列挙する。
     *
     * @return すべてのAnchorHitListener
     */
    public AnchorHitListener[] getAnchorHitListeners(){
        return this.listenerList.getListeners(AnchorHitListener.class);
    }


    /**
     * COPYキーボード操作受信用ダミーAction。
     *
     * <p>COPYキー押下イベントはCOPYコマンド実行イベントに変換され、
     * {@link Discussion}のリスナへ通知される。
     */
    private class CopySelAction extends AbstractAction{

        /**
         * コンストラクタ。
         */
        CopySelAction() throws NullPointerException{
            super();
            return;
        }

        /**
         * {@inheritDoc}
         *
         * @param event COPYキーボード押下イベント
         */
        @Override
        public void actionPerformed(ActionEvent event){
            ActionEvent newEvent =
                    new ActionEvent(
                            this,
                            ActionEvent.ACTION_PERFORMED,
                            ActionManager.CMD_COPY
                    );
            fireActionPerformed(newEvent);
            return;
        }

    }

    /**
     * ポップアップメニュー。
     */
    private static final class DiscussionPopup extends JPopupMenu{

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


        /**
         * コンストラクタ。
         */
        DiscussionPopup(){
            super();

            design();
            setCommand();

            Icon icon = GUIUtils.getWWWIcon();
            this.menuWebTalk.setIcon(icon);

            return;
        }

        /**
         * メニューのデザイン。
         */
        private void design(){
            add(this.menuCopy);
            add(this.menuSelTalk);
            addSeparator();
            add(this.menuJumpAnchor);
            add(this.menuWebTalk);
            addSeparator();
            add(this.menuSummary);
            return;
        }

        /**
         * アクションコマンドの設定。
         */
        private void setCommand(){
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
            return;
        }

        /**
         * {@inheritDoc}
         *
         * <p>状況に応じてボタン群をマスクする。
         *
         * <p>ポップアップクリックの対象となった会話やアンカーを記録する。
         *
         * @param invoker {@inheritDoc}
         * @param x {@inheritDoc}
         * @param y {@inheritDoc}
         */
        @Override
        public void show(Component invoker, int x, int y){
            if( ! (invoker instanceof Discussion) ){
                super.show(invoker, x, y);
                return;
            }
            Discussion dis = (Discussion) invoker;

            Point point = new Point(x, y);

            boolean talkPointed = false;
            Talk activeTalk = null;
            Anchor activeAnchor = null;

            TalkDraw popupTalkDraw = dis.getHittedTalkDraw(point);
            if(popupTalkDraw != null){
                talkPointed = true;
                activeTalk   = popupTalkDraw.getTalk();
                activeAnchor = popupTalkDraw.getAnchor(point);
            }

            dis.setActiveTalk(activeTalk);
            dis.setActiveAnchor(activeAnchor);

            boolean anchorPointed = activeAnchor != null;
            boolean hasSelectedText = dis.getSelected() != null;

            this.menuSelTalk    .setEnabled(talkPointed);
            this.menuWebTalk    .setEnabled(talkPointed);
            this.menuJumpAnchor .setEnabled(anchorPointed);
            this.menuCopy       .setEnabled(hasSelectedText);

            super.show(invoker, x, y);

            return;
        }

        /**
         * ActionListenerを追加する。
         *
         * <p>受信対象はポップアップメニュー押下。
         *
         * @param listener リスナー
         */
        public void addActionListener(ActionListener listener){
            this.listenerList.add(ActionListener.class, listener);

            this.menuCopy       .addActionListener(listener);
            this.menuSelTalk    .addActionListener(listener);
            this.menuJumpAnchor .addActionListener(listener);
            this.menuWebTalk    .addActionListener(listener);
            this.menuSummary    .addActionListener(listener);

            return;
        }

        /**
         * ActionListenerを削除する。
         *
         * @param listener リスナー
         */
        public void removeActionListener(ActionListener listener){
            this.listenerList.remove(ActionListener.class, listener);

            this.menuCopy       .removeActionListener(listener);
            this.menuSelTalk    .removeActionListener(listener);
            this.menuJumpAnchor .removeActionListener(listener);
            this.menuWebTalk    .removeActionListener(listener);
            this.menuSummary    .removeActionListener(listener);

            return;
        }

        /**
         * ActionListenerを列挙する。
         *
         * @return すべてのActionListener
         */
        public ActionListener[] getActionListeners(){
            return this.listenerList.getListeners(ActionListener.class);
        }

    }

}
