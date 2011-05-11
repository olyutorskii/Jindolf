/*
 * Text-Glyph Drawing
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.CharacterIterator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingConstants;

/**
 * 複数行の文字列を矩形内に描画する。
 * 制御文字は'\n'のみサポート。
 */
public class GlyphDraw extends AbstractTextRow implements SwingConstants{

    private static final Color COLOR_SELECTION = new Color(0xb8cfe5);
    private static final Color COLOR_SEARCHHIT = new Color(0xb2b300);
    private static final Color COLOR_HOTTARGET = Color.ORANGE;

    private Color foregroundColor = Color.WHITE;
    private final CharSequence source;

    private float[] dimArray;
    private final List<GlyphVector> lines = new LinkedList<GlyphVector>();
    private Collection<Anchor> anchorSet;
    private final List<MatchInfo> matchList = new LinkedList<MatchInfo>();
    private MatchInfo hotTarget = null;

    private int selectStart = -1;
    private int selectLast  = -1;

    /**
     * コンストラクタ。
     * @param source 文字列
     */
    public GlyphDraw(CharSequence source){
        this(source, FontInfo.DEFAULT_FONTINFO);
        return;
    }

    /**
     * コンストラクタ。
     * @param source 文字列
     * @param fontInfo フォント設定
     */
    public GlyphDraw(CharSequence source, FontInfo fontInfo){
        super(fontInfo);

        this.source = source;

        GlyphVector gv = createGlyphVector(this.source);

        int sourceLength = gv.getNumGlyphs();

        this.dimArray = gv.getGlyphPositions(0, sourceLength+1, null);

        return;
    }

    /**
     * 前景色を得る。
     * @return 前景色
     */
    public Color getColor(){
        return this.foregroundColor;
    }

    /**
     * 前景色を設定する。
     * @param color 前景色
     */
    public void setColor(Color color){
        this.foregroundColor = color;
        return;
    }

    /**
     * アンカーを設定する。
     * アンカーの位置指定はコンストラクタに与えた文字列に対するものでなければ
     * ならない。
     * @param anchorSet アンカーの集合
     */
    public void setAnchorSet(Collection<Anchor> anchorSet){
        this.anchorSet = anchorSet;
        return;
    }

    /**
     * 文字列の占めるピクセル幅を返す。
     * @param fromPos 文字列開始位置
     * @param toPos 文字列終了位置
     * @return ピクセル幅
     */
    public float getSpan(int fromPos, int toPos){
        float from = this.dimArray[fromPos   * 2];
        float to   = this.dimArray[(toPos+1) * 2];
        float span = to - from;
        return span;
    }

