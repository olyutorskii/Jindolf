/*
 * application settings
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import jp.sfjp.jindolf.data.Avatar;
import jp.sfjp.jindolf.data.DialogPref;
import jp.sfjp.jindolf.glyph.Font2Json;
import jp.sfjp.jindolf.glyph.FontInfo;
import jp.sfjp.jindolf.net.ProxyInfo;
import jp.sfjp.jindolf.view.AvatarPics;
import jp.sfjp.jindolf.view.LocalAvatarImg;
import jp.sourceforge.jovsonz.JsBoolean;
import jp.sourceforge.jovsonz.JsObject;
import jp.sourceforge.jovsonz.JsPair;
import jp.sourceforge.jovsonz.JsString;
import jp.sourceforge.jovsonz.JsTypes;
import jp.sourceforge.jovsonz.JsValue;

/**
 * アプリケーションの各種設定。
 */
public class AppSetting{

    // デフォルトのウィンドウサイズ
    private static final int DEF_WIDTH  = 800;
    private static final int DEF_HEIGHT = 600;

    private static final String HASH_FONT        = "font";
    private static final String HASH_USEBODYICON = "useBodyIcon";
    private static final String HASH_USEMONOTOMB = "useMonoTomb";
    private static final String HASH_SIMPLEMODE  = "isSimpleMode";
    private static final String HASH_ALIGNBALOON = "alignBaloonWidth";
    private static final String HASH_PROXY       = "proxy";

    private static final String MSG_NOIMG =
            "画像ファイル{0}が読み込めないため"
            + "{1}の表示に代替イメージを使います。";

    private static final Logger LOGGER = Logger.getAnonymousLogger();


    private final OptionInfo optInfo;
    private final ConfigStore configStore;
    private final Rectangle frameRect;

    private FontInfo fontInfo;

    private ProxyInfo proxyInfo = ProxyInfo.DEFAULT;

    private DialogPref dialogPref = new DialogPref();

    private JsValue loadedNetConfig;
    private JsValue loadedTalkConfig;

    private final Map<String, BufferedImage> avatarFaceMap = new HashMap<>();
    private final Map<String, BufferedImage> avatarBodyMap = new HashMap<>();


    /**
     * コンストラクタ。
     * @param info コマンドライン引数
     */
    public AppSetting(OptionInfo info){
        super();

        this.optInfo = info;
        this.configStore = parseConfigStore(this.optInfo);
        this.frameRect = parseGeometrySetting(this.optInfo);

        return;
    }


    /**
     * 設定格納ディレクトリ関係の解析。
     * @param option コマンドラインオプション情報
     * @return 設定ディレクトリ情報
     */
    private static ConfigStore parseConfigStore(OptionInfo option){
        CmdOption opt = option.getExclusiveOption(CmdOption.OPT_CONFDIR,
                                                  CmdOption.OPT_NOCONF );

        boolean useConfig;
        boolean isImplicitPath;
        Path configPath;

        if(opt == CmdOption.OPT_NOCONF){
            useConfig = false;
            isImplicitPath = true;
            configPath = null;
        }else if(opt == CmdOption.OPT_CONFDIR){
            useConfig = true;
            isImplicitPath = false;
            String optArg = option.getStringArg(opt);
            configPath = Paths.get(optArg);
            configPath = configPath.toAbsolutePath();
        }else{
            useConfig = true;
            isImplicitPath = true;
            configPath = ConfigDirUtils.getDefaultConfDirPath();
        }

        ConfigStore result =
                new ConfigStore(useConfig, isImplicitPath, configPath);

        return result;
    }

