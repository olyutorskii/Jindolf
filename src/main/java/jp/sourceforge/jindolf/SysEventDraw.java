/*
 * system event drawing
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: SysEventDraw.java 975 2009-12-27 12:54:02Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import jp.sourceforge.jindolf.parser.DecodedContent;

/**
 * システムイベントメッセージの描画。
 */
public class SysEventDraw extends AbstractTextRow{

    /** announceメッセージの色。 */
    public static final Color COLOR_ANNOUNCE = new Color(0xffffff);
    /** orderメッセージの色。 */
    public static final Color COLOR_ORDER    = new Color(0xf04040);
    /** extraメッセージの色。 */
    public static final Color COLOR_EXTRA    = new Color(0x808080);

    private static final Color COLOR_SIMPLEFG = Color.BLACK;

    private static final int INSET = 10;
    private static final int UNDER_MARGIN = 15;

    private final SysEvent sysEvent;
    private final GlyphDraw sysMessage;

    private DialogPref dialogPref;
    private Color fgColor;

    /**
     * コンストラクタ。
     * @param sysEvent システムイベント
     */
    public SysEventDraw(SysEvent sysEvent){
        this(sysEvent, new DialogPref(), FontInfo.DEFAULT_FONTINFO);
        return;
    }

    /**
     * コンストラクタ。
     * @param sysEvent システムイベント
     * @param pref 発言表示設定
     * @param fontInfo フォント設定
     */
    public SysEventDraw(SysEvent sysEvent,
                          DialogPref pref,
                          FontInfo fontInfo){
        super(fontInfo);
        this.sysEvent = sysEvent;

        DecodedContent content = this.sysEvent.getContent();
        CharSequence rawContent = content.getRawContent();

        this.sysMessage = new GlyphDraw(rawContent, this.fontInfo);

        this.dialogPref = pref;

        setColorDesign();

        return;
    }

    /**
     * 配色を設定する。
     */
    private void setColorDesign(){
        if(this.dialogPref.isSimpleMode()){
            this.fgColor = COLOR_SIMPLEFG;
        }else{
            this.fgColor = getEventColor();
        }

        this.sysMessage.setColor(this.fgColor);

        return;
    }

    /**
     * システムイベントの取得。
     * @return システムイベント
     */
    public SysEvent getSysEvent(){
        return this.sysEvent;
    }

    /**
     * イベント種別に応じた前景色を返す。
     * @return イベント種別前景色
     */
    private Color getEventColor(){
        Color result;
        switch(this.sysEvent.getEventFamily()){
        case ANNOUNCE:
            result = COLOR_ANNOUNCE;
            break;
        case ORDER:
            result = COLOR_ORDER;
            break;
        case EXTRA:
            result = COLOR_EXTRA;
            break;
        default:
            assert false;
            result = null;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    public Rectangle recalcBounds(){
        int newWidth = getWidth();

        Rectangle child;
        child = this.sysMessage.setWidth(newWidth - INSET - INSET);
        this.bounds.width = newWidth;
        this.bounds.height = child.height + INSET + INSET + UNDER_MARGIN;
        return this.bounds;
    }

    /**
     * {@inheritDoc}
     * @param fontInfo {@inheritDoc}
     */
    @Override
    public void setFontInfo(FontInfo fontInfo){
        super.setFontInfo(fontInfo);
        this.sysMessage.setFontInfo(this.fontInfo);
        recalcBounds();
        return;
    }

    /**
     * 発言設定を更新する。
     * @param pref 発言設定
     */
    public void setDialogPref(DialogPref pref){
        this.dialogPref = pref;

        setColorDesign();
        recalcBounds();

        return;
    }

    /**
     * {@inheritDoc}
     * @param from {@inheritDoc}
     * @param to {@inheritDoc}
     */
    public void drag(Point from, Point to){
        this.sysMessage.drag(from, to);
        return;
    }

    /**
     * {@inheritDoc}
     * @param appendable {@inheritDoc}
     * @return {@inheritDoc}
     * @throws java.io.IOException {@inheritDoc}
     */
    public Appendable appendSelected(Appendable appendable)
            throws IOException{
        this.sysMessage.appendSelected(appendable);
        return appendable;
    }

    /**
     * {@inheritDoc}
     */
    public void clearSelect(){
        this.sysMessage.clearSelect();
        return;
    }

    /**
     * {@inheritDoc}
     * @param xPos {@inheritDoc}
     * @param yPos {@inheritDoc}
     */
    @Override
    public void setPos(int xPos, int yPos){
        super.setPos(xPos, yPos);
        this.sysMessage.setPos(this.bounds.x + INSET, this.bounds.y + INSET);
        return;
    }

    /**
     * {@inheritDoc}
     * @param g {@inheritDoc}
     */
    public void paint(Graphics2D g){
        g.setColor(this.fgColor);

        if(this.dialogPref.isSimpleMode()){
            g.drawLine(this.bounds.x,                     this.bounds.y,
                       this.bounds.x + this.bounds.width, this.bounds.y );
        }else{
            g.drawRect(this.bounds.x,
                       this.bounds.y,
                       this.bounds.width  - 1,
                       this.bounds.height - UNDER_MARGIN);
        }

        this.sysMessage.paint(g);

        return;
    }
}