/*
 * アンカー描画
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: AnchorDraw.java 995 2010-03-15 03:54:09Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * アンカー描画。
 */
public class AnchorDraw extends AbstractTextRow{

    private static final Color COLOR_ANCHOR = new Color(0xffff99);
    private static final Color COLOR_SIMPLEFG = Color.BLACK;
    private static final int UPPER_MARGIN = 3;
    private static final int UNDER_MARGIN = 3;

    private static final Stroke STROKE_DASH;

    static{
        float[] dash = {3.0f};
        STROKE_DASH = new BasicStroke(1.0f,
                                      BasicStroke.CAP_SQUARE,
                                      BasicStroke.JOIN_MITER,
                                      10.0f,
                                      dash,
                                      0.0f);
    }

    private final Talk talk;
    private final GlyphDraw caption;
    private final GlyphDraw dialog;
    private final BufferedImage faceImage;
    private final Point imageOrigin   = new Point();
    private final Point captionOrigin = new Point();
    private final Point dialogOrigin  = new Point();

    private DialogPref dialogPref;

    /**
     * コンストラクタ。
     * @param talk 発言
     */
    public AnchorDraw(Talk talk){
        this(talk, new DialogPref(), FontInfo.DEFAULT_FONTINFO);
        return;
    }

    /**
     * コンストラクタ。
     * @param talk 発言
     * @param pref 発言表示設定
     * @param fontInfo フォント設定
     */
    public AnchorDraw(Talk talk, DialogPref pref, FontInfo fontInfo){
        super(fontInfo);

        this.talk = talk;
        this.caption = new GlyphDraw(getCaptionString(), this.fontInfo);
        this.dialog  = new GlyphDraw(this.talk.getDialog(), this.fontInfo);
        this.dialogPref = pref;
        this.faceImage = getFaceImage();

        setColorDesign();

        return;
    }

    /**
     * 顔アイコンイメージを返す。
     * @return アイコンイメージ
     */
    private BufferedImage getFaceImage(){
        Period period = this.talk.getPeriod();
        Village village = period.getVillage();

        BufferedImage image;
        if(this.talk.isGrave()){
            image = village.getGraveImage();
        }else{
            Avatar avatar = this.talk.getAvatar();
            image = village.getAvatarFaceImage(avatar);
        }

        return image;
    }

    /**
     * 配色を設定する。
     */
    private void setColorDesign(){
        Color fgColor;
        if(this.dialogPref.isSimpleMode()){
            fgColor = COLOR_SIMPLEFG;
        }else{
            fgColor = COLOR_ANCHOR;
        }
        this.caption.setColor(fgColor);
        this.dialog .setColor(fgColor);
        return;
    }

    /**
     * キャプション文字列の取得。
     * @return キャプション文字列
     */
    private CharSequence getCaptionString(){
        StringBuilder result = new StringBuilder();
        Avatar avatar = this.talk.getAvatar();

        if(this.talk.hasTalkNo()){
            result.append(this.talk.getAnchorNotation_G()).append(' ');
        }
        result.append(avatar.getFullName())
              .append(' ')
              .append(this.talk.getAnchorNotation());

        return result;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    public Rectangle recalcBounds(){
        int newWidth = getWidth();

        int imageWidth  = 0;
        int imageHeight = 0;
        if( ! this.dialogPref.isSimpleMode() ){
            imageWidth  = this.faceImage.getWidth(null);
            imageHeight = this.faceImage.getHeight(null);
        }

        this.caption.setWidth(newWidth - imageWidth);
        int captionWidth  = this.caption.getWidth();
        int captionHeight = this.caption.getHeight();

        this.dialog.setWidth(newWidth);
        int dialogWidth  = this.dialog.getWidth();
        int dialogHeight = this.dialog.getHeight();

        int headerHeight = Math.max(imageHeight, captionHeight);

        int totalWidth = Math.max(imageWidth + captionWidth, dialogWidth);

        int totalHeight = headerHeight;
        totalHeight += dialogHeight;

        int imageYpos;
        int captionYpos;
        if(imageHeight > captionHeight){
            imageYpos = 0;
            captionYpos = (imageHeight - captionHeight) / 2;
        }else{
            imageYpos = (captionHeight - imageHeight) / 2;
            captionYpos = 0;
        }

        this.imageOrigin  .setLocation(0,
                                       UPPER_MARGIN + imageYpos);
        this.captionOrigin.setLocation(imageWidth,
                                       UPPER_MARGIN + captionYpos);
        this.dialogOrigin .setLocation(0,
                                       UPPER_MARGIN + headerHeight);

        if(this.dialogPref.isSimpleMode()){
            this.bounds.width  = newWidth;
        }else{
            this.bounds.width  = totalWidth;
        }
        this.bounds.height = UPPER_MARGIN + totalHeight + UNDER_MARGIN;

        return this.bounds;
    }

    /**
     * {@inheritDoc}
     * @param fontInfo {@inheritDoc}
     */
    @Override
    public void setFontInfo(FontInfo fontInfo){
        super.setFontInfo(fontInfo);

        this.caption.setFontInfo(this.fontInfo);
        this.dialog .setFontInfo(this.fontInfo);

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
        this.caption.drag(from, to);
        this.dialog.drag(from, to);
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
        this.caption.appendSelected(appendable);
        this.dialog.appendSelected(appendable);
        return appendable;
    }

    /**
     * {@inheritDoc}
     */
    public void clearSelect(){
        this.caption.clearSelect();
        this.dialog.clearSelect();
        return;
    }

    /**
     * 検索文字列パターンを設定する。
     * @param searchRegex パターン
     * @return ヒット数
     */
    public int setRegex(Pattern searchRegex){
        int total = 0;

        total += this.dialog.setRegex(searchRegex);

        return total;
    }

    /**
     * {@inheritDoc}
     * @param g {@inheritDoc}
     */
    public void paint(Graphics2D g){
        final int xPos = this.bounds.x;
        final int yPos = this.bounds.y;

        if(this.dialogPref.isSimpleMode()){
            Stroke oldStroke = g.getStroke();
            g.setStroke(STROKE_DASH);
            g.drawLine(xPos,                     this.bounds.y,
                       xPos + this.bounds.width, this.bounds.y );
            g.setStroke(oldStroke);
        }else{
            g.drawImage(this.faceImage,
                        xPos + this.imageOrigin.x,
                        yPos + this.imageOrigin.y,
                        null );
        }

        this.caption.setPos(xPos + this.captionOrigin.x,
                            yPos + this.captionOrigin.y );
        this.caption.paint(g);

        this.dialog.setPos(xPos + this.dialogOrigin.x,
                           yPos + this.dialogOrigin.y );
        this.dialog.paint(g);

        return;
    }

}