    /**
     * 指定領域の文字列から行情報を生成し内部に登録する。
     * @param from 文字列開始位置
     * @param to 文字列終了位置
     * @return 行情報
     */
    protected GlyphVector createLine(int from, int to){
        GlyphVector line = createGlyphVector(this.source, from, to + 1);
        this.lines.add(line);
        return line;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    // TODO 最後が \n で終わるダイアログが無限再帰を起こす？
    @Override
    public Rectangle recalcBounds(){
        float newWidth = (float) getWidth();
        this.lines.clear();
        CharacterIterator iterator;
        iterator = new SequenceCharacterIterator(this.source);
        int from = iterator.getIndex();
        int to = from;
        for(;;){
           char ch =  iterator.current();

           if(ch == CharacterIterator.DONE){
               if(from < to){
                   createLine(from, to - 1);
               }
               break;
           }

           if(ch == '\n'){
               createLine(from, to);
               to++;
               from = to;
               iterator.next();
               continue;
           }

           float fwidth = getSpan(from, to);
           if(fwidth > newWidth){
               if(from < to){
                   createLine(from, to - 1);
                   from = to;
               }else{
                   createLine(from, to);
                   to++;
                   from = to;
                   iterator.next();
               }
               continue;
           }

           to++;
           iterator.next();
        }

        int totalWidth = 0;
        int totalHeight = 0;
        for(GlyphVector gv : this.lines){
            Rectangle2D r2d = gv.getLogicalBounds();
            Rectangle rect = r2d.getBounds();
            totalWidth = Math.max(totalWidth, rect.width);
            totalHeight += rect.height;
        }

        this.bounds.width  = totalWidth;
        this.bounds.height = totalHeight;

        return this.bounds;
    }

    /**
     * {@inheritDoc}
     * @param fontInfo {@inheritDoc}
     */
    @Override
    public void setFontInfo(FontInfo fontInfo){
        super.setFontInfo(fontInfo);

        GlyphVector gv = createGlyphVector(this.source);

        int sourceLength = gv.getNumGlyphs();

        this.dimArray = gv.getGlyphPositions(0, sourceLength+1, null);

        recalcBounds();

        return;
    }

    /**
     * 指定された点座標が文字列のどこを示すか判定する。
     * @param pt 点座標
     * @return 文字位置。座標が文字列以外を示す場合は-1を返す。
     */
    public int getCharIndex(Point pt){
        if( ! this.bounds.contains(pt) ) return -1;

        int sPos = 0;
        int xPos = this.bounds.x;
        int yPos = this.bounds.y;
        for(GlyphVector gv : this.lines){
            Rectangle2D r2d = gv.getLogicalBounds();
            Rectangle rect = r2d.getBounds();
            rect.x = xPos;
            rect.y = yPos;
            int sourceLength = gv.getNumGlyphs();
            if(rect.contains(pt)){
                for(int pos = 0; pos < sourceLength; pos++){
                    float span = getSpan(sPos, sPos+pos);
                    if(span+xPos > pt.x) return sPos + pos;
                }
                return -1;
            }
            yPos += rect.height;
            sPos += sourceLength;
        }

        return -1;
    }

    /**
     * {@inheritDoc}
     * @param appendable {@inheritDoc}
     * @return {@inheritDoc}
     * @throws java.io.IOException {@inheritDoc}
     */
    @Override
    public Appendable appendSelected(Appendable appendable)
            throws IOException{
        if(this.selectStart < 0 || this.selectLast < 0) return appendable;
        CharSequence subsel;
        subsel = this.source.subSequence(this.selectStart,
                                         this.selectLast + 1);
        appendable.append(subsel);
        return appendable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearSelect(){
        this.selectStart = -1;
        this.selectLast  = -1;
        return;
    }

    /**
     * 指定した部分文字列を選択された状態にする。
     * @param start 文字列開始位置
     * @param last 文字列終了位置
     */
    public void select(int start, int last){
        if(start < last){
            this.selectStart = start;
            this.selectLast  = last;
        }else{
            this.selectStart = last;
            this.selectLast  = start;
        }
        this.selectLast  = Math.min(this.source.length() - 1,
                                    this.selectLast );
        return;
    }

    /**
     * {@inheritDoc}
     * @param from {@inheritDoc}
     * @param to {@inheritDoc}
     */
    @Override
    public void drag(Point from, Point to){
        Point fromPt = from;
        Point toPt = to;
        if(fromPt.y > toPt.y || (fromPt.y == toPt.y && fromPt.x > toPt.x)){
            Point swapPt = fromPt;
            fromPt = toPt;
            toPt = swapPt;
        }

        int fromDirection = GUIUtils.getDirection(this.bounds, fromPt);
        int toDirection   = GUIUtils.getDirection(this.bounds, toPt);

        if(fromDirection == toDirection){
            if(   fromDirection == NORTH
               || fromDirection == SOUTH){
                clearSelect();
                return;
            }
        }

        int fromIndex = -1;
        int toIndex   = -1;

        if(fromDirection == NORTH){
            fromIndex = 0;
        }
        if(toDirection == SOUTH){
            toIndex = this.source.length() - 1;
        }

        if(fromIndex < 0){
            fromIndex = getCharIndex(fromPt);
        }
        if(toIndex < 0){
            toIndex = getCharIndex(toPt);
        }

        if(fromIndex >= 0 && toIndex >= 0){
            select(fromIndex, toIndex);
            return;
        }

        int xPos = this.bounds.x;
        int yPos = this.bounds.y;
        int accumPos = 0;
        for(GlyphVector gv : this.lines){
            int glyphStart = accumPos;
            int glyphLast   = accumPos + gv.getNumGlyphs() - 1;
            Rectangle2D r2d = gv.getLogicalBounds();
            Rectangle rect = r2d.getBounds();
            rect.x += xPos;
            rect.y = yPos;

            if(   fromIndex < 0
               && GUIUtils.getDirection(rect, fromPt) == SOUTH){
                yPos += rect.height;
                accumPos = glyphLast + 1;
                continue;
            }else if(   toIndex < 0
                     && GUIUtils.getDirection(rect, toPt) == NORTH){
                break;
            }

            if(fromIndex < 0){
                int dir = GUIUtils.getDirection(rect, fromPt);
                if(dir == EAST){
                    fromIndex = glyphStart;
                }else if(dir == WEST){
                    fromIndex = glyphLast+1;
                }
            }
            if(toIndex < 0){
                int dir = GUIUtils.getDirection(rect, toPt);
                if(dir == EAST){
                    toIndex = glyphStart - 1;
                }else if(dir == WEST){
                    toIndex = glyphLast;
                }
            }

            if(fromIndex >= 0 && toIndex >= 0){
                select(fromIndex, toIndex);
                return;
            }

            yPos += rect.height;
            accumPos = glyphLast + 1;
        }

        clearSelect();
        return;
    }

    /**
     * 文字列検索がヒットした箇所のハイライト描画を行う。
     * @param g グラフィックスコンテキスト
     */
    private void paintRegexHitted(Graphics2D g){
        if(this.matchList.size() <= 0) return;

        FontMetrics metrics = g.getFontMetrics();
        final int ascent  = metrics.getAscent();

        int xPos = this.bounds.x;
        int yPos = this.bounds.y + ascent;

        int accumPos = 0;

        for(GlyphVector line : this.lines){
            int glyphStart = accumPos;
            int glyphLast   = accumPos + line.getNumGlyphs() - 1;

            for(MatchInfo match : this.matchList){
                int matchStart = match.getStartPos();
                int matchLast  = match.getEndPos() - 1;

                if(matchLast < glyphStart) continue;
                if(glyphLast < matchStart) break;

                int hilightStart = Math.max(matchStart, glyphStart);
                int hilightLast  = Math.min(matchLast,  glyphLast);
                Shape shape;
                shape = line.getGlyphLogicalBounds(hilightStart - glyphStart);
                Rectangle hilight = shape.getBounds();
                shape = line.getGlyphLogicalBounds(hilightLast - glyphStart);
                hilight.add(shape.getBounds());

                if(match == this.hotTarget){
                    g.setColor(COLOR_HOTTARGET);
                }else{
                    g.setColor(COLOR_SEARCHHIT);
                }

                g.fillRect(xPos + hilight.x,
                           yPos + hilight.y,
                           hilight.width,
                           hilight.height );
            }

            Rectangle2D r2d = line.getLogicalBounds();
            Rectangle rect = r2d.getBounds();

            yPos += rect.height;

            accumPos = glyphLast + 1;
        }

        return;
    }

    /**
     * 選択文字列のハイライト描画を行う。
     * @param g グラフィックスコンテキスト
     */
    private void paintSelected(Graphics2D g){
        if(this.selectStart < 0 || this.selectLast < 0) return;

        g.setColor(COLOR_SELECTION);

        int xPos = this.bounds.x;
        int yPos = this.bounds.y;

        int accumPos = 0;

        for(GlyphVector line : this.lines){
            int glyphStart = accumPos;
            int glyphLast   = accumPos + line.getNumGlyphs() - 1;

            if(this.selectLast < glyphStart) break;

            Rectangle2D r2d = line.getLogicalBounds();
            Rectangle rect = r2d.getBounds();

            if(glyphLast < this.selectStart){
                yPos += rect.height;
                accumPos = glyphLast + 1;
                continue;
            }

            int hilightStart = Math.max(this.selectStart, glyphStart);
            int hilightLast  = Math.min(this.selectLast,  glyphLast);
            Shape shape;
            shape = line.getGlyphLogicalBounds(hilightStart - glyphStart);
            Rectangle hilight = shape.getBounds();
            shape = line.getGlyphLogicalBounds(hilightLast - glyphStart);
            hilight.add(shape.getBounds());

            g.fillRect(xPos + hilight.x,
                       yPos,
                       hilight.width,
                       hilight.height );

            yPos += rect.height;
            accumPos = glyphLast + 1;
        }

        return;
    }

    /**
     * アンカー文字列のハイライト描画を行う。
     * @param g グラフィックスコンテキスト
     */
    private void paintAnchorBack(Graphics2D g){
        if(this.anchorSet == null) return;
        if(this.anchorSet.size() <= 0) return;

        FontMetrics metrics = g.getFontMetrics();
        final int ascent  = metrics.getAscent();

        g.setColor(Color.GRAY);

        int xPos = this.bounds.x;
        int yPos = this.bounds.y + ascent;

        int accumPos = 0;

        for(GlyphVector line : this.lines){
            int glyphStart = accumPos;
            int glyphLast   = accumPos + line.getNumGlyphs() - 1;

            for(Anchor anchor : this.anchorSet){
                int anchorStart = anchor.getStartPos();
                int anchorLast   = anchor.getEndPos() - 1;

                if(anchorLast < glyphStart) continue;
                if(glyphLast < anchorStart) break;

                int hilightStart = Math.max(anchorStart, glyphStart);
                int hilightLast  = Math.min(anchorLast,  glyphLast);
                Shape shape;
                shape = line.getGlyphLogicalBounds(hilightStart - glyphStart);
                Rectangle hilight = shape.getBounds();
                shape = line.getGlyphLogicalBounds(hilightLast - glyphStart);
                hilight.add(shape.getBounds());

                g.fillRect(xPos + hilight.x,
                           yPos + hilight.y,
                           hilight.width,
                           hilight.height );
            }

            Rectangle2D r2d = line.getLogicalBounds();
            Rectangle rect = r2d.getBounds();

            yPos += rect.height;

            accumPos = glyphLast + 1;
        }

        return;
    }

    /**
     * {@inheritDoc}
     * @param g {@inheritDoc}
     */
    @Override
    public void paint(Graphics2D g){
        g.setFont(this.fontInfo.getFont());
        FontMetrics metrics = g.getFontMetrics();
        int ascent  = metrics.getAscent();

        int xPos = this.bounds.x;
        int yPos = this.bounds.y + ascent;

        paintAnchorBack(g);
        paintRegexHitted(g);
        paintSelected(g);

        g.setColor(this.foregroundColor);
        for(GlyphVector gv : this.lines){
            g.drawGlyphVector(gv, xPos, yPos);

            Rectangle2D r2d = gv.getLogicalBounds();
            Rectangle rect = r2d.getBounds();

            yPos += rect.height;
        }

        return;
    }

    /**
     * 与えられた座標にアンカー文字列が存在すればAnchorを返す。
     * @param pt 座標
     * @return アンカー
     */
    public Anchor getAnchor(Point pt){
        int targetIdx = getCharIndex(pt);
        if(targetIdx < 0) return null;

        for(Anchor anchor : this.anchorSet){
            int anchorStart = anchor.getStartPos();
            int anchorEnd   = anchor.getEndPos();
            if(anchorStart <= targetIdx && targetIdx <= anchorEnd - 1){
                return anchor;
            }
        }

        return null;
    }

    /**
     * 与えられた座標に検索マッチ文字列があればそのインデックスを返す。
     * @param pt 座標
     * @return 検索マッチインデックス
     */
    public int getRegexMatchIndex(Point pt){
        int targetIdx = getCharIndex(pt);
        if(targetIdx < 0) return -1;

        int index = 0;
        for(MatchInfo info : this.matchList){
            int matchStart = info.getStartPos();
            int matchEnd   = info.getEndPos();
            if(matchStart <= targetIdx && targetIdx <= matchEnd - 1){
                return index;
            }
            index++;
        }

        return -1;
    }

    /**
     * 検索文字列パターンを設定する。
     * @param searchRegex パターン
     * @return ヒット数
     */
    public int setRegex(Pattern searchRegex){
        clearHotTarget();
        this.matchList.clear();
        if(searchRegex == null) return 0;

        Matcher matcher = searchRegex.matcher(this.source);
        while(matcher.find()){
            int startPos = matcher.start();
            int endPos   = matcher.end();
            if(startPos >= endPos) break;  // 長さ0マッチは無視
            MatchInfo matchInfo = new MatchInfo(startPos, endPos);
            this.matchList.add(matchInfo);
        }

        return getRegexMatches();
    }

    /**
     * 検索ハイライトインデックスを返す。
     * @return 検索ハイライトインデックス。見つからなければ-1。
     */
    public int getHotTargetIndex(){
        return this.matchList.indexOf(this.hotTarget);
    }

    /**
     * 検索ハイライトを設定する。
     * @param index ハイライトインデックス。負ならハイライト全クリア。
     */
    public void setHotTargetIndex(int index){
        if(index < 0){
            clearHotTarget();
            return;
        }
        this.hotTarget = this.matchList.get(index);
        return;
    }

    /**
     * 検索一致件数を返す。
     * @return 検索一致件数
     */
    public int getRegexMatches(){
        return this.matchList.size();
    }

    /**
     * 特別な検索ハイライト描画をクリアする。
     */
    public void clearHotTarget(){
        this.hotTarget = null;
        return;
    }

    /**
     * 特別な検索ハイライト領域の寸法を返す。
     * @return ハイライト領域寸法
     */
    public Rectangle getHotTargetRectangle(){
        Rectangle result = null;

        if(this.hotTarget == null) return result;

        int xPos = this.bounds.x;
        int yPos = this.bounds.y;

        int accumPos = 0;

        int matchStart = this.hotTarget.getStartPos();
        int matchLast  = this.hotTarget.getEndPos() - 1;

        for(GlyphVector gv : this.lines){
            int glyphStart = accumPos;
            int glyphLast   = accumPos + gv.getNumGlyphs() - 1;

            if(matchLast < glyphStart) break;

            if(matchStart <= glyphLast){
                int hilightStart = Math.max(matchStart, glyphStart);
                int hilightLast  = Math.min(matchLast,  glyphLast);

                Shape shape;
                shape = gv.getGlyphLogicalBounds(hilightStart - glyphStart);
                Rectangle hilight = shape.getBounds();
                shape = gv.getGlyphLogicalBounds(hilightLast - glyphStart);
                hilight.add(shape.getBounds());

                Rectangle temp = new Rectangle(xPos + hilight.x,
                                               yPos,
                                               hilight.width,
                                               hilight.height);
                if(result == null){
                    result = temp;
                }else{
                    result.add(temp);
                }
            }

            Rectangle2D r2d = gv.getLogicalBounds();
            Rectangle rect = r2d.getBounds();
            yPos += rect.height;

            accumPos = glyphLast + 1;
        }

        return result;
    }

    /**
     * 検索ヒット情報。
     */
    private static class MatchInfo{

        private final int startPos;
        private final int endPos;

        /**
         * コンストラクタ。
         * @param startPos ヒット開始位置
         * @param endPos ヒット終了位置
         */
        public MatchInfo(int startPos, int endPos){
            super();
            this.startPos = startPos;
            this.endPos   = endPos;
            return;
        }

        /**
         * ヒット開始位置を取得する。
         * @return ヒット開始位置
         */
        public int getStartPos(){
            return this.startPos;
        }

        /**
         * ヒット終了位置を取得する。
         * @return ヒット終了位置
         */
        public int getEndPos(){
            return this.endPos;
        }
    }

}