    /**
     * ウィンドウジオメトリ関係の設定。
     * @param option コマンドラインオプション情報
     * @return ウィンドウ矩形。
     */
    private static Rectangle parseGeometrySetting(OptionInfo option){
        Rectangle result = new Rectangle(Integer.MIN_VALUE,
                                         Integer.MIN_VALUE,
                                         DEF_WIDTH,
                                         DEF_HEIGHT );

        Integer ival;

        ival = option.initialFrameWidth();
        if(ival != null) result.width = ival;

        ival = option.initialFrameHeight();
        if(ival != null) result.height = ival;

        ival = option.initialFrameXpos();
        if(ival != null) result.x = ival;

        ival = option.initialFrameYpos();
        if(ival != null) result.y = ival;

        return result;
    }


    /**
     * フォントオプションの解析。
     * @param baseFont 元のフォント設定。
     * @return コマンドライン設定で補正されたフォント設定
     */
    private FontInfo parseFontOption(FontInfo baseFont){
        FontInfo result;

        String fontName = this.optInfo.getStringArg(CmdOption.OPT_INITFONT);
        if(fontName != null){
            Font font = Font.decode(fontName);
            result = baseFont.deriveFont(font);
        }else{
            result = baseFont;
        }

        Boolean useAntiAlias =
                this.optInfo.getBooleanArg(CmdOption.OPT_ANTIALIAS);
        if(useAntiAlias == null){
            useAntiAlias = baseFont.isAntiAliased();
        }

        Boolean useFractional =
                this.optInfo.getBooleanArg(CmdOption.OPT_FRACTIONAL);
        if(useFractional == null){
            useFractional = baseFont.usesFractionalMetrics();
        }

        result = result.deriveRenderContext(useAntiAlias,
                                            useFractional );

        return result;
    }

    /**
     * コマンドラインオプション情報を返す。
     * @return コマンドラインオプション情報
     */
    public OptionInfo getOptionInfo(){
        return this.optInfo;
    }

    /**
     * 設定格納情報を返す。
     * @return 設定格納情報
     */
    public ConfigStore getConfigStore(){
        return this.configStore;
    }

    /**
     * 初期のフレーム幅を返す。
     * @return 初期のフレーム幅
     */
    public int initialFrameWidth(){
        int width = this.frameRect.width;
        return width;
    }

    /**
     * 初期のフレーム高を返す。
     * @return 初期のフレーム高
     */
    public int initialFrameHeight(){
        int height = this.frameRect.height;
        return height;
    }

    /**
     * 初期のフレーム位置のX座標を返す。
     * 特に指示されていなければInteger.MIN_VALUEを返す。
     * @return 初期のフレーム位置のX座標
     */
    public int initialFrameXpos(){
        int xPos = this.frameRect.x;
        return xPos;
    }

    /**
     * 初期のフレーム位置のY座標を返す。
     * 特に指示されていなければInteger.MIN_VALUEを返す。
     * @return 初期のフレーム位置のY座標
     */
    public int initialFrameYpos(){
        int yPos = this.frameRect.y;
        return yPos;
    }

    /**
     * フォント設定を返す。
     * @return フォント設定
     */
    public FontInfo getFontInfo(){
        if(this.fontInfo == null){
            this.fontInfo = parseFontOption(FontInfo.DEFAULT_FONTINFO);
        }
        return this.fontInfo;
    }

    /**
     * フォント設定を更新する。
     * @param fontInfo フォント設定
     */
    public void setFontInfo(FontInfo fontInfo){
        this.fontInfo = fontInfo;
        return;
    }

    /**
     * プロクシ設定を返す。
     * @return プロクシ設定
     */
    public ProxyInfo getProxyInfo(){
        return this.proxyInfo;
    }

    /**
     * プロクシ設定を更新する。
     * @param proxyInfo プロクシ設定。nullならプロクシなしと解釈される。
     */
    public void setProxyInfo(ProxyInfo proxyInfo){
        if(proxyInfo == null) this.proxyInfo = ProxyInfo.DEFAULT;
        else                  this.proxyInfo = proxyInfo;
        return;
    }

