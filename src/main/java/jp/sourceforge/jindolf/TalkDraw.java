/*
 * 会話部描画
 *
 * Copyright(c) 2008 olyutorskii
 * $Id: TalkDraw.java 995 2010-03-15 03:54:09Z olyutorskii $
 */

package jp.sourceforge.jindolf;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import jp.sourceforge.jindolf.corelib.TalkType;

/**
 * 会話部の描画。
 * 会話部描画領域は、キャプション部と発言部から構成される。
 */
public class TalkDraw extends AbstractTextRow{

    /** 通常会話の色。 */
    public static final Color COLOR_PUBLIC   = new Color(0xffffff);
    /** 狼間ささやきの色。 */
    public static final Color COLOR_WOLFONLY = new Color(0xff7777);
    /** 灰色発言の色。 */
    public static final Color COLOR_PRIVATE  = new Color(0x939393);
    /** 墓下発言の色。 */
    public static final Color COLOR_GRAVE    = new Color(0x9fb7cf);

    private static final Color COLOR_CAPTIONFG = Color.WHITE;
    private static final Color COLOR_DIALOGFG  = Color.BLACK;

    private static final Color COLOR_SIMPLEFG = Color.BLACK;
    private static final Color COLOR_SIMPLEBG = Color.WHITE;

    private static final int BALOONTIP_WIDTH = 16;
    private static final int BALOONTIP_HEIGHT = 8;
    private static final int UPPER_MARGIN = 5;
    private static final int UNDER_MARGIN = 10;
    private static final int OFFSET_ANCHOR = 36;
    private static final int CAPTION_DIALOG_GAP = 3;

    private static final Color COLOR_TRANS = new Color(0, 0, 0, 0);
    private static final int BALOON_R = 10;
    private static final BufferedImage BALOON_PUBLIC;
    private static final BufferedImage BALOON_WOLFONLY;
    private static final BufferedImage BALOON_GRAVE;
    private static final BufferedImage BALOON_PRIVATE;
    private static final BufferedImage SQUARE_PUBLIC;
    private static final BufferedImage SQUARE_WOLFONLY;
    private static final BufferedImage SQUARE_GRAVE;
    private static final BufferedImage SQUARE_PRIVATE;

    private static final float ANCHOR_FONT_RATIO = 0.9f;

    static{
        BALOON_PUBLIC   = createWedgeImage(COLOR_PUBLIC);
        BALOON_WOLFONLY = createBubbleImage(COLOR_WOLFONLY);
        BALOON_PRIVATE  = createBubbleImage(COLOR_PRIVATE);
        BALOON_GRAVE    = createBubbleImage(COLOR_GRAVE);
        SQUARE_PUBLIC   = createSquareImage(COLOR_PUBLIC);
        SQUARE_WOLFONLY = createSquareImage(COLOR_WOLFONLY);
        SQUARE_GRAVE    = createSquareImage(COLOR_GRAVE);
        SQUARE_PRIVATE  = createSquareImage(COLOR_PRIVATE);
    }

