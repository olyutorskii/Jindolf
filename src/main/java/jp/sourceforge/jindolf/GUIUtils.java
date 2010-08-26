/*
 * GUI utilities
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * GUI関連のユーティリティクラス。
 */
public final class GUIUtils{

    private static final String RES_LOGOICON =
            "resources/image/logo.png";
    private static final String RES_WINDOWICON =
            "resources/image/winicon.png";
    private static final String RES_WWWICON =
            "resources/image/www.png";
    private static final String RES_NOIMAGE =
            "resources/image/noimage.png";
    private static BufferedImage logoImage;
    private static Icon logoIcon;
    private static BufferedImage windowIconImage;
    private static Icon wwwIcon;
    private static BufferedImage noImage;

    private static final RenderingHints HINTS_QUALITY;
    private static final RenderingHints HINTS_SPEEDY;

    private static final BufferedImageOp OP_MONOIMG;

    private static final Runnable TASK_NOTHING = new Runnable(){
        /** 何もしない。 */
        public void run(){}
    };

    static{
        HINTS_QUALITY = new RenderingHints(null);
        HINTS_SPEEDY  = new RenderingHints(null);

        HINTS_QUALITY.put(RenderingHints.KEY_ANTIALIASING,
                          RenderingHints.VALUE_ANTIALIAS_ON);
        HINTS_SPEEDY.put(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_OFF);

        HINTS_QUALITY.put(RenderingHints.KEY_RENDERING,
                          RenderingHints.VALUE_RENDER_QUALITY);
        HINTS_SPEEDY.put(RenderingHints.KEY_RENDERING,
                         RenderingHints.VALUE_RENDER_SPEED);

        HINTS_QUALITY.put(RenderingHints.KEY_DITHERING,
                          RenderingHints.VALUE_DITHER_ENABLE);
        HINTS_SPEEDY.put(RenderingHints.KEY_DITHERING,
                         RenderingHints.VALUE_DITHER_DISABLE);

        HINTS_QUALITY.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        HINTS_SPEEDY.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                         RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        HINTS_QUALITY.put(RenderingHints.KEY_FRACTIONALMETRICS,
                          RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        HINTS_SPEEDY.put(RenderingHints.KEY_FRACTIONALMETRICS,
                         RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

        HINTS_QUALITY.put(RenderingHints.KEY_INTERPOLATION,
                          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        HINTS_SPEEDY.put(RenderingHints.KEY_INTERPOLATION,
                         RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        HINTS_QUALITY.put(RenderingHints.KEY_ALPHA_INTERPOLATION,
                          RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        HINTS_SPEEDY.put(RenderingHints.KEY_ALPHA_INTERPOLATION,
                         RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

        HINTS_QUALITY.put(RenderingHints.KEY_COLOR_RENDERING,
                          RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        HINTS_SPEEDY.put(RenderingHints.KEY_COLOR_RENDERING,
                         RenderingHints.VALUE_COLOR_RENDER_SPEED);

        HINTS_QUALITY.put(RenderingHints.KEY_STROKE_CONTROL,
                          RenderingHints.VALUE_STROKE_PURE);
        HINTS_SPEEDY.put(RenderingHints.KEY_STROKE_CONTROL,
                         RenderingHints.VALUE_STROKE_NORMALIZE);
    }

    static{
        ColorSpace mono = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        OP_MONOIMG = new ColorConvertOp(mono, null);
    }

    /**
     * 描画品質優先の描画ヒントを返す。
     * @return 描画ヒント
     */
    public static RenderingHints getQualityHints(){
        return HINTS_QUALITY;
    }

    /**
     * リソース名からイメージを取得する。
     * @param resource リソース名
     * @return イメージ
     * @throws java.io.IOException 入力エラー
     */
    public static BufferedImage loadImageFromResource(String resource)
            throws IOException{
        BufferedImage result;

        URL url = Jindolf.getResource(resource);
        result = ImageIO.read(url);

        return result;
    }

    /**
     * ロゴイメージを得る。
     * @return ロゴイメージ
     */
    public static BufferedImage getLogoImage(){
        if(logoImage != null){
            return logoImage;
        }

        BufferedImage image;
        try{
            image = loadImageFromResource(RES_LOGOICON);
        }catch(IOException e){
            Jindolf.logger().warn("ロゴイメージの取得に失敗しました", e);
            image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            // TODO デカく "狼" とでも描くか？
        }

        logoImage = image;

        return logoImage;
    }

    /**
     * 各種ウィンドウのアイコンイメージを得る。
     * @return アイコンイメージ
     */
    public static BufferedImage getWindowIconImage(){
        if(windowIconImage != null){
            return windowIconImage;
        }

        BufferedImage image;
        try{
            image = loadImageFromResource(RES_WINDOWICON);
        }catch(IOException e){
            Jindolf.logger().warn("アイコンイメージの取得に失敗しました", e);
            image = getLogoImage();
        }

        windowIconImage = image;

        return windowIconImage;
    }

    /**
     * ロゴアイコンを得る。
     * @return ロゴアイコン
     */
    public static Icon getLogoIcon(){
        if(logoIcon != null){
            return logoIcon;
        }

        Icon icon = new ImageIcon(getLogoImage());

        logoIcon = icon;

        return logoIcon;
    }

    /**
     * WWWアイコンを得る。
     * @return WWWアイコン
     */
    public static Icon getWWWIcon(){
        if(wwwIcon != null){
            return wwwIcon;
        }

        URL url = Jindolf.getResource(RES_WWWICON);
        wwwIcon = new ImageIcon(url);

        return wwwIcon;
    }

    /**
     * NoImageイメージを得る。
     * @return NoImageイメージ
     */
    public static BufferedImage getNoImage(){
        if(noImage != null){
            return noImage;
        }

        URL url = Jindolf.getResource(RES_NOIMAGE);
        try{
            noImage = ImageIO.read(url);
        }catch(IOException e){
            assert false;
            noImage = getLogoImage();
        }

        return noImage;
    }

    /**
     * AWTディスパッチイベント処理を促す。
     */
    public static void dispatchEmptyAWTEvent(){
        if(SwingUtilities.isEventDispatchThread()){
            return;
        }

        try{
            SwingUtilities.invokeAndWait(TASK_NOTHING);
        }catch(InterruptedException e){
            // IGNORE
        }catch(InvocationTargetException e){
            // IGNORE
        }

        return;
    }

    /**
     * 矩形と点座標の相対関係を判定する。
     * ・矩形に点座標が含まれればSwingContants.CENTER
     * ・矩形の上辺より上に点座標が位置すればSwingContants.NORTH
     * ・矩形の下辺より下に点座標が位置すればSwingContants.SOUTH
     * ・矩形の上辺と下辺内に収まるが右辺からはみ出すときはSwingContants.EAST
     * ・矩形の上辺と下辺内に収まるが左辺からはみ出すときはSwingContants.WEST
     * @param rect 矩形
     * @param pt 点座標
     * @return 判定結果
     */
    public static int getDirection(Rectangle rect, Point pt){
        if(pt.y < rect.y){
            return SwingConstants.NORTH;
        }
        if(rect.y + rect.height <= pt.y){
            return SwingConstants.SOUTH;
        }
        if(pt.x < rect.x){
            return SwingConstants.EAST;
        }
        if(rect.x + rect.width <= pt.x){
            return SwingConstants.WEST;
        }
        return SwingConstants.CENTER;
    }

    /**
     * ウィンドウ属性を設定する。
     * @param window ウィンドウ
     * @param isResizable リサイズ可ならtrue
     * @param isDynamic リサイズに伴う再描画ならtrue
     * @param isAutoLocation 自動位置決め機構を使うならtrue
     */
    public static void modifyWindowAttributes(Window window,
                                                 boolean isResizable,
                                                 boolean isDynamic,
                                                 boolean isAutoLocation){
        Toolkit kit = window.getToolkit();
        kit.setDynamicLayout(isDynamic);

        window.setLocationByPlatform(isAutoLocation);

        if(window instanceof Frame){
            Frame frame = (Frame) window;
            frame.setIconImage(getWindowIconImage());
            frame.setResizable(isResizable);
        }else if(window instanceof Dialog){
            Dialog dialog = (Dialog) window;
            dialog.setResizable(isResizable);
        }

        return;
    }

    /**
     * コンポーネントの既存ボーダー内側にマージンをもうける。
     * @param comp 対象コンポーネント
     * @param top 上マージン
     * @param left 左マージン
     * @param bottom 下マージン
     * @param right 右マージン
     */
    public static void addMargin(Component comp,
                                  int top, int left, int bottom, int right){
        if( ! (comp instanceof JComponent) ) return;
        JComponent jcomp = (JComponent) comp;

        Border outer = jcomp.getBorder();
        Border inner =
                BorderFactory.createEmptyBorder(top, left, bottom, right);

        Border border;
        if(outer == null){
            border = inner;
        }else{
            border = BorderFactory.createCompoundBorder(outer, inner);
        }

        jcomp.setBorder(border);

        return;
    }

    /**
     * 独自ロガーにエラーや例外を吐く、
     * カスタム化されたイベントキューに差し替える。
     */
    public static void replaceEventQueue(){
        Toolkit kit = Toolkit.getDefaultToolkit();
        EventQueue oldQueue = kit.getSystemEventQueue();
        EventQueue newQueue = new EventQueue(){
            private static final String FATALMSG =
                    "イベントディスパッチ中に異常が起きました。";
            @Override
            protected void dispatchEvent(AWTEvent event){
                try{
                    super.dispatchEvent(event);
                }catch(RuntimeException e){
                    Jindolf.logger().fatal(FATALMSG, e);
                    throw e;
                }catch(Exception e){
                    Jindolf.logger().fatal(FATALMSG, e);
                }catch(Error e){
                    Jindolf.logger().fatal(FATALMSG, e);
                    throw e;
                }
                // TODO Toolkit#beep()もするべきか
                // TODO モーダルダイアログを出すべきか
                // TODO 標準エラー出力抑止オプションを用意すべきか
                // TODO セキュリティバイパス
                return;
            }
        };
        oldQueue.push(newQueue);
        return;
    }

    /**
     * 任意のイメージを多階調モノクロ化する。
     * 寸法は変わらない。
     * @param image イメージ
     * @return モノクロ化イメージ
     */
    public static BufferedImage createMonoImage(BufferedImage image){
        BufferedImage result;
        result = OP_MONOIMG.filter(image, null);
        return result;
    }

    /**
     * 隠れコンストラクタ。
     */
    private GUIUtils(){
        assert false;
        throw new AssertionError();
    }

}