    /**
     * 発言表示設定を返す。
     * @return 表示設定
     */
    public DialogPref getDialogPref(){
        return this.dialogPref;
    }

    /**
     * 発言表示設定を返す。
     * @param pref 表示設定
     */
    public void setDialogPref(DialogPref pref){
        if(pref == null) this.dialogPref = new DialogPref();
        else             this.dialogPref = pref;
        return;
    }

    /**
     * ネットワーク設定をロードする。
     */
    private void loadNetConfig(){
        JsObject root = this.configStore.loadNetConfig();
        if(root == null) return;
        this.loadedNetConfig = root;

        JsValue value = root.getValue(HASH_PROXY);
        if( ! (value instanceof JsObject) ) return;
        JsObject proxy = (JsObject) value;

        ProxyInfo info = ProxyInfo.decodeJson(proxy);

        setProxyInfo(info);

        return;
    }

    /**
     * 会話表示設定をロードする。
     */
    private void loadTalkConfig(){
        JsObject root = this.configStore.loadTalkConfig();
        if(root == null) return;
        this.loadedTalkConfig = root;

        JsValue value = root.getValue(HASH_FONT);
        if(value instanceof JsObject){
            JsObject font = (JsObject) value;
            FontInfo info = Font2Json.decodeJson(font);
            info = parseFontOption(info);
            setFontInfo(info);
        }

        DialogPref pref = new DialogPref();
        JsBoolean boolValue;
        value = root.getValue(HASH_USEBODYICON);
        if(value instanceof JsBoolean){
            boolValue = (JsBoolean) value;
            pref.setBodyImageSetting(boolValue.booleanValue());
        }
        value = root.getValue(HASH_USEMONOTOMB);
        if(value instanceof JsBoolean){
            boolValue = (JsBoolean) value;
            pref.setMonoImageSetting(boolValue.booleanValue());
        }
        value = root.getValue(HASH_SIMPLEMODE);
        if(value instanceof JsBoolean){
            boolValue = (JsBoolean) value;
            pref.setSimpleMode(boolValue.booleanValue());
        }
        value = root.getValue(HASH_ALIGNBALOON);
        if(value instanceof JsBoolean){
            boolValue = (JsBoolean) value;
            pref.setAlignBalooonWidthSetting(boolValue.booleanValue());
        }
        setDialogPref(pref);

        return;
    }

    /**
     * JSONをパースしAvatar-Image間マップに反映させる。
     *
     * @param json JSON構造
     * @param map マップ
     */
    private void parseImgMap(JsObject json, Map<String, BufferedImage> map){
        Path imgDir = this.configStore.getLocalImgDir();

        List<JsPair> pairList = json.getPairList();
        for(JsPair pair : pairList){
            String avatarId = pair.getName();
            JsValue value = pair.getValue();

            if(value.getJsTypes() != JsTypes.STRING) continue;
            JsString sVal = (JsString)value;
            String imgName = sVal.toRawString();

            Path imgPath = Paths.get(imgName);
            Path full = imgDir.resolve(imgPath);
            File file = full.toFile();
            if(        ! file.isAbsolute()
                    || ! file.exists()
                    || ! file.isFile()
                    || ! file.canRead() ){
                String msg = MessageFormat.format(
                        MSG_NOIMG, file.getPath(), avatarId
                );
                LOGGER.info(msg);
                continue;
            }

            BufferedImage image;
            try {
                image = ImageIO.read(file);
            }catch(IOException e){
                String msg = MessageFormat.format(
                        MSG_NOIMG, file.getPath(), avatarId
                );
                LOGGER.info(msg);
                continue;
            }

            map.put(avatarId, image);
        }

        return;
    }