    /**
     * 指定した色で描画したクサビイメージを取得する。
     * @param color 色
     * @return クサビイメージ
     */
    private static BufferedImage createWedgeImage(Color color){
        BufferedImage image;
        image = new BufferedImage(BALOONTIP_WIDTH,
                                  BALOONTIP_HEIGHT,
                                  BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        RenderingHints renderHints = GUIUtils.getQualityHints();
        g2.addRenderingHints(renderHints);
        g2.setColor(COLOR_TRANS);
        g2.fillRect(0, 0, BALOONTIP_WIDTH, BALOONTIP_HEIGHT);
        g2.setColor(color);
        Polygon poly = new Polygon();
        poly.addPoint(8, 8);
        poly.addPoint(16, 8);
        poly.addPoint(16, 0);
        g2.fillPolygon(poly);
        return image;
    }

    /**
     * 指定した色で描画した泡イメージを取得する。
     * @param color 色
     * @return 泡イメージ
     */
    private static BufferedImage createBubbleImage(Color color){
        BufferedImage image;
        image = new BufferedImage(BALOONTIP_WIDTH,
                                  BALOONTIP_HEIGHT,
                                  BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        RenderingHints renderHints = GUIUtils.getQualityHints();
        g2.addRenderingHints(renderHints);
        g2.setColor(COLOR_TRANS);
        g2.fillRect(0, 0, BALOONTIP_WIDTH, BALOONTIP_HEIGHT);
        g2.setColor(color);
        g2.fillOval(2, 4, 4, 4);
        g2.fillOval(8, 2, 6, 6);
        return image;
    }

    /**
     * 指定した色で描画した長方形イメージを返す。
     * @param color 色
     * @return 長方形イメージ
     */
    private static BufferedImage createSquareImage(Color color){
        BufferedImage image;
        image = new BufferedImage(BALOONTIP_WIDTH,
                                  BALOONTIP_HEIGHT,
                                  BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        RenderingHints renderHints = GUIUtils.getQualityHints();
        g2.addRenderingHints(renderHints);
        g2.setColor(color);
        g2.fillRect(0, 0, BALOONTIP_WIDTH, BALOONTIP_HEIGHT);
        return image;
    }

    /**
     * 会話表示用フォントからアンカー表示用フォントを派生させる。
     * @param font 派生元フォント
     * @return 派生先フォント
     */
    private static Font deriveAnchorFont(Font font){
        float fontSize = font.getSize2D();
        float newSize = fontSize * ANCHOR_FONT_RATIO;
        return font.deriveFont(newSize);
    }

    /**
     * 会話表示用フォント設定からアンカー表示用フォント設定を派生させる。
     * @param info 派生元フォント設定
     * @return 派生先フォント設定
     */
    private static FontInfo deriveAnchorFontInfo(FontInfo info){
        Font newFont = deriveAnchorFont(info.getFont());
        FontInfo result = info.deriveFont(newFont);
        return result;
    }

    /**
     * 発言種別毎の色を返す。
     * @param type 発言種別
     * @return 色
     */
    public static Color getTypedColor(TalkType type){
        Color result;

        switch(type){
        case PUBLIC:   result = TalkDraw.COLOR_PUBLIC;   break;
        case WOLFONLY: result = TalkDraw.COLOR_WOLFONLY; break;
        case GRAVE:    result = TalkDraw.COLOR_GRAVE;    break;
        case PRIVATE:  result = TalkDraw.COLOR_PRIVATE;  break;
        default:       return null;
        }

        return result;
    }

    private final Talk talk;
    private Anchor showingAnchor;

    private final GlyphDraw caption;
    private BufferedImage faceImage;
    private final GlyphDraw dialog;
    private final List<AnchorDraw> anchorTalks = new LinkedList<AnchorDraw>();
    private Point imageOrigin;
    private Point dialogOrigin;
    private Point tipOrigin;
    private int baloonWidth;
    private int baloonHeight;

    private FontInfo anchorFontInfo;
    private DialogPref dialogPref;

    /**
     * コンストラクタ。
     * @param talk 一発言
     */
    public TalkDraw(Talk talk){
        this(talk, new DialogPref(), FontInfo.DEFAULT_FONTINFO);
        return;
    }

    /**
     * コンストラクタ。
     * @param talk 一発言
     * @param dialogPref 発言表示設定
     * @param fontInfo フォント設定
     */
    public TalkDraw(Talk talk, DialogPref dialogPref, FontInfo fontInfo){
        super(fontInfo);

        this.talk = talk;
        this.anchorFontInfo = deriveAnchorFontInfo(this.fontInfo);
        this.dialogPref = dialogPref;

        this.faceImage = getFaceImage();
        this.caption = new GlyphDraw(getCaptionString(), this.fontInfo);
        this.dialog  = new GlyphDraw(this.talk.getDialog(), this.fontInfo);

        setColorDesign();

        Period period = this.talk.getPeriod();
        List<Anchor> anchorList = Anchor.getAnchorList(this.talk.getDialog(),
                                                       period.getDay() );
        this.dialog.setAnchorSet(anchorList);

        return;
    }

    /**
     * 配色を設定する。
     */
    private void setColorDesign(){
        if(this.dialogPref.isSimpleMode()){
            this.caption.setColor(COLOR_SIMPLEFG);
        }else{
            this.caption.setColor(COLOR_CAPTIONFG);
        }

        this.dialog.setColor(COLOR_DIALOGFG);

        return;
    }

    /**
     * Talk取得。
     * @return Talkインスタンス
     */
    public Talk getTalk(){
        return this.talk;
    }

    /**
     * 顔イメージを返す。
     * @return 顔イメージ
     */
    private BufferedImage getFaceImage(){
        Village village = this.talk.getPeriod().getVillage();
        Avatar avatar = this.talk.getAvatar();

        boolean useBodyImage = this.dialogPref.useBodyImage();
        boolean useMonoImage = this.dialogPref.useMonoImage();

        BufferedImage image;
        if(this.talk.isGrave()){
            if(useMonoImage){
                if(useBodyImage){
                    image = village.getAvatarBodyMonoImage(avatar);
                }else{
                    image = village.getAvatarFaceMonoImage(avatar);
                }
            }else{
                if(useBodyImage){
                    image = village.getGraveBodyImage();
                }else{
                    image = village.getGraveImage();
                }
            }
        }else{
            if(useBodyImage){
                image = village.getAvatarBodyImage(avatar);
            }else{
                image = village.getAvatarFaceImage(avatar);
            }
        }

        return image;
    }

    /**
     * キャプション文字列を取得する。
     * @return キャプション文字列
     */
    private CharSequence getCaptionString(){
        StringBuilder result = new StringBuilder();

        Avatar avatar = this.talk.getAvatar();

        if(this.talk.hasTalkNo()){
            result.append(this.talk.getAnchorNotation_G()).append(' ');
        }
        result.append(avatar.getFullName()).append(' ');
        result.append(this.talk.getAnchorNotation());
        result.append('\n');

        DateFormat dform =
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                           DateFormat.MEDIUM);
        long epoch = this.talk.getTimeFromID();
        String decoded = dform.format(epoch);
        result.append(decoded);

        int count = this.talk.getTalkCount();
        if(count > 0){
            TalkType type = this.talk.getTalkType();
            result.append(" (").append(Talk.encodeColorName(type));
            result.append('#').append(count).append(')');
        }

        int charNum = this.talk.getTotalChars();
        if(charNum > 0){
            result.append(' ').append(charNum).append('字');
        }

        return result;
    }

    /**
     * 会話部背景色を返す。
     * @return 会話部背景色
     */
    protected Color getTalkBgColor(){
        if(this.dialogPref.isSimpleMode()) return COLOR_SIMPLEBG;

        TalkType type = this.talk.getTalkType();
        Color result = getTypedColor(type);

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
        if( ! this.dialogPref.isSimpleMode()){
            imageWidth  = this.faceImage.getWidth(null);
            imageHeight = this.faceImage.getHeight(null);
        }

        int tipWidth = BALOON_WOLFONLY.getWidth();

        int modWidth;
        int minWidth = imageWidth + tipWidth + BALOON_R * 2;
        if(newWidth < minWidth) modWidth = minWidth;
        else                    modWidth = newWidth;

        this.caption.setWidth(modWidth);
        int captionWidth  = this.caption.getWidth();
        int captionHeight = this.caption.getHeight() + CAPTION_DIALOG_GAP;

        this.dialog.setWidth(modWidth - minWidth);
        int dialogWidth  = this.dialog.getWidth();
        int dialogHeight = this.dialog.getHeight();

        if(this.dialogPref.alignBaloonWidth()){
            this.baloonWidth  = (modWidth - minWidth) + BALOON_R * 2;
        }else{
            this.baloonWidth  = dialogWidth + BALOON_R * 2;
        }
        this.baloonHeight = dialogHeight + BALOON_R * 2;

        int imageAndDialogWidth = imageWidth + tipWidth + this.baloonWidth;

        int totalWidth = Math.max(captionWidth, imageAndDialogWidth);

        int totalHeight = captionHeight;
        totalHeight += Math.max(imageHeight, this.baloonHeight);

        int imageYpos = captionHeight;
        int dialogYpos = captionHeight;
        int tipYpos = captionHeight;
        if(imageHeight < this.baloonHeight){
            imageYpos += (this.baloonHeight - imageHeight) / 2;
            tipYpos += (this.baloonHeight - BALOON_WOLFONLY.getHeight()) / 2;
            dialogYpos += BALOON_R;
        }else{
            dialogYpos += (imageHeight - this.baloonHeight) / 2 + BALOON_R;
            tipYpos += (imageHeight - BALOON_WOLFONLY.getHeight()) / 2;
        }

        this.imageOrigin = new Point(0, imageYpos);
        this.caption.setPos(this.bounds.x + 0, this.bounds.y + 0);
        this.dialogOrigin =
                new Point(imageWidth+tipWidth+BALOON_R, dialogYpos);
        this.dialog.setPos(this.bounds.x + imageWidth+tipWidth+BALOON_R,
                           this.bounds.y + dialogYpos);
        this.tipOrigin = new Point(imageWidth, tipYpos);

        for(AnchorDraw anchorDraw : this.anchorTalks){
            anchorDraw.setWidth(modWidth - OFFSET_ANCHOR);
            totalHeight += anchorDraw.getHeight();
        }

        if(   this.dialogPref.isSimpleMode()
           || this.dialogPref.alignBaloonWidth() ){
            this.bounds.width = newWidth;
        }else{
            this.bounds.width = totalWidth;
        }
        this.bounds.height = UPPER_MARGIN + totalHeight + UNDER_MARGIN;

        return this.bounds;
    }

    /**
     * {@inheritDoc}
     * @param xPos {@inheritDoc}
     * @param yPos {@inheritDoc}
     */
    @Override
    public void setPos(int xPos, int yPos){
        super.setPos(xPos, yPos);
        this.caption.setPos(this.bounds.x, this.bounds.y + UPPER_MARGIN);
        this.dialog.setPos(this.bounds.x + this.dialogOrigin.x,
                           this.bounds.y + this.dialogOrigin.y
                                         + UPPER_MARGIN);
        return;
    }

    /**
     * アイコンイメージとフキダシを繋ぐ補助イメージを返す。
     * @return 補助イメージ
     */
    private BufferedImage getTipImage(){
        BufferedImage tip;

        TalkType type = this.talk.getTalkType();

        if(this.dialogPref.isSimpleMode()){
            switch(type){
            case PUBLIC:   tip = SQUARE_PUBLIC;   break;
            case WOLFONLY: tip = SQUARE_WOLFONLY; break;
            case GRAVE:    tip = SQUARE_GRAVE;    break;
            case PRIVATE:  tip = SQUARE_PRIVATE;  break;
            default:
                assert false;
                tip = null;
                break;
            }
        }else{
            switch(type){
            case PUBLIC:   tip = BALOON_PUBLIC;   break;
            case WOLFONLY: tip = BALOON_WOLFONLY; break;
            case GRAVE:    tip = BALOON_GRAVE;    break;
            case PRIVATE:  tip = BALOON_PRIVATE;  break;
            default:
                assert false;
                tip = null;
                break;
            }
        }

        return tip;
    }

    /**
     * {@inheritDoc}
     * @param g {@inheritDoc}
     */
    public void paint(Graphics2D g){
        final int xPos = this.bounds.x;
        final int yPos = this.bounds.y + UPPER_MARGIN;

        this.caption.paint(g);

        if(this.dialogPref.isSimpleMode() ){
            g.drawLine(xPos,                     this.bounds.y,
                       xPos + this.bounds.width, this.bounds.y );
        }else{
            g.drawImage(this.faceImage,
                        xPos + this.imageOrigin.x,
                        yPos + this.imageOrigin.y,
                        null );
        }

        BufferedImage tip = getTipImage();
        g.drawImage(tip,
                    xPos + this.tipOrigin.x,
                    yPos + this.tipOrigin.y,
                    null );

        g.setColor(getTalkBgColor());
        g.fillRoundRect(
                xPos + this.dialogOrigin.x - BALOON_R,
                yPos + this.dialogOrigin.y - BALOON_R,
                this.baloonWidth,
                this.baloonHeight,
                BALOON_R,
                BALOON_R );

        this.dialog.paint(g);

        int anchorX = xPos + OFFSET_ANCHOR;
        int anchorY = yPos + this.dialogOrigin.y + baloonHeight;

        for(AnchorDraw anchorDraw : this.anchorTalks){
            anchorDraw.setPos(anchorX, anchorY);
            anchorDraw.paint(g);
            anchorY += anchorDraw.getHeight();
        }

        return;
    }

    /**
     * {@inheritDoc}
     * @param fontInfo {@inheritDoc}
     */
    @Override
    public void setFontInfo(FontInfo fontInfo){
        super.setFontInfo(fontInfo);

        this.anchorFontInfo = deriveAnchorFontInfo(this.fontInfo);

        this.caption.setFontInfo(this.fontInfo);
        this.dialog .setFontInfo(this.fontInfo);

        for(AnchorDraw anchorDraw : this.anchorTalks){
            anchorDraw.setFontInfo(this.anchorFontInfo);
        }

        recalcBounds();

        return;
    }

    /**
     * 発言設定を更新する。
     * @param pref 発言設定
     */
    public void setDialogPref(DialogPref pref){
        this.dialogPref = pref;
        this.faceImage = getFaceImage();

        for(AnchorDraw anchorDraw : this.anchorTalks){
            anchorDraw.setDialogPref(this.dialogPref);
        }

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
        for(AnchorDraw anchorDraw : this.anchorTalks){
            anchorDraw.drag(from, to);
        }
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
        this.dialog .appendSelected(appendable);

        for(AnchorDraw anchorDraw : this.anchorTalks){
            anchorDraw.appendSelected(appendable);
        }

        return appendable;
    }

    /**
     * {@inheritDoc}
     */
    public void clearSelect(){
        this.caption.clearSelect();
        this.dialog.clearSelect();
        for(AnchorDraw anchorDraw : this.anchorTalks){
            anchorDraw.clearSelect();
        }
        return;
    }

    /**
     * 与えられた座標にアンカー文字列が存在すればAnchorを返す。
     * @param pt 座標
     * @return アンカー
     */
    public Anchor getAnchor(Point pt){
        Anchor result = this.dialog.getAnchor(pt);
        return result;
    }

    /**
     * アンカーを展開表示する。
     * アンカーにnullを指定すればアンカー表示は非表示となる。
     * @param anchor アンカー
     * @param talkList アンカーの示す一連のTalk
     */
    public void showAnchorTalks(Anchor anchor, List<Talk> talkList){
        if(anchor == null || this.showingAnchor == anchor){
            this.showingAnchor = null;
            this.anchorTalks.clear();
            recalcBounds();
            return;
        }

        this.showingAnchor = anchor;

        this.anchorTalks.clear();
        for(Talk anchorTalk : talkList){
            AnchorDraw anchorDraw =
                    new AnchorDraw(anchorTalk,
                                   this.dialogPref,
                                   this.anchorFontInfo );
            this.anchorTalks.add(anchorDraw);
        }

        recalcBounds();

        return;
    }

    /**
     * 与えられた座標に検索マッチ文字列があればそのインデックスを返す。
     * @param pt 座標
     * @return 検索マッチインデックス
     */
    public int getRegexMatchIndex(Point pt){
        int index = this.dialog.getRegexMatchIndex(pt);
        return index;
    }

    /**
     * 検索文字列パターンを設定する。
     * @param searchRegex パターン
     * @return ヒット数
     */
    public int setRegex(Pattern searchRegex){
        int total = 0;

        total += this.dialog.setRegex(searchRegex);
/*
        for(AnchorDraw anchorDraw : this.anchorTalks){
            total += anchorDraw.setRegex(searchRegex);
        }
*/ // TODO よくわからんので保留
        return total;
    }

    /**
     * 検索ハイライトインデックスを返す。
     * @return 検索ハイライトインデックス。見つからなければ-1。
     */
    public int getHotTargetIndex(){
        return this.dialog.getHotTargetIndex();
    }

    /**
     * 検索ハイライトを設定する。
     * @param index ハイライトインデックス。負ならハイライト全クリア。
     */
    public void setHotTargetIndex(int index){
        this.dialog.setHotTargetIndex(index);
        return;
    }

    /**
     * 検索一致件数を返す。
     * @return 検索一致件数
     */
    public int getRegexMatches(){
        return this.dialog.getRegexMatches();
    }

    /**
     * 特別な検索ハイライト描画をクリアする。
     */
    public void clearHotTarget(){
        this.dialog.clearHotTarget();
        return;
    }

    /**
     * 特別な検索ハイライト領域の寸法を返す。
     * @return ハイライト領域寸法
     */
    public Rectangle getHotTargetRectangle(){
        return this.dialog.getHotTargetRectangle();
    }

}