    /**
     * ローカル画像設定をロードする。
     */
    private void loadLocalImageConfig(){
        JsObject root = this.configStore.loadLocalImgConfig();
        if(root == null) return;

        JsValue faceConfig = root.getValue("avatarFace");
        JsValue bodyConfig = root.getValue("avatarBody");
        if(faceConfig.getJsTypes() != JsTypes.OBJECT) return;
        if(bodyConfig.getJsTypes() != JsTypes.OBJECT) return;

        JsObject jsonFace = (JsObject) faceConfig;
        parseImgMap(jsonFace, this.avatarFaceMap);


        JsObject jsonBody = (JsObject) bodyConfig;
        parseImgMap(jsonBody, this.avatarBodyMap);

        return;
    }

    /**
     * ローカル代替イメージを画像キャッシュに反映させる。
     *
     * @param avatarPics 画像キャッシュ
     */
    public void applyLocalImage(AvatarPics avatarPics){
        BufferedImage graveImage     = this.avatarFaceMap.get("tomb");
        BufferedImage graveBodyImage = this.avatarBodyMap.get("tomb");

        if(graveImage == null){
            graveImage = LocalAvatarImg.getGraveImage();
        }
        if(graveBodyImage == null){
            graveBodyImage = LocalAvatarImg.getGraveBodyImage();
        }

        avatarPics.setGraveImage(graveImage);
        avatarPics.setGraveBodyImage(graveBodyImage);

        for(Avatar avatar : Avatar.getPredefinedAvatarList()){
            String avatarId = avatar.getIdentifier();

            BufferedImage faceImage = this.avatarFaceMap.get(avatarId);
            BufferedImage bodyImage = this.avatarBodyMap.get(avatarId);

            if(faceImage == null){
                faceImage = LocalAvatarImg.getAvatarFaceImage(avatarId);
            }
            if(bodyImage == null){
                bodyImage = LocalAvatarImg.getAvatarBodyImage(avatarId);
            }

            avatarPics.setAvatarFaceImage(avatar, faceImage);
            avatarPics.setAvatarBodyImage(avatar, bodyImage);
        }

        return;
    }

    /**
     * ネットワーク設定をセーブする。
     */
    private void saveNetConfig(){
        if( ! getConfigStore().useStoreFile() ) return;

        JsObject root = new JsObject();
        JsObject proxy = ProxyInfo.buildJson(getProxyInfo());
        root.putValue(HASH_PROXY, proxy);

        if(this.loadedNetConfig != null){
            if(this.loadedNetConfig.equals(root)) return;
        }

        this.configStore.saveNetConfig(root);

        return;
    }

    /**
     * 会話表示設定をセーブする。
     */
    private void saveTalkConfig(){
        if( ! getConfigStore().useStoreFile() ) return;

        JsObject root = new JsObject();

        JsObject font = Font2Json.buildJson(getFontInfo());
        root.putValue(HASH_FONT, font);

        DialogPref pref = getDialogPref();
        JsPair useBodyIcon =
                new JsPair(HASH_USEBODYICON, pref.useBodyImage());
        JsPair useMonoTomb =
                new JsPair(HASH_USEMONOTOMB, pref.useMonoImage());
        JsPair isSimple =
                new JsPair(HASH_SIMPLEMODE, pref.isSimpleMode());
        JsPair alignBaloon =
                new JsPair(HASH_ALIGNBALOON, pref.alignBaloonWidth());
        root.putPair(useBodyIcon);
        root.putPair(useMonoTomb);
        root.putPair(isSimple);
        root.putPair(alignBaloon);

        if(this.loadedTalkConfig != null){
            if(this.loadedTalkConfig.equals(root)) return;
        }

        this.configStore.saveTalkConfig(root);

        return;
    }

    /**
     * 各種設定を設定格納ディレクトリからロードする。
     */
    public void loadConfig(){
        loadNetConfig();
        loadTalkConfig();
        loadLocalImageConfig();
        return;
    }

    /**
     * 各種設定を設定格納ディレクトリへセーブする。
     */
    public void saveConfig(){
        saveNetConfig();
        saveTalkConfig();
        return;
    }

}
